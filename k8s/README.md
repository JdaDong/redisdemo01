# Kubernetes Redis 部署指南

本文档介绍如何在本地 Kubernetes 环境中部署 Redis 实例。

## 前置要求

1. **Kubernetes 集群**: 确保您有一个运行的 Kubernetes 集群
   - Minikube
   - Docker Desktop (内置 Kubernetes)
   - Kind (Kubernetes in Docker)
   - 或其他 Kubernetes 发行版

2. **kubectl**: 安装并配置好 kubectl 命令行工具

## 部署步骤

### 方法一：使用部署脚本（推荐）

```bash
cd k8s
./deploy-redis.sh
```

### 方法二：手动部署

```bash
# 部署 Redis Deployment
kubectl apply -f redis-deployment.yaml

# 部署 Redis Service
kubectl apply -f redis-service.yaml

# 检查部署状态
kubectl get pods -l app=redis
kubectl get svc redis-service
```

## 配置文件说明

### redis-deployment.yaml
- 使用官方 Redis 7.2 Alpine 镜像
- 配置了资源限制（内存256Mi-512Mi，CPU 100m-250m）
- 包含健康检查（liveness 和 readiness probes）
- 使用 emptyDir 卷存储数据（重启后数据会丢失）

### redis-service.yaml
- 创建 NodePort 类型的服务
- 内部集群访问地址: `redis-service:6379`
- 外部访问地址: `localhost:30379` (如果使用 Minikube/Docker Desktop)

## 访问 Redis

### 从集群外部访问
```bash
# 使用NodePort直接访问
redis-cli -h localhost -p 30379

# 或者在Java代码中使用
Jedis jedis = new Jedis("localhost", 30379);
```

### 从集群内部访问
```bash
# 进入 Redis Pod
kubectl exec -it $(kubectl get pods -l app=redis -o jsonpath='{.items[0].metadata.name}') -- redis-cli

# 或者使用临时客户端
kubectl run redis-client --rm -it --image=redis:7.2-alpine -- redis-cli -h redis-service
```

### 从外部访问（如果需要）

如果需要从集群外部访问，可以创建 NodePort 服务：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: redis-nodeport
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
    nodePort: 30379
  type: NodePort
```

然后使用 `localhost:30379` 访问（如果使用 Minikube/Docker Desktop）。

## 测试连接

在您的 Java 代码中，可以使用以下连接字符串：
```java
Jedis jedis = new Jedis("redis-service", 6379);
```

## 数据持久化

当前配置使用 `emptyDir`，数据在 Pod 重启后会丢失。如果需要数据持久化，可以考虑：

1. 使用 PersistentVolumeClaim (PVC)
2. 使用 StatefulSet 替代 Deployment
3. 配置 Redis 持久化（RDB/AOF）

## 清理部署

```bash
# 删除部署
kubectl delete -f redis-deployment.yaml
kubectl delete -f redis-service.yaml

# 或者使用标签删除
kubectl delete all -l app=redis
```

## 故障排查

```bash
# 查看 Pod 状态
kubectl get pods -l app=redis

# 查看 Pod 详情
kubectl describe pod <pod-name>

# 查看日志
kubectl logs -l app=redis

# 查看服务详情
kubectl describe svc redis-service
```

## 部署脚本命令

现在提供了功能完整的部署脚本 `deploy-redis.sh`，支持以下命令：

```bash
# 显示帮助
./deploy-redis.sh help

# 部署Redis (默认)
./deploy-redis.sh deploy
./deploy-redis.sh      # 简写，默认部署

# 停止Redis
./deploy-redis.sh stop
./deploy-redis.sh s     # 简写

# 清理所有Redis资源
./deploy-redis.sh cleanup
./deploy-redis.sh c     # 简写

# 验证配置文件
./deploy-redis.sh validate
./deploy-redis.sh v     # 简写

# 查看Redis日志
./deploy-redis.sh logs
./deploy-redis.sh l     # 简写

# 端口转发调试
./deploy-redis.sh port-forward
./deploy-redis.sh pf    # 简写

# 重启Redis
./deploy-redis.sh restart
./deploy-redis.sh r     # 简写

# 查看状态
./deploy-redis.sh status
./deploy-redis.sh st    # 简写
```

### 常用工作流程

1. **首次部署**:
   ```bash
   ./deploy-redis.sh deploy
   ```

2. **查看状态**:
   ```bash
   ./deploy-redis.sh status
   ```

3. **查看日志**:
   ```bash
   ./deploy-redis.sh logs
   ```

4. **端口转发调试**:
   ```bash
   ./deploy-redis.sh port-forward
   # 在另一个终端使用: redis-cli -h localhost -p 6379
   ```

5. **重启服务**:
   ```bash
   ./deploy-redis.sh restart
   ```

6. **清理环境**:
   ```bash
   ./deploy-redis.sh cleanup
   ```

## 注意事项

1. 确保 Kubernetes 集群正常运行
2. 确保有足够的资源运行 Redis
3. 生产环境建议使用持久化存储和适当的资源限制
4. 端口转发功能适合调试，生产环境建议使用NodePort或LoadBalancer
5. 清理命令会删除所有标签为app=redis的资源，请谨慎使用