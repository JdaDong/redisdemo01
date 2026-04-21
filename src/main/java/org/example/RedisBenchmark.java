package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Redis性能测试工具
 * 测试各种Redis操作的性能表现
 */
public class RedisBenchmark {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 30379;
    private static final int DEFAULT_THREADS = 10;
    private static final int DEFAULT_OPERATIONS = 10000;
    
    public static void main(String[] args) {
        System.out.println("=== Redis性能测试工具 ===");
        System.out.println("测试配置:");
        System.out.println("  主机: " + DEFAULT_HOST);
        System.out.println("  端口: " + DEFAULT_PORT);
        System.out.println("  线程数: " + DEFAULT_THREADS);
        System.out.println("  操作数: " + DEFAULT_OPERATIONS);
        System.out.println("=============================\n");
        
        // 初始化连接池
        JedisPool jedisPool = createJedisPool(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_THREADS + 2);
        
        try {
            // 1. 测试String操作
            testStringOperations(jedisPool, "string-test", DEFAULT_THREADS, DEFAULT_OPERATIONS);
            
            // 2. 测试Hash操作
            testHashOperations(jedisPool, "hash-test", DEFAULT_THREADS, DEFAULT_OPERATIONS / 2);
            
            // 3. 测试List操作
            testListOperations(jedisPool, "list-test", DEFAULT_THREADS, DEFAULT_OPERATIONS / 2);
            
            // 4. 测试Set操作
            testSetOperations(jedisPool, "set-test", DEFAULT_THREADS, DEFAULT_OPERATIONS / 2);
            
            // 5. 测试并发性能
            testConcurrentPerformance(jedisPool, DEFAULT_THREADS, DEFAULT_OPERATIONS);
            
            // 6. 测试连接池性能
            testConnectionPoolPerformance(jedisPool, DEFAULT_OPERATIONS);
            
            System.out.println("\n=== 性能测试完成 ===");
            
        } finally {
            // 清理测试数据
            cleanupTestData(jedisPool);
            jedisPool.close();
        }
    }
    
