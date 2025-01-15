# System Architecture

## High-Level Architecture
```mermaid
graph TB
    subgraph "Client Layer"
        Client[Client/API Consumer]
    end

    subgraph "API Layer"
        REST[REST Controllers]
        Swagger[OpenAPI/Swagger]
    end

    subgraph "Application Layer"
        CommandServices[Command Services]
        QueryServices[Query Services]
    end

    subgraph "Domain Layer"
        Models[Domain Models]
        Events[Domain Events]
        Utils[Domain Utils]
    end

    subgraph "Infrastructure Layer"
        Kafka[Kafka]
        PostgreSQL[(PostgreSQL)]
        Consumers[Kafka Consumers]
    end

    Client --> REST
    Client --> Swagger
    REST --> CommandServices
    REST --> QueryServices
    CommandServices --> Models
    CommandServices --> Events
    CommandServices --> Kafka
    QueryServices --> Models
    QueryServices --> PostgreSQL
    Consumers --> Kafka
    Consumers --> CommandServices
    Models --> PostgreSQL
```

## CQRS Pattern Implementation
```mermaid
graph LR
    subgraph "Command Side"
        LC[Location Command]
        CS[CourierLocationCommandService]
        SEC[StoreEntryCommandService]
        KP[Kafka Producer]
    end

    subgraph "Query Side"
        QC[Query Controller]
        CQS[CourierQueryService]
        R[(Read Database)]
    end

    subgraph "Event Processing"
        KF{Kafka}
        CLC[CourierLocationConsumer]
        SEC2[StoreEntryConsumer]
    end

    LC --> CS
    CS --> KP
    KP --> KF
    KF --> CLC
    KF --> SEC2
    CLC --> CS
    SEC2 --> SEC
    QC --> CQS
    CQS --> R
```

## Event Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant API as REST API
    participant CS as CommandService
    participant K as Kafka
    participant Con as Consumer
    participant DB as PostgreSQL

    C->>API: POST /couriers/{id}/locations
    API->>CS: Process Location
    CS->>DB: Update Courier Location
    CS->>K: Publish LocationEvent
    K->>Con: Consume Event
    Con->>CS: Process Event
    CS->>DB: Update Store Entry
    CS->>K: Publish StoreEntryEvent
```

## Database Schema
```mermaid
erDiagram
    COURIER {
        bigint id PK
        double total_travel_distance
        double last_latitude
        double last_longitude
        boolean is_active
    }
    STORE {
        bigint id PK
        string name
        double latitude
        double longitude
    }
    STORE_ENTRY {
        bigint id PK
        bigint courier_id FK
        bigint store_id FK
        timestamp entry_time
        double entry_latitude
        double entry_longitude
    }
    COURIER ||--o{ STORE_ENTRY : has
    STORE ||--o{ STORE_ENTRY : has
```

## Component Details

### Domain Layer
- **Models**: Courier, Store, StoreEntry
- **Events**: CourierLocationEvent, StoreEntryEvent
- **Utils**: GeoUtils (Haversine calculations)

### Application Layer
- **Command Services**:
  - CourierLocationCommandService: Processes location updates
  - StoreEntryCommandService: Handles store entry events
- **Query Services**:
  - CourierQueryService: Retrieves courier information

### Infrastructure Layer
- **Messaging**:
  - Kafka for event streaming
  - Topics: courier.location, store.entry
- **Database**:
  - PostgreSQL for persistent storage
  - Flyway for migrations
- **API Documentation**:
  - OpenAPI/Swagger UI

### Features
- Real-time location tracking
- Automatic store proximity detection (100m radius)
- Store entry logging with cooldown (1 minute)
- Total travel distance calculation
- Event-driven architecture
- RESTful API endpoints 