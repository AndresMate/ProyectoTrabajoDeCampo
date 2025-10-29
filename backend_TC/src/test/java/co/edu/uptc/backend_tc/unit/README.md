# UPTC Tournament Management System - Unit Tests

This directory contains unit tests for the UPTC Tournament Management System, implementing comprehensive unit testing with mocks and code coverage analysis.

## Overview

The unit tests validate individual components of the tournament management system in isolation, including:

- **Service Layer Tests** - Business logic validation with mocked dependencies
- **Authentication Tests** - JWT authentication and security validation
- **Data Validation Tests** - Input validation and business rules
- **Error Handling Tests** - Exception scenarios and edge cases

## Test Structure

### Directory Organization

```
src/test/java/co/edu/uptc/backend_tc/
├── unit/
│   ├── service/              # Service layer unit tests
│   │   ├── TournamentServiceTest.java
│   │   ├── InscriptionServiceTest.java
│   │   └── AuthServiceTest.java
│   ├── controller/           # Controller layer unit tests (planned)
│   ├── mapper/               # Mapper unit tests (planned)
│   ├── validation/           # Custom validation tests (planned)
│   ├── security/             # Security-specific tests (planned)
│   └── util/                 # Utility class tests (planned)
├── fixtures/                 # Test data builders and fixtures
│   ├── TournamentTestDataBuilder.java
│   ├── UserTestDataBuilder.java
│   ├── TeamTestDataBuilder.java
│   └── TournamentFixtures.java
└── integration/              # Integration tests (existing)
```

### Test Data Builders

The project uses the **Builder Pattern** for creating test data:

- **`TournamentTestDataBuilder`** - Creates Tournament entities with customizable properties
- **`UserTestDataBuilder`** - Creates User entities with different roles and states
- **`TeamTestDataBuilder`** - Creates Team entities with various configurations
- **`TournamentFixtures`** - Static factory methods for common test scenarios

### Fixtures and Test Data

- **Consistent Data**: All tests use standardized test data
- **Unique Values**: Timestamps and IDs prevent conflicts between tests
- **Realistic Scenarios**: Test data reflects real-world usage patterns
- **Edge Cases**: Special fixtures for boundary conditions

## Running Unit Tests

### Prerequisites

- Java 17+
- Maven wrapper (`mvnw`) with execute permissions
- Spring Boot test profile configured

### Execution Methods

#### 1. Using the Unit Test Script (Recommended)
```bash
# Run all unit tests with coverage
./run-unit-tests.sh
```

#### 2. Using Maven Directly
```bash
# Run unit tests only
./mvnw test -Dtest="**/unit/**/*Test" -Dspring.profiles.active=test

# Run with coverage report
./mvnw test jacoco:report -Dtest="**/unit/**/*Test"

# Check coverage thresholds
./mvnw jacoco:check
```

#### 3. Running Specific Test Classes
```bash
# Run specific service tests
./mvnw test -Dtest="TournamentServiceTest"

# Run all service tests
./mvnw test -Dtest="**/unit/service/**/*Test"
```

## Test Configuration

### Unit Test Profile

The unit tests use a dedicated profile (`unit-test`) with:

- **H2 In-Memory Database**: Fast, isolated test database
- **Mocked Dependencies**: External services are mocked
- **Minimal Logging**: Reduced log output for cleaner test runs
- **Disabled Security**: Security is mocked for unit tests
- **Test-Specific Properties**: Optimized for unit testing

### Coverage Configuration

JaCoCo is configured with the following thresholds:

- **Overall Instruction Coverage**: 80%
- **Branch Coverage**: 75%
- **Line Coverage**: 85%
- **Service Layer Coverage**: 85%
- **Controller Layer Coverage**: 80%

## Test Categories

### 1. Service Layer Tests

#### TournamentServiceTest
- ✅ Tournament creation with valid/invalid data
- ✅ Tournament retrieval and search
- ✅ Tournament status transitions
- ✅ Tournament deletion with business rules
- ✅ Tournament completion validation
- ✅ Tournament cancellation

#### InscriptionServiceTest
- ✅ Team inscription creation
- ✅ Inscription approval/rejection workflow
- ✅ Player availability validation
- ✅ Team name uniqueness validation
- ✅ Tournament capacity validation
- ✅ Category-sport compatibility validation

#### AuthServiceTest
- ✅ User authentication with valid/invalid credentials
- ✅ JWT token generation
- ✅ Password change functionality
- ✅ User role handling
- ✅ Security context management

### 2. Test Data Builders

#### TournamentTestDataBuilder
```java
// Create a valid tournament
Tournament tournament = TournamentTestDataBuilder.aValidTournament().build();

// Create tournament with specific status
Tournament inProgress = TournamentTestDataBuilder.anInProgressTournament().build();

// Create tournament with custom properties
Tournament custom = TournamentTestDataBuilder.aValidTournament()
    .withName("Custom Tournament")
    .withMaxTeams(16)
    .build();
```

#### UserTestDataBuilder
```java
// Create different user types
User admin = UserTestDataBuilder.anAdminUser().build();
User player = UserTestDataBuilder.aPlayerUser().build();
User referee = UserTestDataBuilder.aRefereeUser().build();
```

