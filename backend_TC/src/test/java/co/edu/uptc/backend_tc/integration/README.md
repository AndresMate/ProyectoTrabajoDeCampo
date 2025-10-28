# UPTC Tournament Management System - Integration Tests

This directory contains integration tests for the UPTC Tournament Management System, implementing the Integration Test Plan with H2 in-memory database for testing.

## Overview

The integration tests validate the core functionality of the tournament management system, including:

- **CP001: Registration System Integration** - Tournament creation and team inscription
- **CP002: File Validation Integration** - Data validation and persistence
- **CP003: Fixture Generation Integration** - Match creation and scheduling

## Test Configuration

### Database
- **Type**: H2 in-memory database
- **Profile**: `test`
- **Configuration**: `src/test/resources/application-test.properties`
- **DDL**: `create-drop` (automatic schema creation/deletion)

### Test Features
- **Isolation**: Each test runs in its own transaction (`@Transactional`)
- **Unique Data**: Test data includes timestamps to avoid conflicts
- **Persistence Validation**: Tests verify database persistence and entity relationships
- **Clean State**: Database is cleaned before each test execution

## Running Tests

### Prerequisites
- Java 17+
- Maven wrapper (`mvnw`) with execute permissions
- Spring Boot test profile configured

### Execution
```bash
# Run integration tests
./run-integration-tests.sh

# Or run directly with Maven
./mvnw test -Dtest=TournamentIntegrationTest -Dspring.profiles.active=test
```

### Test Reports
- **Location**: `target/test-reports/`
- **Format**: Surefire XML reports + Markdown summary
- **Summary**: `test-summary.md` with consolidated results

## Test Structure

### Main Test Class
- **File**: `TournamentIntegrationTest.java`
- **Package**: `co.edu.uptc.backend_tc.integration`
- **Annotations**: `@SpringBootTest`, `@ActiveProfiles("test")`, `@Transactional`

### Test Methods
1. `testCreateTournamentWithOpenInscriptions()` - CP001: Tournament creation
2. `testCreateTeamInscription()` - CP001: Team registration
3. `testCreateBasicMatches()` - CP003: Match generation

### Helper Methods
- `createSport()` - Creates unique sport entities
- `createCategory()` - Creates unique category entities
- `createUser()` - Creates unique user entities
- `createClub()` - Creates unique club entities
- `createTournament()` - Creates unique tournament entities
- `createVenue()` - Creates unique venue entities
- `createScenario()` - Creates unique scenario entities
- `createTeam()` - Creates unique team entities

## Configuration Details

### H2 Database Settings
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

### Test Profile Features
- **SQL Logging**: Enabled for debugging
- **Transaction Management**: Automatic rollback
- **Security**: JWT configuration for testing
- **File Upload**: Configured for testing scenarios

## Expected Results

When tests pass successfully:
- ✅ **Total tests executed**: 3
- ✅ **Successful tests**: 3
- ✅ **Failed tests**: 0
- ✅ **System compliance**: Meets Integration Test Plan requirements

## Troubleshooting

### Common Issues
1. **Permission Denied**: Ensure `mvnw` has execute permissions (`chmod +x mvnw`)
2. **Database Errors**: Verify H2 configuration in `application-test.properties`
3. **Test Failures**: Check entity relationships and validation constraints
4. **Compilation Errors**: Ensure all dependencies are properly configured

### Debugging
- Enable SQL logging: `spring.jpa.show-sql=true`
- Check test logs in `target/test-reports/`
- Verify entity creation with unique timestamps

## Integration with CI/CD

The integration tests are designed to run in continuous integration environments:
- **No External Dependencies**: Uses H2 in-memory database
- **Fast Execution**: Optimized for quick feedback
- **Reliable Results**: Transaction isolation prevents test interference
- **Detailed Reporting**: Comprehensive test reports for analysis

## Maintenance

### Adding New Tests
1. Follow the existing pattern with `@Test` and `@DisplayName`
2. Use helper methods for data creation
3. Include proper assertions and persistence verification
4. Update test count in `run-integration-tests.sh` if needed

### Updating Configuration
- Modify `application-test.properties` for test-specific settings
- Update helper methods for new entity requirements
- Adjust test data creation for new validation rules

## Related Documentation

- **Integration Test Plan**: Master document with test requirements
- **Entity Documentation**: JPA entity relationships and constraints
- **API Documentation**: REST endpoint specifications
- **Database Schema**: Entity relationship diagrams
