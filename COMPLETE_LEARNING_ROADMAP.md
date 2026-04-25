# rag-ai 完整学习路线（由浅入深）

> 目标：帮你**彻底吃透**这个项目。  
> 这份文档不是普通的项目介绍，而是把你从“看懂目录”一步步带到“理清业务、读懂主链路、识别每个文件的作用、理解系统边界”。

---

## 1. 先说结论：这个项目到底是什么

这个仓库不是一个单体项目，而是三块东西组合在一起：

1. **主后端**：`rag-ai-back`
   - 一个 Spring Boot AI 后台
   - 承担聊天、RAG、知识库、绘图、用户、日志、敏感词、热词分析等核心业务

2. **主前端**：`app/xushu-rag-ai-front`
   - 一个 Vue 3 + Vite 管理控制台
   - 负责把后端能力组织成可操作页面

3. **能力子系统**：`SuperSQL`
   - 一个独立的 Text-to-SQL / 结构化检索增强系统
   - 一部分能力被主项目嵌入使用
   - 自己也能独立运行（console / mcp / ui）

换句话说：

> **主产品是一个以 RAG 问答为核心的 AI 中台，SuperSQL 是它接入的一个结构化查询引擎。**

---

## 2. 你要建立的总心智模型

真正吃透这个项目，不是背文件名，而是建立下面这几个心智模型。

### 2.1 主产品的业务骨架

主产品大致由 5 条能力线组成：

- **AI 对话线**：普通聊天 + RAG 问答
- **知识库线**：文件上传、切片、向量化、按来源问答
- **结构化问答线**：通过 SuperSQL 做统计、计数、求和类问题
- **治理线**：敏感词过滤、日志记录、用户管理
- **运营线**：日志分词、热词统计、可视化展示

### 2.2 主后端不是“纯 RAG 服务”

后端同时管理：

- HTTP API
- 文件存储
- 向量存储
- 关系型数据
- 用户认证
- 运营分析

它是一个 **AI 后台系统**，不是单纯的“调用大模型接口”。

### 2.3 主前端不是“复杂前端框架示例”

主前端本质上是：

- 路由编排层
- 页面组织层
- API 调用层

重点是**页面如何映射后端能力**，不是复杂状态管理技巧。

### 2.4 SuperSQL 不是主产品本身

SuperSQL 是一个独立子工程，它：

- 能独立做 Text-to-SQL
- 也能被主项目当作“工具能力”接入

所以阅读时一定要分清：

- **主产品逻辑**
- **SuperSQL 自己的产品逻辑**
- **两者的集成缝**

---

## 3. 项目总体结构图

```text
rag-ai/
├─ pom.xml
├─ rag-ai-back/                    # 主后端
├─ app/
│  └─ xushu-rag-ai-front/          # 主前端
└─ SuperSQL/                       # 独立子工程
   ├─ super-sql-core/
   ├─ super-sql-spring-boot-starter/
   ├─ super-sql-mybatis-plus/
   ├─ super-sql-console/
   ├─ super-sql-mcp/
   └─ super-sql-ui/
```

### 关键边界

- 根 `pom.xml` 只聚合：`rag-ai-back` 和 `SuperSQL`
- `app/xushu-rag-ai-front` **不在 Maven reactor 中**
- `SuperSQL/super-sql-ui` 也是独立前端，不是主前端的一部分

---

## 4. 功能总览（先知道“它能干什么”）

在进入学习路线之前，先把功能认全。

### 4.1 主功能

1. **RAG 问答**
   - 上传知识文件
   - 选择知识来源
   - 基于知识库进行问答

2. **普通聊天**
   - 不走知识库的流式聊天接口

3. **AI 绘图**
   - 根据 prompt 生成图片

4. **用户管理 / 登录认证**
   - 登录、注册、修改密码、用户管理

5. **敏感词治理**
   - 维护敏感词与敏感词分类
   - 在聊天/RAG 请求时做过滤

6. **日志审计**
   - 对关键接口进行 AOP 请求记录

7. **热词分析**
   - 从请求日志中抽词
   - 统计高频词
   - 在前端可视化展示

8. **结构化查询能力**
   - 对统计类问题，可能由模型通过工具调用转入 SuperSQL

---

## 5. 学习路线：由浅入深

