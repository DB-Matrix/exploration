package com.dbmatrix.pipeline.service;

import com.dbmatrix.pipeline.model.ForeignKeyInfo;
import com.dbmatrix.pipeline.model.TableInfo;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Neo4jSyncService {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jSyncService.class);

    @Autowired
    private Driver neo4jDriver;

    /**
     * Sync database schema to Neo4j
     * Creates nodes for tables and relationships for foreign keys
     */
    public void syncSchemaToNeo4j(List<TableInfo> tables, List<ForeignKeyInfo> foreignKeys) {
        try (Session session = neo4jDriver.session()) {
            // Create nodes for all tables
            createTableNodes(session, tables);

            // Create relationships for foreign keys
            createForeignKeyRelationships(session, foreignKeys);

            logger.info("Successfully synced {} tables and {} foreign keys to Neo4j", 
                       tables.size(), foreignKeys.size());
        } catch (Exception e) {
            logger.error("Error syncing schema to Neo4j", e);
            throw new RuntimeException("Failed to sync schema to Neo4j", e);
        }
    }

    /**
     * Create nodes for each table
     * Uses composite key: databaseName + tableName to uniquely identify tables
     */
    private void createTableNodes(Session session, List<TableInfo> tables) {
        String cypher = """
            MERGE (t:Table {name: $tableName, database: $databaseName})
            SET t.schema = $schemaName,
                t.updatedAt = datetime()
            """;

        for (TableInfo table : tables) {
            session.run(cypher, Values.parameters(
                "tableName", table.getTableName(),
                "databaseName", table.getDatabaseName(),
                "schemaName", table.getSchemaName()
            ));
            logger.debug("Created/updated table node: {}.{}", 
                        table.getDatabaseName(), table.getTableName());
        }
    }

    /**
     * Create relationships between tables based on foreign keys
     * Only creates relationships within the same database
     */
    private void createForeignKeyRelationships(Session session, List<ForeignKeyInfo> foreignKeys) {
        String cypher = """
            MATCH (source:Table {name: $sourceTable, database: $databaseName})
            MATCH (target:Table {name: $targetTable, database: $databaseName})
            MERGE (source)-[r:HAS_FOREIGN_KEY {
                constraintName: $constraintName,
                sourceColumn: $sourceColumn,
                targetColumn: $targetColumn,
                database: $databaseName
            }]->(target)
            SET r.updatedAt = datetime()
            """;

        for (ForeignKeyInfo fk : foreignKeys) {
            session.run(cypher, Values.parameters(
                "sourceTable", fk.getSourceTable(),
                "targetTable", fk.getTargetTable(),
                "databaseName", fk.getDatabaseName(),
                "constraintName", fk.getConstraintName(),
                "sourceColumn", fk.getSourceColumn(),
                "targetColumn", fk.getTargetColumn()
            ));
            logger.debug("Created foreign key relationship: {}.{} -> {}.{}", 
                        fk.getDatabaseName(), fk.getSourceTable(),
                        fk.getDatabaseName(), fk.getTargetTable());
        }
    }
}


