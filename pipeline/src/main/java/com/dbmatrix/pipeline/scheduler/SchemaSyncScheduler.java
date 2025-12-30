package com.dbmatrix.pipeline.scheduler;

import com.dbmatrix.pipeline.model.ForeignKeyInfo;
import com.dbmatrix.pipeline.model.TableInfo;
import com.dbmatrix.pipeline.service.Neo4jSyncService;
import com.dbmatrix.pipeline.service.SchemaDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SchemaSyncScheduler.class);

    @Autowired
    private SchemaDiscoveryService schemaDiscoveryService;

    @Autowired
    private Neo4jSyncService neo4jSyncService;

    /**
     * Trigger a periodic schema discovery for configured PostgreSQL databases and sync the results to Neo4j.
     *
     * Discovers tables and foreign keys from the orders_db and products_db databases, aggregates the discovered
     * schema information, and invokes the Neo4j sync service to persist the combined schema. This method is
     * executed on a fixed schedule by the Spring scheduler.
     */
    @Scheduled(fixedRate = 300000) // 300000 milliseconds = 5 minutes
    public void syncSchema() {
        logger.info("Starting scheduled schema sync for all databases...");
        
        try {
            List<TableInfo> allTables = new java.util.ArrayList<>();
            List<ForeignKeyInfo> allForeignKeys = new java.util.ArrayList<>();

            // Sync orders_db
            logger.info("Syncing orders_db...");
            List<TableInfo> ordersTables = schemaDiscoveryService.discoverTables("orders_db");
            List<ForeignKeyInfo> ordersForeignKeys = schemaDiscoveryService.discoverForeignKeys("orders_db");
            logger.info("Discovered {} tables and {} foreign keys from orders_db", 
                       ordersTables.size(), ordersForeignKeys.size());
            allTables.addAll(ordersTables);
            allForeignKeys.addAll(ordersForeignKeys);

            // Sync products_db
            logger.info("Syncing products_db...");
            List<TableInfo> productsTables = schemaDiscoveryService.discoverTables("products_db");
            List<ForeignKeyInfo> productsForeignKeys = schemaDiscoveryService.discoverForeignKeys("products_db");
            logger.info("Discovered {} tables and {} foreign keys from products_db", 
                       productsTables.size(), productsForeignKeys.size());
            allTables.addAll(productsTables);
            allForeignKeys.addAll(productsForeignKeys);

            // Sync all to Neo4j
            neo4jSyncService.syncSchemaToNeo4j(allTables, allForeignKeys);

            logger.info("Schema sync completed successfully. Total: {} tables, {} foreign keys", 
                       allTables.size(), allForeignKeys.size());
        } catch (Exception e) {
            logger.error("Error during schema sync", e);
        }
    }
}

