# API使用示例

本文档提供完整的API调用示例，方便快速测试和集成。

## 🔐 认证流程

### 1. 注册新用户

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**响应**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "testuser",
  "email": "test@example.com",
  "role": "USER"
}
```

### 2. 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**响应**（保存token用于后续请求）:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwNjc...",
  "username": "testuser",
  "email": "test@example.com",
  "role": "USER"
}
```

### 3. 获取当前用户信息

```bash
TOKEN="your_token_here"

curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

## 📰 新闻管理

### 4. 获取新闻列表

```bash
# 基本查询（第1页，每页20条）
curl -X GET "http://localhost:8080/api/news?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 按类型筛选（只看RSS新闻）
curl -X GET "http://localhost:8080/api/news?page=0&size=20&type=RSS" \
  -H "Authorization: Bearer $TOKEN"

# 按时间范围筛选
curl -X GET "http://localhost:8080/api/news?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59" \
  -H "Authorization: Bearer $TOKEN"

# 按标签筛选
curl -X GET "http://localhost:8080/api/news?tag=ai" \
  -H "Authorization: Bearer $TOKEN"

# 排序（按发布时间降序）
curl -X GET "http://localhost:8080/api/news?sortBy=publishedAt&direction=DESC" \
  -H "Authorization: Bearer $TOKEN"
```

**响应**:
```json
{
  "content": [
    {
      "id": 1,
      "title": "AI Breakthrough in 2024",
      "content": "...",
      "summary": "...",
      "url": "https://example.com/article1",
      "source": "TechCrunch",
      "author": "John Doe",
      "publishedAt": "2024-11-15T10:30:00",
      "tags": ["ai", "technology"],
      "contentType": "RSS",
      "createdAt": "2024-11-15T12:00:00"
    }
  ],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### 5. 获取单条新闻

```bash
curl -X GET http://localhost:8080/api/news/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. 更新新闻

```bash
curl -X PUT http://localhost:8080/api/news/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "tags": ["ai", "ml", "tech"],
    "source": "Updated Source"
  }'
```

### 7. 删除单条新闻

```bash
curl -X DELETE http://localhost:8080/api/news/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 8. 批量删除新闻

```bash
curl -X DELETE http://localhost:8080/api/news/batch \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3, 4, 5]'
```

### 9. 获取所有新闻源

```bash
curl -X GET http://localhost:8080/api/news/sources \
  -H "Authorization: Bearer $TOKEN"
```

**响应**:
```json
["TechCrunch", "The Verge", "Wired", "Ars Technica"]
```

### 10. 获取热门标签

```bash
# 获取Top 10标签
curl -X GET "http://localhost:8080/api/news/tags/top?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

**响应**:
```json
["ai", "technology", "innovation", "startup", "cloud"]
```

### 11. 获取统计信息

```bash
curl -X GET http://localhost:8080/api/news/stats \
  -H "Authorization: Bearer $TOKEN"
```

**响应**:
```json
{
  "totalArticles": 1234,
  "totalSources": 20,
  "topTags": ["ai", "tech", "cloud", ...]
}
```

## 🔍 语义查询

### 12. 智能问答查询

```bash
# 基础查询
curl -X POST http://localhost:8080/api/query \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "人工智能最新进展是什么？",
    "topK": 5,
    "useRerank": true,
    "allowWebSearch": true
  }'
```

**响应**:
```json
{
  "query": "人工智能最新进展是什么？",
  "results": [
    {
      "id": "123",
      "title": "GPT-4发布：AI新里程碑",
      "content": "...",
      "score": 0.89,
      "url": "https://...",
      "source": "TechCrunch",
      "metadata": {...}
    }
  ],
  "answer": "根据最新资料，人工智能领域在2024年取得了重大突破...",
  "fromWeb": false,
  "resultCount": 5
}
```

### 13. 查询服务健康状态

```bash
curl -X GET http://localhost:8080/api/query/health \
  -H "Authorization: Bearer $TOKEN"
```

**响应**:
```json
{
  "ollama": true,
  "webSearch": false
}
```

## 📤 文件上传

### 14. 上传Excel文件

```bash
curl -X POST http://localhost:8080/api/upload/excel \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@news_data.xlsx"
```

**Excel格式要求**:
| 标题 | 内容 | URL | 来源 |
|------|------|-----|------|
| AI新突破 | 详细内容... | https://... | 科技日报 |

**响应**:
```json
{
  "status": "success",
  "count": 10,
  "message": "成功上传 10 条数据"
}
```

### 15. 上传文本内容

```bash
curl -X POST http://localhost:8080/api/upload/text \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "手动添加的新闻",
    "content": "这是新闻的详细内容...",
    "source": "手动上传"
  }'
```

**响应**:
```json
{
  "status": "success",
  "id": 456,
  "message": "成功上传文本内容"
}
```

## 📊 聚类分析

### 16. 获取聚类报告

```bash
# 生成10个簇的聚类分析
curl -X GET "http://localhost:8080/api/analytics/clusters?nClusters=10" \
  -H "Authorization: Bearer $TOKEN"
