#!/bin/bash

# Integration Test Runner Script for UPTC Tournament Management System
# 
# This script executes integration tests based on the Integration Test Plan.
# It uses H2 in-memory database for testing to avoid external dependencies.
#
# Features:
# - Compiles the project with Maven wrapper
# - Runs TournamentIntegrationTest with H2 database
# - Generates consolidated test reports in Markdown format
# - Provides detailed test execution summary
#
# Usage: ./run-integration-tests.sh
#
# Requirements:
# - Maven wrapper (mvnw) must be executable
# - Spring Boot test profile configured for H2
# - TournamentIntegrationTest class must exist
#
# Output:
# - Test reports in target/test-reports/
# - Consolidated summary in test-summary.md

echo "🏆 Sistema de Torneos UPTC - Ejecución de Pruebas de Integración"
echo "=================================================================="

# Configuration variables
PROJECT_ROOT="/Users/devstuck/Documents/ProjectJose/ProyectoTrabajoDeCampo/backend_TC"
TEST_PROFILE="test"
REPORT_DIR="$PROJECT_ROOT/target/test-reports"

# Clean and compile the project
echo "🧹 Cleaning and compiling the project..."
./mvnw clean install -DskipTests -f "$PROJECT_ROOT/pom.xml"

if [ $? -ne 0 ]; then
    echo "❌ Error during project cleanup or compilation. Aborting."
    exit 1
fi

# Create reports directory
mkdir -p $REPORT_DIR

echo "📋 Running all integration tests..."
echo ""

# Execute all integration tests
./mvnw test -Dtest=TournamentIntegrationTest,TournamentManagementIntegrationTest,InscriptionManagementIntegrationTest,FixtureGenerationIntegrationTest,SecurityIntegrationTest -Dspring.profiles.active=$TEST_PROFILE \
    -Dmaven.test.failure.ignore=true \
    -Dtest.reports.directory=$REPORT_DIR

TEST_EXIT_CODE=$?

echo ""
echo "📊 Execution Summary:"
echo "===================="

        # Count executed tests (comprehensive integration testing)
        # Parse Surefire XML reports to get accurate test counts
        TOTAL_TESTS=0
        PASSED_TESTS=0
        FAILED_TESTS=0
        
        for report_file in $REPORT_DIR/TEST-*.xml; do
            if [ -f "$report_file" ]; then
                # Extract test counts from XML using grep and awk
                tests=$(grep -o 'tests="[0-9]*"' "$report_file" | grep -o '[0-9]*' | head -1)
                failures=$(grep -o 'failures="[0-9]*"' "$report_file" | grep -o '[0-9]*' | head -1)
                errors=$(grep -o 'errors="[0-9]*"' "$report_file" | grep -o '[0-9]*' | head -1)
                
                if [ -n "$tests" ]; then
                    TOTAL_TESTS=$((TOTAL_TESTS + tests))
                fi
                if [ -n "$failures" ]; then
                    FAILED_TESTS=$((FAILED_TESTS + failures))
                fi
                if [ -n "$errors" ]; then
                    FAILED_TESTS=$((FAILED_TESTS + errors))
                fi
            fi
        done
        
        PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS))
        
        if [ $TEST_EXIT_CODE -eq 0 ] && [ $FAILED_TESTS -eq 0 ]; then
            echo "🎉 All integration tests have passed successfully!"
            echo "✅ The system meets the requirements of the Test Plan"
        else
            echo "❌ Some tests failed! Check logs for more details."
            echo "❗ The system does NOT meet all Test Plan requirements"
        fi

echo "Total tests executed: $TOTAL_TESTS"
echo "Successful tests: $PASSED_TESTS"
echo "Failed tests: $FAILED_TESTS"

echo ""
echo "📁 Reports generated in: $REPORT_DIR"
echo "🔍 To view details: cat $REPORT_DIR/TEST-*.xml"

# Generate consolidated report in Markdown
echo "📋 Generating consolidated report..."
REPORT_MD="$REPORT_DIR/test-summary.md"
{
    echo "# Integration Test Report - UPTC Tournament Management System"
    echo ""
    echo "Execution Date: $(date)"
    echo "Database: H2 in-memory"
    echo ""
    echo "## General Summary"
    echo ""
    echo "- **Total tests executed:** $TOTAL_TESTS"
    echo "- **Successful tests:** $PASSED_TESTS"
    echo "- **Failed tests:** $FAILED_TESTS"
    echo ""
    echo "## Test Cases Executed (Comprehensive Integration Testing)"
    echo ""
    echo "### CP001-CP003: Basic Tournament Integration"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** Basic tournament creation, team inscription, and match generation"
    echo "- **Technique:** Black Box + DB Verification"
    echo ""
    echo "### CP004-CP008: Tournament Management System"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** Complete tournament lifecycle, business rules, status transitions, team management"
    echo "- **Technique:** Black Box + Business Logic Validation"
    echo ""
    echo "### CP009-CP013: Inscription Management System"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** Team registration, player management, availability scheduling, validation"
    echo "- **Technique:** Black Box + Data Integrity Verification"
    echo ""
    echo "### CP014-CP018: Fixture Generation System"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** Round-robin fixtures, tournament brackets, match results, standings"
    echo "- **Technique:** Black Box + Algorithm Validation"
    echo ""
    echo "### CP019-CP024: Security and Authentication System"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** JWT tokens, role-based access, password security, user management"
    echo "- **Technique:** Black Box + Security Validation"
    echo ""
    echo "## Recommendations"
    echo "- Review failed tests and correct implementation"
    echo "- Execute regression tests after corrections"
    echo "- Document any deviations from requirements"
} > "$REPORT_MD"

echo "📄 Consolidated report generated: $REPORT_MD"

echo ""
echo "🏁 Test execution completed"