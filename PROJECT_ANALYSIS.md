# rag-ai 项目分析文档

> 生成时间：2026-04-25  
> 分析范围：`rag-ai-back`、`app/xushu-rag-ai-front`、`SuperSQL`  
> 结论来源：基于代码、配置、目录结构与关键实现的只读分析

---

## 1. 项目总体定位

这个仓库不是一个“单体项目目录”，而是一个**复合型工作区**：

1. **主业务应用**：一个基于 Spring Boot 的 RAG/AI 后端，目录为 `rag-ai-back`
2. **主业务前端**：一个基于 Vue 3 + Vite 的前端控制台，目录为 `app/xushu-rag-ai-front`
3. **嵌入式能力子项目**：一个名为 **SuperSQL** 的独立多模块工程，目录为 `SuperSQL`

根目录 `pom.xml` 只是 **Maven 聚合 POM**，它只聚合了：

- `rag-ai-back`
- `SuperSQL`

也就是说：**主前端 `app/xushu-rag-ai-front` 不在 Maven Reactor 里**，它是单独的 Node/Vite 项目。

---

## 2. 仓库结构总览

```text
rag-ai/
├─ pom.xml                         # 根聚合 POM
├─ rag-ai-back/                    # 主后端（Spring Boot）
├─ app/
│  └─ xushu-rag-ai-front/          # 主前端（Vue 3 + Vite）
└─ SuperSQL/                       # 独立的 SuperSQL 多模块工程
```

### 2.1 主后端入口

- `rag-ai-back/src/main/java/com/le/rag/LeDonRagAiApplication.java`
  - `@SpringBootApplication`
  - `@EnableScheduling`

说明主后端是一个带**定时任务**能力的 Spring Boot 应用。

### 2.2 主前端入口

- `app/xushu-rag-ai-front/src/main.ts`
  - 注册了 `router`
  - 注册了 `pinia`
  - 注册了 `Element Plus`
  - 注册了代码高亮指令

### 2.3 SuperSQL 子项目入口

SuperSQL 不只是一个 library 文件夹，而是**可独立运行**的多模块项目。已发现多个启动类，例如：

- `SuperSQL/super-sql-console/.../ConsoleApplication.java`
- `SuperSQL/super-sql-mcp/super-sql-mcp-webmvc/.../WebMvcMcpApplication.java`
- `SuperSQL/super-sql-mcp/super-sql-mcp-stdio/.../SuperSQLMcpServerApplication.java`
- `SuperSQL/super-sql-mcp/super-sql-mcp-client/.../McpClientApplication.java`

这意味着仓库里实际上包含了**两个产品面**：

- 主应用：RAG AI 管理平台
- 子产品：SuperSQL 文本转 SQL 工具链

---

## 3. 主应用技术架构

### 3.1 后端技术栈

根据根 `pom.xml` 和 `rag-ai-back/pom.xml`，主后端的核心栈为：

- **Java 17**
- **Spring Boot 3.5.8**
- **Spring AI 1.1.2**
- **Spring AI Alibaba / DashScope**
- **MyBatis-Plus**
- **MySQL**
- **Redis**
- **Milvus 向量库**
- **Apache Tika 文档解析**
- **JWT 鉴权**
- **PageHelper 分页**
- **Druid 数据源**
- **IK Analyzer 分词**

### 3.2 前端技术栈

根据 `app/xushu-rag-ai-front/package.json`：

- **Vue 3**
- **TypeScript**
- **Vite**
- **Vue Router**
- **Pinia**
- **Element Plus**
- **Axios**
- **fetch-event-source**（用于流式 SSE/流式响应）
- **ECharts / echarts-wordcloud**
- **marked / highlight.js / MathJax**

### 3.3 AI 与模型能力

从 `rag-ai-back/src/main/resources/application.yml` 可见，当前主应用使用的是：

- **DashScope Chat 模型**：`qwen-plus`
- **DashScope Image 模型**：`wan2.2-t2i-plus`
- **Milvus 向量存储**
- 可见有注释掉的 Ollama 配置，说明项目曾考虑或支持本地模型路线，但**当前主配置不是 Ollama**。

---

## 4. 主后端分层结构

主后端代码集中在包：`com.le.rag`

目录结构如下：