下面是我建议的**最合理学习顺序**。你按这个顺序走，会比从目录开始乱读快很多。

---

## 阶段 0：先建立地图，不碰实现

### 学习目标

- 明白仓库分成哪三块
- 知道主产品和 SuperSQL 的边界
- 知道后端、前端、SuperSQL 各自的入口在哪

### 建议阅读文件

- `pom.xml`
- `rag-ai-back/pom.xml`
- `app/xushu-rag-ai-front/package.json`
- `SuperSQL/pom.xml`
- `PROJECT_ANALYSIS.md`
- `MODULE_READING_GUIDE.md`

### 这一阶段必须回答的问题

1. 主产品由哪两部分组成？
2. 为什么说 SuperSQL 不是主产品本身？
3. 为什么说主前端不是 Maven 模块？

### 学完产出

你应该能自己画出下面这张图：

`主前端 -> 主后端 -> MySQL/Redis/Milvus/本地文件`，以及 `主后端 -> SuperSQL`

---

## 阶段 1：理解主后端“运行底座”

### 学习目标

- 知道后端怎么启动
- 知道主配置里依赖哪些基础设施
- 知道登录态和 API 版本前缀怎么进系统

### 建议阅读文件

- `rag-ai-back/src/main/java/com/le/rag/LeDonRagAiApplication.java`
- `rag-ai-back/src/main/resources/application.yml`
- `rag-ai-back/src/main/resources/application-dev.yml`
- `rag-ai-back/src/main/java/com/le/rag/common/ApplicationConstant.java`
- `rag-ai-back/src/main/java/com/le/rag/config/ApplicationConfig.java`
- `rag-ai-back/src/main/java/com/le/rag/common/JwtTokenUserInterceptor.java`
- `rag-ai-back/src/main/java/com/le/rag/context/BaseContext.java`
- `rag-ai-back/src/main/java/com/le/rag/config/JwtProperties.java`
- `rag-ai-back/src/main/java/com/le/rag/utils/JwtUtil.java`
- `rag-ai-back/src/main/java/com/le/rag/config/ChatModelConfig.java`
- `rag-ai-back/src/main/java/com/le/rag/config/EmbeddingModelConfig.java`

### 这一阶段最重要的理解点

1. API 统一前缀是 `/api/v1`
2. 当前主配置里能看到：MySQL / Redis / Milvus / 本地文件存储
3. JWT 通过拦截器把 userId 写到 `BaseContext`
4. 后端的主 `ChatModel` / `EmbeddingModel` 需要以配置类为准理解，不要只看 YAML 表面

### 学完产出

你应该能用自己的话解释：

> 一个带 JWT 登录态、向量库、关系库、本地文件存储和 AI 模型配置的 Spring Boot 应用是如何搭起来的。

---

## 阶段 2：理解最核心业务——知识库上传 + RAG 问答

这是整个项目最重要的一阶段。

### 学习目标

- 吃透知识库是如何入库的
- 吃透 RAG 是如何回答问题的
- 明白前后端是怎么配合完成这一链路的

### 建议阅读文件

#### 后端

- `rag-ai-back/src/main/java/com/le/rag/controller/KnowledgeController.java`
- `rag-ai-back/src/main/java/com/le/rag/service/impl/AliOssFileServiceImpl.java`
- `rag-ai-back/src/main/java/com/le/rag/utils/AliOssUtil.java`
- `rag-ai-back/src/main/java/com/le/rag/entity/AliOssFile.java`
- `rag-ai-back/src/main/java/com/le/rag/mapper/AliOssFileMapper.java`
- `rag-ai-back/src/main/resources/mapper/AliOssFileMapper.xml`
- `rag-ai-back/src/main/java/com/le/rag/controller/AiRagController.java`
- `rag-ai-back/src/main/java/com/le/rag/advisors/MetadataAwareQuestionAnswerAdvisor.java`
- `rag-ai-back/src/main/java/com/le/rag/tools/RagTool.java`

#### 前端

- `app/xushu-rag-ai-front/src/view/know/KnowHubView.vue`
- `app/xushu-rag-ai-front/src/api/KnowHubApi.ts`
- `app/xushu-rag-ai-front/src/view/ragChat/RagChatView.vue`
- `app/xushu-rag-ai-front/src/api/StreamApi.ts`