### 3. Fixtures

#### TournamentFixtures
```java
// Common tournament scenarios
Tournament validTournament = TournamentFixtures.validTournament();
Tournament inProgressTournament = TournamentFixtures.inProgressTournament();
Tournament finishedTournament = TournamentFixtures.finishedTournament();

// Create lists for testing
List<Tournament> tournaments = TournamentFixtures.createTournamentList(5);
List<User> users = TournamentFixtures.createUserList(3);
```

## Test Patterns and Best Practices

### 1. Arrange-Act-Assert (AAA) Pattern

```java
@Test
@DisplayName("Should create tournament when valid data is provided")
void testCreateTournament_WithValidData_ShouldReturnTournamentDTO() {
    // Arrange
    TournamentDTO dto = TournamentFixtures.validTournamentDTO();
    when(tournamentRepository.save(any(Tournament.class))).thenReturn(validTournament);
    
    // Act
    TournamentResponseDTO result = tournamentService.create(dto);
    
    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(dto.getName());
    verify(tournamentRepository).save(any(Tournament.class));
}
```

### 2. Mocking Strategy

- **Mock External Dependencies**: Database repositories, external services
- **Mock Authentication**: Security context and JWT services
- **Mock File Operations**: File upload and storage services
- **Verify Interactions**: Ensure mocks are called with expected parameters

### 3. Test Naming Convention

- **Method Names**: `test{Method}_{Condition}_{ExpectedResult}`
- **Display Names**: Human-readable descriptions using `@DisplayName`
- **Class Names**: `{ClassUnderTest}Test`

### 4. Assertions

- **AssertJ**: Fluent assertions for better readability
- **Comprehensive Checks**: Verify return values, state changes, and interactions
- **Exception Testing**: Validate error conditions and messages

## Coverage Reports

### Generated Reports

After running unit tests, the following reports are generated:

- **HTML Report**: `target/site/jacoco/index.html` - Interactive coverage report
- **CSV Report**: `target/site/jacoco/jacoco.csv` - Machine-readable coverage data
- **XML Report**: `target/site/jacoco/jacoco.xml` - CI/CD integration format
- **Test Summary**: `unit-test-summary.md` - Consolidated test results

### Coverage Metrics

The reports show coverage for:

- **Instructions**: Individual bytecode instructions
- **Branches**: Conditional statements and loops
- **Lines**: Source code lines
- **Methods**: Individual methods
- **Classes**: Complete classes

## Continuous Integration

### CI/CD Integration

The unit tests are designed for CI/CD pipelines:

- **Fast Execution**: Optimized for quick feedback
- **No External Dependencies**: Uses in-memory database
- **Coverage Validation**: Fails build if thresholds not met
- **Detailed Reporting**: Comprehensive test and coverage reports

### Maven Goals

```bash
# Prepare JaCoCo agent
mvn jacoco:prepare-agent

# Run tests with coverage
mvn test

# Generate coverage report
mvn jacoco:report

# Check coverage thresholds
mvn jacoco:check
```

## Maintenance and Extensions

### Adding New Unit Tests

1. **Follow Existing Patterns**: Use the same structure and naming conventions
2. **Create Test Data Builders**: For new entities, create corresponding builders
3. **Add Fixtures**: Include common scenarios in fixtures
4. **Update Coverage**: Ensure new code meets coverage thresholds

### Updating Test Data

1. **Modify Builders**: Update builders for new entity properties
2. **Add Fixtures**: Create new fixtures for additional scenarios
3. **Update Tests**: Modify existing tests to use new data structures

### Debugging Tests

- **Enable SQL Logging**: Set `spring.jpa.show-sql=true` in test properties
- **Check Mock Interactions**: Use `verify()` to ensure mocks are called correctly
- **Validate Test Data**: Ensure test data is realistic and complete

## Troubleshooting

### Common Issues

1. **Test Failures**: Check mock configurations and test data
2. **Coverage Issues**: Review uncovered code and add appropriate tests
3. **Performance**: Optimize test data creation and mock setup
4. **Flaky Tests**: Ensure tests are deterministic and isolated

### Debug Commands

```bash
# Run specific test with verbose output
./mvnw test -Dtest="TournamentServiceTest" -X

# Check test compilation
./mvnw test-compile

# Generate coverage without running tests
./mvnw jacoco:report -DskipTests
```

## Related Documentation

- **Integration Tests**: See `src/test/java/co/edu/uptc/backend_tc/integration/README.md`
- **Test Plan**: See `PLAN_DE_PRUEBAS_INTEGRACION_ACTUALIZADO.html`
- **Service Documentation**: See individual service classes
- **Entity Documentation**: See JPA entity classes

## Quality Metrics

### Current Status

- **Total Unit Tests**: 3 service test classes implemented
- **Test Coverage**: JaCoCo configured with 80%+ thresholds
- **Test Data**: Comprehensive builders and fixtures
- **Documentation**: Complete test documentation

### Goals

- **Service Coverage**: 100% of service methods tested
- **Controller Coverage**: 80%+ of controller endpoints tested
- **Edge Cases**: Comprehensive error scenario testing
- **Performance**: Fast test execution (< 30 seconds)
