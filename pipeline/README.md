# DBMatrix Pipeline

A Spring Boot application that automatically syncs PostgreSQL database schema to Neo4j every 5 minutes.

## Overview

This pipeline service:
- Connects to both `orders_db` and `products_db` PostgreSQL databases
- Discovers all tables and foreign key relationships from both databases
- Creates nodes in Neo4j (one per table, tagged with database name)
- Creates directed relationships in Neo4j (based on foreign keys)

## Prerequisites

- macOS (for Makefile automation)
- Homebrew (for installing Java and Maven)
- Docker Desktop (for running databases)
- Java 17 or higher
- Maven 3.6+
- PostgreSQL database running (from docker-compose.yml)
- Neo4j database running (from docker-compose.yml)

## Configuration

The application is configured via `src/main/resources/application.properties`:

- **PostgreSQL Orders**: `localhost:5432/orders_db`
- **PostgreSQL Products**: `localhost:5433/products_db`
- **Neo4j**: `bolt://localhost:7687`

Make sure these match your docker-compose setup.

## Quick Start with Makefile

The easiest way to get started is using the Makefile in the project root:

```bash
# Install dependencies, start Docker, build and run everything
make all

# Or step by step:
make install-deps      # Install Java 17 and Maven
make start-docker      # Start PostgreSQL and Neo4j containers
make build            # Build the Spring Boot application
make run              # Run the Spring Boot application
```

### Available Makefile Commands

- `make help` - Show all available commands
- `make install-deps` - Install Java 17 and Maven via Homebrew
- `make start-docker` - Start Docker containers (PostgreSQL and Neo4j)
- `make stop-docker` - Stop Docker containers
- `make build` - Build the Spring Boot application
- `make run` - Run the Spring Boot application
- `make run-background` - Run in background (logs to spring-boot.log)
- `make stop-app` - Stop the Spring Boot application
- `make clean` - Clean build artifacts
- `make all` - Do everything: install deps, start Docker, build and run

## Manual Setup

### Building

```bash
cd pipeline
mvn clean install
```

### Running

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/pipeline-1.0.0.jar
```

## How It Works

1. **Scheduled Task**: Runs every 5 minutes (300,000 milliseconds)
2. **Schema Discovery**: Queries PostgreSQL `information_schema` to discover:
   - All tables in the `public` schema
   - All foreign key constraints
3. **Neo4j Sync**: 
   - Creates/updates `Table` nodes with properties: `name`, `schema`
   - Creates `HAS_FOREIGN_KEY` relationships between tables
   - Relationship properties include: `constraintName`, `sourceColumn`, `targetColumn`

## Neo4j Graph Structure

- **Nodes**: Label `Table` with properties:
  - `name`: Table name
  - `database`: Database name (`orders_db` or `products_db`)
  - `schema`: Schema name (typically "public")
  - `updatedAt`: Timestamp of last update

- **Relationships**: Type `HAS_FOREIGN_KEY` with properties:
  - `constraintName`: Foreign key constraint name
  - `sourceColumn`: Column in source table
  - `targetColumn`: Column in target table
  - `database`: Database name
  - `updatedAt`: Timestamp of last update

## Example Queries

To view the schema graph in Neo4j Browser:

```cypher
// View all tables from both databases
MATCH (t:Table)
RETURN t.database, t.name
ORDER BY t.database, t.name

// View relationships from orders_db
MATCH (t:Table {database: 'orders_db'})-[r:HAS_FOREIGN_KEY]->(target:Table {database: 'orders_db'})
RETURN t, r, target

// View relationships from products_db
MATCH (t:Table {database: 'products_db'})-[r:HAS_FOREIGN_KEY]->(target:Table {database: 'products_db'})
RETURN t, r, target

// View all relationships
MATCH (t:Table)-[r:HAS_FOREIGN_KEY]->(target:Table)
RETURN t, r, target
```

## Logging

The application logs:
- Discovery of tables and foreign keys
- Successful sync operations
- Any errors during sync

Check logs for details about each sync operation.


