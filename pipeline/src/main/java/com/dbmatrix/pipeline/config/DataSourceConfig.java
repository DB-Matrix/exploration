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

    /**
     * Provides a DataSource configured from properties under "spring.datasource.orders".
     *
     * @return the DataSource built using configuration properties prefixed with "spring.datasource.orders"
     */
    @Bean(name = "ordersDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.orders")
    public DataSource ordersDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Creates a DataSource configured from properties under "spring.datasource.products".
     *
     * The returned DataSource is populated from configuration properties prefixed with
     * "spring.datasource.products".
     *
     * @return a DataSource configured for the products database
     */
    @Bean(name = "productsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.products")
    public DataSource productsDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Provide a template for JDBC operations against the orders data source.
     *
     * @param dataSource the orders DataSource bean qualified as "ordersDataSource"
     * @return a JdbcTemplate that uses the provided orders DataSource
     */
    @Bean(name = "ordersJdbcTemplate")
    @Primary
    public JdbcTemplate ordersJdbcTemplate(@Qualifier("ordersDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Creates a JdbcTemplate configured to use the products DataSource.
     *
     * @param dataSource the DataSource for the products database (injected from the "productsDataSource" bean)
     * @return a JdbcTemplate that executes queries against the products DataSource
     */
    @Bean(name = "productsJdbcTemplate")
    public JdbcTemplate productsJdbcTemplate(@Qualifier("productsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
