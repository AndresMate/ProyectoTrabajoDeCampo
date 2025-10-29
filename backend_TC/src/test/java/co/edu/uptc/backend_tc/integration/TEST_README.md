# UPTC Tournament Management System - Testing Guide

This document provides comprehensive information about all testing strategies implemented in the UPTC Tournament Management System, including both **Unit Tests** and **Integration Tests**.

## 📋 Overview

The testing suite validates the complete functionality of the tournament management system through two complementary approaches:

### 🧪 Unit Tests
- **Purpose**: Test individual components in isolation
- **Scope**: Service layer business logic
- **Framework**: JUnit 5 + Mockito
- **Database**: No database (mocked dependencies)

### 🔗 Integration Tests
- **Purpose**: Test complete workflows with real database
- **Scope**: End-to-end functionality validation
- **Framework**: Spring Boot Test + H2 Database
- **Database**: H2 in-memory database

## 🎯 Test Coverage

### Unit Tests Coverage
- **AuthServiceTest**: 13 tests - Authentication and authorization logic
- **TournamentServiceTest**: 20 tests - Tournament management operations
- **InscriptionServiceTest**: 24 tests - Team registration and inscription management
- **Total Unit Tests**: 57 tests ✅ (100% passing)

### Integration Tests Coverage
- **CP001: Registration System Integration** - Tournament creation and team inscription
- **CP002: File Validation Integration** - Data validation and persistence
- **CP003: Fixture Generation Integration** - Match creation and scheduling
- **Total Integration Tests**: 5 test classes

## 🚀 Running Tests

### Prerequisites
- Java 17+
- Maven wrapper (`mvnw`) with execute permissions
- Spring Boot test profile configured

### Execution Commands

#### Run All Tests (Unit + Integration)
```bash
# Run all tests
./mvnw test -Dspring.profiles.active=test

# Run with coverage report
./mvnw test -Dspring.profiles.active=test jacoco:report
```

#### Run Unit Tests Only
```bash
# Run only unit tests
./mvnw test -Dtest="**/unit/**/*Test" -Dspring.profiles.active=test

# Run specific unit test class
./mvnw test -Dtest="AuthServiceTest" -Dspring.profiles.active=test
```

#### Run Integration Tests Only
```bash
# Run integration tests using the provided script
./run-integration-tests.sh

# Or run directly with Maven
./mvnw test -Dtest="**/integration/**/*Test" -Dspring.profiles.active=test

# Run specific integration test class
./mvnw test -Dtest="TournamentIntegrationTest" -Dspring.profiles.active=test
```

#### Run Tests by Package
```bash
# Run all service tests (unit tests)
./mvnw test -Dtest="co.edu.uptc.backend_tc.unit.service.*Test" -Dspring.profiles.active=test

# Run all integration tests
./mvnw test -Dtest="co.edu.uptc.backend_tc.integration.*Test" -Dspring.profiles.active=test
```

## ⚙️ Test Configuration

### Unit Tests Configuration
- **Framework**: JUnit 5 + Mockito + AssertJ
- **Isolation**: Each test is completely isolated using mocks
- **Dependencies**: All external dependencies are mocked
- **Performance**: Fast execution (no database operations)

### Integration Tests Configuration
- **Database**: H2 in-memory database
- **Profile**: `test`
- **Configuration**: `src/test/resources/application-test.properties`
- **DDL**: `create-drop` (automatic schema creation/deletion)

#### H2 Database Settings
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Features
- **Isolation**: Each test runs in its own transaction (`@Transactional`)
- **Unique Data**: Test data includes timestamps to avoid conflicts
- **Persistence Validation**: Tests verify database persistence and entity relationships
- **Clean State**: Database is cleaned before each test execution

## 📊 Test Reports

### Report Locations
- **Unit Tests**: `target/test-reports/co/edu/uptc/backend_tc/unit/`
- **Integration Tests**: `target/test-reports/co/edu/uptc/backend_tc/integration/`
- **Coverage Report**: `target/site/jacoco/index.html`
- **Summary**: `target/test-reports/test-summary.md`

### Report Formats
- **Surefire XML**: Standard Maven test reports
- **JaCoCo HTML**: Code coverage reports
- **Markdown Summary**: Consolidated results

## 🏗️ Test Structure

