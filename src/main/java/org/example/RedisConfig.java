package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Redis配置管理类
 * 支持从配置文件、环境变量、系统属性、命令行参数读取配置
 */
public class RedisConfig {
    
    // 默认配置
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6379;
    private static final int DEFAULT_DATABASE = 0;
    private static final int DEFAULT_TIMEOUT = 2000; // 2秒
    private static final int DEFAULT_MAX_TOTAL = 8;
    private static final int DEFAULT_MAX_IDLE = 4;
    private static final int DEFAULT_MIN_IDLE = 1;
    
    // 配置键名
    private static final String KEY_REDIS_HOST = "redis.host";
    private static final String KEY_REDIS_PORT = "redis.port";
    private static final String KEY_REDIS_DATABASE = "redis.database";
    private static final String KEY_REDIS_PASSWORD = "redis.password";
    private static final String KEY_REDIS_TIMEOUT = "redis.timeout";
    private static final String KEY_REDIS_MAX_TOTAL = "redis.pool.maxTotal";
    private static final String KEY_REDIS_MAX_IDLE = "redis.pool.maxIdle";
    private static final String KEY_REDIS_MIN_IDLE = "redis.pool.minIdle";
    private static final String KEY_REDIS_TEST_ON_BORROW = "redis.pool.testOnBorrow";
    
    // 环境变量名
    private static final String ENV_REDIS_HOST = "REDIS_HOST";
    private static final String ENV_REDIS_PORT = "REDIS_PORT";
    private static final String ENV_REDIS_PASSWORD = "REDIS_PASSWORD";
    private static final String ENV_REDIS_DATABASE = "REDIS_DATABASE";
    
    // 系统属性名
    private static final String SYS_REDIS_HOST = "redis.host";
    private static final String SYS_REDIS_PORT = "redis.port";
    
    private String host;
    private int port;
    private int database;
    private String password;
    private int timeout;
    private int maxTotal;
    private int maxIdle;
    private int minIdle;
    private boolean testOnBorrow;
    
    private Properties properties;
    
    /**
     * 构造函数，自动加载配置
     */
    public RedisConfig() {
        // 加载配置
        loadConfig();
    }
    
    /**
     * 加载配置，按照优先级：配置文件 > 环境变量 > 系统属性 > 默认值
     */
    private void loadConfig() {
        properties = new Properties();
        
        // 1. 首先尝试从配置文件加载
        loadFromConfigFile();
        
        // 2. 从环境变量加载（覆盖配置文件）
        loadFromEnvironment();
        
        // 3. 从系统属性加载（覆盖环境变量）
        loadFromSystemProperties();
        
        // 4. 设置最终值
        setFinalValues();
    }
    
