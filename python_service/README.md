# Python Integration Service

Python服务用于处理AI相关功能：嵌入、重排、聚类和向量数据库操作。

## 功能

1. **文本嵌入** - 使用 all-MiniLM-L6-v2 模型
2. **文档重排** - 使用 ms-marco-MiniLM-L-6-v2 模型
3. **向量搜索** - ChromaDB 语义搜索
4. **文本聚类** - KMeans 聚类分析
5. **知识库管理** - 添加/删除文档

## 安装

```bash
cd python_service
pip install -r requirements.txt
```

## 运行

```bash
python main.py
# 或
uvicorn main:app --reload --port 8000
```

服务将运行在 http://localhost:8000

## API文档

启动服务后访问：
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 环境变量

- `CHROMA_DB_PATH`: ChromaDB数据存储路径（默认: ./chroma_db）

