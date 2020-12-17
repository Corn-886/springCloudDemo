package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * <p>Author: chenzz
 * <p>Date: Sep 12, 2019
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restHasErrorTemplate(ClientHttpRequestFactory factory) throws Exception {
        RestTemplate restTemplate = new RestTemplate(factory);
        ResponseErrorHandler responseErrorHandler = new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return true;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

            }
        };
        restTemplate.setErrorHandler(responseErrorHandler);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        //读取超时5秒
        factory.setReadTimeout(5000);
        //连接超时15秒
        factory.setConnectTimeout(15000);
        return factory;
    }
}

