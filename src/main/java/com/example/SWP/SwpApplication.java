package com.example.SWP;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SwpApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        System.setProperty("server.port", dotenv.get("SERVER_PORT", "8080"));
        System.setProperty("spring.datasource.url", dotenv.get("DB_URL", ""));
        System.setProperty("spring.datasource.username", dotenv.get("DB_USERNAME", ""));
        System.setProperty("spring.datasource.password", dotenv.get("DB_PASSWORD", ""));
        System.setProperty("spring.datasource.driver-class-name", dotenv.get("DB_DRIVER", "com.microsoft.sqlserver.jdbc.SQLServerDriver"));

        System.setProperty("spring.mail.host", dotenv.get("MAIL_HOST", "smtp.gmail.com"));
        System.setProperty("spring.mail.port", dotenv.get("MAIL_PORT", "587"));
        System.setProperty("spring.mail.username", dotenv.get("MAIL_USERNAME", ""));
        System.setProperty("spring.mail.password", dotenv.get("MAIL_PASSWORD", ""));
        System.setProperty("spring.mail.properties.mail.smtp.auth", dotenv.get("MAIL_SMTP_AUTH", "true"));
        System.setProperty("spring.mail.properties.mail.smtp.starttls.enable", dotenv.get("MAIL_SMTP_STARTTLS_ENABLE", "true"));

        System.setProperty("spring.data.redis.host", dotenv.get("REDIS_HOST", "localhost"));
        System.setProperty("spring.data.redis.port", dotenv.get("REDIS_PORT", "6379"));
        System.setProperty("spring.data.redis.password", dotenv.get("REDIS_PASSWORD", ""));
        System.setProperty("spring.data.redis.ssl.enabled", dotenv.get("REDIS_SSL_ENABLED", "false"));

        System.setProperty("jwt.secret", dotenv.get("JWT_SECRET", "defaultSecretKey"));
        System.setProperty("jwt.access-expiration", dotenv.get("JWT_ACCESS_EXPIRATION", "900000"));
        System.setProperty("jwt.refresh-expiration", dotenv.get("JWT_REFRESH_EXPIRATION", "604800000"));
        System.setProperty("jwt.issuer", dotenv.get("JWT_ISSUER", "localhost:8080"));

        System.setProperty("spring.security.oauth2.client.registration.google.client-id", dotenv.get("GOOGLE_CLIENT_ID", ""));
        System.setProperty("spring.security.oauth2.client.registration.google.client-secret", dotenv.get("GOOGLE_CLIENT_SECRET", ""));

        SpringApplication.run(SwpApplication.class, args);
	}

}