```

**响应**:
```json
{
  "clusters": [
    {
      "cluster_id": 0,
      "keywords": ["ai", "machine", "learning", "model", "data"],
      "count": 145
    },
    {
      "cluster_id": 1,
      "keywords": ["cloud", "aws", "azure", "infrastructure", "computing"],
      "count": 98
    }
  ],
  "top_keywords": ["ai", "technology", "cloud", "innovation", "startup", 
                   "data", "security", "mobile", "software", "internet"]
}
```

## 🔄 RSS抓取

### 17. 手动触发RSS抓取

```bash
# 抓取指定的RSS源
curl -X POST http://localhost:8080/api/ingestion/rss \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "feedUrls": [
      "https://techcrunch.com/feed/",
      "https://www.theverge.com/rss/index.xml",
      "https://arstechnica.com/feed/"
    ]
  }'
```

**响应**:
```json
{
  "status": "success",
  "ingestedCount": 45,
  "message": "成功入库 45 条新闻"
}
```

## 🐍 Python服务API

### 18. 检查Python服务健康

```bash
curl http://localhost:8000/health
```

**响应**:
```json
{
  "status": "healthy",
  "embedding_model": "all-MiniLM-L6-v2",
  "rerank_model": "ms-marco-MiniLM-L-6-v2",
  "chromadb_documents": 1234
}
```

### 19. 文本嵌入

```bash
curl -X POST http://localhost:8000/embed \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "人工智能正在改变世界",
      "机器学习是AI的核心技术"
    ]
  }'
```

**响应**:
```json
{
  "embeddings": [
    [0.123, -0.456, 0.789, ...],
    [0.234, -0.567, 0.890, ...]
  ]
}
```

### 20. 语义搜索

```bash
curl -X POST http://localhost:8000/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "人工智能最新进展",
    "top_k": 5
  }'
```

### 21. 重排序

```bash
curl -X POST http://localhost:8000/rerank \
  -H "Content-Type: application/json" \
  -d '{
    "query": "人工智能",
    "documents": [
      {"id": "1", "text": "AI发展迅速"},
      {"id": "2", "text": "云计算技术"}
    ],
    "top_k": 2
  }'
```

### 22. 聚类分析

```bash
curl -X POST http://localhost:8000/cluster \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "AI和机器学习",
      "云计算和大数据",
      "深度学习算法"
    ],
    "n_clusters": 2
  }'
```

### 23. 获取文档数量

```bash
curl http://localhost:8000/documents/count
```

**响应**:
```json
{
  "count": 1234
}
```

## 📝 完整工作流示例

### 场景：从注册到查询的完整流程

```bash
#!/bin/bash

# 1. 注册用户
echo "1. 注册用户..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@example.com","password":"demo123"}')

TOKEN=$(echo $REGISTER_RESPONSE | jq -r '.token')
echo "Token: $TOKEN"

# 2. 触发RSS抓取
echo "2. 抓取RSS新闻..."
curl -s -X POST http://localhost:8080/api/ingestion/rss \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"feedUrls":["https://techcrunch.com/feed/"]}' | jq

# 等待处理
sleep 10

# 3. 查看新闻列表
echo "3. 查看新闻列表..."
curl -s -X GET "http://localhost:8080/api/news?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN" | jq '.content[] | {id, title, source}'

# 4. 语义查询
echo "4. 执行语义查询..."
curl -s -X POST http://localhost:8080/api/query \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query":"AI最新动态","topK":3}' | jq

# 5. 获取聚类分析
echo "5. 获取聚类分析..."
curl -s -X GET "http://localhost:8080/api/analytics/clusters?nClusters=5" \
  -H "Authorization: Bearer $TOKEN" | jq '.top_keywords'

echo "完成！"
```

## 🧪 测试工具推荐

### Postman Collection

可以导入以下环境变量：
- `base_url`: `http://localhost:8080`
- `token`: `your_jwt_token`

### HTTPie使用

```bash
# 安装
pip install httpie

# 登录
http POST :8080/api/auth/login username=testuser password=password123

# 查询（自动格式化）
http GET :8080/api/news "Authorization:Bearer $TOKEN"
```

### cURL优化技巧

```bash
# 保存cookie
curl -c cookies.txt ...

# 使用cookie
curl -b cookies.txt ...

# 显示详细信息
curl -v ...

# 只看响应头
curl -I ...

# 格式化JSON输出
curl ... | jq .
```

## 🔧 常见问题

### Q: 401 Unauthorized错误？
**A**: 检查token是否正确，是否过期（24小时有效期）

### Q: 500 Internal Server Error？
**A**: 检查后端日志 `backend.log`，可能是服务未启动

### Q: 查询无结果？
**A**: 确保已抓取新闻并入库到知识库

### Q: 邮件未收到？
**A**: 检查 `EMAIL_NOTIFICATION_ENABLED=true` 和SMTP配置

---

**提示**: 所有带有 `$TOKEN` 的请求都需要先登录获取token！

更多API详情请参考: [backend/README.md](backend/README.md)

