package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XAddParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis九种数据结构完整示例
 * 运行前请确保Redis服务器已启动
 */
public class RedisExamples {
    
    public static void main(String[] args) {
        // 连接到Redis服务器
        Jedis jedis = new Jedis("localhost", 30379);
        
        try {
            // 测试连接
            jedis.ping();
            System.out.println("连接Redis服务器成功");
            
            // 1. String示例
            System.out.println("\n=== 1. String示例 ===");
            stringExample(jedis);
            
            // 2. List示例
            System.out.println("\n=== 2. List示例 ===");
            listExample(jedis);
            
            // 3. Set示例
            System.out.println("\n=== 3. Set示例 ===");
            setExample(jedis);
            
            // 4. Hash示例
            System.out.println("\n=== 4. Hash示例 ===");
            hashExample(jedis);
            
            // 5. ZSet示例
            System.out.println("\n=== 5. ZSet示例 ===");
            zsetExample(jedis);
            
            // 6. Bitmaps示例
            System.out.println("\n=== 6. Bitmaps示例 ===");
            bitmapExample(jedis);
            
            // 7. HyperLogLog示例
            System.out.println("\n=== 7. HyperLogLog示例 ===");
            hyperLogLogExample(jedis);
            
            // 8. Geospatial示例
            System.out.println("\n=== 8. Geospatial示例 ===");
            geospatialExample(jedis);
            
            // 9. Streams示例
            System.out.println("\n=== 9. Streams示例 ===");
            streamExample(jedis);
            
        } catch (Exception e) {
            System.out.println("执行出错: " + e.getMessage());
            
            // 如果是连接错误，提供详细的解决方案
            if (e.getMessage().contains("Failed to connect") || e.getMessage().contains("Connection refused")) {
                System.out.println("\n=== Redis连接失败解决方案 ===");
                System.out.println("1. 使用Docker启动Redis: docker run -d -p 6379:6379 --name redis-demo redis:7.2-alpine");
                System.out.println("2. 使用Homebrew启动Redis: brew services start redis");
                System.out.println("3. 使用Kubernetes部署(NodePort): cd k8s && ./deploy-redis.sh (端口30379)");
                System.out.println("4. 修改连接配置: 编辑代码中的Jedis构造函数参数");
            }
            
            e.printStackTrace();
        } finally {
            // 清理测试数据
            cleanupTestData(jedis);
            jedis.close();
            System.out.println("\nRedis连接已关闭");
        }
    }
    
    private static void stringExample(Jedis jedis) {
        // 设置字符串
        jedis.set("demo:user:name", "张三");
        jedis.set("demo:article:views", "0");
        
        // 获取字符串
        String userName = jedis.get("demo:user:name");
        System.out.println("用户名: " + userName);
        
        // 自增操作
        jedis.incr("demo:article:views");
        String views = jedis.get("demo:article:views");
        System.out.println("阅读量: " + views);
    }
    
    private static void listExample(Jedis jedis) {
        // 从左侧添加元素
        jedis.lpush("demo:news:latest", "文章1", "文章2", "文章3");
        
        // 从右侧添加元素
        jedis.rpush("demo:news:latest", "文章4");
        
        // 获取列表范围
        List<String> news = jedis.lrange("demo:news:latest", 0, 2);
        System.out.println("最新文章: " + news);
        
        // 弹出元素
        String latestArticle = jedis.lpop("demo:news:latest");
        System.out.println("最新文章: " + latestArticle);
    }
    
    private static void setExample(Jedis jedis) {
        // 添加元素
        jedis.sadd("demo:user:1001:tags", "java", "redis", "spring");
        jedis.sadd("demo:user:1002:tags", "python", "redis", "django");
        
        // 获取所有元素
        Set<String> tags = jedis.smembers("demo:user:1001:tags");
        System.out.println("用户标签: " + tags);
        
        // 求交集（共同标签）
        Set<String> commonTags = jedis.sinter("demo:user:1001:tags", "demo:user:1002:tags");
        System.out.println("共同标签: " + commonTags);
    }
    
    private static void hashExample(Jedis jedis) {
        // 设置哈希字段
        jedis.hset("demo:user:1001", "name", "李四");
        jedis.hset("demo:user:1001", "age", "25");
        jedis.hset("demo:user:1001", "email", "lisi@example.com");
        
        // 获取单个字段
        String name = jedis.hget("demo:user:1001", "name");
        System.out.println("用户名: " + name);
        
        // 获取所有字段
        Map<String, String> userInfo = jedis.hgetAll("demo:user:1001");
        System.out.println("用户信息: " + userInfo);
        
        // 增加数字字段
        jedis.hincrBy("demo:user:1001", "age", 1);
        String newAge = jedis.hget("demo:user:1001", "age");
        System.out.println("新年龄: " + newAge);
    }
    