### Unit Tests Structure
```
src/test/java/co/edu/uptc/backend_tc/unit/
├── service/
│   ├── AuthServiceTest.java          # 13 tests
│   ├── TournamentServiceTest.java    # 20 tests
│   └── InscriptionServiceTest.java    # 24 tests
└── fixtures/
    ├── TournamentFixtures.java
    ├── TournamentTestDataBuilder.java
    ├── TeamTestDataBuilder.java
    └── UserTestDataBuilder.java
```

### Integration Tests Structure
```
src/test/java/co/edu/uptc/backend_tc/integration/
├── TournamentIntegrationTest.java
├── InscriptionManagementIntegrationTest.java
├── FixtureGenerationIntegrationTest.java
├── SecurityIntegrationTest.java
└── TournamentManagementIntegrationTest.java
```

## 🎯 Expected Results

### Unit Tests Results
When unit tests pass successfully:
- ✅ **Total tests executed**: 57
- ✅ **Successful tests**: 57
- ✅ **Failed tests**: 0
- ✅ **Coverage**: High code coverage with JaCoCo

### Integration Tests Results
When integration tests pass successfully:
- ✅ **Total tests executed**: 5 classes
- ✅ **Successful tests**: All integration scenarios
- ✅ **Failed tests**: 0
- ✅ **System compliance**: Meets Integration Test Plan requirements

## 🔧 Troubleshooting

### Common Issues

#### Unit Tests Issues
1. **UnnecessaryStubbingException**: Remove unused mocks or use `@MockitoSettings(strictness = Strictness.LENIENT)`
2. **NullPointerException**: Ensure all required objects are properly mocked
3. **AssertionError**: Verify expected values match actual service behavior

#### Integration Tests Issues
1. **Permission Denied**: Ensure `mvnw` has execute permissions (`chmod +x mvnw`)
2. **Database Errors**: Verify H2 configuration in `application-test.properties`
3. **Test Failures**: Check entity relationships and validation constraints
4. **Compilation Errors**: Ensure all dependencies are properly configured

### Debugging Tips
- **Unit Tests**: Use `@MockitoSettings(strictness = Strictness.LENIENT)` for debugging
- **Integration Tests**: Enable SQL logging: `spring.jpa.show-sql=true`
- **Both**: Check test logs in `target/test-reports/`
- **Coverage**: Review JaCoCo reports for uncovered code

## 🚀 CI/CD Integration

The test suite is designed for continuous integration environments:

### Unit Tests
- **Fast Execution**: No database dependencies
- **Reliable**: Deterministic results with mocks
- **Parallel Execution**: Can run concurrently

### Integration Tests
- **No External Dependencies**: Uses H2 in-memory database
- **Fast Execution**: Optimized for quick feedback
- **Reliable Results**: Transaction isolation prevents test interference

## 📈 Test Maintenance

### Adding New Unit Tests
1. Follow existing patterns with `@Test` and `@DisplayName`
2. Use `@Mock` for dependencies
3. Include proper assertions with AssertJ
4. Use Test Data Builders for complex objects

### Adding New Integration Tests
1. Follow the existing pattern with `@Test` and `@DisplayName`
2. Use helper methods for data creation
3. Include proper assertions and persistence verification
4. Update test count in scripts if needed

### Updating Configuration
- Modify `application-test.properties` for test-specific settings
- Update helper methods for new entity requirements
- Adjust test data creation for new validation rules

## 📚 Related Documentation

- **Integration Test Plan**: Master document with test requirements
- **Entity Documentation**: JPA entity relationships and constraints
- **API Documentation**: REST endpoint specifications
- **Database Schema**: Entity relationship diagrams
- **Maven Configuration**: `pom.xml` with testing dependencies

## 🎉 Success Metrics

### Current Status
- **Unit Tests**: 57/57 passing (100%) ✅
- **Integration Tests**: 5/5 classes implemented ✅
- **Code Coverage**: JaCoCo configured and reporting ✅
- **Build Status**: All tests passing ✅

### Quality Indicators
- **Zero Test Failures**: All tests pass consistently
- **High Coverage**: Comprehensive test coverage
- **Fast Execution**: Quick feedback for developers
- **Maintainable**: Well-structured and documented tests

---

**Last Updated**: October 2024  
**Test Framework**: JUnit 5 + Mockito + Spring Boot Test  
**Database**: H2 In-Memory  
**Coverage Tool**: JaCoCo