# Redis 九种数据结构详解

## 概述
Redis支持9种主要的数据结构，每种都有其特定的使用场景和优势。

## 1. String（字符串）

### 介绍
最基本的Redis数据类型，可以存储字符串、整数或浮点数。

### 使用场景
- 缓存用户会话信息
- 计数器（文章阅读量、点赞数）
- 分布式锁
- 简单的键值存储

### Java代码示例
```java
import redis.clients.jedis.Jedis;

public class StringExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 设置字符串
        jedis.set("user:1001:name", "张三");
        jedis.set("article:2001:views", "0");
        
        // 获取字符串
        String userName = jedis.get("user:1001:name");
        System.out.println("用户名: " + userName);
        
        // 自增操作
        jedis.incr("article:2001:views");
        String views = jedis.get("article:2001:views");
        System.out.println("阅读量: " + views);
        
        jedis.close();
    }
}
```

## 2. List（列表）

### 介绍
字符串列表，按插入顺序排序，支持双向操作。

### 使用场景
- 消息队列
- 最新文章列表
- 用户操作历史记录
- 任务队列

### Java代码示例
```java
import redis.clients.jedis.Jedis;
import java.util.List;

public class ListExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 从左侧添加元素
        jedis.lpush("news:latest", "文章1", "文章2", "文章3");
        
        // 从右侧添加元素
        jedis.rpush("news:latest", "文章4");
        
        // 获取列表范围
        List<String> news = jedis.lrange("news:latest", 0, 2);
        System.out.println("最新文章: " + news);
        
        // 弹出元素
        String latestArticle = jedis.lpop("news:latest");
        System.out.println("最新文章: " + latestArticle);
        
        jedis.close();
    }
}
```

## 3. Set（集合）

### 介绍
无序的字符串集合，元素唯一，不允许重复。

### 使用场景
- 标签系统
- 好友关系
- 唯一值存储
- 共同好友/共同关注

### Java代码示例
```java
import redis.clients.jedis.Jedis;
import java.util.Set;

public class SetExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 添加元素
        jedis.sadd("user:1001:tags", "java", "redis", "spring");
        jedis.sadd("user:1002:tags", "python", "redis", "django");
        
        // 获取所有元素
        Set<String> tags = jedis.smembers("user:1001:tags");
        System.out.println("用户标签: " + tags);
        
        // 求交集（共同标签）
        Set<String> commonTags = jedis.sinter("user:1001:tags", "user:1002:tags");
        System.out.println("共同标签: " + commonTags);
        
        jedis.close();
    }
}
```

## 4. Hash（哈希）

### 介绍
字段-值对的集合，适合存储对象。

### 使用场景
- 用户信息存储
- 商品信息
- 配置信息
- 对象属性存储

### Java代码示例
```java
import redis.clients.jedis.Jedis;
import java.util.Map;

public class HashExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 设置哈希字段
        jedis.hset("user:1001", "name", "李四");
        jedis.hset("user:1001", "age", "25");
        jedis.hset("user:1001", "email", "lisi@example.com");
        
        // 获取单个字段
        String name = jedis.hget("user:1001", "name");
        System.out.println("用户名: " + name);
        
        // 获取所有字段
        Map<String, String> userInfo = jedis.hgetAll("user:1001");
        System.out.println("用户信息: " + userInfo);
        
        // 增加数字字段
        jedis.hincrBy("user:1001", "age", 1);
        String newAge = jedis.hget("user:1001", "age");
        System.out.println("新年龄: " + newAge);
        
        jedis.close();
    }
}
```

## 5. ZSet（有序集合）

### 介绍
带分数的集合，元素按分数排序。

### 使用场景
- 排行榜
- 优先级队列
- 时间线
- 带权重的数据排序

### Java代码示例
```java
import redis.clients.jedis.Jedis;
import java.util.Set;

public class ZSetExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 添加带分数的元素
        jedis.zadd("leaderboard", 100, "player1");
        jedis.zadd("leaderboard", 85, "player2");
        jedis.zadd("leaderboard", 95, "player3");
        
        // 获取排名前3的玩家
        Set<String> topPlayers = jedis.zrevrange("leaderboard", 0, 2);
        System.out.println("排行榜前三: " + topPlayers);
        
        // 获取玩家排名
        Long rank = jedis.zrevrank("leaderboard", "player1");
        System.out.println("player1排名: " + (rank != null ? rank + 1 : "未上榜"));
        
        // 增加分数
        jedis.zincrby("leaderboard", 10, "player2");
        
        jedis.close();
    }
}
```

## 6. Bitmaps（位图）

### 介绍
通过位操作实现的字符串，用于位级操作。

### 使用场景
- 用户签到系统
- 活跃用户统计
- 布隆过滤器
- 特征标记