    private static void zsetExample(Jedis jedis) {
        // 添加带分数的元素
        jedis.zadd("demo:leaderboard", 100, "player1");
        jedis.zadd("demo:leaderboard", 85, "player2");
        jedis.zadd("demo:leaderboard", 95, "player3");
        
        // 获取排名前3的玩家
        java.util.List<String> topPlayers = jedis.zrevrange("demo:leaderboard", 0, 2);
        System.out.println("排行榜前三: " + topPlayers);
        
        // 获取玩家排名
        Long rank = jedis.zrevrank("demo:leaderboard", "player1");
        System.out.println("player1排名: " + (rank != null ? rank + 1 : "未上榜"));
        
        // 增加分数
        jedis.zincrby("demo:leaderboard", 10, "player2");
    }
    
    private static void bitmapExample(Jedis jedis) {
        // 用户签到（第1天、第3天、第5天）
        jedis.setbit("demo:user:1001:sign:2024", 0, true);  // 第1天
        jedis.setbit("demo:user:1001:sign:2024", 2, true);  // 第3天
        jedis.setbit("demo:user:1001:sign:2024", 4, true);  // 第5天
        
        // 检查第3天是否签到
        boolean signed = jedis.getbit("demo:user:1001:sign:2024", 2);
        System.out.println("第3天是否签到: " + signed);
        
        // 统计签到天数
        Long signCount = jedis.bitcount("demo:user:1001:sign:2024");
        System.out.println("总签到天数: " + signCount);
    }
    
    private static void hyperLogLogExample(Jedis jedis) {
        // 添加访问用户
        jedis.pfadd("demo:uv:2024-01-01", "user1", "user2", "user3", "user1");
        jedis.pfadd("demo:uv:2024-01-01", "user4", "user5");
        
        // 估算独立用户数
        Long uvCount = jedis.pfcount("demo:uv:2024-01-01");
        System.out.println("独立访客数: " + uvCount);
        
        // 合并多天的UV统计
        jedis.pfadd("demo:uv:2024-01-02", "user2", "user3", "user6");
        jedis.pfmerge("demo:uv:2024-01-01-02", "demo:uv:2024-01-01", "demo:uv:2024-01-02");
        
        Long totalUV = jedis.pfcount("demo:uv:2024-01-01-02");
        System.out.println("两天总UV: " + totalUV);
    }
    
    private static void geospatialExample(Jedis jedis) {
        // 添加地理位置
        jedis.geoadd("demo:cities", 116.3974, 39.9093, "北京");
        jedis.geoadd("demo:cities", 121.4737, 31.2304, "上海");
        jedis.geoadd("demo:cities", 113.2644, 23.1291, "广州");
        
        // 计算两地距离
        Double distance = jedis.geodist("demo:cities", "北京", "上海");
        System.out.println("北京到上海距离: " + (distance != null ? distance + "公里" : "无法计算"));
        
        // 简化地理空间查询 - 只使用基本功能
        // 获取所有城市坐标
        java.util.List<redis.clients.jedis.GeoCoordinate> coordinates = jedis.geopos("demo:cities", "北京", "上海", "广州");
        if (coordinates != null) {
            System.out.println("城市坐标: " + coordinates);
        }
    }
    
    private static void streamExample(Jedis jedis) {
        // 添加消息到流
        Map<String, String> message1 = new java.util.HashMap<>();
        message1.put("type", "order");
        message1.put("amount", "100");
        jedis.xadd("demo:orders", XAddParams.xAddParams(), message1);
        
        Map<String, String> message2 = new java.util.HashMap<>();
        message2.put("type", "payment");
        message2.put("amount", "150");
        jedis.xadd("demo:orders", XAddParams.xAddParams(), message2);
        
        // 使用简化的流读取方式 - 直接读取流内容
        // Jedis 5.x 中流操作API有所变化，这里使用基本功能演示
        System.out.println("流消息已添加，可以使用Redis客户端工具查看流内容");
        System.out.println("示例命令: XREAD COUNT 10 STREAMS demo:orders 0");
    }
    
    private static void cleanupTestData(Jedis jedis) {
        // 删除所有测试数据
        String[] keysToDelete = {
            "demo:user:name", "demo:article:views", "demo:news:latest",
            "demo:user:1001:tags", "demo:user:1002:tags", "demo:user:1001",
            "demo:leaderboard", "demo:user:1001:sign:2024", "demo:uv:2024-01-01",
            "demo:uv:2024-01-02", "demo:uv:2024-01-01-02", "demo:cities", "demo:orders"
        };
        
        for (String key : keysToDelete) {
            jedis.del(key);
        }
        System.out.println("测试数据已清理");
    }
}