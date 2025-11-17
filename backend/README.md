# XU-News-AI-RAG Backend

Spring Boot后端服务，提供新闻RAG系统的核心功能。

## 功能特性

### ✅ 已实现功能

1. **用户认证系统** (Spring Security + JWT)
   - 用户注册/登录
   - JWT token认证
   - 角色权限管理（USER, ADMIN）

2. **定时任务机制**
   - RSS新闻自动抓取
   - 定时任务调度（默认每6小时）
   - 支持手动触发抓取

3. **邮件通知**
   - 新闻入库成功通知
   - 自定义邮件通知
   - 异步邮件发送

4. **知识库管理API**
   - 查看新闻列表（支持分页、筛选）
   - 单条/批量删除
   - 编辑文章元数据
   - 文件上传（Excel、文本）

5. **语义查询功能**
   - 向量语义搜索
   - 重排序优化
   - 与Python服务集成

6. **联网搜索回退**
   - 知识库无结果时自动联网搜索
   - 百度搜索API集成（可配置）

7. **聚类分析**
   - Top-10关键词分布
   - 新闻内容聚类报告

8. **Ollama集成**
   - 本地LLM推理（qwen2.5:3b）
   - 答案生成和摘要

## 技术栈

- **框架**: Spring Boot 3.2.0
- **数据库**: PostgreSQL (可切换H2)
- **安全**: Spring Security + JWT
- **任务调度**: Spring Quartz
- **邮件**: Spring Mail
- **HTTP客户端**: WebFlux WebClient
- **文件处理**: Apache POI

## 快速开始

### 前置要求

- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (或使用H2)
- Python服务运行中 (端口8000)
- Ollama服务运行中 (端口11434)

### 1. 配置数据库

创建数据库：

```bash
createdb xu_news
```

或修改 `application.yml` 使用H2内存数据库。

### 2. 配置环境变量

创建 `.env` 文件或设置环境变量：

```bash
# 数据库
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/xu_news
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password

# JWT密钥（必须至少256位）
export JWT_SECRET=your-secret-key-must-be-at-least-256-bits-long

# 邮件配置
export EMAIL_HOST=smtp.gmail.com
export EMAIL_USERNAME=your-email@gmail.com
export EMAIL_PASSWORD=your-app-password
export EMAIL_FROM=your-email@gmail.com

# Ollama配置
export OLLAMA_URL=http://localhost:11434
export OLLAMA_MODEL=qwen2.5:3b

# Python服务配置
export PYTHON_SERVICE_URL=http://localhost:8000

# 百度搜索API（可选）
export BAIDU_API_KEY=your-baidu-key
export BAIDU_API_SECRET=your-baidu-secret
```

### 3. 编译运行

```bash
# 编译
mvn clean install

# 运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/news-rag-1.0.0.jar
```

服务将运行在 http://localhost:8080

## API文档

### 认证 API

#### 注册
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "user1",
  "email": "user1@example.com",
  "password": "password123"
}
```

#### 登录
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "password123"
}
```

响应：
```json
{
  "token": "eyJhbGc...",
  "username": "user1",
  "email": "user1@example.com",
  "role": "USER"
}
```

#### 获取当前用户
```bash
GET /api/auth/me
Authorization: Bearer <token>
```

### 新闻管理 API

#### 获取新闻列表
```bash
GET /api/news?page=0&size=20&sortBy=publishedAt&direction=DESC
Authorization: Bearer <token>
```

#### 获取单条新闻
```bash
GET /api/news/{id}
Authorization: Bearer <token>
```

#### 更新新闻
```bash
PUT /api/news/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "tags": ["tech", "ai"]
}
```

#### 删除新闻
```bash
DELETE /api/news/{id}
Authorization: Bearer <token>
```

#### 批量删除
```bash
DELETE /api/news/batch
Authorization: Bearer <token>
Content-Type: application/json

[1, 2, 3, 4, 5]
```

### 语义查询 API

#### 查询
```bash
POST /api/query
Authorization: Bearer <token>
Content-Type: application/json

{
  "query": "人工智能最新进展",
  "topK": 5,
  "useRerank": true,
  "allowWebSearch": true
}
```

响应：
```json
{
  "query": "人工智能最新进展",
  "results": [...],
  "answer": "根据检索结果...",
  "fromWeb": false,
  "resultCount": 5
}
```

### 上传 API

#### 上传Excel
```bash
POST /api/upload/excel
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <excel_file>
```

Excel格式要求：
- 第一行为标题行
- 列：标题 | 内容 | URL | 来源

#### 上传文本
```bash
POST /api/upload/text
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "文章标题",
  "content": "文章内容...",
  "source": "手动上传"
}
```

### 聚类分析 API

#### 获取聚类报告
```bash
GET /api/analytics/clusters?nClusters=10
Authorization: Bearer <token>
```

### 抓取管理 API

#### 手动触发RSS抓取
```bash
POST /api/ingestion/rss
Authorization: Bearer <token>
Content-Type: application/json

{
  "feedUrls": [
    "https://techcrunch.com/feed/",
    "https://www.theverge.com/rss/index.xml"
  ]
}
```

## 定时任务

RSS抓取任务默认配置为每6小时运行一次。可在 `application.yml` 中修改：

```yaml
app:
  rss:
    schedule:
      cron: "0 0 */6 * * *"  # 每6小时
```

Cron表达式示例：
- `0 0 */6 * * *` - 每6小时
- `0 0 0 * * *` - 每天午夜
- `0 0 12 * * *` - 每天中午12点
- `0 0/30 * * * *` - 每30分钟

## 邮件通知

成功抓取新闻后会自动发送邮件通知。确保配置：

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # Gmail需使用应用专用密码

app:
  email:
    from: your-email@gmail.com
    notification:
      enabled: true
```

## 数据库迁移

首次运行会自动创建表结构（Hibernate DDL-auto: update）。

生产环境建议使用Flyway或Liquibase进行版本控制。

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 生产部署

1. 修改 `application.yml`：
   - 设置 `ddl-auto: validate`
   - 禁用 `show-sql`
   - 配置生产数据库

2. 使用环境变量或外部配置文件

3. 打包：
```bash
mvn clean package -DskipTests
```

4. 运行：
```bash
java -jar target/news-rag-1.0.0.jar --spring.profiles.active=prod
```

## 故障排查

### 常见问题

1. **JWT错误** - 确保JWT_SECRET至少256位
2. **数据库连接失败** - 检查PostgreSQL服务和连接配置
3. **Python服务连接失败** - 确保Python服务在端口8000运行
4. **Ollama连接失败** - 确保Ollama服务运行且模型已下载
5. **邮件发送失败** - 检查SMTP配置和应用专用密码

### 日志

日志级别可在 `application.yml` 配置：

```yaml
logging:
  level:
    root: INFO
    com.xu.news: DEBUG
```

## 许可证

MIT License

