package dev.fkreuzer.shotlog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("shotlog.disag")
public record DisagProperties(String webhookUrl, String webhookSecret) {
}