### 这条链路要怎么理解

#### 知识库上传

1. 用户从前端上传文件
2. 后端把文件保存到本地目录
3. 使用 Tika 读出内容
4. 使用 `TokenTextSplitter` 切分文档
5. 把切片写入向量库
6. 在 MySQL 里记录文件元数据和向量切片 ID

#### RAG 回答

1. 用户在前端提问
2. 前端把选中的文件来源作为 `sources` 发给后端
3. 后端按来源过滤检索向量文档
4. 检索结果经 `MetadataAwareQuestionAnswerAdvisor` 拼入 prompt
5. 模型结合上下文进行回答

### 这一阶段必须回答的问题

1. 文件为什么既要存本地，又要存 MySQL 元数据？
2. `sources` 是按什么过滤的？
3. 为什么需要自定义 `MetadataAwareQuestionAnswerAdvisor`？
4. `RagTool` 为什么被挂到聊天链路里？

### 学完产出

你应该能自己口述完整主链路：

> 上传文件 -> 解析/切片/向量化 -> 前端选中文件 -> RAG 检索 -> 模型回答

---

## 阶段 3：理解普通聊天、绘图与结构化查询分流

### 学习目标

- 区分普通聊天和 RAG 问答的不同
- 理解绘图是独立能力线
- 理解统计类问题为什么会走向 SuperSQL

### 建议阅读文件

- `rag-ai-back/src/main/java/com/le/rag/controller/ChatController.java`
- `rag-ai-back/src/main/java/com/le/rag/controller/DrawImageController.java`
- `rag-ai-back/src/main/java/com/le/rag/service/SuperSqlIntegrationService.java`
- `rag-ai-back/src/main/java/com/le/rag/tools/RagTool.java`
- `app/xushu-rag-ai-front/src/view/draw/DrawImageView.vue`
- `app/xushu-rag-ai-front/src/api/DrawApi.ts`
- `app/xushu-rag-ai-front/src/api/ChatApi.ts`
- `app/xushu-rag-ai-front/src/api/RagApi.ts`

### 这一阶段要建立的理解

1. **普通聊天**：不依赖知识库，主要是 prompt + memory
2. **RAG 问答**：依赖向量检索和上下文增强
3. **AI 绘图**：调用 ImageModel，和聊天链路是分开的
4. **结构化问题**：模型可能调用 `RagTool`，再通过 SuperSQL 生成 SQL 并执行

### 学完产出

你应该能把“AI 能力矩阵”讲清楚：

- 什么问题走普通聊天
- 什么问题走 RAG
- 什么问题走绘图
- 什么问题可能走 SuperSQL

---

## 阶段 4：理解认证、权限、日志与治理

### 学习目标

- 知道请求为什么会被记录日志
- 知道热词是怎么来的
- 知道敏感词是怎么参与聊天拦截的
- 知道权限体系的实际成熟度

### 建议阅读文件

- `rag-ai-back/src/main/java/com/le/rag/aop/LoggingAspect.java`
- `rag-ai-back/src/main/java/com/le/rag/annotation/Loggable.java`
- `rag-ai-back/src/main/java/com/le/rag/controller/LogInfoController.java`
- `rag-ai-back/src/main/java/com/le/rag/scheduled/TaskJobScheduled.java`
- `rag-ai-back/src/main/java/com/le/rag/controller/WordFrequencyController.java`
- `rag-ai-back/src/main/java/com/le/rag/controller/SensitiveWordController.java`
- `rag-ai-back/src/main/java/com/le/rag/controller/SensitiveCategoryController.java`
- `rag-ai-back/src/main/java/com/le/rag/service/impl/SensitiveWordServiceImpl.java`
- `app/xushu-rag-ai-front/src/view/logInfo/LogInfoView.vue`
- `app/xushu-rag-ai-front/src/view/frequency/WordFrequencyView.vue`
- `app/xushu-rag-ai-front/src/view/sensitive/SensitiveWordView.vue`
- `app/xushu-rag-ai-front/src/view/sensitive/SenCategoryView.vue`

### 这一阶段要吃透的业务逻辑

#### 日志链路

带 `@Loggable` 的接口会被切面拦截，记录请求参数。

#### 热词链路

