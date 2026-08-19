# User Service

Core microservice for user management, authentication, and page operations in the Facebook Clone platform.

## Features

- **User Management**: Registration, profiles, avatars, cover photos
- **Page Management**: Create and manage business pages
- **E2EE Vault**: Secure PIN backup with encryption
- **GraphQL API**: Modern GraphQL endpoint for all operations
- **gRPC Services**: Communication with other microservices
- **Redis Caching**: Session and token management
- **MinIO Integration**: Avatar and media storage

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.x
- **API**: GraphQL (primary), gRPC
- **Database**: PostgreSQL 16 with CloudNativePG
- **Cache**: Redis 7
- **File Storage**: MinIO S3-compatible
- **Observability**: OpenTelemetry + Sentry

### Service Ports
- `8081` - HTTP (GraphQL + REST health)
- `9090` - gRPC Server
- `8081/graphql` - GraphQL Endpoint
- `8081/graphiql` - GraphQL Interactive UI

### Internal Ports (not exposed)
- `/internal/mqtt-auth/*` - MQTT Authentication (service-to-service)

## Getting Started

### Build
```bash
mvn clean package -DskipTests
```

### Run Locally (Docker Compose)
```bash
docker-compose up -d user-service
```

The service will connect to:
- PostgreSQL: `postgresql.apps.svc.cluster.local:5432`
- Redis: `redis-master.apps.svc.cluster.local:6379`
- Neo4j: `neo4j.apps.svc.cluster.local:7687`
- MinIO: `http://localhost:9000`

### Run Kubernetes (Helm)
```bash
helm install user-service charts/backend -n apps
```

## GraphQL API

### Quick Start

1. **Navigate to GraphQL UI**:
   ```
   http://localhost:8081/graphiql
   ```

2. **Example Query**:
   ```graphql
   query {
     pages {
       id
       name
       domain
     }
   }
   ```

3. **Example Mutation**:
   ```graphql
   mutation {
     registerUser(input: {
       keycloakUserId: "kc-123"
       username: "johndoe"
       email: "john@example.com"
       firstName: "John"
       lastName: "Doe"
     }) {
       success
       userId
       user {
         id
         username
       }
     }
   }
   ```

### Authentication
Include user ID in headers:
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

### Available Operations

#### Queries
- `user(id: ID!)` - Get user profile
- `pages` - List all pages
- `page(id: ID!)` - Get page by ID
- `pageByWebsite(domain: String!)` - Get page by domain
- `userPages(userId: ID!)` - Get user's pages
- `activePageToken` - Get active page token
- `vault(userId: ID!)` - Get vault data

#### Mutations
- `registerUser(input: RegisterInput!)` - Register new user
- `uploadAvatar(userId: ID!, file: Upload!)` - Upload avatar
- `uploadCover(userId: ID!, file: Upload!)` - Upload cover photo
- `createPage(input: CreatePageInput!)` - Create page
- `exchangePageToken(pageId: ID!)` - Get page access token
- `verifyPageAccess(pageId: ID!)` - Check authorization
- `clearActivePageToken` - Clear page session
- `saveVault(userId: ID!, input: SaveVaultInput!)` - Save vault data
- `updateVaultAttempts(userId: ID!, attempts: Int!)` - Update vault attempts

### Documentation
- [GraphQL Reference](./GRAPHQL_REFERENCE.md) - Query/mutation examples
- [Migration Guide](./GRAPHQL_MIGRATION.md) - REST to GraphQL migration details

## gRPC Services

User Service exposes gRPC endpoints on port `9090` for inter-service communication.

### Available Services
- `UserService` - User profile operations
- `PageService` - Page management operations

### Clients
- `SocialGraphClient` - Communicates with socialgraph-service
- `SearchServiceClient` - Communicates with search-service

## Database

### Tables
- `users` - User profiles
- `pages` - Business pages
- `family_members` - Family relationships
- `life_events` - Life milestones
- `social_links` - Social media links
- `search_users` - Search index

### Schema Management
- Automatic DDL with Hibernate: `spring.jpa.hibernate.ddl-auto=update`
- Migrations can be managed with Flyway (optional)

## Configuration

### Environment Variables

