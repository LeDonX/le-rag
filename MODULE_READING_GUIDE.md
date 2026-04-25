# rag-ai 分模块阅读指南

> 目标：不是解释“这个项目有什么功能”，而是告诉你 **代码该怎么读**。  
> 阅读方式：按 **后端 / 前端 / SuperSQL** 三个部分拆开，先抓入口，再抓主链路，最后补治理与边角。

---

## 1. 推荐阅读策略

如果你第一次接手这个仓库，不建议从根目录开始逐文件乱看。更高效的方式是：

1. **先看主应用后端**：搞清楚接口、配置、主业务链路
2. **再看主应用前端**：搞清楚页面与接口的映射关系
3. **最后看 SuperSQL**：理解它是独立子项目，还是主应用里的能力来源

一句话概括：

> **先理解主产品，再理解支撑它的子系统。**

---

## 2. 后端阅读指南（`rag-ai-back`）

后端是整个项目的核心，建议按下面顺序读。

### 2.1 第一步：先看入口和总配置

先读这几个文件：

- `rag-ai-back/src/main/java/com/le/rag/LeDonRagAiApplication.java`
- `rag-ai-back/src/main/resources/application.yml`
- `rag-ai-back/pom.xml`

你要从这里确认几件事：

- 这是一个 **Spring Boot 3 + Java 17** 应用
- 开了 **定时任务**（`@EnableScheduling`）
- 当前主配置依赖的是：
  - MySQL
  - Redis
  - Milvus
  - DashScope
  - 本地文件存储

这一步的目标不是看细节，而是建立对后端“运行底座”的整体印象。

---

### 2.2 第二步：看接口总入口，建立功能地图

接着读 `controller/` 目录：

- `AiRagController.java`
- `ChatController.java`
- `KnowledgeController.java`
- `DrawImageController.java`
- `UserController.java`
- `SensitiveWordController.java`
- `SensitiveCategoryController.java`
- `LogInfoController.java`
- `WordFrequencyController.java`

这一步你要做的不是深入实现，而是先回答：

- 后端到底暴露了哪些 API 面？
- 哪些是主业务？
- 哪些是治理能力？
- 哪些只是后台管理接口？

读完后你应该能形成这个大图：

- **AI 主链路**：RAG 问答、普通聊天、AI 绘图
- **知识库链路**：文件上传、查询、删除、下载
- **治理链路**：敏感词、日志、热词分析
- **账户链路**：登录、注册、用户管理

---

### 2.3 第三步：重点读主链路——知识库上传 + RAG 问答

如果你只想快速搞懂项目“最核心的价值”，优先读下面这组文件：

#### RAG 问答链路

- `controller/AiRagController.java`
- `advisors/MetadataAwareQuestionAnswerAdvisor.java`
- `tools/RagTool.java`
- `context/BaseContext.java`

建议重点看：

- 请求是怎么进来的
- `sources` 是怎么参与向量过滤的
- Prompt 是怎么组装的
- 聊天记忆是怎么挂上的
- 检索文档是怎么拼进上下文的
- 为什么会接入 `RagTool`

#### 知识库入库链路

- `controller/KnowledgeController.java`
- `service/impl/AliOssFileServiceImpl.java`
- `utils/AliOssUtil.java`
- `entity/AliOssFile.java`
- `mapper/AliOssFileMapper.java`

这里要看清楚真实流程：

1. 文件上传
2. 本地保存
3. Tika 解析
4. 文本切片
5. 向量入库
6. MySQL 记录文件元数据

这一组文件读懂了，项目主价值基本就读懂了一半。

---

### 2.4 第四步：看认证与全局横切逻辑

接着读这些文件：

- `config/ApplicationConfig.java`
- `common/JwtTokenUserInterceptor.java`
- `config/JwtProperties.java`
- `utils/JwtUtil.java`
- `aop/LoggingAspect.java`
- `annotation/Loggable.java`
- `exception/GlobalExceptionHandler.java`

这一步你要理解的是：

- 登录态是怎么进来的
- 请求是怎么被拦截的
- 用户 ID 是怎么传到业务层的
- 哪些接口会被自动记录日志
- 全局异常怎么处理

这里能帮助你看清项目“像不像一个正式系统”，而不是只有业务代码。

---

### 2.5 第五步：看运营与治理链路

建议按下面顺序读：

- `aop/LoggingAspect.java`
- `entity/LogInfo.java`
- `controller/LogInfoController.java`
- `scheduled/TaskJobScheduled.java`
- `entity/WordFrequency.java`
- `controller/WordFrequencyController.java`

这组代码很值得读，因为它体现了项目的“运营思维”：

- 不只是记录日志
- 还会定时分析日志内容
- 自动提取热点词
- 再在前端可视化展示

读这部分时要重点关注：

- 数据来源是什么
- 定时任务怎么跑
- IK 分词怎么做
- Redis 缓存怎么参与

---

### 2.6 第六步：最后补 CRUD 和后台管理模块

这一层建议放到后面读，因为它们更容易理解：

