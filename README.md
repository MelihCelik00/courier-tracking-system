# Courier Tracking Service

This service tracks couriers' locations, detects when they enter store proximity, and calculates their total travel distances.

## Prerequisites

- Java 21
- Docker and Docker Compose
- Gradle

## Getting Started

1. Start the required services (PostgreSQL, pgAdmin, and Kafka):
```bash
docker-compose up -d
```

2. Build the project:
```bash
./gradlew clean build
```

3. Run the application:
```bash
./gradlew bootRun
```

The application will be available at `http://localhost:8080`

## Database Visualization with pgAdmin

1. Access pgAdmin at `http://localhost:5050`
   - Email: admin@admin.com
   - Password: admin

2. Add a new server in pgAdmin:
   - Right click on 'Servers' → 'Register' → 'Server'
   - General tab:
     - Name: CourierTracking
   - Connection tab:
     - Host: postgres
     - Port: 5432
     - Database: courier_tracking
     - Username: postgres
     - Password: postgres

3. View tables:
   - Expand: Servers → CourierTracking → Databases → courier_tracking → Schemas → public → Tables
   - Right-click on any table and select "View/Edit Data" → "All Rows"

## API Documentation

Once the application is running, you can access the OpenAPI documentation at:
`http://localhost:8080/swagger-ui.html`

## Testing the Application

1. Report a courier location:
```bash
curl -X POST "http://localhost:8080/api/v1/couriers/1/locations?latitude=40.9923307&longitude=29.1244229"
```

2. Get courier details:
```bash
curl -X GET "http://localhost:8080/api/v1/couriers/1"
```

3. Get courier's total travel distance:
```bash
curl -X GET "http://localhost:8080/api/v1/couriers/1/total-travel-distance"
```

## Example Test Scenario

1. Report courier approaching Ataşehir MMM Migros:
```bash
# Location 1 - 150m away from store
curl -X POST "http://localhost:8080/api/v1/couriers/1/locations?latitude=40.9923307&longitude=29.1234229"

# Location 2 - Within store radius (100m)
curl -X POST "http://localhost:8080/api/v1/couriers/1/locations?latitude=40.9923307&longitude=29.1244229"

# Location 3 - Moving away from store
curl -X POST "http://localhost:8080/api/v1/couriers/1/locations?latitude=40.9923307&longitude=29.1254229"
```

2. Check total travel distance:
```bash
curl -X GET "http://localhost:8080/api/v1/couriers/1/total-travel-distance"
```

## Stopping the Application

1. Stop the Spring Boot application by pressing `Ctrl+C` in the terminal where it's running

2. Stop and remove Docker containers:
```bash
# Stop containers
docker-compose down

# Remove volumes (optional, will delete all data)
docker-compose down -v
```

## Features

- Real-time courier location tracking
- Automatic store proximity detection (100m radius)
- Store entry logging with 1-minute cooldown
- Total travel distance calculation using Haversine formula
- Event-driven architecture using Kafka
- RESTful API with OpenAPI documentation
- Database visualization with pgAdmin

## Architecture

The application follows a hybrid architecture combining CQRS and Hexagonal Architecture:

- **Domain Layer**: Contains business logic and entities
- **Application Layer**: Handles commands and queries
- **Infrastructure Layer**: Manages external interactions (Kafka, REST, Database)

## Database Schema

- `courier`: Stores courier information and current location
- `store`: Contains store locations
- `store_entry`: Logs courier entries into store proximity

## Event Flow

1. Courier location update received via REST API
2. Location event published to Kafka
3. Location processed and store proximity checked
4. If within store radius, store entry event published
5. Store entry processed and logged if cooldown period passed 