日志表中的请求参数被定时任务扫描，用 IK 分词器做分词，再把高频词写回数据库，并缓存到 Redis。

#### 敏感词链路

聊天 / RAG 请求先走敏感词判断，如果命中就直接阻断或返回敏感词提示。

### 这一阶段最容易误判的点

1. 权限控制不是完整 RBAC
2. 前端的 `roles` 更像页面访问控制，不等于后端强授权
3. 运营分析是“日志衍生能力”，不是独立业务源

---

## 阶段 5：理解用户模块与后台 CRUD 风格

### 学习目标

- 明白后台管理 CRUD 的代码组织方式
- 理解用户登录/注册/修改密码/分页等接口模式
- 顺便理解 MyBatis-Plus 在项目里的使用风格

### 建议阅读文件

- `rag-ai-back/src/main/java/com/le/rag/controller/UserController.java`
- `rag-ai-back/src/main/java/com/le/rag/service/UserService.java`
- `rag-ai-back/src/main/java/com/le/rag/service/impl/UserServiceImpl.java`
- `rag-ai-back/src/main/java/com/le/rag/entity/User.java`
- `rag-ai-back/src/main/java/com/le/rag/mapper/UserMapper.java`
- `rag-ai-back/src/main/resources/mapper/UserMapper.xml`
- `app/xushu-rag-ai-front/src/view/login/RegisterLoginView.vue`
- `app/xushu-rag-ai-front/src/view/user/UserView.vue`
- `app/xushu-rag-ai-front/src/api/UserApi.ts`

### 这一阶段的重点

1. 看看用户模块的接口长什么样
2. 看分页查询怎么做
3. 看修改密码和登录怎么做
4. 顺便识别安全/设计上的不完善处

### 学完产出

你应该能看懂这个项目里“标准后台 CRUD 模块”长什么样。

---

## 阶段 6：最后再进入 SuperSQL

这是你真正“吃透整个仓库”的最后一块，而不是第一块。

### 学习目标

- 明白 SuperSQL 自己的产品边界
- 明白它的核心引擎做了什么
- 明白它如何作为 starter 被主项目接入
- 明白它与主项目的关系不是“主模块”，而是“能力子系统”

### 建议阅读顺序

#### 先读说明和模块边界

- `SuperSQL/README.md`
- `SuperSQL/pom.xml`

#### 再读核心引擎层

- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/engine/AbstractSqlEngine.java`
- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/engine/SqlEngine.java`
- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/engine/SpringSqlEngine.java`
- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/engine/RagEngine.java`
- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/engine/SpringRagEngine.java`
- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/engine/TrainingPlanGenerator.java`
- `SuperSQL/super-sql-core/src/main/java/com/aispace/supersql/prompt/SqlAssistantPrompt.java`

#### 再读 builder / model / util / vector 辅助层

- `builder/*`
- `model/*`
- `util/*`
- `vector/*`

#### 再读 Spring 接入层

- `SuperSQL/super-sql-spring-boot-starter/src/main/java/com/aispace/supersql/spring/configure/SuperSqlAutoConfiguration.java`
- `SuperSQL/super-sql-spring-boot-starter/src/main/java/com/aispace/supersql/spring/configure/SuperSQLProperties.java`
- `SuperSQL/super-sql-spring-boot-starter/src/main/java/com/aispace/supersql/spring/configure/SuperSqlMybatisPlusConfiguration.java`

#### 再读 SQL 执行层

- `SuperSQL/super-sql-mybatis-plus/src/main/java/com/aispace/supersql/mybatisplus/service/ExecuteSqlService.java`
- `SuperSQL/super-sql-mybatis-plus/src/main/java/com/aispace/supersql/mybatisplus/mapper/ExecuteSqlMapper.java`

#### 最后看 console / mcp / ui

- `SuperSQL/super-sql-console/*`
- `SuperSQL/super-sql-mcp/*`
- `SuperSQL/super-sql-ui/*`

### 这一阶段必须理解的深层概念

1. `train` 本质上不是微调，而更像结构化知识写入向量库
2. Text-to-SQL 不是“直接从自然语言生 SQL”，而是“检索增强 + prompt 组织 + SQL 执行”
3. 主项目并没有把 SuperSQL 当成主 UI，而是通过 `RagTool` 把它作为工具接入

