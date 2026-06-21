package com.splitease.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        if (databaseUrl == null || databaseUrl.isEmpty() || databaseUrl.startsWith("jdbc:")) {
            // Fallback to default Spring Boot configuration if URL is empty or already a JDBC URL
            return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
        }

        URI dbUri = new URI(databaseUrl);

        String username = null;
        String password = null;
        if (dbUri.getUserInfo() != null) {
            String[] userInfo = dbUri.getUserInfo().split(":");
            username = userInfo[0];
            if (userInfo.length > 1) {
                password = userInfo[1];
            }
        }

        int port = dbUri.getPort() != -1 ? dbUri.getPort() : 5432;
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + port + dbUri.getPath();
        
        // Neon requires sslmode=require
        if (dbUri.getQuery() != null) {
            dbUrl += "?" + dbUri.getQuery();
        } else {
            dbUrl += "?sslmode=require";
        }

        HikariConfig basicConfig = new HikariConfig();
        basicConfig.setJdbcUrl(dbUrl);
        if (username != null) basicConfig.setUsername(username);
        if (password != null) basicConfig.setPassword(password);
        basicConfig.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(basicConfig);
    }
}
