# Contributing to ApexEditorIDE

Thank you for your interest in contributing to ApexEditorIDE! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Environment](#development-environment)
- [Coding Standards](#coding-standards)
- [Testing Procedures](#testing-procedures)
- [Submitting Changes](#submitting-changes)
- [Reporting Issues](#reporting-issues)
- [Commit Message Guidelines](#commit-message-guidelines)

## Code of Conduct

This project and everyone participating in it is governed by our commitment to:
- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on constructive feedback
- Respect different viewpoints and experiences

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Create a new branch for your feature or bug fix
4. Make your changes
5. Submit a pull request

## Development Environment

### Prerequisites

- **Java 8** or higher
- **Maven 3.3+**
- **MongoDB** (for local development)
- **Git**

### Setting Up Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/Nagendra080389/ApexEditorIDE.git
   cd ApexEditorIDE
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Coding Standards

### Java Guidelines

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Keep methods small and focused (single responsibility)
- Write self-documenting code with clear intent
- Maximum line length: 120 characters
- Use proper indentation (4 spaces)

### Code Structure

- Organize imports (remove unused imports)
- Group related classes in appropriate packages
- Use `final` keyword where applicable
- Follow existing project structure:
  - `controller/` - REST controllers
  - `service/` - Business logic
  - `dao/` - Data access objects
  - `model/` - Domain models

### Documentation

- Add Javadoc comments for public classes and methods
- Include `@param`, `@return`, and `@throws` where applicable
- Keep comments up-to-date with code changes

### Example:
```java
/**
 * Retrieves user details by user ID.
 *
 * @param userId the unique identifier of the user
 * @return UserDomain object containing user details
 * @throws IllegalArgumentException if userId is null or empty
 */
public UserDomain getUserById(String userId) {
    // implementation
}
```

## Testing Procedures

### Running Tests

Execute all tests using Maven:
```bash
./mvnw test
```

### Test Coverage Requirements

- All new code must include unit tests
- Aim for minimum 70% code coverage
- Tests should be in `src/test/java` mirroring the main package structure

### Writing Tests

- Use JUnit 4 (as configured in pom.xml)
- Use Mockito for mocking dependencies
- Follow naming convention: `methodName_StateUnderTest_ExpectedBehavior`
- Include both positive and negative test cases

### Example:
```java
@Test
public void getUserById_validId_returnsUser() {
    // Arrange
    String userId = "valid-id";
    when(userRepository.findById(userId)).thenReturn(mockUser);

    // Act
    UserDomain result = userService.getUserById(userId);

    // Assert
    assertNotNull(result);
    assertEquals(userId, result.getId());
}
```

## Submitting Changes

### Pull Request Process

1. Ensure your branch is up-to-date with the base branch:
   ```bash
   git fetch origin
   git rebase origin/master
   ```

2. Run all tests and ensure they pass:
   ```bash
   ./mvnw clean test
   ```

3. Update documentation if applicable

4. Commit your changes with descriptive messages

5. Push to your fork:
   ```bash
   git push origin your-branch-name
   ```

6. Open a Pull Request against the `master` branch

7. Fill out the PR template with:
   - Clear description of changes
   - Related issue numbers (if any)
   - Testing performed

### PR Review Process

- Maintainers will review your PR within 5 business days
- Address review comments promptly
- Be open to feedback and suggestions
- All CI checks must pass before merging

## Reporting Issues

### Bug Reports

When reporting bugs, please include:
- Clear, descriptive title
- Steps to reproduce
- Expected behavior
- Actual behavior
- Environment details (OS, Java version, etc.)
- Relevant logs or screenshots

### Feature Requests

For feature requests, provide:
- Use case description
- Expected functionality
- Potential implementation approach (if known)

## Commit Message Guidelines

Follow conventional commit format:

```
type(scope): subject

body (optional)

footer (optional)
```

### Types:
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, semicolons, etc.)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Build process or auxiliary tool changes

### Examples:
```
feat(controller): add pagination support for user list

fix(dao): resolve null pointer exception in user search

docs: update API documentation for PMD endpoints

test(service): add unit tests for PmdReviewService
```

## Questions?

Feel free to open an issue for questions or reach out to maintainers.

Thank you for contributing to ApexEditorIDE!