- `controller/`：HTTP 接口层
- `service/`、`service/impl/`：业务服务层
- `mapper/`：MyBatis Mapper 接口
- `entity/`：数据库实体
- `pojo/dto/`、`pojo/vo/`：请求/响应对象
- `config/`：Spring 配置
- `common/`、`constant/`、`context/`、`exception/`：公共设施
- `aop/`、`annotation/`：日志切面与注解
- `advisors/`、`tools/`：Spring AI 扩展能力
- `scheduled/`：定时任务
- `utils/`：工具类

### 4.1 架构风格判断

它更像是：

- **一个单体 Spring Boot 应用**
- 内部按照 **controller → service → mapper/entity** 分层
- 提供多个功能面接口

而**不是**严格意义上的“多后端模块、强边界领域拆分”架构。换句话说：

> 这里的“模块”更偏向业务功能面，而不是独立部署或强隔离子系统。

---

## 5. 主应用功能模块分析

下面按“实际代码入口 + 前端页面/路由 + 关键实现”来拆解。

---

## 5.1 AI 问答 / RAG 对话模块

### 后端入口

- `rag-ai-back/src/main/java/com/le/rag/controller/AiRagController.java`
- 路由前缀：`/api/v1/ai`
- 关键接口：`POST /rag`

### 核心能力

该模块是主应用最核心的能力之一，提供：

- 基于用户提问的 RAG 问答
- 支持按 `sources` 指定知识库文件范围进行检索
- 集成聊天记忆 `ChatMemory`
- 集成向量检索 `QuestionAnswerAdvisor`
- 集成自定义 `MetadataAwareQuestionAnswerAdvisor`
- 集成敏感词过滤
- 注册了 `RagTool`，使模型可以在特定场景下调用 SuperSQL 聚合查询能力

### 关键实现细节

- 默认系统提示词在 `AiRagController` 中直接构造
- 当前日期会动态注入 prompt
- 如果传入 `sources`，会对向量检索加上 `filterExpression`
- 检索结果会通过 `MetadataAwareQuestionAnswerAdvisor` 拼入上下文，并附带“来源文件”信息

### 前端对应

- 路由：`/ragChat`
- 页面：`app/xushu-rag-ai-front/src/view/ragChat/RagChatView.vue`
- 流式调用实现：`src/api/StreamApi.ts`

### 评价

这是项目的**第一核心业务模块**，并且不是简单 demo，而是具备：

- 向量检索
- 文件范围过滤
- 聊天记忆
- 工具调用扩展
- 中文提示控制

等完整链路能力。

---

## 5.2 普通流式聊天模块

### 后端入口

- `rag-ai-back/src/main/java/com/le/rag/controller/ChatController.java`
- 路由前缀：`/api/v1/chat`
- 关键接口：`GET /stream`

### 能力说明

该模块提供不走知识库检索的普通流式对话：

- 自定义 prompt
- 聊天记忆
- 敏感词过滤
- 基于 `BaseContext` 中的用户 ID 进行会话隔离

### 架构观察

后端能力是存在的，但前端并没有清晰地做成独立的一块“普通聊天产品页面”；主页面更偏向 RAG 问答。

**因此，文档里更适合把它描述为：**

> 主应用提供“RAG 问答 + 普通聊天”两类对话接口，但前端主入口更偏向 RAG 场景。

---

## 5.3 知识库管理模块

### 后端入口

- `rag-ai-back/src/main/java/com/le/rag/controller/KnowledgeController.java`
- 路由前缀：`/api/v1/knowledge`

### 关键接口

- `POST /file/upload`：上传知识库文件
- `GET /contents`：查询文件列表
- `DELETE /delete`：删除文件
- `GET /download`：下载文件

### 实际处理链路

这是项目里最值得重点理解的链路之一：

1. 用户上传文件
2. 通过 `AliOssUtil` 保存到本地目录（当前不是 OSS）
3. 使用 `TikaDocumentReader` 解析文档
4. 使用 `TokenTextSplitter` 切片
5. 将切片写入 `VectorStore`（Milvus）
6. 把文件元数据 + 向量 ID 列表写入 MySQL `ali_oss_file`

对应核心实现：

- `KnowledgeController.java`
- `service/impl/AliOssFileServiceImpl.java`
- `utils/AliOssUtil.java`
- `entity/AliOssFile.java`
- `mapper/AliOssFileMapper.java`

