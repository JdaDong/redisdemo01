# Redis 数据结构示例项目

## 项目概述

本项目提供了Redis九种数据结构的完整示例代码和详细文档，帮助开发者深入理解Redis的各种数据结构和应用场景。

## 项目结构

```
redisdemo01/
├── src/main/java/org/example/
│   ├── Main.java          # 原始主类
│   ├── RedisExamples.java # Redis九种数据结构示例
│   ├── RedisCLI.java      # 交互式Redis命令行工具（新增）
│   ├── RedisBenchmark.java # Redis性能测试工具（新增）
│   └── RedisConfig.java   # Redis配置管理类（新增）
├── k8s/                  # Kubernetes部署配置
│   ├── deploy-redis.sh   # 智能部署脚本
│   ├── redis-deployment.yaml
│   ├── redis-service.yaml
│   └── README.md
├── REDIS_DATA_STRUCTURES.md # Redis数据结构详细文档
├── install-redis-cli.sh  # Redis CLI安装脚本
├── start-redis.sh        # Redis启动脚本
├── pom.xml               # Maven配置文件
└── README.md            # 项目说明
```

## 前置要求

1. **Java 22+** - 项目使用Java 22
2. **Maven** - 项目管理工具
3. **Redis服务器** - 需要安装并运行Redis服务器

## 安装和运行

### 1. 安装Redis服务器

#### macOS (使用Homebrew)
```bash
brew install redis
brew services start redis
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
```

#### Windows
下载并安装Redis for Windows：https://github.com/microsoftarchive/redis/releases

#### Kubernetes (NodePort方式)
使用k8s目录下的配置文件部署，提供外部访问端口30379：
```bash
cd k8s
kubectl apply -f redis-deployment.yaml
kubectl apply -f redis-service.yaml

# 外部访问: localhost:30379
# Java代码: new Jedis("localhost", 30379)
```

### 2. 安装Redis命令行工具（redis-cli）
如需使用redis-cli测试连接，请先安装：

#### macOS
```bash
brew install redis
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install redis-tools
```

或者使用安装脚本：
```bash
chmod +x install-redis-cli.sh
./install-redis-cli.sh
```

#### 替代测试方法（如果未安装redis-cli）
```bash
# 使用telnet
$ telnet localhost 30379
> ping
> Ctrl+] 然后 q 退出

# 使用nc (netcat)
$ echo "ping" | nc localhost 30379

# 直接运行Java程序
$ mvn compile exec:java -Dexec.mainClass="org.example.RedisExamples"
```

### 3. 编译和运行项目

```bash
# 编译项目
mvn compile

# 运行Redis示例程序（基本数据结构）
mvn exec:java -Dexec.mainClass="org.example.RedisExamples"

# 运行Redis交互式CLI工具
mvn exec:java -Dexec.mainClass="org.example.RedisCLI"

# 运行Redis性能测试工具
mvn exec:java -Dexec.mainClass="org.example.RedisBenchmark"

# 运行Redis配置测试
mvn exec:java -Dexec.mainClass="org.example.RedisConfig"

# 或者直接运行
java -cp target/classes:target/dependency/* org.example.RedisExamples
```

### 4. 运行单个示例

您也可以直接运行主类：
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Redis数据结构示例

项目包含以下九种Redis数据结构的完整示例：

1. **String（字符串）** - 基本键值存储
2. **List（列表）** - 有序列表操作
3. **Set（集合）** - 无序唯一集合
4. **Hash（哈希）** - 字段值对存储
5. **ZSet（有序集合）** - 带分数排序集合
6. **Bitmaps（位图）** - 位操作
7. **HyperLogLogs** - 基数统计
8. **Geospatial（地理空间）** - 地理位置操作
9. **Streams（流）** - 消息流处理

## 详细文档

查看 [REDIS_DATA_STRUCTURES.md](./REDIS_DATA_STRUCTURES.md) 文件获取每种数据结构的:
- 详细功能介绍
- 使用场景说明
- 完整的Java代码示例
- 最佳实践建议

## 依赖说明

