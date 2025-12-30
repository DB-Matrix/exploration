package com.dbmatrix.pipeline.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name = "ordersDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.orders")
    public DataSource ordersDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "productsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.products")
    public DataSource productsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "ordersJdbcTemplate")
    @Primary
    public JdbcTemplate ordersJdbcTemplate(@Qualifier("ordersDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "productsJdbcTemplate")
    public JdbcTemplate productsJdbcTemplate(@Qualifier("productsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