---

## 阶段 7：做一次真正的端到端复盘

到了最后一阶段，不要再按模块读，而要按“用户一次真实操作”来串起来。

### 复盘路线 A：知识库问答链路

1. `KnowHubView.vue`
2. `KnowHubApi.ts`
3. `KnowledgeController.java`
4. `AliOssFileServiceImpl.java`
5. `AliOssUtil.java`
6. `RagChatView.vue`
7. `StreamApi.ts`
8. `AiRagController.java`
9. `MetadataAwareQuestionAnswerAdvisor.java`

### 复盘路线 B：统计类结构化问答链路

1. `RagChatView.vue`
2. `StreamApi.ts`
3. `AiRagController.java`
4. `RagTool.java`
5. `SuperSqlIntegrationService.java`
6. `SuperSQL/super-sql-core/*`
7. `SuperSQL/super-sql-mybatis-plus/*`

### 复盘路线 C：运营分析链路

1. `@Loggable`
2. `LoggingAspect.java`
3. `LogInfoController.java`
4. `TaskJobScheduled.java`
5. `WordFrequencyController.java`
6. `WordFrequencyView.vue`

如果这三条复盘路线你都能讲清楚，你对项目就已经不只是“看过”，而是基本吃透了。

---

## 6. 文件 / 类作用索引（源码地图）

下面这部分是你要的“每个类/文件作用”地图。  
说明：后端与 SuperSQL 的 Java 文件按包分组；前端按页面、API、路由、基础设施分组。为了可读性，这里用**一行一句**说明它们的主要职责。

---

## 6.1 后端类文件作用索引（`rag-ai-back/src/main/java/com/le/rag`）

### 入口

- `LeDonRagAiApplication.java`：Spring Boot 主启动类，开启定时任务。

### controller

- `AiRagController.java`：RAG 问答主入口，负责问答、来源过滤、检索增强与工具挂载。
- `ChatController.java`：普通流式聊天入口，不依赖知识库检索。
- `DrawImageController.java`：AI 绘图入口，返回图片流。
- `KnowledgeController.java`：知识库文件上传、查询、删除、下载入口。
- `LogInfoController.java`：日志数据查询与批量处理入口。
- `SensitiveCategoryController.java`：敏感词分类管理入口。
- `SensitiveWordController.java`：敏感词管理入口。
- `UserController.java`：登录、注册、用户信息与密码修改入口。
- `WordFrequencyController.java`：热词统计查询入口。

### config

- `AliOssProperties.java`：文件存储相关配置对象。
- `ApplicationConfig.java`：Spring MVC 配置、拦截器注册、基础 Bean 注册。
- `ChatModelConfig.java`：聊天模型 Bean 配置。
- `EmbeddingModelConfig.java`：Embedding 模型 Bean 配置。
- `JwtProperties.java`：JWT 相关配置对象。
- `MyBatisPlusConfig.java`：MyBatis-Plus 配置。
- `OssConfiguration.java`：文件存储实现相关 Bean 配置。
- `RedisConfig.java`：Redis 相关配置。

### common

- `ApplicationConstant.java`：应用公共常量，如 API 前缀。
- `BaseResponse.java`：统一响应包装对象。
- `ChatType.java`：聊天类型相关枚举/定义。
- `ErrorCode.java`：错误码定义。
- `JpaConverterListJson.java`：JPA JSON/List 转换器。
- `JwtTokenUserInterceptor.java`：JWT 登录态拦截器。
- `PageResult.java`：分页结果对象。
- `ResultUtils.java`：统一响应构造工具。

### advisors / tools / annotation / aop / context

- `MetadataAwareQuestionAnswerAdvisor.java`：把检索文档与来源信息拼入用户上下文。
- `RagTool.java`：提供给模型调用的结构化查询工具入口。
- `Loggable.java`：日志切面的标记注解。
- `LoggingAspect.java`：对带注解的方法做 AOP 请求日志记录。
- `BaseContext.java`：基于 ThreadLocal 保存当前用户上下文。

### service

- `AliOssFileService.java`：知识库文件服务接口。
- `LogInfoService.java`：日志服务接口。
- `SensitiveCategoryService.java`：敏感词分类服务接口。
- `SensitiveWordService.java`：敏感词服务接口。
- `SuperSqlIntegrationService.java`：主项目对 SuperSQL 的封装接入服务。
- `UserService.java`：用户服务接口。
- `WordFrequencyService.java`：热词服务接口。