    private static JedisPool createJedisPool(String host, int port, int maxConnections) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxConnections);
        config.setMaxIdle(maxConnections / 2);
        config.setMinIdle(2);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        
        return new JedisPool(config, host, port);
    }
    
    private static void testStringOperations(JedisPool jedisPool, String keyPrefix, int threads, int operations) {
        System.out.println("\n=== 测试String操作性能 ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    long threadOperations = operations / threads;
                    long start = System.currentTimeMillis();
                    
                    for (int j = 0; j < threadOperations; j++) {
                        String key = keyPrefix + ":" + threadId + ":" + j;
                        String value = "value-" + j + "-" + System.currentTimeMillis();
                        
                        // SET操作
                        jedis.set(key, value);
                        
                        // GET操作
                        jedis.get(key);
                        
                        // INCR操作（每10次操作执行一次）
                        if (j % 10 == 0) {
                            jedis.incr(keyPrefix + ":counter:" + threadId);
                        }
                    }
                    
                    return threadOperations * 2 + (threadOperations / 10); // SET + GET + INCR
                }
            }));
        }
        
        // 等待所有任务完成
        long totalOperations = 0;
        for (Future<Long> future : futures) {
            try {
                totalOperations += future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double opsPerSecond = totalOperations * 1000.0 / duration;
        
        System.out.println("结果:");
        System.out.println("  总操作数: " + totalOperations);
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  平均吞吐量: " + String.format("%.2f", opsPerSecond) + " ops/sec");
        System.out.println("  平均延迟: " + String.format("%.3f", duration * 1.0 / totalOperations) + "ms/op");
    }
    
    private static void testHashOperations(JedisPool jedisPool, String keyPrefix, int threads, int operations) {
        System.out.println("\n=== 测试Hash操作性能 ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    long threadOperations = operations / threads;
                    
                    for (int j = 0; j < threadOperations; j++) {
                        String key = keyPrefix + ":" + threadId;
                        String field = "field-" + j;
                        String value = "value-" + j + "-" + System.currentTimeMillis();
                        
                        // HSET操作
                        jedis.hset(key, field, value);
                        
                        // HGET操作
                        jedis.hget(key, field);
                        
                        // HGETALL操作（每50次操作执行一次）
                        if (j % 50 == 0) {
                            jedis.hgetAll(key);
                        }
                    }
                    
                    return threadOperations * 2 + (threadOperations / 50); // HSET + HGET + HGETALL
                }
            }));
        }
        
        // 计算结果
        long totalOperations = 0;
        for (Future<Long> future : futures) {
            try {
                totalOperations += future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double opsPerSecond = totalOperations * 1000.0 / duration;
        
        System.out.println("结果:");
        System.out.println("  总操作数: " + totalOperations);
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  平均吞吐量: " + String.format("%.2f", opsPerSecond) + " ops/sec");
        System.out.println("  平均延迟: " + String.format("%.3f", duration * 1.0 / totalOperations) + "ms/op");
    }
    
    private static void testListOperations(JedisPool jedisPool, String keyPrefix, int threads, int operations) {
        System.out.println("\n=== 测试List操作性能 ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    long threadOperations = operations / threads;
                    
                    for (int j = 0; j < threadOperations; j++) {
                        String key = keyPrefix + ":" + threadId;
                        String value = "item-" + j + "-" + System.currentTimeMillis();
                        
                        // LPUSH操作
                        jedis.lpush(key, value);
                        
                        // RPUSH操作（交替执行）
                        if (j % 2 == 0) {
                            jedis.rpush(key, "rpush-" + value);
                        }
                        
                        // LRANGE操作（每20次操作执行一次）
                        if (j % 20 == 0) {
                            jedis.lrange(key, 0, 10);
                        }
                    }
                    
                    return threadOperations + (threadOperations / 2) + (threadOperations / 20);
                }
            }));
        }
        
        // 计算结果
        long totalOperations = 0;
        for (Future<Long> future : futures) {
            try {
                totalOperations += future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double opsPerSecond = totalOperations * 1000.0 / duration;
        
        System.out.println("结果:");
        System.out.println("  总操作数: " + totalOperations);
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  平均吞吐量: " + String.format("%.2f", opsPerSecond) + " ops/sec");
        System.out.println("  平均延迟: " + String.format("%.3f", duration * 1.0 / totalOperations) + "ms/op");
    }
    
    private static void testSetOperations(JedisPool jedisPool, String keyPrefix, int threads, int operations) {
        System.out.println("\n=== 测试Set操作性能 ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Long>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    long threadOperations = operations / threads;
                    
                    for (int j = 0; j < threadOperations; j++) {
                        String key = keyPrefix + ":" + threadId;
                        String member = "member-" + j + "-" + System.currentTimeMillis();
                        
                        // SADD操作
                        jedis.sadd(key, member);
                        
                        // SISMEMBER操作
                        jedis.sismember(key, member);
                        
                        // SMEMBERS操作（每30次操作执行一次）
                        if (j % 30 == 0) {
                            jedis.smembers(key);
                        }
                    }
                    
                    return threadOperations * 2 + (threadOperations / 30); // SADD + SISMEMBER + SMEMBERS
                }
            }));
        }
        
        // 计算结果
        long totalOperations = 0;
        for (Future<Long> future : futures) {
            try {
                totalOperations += future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double opsPerSecond = totalOperations * 1000.0 / duration;
        
        System.out.println("结果:");
        System.out.println("  总操作数: " + totalOperations);
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  平均吞吐量: " + String.format("%.2f", opsPerSecond) + " ops/sec");
        System.out.println("  平均延迟: " + String.format("%.3f", duration * 1.0 / totalOperations) + "ms/op");
    }
    
    private static void testConcurrentPerformance(JedisPool jedisPool, int threads, int operations) {
        System.out.println("\n=== 测试并发性能 ===");
        
        // 测试不同线程数下的性能
        int[] threadCounts = {1, 2, 4, 8, 16};
        
        for (int threadCount : threadCounts) {
            System.out.println("\n线程数: " + threadCount);
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<Void>> futures = new ArrayList<>();
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                futures.add(executor.submit(() -> {
                    try (Jedis jedis = jedisPool.getResource()) {
                        long threadOperations = operations / threadCount;
                        
                        for (int j = 0; j < threadOperations; j++) {
                            String key = "concurrent:" + threadId + ":" + j;
                            String value = "value-" + j;
                            
                            // 混合操作
                            jedis.set(key, value);
                            jedis.get(key);
                            
                            if (j % 5 == 0) {
                                jedis.incr("concurrent:counter:" + threadId);
                            }
                        }
                    }
                    return null;
                }));
            }
            
            // 等待所有任务完成
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            double opsPerSecond = operations * 2.0 * 1000 / duration; // 每个操作包括SET和GET
            
            System.out.println("  吞吐量: " + String.format("%.2f", opsPerSecond) + " ops/sec");
            System.out.println("  延迟: " + String.format("%.3f", duration * 1.0 / operations) + "ms/op");
        }
    }
    
    private static void testConnectionPoolPerformance(JedisPool jedisPool, int operations) {
        System.out.println("\n=== 测试连接池性能 ===");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < operations; i++) {
            try (Jedis jedis = jedisPool.getResource()) {
                String key = "pool:test:" + i;
                jedis.set(key, "test-value");
                jedis.get(key);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double opsPerSecond = operations * 2.0 * 1000 / duration;
        
        System.out.println("结果:");
        System.out.println("  总操作数: " + (operations * 2));
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  吞吐量: " + String.format("%.2f", opsPerSecond) + " ops/sec");
        System.out.println("  平均连接获取时间: " + String.format("%.3f", duration * 1.0 / operations) + "ms/op");
    }
    
    private static void cleanupTestData(JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 删除所有测试数据
            String[] patterns = {"string-test:*", "hash-test:*", "list-test:*", "set-test:*", "concurrent:*", "pool:test:*"};
            
            for (String pattern : patterns) {
                java.util.Set<String> keys = jedis.keys(pattern);
                if (!keys.isEmpty()) {
                    String[] keyArray = keys.toArray(new String[0]);
                    jedis.del(keyArray);
                }
            }
            
            System.out.println("\n测试数据已清理");
        }
    }
}
