# XU-News-AI-RAG (Improved)

Goal: Build a personalized news RAG knowledge base supporting RSS/web scraping, embeddin:XXx;xgs & vector search, local LLM reasoning via Ollama (qwen2.5::3b), Spring Security + JWT authentication, file imports, email notifications, clustering analysis, and bilingual (EN/CN) UI/docs.

Quick start (dev):
1. Start Ollama locally.
2. Run embedding service (see docker-compose).
3. Configure backend database and SMTP in backend/application.yml.
4. Start backend and frontend dev servers.

Structure:
- backend/ (Spring Boot)
- embedding-service/ (Python + sentence-transformers + Chroma/Milvus)
- frontend/ (React + Ant Design, i18n)
- docs/
