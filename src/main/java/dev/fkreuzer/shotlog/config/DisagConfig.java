package dev.fkreuzer.shotlog.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(DisagProperties.class)
public class DisagConfig {

    @Bean
    public RestClient disagRestClient() {
        return RestClient.create();
    }
}