### 前端对应

- 路由：`/know-hub`
- 页面：`app/xushu-rag-ai-front/src/view/know/KnowHubView.vue`
- API：`src/api/KnowHubApi.ts`

### 重要说明

类名里虽然保留了 `AliOss` 命名，但当前配置 `application.yml` 明确显示：

- `app.storage.type: local`
- 上传目录与下载目录都是本地磁盘路径

因此：

> 当前实际落地方式是**本地文件存储 + 向量库存储 + MySQL 元数据存储**，不是阿里云 OSS。

---

## 5.4 AI 绘图模块

### 后端入口

- `rag-ai-back/src/main/java/com/le/rag/controller/DrawImageController.java`
- 路由前缀：`/api/v1/draw`
- 关键接口：`GET /image`

### 能力说明

该模块调用 Spring AI 的 `ImageModel` 生成图片 URL，再由后端把图片流拉回并转发给前端。

### 前端对应

- 路由：`/draw`
- 页面：`app/xushu-rag-ai-front/src/view/draw/DrawImageView.vue`

### 特点

该模块不是“返回 URL 给前端”，而是由后端代拉图片并写回响应流。这意味着：

- 前端调用体验简单
- 但后端承担了额外的下载转发职责

---

## 5.5 用户与认证模块

### 后端入口

- `rag-ai-back/src/main/java/com/le/rag/controller/UserController.java`
- 路由前缀：`/api/v1/user`

### 关键接口

- `POST /register`
- `POST /login`
- `POST /logout`
- `POST /updatePassword`
- `GET /page`
- `GET /{id}`
- `PUT /update`
- `POST /status/{status}`
- `POST /addUser`

### 鉴权机制

项目使用的是**JWT + Spring MVC Interceptor** 方案，而不是 Spring Security。

关键文件：

- `common/JwtTokenUserInterceptor.java`
- `config/ApplicationConfig.java`
- `config/JwtProperties.java`
- `utils/JwtUtil.java`
- `context/BaseContext.java`

流程是：

1. 前端带上 token
2. `JwtTokenUserInterceptor` 校验 token
3. 解析出用户 ID
4. 将用户 ID 写入 `BaseContext.threadLocal`
5. 业务接口从 `BaseContext` 获取当前用户

### 前端对应

- 路由：`/login`、`/user`
- 页面：
  - `view/login/RegisterLoginView.vue`
  - `view/user/UserView.vue`

### 重要 caveat

项目前端路由里存在 `roles: ['admin']` 的页面控制，但后端并未看到完整 RBAC/角色校验体系。当前更像是：

- 前端做页面可见性控制
- 后端只做“登录态校验”

所以：

> **权限控制偏前端化，后端更偏“是否登录”校验，而不是严格角色授权。**

---

## 5.6 敏感词与敏感词分类模块

### 后端入口

- `SensitiveWordController.java` → `/api/v1/sensitive`
- `SensitiveCategoryController.java` → `/api/v1/category`

### 功能说明

用于维护：

- 敏感词条目
- 敏感词分类

而这些数据会被：

- `AiRagController`
- `ChatController`

用于请求内容过滤。

### 前端对应

- 路由：`/sensitive`、`/senCategory`
- 页面：`view/sensitive/`

### 评价

这是一个很典型的“AI 应用治理能力”模块，说明项目不是单纯追求“能调用模型”，而是已经开始考虑内容管控。

---

## 5.7 日志审计模块

### 后端入口

- `LogInfoController.java` → `/api/v1/log`
- `aop/LoggingAspect.java`
- 注解：`annotation/Loggable.java`

### 工作方式

项目通过 AOP 切面对带 `@Loggable` 的方法记录：

- 方法名
- 类名
- 请求时间
- 请求参数

并写入 `log_info` 表。

### 前端对应

- 路由：`/logInfo`
- 页面：`view/logInfo/LogInfoView.vue`

### 评价

这是一个运维/审计支撑模块，同时也为后面的“热点词分析”提供原始数据来源。

---

## 5.8 热点词分析模块

### 后端入口

- `WordFrequencyController.java` → `/api/v1/frequency`
- `scheduled/TaskJobScheduled.java`

### 工作方式

该模块不是简单人工维护数据，而是一个**自动分析链路**：

