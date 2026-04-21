package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.Scanner;

/**
 * Redis交互式命令行工具
 * 允许用户在运行时测试各种Redis命令
 */
public class RedisCLI {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 30379;
    private static JedisPool jedisPool;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Redis交互式命令行工具 ===");
        System.out.println("请输入Redis连接配置（直接回车使用默认值）:");
        
        System.out.print("主机地址 [localhost]: ");
        String host = scanner.nextLine();
        if (host.trim().isEmpty()) {
            host = DEFAULT_HOST;
        }
        
        System.out.print("端口号 [30379]: ");
        String portStr = scanner.nextLine();
        int port = portStr.trim().isEmpty() ? DEFAULT_PORT : Integer.parseInt(portStr);
        
        // 初始化连接池
        initJedisPool(host, port);
        
        System.out.println("\n已连接到: " + host + ":" + port);
        System.out.println("输入 'help' 查看可用命令");
        System.out.println("输入 'exit' 退出");
        System.out.println("=====================================\n");
        
        // 主命令循环
        while (true) {
            System.out.print("redis> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("再见！");
                break;
            }
            
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            
            if (input.equalsIgnoreCase("clear")) {
                // 清屏
                for (int i = 0; i < 50; i++) {
                    System.out.println();
                }
                continue;
            }
            
            // 执行命令
            executeCommand(input);
        }
        
