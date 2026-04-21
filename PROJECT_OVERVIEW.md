# Redis Demo 项目概览

## 📋 项目简介

这是一个完整的Redis学习和演示项目，涵盖了Redis九种数据结构、性能测试、交互式命令行工具、配置管理以及Kubernetes部署等功能。项目旨在提供一个全面的Redis学习和开发环境。

## 🚀 核心特性

### 1. **Redis数据结构完整示例**
- String、List、Set、Hash、ZSet、Bitmaps、HyperLogLogs、Geospatial、Streams
- 每种数据结构都有完整的Java代码示例和详细注释
- 实际应用场景说明

### 2. **交互式Redis CLI工具**
- 类似原生redis-cli的交互式体验
- 支持命令自动补全和提示
- 连接配置可自定义
- 实时命令执行和结果展示

### 3. **性能测试套件**
- 全面性能测试：String、Hash、List、Set、ZSet操作
- 并发性能测试（1-16线程）
- 连接池性能分析
- 详细的性能统计报告

### 4. **智能配置管理**
- 多配置源支持：配置文件 > 环境变量 > 系统属性 > 默认值
- 配置文件自动发现
- 配置验证和错误提示
- 环境特定的配置模板

### 5. **Kubernetes部署工具**
- 一键部署脚本（支持部署、停止、重启、清理）
- 实时日志查看
- 端口转发调试
- 配置文件验证
- NodePort支持（外部访问端口30379）

## 📁 项目结构

```
redisdemo01/
├── src/main/java/org/example/
│   ├── Main.java                    # 原始主类
│   ├── RedisExamples.java          # Redis九种数据结构示例（核心）
│   ├── RedisCLI.java               # 交互式命令行工具（新增）
│   ├── RedisBenchmark.java         # 性能测试工具（新增）
│   └── RedisConfig.java            # 配置管理类（新增）
│
├── k8s/                            # Kubernetes部署配置
│   ├── deploy-redis.sh             # 智能部署脚本
│   ├── redis-deployment.yaml       # Deployment配置
│   ├── redis-service.yaml          # Service配置（NodePort:30379）
│   └── README.md                   # 部署说明
│
├── config/                         # 配置文件目录
│   └── redis.properties.example    # 配置模板
│
├── REDIS_DATA_STRUCTURES.md        # Redis数据结构详细文档（389行）
├── PROJECT_OVERVIEW.md             # 项目概览（本文件）
├── README.md                       # 项目主说明文档
├── quick-start.sh                  # 快速启动向导脚本
├── install-redis-cli.sh            # Redis CLI安装脚本
├── start-redis.sh                  # Redis启动脚本
└── pom.xml                         # Maven配置（已优化）
```

## 🎯 快速开始

### 方法一：使用快速启动向导（推荐）
```bash
# 1. 给脚本执行权限
chmod +x quick-start.sh

# 2. 启动向导
./quick-start.sh

# 3. 按菜单选择功能：
#    - 检查环境依赖
#    - 部署到Kubernetes
#    - 运行所有示例
#    - 测试所有功能
```

### 方法二：分步操作

#### 步骤1：检查环境
```bash
# 检查Java和Maven
java -version
mvn -version

# 检查Kubernetes（可选）
kubectl version
```

#### 步骤2：部署Redis
```bash
# 方式A：使用Kubernetes（推荐）
cd k8s
./deploy-redis.sh deploy

# 方式B：本地Docker
chmod +x start-redis.sh
./start-redis.sh

# 方式C：手动启动Redis服务
redis-server &
```

#### 步骤3：运行示例程序
```bash
# 基本数据结构示例
mvn compile exec:java -Dexec.mainClass="org.example.RedisExamples"

# 交互式CLI
mvn compile exec:java -Dexec.mainClass="org.example.RedisCLI"

# 性能测试
mvn compile exec:java -Dexec.mainClass="org.example.RedisBenchmark"

# 配置测试
mvn compile exec:java -Dexec.mainClass="org.example.RedisConfig"
```

#### 步骤4：测试连接
```bash
# 使用redis-cli（如果已安装）
redis-cli -h localhost -p 30379 ping

# 或使用替代方法
telnet localhost 30379  # 输入ping后Ctrl+]然后q退出
echo "ping" | nc localhost 30379
```

## 🔧 配置系统

### 配置优先级（从高到低）：
1. **命令行参数**：`--host localhost --port 30379`
2. **配置文件**：`redis.properties`
3. **环境变量**：`REDIS_HOST`、`REDIS_PORT`
4. **系统属性**：`-Dredis.host=localhost`
5. **默认值**：`localhost:30379`

### 配置文件位置（自动发现）：
- 项目根目录 `/redis.properties`
- `config/redis.properties`
- `src/main/resources/redis.properties`
- `~/.redis-demo/redis.properties`

### 创建配置文件：
```bash
# 从模板创建
cp config/redis.properties.example redis.properties

# 编辑配置
nano redis.properties
```

## 🛠️ 部署管理

### Kubernetes部署命令：
```bash
cd k8s

# 显示帮助
./deploy-redis.sh help

# 部署
./deploy-redis.sh deploy

# 查看状态
./deploy-redis.sh status

# 查看实时日志
./deploy-redis.sh logs

# 端口转发调试
./deploy-redis.sh port-forward

# 重启服务
./deploy-redis.sh restart

# 清理环境
./deploy-redis.sh cleanup
```

## 📊 学习路径

### 初学者路径：
1. 阅读 [REDIS_DATA_STRUCTURES.md](./REDIS_DATA_STRUCTURES.md) 了解数据结构
2. 运行 `quick-start.sh` 进行环境设置
3. 运行 `RedisExamples.java` 查看基础示例
4. 使用 `RedisCLI.java` 进行交互式学习

### 进阶路径：
1. 运行 `RedisBenchmark.java` 了解性能特性
2. 学习 `RedisConfig.java` 的配置系统
3. 研究Kubernetes部署脚本
4. 查看源码实现细节

### 生产部署：
1. 配置 `redis.properties` 文件
2. 使用Kubernetes部署脚本
3. 设置合适的连接池参数
4. 配置监控和日志

## 🔗 相关文档

- [Redis官方文档](https://redis.io/docs/)
- [Jedis GitHub仓库](https://github.com/redis/jedis)
- [Kubernetes官方文档](https://kubernetes.io/docs/)
- [Maven官方文档](https://maven.apache.org/guides/)

## 🐛 故障排除

### 常见问题：

**Q: 连接被拒绝？**
A: 确保Redis服务正在运行：
```bash
# 检查Kubernetes部署
kubectl get pods -l app=redis

# 检查本地Redis
redis-cli ping
```

**Q: 端口30379无法访问？**
A: 可能是NodePort配置问题，尝试端口转发：
```bash
cd k8s
./deploy-redis.sh port-forward
```

**Q: Maven依赖下载失败？**
A: 清理缓存：
```bash
mvn clean
```

**Q: Java版本不兼容？**
A: 确保使用Java 22或更高版本：
```bash
java -version
```

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源。

---

**最后更新：** 2026-04-21
**版本：** 1.0.0
**状态：** 生产就绪