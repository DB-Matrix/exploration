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
     * Synchronizes provided table and foreign-key metadata into Neo4j as table nodes and
     * HAS_FOREIGN_KEY relationships.
     *
     * This will create or update nodes for each table and create or update relationships for
     * each foreign key (within the same database) in the Neo4j instance supplied by the
     * injected driver.
     *
     * @param tables      list of table metadata to create or update as Neo4j nodes
     * @param foreignKeys list of foreign-key metadata to create or update as relationships
     * @throws RuntimeException if an error occurs while synchronizing the schema to Neo4j
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
     * Create or update Neo4j nodes representing the provided tables.
     *
     * Each node is uniquely identified by the combination of database name and table name.
     * The node's `schema` property is set from the table data and `updatedAt` is set to the current datetime.
     *
     * @param tables list of TableInfo objects describing the tables to create or update
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
     * Create HAS_FOREIGN_KEY relationships between table nodes for the provided foreign keys within the same database.
     *
     * For each entry in {@code foreignKeys}, ensures a relationship from the source table node to the target table node
     * in the same database and sets relationship properties including constraint name, source column, target column,
     * database, and an updatedAt timestamp.
     *
     * @param foreignKeys list of foreign key descriptors; each item must provide source table, target table, database,
     *                    constraint name, source column, and target column
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