### service/impl

- `AliOssFileServiceImpl.java`：知识库文件的查询、删除、下载等实现。
- `LogInfoServiceImpl.java`：日志服务实现。
- `SensitiveCategoryServiceImpl.java`：敏感词分类服务实现。
- `SensitiveWordServiceImpl.java`：敏感词服务实现。
- `UserServiceImpl.java`：用户登录、注册、分页、状态等逻辑实现。
- `WordFrequencyServiceImpl.java`：热词统计服务实现。

### mapper

- `AliOssFileMapper.java`：知识库文件表 Mapper。
- `LogInfoMapper.java`：日志表 Mapper。
- `SensitiveCategoryMapper.java`：敏感词分类 Mapper。
- `SensitiveWordMapper.java`：敏感词 Mapper。
- `StructuredDataMapper.java`：结构化数据 Mapper（当前主流程中存在感较弱）。
- `UserMapper.java`：用户表 Mapper。
- `WordFrequencyMapper.java`：热词表 Mapper。

### entity

- `AliOssFile.java`：知识库文件元数据实体。
- `LogInfo.java`：日志记录实体。
- `SensitiveCategory.java`：敏感词分类实体。
- `SensitiveWord.java`：敏感词实体。
- `StructuredData.java`：结构化数据实体。
- `User.java`：用户实体。
- `WordFrequency.java`：热词实体。

### pojo/dto

- `PasswordDTO.java`：修改密码请求对象。
- `QueryFileDTO.java`：知识库文件分页/查询请求对象。
- `RagQueryRequest.java`：RAG 查询请求对象。
- `UserDTO.java`：用户新增/编辑请求对象。
- `UserPageQueryDTO.java`：用户分页查询请求对象。
- `WordFrequencyPageQueryDTO.java`：热词分页查询请求对象。

### pojo/vo

- `UserLoginVO.java`：登录成功后的返回对象。

### constant

- `JwtClaimsConstant.java`：JWT claims 键定义。
- `MessageConstant.java`：消息文案常量。
- `PasswordConstant.java`：密码相关常量。
- `StatusConstant.java`：状态值常量。

### exception

- `AccountLockedException.java`：账户锁定异常。
- `AccountNotFoundException.java`：账户不存在异常。
- `BaseException.java`：基础异常类。
- `BusinessException.java`：业务异常类。
- `GlobalExceptionHandler.java`：全局异常处理器。
- `PasswordErrorException.java`：密码错误异常。
- `ThrowUtils.java`：异常抛出辅助工具。

### utils

- `AliOssUtil.java`：文件存储、读取、删除、下载工具。
- `JwtUtil.java`：JWT 生成与解析工具。
- `SearchUtils.java`：搜索相关工具（当前主链路中存在感较弱）。

### scheduled

- `TaskJobScheduled.java`：定时分析日志并生成热词统计。

---

## 6.2 前端文件作用索引（`app/xushu-rag-ai-front/src`）

### 启动与布局

- `main.ts`：前端应用启动入口。
- `App.vue`：应用根组件。
- `layout/BasicLayout.vue`：主布局骨架。
- `components/BasicAside.vue`：侧边菜单组件。
- `components/MDView.vue`：Markdown 展示类组件。

### 路由

- `router/index.ts`：路由实例与全局前置守卫。
- `router/config.ts`：路由表与菜单配置。

### HTTP 基础设施

- `http/config.ts`：API 基址与请求头基础配置。
- `http/index.ts`：axios 实例与拦截器。

### API 封装

- `api/authUtils.ts`：登录失效等鉴权处理辅助。
- `api/common.ts`：接口常量集合。
- `api/ChatApi.ts`：聊天接口封装。
- `api/RagApi.ts`：RAG 接口封装。
- `api/StreamApi.ts`：流式 SSE/聊天请求封装。
- `api/KnowHubApi.ts`：知识库接口封装。
- `api/DrawApi.ts`：绘图接口封装。
- `api/UserApi.ts`：用户接口封装。
- `api/SensitiveApi.ts`：敏感词接口封装。
- `api/LogApi.ts`：日志接口封装。
- `api/FrequencyApi.ts`：热词接口封装。
- `api/ManageApi.ts`：管理类接口封装（可能含历史残留接口）。
- `api/data.ts`：前端数据辅助定义。
- `api/dto.ts`：前端请求参数类型定义。

