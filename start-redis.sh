#!/bin/bash

echo "=== Redis启动脚本 ==="
echo "请选择启动方式:"
echo "1. Docker (推荐)"
echo "2. Homebrew"
echo "3. Kubernetes"
echo "4. 退出"

read -p "请输入选项 [1-4]: " choice

case $choice in
    1)
        echo "使用Docker启动Redis..."
        if command -v docker &> /dev/null; then
            docker run -d -p 6379:6379 --name redis-demo redis:7.2-alpine
            echo "Redis已启动，使用: docker ps 查看状态"
            echo "停止命令: docker stop redis-demo"
            echo "删除命令: docker rm redis-demo"
        else
            echo "错误: Docker未安装"
        fi
        ;;
    2)
        echo "使用Homebrew启动Redis..."
        if command -v brew &> /dev/null; then
            brew services start redis
            echo "Redis已启动，使用: brew services list 查看状态"
        else
            echo "错误: Homebrew未安装"
        fi
        ;;
    3)
        echo "使用Kubernetes部署Redis..."
        if command -v kubectl &> /dev/null; then
            cd k8s
            ./deploy-redis.sh
        else
            echo "错误: kubectl未安装"
        fi
        ;;
    4)
        echo "退出"
        exit 0
        ;;
    *)
        echo "无效选项"
        ;;
esac

echo ""
echo "测试连接:"
echo "redis-cli ping"
echo "或运行Java程序: mvn compile exec:java -Dexec.mainClass="org.example.RedisExamples""