- `UserController.java` + `UserServiceImpl.java`
- `SensitiveWordController.java` + 对应 service/mapper/entity
- `SensitiveCategoryController.java` + 对应 service/mapper/entity

这些模块主要用来补齐：

- 数据模型长什么样
- MyBatis-Plus 在项目里的使用方式
- 后台管理接口的常见风格

如果你已经理解了主链路，这些模块再回来看会非常快。

---

### 2.7 后端阅读时的注意点

后端有几个很容易误判的地方：

1. **`AliOss*` 命名不代表当前真的在用 OSS**  
   实际主配置是本地存储。

2. **`init.sql` 不等于当前线上真实架构**  
   它保留了 MySQL `vector_store` 表痕迹，但当前主配置走的是 Milvus。

3. **权限控制不等于 RBAC**  
   现在看到的是 JWT 登录态校验，不是完整角色权限体系。

4. **JPA 依赖不代表项目是 JPA 风格**  
   主持久层风格还是 MyBatis-Plus + XML Mapper。

---

## 3. 前端阅读指南（`app/xushu-rag-ai-front`）

前端的作用不是“独立定义业务”，而是把后端能力组织成控制台页面。所以前端阅读重点在：

- 页面是怎么组织的
- 页面如何映射后端接口
- 用户路径是什么

---

### 3.1 第一步：看启动与路由

先读：

- `src/main.ts`
- `src/router/index.ts`
- `src/router/config.ts`

你要先搞清楚：

- 应用怎么启动
- 路由有哪些页面
- 哪些页面需要登录
- 哪些页面带“管理员”限制

读完后你应该知道前端主页面有哪些：

- `/ragChat`
- `/draw`
- `/know-hub`
- `/user`
- `/logInfo`
- `/sensitive`
- `/senCategory`
- `/frequency`

这里其实已经把整个产品导航地图画出来了。

---

### 3.2 第二步：看 HTTP 基础设施

再读：

- `src/http/config.ts`
- `src/http/index.ts`（如果存在）
- `vite.config.ts`
- `src/api/authUtils.ts`

你要看清楚：

- API 基址是 `/api/v1`
- 开发环境通过 Vite 代理到 `http://localhost:8989`
- token 放在哪里
- 401 怎么处理

这一步是前后端联调的关键。

---

### 3.3 第三步：按“页面 → API → 后端接口”成组阅读

推荐按下面分组读。

#### 组 1：RAG 页面

- 页面：`src/view/ragChat/RagChatView.vue`
- API：`src/api/RagApi.ts`、`src/api/StreamApi.ts`

读的时候重点问自己：

- 页面如何选择知识源
- 流式返回是怎么接的
- 消息状态怎么维护
- 哪些地方和 `/ai/rag` 对应

#### 组 2：知识库页面

- 页面：`src/view/know/KnowHubView.vue`
- API：`src/api/KnowHubApi.ts`

重点看：

- 文件上传怎么做
- 列表查询怎么做
- 删除/下载怎么做
- 页面操作和后端 `/knowledge/*` 怎么对应

#### 组 3：绘图页面

- 页面：`src/view/draw/DrawImageView.vue`
- API：`src/api/DrawApi.ts`

重点看：

- 提示词怎么传
- 图片结果怎么展示
- 和后端 `/draw/image` 的实际匹配关系是否完全一致

#### 组 4：治理页面

- `src/view/sensitive/`
- `src/view/logInfo/`
- `src/view/frequency/`
- 对应 API：`SensitiveApi.ts`、`LogApi.ts`、`FrequencyApi.ts`

重点看：

- 管理页面是纯 CRUD，还是带图表/统计
- 哪些是管理员可见
- 和后端控制器是一一对应还是有漂移

#### 组 5：用户与登录页面

- 页面：`src/view/login/RegisterLoginView.vue`
- 页面：`src/view/user/UserView.vue`
- API：`src/api/UserApi.ts`

重点看：

- 登录成功后前端存了什么
- `userRole` 是怎么来的
- 页面角色限制和后端能力是否真的一致

---

### 3.4 第四步：最后再统一看 `api/` 目录

前端 `src/api/` 目录值得单独扫一遍：

- `RagApi.ts`
- `KnowHubApi.ts`
- `DrawApi.ts`
- `UserApi.ts`
- `SensitiveApi.ts`
- `LogApi.ts`
- `FrequencyApi.ts`
- `common.ts`
- `StreamApi.ts`

原因是这里能快速暴露出两个问题：

1. **哪些 API 是主用中的**
2. **哪些 API 已经陈旧或漂移**

例如当前就能看出：

- 有些前端常量定义了后端未明显实现的接口
- 有些调用方式和后端真实方法签名不完全一致

所以这一步适合作为“理解完成后的复盘”，而不是上来就看。

---

### 3.5 前端阅读时的注意点

1. **不要把路由里的 `roles` 当成完整权限系统**  
   它更多是前端页面访问控制，不等于后端真的做了角色授权。

2. **不要默认所有 API 常量都还在生效**  
   `api/` 里有历史残留痕迹，要以实际页面引用和后端实现为准。

