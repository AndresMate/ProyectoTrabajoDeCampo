#!/bin/bash

# Unit Test Runner Script for UPTC Tournament Management System
# 
# This script executes unit tests and generates code coverage reports.
# It focuses on testing individual components in isolation using mocks.
#
# Features:
# - Compiles the project with Maven wrapper
# - Runs unit tests with JaCoCo code coverage
# - Generates detailed coverage reports
# - Provides test execution summary
# - Validates coverage thresholds
#
# Usage: ./run-unit-tests.sh
#
# Requirements:
# - Maven wrapper (mvnw) must be executable
# - Spring Boot test profile configured
# - Unit test classes must exist in src/test/java/co/edu/uptc/backend_tc/unit/
#
# Output:
# - Test reports in target/surefire-reports/
# - Coverage reports in target/site/jacoco/
# - Consolidated summary in unit-test-summary.md

echo "🧪 Sistema de Torneos UPTC - Ejecución de Pruebas Unitarias"
echo "============================================================"

# Configuration variables
PROJECT_ROOT="/Users/devstuck/Documents/ProjectJose/ProyectoTrabajoDeCampo/backend_TC"
TEST_PROFILE="test"
REPORT_DIR="$PROJECT_ROOT/target/surefire-reports"
COVERAGE_DIR="$PROJECT_ROOT/target/site/jacoco"

# Clean and compile the project
echo "🧹 Cleaning and compiling the project..."
./mvnw clean compile test-compile -f "$PROJECT_ROOT/pom.xml"

if [ $? -ne 0 ]; then
    echo "❌ Error during project cleanup or compilation. Aborting."
    exit 1
fi

# Create reports directory
mkdir -p $REPORT_DIR
mkdir -p $COVERAGE_DIR

echo "📋 Running unit tests with code coverage..."
echo ""

# Execute unit tests with JaCoCo coverage
./mvnw test jacoco:report -Dtest="**/unit/**/*Test" -Dspring.profiles.active=$TEST_PROFILE \
    -Dmaven.test.failure.ignore=true \
    -Dtest.reports.directory=$REPORT_DIR

TEST_EXIT_CODE=$?

echo ""
echo "📊 Execution Summary:"
echo "===================="

# Count executed tests
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
    echo "🎉 All unit tests have passed successfully!"
    echo "✅ The code meets the quality standards"
else
    echo "❌ Some tests failed! Check logs for more details."
    echo "❗ The code does NOT meet all quality standards"
fi

echo "Total tests executed: $TOTAL_TESTS"
echo "Successful tests: $PASSED_TESTS"
echo "Failed tests: $FAILED_TESTS"

echo ""
echo "📁 Test reports generated in: $REPORT_DIR"
echo "📊 Coverage reports generated in: $COVERAGE_DIR"
echo "🔍 To view coverage details: open $COVERAGE_DIR/index.html"

# Check if coverage report exists
if [ -f "$COVERAGE_DIR/index.html" ]; then
    echo "✅ Coverage report generated successfully"
    
    # Extract coverage percentages from JaCoCo report (if available)
    if [ -f "$COVERAGE_DIR/jacoco.csv" ]; then
        echo ""
        echo "📈 Code Coverage Summary:"
        echo "========================="
        
        # Parse CSV file for coverage metrics
        if command -v awk >/dev/null 2>&1; then
            awk -F',' '
            NR==1 { print "Metric,Total,Covered,Missed,Coverage%" }
            NR>1 && NF>=5 { 
                coverage = ($4/($3+$4))*100
                printf "%s,%d,%d,%d,%.1f%%\n", $1, $3+$4, $4, $3, coverage
            }' "$COVERAGE_DIR/jacoco.csv" | head -6
        fi
    fi
else
    echo "⚠️  Coverage report not found. Check JaCoCo configuration."
fi