    /**
     * 从配置文件加载
     */
    private void loadFromConfigFile() {
        String[] configFiles = {
            "redis.properties",
            "config/redis.properties",
            "src/main/resources/redis.properties",
            System.getProperty("user.home") + "/.redis-demo/redis.properties"
        };
        
        for (String configFile : configFiles) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                System.out.println("已加载配置文件: " + configFile);
                break; // 加载成功就停止
            } catch (IOException e) {
                // 文件不存在，继续尝试下一个
            }
        }
    }
    
    /**
     * 从环境变量加载
     */
    private void loadFromEnvironment() {
        String envHost = System.getenv(ENV_REDIS_HOST);
        if (envHost != null && !envHost.trim().isEmpty()) {
            properties.setProperty(KEY_REDIS_HOST, envHost);
        }
        
        String envPort = System.getenv(ENV_REDIS_PORT);
        if (envPort != null && !envPort.trim().isEmpty()) {
            properties.setProperty(KEY_REDIS_PORT, envPort);
        }
        
        String envPassword = System.getenv(ENV_REDIS_PASSWORD);
        if (envPassword != null && !envPassword.trim().isEmpty()) {
            properties.setProperty(KEY_REDIS_PASSWORD, envPassword);
        }
        
        String envDatabase = System.getenv(ENV_REDIS_DATABASE);
        if (envDatabase != null && !envDatabase.trim().isEmpty()) {
            properties.setProperty(KEY_REDIS_DATABASE, envDatabase);
        }
    }
    
    /**
     * 从系统属性加载
     */
    private void loadFromSystemProperties() {
        String sysHost = System.getProperty(SYS_REDIS_HOST);
        if (sysHost != null && !sysHost.trim().isEmpty()) {
            properties.setProperty(KEY_REDIS_HOST, sysHost);
        }
        
        String sysPort = System.getProperty(SYS_REDIS_PORT);
        if (sysPort != null && !sysPort.trim().isEmpty()) {
            properties.setProperty(KEY_REDIS_PORT, sysPort);
        }
    }
    
    /**
     * 设置最终配置值
     */
    private void setFinalValues() {
        host = properties.getProperty(KEY_REDIS_HOST, DEFAULT_HOST);
        port = Integer.parseInt(properties.getProperty(KEY_REDIS_PORT, String.valueOf(DEFAULT_PORT)));
        database = Integer.parseInt(properties.getProperty(KEY_REDIS_DATABASE, String.valueOf(DEFAULT_DATABASE)));
        password = properties.getProperty(KEY_REDIS_PASSWORD, null);
        timeout = Integer.parseInt(properties.getProperty(KEY_REDIS_TIMEOUT, String.valueOf(DEFAULT_TIMEOUT)));
        maxTotal = Integer.parseInt(properties.getProperty(KEY_REDIS_MAX_TOTAL, String.valueOf(DEFAULT_MAX_TOTAL)));
        maxIdle = Integer.parseInt(properties.getProperty(KEY_REDIS_MAX_IDLE, String.valueOf(DEFAULT_MAX_IDLE)));
        minIdle = Integer.parseInt(properties.getProperty(KEY_REDIS_MIN_IDLE, String.valueOf(DEFAULT_MIN_IDLE)));
        testOnBorrow = Boolean.parseBoolean(properties.getProperty(KEY_REDIS_TEST_ON_BORROW, "true"));
    }
    
    // Getter方法
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getDatabase() {
        return database;
    }
    
    public String getPassword() {
        return password;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getMaxTotal() {
        return maxTotal;
    }
    
    public int getMaxIdle() {
        return maxIdle;
    }
    
    public int getMinIdle() {
        return minIdle;
    }
    
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }
    
    /**
     * 获取连接URL
     */
    public String getConnectionUrl() {
        return "redis://" + host + ":" + port + "/" + database;
    }
    
    /**
     * 显示当前配置
     */
    public void displayConfig() {
        System.out.println("=== Redis配置信息 ===");
        System.out.println("主机: " + host);
        System.out.println("端口: " + port);
        System.out.println("数据库: " + database);
        System.out.println("密码: " + (password != null ? "已设置" : "未设置"));
        System.out.println("超时: " + timeout + "ms");
        System.out.println("连接池配置:");
        System.out.println("  最大连接数: " + maxTotal);
        System.out.println("  最大空闲连接: " + maxIdle);
        System.out.println("  最小空闲连接: " + minIdle);
        System.out.println("  借出时测试: " + testOnBorrow);
        System.out.println("连接URL: " + getConnectionUrl());
        System.out.println("========================\n");
    }
    
    /**
     * 创建示例配置文件
     */
    public static String createSampleConfigFile() {
        return "# Redis配置示例\n" +
               "# 复制此文件为 redis.properties 并修改配置\n\n" +
               "# 连接配置\n" +
               "redis.host=localhost\n" +
               "redis.port=6379\n" +
               "redis.database=0\n" +
               "# redis.password=your_password\n" +
               "redis.timeout=2000\n\n" +
               "# 连接池配置\n" +
               "redis.pool.maxTotal=8\n" +
               "redis.pool.maxIdle=4\n" +
               "redis.pool.minIdle=1\n" +
               "redis.pool.testOnBorrow=true\n\n" +
               "# 配置加载优先级:\n" +
               "# 1. 配置文件 (redis.properties)\n" +
               "# 2. 环境变量 (REDIS_HOST, REDIS_PORT, REDIS_PASSWORD, REDIS_DATABASE)\n" +
               "# 3. 系统属性 (-Dredis.host=localhost -Dredis.port=6379)\n" +
               "# 4. 默认值";
    }
    
    /**
     * 测试配置类
     */
    public static void main(String[] args) {
        System.out.println("=== Redis配置测试 ===\n");
        
        // 创建配置实例
        RedisConfig config = new RedisConfig();
        
        // 显示配置
        config.displayConfig();
        
        // 显示示例配置文件内容
        System.out.println("=== 示例配置文件 ===");
        System.out.println(createSampleConfigFile());
        
        System.out.println("\n=== 使用示例 ===");
        System.out.println("1. 设置环境变量:");
        System.out.println("   export REDIS_HOST=localhost");
        System.out.println("   export REDIS_PORT=6379");
        System.out.println("\n2. 命令行参数:");
        System.out.println("   java -Dredis.host=localhost -Dredis.port=6379 -jar your-app.jar");
        System.out.println("\n3. 配置文件:");
        System.out.println("   创建 redis.properties 文件并放置于:");
        System.out.println("   - 项目根目录");
        System.out.println("   - config/ 目录");
        System.out.println("   - src/main/resources/ 目录");
        System.out.println("   - ~/.redis-demo/ 目录");
    }
}