1. 定时任务每小时执行一次
2. 清理 Redis 缓存
3. 拉取日志表中的请求参数
4. 使用 IK 分词器进行中文分词
5. 过滤过短/过长词
6. 对词频进行新增或更新
7. 对结果提供查询接口

### 前端对应

- 路由：`/frequency`
- 页面：`view/frequency/WordFrequencyView.vue`
- 使用 ECharts 展示词频统计

### 评价

这是项目里很有产品味的一部分：

> 用户使用日志 → 自动提取热点词 → 可视化分析

说明它已经开始从“功能平台”演化为“带运营分析能力的 AI 后台”。

---

## 6. SuperSQL 子项目分析

`SuperSQL` 是本仓库中非常重要但容易误解的部分。

### 6.1 它不是单纯依赖源码

`SuperSQL/pom.xml` 显示它自身就是一个多模块工程，包含：

- `super-sql-core`
- `super-sql-mybatis-plus`
- `super-sql-spring-boot-starter`
- `super-sql-console`
- `super-sql-mcp/*`
- `super-sql-ui`

### 6.2 它在主项目中的角色

主后端 `rag-ai-back/pom.xml` 引入了：

- `com.aispace.supersql:super-sql-spring-boot-starter`

因此主应用中确实把 SuperSQL 当作一个能力组件在用。

相关接入点：

- `service/SuperSqlIntegrationService.java`
- `tools/RagTool.java`

其中 `RagTool#getAggregationQuery()` 明确说明：

- 当用户问题涉及统计、求和、计数、平均值等聚合操作时
- 可以借助 SuperSQL 做 text-to-sql，再执行 SQL

### 6.3 它在仓库中的另一层身份

除了作为主应用的内嵌能力，SuperSQL 自己又是：

- 一个可独立启动的控制台应用
- 一个可独立启动的 UI
- 一组 MCP 服务/客户端

所以更准确的描述是：

> **SuperSQL 既是主应用的底层能力组件，又是仓库里独立存在的一套产品/工具链。**

---

## 7. 关键请求链路

### 7.1 知识库上传 → RAG 回答

这是最核心的端到端链路：

1. 前端从 `KnowHubView.vue` 上传文件
2. `KnowHubApi.ts` 调后端 `/api/v1/knowledge/file/upload`
3. 后端保存文件到本地目录
4. 使用 Tika 读取文本
5. 使用 `TokenTextSplitter` 做切片
6. 切片写入 Milvus
7. 文件元数据与切片 ID 写入 MySQL
8. 用户在 RAG 页面选择文件来源
9. 前端把 `message + sources` 传给 `/api/v1/ai/rag`
10. 后端按来源过滤向量检索
11. 检索结果拼入 prompt，返回流式回答

这条链路体现了项目最核心的“知识库驱动问答”能力。

### 7.2 请求日志 → 热词分析

另一条很有代表性的链路是：

1. 用户调用带 `@Loggable` 的接口
2. `LoggingAspect` 记录请求参数到 `log_info`
3. 定时任务扫描日志内容并做 IK 分词
4. 词频结果写入 `word_frequency`
5. 前端用图表页面展示热点词

这条链路代表的是：

> **从使用数据中挖掘运营价值**

---

## 8. 运行与配置要点

### 8.1 主应用默认端口

- 后端：`8989`（见 `application.yml`）
- 前端：`8980`（见 `vite.config.ts`）

并且前端通过 Vite 代理把 `/api` 转发到 `http://localhost:8989`。

### 8.2 前端 API 基址

- `app/xushu-rag-ai-front/src/http/config.ts`
- 基址：`/api/v1`

后端接口统一版本前缀：

- `common/ApplicationConstant.java`
- `API_VERSION = /api/v1`

这一点前后端是一致的。

### 8.3 当前主配置使用的基础设施

从 `rag-ai-back/src/main/resources/application.yml` 看，当前主配置倾向于：

- **MySQL**：业务数据
- **Redis**：缓存
- **Milvus**：向量存储
- **本地磁盘目录**：文件存储
- **DashScope**：聊天与绘图模型

---

## 9. 重要架构观察与风险点

这一节非常重要，它决定你是否能“系统性理解项目”，而不只是看见功能列表。

### 9.1 项目存在“配置与实现表面不完全一致”的现象

#### 现象 1：存储命名与实际实现不一致

