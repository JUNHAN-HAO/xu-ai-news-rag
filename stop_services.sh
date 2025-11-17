#!/bin/bash

# XU-News-AI-RAG 服务停止脚本

echo "==================================="
echo "停止 XU-News-AI-RAG 服务"
echo "==================================="

# 停止 Spring Boot 后端
echo "🛑 停止后端服务..."
pkill -f "spring-boot" && echo "✅ 后端已停止" || echo "⚠️  后端未运行"

# 停止 Python 服务
echo "🛑 停止 Python 服务..."
pkill -f "python main.py" && echo "✅ Python 服务已停止" || echo "⚠️  Python 服务未运行"

# 停止 Ollama (可选)
read -p "是否停止 Ollama 服务? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    pkill ollama && echo "✅ Ollama 已停止" || echo "⚠️  Ollama 未运行"
fi

echo ""
echo "==================================="
echo "✅ 服务已停止"
echo "==================================="

