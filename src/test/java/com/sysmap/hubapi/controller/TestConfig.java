package com.sysmap.hubapi.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
class TestConfig {

    @Bean
    RestTemplate restTemplate() {
        var rt = new RestTemplate();
        // Não lança exceção em 4xx/5xx — deixa o teste verificar o status
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response)
                    throws java.io.IOException {
                return false;
            }
        });
        return rt;
    }
}