虽然很多类还叫 `AliOss*`，但当前实现实际是**本地文件存储**，不是 OSS。

#### 现象 2：`init.sql` 与当前向量配置并不完全一致

`init.sql` 里存在 `vector_store` MySQL 表结构，但当前主配置使用的是 **Milvus**。这说明仓库里保留了旧方案或备用方案痕迹。

#### 现象 3：`docker-compose.yml` 与当前 `application.yml` 并不完全同一套基础设施口径

说明仓库内存在**历史方案 / 替代部署方案 / 未清理配置**。

---

### 9.2 前端接口定义存在陈旧或漂移迹象

前端 API 常量里存在一些后端未明显实现的接口定义，例如：

- `/chat/simple`
- `/chat/models`
- `/one-api/*`
- `/user/download`

这说明：

> 前端接口封装层中有一部分是历史遗留、规划中、或未完全同步清理的内容。

---

### 9.3 权限控制更偏前端，后端角色控制不强

前端用 `roles: ['admin']` 控制页面进入，但后端主要看到的是 JWT 登录态校验，没有看到完整的强角色授权体系。

这意味着：

- 前端有“管理员页面”概念
- 后端未形成严格 RBAC 闭环

如果后续要上线或多角色扩展，这块需要加强。

---

### 9.4 下载语义更像“服务端导出”，不完全是浏览器文件直传下载

知识库模块中的“下载”能力，在后端实现上更像是：

- 把已有文件复制/输出到配置的下载目录
- 再返回操作成功结果

而不是标准浏览器文件流下载接口语义。

这类差异在接手项目时很容易误判，所以需要特别记住。

---

### 9.5 JPA 依赖存在，但主持久层实际以 MyBatis-Plus 为主

虽然依赖中包含 `spring-boot-starter-data-jpa`，但当前主业务持久层证据主要集中在：

- `mapper/*.java`
- `resources/mapper/*.xml`
- MyBatis-Plus `ServiceImpl`

所以项目的真实数据访问主风格是：

> **MyBatis-Plus + XML Mapper**，而不是 JPA 主导。

---

## 10. 建议的阅读顺序

如果你要快速上手这个项目，建议按下面顺序阅读：

### 第 1 步：看整体入口

- `pom.xml`
- `rag-ai-back/pom.xml`
- `rag-ai-back/src/main/resources/application.yml`
- `app/xushu-rag-ai-front/package.json`
- `app/xushu-rag-ai-front/vite.config.ts`

### 第 2 步：看主链路

- `KnowledgeController.java`
- `AiRagController.java`
- `AliOssFileServiceImpl.java`
- `MetadataAwareQuestionAnswerAdvisor.java`
- `RagTool.java`

### 第 3 步：看治理与运营能力

- `JwtTokenUserInterceptor.java`
- `ApplicationConfig.java`
- `LoggingAspect.java`
- `TaskJobScheduled.java`
- `WordFrequencyController.java`

### 第 4 步：看前端页面映射

- `src/router/config.ts`
- `view/ragChat/`
- `view/know/`
- `view/draw/`
- `view/sensitive/`
- `view/logInfo/`
- `view/frequency/`

### 第 5 步：再理解 SuperSQL

- `SuperSQL/README.md`
- `SuperSQL/pom.xml`
- `super-sql-spring-boot-starter`
- `super-sql-core`
- `super-sql-console`

---

## 11. 最终结论

这个项目可以概括为：

> 一个以 **RAG 知识库问答** 为核心的 Spring Boot + Vue AI 应用，附带 **用户管理、敏感词治理、日志审计、热词分析、AI 绘图** 等后台能力，并在内部集成了一个可独立运行的 **SuperSQL 文本转 SQL 工具体系**。

它的成熟度不是“玩具 demo”，因为已经具备：

- 知识库上传与向量化
- 流式问答
- 聊天记忆
- 敏感词治理
- JWT 登录态
- 操作日志沉淀
- 词频分析
- 前后端完整页面映射

但它也明显保留了演进痕迹：

- 历史配置未完全收敛
- 前后端接口定义有漂移
- 权限模型偏弱
- SuperSQL 与主应用的职责边界还可以进一步文档化

所以从工程视角看，这个项目最准确的状态是：

> **一个已经具备完整产品雏形、并处于持续演进中的 AI/RAG 管理平台。**
