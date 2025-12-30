.PHONY: help install-java install-maven install-deps setup-docker start-docker stop-docker build run clean all stop

# Variables
JAVA_VERSION := 17
MAVEN_VERSION := 3.9.12
PROJECT_ROOT := $(shell pwd)
PIPELINE_DIR := $(PROJECT_ROOT)/pipeline
SETUP_DIR := $(PROJECT_ROOT)/setup

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

help: ## Show this help message
	@echo "$(GREEN)Available targets:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'

install-java: ## Install Java 17 using Homebrew
	@echo "$(GREEN)Checking Java installation...$(NC)"
	@if command -v java >/dev/null 2>&1 && java -version 2>&1 | grep -q "17"; then \
		echo "$(GREEN)Java 17 is already installed$(NC)"; \
	else \
		echo "$(YELLOW)Installing Java $(JAVA_VERSION)...$(NC)"; \
		brew install openjdk@$(JAVA_VERSION) || true; \
		echo "$(GREEN)Java $(JAVA_VERSION) installed.$(NC)"; \
		echo "$(YELLOW)Note: You may need to add to your ~/.zshrc:$(NC)"; \
		echo "  export PATH=\"/opt/homebrew/opt/openjdk@$(JAVA_VERSION)/bin:\$$PATH\""; \
		echo "  export JAVA_HOME=\"/opt/homebrew/opt/openjdk@$(JAVA_VERSION)\""; \
	fi

install-maven: ## Install Maven using Homebrew
	@echo "$(GREEN)Checking Maven installation...$(NC)"
	@if command -v mvn >/dev/null 2>&1; then \
		echo "$(GREEN)Maven is already installed: $$(mvn --version | head -1)$(NC)"; \
	else \
		echo "$(YELLOW)Installing Maven...$(NC)"; \
		brew install maven || true; \
		echo "$(GREEN)Maven installed.$(NC)"; \
	fi

install-deps: install-java install-maven ## Install all dependencies (Java and Maven)
	@echo "$(GREEN)All dependencies installed!$(NC)"

setup-env: ## Setup environment variables for Java
	@echo "$(YELLOW)Setting up environment variables...$(NC)"
	@export PATH="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)/bin:$$PATH" 2>/dev/null || true
	@export JAVA_HOME="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)" 2>/dev/null || true
	@echo "$(GREEN)Environment variables set (for current session)$(NC)"

check-docker: ## Check if Docker is running
	@if ! docker info >/dev/null 2>&1; then \
		echo "$(RED)Error: Docker is not running. Please start Docker Desktop.$(NC)"; \
		exit 1; \
	else \
		echo "$(GREEN)Docker is running$(NC)"; \
	fi

start-docker: check-docker ## Start Docker containers (PostgreSQL and Neo4j)
	@echo "$(GREEN)Starting Docker containers...$(NC)"
	@cd $(SETUP_DIR) && docker-compose up -d
	@echo "$(GREEN)Waiting for services to be healthy...$(NC)"
	@sleep 5
	@echo "$(GREEN)Docker containers started!$(NC)"
	@echo "$(YELLOW)PostgreSQL Orders: localhost:5432$(NC)"
	@echo "$(YELLOW)PostgreSQL Products: localhost:5433$(NC)"
	@echo "$(YELLOW)Neo4j Browser: http://localhost:7474$(NC)"
	@echo "$(YELLOW)Neo4j Bolt: localhost:7687$(NC)"

stop-docker: ## Stop Docker containers
	@echo "$(YELLOW)Stopping Docker containers...$(NC)"
	@cd $(SETUP_DIR) && docker-compose down
	@echo "$(GREEN)Docker containers stopped.$(NC)"

restart-docker: stop-docker start-docker ## Restart Docker containers

status-docker: ## Show status of Docker containers
	@cd $(SETUP_DIR) && docker-compose ps

build: setup-env ## Build the Spring Boot application
	@echo "$(GREEN)Building Spring Boot application...$(NC)"
	@export PATH="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)/bin:$$PATH" && \
	export JAVA_HOME="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)" && \
	cd $(PIPELINE_DIR) && mvn clean package -DskipTests
	@echo "$(GREEN)Build completed!$(NC)"

run: setup-env ## Run the Spring Boot application
	@echo "$(GREEN)Starting Spring Boot application...$(NC)"
	@export PATH="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)/bin:$$PATH" && \
	export JAVA_HOME="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)" && \
	cd $(PIPELINE_DIR) && mvn spring-boot:run

run-background: setup-env ## Run the Spring Boot application in background
	@echo "$(GREEN)Starting Spring Boot application in background...$(NC)"
	@export PATH="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)/bin:$$PATH" && \
	export JAVA_HOME="/opt/homebrew/opt/openjdk@$(JAVA_VERSION)" && \
	cd $(PIPELINE_DIR) && nohup mvn spring-boot:run > spring-boot.log 2>&1 & \
	echo "$$!" > spring-boot.pid && \
	echo "$(GREEN)Spring Boot application started in background (PID: $$(cat $(PIPELINE_DIR)/spring-boot.pid))$(NC)" && \
	echo "$(YELLOW)Logs: $(PIPELINE_DIR)/spring-boot.log$(NC)"

stop-app: ## Stop the Spring Boot application
	@if [ -f $(PIPELINE_DIR)/spring-boot.pid ]; then \
		PID=$$(cat $(PIPELINE_DIR)/spring-boot.pid); \
		if ps -p $$PID > /dev/null 2>&1; then \
			kill $$PID && echo "$(GREEN)Spring Boot application stopped (PID: $$PID)$(NC)"; \
		else \
			echo "$(YELLOW)Process not found$(NC)"; \
		fi; \
		rm -f $(PIPELINE_DIR)/spring-boot.pid; \
	else \
		echo "$(YELLOW)No PID file found. Trying to kill process on port 8080...$(NC)"; \
		lsof -ti:8080 | xargs kill -9 2>/dev/null || echo "$(YELLOW)No process found on port 8080$(NC)"; \
	fi

clean: ## Clean Maven build artifacts
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	@cd $(PIPELINE_DIR) && mvn clean
	@rm -f $(PIPELINE_DIR)/spring-boot.log $(PIPELINE_DIR)/spring-boot.pid
	@echo "$(GREEN)Clean completed!$(NC)"

all: install-deps start-docker build run ## Install dependencies, start Docker, build and run application

setup: install-deps start-docker ## Setup everything (install deps and start Docker)

.DEFAULT_GOAL := help

