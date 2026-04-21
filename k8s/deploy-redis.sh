#!/bin/bash

# 使用说明
show_usage() {
    echo "用法: $0 [命令]"
    echo "命令:"
    echo "  deploy    - 部署Redis (默认)"
    echo "  stop      - 停止Redis"
    echo "  cleanup   - 清理所有Redis资源"
    echo "  validate  - 验证配置文件"
    echo "  logs      - 查看Redis日志"
    echo "  port-forward - 端口转发调试"
    echo "  restart   - 重启Redis"
    echo "  status    - 查看状态"
    echo "  help      - 显示帮助"
    echo ""
}

# 检查kubectl是否可用
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        echo "错误: kubectl 未安装，请先安装 kubectl"
        exit 1
    fi
}

# 检查Kubernetes集群状态
check_cluster() {
    echo "检查Kubernetes集群状态..."
    kubectl cluster-info
}

# 部署Redis
deploy_redis() {
    echo "=== 部署Redis到Kubernetes ==="
    
    check_kubectl
    check_cluster
    
    echo "部署Redis Deployment..."
    kubectl apply -f redis-deployment.yaml
    
    echo "部署Redis Service..."
    kubectl apply -f redis-service.yaml
    
    # 等待Pod启动
    echo "等待Redis Pod启动..."
    kubectl wait --for=condition=Ready pod -l app=redis --timeout=120s 2>/dev/null || true
    
    show_status
    
    echo "=== 部署完成 ==="
    echo "Redis已部署到Kubernetes集群"
    echo "内部访问地址: redis-service:6379"
    echo "外部访问地址: localhost:30379 (NodePort)"
    echo "查看日志: kubectl logs -l app=redis"
    echo "进入容器: kubectl exec -it \$(kubectl get pods -l app=redis -o jsonpath='{.items[0].metadata.name}') -- redis-cli"
    echo "测试外部连接:"
    if command -v redis-cli &> /dev/null; then
        echo "redis-cli -h localhost -p 30379 ping"
        redis-cli -h localhost -p 30379 ping 2>/dev/null || echo "连接测试失败，请稍后重试"
    else
        echo "警告: redis-cli 未安装，使用以下方法安装:"
        echo "macOS: brew install redis"
        echo "Linux: sudo apt install redis-tools"
        echo "\n替代测试方法:"
        echo "1. 运行Java程序: mvn compile exec:java -Dexec.mainClass=\"org.example.RedisExamples\""
        echo "2. 使用telnet: telnet localhost 30379 (输入ping后Ctrl+]然后q退出)"
        echo "3. 使用nc: echo \"ping\" | nc localhost 30379"
    fi
}

# 停止Redis
stop_redis() {
    echo "=== 停止Redis实例 ==="
    
    check_kubectl
    
    echo "删除Redis Service..."
    kubectl delete -f redis-service.yaml 2>/dev/null || true
    
    echo "删除Redis Deployment..."
    kubectl delete -f redis-deployment.yaml 2>/dev/null || true
    
    echo "等待资源清理..."
    sleep 3
    
    echo "=== 停止完成 ==="
    echo "Redis实例已停止"
    echo "当前状态:"
    show_status
}

# 清理所有Redis相关资源
cleanup_redis() {
    echo "=== 清理所有Redis相关资源 ==="
    
    check_kubectl
    
    echo "清理所有标签为app=redis的资源..."
    
    # 删除所有相关资源
    kubectl delete deployment -l app=redis 2>/dev/null || true
    kubectl delete service -l app=redis 2>/dev/null || true
    kubectl delete pod -l app=redis 2>/dev/null || true
    kubectl delete configmap -l app=redis 2>/dev/null || true
    kubectl delete secret -l app=redis 2>/dev/null || true
    kubectl delete pvc -l app=redis 2>/dev/null || true
    
    echo "等待资源清理..."
    sleep 5
    
    echo "=== 清理完成 ==="
    echo "所有Redis相关资源已清理"
    echo "\n剩余资源状态:"
    echo "Deployments:"
    kubectl get deployments 2>/dev/null || echo "无"
    echo "\nServices:"
    kubectl get services 2>/dev/null || echo "无"
    echo "\nPods:"
    kubectl get pods 2>/dev/null || echo "无"
}

# 验证配置文件
validate_configs() {
    echo "=== 验证Redis配置文件 ==="
    
    echo "验证部署文件: redis-deployment.yaml"
    kubectl apply --dry-run=client -f redis-deployment.yaml
    
    echo "\n验证服务文件: redis-service.yaml"
    kubectl apply --dry-run=client -f redis-service.yaml
    
    echo "\n配置文件语法检查:"
    echo "部署文件内容摘要:"
    echo "---"
    grep -E "^(apiVersion|kind|metadata:|  name:|spec:|  containers:|  - name:|    image:|    ports:|  selector:|  type:)" redis-deployment.yaml | head -15
    
    echo "\n服务文件内容摘要:"
    echo "---"
    grep -E "^(apiVersion|kind|metadata:|  name:|spec:|  selector:|  ports:|    nodePort:|  type:)" redis-service.yaml | head -15
    
    echo "\n=== 验证完成 ==="
    echo "如果看到'successfully validated'，则表示配置文件语法正确"
}