### Java代码示例
```java
import redis.clients.jedis.Jedis;

public class BitmapExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 用户签到（第1天、第3天、第5天）
        jedis.setbit("user:1001:sign:2024", 0, true);  // 第1天
        jedis.setbit("user:1001:sign:2024", 2, true);  // 第3天
        jedis.setbit("user:1001:sign:2024", 4, true);  // 第5天
        
        // 检查第3天是否签到
        boolean signed = jedis.getbit("user:1001:sign:2024", 2);
        System.out.println("第3天是否签到: " + signed);
        
        // 统计签到天数
        Long signCount = jedis.bitcount("user:1001:sign:2024");
        System.out.println("总签到天数: " + signCount);
        
        jedis.close();
    }
}
```

## 7. HyperLogLogs

### 介绍
用于基数统计的概率性数据结构，占用固定内存。

### 使用场景
- UV（独立访客）统计
- 网站访问量统计
- 大规模数据去重计数
- 近似唯一计数

### Java代码示例
```java
import redis.clients.jedis.Jedis;

public class HyperLogLogExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 添加访问用户
        jedis.pfadd("uv:2024-01-01", "user1", "user2", "user3", "user1");
        jedis.pfadd("uv:2024-01-01", "user4", "user5");
        
        // 估算独立用户数
        Long uvCount = jedis.pfcount("uv:2024-01-01");
        System.out.println("独立访客数: " + uvCount);
        
        // 合并多天的UV统计
        jedis.pfadd("uv:2024-01-02", "user2", "user3", "user6");
        jedis.pfmerge("uv:2024-01-01-02", "uv:2024-01-01", "uv:2024-01-02");
        
        Long totalUV = jedis.pfcount("uv:2024-01-01-02");
        System.out.println("两天总UV: " + totalUV);
        
        jedis.close();
    }
}
```

## 8. Geospatial（地理空间）

### 介绍
存储地理位置信息，支持距离计算和范围查询。

### 使用场景
- 附近的人/地点
- 地理位置搜索
- 距离计算
- 地理围栏

### Java代码示例
```java
import redis.clients.jedis.Jedis;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.params.GeoRadiusParam;
import java.util.List;

public class GeospatialExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 添加地理位置
        jedis.geoadd("cities", 116.3974, 39.9093, "北京");  // 经度, 纬度, 名称
        jedis.geoadd("cities", 121.4737, 31.2304, "上海");
        jedis.geoadd("cities", 113.2644, 23.1291, "广州");
        
        // 计算两地距离
        Double distance = jedis.geodist("cities", "北京", "上海", GeoUnit.KM);
        System.out.println("北京到上海距离: " + distance + "公里");
        
        // 查找附近的城市（以北京为中心，500公里半径）
        List<GeoRadiusResponse> nearbyCities = jedis.georadius("cities", 116.3974, 39.9093, 500, GeoUnit.KM, 
            GeoRadiusParam.geoRadiusParam().withCoord().withDist());
        
        for (GeoRadiusResponse city : nearbyCities) {
            System.out.println(city.getMemberByString() + " - 距离: " + city.getDistance() + "公里");
        }
        
        jedis.close();
    }
}
```

## 9. Streams（流）

### 介绍
Redis 5.0引入，用于消息队列和事件流处理。

### 使用场景
- 消息队列
- 事件溯源
- 实时数据处理
- 日志收集

### Java代码示例
```java
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntry;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.params.XReadGroupParams;
import java.util.List;
import java.util.Map;

public class StreamExample {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        
        // 添加消息到流
        Map<String, String> message1 = Map.of("type", "order", "amount", "100");
        StreamEntryID id1 = jedis.xadd("orders", XAddParams.xAddParams(), message1);
        
        Map<String, String> message2 = Map.of("type", "payment", "amount", "150");
        StreamEntryID id2 = jedis.xadd("orders", XAddParams.xAddParams(), message2);
        
        // 读取消息
        List<Map.Entry<String, List<StreamEntry>>> messages = jedis.xread(
            XReadParams.xReadParams().count(10), 
            Map.of("orders", StreamEntryID.LAST_ENTRY)
        );
        
        for (Map.Entry<String, List<StreamEntry>> stream : messages) {
            for (StreamEntry entry : stream.getValue()) {
                System.out.println("消息ID: " + entry.getID() + ", 内容: " + entry.getFields());
            }
        }
        
        jedis.close();
    }
}
```

## 总结

| 数据结构 | 主要特点 | 典型应用场景 |
|---------|---------|-------------|
| String | 简单键值存储 | 缓存、计数器、分布式锁 |
| List | 有序列表，双向操作 | 消息队列、最新列表 |
| Set | 无序集合，元素唯一 | 标签系统、好友关系 |
| Hash | 字段-值对 | 对象存储、用户信息 |
| ZSet | 带分数排序集合 | 排行榜、优先级队列 |
| Bitmaps | 位操作 | 签到系统、活跃统计 |
| HyperLogLog | 基数统计 | UV统计、去重计数 |
| Geospatial | 地理位置 | 附近搜索、距离计算 |
| Streams | 消息流 | 消息队列、事件处理 |

每种数据结构都有其独特的优势和适用场景，根据具体需求选择合适的数据结构可以大大提高系统性能和开发效率。