### 页面（view）

- `view/login/RegisterLoginView.vue`：登录 / 注册 / 个人信息相关页面。
- `view/ragChat/RagChatView.vue`：主 RAG 聊天页面。
- `view/know/KnowHubView.vue`：知识库管理页面。
- `view/draw/DrawImageView.vue`：AI 绘图页面。
- `view/user/UserView.vue`：用户管理页面。
- `view/logInfo/LogInfoView.vue`：日志管理页面。
- `view/frequency/WordFrequencyView.vue`：热词统计可视化页面。
- `view/sensitive/SensitiveWordView.vue`：敏感词管理页面。
- `view/sensitive/SenCategoryView.vue`：敏感词分类管理页面。

### store

- `store/message.ts`：消息相关状态存储。
- `store/options.ts`：配置/选项相关状态存储。

### 类型声明

- `auto-import.d.ts`：自动导入类型声明。
- `components.d.ts`：组件自动注册类型声明。
- `vite-env.d.ts`：Vite 环境类型声明。

---

## 6.3 SuperSQL Java 文件作用索引（按模块）

### `super-sql-core`

#### enumd

- `TrainPolicyType.java`：训练策略类型定义。

#### engine

- `AbstractSqlEngine.java`：SQL 引擎抽象基类。
- `SqlEngine.java`：SQL 引擎接口/抽象定义。
- `SpringSqlEngine.java`：Spring 环境下的 SQL 引擎实现。
- `RagEngine.java`：RAG 引擎接口/抽象定义。
- `SpringRagEngine.java`：Spring 环境下的 RAG 引擎实现。
- `TrainingPlanGenerator.java`：训练计划生成逻辑。

#### vector

- `BaseVectorStore.java`：向量存储抽象。
- `SpringVectorStore.java`：Spring 风格向量存储适配实现。

#### prompt

- `SqlAssistantPrompt.java`：SQL 生成相关 prompt 模板/组装逻辑。

#### util

- `TextProcessor.java`：文本处理工具。
- `SqlExtractorUtils.java`：SQL 抽取/解析辅助工具。

#### service

- `IExecuteSqlService.java`：SQL 执行服务抽象接口。

#### rerank

- `RerankModel.java`：重排序模型抽象。
- `DefaultRerankModel.java`：默认重排序实现。

#### model

- `DocumentWithScore.java`：带分数的文档对象。
- `RerankOptions.java`：重排序选项。
- `RerankRequest.java`：重排序请求对象。
- `RerankResponse.java`：重排序响应对象。
- `RerankResponseMetadata.java`：重排序响应元数据。
- `RerankResultMetadata.java`：重排序结果元数据。

#### factory

- `ChatClientFactory.java`：聊天客户端构造工厂。
- `HeaderBuilder.java`：请求头构造器。
- `RequestFactory.java`：请求对象构造工厂。

#### builder

- `FollowupQuestionsBuilder.java`：追问问题构造器。
- `RagOptions.java`：RAG 选项构造器。
- `SqlEngineBuilder.java`：SQL 引擎构造器。
- `SqlpromptBuilder.java`：SQL Prompt 构造器。
- `TrainBuilder.java`：训练请求构造器。

### `super-sql-spring-boot-starter`

- `SuperSqlAutoConfiguration.java`：自动装配主入口。
- `SuperSQLProperties.java`：SuperSQL 配置对象。
- `SuperSqlMybatisPlusConfiguration.java`：MyBatis-Plus 相关装配。
- `SpringReRankProperties.java`：重排序配置对象。
- `YmlPropertySourceFactory.java`：YAML 配置源工厂。

### `super-sql-mybatis-plus`

- `ExecuteSqlMapper.java`：执行 SQL 的 Mapper。
- `ExecuteSqlService.java`：执行 SQL 的服务实现。

### `super-sql-console`

