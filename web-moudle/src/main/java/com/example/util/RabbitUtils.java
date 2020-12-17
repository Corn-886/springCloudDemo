package com.example.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @Classname RabbitUtils
 * @Description TODO
 * @Author Jack
 * Date 2020/9/23 17:54
 * Version 1.0
 */
public class RabbitUtils {
    private static ConnectionFactory connectionFactory = new ConnectionFactory();

    static {
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        connectionFactory.setVirtualHost("/");
    }

    public static Connection getConnection() throws Exception {
        Connection conn = null;

        try {
            conn = connectionFactory.newConnection();
        } catch (Exception e) {
            throw new Exception(e);
        }
        return conn;

    }
}