# 查看日志
show_logs() {
    echo "=== 查看Redis日志 ==="
    
    check_kubectl
    
    local pod_name=$(kubectl get pods -l app=redis -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    
    if [ -z "$pod_name" ]; then
        echo "错误: 未找到运行的Redis Pod"
        echo "请先运行: ./deploy-redis.sh deploy"
        exit 1
    fi
    
    echo "Pod名称: $pod_name"
    echo "\n日志选项:"
    echo "1. 查看最新日志"
    echo "2. 实时跟踪日志"
    echo "3. 查看最近100行日志"
    echo "4. 查看指定时间范围内的日志"
    echo ""
    
    read -p "请选择选项 (1-4, 默认1): " choice
    choice=${choice:-1}
    
    case $choice in
        1)
            echo "\n查看最新日志:"
            kubectl logs -l app=redis
            ;;
        2)
            echo "\n实时跟踪日志 (Ctrl+C退出):"
            kubectl logs -l app=redis -f
            ;;
        3)
            echo "\n查看最近100行日志:"
            kubectl logs -l app=redis --tail=100
            ;;
        4)
            echo "\n查看最近5分钟内的日志:"
            kubectl logs -l app=redis --since=5m
            ;;
        *)
            echo "\n查看最新日志:"
            kubectl logs -l app=redis
            ;;
    esac
    
    echo "\n=== 日志查看完成 ==="
    echo "提示: 使用实时跟踪(选项2)可以持续监控日志输出"
}

# 端口转发
port_forward() {
    echo "=== Redis端口转发 ==="
    
    check_kubectl
    
    local pod_name=$(kubectl get pods -l app=redis -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
    
    if [ -z "$pod_name" ]; then
        echo "错误: 未找到运行的Redis Pod"
        echo "请先运行: ./deploy-redis.sh deploy"
        exit 1
    fi
    
    echo "Pod名称: $pod_name"
    echo "\n端口转发选项:"
    echo "1. 转发到本地端口 6379 (默认)"
    echo "2. 转发到自定义端口"
    echo "3. 停止所有端口转发"
    echo ""
    
    read -p "请选择选项 (1-3, 默认1): " choice
    choice=${choice:-1}
    
    case $choice in
        1)
            echo "\n转发Redis端口 6379 -> 本地 6379 (Ctrl+C停止):"
            echo "使用: redis-cli -h localhost -p 6379"
            echo "\n开始转发..."
            kubectl port-forward pods/$pod_name 6379:6379
            ;;
        2)
            read -p "请输入本地端口号 (默认: 6379): " local_port
            local_port=${local_port:-6379}
            echo "\n转发Redis端口 6379 -> 本地 $local_port (Ctrl+C停止):"
            echo "使用: redis-cli -h localhost -p $local_port"
            echo "\n开始转发..."
            kubectl port-forward pods/$pod_name $local_port:6379
            ;;
        3)
            echo "\n停止所有端口转发进程..."
            pkill -f "kubectl port-forward" 2>/dev/null || true
            echo "所有端口转发已停止"
            ;;
        *)
            echo "\n转发Redis端口 6379 -> 本地 6379 (Ctrl+C停止):"
            echo "使用: redis-cli -h localhost -p 6379"
            echo "\n开始转发..."
            kubectl port-forward pods/$pod_name 6379:6379
            ;;
    esac
    
    echo "\n=== 端口转发完成 ==="
    echo "提示: 端口转发后可以使用标准端口6379连接，无需使用NodePort 30379"
}

# 重启Redis
restart_redis() {
    echo "=== 重启Redis实例 ==="
    
    check_kubectl
    
    echo "先停止Redis..."
    stop_redis
    
    echo "\n重新部署Redis..."
    deploy_redis
    
    echo "=== 重启完成 ==="
}

# 查看状态
show_status() {
    echo "=== Redis状态 ==="
    
    check_kubectl
    
    echo "Pods状态:"
    kubectl get pods -l app=redis 2>/dev/null || echo "没有运行的Redis Pod"
    
    echo "\nService状态:"
    kubectl get svc redis-service 2>/dev/null || echo "没有Redis Service"
    
    echo "\nPod详情:"
    kubectl describe pods -l app=redis 2>/dev/null | head -30 || echo "无法获取Pod详情"
}

# 主逻辑
main() {
    COMMAND="${1:-deploy}"
    
    case "$COMMAND" in
        deploy|d)
            deploy_redis
            ;;
        stop|s)
            stop_redis
            ;;
        cleanup|c)
            cleanup_redis
            ;;
        validate|v)
            validate_configs
            ;;
        logs|l)
            show_logs
            ;;
        port-forward|pf)
            port_forward
            ;;
        restart|r)
            restart_redis
            ;;
        status|st)
            show_status
            ;;
        help|h|--help|-h)
            show_usage
            ;;
        *)
            echo "错误: 未知命令 '$COMMAND'"
            show_usage
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"