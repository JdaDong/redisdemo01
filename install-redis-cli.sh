#!/bin/bash

echo "=== Redis CLI 安装脚本 ==="
echo "检测系统类型..."

if [[ "$(uname)" == "Darwin" ]]; then
    echo "检测到 macOS 系统"
    
    # 检查是否已安装Homebrew
    if ! command -v brew &> /dev/null; then
        echo "错误: Homebrew 未安装"
        echo "请先安装 Homebrew: https://brew.sh"
        echo "安装命令:"
        echo "/bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
        exit 1
    fi
    
    echo "安装 redis..."
    brew install redis
    
    echo "\n安装完成!"
    echo "redis-cli 版本:"
    redis-cli --version
    
elif [[ "$(uname)" == "Linux" ]]; then
    echo "检测到 Linux 系统"
    
    # 检查系统发行版
    if command -v apt &> /dev/null; then
        echo "检测到 Debian/Ubuntu 系统"
        echo "更新软件包列表..."
        sudo apt update
        
        echo "安装 redis-tools..."
        sudo apt install -y redis-tools
        
    elif command -v yum &> /dev/null; then
        echo "检测到 CentOS/RHEL 系统"
        echo "安装 redis..."
        sudo yum install -y redis
        
    else
        echo "错误: 无法识别的Linux发行版"
        echo "请手动安装 redis-cli:"
        echo "1. Debian/Ubuntu: sudo apt install redis-tools"
        echo "2. CentOS/RHEL: sudo yum install redis"
        echo "3. Arch Linux: sudo pacman -S redis"
        exit 1
    fi
    
    echo "\n安装完成!"
    echo "redis-cli 版本:"
    redis-cli --version
    
else
    echo "错误: 不支持的操作系统 $(uname)"
    echo "请手动安装 redis-cli:"
    echo "Windows: 下载 Redis for Windows - https://github.com/microsoftarchive/redis/releases"
    exit 1
fi

echo "\n使用示例:"
echo "1. 测试连接: redis-cli -h localhost -p 30379 ping"
echo "2. 进入交互模式: redis-cli -h localhost -p 30379"
echo "3. 执行命令: redis-cli -h localhost -p 30379 set test_key 'hello'"
echo "4. 查看键: redis-cli -h localhost -p 30379 keys '*'"