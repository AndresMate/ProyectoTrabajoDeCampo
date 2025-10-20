# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sports Tournament Management System (Sistema de Gestión de Torneos Deportivos) for UPTC. A Spring Boot 3 REST API that manages tournaments, teams, players, matches, inscriptions, and standings with role-based access control.

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 17
- **Database**: PostgreSQL (hosted on Supabase)
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT authentication
- **Documentation**: Swagger/OpenAPI (springdoc-openapi)
- **Build Tool**: Maven
- **Additional Libraries**: Lombok, Apache POI (Excel reports)

## Essential Commands

### Build and Run
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Build without running tests
./mvnw clean install -DskipTests

# Run tests only
./mvnw test

# Run a specific test class
./mvnw test -Dtest=BackendTcApplicationTests
```

### Development
The application runs on the default port (8080) and connects to a Supabase PostgreSQL database. Configuration is in `src/main/resources/application.properties`.

**Swagger UI**: http://localhost:8080/swagger-ui.html
**API Docs**: http://localhost:8080/v3/api-docs

## Architecture Overview

### Layered Architecture Pattern

The codebase follows a strict layered architecture:

```
Controller → Service → Repository → Database
     ↓          ↓
   DTO   →   Entity
     ↑          ↑
  Mapper  ←  Mapper
