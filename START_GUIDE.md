# XU-News-AI-RAG 启动指南

完整的项目启动步骤，让您快速运行整个系统。

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │────▶│   Backend       │────▶│  Python Service │
│  (Next.js)      │     │ (Spring Boot)   │     │   (FastAPI)     │
│  Port: 3000     │     │  Port: 8080     │     │   Port: 8000    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │                        │
                               ▼                        ▼
                        ┌─────────────┐         ┌─────────────┐
                        │ PostgreSQL  │         │  ChromaDB   │
                        │ Port: 5432  │         │  (embedded) │
                        └─────────────┘         └─────────────┘
                               │
                               ▼
                        ┌─────────────┐
                        │   Ollama    │
                        │ Port: 11434 │
                        └─────────────┘
```

## 前置要求

确保以下软件已安装：

- ✅ Java 17 或更高版本
- ✅ Maven 3.6+
- ✅ Python 3.8+
- ✅ Node.js 18+
- ✅ PostgreSQL 12+ (或使用H2内存数据库)
- ✅ Ollama

## 第一步：安装和配置 Ollama

### 1.1 安装 Ollama

```bash
# Linux / WSL
curl -fsSL https://ollama.ai/install.sh | sh

# macOS
brew install ollama

# Windows
# 下载安装程序：https://ollama.ai/download
```

### 1.2 启动 Ollama 服务

```bash
# 启动 Ollama 服务（后台运行）
ollama serve
```

### 1.3 下载模型

```bash
# 下载 qwen2.5:3b 模型（约2GB）
ollama pull qwen2.5:3b

# 验证模型
ollama list
```

## 第二步：配置数据库

### 选项A：使用 PostgreSQL（推荐生产环境）

```bash
# 安装 PostgreSQL (Ubuntu/Debian)
sudo apt update
sudo apt install postgresql postgresql-contrib

# 创建数据库和用户
sudo -u postgres psql
CREATE DATABASE xu_news;
CREATE USER xu WITH PASSWORD 'xu_pass';
GRANT ALL PRIVILEGES ON DATABASE xu_news TO xu;
\q
```

### 选项B：使用 H2 内存数据库（开发测试）

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:xu_news
    driver-class-name: org.h2.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
```

## 第三步：启动 Python 服务

### 3.1 安装依赖

```bash
cd python_service
pip install -r requirements.txt
```

首次运行会自动下载以下模型（约500MB）：
- `all-MiniLM-L6-v2` (嵌入模型)
- `ms-marco-MiniLM-L-6-v2` (重排模型)

### 3.2 启动服务

```bash
# 方式1：直接运行
python main.py

# 方式2：使用 uvicorn
uvicorn main:app --reload --port 8000

# 后台运行（Linux/Mac）
nohup python main.py > python_service.log 2>&1 &
```

验证服务：
```bash
curl http://localhost:8000/health
```

预期输出：
```json
{
  "status": "healthy",
  "embedding_model": "all-MiniLM-L6-v2",
  "rerank_model": "ms-marco-MiniLM-L-6-v2",
  "chromadb_documents": 0
}
```

## 第四步：配置后端

### 4.1 配置环境变量

```bash
cd backend
cp .env.example .env
# 编辑 .env 文件，填入实际配置
```

关键配置项：

```bash
# 数据库（必须）
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/xu_news
SPRING_DATASOURCE_USERNAME=xu
SPRING_DATASOURCE_PASSWORD=xu_pass

# JWT密钥（必须，至少256位）
JWT_SECRET=your-very-long-secret-key-at-least-256-bits

# 邮件（可选，用于通知）
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-gmail-app-password
EMAIL_FROM=your-email@gmail.com
```

### 4.2 编译和启动

```bash
# 编译
mvn clean install

# 启动
mvn spring-boot:run
```

验证服务：
```bash
curl http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@example.com","password":"admin123"}'
```

## 第五步：启动前端（可选）

如果项目有前端：

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:3000

## 第六步：验证系统

### 6.1 注册用户

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 6.2 登录获取Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

保存返回的 `token`。

### 6.3 触发RSS抓取

```bash
curl -X POST http://localhost:8080/api/ingestion/rss \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "feedUrls": [
      "https://techcrunch.com/feed/"
    ]
  }'
```

### 6.4 查询新闻

```bash
curl http://localhost:8080/api/news \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6.5 语义查询

```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "query": "人工智能最新进展",
    "topK": 5,
    "useRerank": true
  }'
```

### 6.6 聚类分析

```bash
curl http://localhost:8080/api/analytics/clusters?nClusters=10 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 常见问题

### 问题1：Python服务无法连接

**症状**：后端日志显示连接Python服务失败

**解决**：
```bash
# 检查Python服务是否运行
curl http://localhost:8000/health

# 查看Python服务日志
tail -f python_service.log
```

### 问题2：Ollama无法连接

**症状**：查询返回错误，无法生成答案

**解决**：
```bash
# 检查Ollama服务
curl http://localhost:11434/api/tags

# 重启Ollama
pkill ollama
ollama serve &

# 验证模型
ollama list
```

### 问题3：数据库连接失败

**症状**：后端启动失败，数据库连接错误

**解决**：
```bash
# 检查PostgreSQL
sudo systemctl status postgresql

# 测试连接
psql -U xu -d xu_news -h localhost
```

### 问题4：JWT token验证失败

**症状**：所有API返回401

**解决**：
- 确保 `JWT_SECRET` 至少256位
- 检查token是否正确携带在 `Authorization: Bearer <token>` 头中
- Token有效期为24小时，过期需重新登录

### 问题5：邮件发送失败

**症状**：RSS抓取成功但未收到邮件

**解决**：
- Gmail需要使用应用专用密码，不能使用账号密码
- 检查 `EMAIL_NOTIFICATION_ENABLED=true`
- 查看后端日志确认SMTP错误

## 快速启动脚本

创建 `start_all.sh`（Linux/Mac）：

```bash
#!/bin/bash

# 启动Ollama
echo "Starting Ollama..."
ollama serve &
sleep 3

# 启动Python服务
echo "Starting Python Service..."
cd python_service
python main.py &
cd ..
sleep 5

# 启动后端
echo "Starting Backend..."
cd backend
mvn spring-boot:run &
cd ..

echo "All services started!"
echo "Backend: http://localhost:8080"
echo "Python Service: http://localhost:8000"
echo "Ollama: http://localhost:11434"
```

使用：
```bash
chmod +x start_all.sh
./start_all.sh
```

## 停止服务

```bash
# 停止所有Java进程
pkill -f spring-boot

# 停止Python服务
pkill -f "python main.py"

# 停止Ollama
pkill ollama
```

## 下一步

1. 📖 阅读 [backend/README.md](backend/README.md) 了解完整API文档
2. 📖 阅读 [python_service/README.md](python_service/README.md) 了解Python服务详情
3. 🔧 配置 `feeds.yaml` 添加你的RSS源
4. 📧 配置邮件通知
5. 🎨 自定义前端界面

## 获取帮助

- 查看日志文件排查问题
- 检查各服务的健康检查端点
- 参考各服务的README文档

祝使用愉快！🎉