        closeJedisPool();
        scanner.close();
    }
    
    private static void initJedisPool(String host, int port) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(5);
        config.setMaxIdle(3);
        config.setMinIdle(1);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        
        jedisPool = new JedisPool(config, host, port);
    }
    
    private static void closeJedisPool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
    
    private static void showHelp() {
        System.out.println("\n=== Redis CLI 帮助 ===");
        System.out.println("通用命令:");
        System.out.println("  help     - 显示此帮助");
        System.out.println("  exit     - 退出程序");
        System.out.println("  clear    - 清屏");
        System.out.println("  ping     - 测试连接");
        System.out.println("  info     - 查看服务器信息");
        System.out.println("  keys *   - 查看所有键");
        System.out.println("\n数据结构命令:");
        System.out.println("  String:");
        System.out.println("    set key value        - 设置字符串");
        System.out.println("    get key              - 获取字符串");
        System.out.println("    incr key             - 自增");
        System.out.println("  List:");
        System.out.println("    lpush key value...   - 左侧添加");
        System.out.println("    rpush key value...   - 右侧添加");
        System.out.println("    lrange key start end - 获取范围");
        System.out.println("  Hash:");
        System.out.println("    hset key field value - 设置哈希字段");
        System.out.println("    hget key field       - 获取哈希字段");
        System.out.println("    hgetall key          - 获取所有字段");
        System.out.println("\n示例:");
        System.out.println("  redis> set name Alice");
        System.out.println("  redis> get name");
        System.out.println("  redis> hset user:1 name Bob age 25");
        System.out.println("  redis> hgetall user:1");
        System.out.println("=====================================\n");
    }
    
    private static void executeCommand(String command) {
        try (Jedis jedis = jedisPool.getResource()) {
            // 分割命令和参数
            String[] parts = command.split("\\s+");
            String cmd = parts[0].toLowerCase();
            
            switch (cmd) {
                case "ping":
                    String result = jedis.ping();
                    System.out.println(result);
                    break;
                    
                case "info":
                    String info = jedis.info();
                    System.out.println(info);
                    break;
                    
                case "keys":
                    if (parts.length < 2) {
                        System.out.println("错误: 需要指定模式，例如: keys *");
                        return;
                    }
                    java.util.Set<String> keys = jedis.keys(parts[1]);
                    System.out.println("匹配的键: " + keys);
                    break;
                    
                case "set":
                    if (parts.length < 3) {
                        System.out.println("错误: set命令需要键和值参数");
                        return;
                    }
                    jedis.set(parts[1], parts[2]);
                    System.out.println("OK");
                    break;
                    
                case "get":
                    if (parts.length < 2) {
                        System.out.println("错误: get命令需要键参数");
                        return;
                    }
                    String value = jedis.get(parts[1]);
                    System.out.println(value != null ? value : "(nil)");
                    break;
                    
                case "incr":
                    if (parts.length < 2) {
                        System.out.println("错误: incr命令需要键参数");
                        return;
                    }
                    long newValue = jedis.incr(parts[1]);
                    System.out.println(newValue);
                    break;
                    
                case "del":
                    if (parts.length < 2) {
                        System.out.println("错误: del命令需要键参数");
                        return;
                    }
                    long deleted = jedis.del(parts[1]);
                    System.out.println(deleted + "个键已删除");
                    break;
                    
                case "lpush":
                    if (parts.length < 3) {
                        System.out.println("错误: lpush命令需要键和至少一个值参数");
                        return;
                    }
                    String[] lpushValues = new String[parts.length - 2];
                    System.arraycopy(parts, 2, lpushValues, 0, parts.length - 2);
                    long lpushResult = jedis.lpush(parts[1], lpushValues);
                    System.out.println(lpushResult);
                    break;
                    
                case "rpush":
                    if (parts.length < 3) {
                        System.out.println("错误: rpush命令需要键和至少一个值参数");
                        return;
                    }
                    String[] rpushValues = new String[parts.length - 2];
                    System.arraycopy(parts, 2, rpushValues, 0, parts.length - 2);
                    long rpushResult = jedis.rpush(parts[1], rpushValues);
                    System.out.println(rpushResult);
                    break;
                    
                case "lrange":
                    if (parts.length < 4) {
                        System.out.println("错误: lrange命令需要键、起始和结束索引");
                        return;
                    }
                    java.util.List<String> listValues = jedis.lrange(
                        parts[1], 
                        Long.parseLong(parts[2]), 
                        Long.parseLong(parts[3])
                    );
                    for (int i = 0; i < listValues.size(); i++) {
                        System.out.println((i + 1) + ") " + listValues.get(i));
                    }
                    break;
                    
                case "hset":
                    if (parts.length < 4) {
                        System.out.println("错误: hset命令需要键、字段和值参数");
                        return;
                    }
                    jedis.hset(parts[1], parts[2], parts[3]);
                    System.out.println("OK");
                    break;
                    
                case "hget":
                    if (parts.length < 3) {
                        System.out.println("错误: hget命令需要键和字段参数");
                        return;
                    }
                    String fieldValue = jedis.hget(parts[1], parts[2]);
                    System.out.println(fieldValue != null ? fieldValue : "(nil)");
                    break;
                    
                case "hgetall":
                    if (parts.length < 2) {
                        System.out.println("错误: hgetall命令需要键参数");
                        return;
                    }
                    java.util.Map<String, String> hashMap = jedis.hgetAll(parts[1]);
                    for (java.util.Map.Entry<String, String> entry : hashMap.entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());
                    }
                    break;
                    
                case "ttl":
                    if (parts.length < 2) {
                        System.out.println("错误: ttl命令需要键参数");
                        return;
                    }
                    long ttl = jedis.ttl(parts[1]);
                    System.out.println(ttl > 0 ? ttl + "秒" : (ttl == -1 ? "永不过期" : "键不存在或已过期"));
                    break;
                    
                case "expire":
                    if (parts.length < 3) {
                        System.out.println("错误: expire命令需要键和过期时间参数");
                        return;
                    }
                    long expireResult = jedis.expire(parts[1], Long.parseLong(parts[2]));
                    System.out.println(expireResult == 1 ? "OK" : "失败: 键不存在");
                    break;
                    
                default:
                    System.out.println("未知命令: " + cmd + " (输入 'help' 查看可用命令)");
            }
            
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }
}