3. **先看页面主流程，再看组件细节**  
   这个项目更重要的是页面与接口映射，不是前端抽象技巧。

---

## 4. SuperSQL 阅读指南（`SuperSQL`）

SuperSQL 最容易让人读混，因为它既是：

- 主项目依赖的能力来源
- 又是仓库内独立存在的一套工具链

所以读它时一定要先分清：

> **“它怎么独立运行”** 和 **“主项目怎么用它”** 是两件事。

---

### 4.1 第一步：先看 SuperSQL 自己是什么

先读：

- `SuperSQL/README.md`
- `SuperSQL/pom.xml`

你要先搞清楚：

- 它的目标是什么
- 它解决的是哪类问题
- 它有哪些模块

读完以后你应该明确：

- 它是一个 **Text-to-SQL / RAG SQL** 框架
- 不是主应用专属代码
- 它本身是一套可独立发布、运行、演示的工程

---

### 4.2 第二步：看核心能力在什么模块

建议按这个顺序：

- `super-sql-core`
- `super-sql-mybatis-plus`
- `super-sql-spring-boot-starter`

其中重点是：

#### `super-sql-core`

看核心引擎、训练、SQL 生成相关能力。

#### `super-sql-spring-boot-starter`

重点看自动配置：

- `SuperSqlAutoConfiguration.java`

这部分告诉你：

- Spring Boot 项目如何零侵入接入 SuperSQL
- 主项目为什么可以直接注入 `SpringSqlEngine`

这一层读懂后，你就知道主项目为什么能直接使用 SuperSQL，而不需要自己手写大量初始化代码。

---

### 4.3 第三步：看它的独立运行面

再读这些模块：

- `super-sql-console`
- `super-sql-ui`
- `super-sql-mcp/*`

建议阅读方式：

#### `super-sql-console`

看它暴露了哪些 controller：

- 聊天
- SQL 训练
- 文档管理

这是“官方演示/控制台式入口”。

#### `super-sql-ui`

看它的前端页面与路由，理解它如何把 SuperSQL 做成独立产品界面。

#### `super-sql-mcp`

如果你关心 AI Agent / MCP 接入，这一块值得单独看；否则可以先略过，后面再补。

---

### 4.4 第四步：最后回到主项目，理解 SuperSQL 是怎么被用上的

完成上面步骤后，再回主项目看：

- `rag-ai-back/src/main/java/com/le/rag/service/SuperSqlIntegrationService.java`
- `rag-ai-back/src/main/java/com/le/rag/tools/RagTool.java`

这时你会更容易明白：

- 主项目没有自己实现 text-to-sql
- 它是把 SuperSQL 当成底层引擎
- `RagTool` 是主项目把 SuperSQL 暴露给 LLM 工具调用的桥

也就是说：

> 在主项目里，SuperSQL 不是“另一个页面模块”，而是“一个底层智能能力插件”。

---

### 4.5 阅读 SuperSQL 时的注意点

1. **不要把 SuperSQL 和主业务代码混成一个系统读**  
   它是嵌入式子项目，但同时又可独立运行。

2. **先读 README 和 starter，再读 console/UI**  
   否则你会只看到演示层，看不清底层设计。

3. **主项目只用了它的一部分能力**  
   不要默认 SuperSQL 所有模块都在主项目里被完整使用。

---

## 5. 最省时间的分模块上手路径

如果你时间很少，建议直接走下面这条路径：

### 路线 A：只想快速理解主业务

1. `rag-ai-back/application.yml`
2. `AiRagController.java`
3. `KnowledgeController.java`
4. `AliOssFileServiceImpl.java`
5. `app/xushu-rag-ai-front/src/router/config.ts`
6. `RagChatView.vue`
7. `KnowHubView.vue`

### 路线 B：想接手后端开发

1. 后端入口与配置
2. controller 全扫一遍
3. RAG + Knowledge 主链路
4. JWT / AOP / Exception 全局逻辑
5. Log / Frequency 定时分析链路
6. User / Sensitive 等 CRUD 补齐

### 路线 C：想接手前端开发

1. `main.ts`
2. `router/index.ts` + `router/config.ts`
3. `http/config.ts` + `vite.config.ts`
4. `view/ragChat` + `api/StreamApi.ts`
5. `view/know` + `api/KnowHubApi.ts`
6. 其余治理页面

### 路线 D：想研究 SuperSQL

1. `SuperSQL/README.md`
2. `SuperSQL/pom.xml`
3. `super-sql-core`
4. `super-sql-spring-boot-starter`
5. `super-sql-console`
6. 回主项目看 `RagTool.java`

---

## 6. 最终建议

这份仓库最容易读乱的原因，不是代码量大，而是它同时包含：

- 一个主业务系统
- 一个主业务前端
- 一个嵌入式但可独立运行的子工程（SuperSQL）

所以最好的阅读方法不是“从目录树往下扫”，而是：

> **先主后辅，先链路后细节，先产品入口后基础设施。**

如果你按照这份阅读顺序走，通常会比直接硬啃目录快很多，而且不容易把：

- 主应用代码
- 前端页面代码
- SuperSQL 演示代码

混在一起理解。
