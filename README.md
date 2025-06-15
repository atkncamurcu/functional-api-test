# ReqRes API Testing Project

This project implements comprehensive test automation for the [ReqRes API](https://reqres.in/) using REST Assured framework and TestNG.

## Overview

The project tests the following aspects of the ReqRes API:

1. CRUD operations testing
2. Authentication flow testing
3. Pagination testing
4. Error handling scenarios
5. Response time assertions
6. Schema validation
7. Data integrity checks

## Project Structure

```
├── pom.xml                  # Maven configuration file
└── src
    └── test
        ├── java
        │   └── com
        │       └── reqres
        │           └── api
        │               ├── models       # POJO classes for JSON objects
        │               ├── tests        # Test classes
        │               └── utils        # Utility classes
        └── resources
            ├── log4j2.xml              # Logging configuration
            ├── schemas                 # JSON schemas for validation
            └── testng.xml              # TestNG configuration
```

## Test Classes

- `UserCrudTests`: Tests CRUD operations on user resources
- `ResourceCrudTests`: Tests CRUD operations on unknown resources
- `AuthenticationTests`: Tests login, registration and token handling
- `PaginationTests`: Tests pagination functionality
- `ErrorHandlingTests`: Tests API error responses and error handling
- `PerformanceTests`: Tests response times and concurrency
- `SchemaValidationTests`: Tests response JSON schema validation
- `DataIntegrityTests`: Tests data consistency and integrity

## Authentication

All API requests automatically include the following header for authentication:
```
x-api-key: reqres-free-v1
```

This is configured in the `ApiUtils` class and applied to all requests.

## Running Tests

Execute all tests using Maven:

```bash
mvn clean test
```

Run a specific test class:

```bash
mvn clean test -Dtest=UserCrudTests
```

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- ReqRes API Key (added automatically to requests)
  - Header: `x-api-key: reqres-free-v1`

## Dependencies

- REST Assured 5.3.0
- TestNG 7.7.1
- Jackson 2.15.2
- JSON Schema Validator 5.3.0
- Lombok 1.18.26
- Log4j 2.20.0
