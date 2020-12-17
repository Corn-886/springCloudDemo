package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableEurekaClient
public class OperateApplication {

//    @Bean
//    @LoadBalanced
//    //负责负载均衡
//    RestTemplate restTemplate(){
//        return new RestTemplate();
//    }


    public static void main(String[] args) {
        SpringApplication.run(OperateApplication.class,args);
    }

}
