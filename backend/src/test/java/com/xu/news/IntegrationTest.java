package com.xu.news;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public class IntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("xu_news")
        .withUsername("xu")
        .withPassword("xu_pass");

    static {
        postgres.start();
        System.setProperty("SPRING_DATASOURCE_URL", postgres.getJdbcUrl());
        System.setProperty("SPRING_DATASOURCE_USERNAME", postgres.getUsername());
        System.setProperty("SPRING_DATASOURCE_PASSWORD", postgres.getPassword());
    }

    @Test
    public void contextLoads() {
        // basic spring context smoke test
    }
}
