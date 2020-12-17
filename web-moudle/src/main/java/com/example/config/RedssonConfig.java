package com.example.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class RedssonConfig {

    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        config.useSingleServer().setConnectionPoolSize(1000);
        config.useSingleServer().setConnectionMinimumIdleSize(100);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
