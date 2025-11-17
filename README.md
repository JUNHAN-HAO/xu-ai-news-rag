# XU-News-AI-RAG: Personalized News Intelligent Knowledge Base

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Python](https://img.shields.io/badge/Python-3.8%2B-blue.svg)](https://www.python.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0%2B-green.svg)](https://spring.io/projects/spring-boot)
[![Ollama](https://img.shields.io/badge/Ollama-qwen2.5:3b-orange.svg)](https://ollama.com/)

## Project Overview

XU-News-AI-RAG is an advanced, AI-powered knowledge base system designed for personalized news aggregation and intelligent querying. It leverages RSS feeds, web scraping (with ethical compliance), and Retrieval-Augmented Generation (RAG) to build a robust, searchable repository of news articles. Deployed with local LLMs via Ollama, the system supports semantic search, data management, and automated workflows, making it ideal for users seeking curated, context-aware news insights. This project extends the foundational concepts from [Auto-Blog](https://github.com/kliewerdaniel/autoblog01) by shifting focus to news-specific RAG, integrating Spring Security for secure user authentication, and adding features like email notifications and clustering analytics.

Key goals:
- Automate news ingestion while respecting robots.txt and rate limits.
- Enable secure, user-friendly knowledge base interactions.
- Provide fallback web searches for comprehensive query responses.

## ✅ Implementation Status

**All core features have been fully implemented!** 🎉

- ✅ RSS Feed Ingestion with Scheduling
- ✅ Email Notifications  
- ✅ Spring Security + JWT Authentication
- ✅ Knowledge Base Management (CRUD)
- ✅ Semantic Query with Reranking
- ✅ Web Search Fallback
- ✅ Clustering Analytics
- ✅ Ollama Integration
- ✅ ChromaDB Vector Database
- ✅ File Upload (Excel, Text)

📖 **Quick Start**: See [START_GUIDE.md](START_GUIDE.md) for detailed setup instructions  
📋 **Implementation Details**: See [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)

## Features

1. **Automated News Ingestion**:
   - Scheduled RSS feed parsing and ethical web scraping using tools like Feedparser and Selenium.
   - Intelligent agent-based fetching for dynamic content, ensuring compliance with crawling norms.

2. **Local AI-Powered Knowledge Base**:
   - Built with ChromaDB for vector storage, supporting both structured (Excel/CSV) and unstructured data.
   - Embedding model: `all-MiniLM-L6-v2` for efficient semantic vectorization.
   - Reranking model: `ms-marco-MiniLM-L-6-v2` for refined result relevance.

3. **LLM Integration**:
   - Ollama deployment with `qwen2.5:3b` for local inference, enabling RAG-based generation and query augmentation.

4. **Email Notifications**:
   - Automatic alerts on successful data ingestion, with customizable titles and content via SMTP.

5. **Secure User Authentication**:
   - JWT-based login using Spring Security, with role-based access for admin and standard users.

6. **Knowledge Base Management**:
   - Dashboard for viewing data lists (filter by type, time, tags).
   - Single/batch delete, metadata editing (e.g., tags, sources), and multi-format uploads (PDF, Excel, text).

7. **Semantic Query Interface**:
   - User-driven natural language search with similarity-ranked results from the knowledge base.
   - Fallback to real-time web search (e.g., Baidu API) if no matches found, returning top 3 results refined by LLM.

8. **Analytics & Insights**:
   - Top-10 keyword distribution via clustering reports, generated on-demand for knowledge base overviews.

## Technology Stack

- **Backend**: Spring Boot (Java) for API, authentication, and scheduling; Python for ingestion scripts.
- **Frontend**: Next.js (React, TypeScript) with Tailwind CSS for responsive UI.
- **AI/LLM**: Ollama (`qwen2.5:3b`), Hugging Face Transformers for embeddings (`all-MiniLM-L6-v2`, `ms-marco-MiniLM-L-6-v2`).
- **Vector Database**: ChromaDB for RAG retrieval.
- **Ingestion**: Feedparser (RSS), Selenium/BeautifulSoup (scraping), Apache Commons for Excel handling.
- **Security**: Spring Security with JWT tokens.
- **Other**: Quartz Scheduler (tasks), Spring Mail (notifications), scikit-learn (clustering), Baidu Search API (fallback).

## 🚀 Quick Start

### Automated Setup (Recommended)

```bash
# 1. Clone repository
git clone https://github.com/yourusername/xu-ai-news-rag.git
cd xu-ai-news-rag

# 2. Install prerequisites
# - Java 17+, Maven 3.6+, Python 3.8+, Ollama

# 3. Run startup script
./start_services.sh
```

The script will:
- ✅ Start Ollama service
- ✅ Download qwen2.5:3b model
- ✅ Install Python dependencies
- ✅ Start Python service (port 8000)
- ✅ Compile and start Backend (port 8080)

### Manual Setup

See detailed instructions in [START_GUIDE.md](START_GUIDE.md)

### Prerequisites

- Java 17+ (for Spring Boot)
- Maven 3.6+
- Python 3.8+ (for AI services)
- Ollama (for LLM)
- PostgreSQL 12+ (or H2 for dev)

### Configuration

```bash
# Copy and edit environment config
cp env.example .env
# Edit .env with your settings (database, email, API keys)
```

### Stop Services

```bash
./stop_services.sh
```

## Usage

- **Login**: Navigate to `/login` and authenticate via JWT.
- **Ingest News**: Trigger via scheduler or manual API call (`/api/ingest`).
- **Query Knowledge Base**: Use `/search` endpoint or UI search bar for semantic queries.
- **Manage Data**: Dashboard at `/dashboard` for CRUD operations.
- **View Analytics**: `/analytics` for keyword clusters.

Example RAG Query Flow:
1. User asks: "Latest AI news on ethics."
2. Retrieve top matches from ChromaDB (reranked).
3. If empty, query Baidu API, fetch top 3, embed, and LLM-summarize.

## Key Integrations (Adapted from Auto-Blog)

- **RSS Ingestion**: Extended from `automated_blog_generator.py` to `news_ingestor.py`, adding Excel parsing and email hooks.
- **Ollama RAG**: Updated `llm_client.py` to use `qwen2.5:3b`; integrated with Spring endpoints.
- **ChromaDB Management**: Enhanced `vector_store.py` for uploads/deletes; UI tied to Spring Security.
- **Fallback Search**: New `web_fallback.py` using Baidu API, chained to LLM for response generation.

## Contributing

1. Fork the repo.
2. Create a feature branch (`git checkout -b feature/news-fallback`).
3. Commit changes (`git commit -m 'Add Baidu fallback'`).
4. Push and open a PR.

Follow PEP8 for Python and Spring conventions for Java. Report issues via GitHub.

## License

MIT License - see [LICENSE](LICENSE) for details.

---

## Suggested Initial Adjustments

Based on the Auto-Blog foundation, prioritize these adjustments to align with your XU-News-AI-RAG requirements. Start with core backend integrations before UI polish—this ensures a functional MVP quickly. Estimated order (1-2 days per step, assuming familiarity):

1. **Integrate Specific Ollama Model and Embeddings (High Priority - 1 Day)**:
   - In `agent/llm_client.py` and `vector_store.py`, replace `llama2:13b-chat` with `qwen2.5:3b`.
   - Update SentenceTransformers to `all-MiniLM-L6-v2` for embeddings; add `ms-marco-MiniLM-L-6-v2` for reranking in the RAG pipeline (use Hugging Face pipeline).
   - Test: Run `python automated_blog_generator.py` (rename to `news_ingestor.py`) and verify vectors in ChromaDB.

2. **Add Spring Security JWT for User Login (High Priority - 1-2 Days)**:
   - Shift auth from GitHub OAuth to Spring Boot backend: Add Spring Security dependencies (`spring-boot-starter-security`, `jjwt`).
   - Implement `/auth/login` and `/auth/register` endpoints with JWT generation.
   - Secure all management APIs (e.g., `/api/kb/delete`). Update Next.js frontend to use JWT tokens for API calls.
   - Why first? This gates all user-facing features like management and queries.

3. **Enhance Ingestion with Scheduling, Excel Support, and Email (Medium Priority - 1 Day)**:
   - Use Quartz in Spring Boot for RSS timing (e.g., cron every 6 hours).
   - In `news_ingestor.py`, add pandas/openpyxl for Excel structured data parsing.
   - Integrate `smtplib` or Spring Mail for post-ingestion emails (customize subject: "New News Ingested: {count} Items").

4. **Implement Knowledge Base Management UI (Medium Priority - 1 Day)**:
   - Extend Next.js dashboard: Add filters (type/time), batch delete/edit forms, and file upload (handle via Spring multipart API).
   - Tie to ChromaDB CRUD operations in backend.

5. **Add Semantic Query with Fallback and Clustering (Lower Priority - 1-2 Days)**:
   - Enhance search endpoint: If ChromaDB score < threshold, call Baidu API (use `requests` with your key), embed results, and pipe to Ollama for top-3 summary.
   - For clustering: Add `scikit-learn` KMeans in a new `analytics.py`; generate Top-10 report as JSON for UI chart (e.g., via Recharts).

Test incrementally: After each step, run end-to-end (ingest → query → email). If stuck on Spring integration, reference official docs. Once done, push to your `xu-ai-news-rag` repo! Let me know if you need code snippets for any part.
