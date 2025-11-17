"""
Python Integration Service for XU-News-AI-RAG
提供嵌入、重排、聚类和向量数据库操作
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer, CrossEncoder
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np
import logging
import os

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize FastAPI
app = FastAPI(title="XU-News-AI-RAG Python Service", version="1.0.0")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize models
logger.info("Loading embedding model...")
embedding_model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')

logger.info("Loading reranking model...")
rerank_model = CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2')

# Initialize ChromaDB
logger.info("Initializing ChromaDB...")
chroma_client = chromadb.PersistentClient(
    path=os.getenv("CHROMA_DB_PATH", "./chroma_db"),
    settings=Settings(anonymized_telemetry=False)
)

COLLECTION_NAME = "news_articles"

def get_or_create_collection():
    """Get or create ChromaDB collection"""
    try:
        collection = chroma_client.get_collection(name=COLLECTION_NAME)
        logger.info(f"Using existing collection: {COLLECTION_NAME}")
    except:
        collection = chroma_client.create_collection(
            name=COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"}
        )
        logger.info(f"Created new collection: {COLLECTION_NAME}")
    return collection


# Pydantic models
class EmbedRequest(BaseModel):
    texts: List[str]


class EmbedResponse(BaseModel):
    embeddings: List[List[float]]


class AddDocumentRequest(BaseModel):
    id: str
    text: str
    metadata: Dict[str, Any]


class AddDocumentsRequest(BaseModel):
    documents: List[AddDocumentRequest]


class SearchRequest(BaseModel):
    query: str
    top_k: int = 10


class SearchResult(BaseModel):
    id: str
    text: str
    score: float
    metadata: Dict[str, Any]


class SearchResponse(BaseModel):
    results: List[SearchResult]


class RerankRequest(BaseModel):
    query: str
    documents: List[Dict[str, Any]]  # [{id, text, ...}]
    top_k: int = 5


class RerankResult(BaseModel):
    id: str
    text: str
    score: float
    metadata: Dict[str, Any]


class RerankResponse(BaseModel):
    results: List[RerankResult]


class ClusterRequest(BaseModel):
    texts: List[str]
    n_clusters: int = 10


class ClusterResponse(BaseModel):
    clusters: List[Dict[str, Any]]  # [{cluster_id, keywords, count}]
    top_keywords: List[str]


class DeleteDocumentRequest(BaseModel):
    ids: List[str]


# Endpoints
@app.get("/")
async def root():
    return {
        "service": "XU-News-AI-RAG Python Service",
        "status": "running",
        "version": "1.0.0"
    }


@app.get("/health")
async def health():
    collection = get_or_create_collection()
    doc_count = collection.count()
    return {
        "status": "healthy",
        "embedding_model": "all-MiniLM-L6-v2",
        "rerank_model": "ms-marco-MiniLM-L-6-v2",
        "chromadb_documents": doc_count
    }


@app.post("/embed", response_model=EmbedResponse)
async def embed_texts(request: EmbedRequest):
    """Generate embeddings for texts"""
    try:
        embeddings = embedding_model.encode(request.texts, convert_to_numpy=True)
        return EmbedResponse(embeddings=embeddings.tolist())
    except Exception as e:
        logger.error(f"Embedding error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/documents/add")
async def add_documents(request: AddDocumentsRequest):
    """Add documents to ChromaDB"""
    try:
        collection = get_or_create_collection()
        
        ids = [doc.id for doc in request.documents]
        texts = [doc.text for doc in request.documents]
        metadatas = [doc.metadata for doc in request.documents]
        
        # Generate embeddings
        embeddings = embedding_model.encode(texts, convert_to_numpy=True)
        
        # Add to ChromaDB
        collection.add(
            ids=ids,
            embeddings=embeddings.tolist(),
            documents=texts,
            metadatas=metadatas
        )
        
        logger.info(f"Added {len(ids)} documents to ChromaDB")
        return {"status": "success", "count": len(ids)}
    except Exception as e:
        logger.error(f"Add documents error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/search", response_model=SearchResponse)
async def search_documents(request: SearchRequest):
    """Semantic search in ChromaDB"""
    try:
        collection = get_or_create_collection()
        
        # Generate query embedding
        query_embedding = embedding_model.encode([request.query], convert_to_numpy=True)
        
        # Search
        results = collection.query(
            query_embeddings=query_embedding.tolist(),
            n_results=request.top_k
        )
        
        # Format results
        search_results = []
        if results['ids'] and len(results['ids'][0]) > 0:
            for i in range(len(results['ids'][0])):
                search_results.append(SearchResult(
                    id=results['ids'][0][i],
                    text=results['documents'][0][i],
                    score=1 - results['distances'][0][i],  # Convert distance to similarity
                    metadata=results['metadatas'][0][i]
                ))
        
        return SearchResponse(results=search_results)
    except Exception as e:
        logger.error(f"Search error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/rerank", response_model=RerankResponse)
async def rerank_documents(request: RerankRequest):
    """Rerank documents using cross-encoder"""
    try:
        # Prepare pairs for reranking
        pairs = [[request.query, doc['text']] for doc in request.documents]
        
        # Get scores
        scores = rerank_model.predict(pairs)
        
        # Sort by score
        ranked_indices = np.argsort(scores)[::-1][:request.top_k]
        
        # Format results
        rerank_results = []
        for idx in ranked_indices:
            doc = request.documents[idx]
            rerank_results.append(RerankResult(
                id=doc.get('id', str(idx)),
                text=doc['text'],
                score=float(scores[idx]),
                metadata=doc.get('metadata', {})
            ))
        
        return RerankResponse(results=rerank_results)
    except Exception as e:
        logger.error(f"Rerank error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/cluster", response_model=ClusterResponse)
async def cluster_texts(request: ClusterRequest):
    """Cluster texts and extract keywords"""
    try:
        if len(request.texts) < request.n_clusters:
            request.n_clusters = max(1, len(request.texts) // 2)
        
        # TF-IDF vectorization
        vectorizer = TfidfVectorizer(max_features=100, stop_words='english')
        tfidf_matrix = vectorizer.fit_transform(request.texts)
        
        # KMeans clustering
        kmeans = KMeans(n_clusters=request.n_clusters, random_state=42)
        clusters = kmeans.fit_predict(tfidf_matrix)
        
        # Extract top keywords per cluster
        feature_names = vectorizer.get_feature_names_out()
        cluster_info = []
        all_keywords = []
        
        for cluster_id in range(request.n_clusters):
            # Get cluster center
            center = kmeans.cluster_centers_[cluster_id]
            top_indices = center.argsort()[-5:][::-1]
            keywords = [feature_names[i] for i in top_indices]
            
            # Count documents in cluster
            count = np.sum(clusters == cluster_id)
            
            cluster_info.append({
                "cluster_id": int(cluster_id),
                "keywords": keywords,
                "count": int(count)
            })
            all_keywords.extend(keywords)
        
        # Get top overall keywords
        from collections import Counter
        top_keywords = [word for word, _ in Counter(all_keywords).most_common(10)]
        
        return ClusterResponse(clusters=cluster_info, top_keywords=top_keywords)
    except Exception as e:
        logger.error(f"Clustering error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/documents")
async def delete_documents(request: DeleteDocumentRequest):
    """Delete documents from ChromaDB"""
    try:
        collection = get_or_create_collection()
        collection.delete(ids=request.ids)
        logger.info(f"Deleted {len(request.ids)} documents from ChromaDB")
        return {"status": "success", "count": len(request.ids)}
    except Exception as e:
        logger.error(f"Delete documents error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/documents/count")
async def get_document_count():
    """Get total document count in ChromaDB"""
    try:
        collection = get_or_create_collection()
        count = collection.count()
        return {"count": count}
    except Exception as e:
        logger.error(f"Count error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

