# AI-Code 智能代码生成系统

## 项目概述

AI-Code 是一个基于 Spring Boot 和 AI 技术的智能代码生成系统，旨在帮助开发者快速生成高质量的代码和项目结构。系统集成了 LangChain4j 和 LangGraph4j 等先进的 AI 框架，支持多种代码生成场景，包括单文件代码生成、多文件项目生成、Vue 项目构建等。

## 核心功能

### 1. 智能代码生成
- 支持多种代码生成类型（单文件、多文件、HTML等）
- 基于 AI 的代码质量检查
- 代码生成流程的可视化管理

### 2. 项目构建
- Vue 项目自动构建
- 项目结构优化
- 代码文件自动保存和组织

### 3. 资源管理
- 图片资源自动收集和管理
- 支持多种图片类型（Logo、插图、图表等）
- 图片资源的智能分类和处理

### 4. 用户管理
- 用户注册和登录
- 权限控制和认证
- 用户信息管理

### 5. 应用管理
- 应用创建和配置
- 应用部署和管理
- 应用聊天历史记录

### 6. 系统工具
- 文件操作工具（读取、写入、修改、删除）
- 网页截图功能
- 项目下载功能

## 技术栈

### 后端
- **框架**：Spring Boot 3.5.7
- **语言**：Java 21
- **数据库**：MySQL + MyBatis Flex
- **AI 框架**：LangChain4j 1.1.0, LangGraph4j 1.6.0-rc2
- **缓存**：Redis, Caffeine
- **API 文档**：Knife4j OpenAPI 3
- **其他**：Selenium（网页截图）, Alibaba Cloud OSS（对象存储）

### 前端
- **框架**：Vue 3
- **语言**：TypeScript
- **构建工具**：Vite
- **状态管理**：Pinia
- **路由**：Vue Router

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+

### 后端启动
1. 克隆项目：
   ```bash
   git clone <repository-url>
   cd ai-code
   ```

2. 配置数据库：
   - 创建数据库：`CREATE DATABASE ai_code DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   - 执行 SQL 脚本：`sql/create_tables.sql`

3. 配置应用：
   - 修改 `src/main/resources/application.yml` 中的数据库连接信息和其他配置

4. 构建并启动：
   ```bash
   mvn clean package
   java -jar target/ai-code-0.0.1-SNAPSHOT.jar
   ```

### 前端启动
1. 进入前端目录：
   ```bash
   cd ai-code-frontend
   ```

2. 安装依赖：
   ```bash
   npm install
   ```

3. 启动开发服务器：
   ```bash
   npm run dev
   ```

4. 构建生产版本：
   ```bash
   npm run build
   ```

## 项目结构

### 后端结构
```
src/main/java/com/chao/aicode/
├── ai/              # AI 相关服务和工具
│   ├── model/       # AI 模型和消息定义
│   └── tools/       # AI 工具集
├── annotation/      # 自定义注解
├── aop/             # 面向切面编程
├── common/          # 通用常量和响应结构
├── config/          # 系统配置
├── controller/      # API 控制器
├── converter/       # 数据转换
├── core/            # 核心业务逻辑
│   ├── builder/     # 项目构建器
│   ├── handler/     # 流处理
│   ├── parser/      # 代码解析器
│   └── saver/       # 代码保存器
├── exception/       # 异常处理
├── generator/       # 代码生成器
├── langgraph4j/     # LangGraph4j 工作流
│   ├── ai/          # AI 服务
│   ├── model/       # 模型定义
│   ├── node/        # 工作流节点
│   └── tools/       # 工作流工具
├── manager/         # 第三方服务管理
├── mapper/          # MyBatis 映射器
├── model/           # 数据模型
│   ├── dto/         # 数据传输对象
│   ├── entity/      # 实体类
│   ├── enums/       # 枚举类
│   └── vo/          # 视图对象
├── service/         # 业务服务
├── util/            # 工具类
└── AiCodeApplication.java  # 应用入口
```

### 前端结构
```
ai-code-frontend/src/
├── api/             # API 调用
├── components/      # 组件
├── config/          # 配置
├── layouts/         # 布局
├── pages/           # 页面
│   ├── admin/       # 管理员页面
│   ├── app/         # 应用相关页面
│   └── user/        # 用户相关页面
├── router/          # 路由
├── stores/          # 状态管理
├── utils/           # 工具
├── App.vue          # 应用根组件
└── main.ts          # 前端入口
```

## API 文档

系统集成了 Knife4j OpenAPI 3，启动应用后可通过以下地址访问 API 文档：

```
http://localhost:8080/doc.html
```

## 核心工作流程

1. **代码生成流程**：
   - 用户输入代码生成需求
   - 系统根据需求类型路由到对应服务
   - AI 生成代码
   - 代码质量检查
   - 代码解析和保存

2. **Vue 项目构建流程**：
   - 用户配置项目信息
   - 系统生成项目结构
   - AI 生成组件和页面代码
   - 项目打包和下载

3. **图片资源收集流程**：
   - 用户指定图片需求
   - 系统生成图片收集计划
   - 智能搜索和生成图片
   - 图片分类和管理

## 配置说明

### 主要配置文件
- `src/main/resources/application.yml`：系统主配置
- `src/main/resources/prompt/`：AI 提示模板

### 关键配置项
- 数据库连接信息
- Redis 配置
- OpenAI API 配置
- 阿里云 OSS 配置
- 其他第三方服务配置

## 部署说明

### 生产环境部署
1. 构建后端：
   ```bash
   mvn clean package -DskipTests
   ```

2. 构建前端：
   ```bash
   cd ai-code-frontend
   npm run build
   ```

3. 部署后端应用到服务器
4. 部署前端静态文件到 Web 服务器

### Docker 部署（可选）
项目支持 Docker 部署，可根据需要创建 Dockerfile 和 docker-compose.yml 文件。

## 开发指南

### 代码规范
- 遵循 Java 代码规范
- 前端遵循 Vue 3 和 TypeScript 最佳实践
- 提交代码前确保通过代码质量检查

### 扩展开发
- 新增 AI 工具：在 `ai/tools/` 目录下创建新工具
- 新增工作流节点：在 `langgraph4j/node/` 目录下创建新节点
- 新增 API 接口：在 `controller/` 目录下创建新控制器

## 测试

### 单元测试
```bash
mvn test
```

### 集成测试
系统提供了完整的集成测试用例，可通过以下命令运行：
```bash
mvn verify
```

## 常见问题

1. **AI 模型连接失败**：
   - 检查 API Key 配置
   - 检查网络连接
   - 检查模型服务状态

2. **项目构建失败**：
   - 检查前端依赖安装
   - 检查 Node.js 版本
   - 检查构建配置

3. **数据库连接失败**：
   - 检查数据库服务状态
   - 检查连接配置
   - 检查数据库权限

## 许可证

本项目采用 MIT 许可证。

## 联系我们

如有问题或建议，请通过以下方式联系我们：

- 邮箱：contact@example.com
- GitHub Issues：<repository-url>/issues

---

**AI-Code 智能代码生成系统** - 让代码生成更智能、更高效！