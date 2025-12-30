package com.dbmatrix.pipeline.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Value("${spring.neo4j.uri}")
    private String neo4jUri;

    @Value("${spring.neo4j.authentication.username}")
    private String username;

    @Value("${spring.neo4j.authentication.password}")
    private String password;

    /**
     * Create a Neo4j Driver configured to connect to the configured URI.
     *
     * The driver is authenticated using basic authentication with the configured username and password.
     *
     * @return a configured {@link Driver} connected to the configured URI and authenticated with the configured credentials
     */
    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(neo4jUri, AuthTokens.basic(username, password));
    }
}

