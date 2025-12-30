package com.dbmatrix.pipeline.service;

import com.dbmatrix.pipeline.model.ForeignKeyInfo;
import com.dbmatrix.pipeline.model.TableInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaDiscoveryService {

    private final JdbcTemplate ordersJdbcTemplate;
    private final JdbcTemplate productsJdbcTemplate;

    public SchemaDiscoveryService(
            @Qualifier("ordersJdbcTemplate") JdbcTemplate ordersJdbcTemplate,
            @Qualifier("productsJdbcTemplate") JdbcTemplate productsJdbcTemplate) {
        this.ordersJdbcTemplate = ordersJdbcTemplate;
        this.productsJdbcTemplate = productsJdbcTemplate;
    }

    /**
     * Discover all tables in a specific database
     */
    public List<TableInfo> discoverTables(String databaseName) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(databaseName);
        
        String sql = """
            SELECT table_schema, table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TableInfo table = new TableInfo();
            table.setSchemaName(rs.getString("table_schema"));
            table.setTableName(rs.getString("table_name"));
            table.setDatabaseName(databaseName);
            return table;
        });
    }

    /**
     * Discover all foreign key relationships in a specific database
     */
    public List<ForeignKeyInfo> discoverForeignKeys(String databaseName) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(databaseName);
        
        String sql = """
            SELECT
                tc.constraint_name,
                tc.table_name AS source_table,
                kcu.column_name AS source_column,
                ccu.table_name AS target_table,
                ccu.column_name AS target_column
            FROM information_schema.table_constraints AS tc
            JOIN information_schema.key_column_usage AS kcu
                ON tc.constraint_name = kcu.constraint_name
                AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage AS ccu
                ON ccu.constraint_name = tc.constraint_name
                AND ccu.table_schema = tc.table_schema
            WHERE tc.constraint_type = 'FOREIGN KEY'
            AND tc.table_schema = 'public'
            ORDER BY tc.table_name, tc.constraint_name
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ForeignKeyInfo fk = new ForeignKeyInfo();
            fk.setConstraintName(rs.getString("constraint_name"));
            fk.setSourceTable(rs.getString("source_table"));
            fk.setSourceColumn(rs.getString("source_column"));
            fk.setTargetTable(rs.getString("target_table"));
            fk.setTargetColumn(rs.getString("target_column"));
            fk.setDatabaseName(databaseName);
            return fk;
        });
    }

    private JdbcTemplate getJdbcTemplate(String databaseName) {
        if ("orders_db".equals(databaseName)) {
            return ordersJdbcTemplate;
        } else if ("products_db".equals(databaseName)) {
            return productsJdbcTemplate;
        } else {
            throw new IllegalArgumentException("Unknown database: " + databaseName);
        }
    }
}