```

**Key architectural principles:**
- Controllers handle HTTP requests and delegate to services
- Services contain business logic and orchestrate repository operations
- Repositories (Spring Data JPA) handle database access
- DTOs are used for API contracts; Entities are internal domain models
- Mappers transform between DTOs and Entities

### Package Structure

- `config/` - Spring configuration (Security, CORS, JWT, Swagger)
- `controller/` - REST endpoints organized by resource
- `dto/` - Data Transfer Objects for API requests/responses
  - `dto/response/` - Response DTOs with enriched data
  - `dto/filter/` - DTOs for search/filter operations
  - `dto/page/` - Pagination wrapper DTOs
  - `dto/stats/` - Statistics DTOs
- `entity/` - JPA entities (database models)
  - `entity/id/` - Composite primary key classes
- `exception/` - Custom exceptions and global exception handler
- `mapper/` - Entity ↔ DTO conversions
- `model/` - Enums and value objects (not entities)
- `repository/` - Spring Data JPA repositories
- `service/` - Business logic layer

### Core Domain Entities

**Tournament Hierarchy:**
- `Tournament` - Main tournament entity with status lifecycle (PLANNING → OPEN_FOR_INSCRIPTION → IN_PROGRESS → FINISHED/CANCELLED)
  - Has `Sport`, `Category`, created by `User`
  - Contains multiple `Team`, `Match`, `Standing`, `Inscription`

**Team & Player Management:**
- `Club` - Represents a sports club
- `Team` - Tournament-specific team (linked to Tournament + Club via `Inscription`)
- `Player` - Individual player
- `InscriptionPlayer` - Links players to inscriptions (composite key)
- `TeamRoster` - Links players to teams (composite key)
- `PlayerDocument` - Stores player documentation

**Match Management:**
- `Match` - Game between two teams with status (SCHEDULED → IN_PROGRESS → FINISHED/CANCELLED)
- `MatchResult` - Final score and statistics
- `MatchEvent` - In-game events (goals, cards, substitutions)
- `Sanction` - Player sanctions (yellow/red cards)

**Supporting Entities:**
- `Category` - Age/skill categories (belongs to a Sport)
- `Sport` - Type of sport
- `Venue` - Physical location
- `Scenario` - Specific playing field within a venue
- `Standing` - Tournament standings/rankings (composite key: tournament + team)
- `TeamAvailability` - Team schedule preferences

### Security & Authentication

**JWT-based authentication flow:**
1. User logs in via `/api/auth/login` with email/password
2. `AuthService` authenticates and generates JWT via `JwtService`
3. JWT is returned in `LoginResponseDTO`
4. Subsequent requests include JWT in Authorization header
5. `JwtAuthenticationFilter` validates token and sets SecurityContext
6. `SecurityConfig` enforces role-based access control

**Role hierarchy (UserRole enum):**
- `SUPER_ADMIN` - Full system access
- `ADMIN` - Tournament/category/sport/venue management
- `DELEGATE` - Club/team representative
- `REFEREE` - Match management and event recording
- `SPECTATOR` - Read-only access

**Public endpoints:**
- `/api/auth/**` - Authentication
- `/api/tournaments/public/**` - Public tournament info
- `/api/matches/public/**` - Public match data
- `/api/standings/public/**` - Public standings
- `/api/inscriptions/**` - Tournament registration
- `/api/sports/public/**`, `/api/venues/public/**`, `/api/clubs/**`

### Exception Handling Strategy

`GlobalExceptionHandler` provides centralized error handling:
- `ResourceNotFoundException` → 404
- `BadRequestException` → 400
- `UnauthorizedException` → 401
- `ForbiddenException` → 403
- `ConflictException` → 409
- `BusinessException` → 422 (business rule violations)
- `MethodArgumentNotValidException` → 400 (validation errors)
- `DataIntegrityViolationException` → 409 (database constraints)

All exceptions return structured `ApiError` responses with:
- timestamp, status, error, message, path
- `fieldErrors` for validation failures
- `debugMessage` (only when `app.debug=true`)

### Key Business Logic Patterns

**Fixture Generation (FixtureService):**
- Generates tournament schedules in two modes:
  - `round_robin` - All teams play each other (rotation algorithm)
  - `knockout` - Single elimination tournament
- Only generates fixtures for teams with APPROVED inscriptions
- Finds compatible time slots using team availability schedules
- Validates minimum 2 teams required

**Tournament Lifecycle:**
1. Created in PLANNING status
2. Teams register via Inscriptions (PENDING → APPROVED/REJECTED by admin)
3. Admin starts tournament (`startTournament()`) → IN_PROGRESS (requires ≥2 teams)
4. Matches are played, results recorded
5. Admin completes tournament → FINISHED
6. Can be CANCELLED at any time (except if FINISHED)

**Inscription Flow:**
- Club delegate creates `Inscription` for a Tournament
- Inscription has status: PENDING → APPROVED/REJECTED/WITHDRAWN
- Upon approval, creates `Team` entity linking Club to Tournament
- Players are added via `InscriptionPlayer` with delegate info
- When team is accepted, players are copied to `TeamRoster`

**Match Event Recording:**
- Referees record events via `MatchEvent` (GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION)
- Events link to specific players and match minute
- Yellow/Red cards automatically create `Sanction` records
- `MatchResult` stores final score and statistics

### Data Access Patterns

Repositories use Spring Data JPA with:
- Method name query derivation (e.g., `findByTournamentIdAndCategoryId`)
- `@Query` for complex queries
- Specifications for dynamic filtering (see `TournamentService.buildSpecification`)
- Pagination via `Pageable` parameter
- Projection interfaces for efficient queries

**Important repository queries:**
- `TeamRepository.findByTournamentIdAndCategoryId` - Teams in specific tournament/category
- `StandingRepository.findByTournamentIdOrderByPointsDesc` - Tournament rankings
- `MatchRepository.countByTournamentIdAndStatus` - Match statistics
- `InscriptionPlayerRepository.findByInscriptionId` - Players in inscription
- `TeamAvailabilityRepository.findByTeamIdAndAvailableTrue` - Available time slots

### DTO Mapping Strategy

**Two-way mapping via Mapper classes:**
- `toDTO()` / `toEntity()` for basic conversions
- `toResponseDTO()` for enriched responses (includes related data)
- `MapperUtils.mapPage()` for consistent pagination responses
- Response DTOs include counts, summaries, and nested summaries (e.g., `TournamentResponseDTO` includes team count, match statistics)

**Mapper pattern:**
- Inject required repositories in Mapper constructors
- Use lazy-loading guards (check for null/Hibernate proxies)
- Summary DTOs (e.g., `SportSummaryDTO`) provide minimal data for nested objects
- Filter DTOs are used as Specification builders for search

### Validation Approach

Multi-layer validation:
1. **Jakarta Validation annotations** on DTOs (@NotNull, @NotBlank, @Size, @Email, @Positive, @Future)
2. **Entity-level validation** via `@PrePersist` and `@PreUpdate` (e.g., date range validation)
3. **Service-level business rules** throwing `BusinessException` with error codes
4. **Database constraints** as final safety net (unique, foreign keys, not null)

### Important Configuration Notes

**Database Configuration:**
- Uses Supabase PostgreSQL with connection pooling (HikariCP)
- `spring.jpa.hibernate.ddl-auto=update` - Auto-updates schema (use with caution in production)
- `spring.jpa.show-sql=true` - SQL logging enabled for debugging
- Timezone set to `America/Bogota`

**Security Configuration:**
- JWT secret key configured in application.properties
- Token expiration: 24 hours (86400000 ms)
- CORS enabled via `CorsConfig`
- CSRF disabled (stateless JWT authentication)
- Session management: STATELESS

**Pagination Defaults:**
- Default page size: 10
- Max page size: 100
- Zero-indexed pages

## Common Development Patterns

### Adding a New Entity

1. Create entity class in `entity/` package with JPA annotations
2. Create corresponding DTOs in `dto/` (request/response variants)
3. Create Mapper in `mapper/` package
4. Create Repository interface extending `JpaRepository`
5. Create Service class with business logic
6. Create Controller with REST endpoints
7. Update SecurityConfig if needed for access control

### Adding a New Endpoint

1. Add method to appropriate Controller with `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping`
2. Use DTOs for request/response (never expose entities directly)
3. Inject required Service in controller constructor
4. Add OpenAPI documentation (`@Operation`, `@ApiResponse`)
5. Update SecurityConfig to permit/secure the endpoint
6. Handle exceptions (let GlobalExceptionHandler catch them)

### Implementing Search/Filter

1. Create FilterDTO in `dto/filter/` with search criteria
2. Implement Specification builder in Service (see `TournamentService.buildSpecification`)
3. Use `repository.findAll(spec, pageable)` for query execution
4. Return `PageResponseDTO` with results

### Working with Composite Keys

Some entities use composite keys (e.g., `Standing`, `InscriptionPlayer`, `TeamRoster`):
1. Create `@Embeddable` ID class in `entity/id/` package
2. Use `@EmbeddedId` in entity
3. Implement `equals()` and `hashCode()` in ID class
4. Repository uses composite ID type: `JpaRepository<Standing, StandingId>`

## Testing Notes

- Main test class: `BackendTcApplicationTests` (context load test)
- Database operations can be tested with `@DataJpaTest`
- Controller tests use `@WebMvcTest` with MockMvc
- Security tests require `@WithMockUser` or similar

## API Documentation

After starting the application, comprehensive API documentation is available at:
- Interactive UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

All endpoints are documented with descriptions, request/response schemas, and example values.
