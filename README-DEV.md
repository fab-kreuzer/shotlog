# Shotlog Development Setup

This document describes how to set up a local development environment for the Shotlog application using Docker.

## Prerequisites

- Docker and Docker Compose installed
- Java 17 or higher
- Maven (or use the included Maven wrapper)

## Quick Start

### Option 1: Using the Startup Scripts (Recommended)

**For Windows:**
```bash
dev-start.bat
```

**For Linux/Mac:**
```bash
chmod +x dev-start.sh
./dev-start.sh
```

### Option 2: Manual Setup

1. **Build the application:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Start the development environment:**
   ```bash
   docker-compose -f docker-compose.dev.yml up --build
   ```

## What's Included

The development environment includes:

- **PostgreSQL Database**: Running on port 5432
  - Database: `shotlog_dev`
  - Username: `postgres`
  - Password: `postgres`
  
- **Shotlog Application**: Running on port 8080
  - Profile: `dev`
  - Hot reload enabled
  - SQL logging enabled
  - Thymeleaf cache disabled

## Database Management

- **Flyway migrations** are automatically applied on startup
- Database data is persisted in a Docker volume
- To reset the database, stop the containers and run:
  ```bash
  docker-compose -f docker-compose.dev.yml down -v
  ```

## Development Workflow

1. Start the development environment using one of the methods above
2. The application will be available at http://localhost:8080
3. Make code changes in your IDE
4. The application will automatically restart when changes are detected
5. Database changes should be made through Flyway migrations in `src/main/resources/db/migration/`

## Stopping the Environment

Press `Ctrl+C` in the terminal where docker-compose is running, or run:
```bash
docker-compose -f docker-compose.dev.yml down
```

## Troubleshooting

### Port Conflicts
If you get port conflicts, make sure no other services are running on ports 5432 or 8080.

### Database Connection Issues
If the application can't connect to the database, wait a few seconds for PostgreSQL to fully start up. The health check should ensure proper startup order.

### Build Issues
Make sure you have Java 17 installed and JAVA_HOME is properly set.

## Production vs Development

- **Development** (`docker-compose.dev.yml`): Includes database, uses dev profile, enables hot reload
- **Production** (`docker-compose.yml`): Application only, uses prod profile, requires external database