项目使用 Jedis 作为 Redis Java客户端：
```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>5.1.2</version>
</dependency>
```

## 配置说明

默认连接配置：
- 主机：localhost
- 端口：6379
- 密码：无（如果需要密码，请在代码中修改）

如果需要修改Redis连接配置，请编辑 `RedisExamples.java` 文件中的连接参数。

## 🚀 新增功能特性

### 1. 交互式Redis CLI工具 (`RedisCLI.java`)
提供一个类似原生redis-cli的交互式命令行工具：
```bash
mvn exec:java -Dexec.mainClass="org.example.RedisCLI"
```
**特性：**
- 支持SET、GET、HSET、HGET、LPUSH等常用命令
- 命令自动补全提示
- 连接配置可自定义
- 实时命令执行反馈

### 2. Redis性能测试工具 (`RedisBenchmark.java`)
全面的性能测试和分析工具：
```bash
mvn exec:java -Dexec.mainClass="org.example.RedisBenchmark"
```
**测试内容：**
- String、Hash、List、Set操作性能
- 并发性能测试（1-16线程）
- 连接池性能测试
- 吞吐量和延迟统计

### 3. 智能配置管理 (`RedisConfig.java`)
灵活的配置管理系统，支持多来源配置：
```bash
mvn exec:java -Dexec.mainClass="org.example.RedisConfig"
```
**配置来源优先级：**
1. 配置文件 (`redis.properties`)
2. 环境变量 (`REDIS_HOST`, `REDIS_PORT`)
3. 系统属性 (`-Dredis.host=localhost`)
4. 默认值

### 4. Kubernetes智能部署脚本
```bash
cd k8s
./deploy-redis.sh help
```
**完整命令支持：**
- `deploy` - 部署Redis实例
- `stop` - 停止Redis实例
- `cleanup` - 清理所有Redis资源
- `validate` - 验证配置文件
- `logs` - 查看实时日志
- `port-forward` - 端口转发调试
- `restart` - 重启服务
- `status` - 查看状态

## 🔧 使用场景示例

### 开发和调试
```bash
# 1. 启动Redis (Kubernetes方式)
cd k8s && ./deploy-redis.sh deploy

# 2. 使用交互式CLI测试
mvn exec:java -Dexec.mainClass="org.example.RedisCLI"

# 3. 实时查看日志
cd k8s && ./deploy-redis.sh logs

# 4. 端口转发调试
cd k8s && ./deploy-redis.sh port-forward
# 在另一个终端使用：redis-cli -h localhost -p 6379

# 5. 运行性能测试
mvn exec:java -Dexec.mainClass="org.example.RedisBenchmark"
```

### 生产环境配置
```bash
# 1. 创建配置文件
cat > redis.properties << EOF
redis.host=production-redis.example.com
redis.port=6379
redis.password=your_secret_password
redis.pool.maxTotal=20
redis.pool.maxIdle=10
EOF

# 2. 使用环境变量（推荐）
export REDIS_HOST=production-redis.example.com
export REDIS_PASSWORD=your_secret_password

# 3. 或使用系统属性
java -Dredis.host=production-redis.example.com -jar your-app.jar
```

## 常见问题

### 1. 连接拒绝错误
确保Redis服务器正在运行：
```bash
redis-cli ping
# 应该返回 PONG
```

### 2. 依赖下载失败
尝试清理Maven缓存：
```bash
mvn clean
```

### 3. 端口冲突
如果6379端口被占用，可以修改Redis配置或代码中的端口号。

### 4. 配置文件加载
如果使用配置文件，请确保 `redis.properties` 文件在以下位置之一：
- 项目根目录
- `config/` 目录
- `src/main/resources/` 目录
- `~/.redis-demo/` 目录

## 学习资源

- [Redis官方文档](https://redis.io/docs/)
- [Jedis GitHub仓库](https://github.com/redis/jedis)
- [Redis命令参考](https://redis.io/commands/)

## 贡献

欢迎提交Issue和Pull Request来改进这个示例项目。

## 许可证

MIT License# redisdemo01