**Database**
```
SPRING_DATASOURCE_URL=jdbc:postgresql://postgresql.apps.svc.cluster.local:5432/users_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

**Redis**
```
SPRING_DATA_REDIS_HOST=redis-master.apps.svc.cluster.local
SPRING_DATA_REDIS_PORT=6379
```

**MinIO**
```
MINIO_URL=http://minio.apps.svc.cluster.local:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=avatars
```

**gRPC Clients**
```
SOCIAL_GRAPH_SERVICE_GRPC_ADDRESS=dns:///socialgraph-service.apps.svc.cluster.local:9091
FEED_SERVICE_GRPC_ADDRESS=dns:///feed-service.apps.svc.cluster.local:9092
NOTIFICATION_SERVICE_GRPC_ADDRESS=dns:///notification-service.apps.svc.cluster.local:9093
```

**Observability**
```
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
SENTRY_DSN=https://...@sentry.example.com/1
SENTRY_ENVIRONMENT=production
SENTRY_TRACES_SAMPLE_RATE=0.1
```

### See [application.properties](./src/main/resources/application.properties) for all options

## Health Checks

### Spring Boot Health Endpoint
```bash
curl http://localhost:8081/health
```

### Database Health
```bash
curl http://localhost:8081/health/db
```

### Readiness Probe
```bash
curl http://localhost:8081/health/ready
```

### Liveness Probe
```bash
curl http://localhost:8081/health/live
```

## Observability

### Logging
- Logs are sent to Loki via OpenTelemetry
- Access via Grafana

### Metrics
- Prometheus scrapes `/actuator/prometheus`
- View in Grafana at port 3002

### Distributed Tracing
- Traces exported to Jaeger (http://localhost:16686)
- Service name: `user-service`
- Sampling rate: 10% (production), 100% (development)

### Error Tracking
- Errors sent to Sentry/GlitchTip
- Access at http://localhost:8075

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dgroups=integration
```

### GraphQL Tests
```bash
# Test via cURL
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "query": "query { pages { id name } }"
  }'
```

## API Backwards Compatibility

**BREAKING CHANGE**: This version removes all REST API endpoints. All clients must migrate to GraphQL.

**Removed Endpoints**:
- `POST /api/register`
- `POST /api/users/{userId}/avatar`
- `GET /api/users/avatar/{avatarId}`
- `POST /api/users/{userId}/cover`
- `GET /api/vaults/{userId}`
- `POST /api/vaults/{userId}/attempts`
- `PUT /api/vaults/{userId}`
- `POST /api/pages/{pageId}/token`
- `GET /api/pages/active-token`
- `DELETE /api/pages/active-token`
- `POST /api/pages`
- `GET /api/users/{userId}/pages`
- `GET /api/pages`
- `GET /api/pages/by-website`
- `GET /api/pages/{pageId}`
- `GET /api/pages/{pageId}/verify-access`

**Kept for Internal Use Only**:
- `/internal/mqtt-auth/*` - Internal MQTT authentication (not exposed to clients)

See [GRAPHQL_MIGRATION.md](./GRAPHQL_MIGRATION.md) for migration details.

## Performance

### Caching Strategy
- User profiles: 5 minutes (Redis)
- Pages: 10 minutes (Redis)
- Page tokens: TTL-based (Redis)

### Database Optimization
- Connection pooling via HikariCP
- Indexes on commonly queried fields
- Read replicas for scaling (if configured)

### Monitoring
- Database query times: OpenTelemetry JDBC instrumentation
- GraphQL performance: OpenTelemetry Spring GraphQL instrumentation
- External calls: OpenTelemetry HTTP instrumentation

## Troubleshooting

### User not found
- Verify user ID format is valid UUID
- Check database connection

### Page token expired
- Token TTL: configurable in `PageTokenService`
- Re-request token via `exchangePageToken` mutation

### MinIO connection errors
- Check MinIO is running: `http://localhost:9000`
- Verify credentials in `application.properties`
- Check bucket exists: `avatars`

### GraphQL introspection issues
- Enable introspection: `spring.graphql.schema.printer.enabled=true`
- Check schema file at: `src/main/resources/graphql/schema.graphqls`

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -am 'Add feature'`
4. Push to branch: `git push origin feature/my-feature`
5. Create Pull Request

## License

Proprietary - Facebook Clone Project

## Support

For issues or questions:
1. Check [GraphQL Reference](./GRAPHQL_REFERENCE.md)
2. Review [GraphQL Migration Guide](./GRAPHQL_MIGRATION.md)
3. Check logs: `kubectl logs -f deployment/user-service -n apps`
4. Check traces: `http://localhost:16686` (Jaeger)
5. Check errors: `http://localhost:8075` (Sentry)

## Related Services

- **Keycloak**: Authentication & identity
- **SocialGraph Service**: User relationships
- **Feed Service**: User feed generation
- **Chat Service**: Real-time messaging
- **Notification Service**: User notifications
- **Search Service**: Full-text search
- **Marketplace Service**: Commerce features