- `ConsoleApplication.java`：console Spring Boot 启动类。
- `SuperSqlController.java`：SQL 训练 / 结构化能力入口控制器。
- `SuperChatController.java`：聊天相关入口控制器。
- `DocumentController.java`：文档相关控制器。
- `WebCorsFilter.java`：跨域过滤配置。
- `ChatBO.java`：聊天业务对象。
- `TrainBO.java`：训练业务对象。
- `ResponseResult.java`：统一响应对象。
- `ResponseStatus.java`：响应状态定义。

### `super-sql-mcp-webmvc`

- `WebMvcMcpApplication.java`：MCP WebMVC 启动类。
- `SuperSQLMcpConfig.java`：MCP 配置。
- `SuperSQLService.java`：MCP 暴露的 SuperSQL 工具服务。
- `SQLEngineService.java`：SQL 引擎服务实现。
- `ISQLEngineService.java`：SQL 引擎服务接口。

### `super-sql-mcp-stdio`

- `SuperSQLMcpServerApplication.java`：stdio 模式 MCP 启动类。
- `SuperSQLMcpConfig.java`：stdio MCP 配置。
- `SuperSQLService.java`：stdio MCP 工具服务。
- `SQLEngineService.java`：SQL 引擎服务实现。
- `ISQLEngineService.java`：SQL 引擎服务接口。

### `super-sql-mcp-client`

- `McpClientApplication.java`：MCP client 启动类。
- `SuperSQLMcpController.java`：MCP client 控制器。
- `ResponseResult.java`：响应对象。
- `ResponseStatus.java`：响应状态定义。

---

## 6.4 SuperSQL UI 文件作用索引（`SuperSQL/super-sql-ui/src`）

### 启动与根组件

- `main.ts`：SuperSQL UI 启动入口。
- `App.vue`：根组件。
- `vite-env.d.ts`：Vite 类型声明。

### 路由与鉴权

- `router/index.ts`：路由主入口。
- `router/LoginCheckListener.ts`：登录检查监听逻辑。
- `util/auth.ts`：鉴权辅助工具。

### 布局

- `layout/default-layout.vue`：默认布局。
- `layout/page-layout.vue`：页面布局。

### 组件

- `components/header/index.vue`：头部组件。
- `components/footer/index.vue`：底部组件。
- `components/menu/index.vue`：菜单组件。

### API 层

- `api/request.ts`：请求封装基础层。
- `api/interceptors.ts`：请求/响应拦截器。
- `api/chat.ts`：聊天接口封装。
- `api/knowledge.ts`：知识库接口封装。

### 页面

- `views/login/Login.vue`：登录页面。
- `views/login/components/login-form.vue`：登录表单组件。
- `views/login/components/footer.vue`：登录页底部组件。
- `views/home/index.vue`：首页/主页面。
- `views/chat/index.vue`：聊天页面。
- `views/knowledge/index.vue`：知识管理页面。

### 其余

- `store/index.ts`：状态管理入口。
- `common/common.ts`：公共常量/工具。
- `util/useScroll.ts`：滚动相关组合式工具。

---

## 7. 最后一层：如何判断自己“真的吃透了”

如果你能独立回答下面这些问题，就说明你基本吃透了这个项目：

1. 用户上传文件后，数据分别落到了哪里？
2. RAG 问答和普通聊天的后端链路差别是什么？
3. 为什么说 `RagTool` 是主项目和 SuperSQL 的关键接缝？
4. 热词分析的数据源为什么不是“业务表”，而是日志？
5. 前端的 `roles` 为什么不等于后端真正权限体系？
6. SuperSQL 为什么说是“检索增强的 Text-to-SQL”，而不是“训练一个模型”？
7. 这个仓库里哪部分是主产品，哪部分是能力子系统，哪部分是历史/漂移代码？

如果这 7 个问题你都能用自己的话解释清楚，你就不只是“看过项目”，而是真的把它吃进脑子里了。

---

## 8. 给你的最终建议

不要试图一口气读完全部代码。这个项目最好的学习方法不是“扫目录”，而是：

> **先建立地图，再抓主链路，再补横切与治理，最后才进入 SuperSQL 深水区。**

如果你照这份路线走，通常会经历三个明显阶段：

1. **看懂项目在干什么**
2. **看懂项目是怎么实现的**
3. **看懂项目为什么会这样设计，以及哪里还不成熟**

这才是“真正吃透一个项目”的过程。
