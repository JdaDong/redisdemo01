#!/bin/bash

# Redis Demo 快速启动脚本
# 一键启动和体验Redis演示项目的所有功能

RED=\"\033[31m\"
GREEN=\"\033[32m\"
YELLOW=\"\033[33m\"
BLUE=\"\033[34m\"
MAGENTA=\"\033[35m\"
CYAN=\"\033[36m\"
RESET=\"\033[0m\"

print_header() {
    echo ""
    echo "${CYAN}╔══════════════════════════════════════════════════════════════════╗${RESET}"
    echo "${CYAN}║                 Redis Demo 快速启动向导                         ║${RESET}"
    echo "${CYAN}╚══════════════════════════════════════════════════════════════════╝${RESET}"
    echo ""
}

print_menu() {
    echo "${GREEN}请选择要执行的操作：${RESET}"
    echo "  ${BLUE}1.${RESET} 检查环境依赖"
    echo "  ${BLUE}2.${RESET} 安装Redis CLI工具"
    echo "  ${BLUE}3.${RESET} 启动本地Redis服务器"
    echo "  ${BLUE}4.${RESET} 部署到Kubernetes（使用NodePort 30379）"
    echo "  ${BLUE}5.${RESET} 运行Redis数据结构示例"
    echo "  ${BLUE}6.${RESET} 启动交互式Redis CLI"
    echo "  ${BLUE}7.${RESET} 运行性能测试"
    echo "  ${BLUE}8.${RESET} 测试配置管理"
    echo "  ${BLUE}9.${RESET} 查看项目状态"
    echo "  ${BLUE}10.${RESET} 运行所有功能测试"
    echo "  ${BLUE}0.${RESET} 退出"
    echo ""
}