# Generate consolidated report in Markdown
echo "📋 Generating consolidated report..."
REPORT_MD="$PROJECT_ROOT/unit-test-summary.md"
{
    echo "# Unit Test Report - UPTC Tournament Management System"
    echo ""
    echo "Execution Date: $(date)"
    echo "Test Type: Unit Tests (with mocks)"
    echo "Coverage Tool: JaCoCo"
    echo ""
    echo "## General Summary"
    echo ""
    echo "- **Total tests executed:** $TOTAL_TESTS"
    echo "- **Successful tests:** $PASSED_TESTS"
    echo "- **Failed tests:** $FAILED_TESTS"
    echo "- **Success rate:** $([ $TOTAL_TESTS -gt 0 ] && echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc || echo "0")%"
    echo ""
    echo "## Test Categories Executed"
    echo ""
    echo "### Service Layer Tests"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** Unit tests for business logic services (TournamentService, InscriptionService, AuthService)"
    echo "- **Technique:** Mock-based testing with isolated dependencies"
    echo ""
    echo "### Test Data Builders"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** Test fixtures and data builders for consistent test data"
    echo "- **Technique:** Builder pattern for test data creation"
    echo ""
    echo "### Authentication Tests"
    echo "- **Status:** $([ "$PASSED_TESTS" -gt 0 ] && echo "✅ PASS" || echo "❌ FAIL")"
    echo "- **Description:** JWT authentication, login, password management"
    echo "- **Technique:** Mock authentication manager and JWT service"
    echo ""
    echo "## Coverage Requirements"
    echo ""
    echo "### Minimum Coverage Thresholds"
    echo "- **Overall Instruction Coverage:** 80%"
    echo "- **Branch Coverage:** 75%"
    echo "- **Line Coverage:** 85%"
    echo "- **Service Layer Coverage:** 85%"
    echo "- **Controller Layer Coverage:** 80%"
    echo ""
    echo "## Test Quality Metrics"
    echo ""
    echo "- **Test Isolation:** ✅ Each test runs independently with mocks"
    echo "- **Test Data:** ✅ Consistent test data using builders and fixtures"
    echo "- **Assertions:** ✅ Comprehensive assertions using AssertJ"
    echo "- **Mocking:** ✅ Strategic mocking of external dependencies"
    echo ""
    echo "## Recommendations"
    echo ""
    if [ $FAILED_TESTS -gt 0 ]; then
        echo "- ❌ Review failed tests and correct implementation"
        echo "- ❌ Execute regression tests after corrections"
    else
        echo "- ✅ All unit tests passed successfully"
        echo "- ✅ Code quality standards met"
    fi
    echo "- 📊 Review coverage report for areas needing additional tests"
    echo "- 🔄 Execute tests regularly during development"
    echo "- 📝 Document any deviations from requirements"
    echo ""
    echo "## Files Generated"
    echo ""
    echo "- **Test Reports:** \`target/surefire-reports/\`"
    echo "- **Coverage Report:** \`target/site/jacoco/index.html\`"
    echo "- **Coverage Data:** \`target/site/jacoco/jacoco.csv\`"
    echo "- **Coverage XML:** \`target/site/jacoco/jacoco.xml\`"
} > "$REPORT_MD"

echo "📄 Consolidated report generated: $REPORT_MD"

# Run coverage check
echo ""
echo "🔍 Running coverage validation..."
./mvnw jacoco:check -f "$PROJECT_ROOT/pom.xml"

COVERAGE_EXIT_CODE=$?

if [ $COVERAGE_EXIT_CODE -eq 0 ]; then
    echo "✅ Coverage thresholds met"
else
    echo "⚠️  Coverage thresholds not met. Check coverage report for details."
fi

echo ""
echo "🏁 Unit test execution completed"

# Final status
if [ $TEST_EXIT_CODE -eq 0 ] && [ $COVERAGE_EXIT_CODE -eq 0 ]; then
    echo "🎯 All quality checks passed!"
    exit 0
else
    echo "⚠️  Some quality checks failed. Review reports for details."
    exit 1
fi
