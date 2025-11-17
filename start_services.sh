#!/bin/bash

# XU-News-AI-RAG 服务启动脚本

set -e

echo "==================================="
echo "XU-News-AI-RAG 服务启动"
echo "==================================="

# 检查必要的命令
command -v ollama >/dev/null 2>&1 || { echo "错误: 需要安装 ollama"; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "错误: 需要安装 python3"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "错误: 需要安装 maven"; exit 1; }

# 1. 启动 Ollama
echo ""
echo "1️⃣  检查 Ollama 服务..."
if pgrep -x "ollama" > /dev/null; then
    echo "✅ Ollama 已在运行"
else
    echo "🚀 启动 Ollama..."
    ollama serve > ollama.log 2>&1 &
    sleep 3
    echo "✅ Ollama 已启动"
fi

# 检查模型
echo "📦 检查 qwen2.5:3b 模型..."
if ollama list | grep -q "qwen2.5:3b"; then
    echo "✅ 模型已存在"
else
    echo "📥 下载 qwen2.5:3b 模型（这可能需要几分钟）..."
    ollama pull qwen2.5:3b
fi

# 2. 启动 Python 服务
echo ""
echo "2️⃣  启动 Python 服务..."
cd python_service

# 检查依赖
if [ ! -d "venv" ]; then
    echo "📦 创建虚拟环境..."
    python3 -m venv venv
fi

source venv/bin/activate
pip install -q -r requirements.txt

echo "🚀 启动 Python 服务..."
nohup python main.py > ../python_service.log 2>&1 &
PYTHON_PID=$!
cd ..

# 等待服务启动
sleep 5

# 验证Python服务
if curl -s http://localhost:8000/health > /dev/null; then
    echo "✅ Python 服务已启动 (PID: $PYTHON_PID)"
else
    echo "❌ Python 服务启动失败，查看 python_service.log"
    exit 1
fi

# 3. 启动 Spring Boot 后端
echo ""
echo "3️⃣  启动 Spring Boot 后端..."
cd backend

# 检查环境配置
if [ ! -f ".env" ]; then
    echo "⚠️  警告: .env 文件不存在，使用默认配置"
    echo "建议: cp .env.example .env 并修改配置"
fi

echo "🔨 编译后端..."
mvn clean install -DskipTests -q

echo "🚀 启动后端服务..."
nohup mvn spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
cd ..

# 等待服务启动
echo "⏳ 等待后端启动（这可能需要1-2分钟）..."
for i in {1..60}; do
    if curl -s http://localhost:8080/api/auth/register > /dev/null 2>&1; then
        echo "✅ 后端服务已启动 (PID: $BACKEND_PID)"
        break
    fi
    sleep 2
    if [ $i -eq 60 ]; then
        echo "❌ 后端启动超时，查看 backend.log"
        exit 1
    fi
done

# 显示服务状态
echo ""
echo "==================================="
echo "✅ 所有服务启动成功！"
echo "==================================="
echo ""
echo "📌 服务地址："
echo "   - 后端 API:      http://localhost:8080"
echo "   - Python 服务:   http://localhost:8000"
echo "   - Ollama:        http://localhost:11434"
echo ""
echo "📋 进程 ID："
echo "   - Python:        $PYTHON_PID"
echo "   - Backend:       $BACKEND_PID"
echo ""
echo "📝 日志文件："
echo "   - Python:        python_service.log"
echo "   - Backend:       backend.log"
echo "   - Ollama:        ollama.log"
echo ""
echo "🛑 停止服务："
echo "   ./stop_services.sh"
echo ""
echo "📖 下一步："
echo "   1. 注册用户: curl -X POST http://localhost:8080/api/auth/register ..."
echo "   2. 查看启动指南: cat START_GUIDE.md"
echo ""