check_dependencies() {
    echo "${YELLOW}正在检查环境依赖...${RESET}"
    
    # 检查Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'\"' -f2)
        echo "${GREEN}✓ Java 已安装: $JAVA_VERSION${RESET}"
    else
        echo "${RED}✗ Java 未安装${RESET}"
        echo "  请先安装Java 22+：https://adoptium.net/"
    fi
    
    # 检查Maven
    if command -v mvn &> /dev/null; then
        MAVEN_VERSION=$(mvn -v 2>&1 | grep \"Apache Maven\" | cut -d' ' -f3)
        echo "${GREEN}✓ Maven 已安装: $MAVEN_VERSION${RESET}"
    else
        echo "${RED}✗ Maven 未安装${RESET}"
        echo "  请先安装Maven：https://maven.apache.org/install.html"
    fi
    
    # 检查Redis CLI
    if command -v redis-cli &> /dev/null; then
        echo "${GREEN}✓ redis-cli 已安装${RESET}"
    else
        echo "${YELLOW}⚠ redis-cli 未安装，建议安装用于测试${RESET}"
    fi
    
    # 检查Kubernetes
    if command -v kubectl &> /dev/null; then
        KUBE_VERSION=$(kubectl version --client 2>&1 | grep \"GitVersion\" | cut -d'\"' -f4)
        echo "${GREEN}✓ kubectl 已安装: $KUBE_VERSION${RESET}"
    else
        echo "${YELLOW}⚠ kubectl 未安装，将无法使用Kubernetes部署${RESET}"
    fi
    
    # 检查Docker（可选）
    if command -v docker &> /dev/null; then
        echo "${GREEN}✓ Docker 已安装${RESET}"
    else
        echo "${YELLOW}⚠ Docker 未安装，可使用本地Redis或Kubernetes替代${RESET}"
    fi
}

install_redis_cli() {
    echo "${YELLOW}安装Redis CLI工具...${RESET}"
    
    if [ -f \"install-redis-cli.sh\" ]; then
        chmod +x install-redis-cli.sh
        ./install-redis-cli.sh
    else
        echo "${RED}安装脚本不存在: install-redis-cli.sh${RESET}"
        echo "请手动安装redis-cli："
        echo "  macOS: brew install redis"
        echo "  Linux: sudo apt install redis-tools"
    fi
}

start_local_redis() {
    echo "${YELLOW}启动本地Redis服务器...${RESET}"
    
    if [ -f \"start-redis.sh\" ]; then
        chmod +x start-redis.sh
        echo "执行启动脚本..."
        ./start-redis.sh
    else
        echo "${YELLOW}使用Docker启动Redis...${RESET}"
        if command -v docker &> /dev/null; then
            docker run -d -p 6379:6379 --name redis-demo redis:7.2-alpine
            echo "${GREEN}Redis容器已启动${RESET}"
        else
            echo "${RED}无法启动Redis，请先安装Docker或手动启动Redis服务${RESET}"
        fi
    fi
}

deploy_to_kubernetes() {
    echo "${YELLOW}部署到Kubernetes集群...${RESET}"
    
    if [ -d \"k8s\" ]; then
        cd k8s
        if [ -f \"deploy-redis.sh\" ]; then
            chmod +x deploy-redis.sh
            echo "使用智能部署脚本..."
            ./deploy-redis.sh deploy
        else
            echo "使用kubectl部署..."
            kubectl apply -f redis-deployment.yaml
            kubectl apply -f redis-service.yaml
        fi
        cd ..
    else
        echo "${RED}k8s目录不存在${RESET}"
    fi
}

run_examples() {
    echo "${YELLOW}运行Redis数据结构示例...${RESET}"
    
    if command -v mvn &> /dev/null; then
        mvn compile exec:java -Dexec.mainClass=\"org.example.RedisExamples\"
    else
        echo "${RED}Maven未安装，无法编译运行${RESET}"
    fi
}

run_interactive_cli() {
    echo "${YELLOW}启动交互式Redis CLI...${RESET}"
    
    if command -v mvn &> /dev/null; then
        mvn compile exec:java -Dexec.mainClass=\"org.example.RedisCLI\"
    else
        echo "${RED}Maven未安装，无法编译运行${RESET}"
    fi
}

run_performance_test() {
    echo "${YELLOW}运行Redis性能测试...${RESET}"
    
    if command -v mvn &> /dev/null; then
        mvn compile exec:java -Dexec.mainClass=\"org.example.RedisBenchmark\"
    else
        echo "${RED}Maven未安装，无法编译运行${RESET}"
    fi
}

run_config_test() {
    echo "${YELLOW}测试配置管理...${RESET}"
    
    if command -v mvn &> /dev/null; then
        mvn compile exec:java -Dexec.mainClass=\"org.example.RedisConfig\"
    else
        echo "${RED}Maven未安装，无法编译运行${RESET}"
    fi
}

show_status() {
    echo "${YELLOW}项目状态概览：${RESET}"
    
    echo "${BLUE}项目结构：${RESET}"
    find . -name \"*.java\" | sed 's|^./||' | sort
    
    echo "\n${BLUE}可用脚本：${RESET}"
    find . -name \"*.sh\" | sed 's|^./||' | sort
    
    echo "\n${BLUE}配置文件：${RESET}"
    find . -name \"*.yaml\" -o -name \"*.properties\" | sed 's|^./||' | sort
    
    echo "\n${BLUE}文档文件：${RESET}"
    find . -name \"*.md\" | sed 's|^./||' | sort
}

run_all_tests() {
    echo "${YELLOW}运行完整功能测试套件...${RESET}"
    
    echo "\n${MAGENTA}=== 步骤1: 检查环境依赖 ===${RESET}"
    check_dependencies
    
    echo "\n${MAGENTA}=== 步骤2: 部署Redis（使用Kubernetes） ===${RESET}"
    deploy_to_kubernetes
    
    echo "\n${MAGENTA}=== 步骤3: 运行基础示例 ===${RESET}"
    run_examples
    
    echo "\n${MAGENTA}=== 步骤4: 测试交互式CLI ===${RESET}"
    run_interactive_cli
    
    echo "\n${MAGENTA}=== 步骤5: 运行性能测试 ===${RESET}"
    run_performance_test
    
    echo "\n${MAGENTA}=== 步骤6: 测试配置管理 ===${RESET}"
    run_config_test
    
    echo "\n${GREEN}✓ 所有测试完成！${RESET}"
}

main() {
    print_header
    
    while true; do
        print_menu
        
        read -p "${GREEN}请输入选项 (0-10): ${RESET}" choice
        echo ""
        
        case $choice in
            1)
                check_dependencies
                ;;
            2)
                install_redis_cli
                ;;
            3)
                start_local_redis
                ;;
            4)
                deploy_to_kubernetes
                ;;
            5)
                run_examples
                ;;
            6)
                run_interactive_cli
                ;;
            7)
                run_performance_test
                ;;
            8)
                run_config_test
                ;;
            9)
                show_status
                ;;
            10)
                run_all_tests
                ;;
            0|exit|quit)
                echo "${GREEN}感谢使用Redis Demo，再见！${RESET}"
                break
                ;;
            *)
                echo "${RED}无效选项，请重新选择${RESET}"
                ;;
        esac
        
        echo ""
        read -p "${YELLOW}按Enter键继续...${RESET}" wait
    done
}

# 主程序
main