# Contributing to ApexEditorIDE

Thank you for your interest in contributing to ApexEditorIDE! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
- [Development Guidelines](#development-guidelines)
- [Submitting Changes](#submitting-changes)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

This project and everyone participating in it is expected to uphold professional and respectful communication. Be kind, constructive, and helpful in all interactions.

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK) 8** - The project is built using Java 8
- **Apache Maven 3.x** - For building and managing dependencies
- **MongoDB** - Required for running the application (used for data persistence)
- **Git** - For version control

### Setting Up the Development Environment

1. **Fork the repository** on GitHub
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/ApexEditorIDE.git
   cd ApexEditorIDE
   ```

3. **Build the project**:
   ```bash
   ./mvnw clean install
   ```
   On Windows:
   ```bash
   mvnw.cmd clean install
   ```

4. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

## How to Contribute

### Reporting Bugs

If you find a bug, please report it by opening an issue with:

- A clear, descriptive title
- A description of the expected vs. actual behavior
- Steps to reproduce the issue
- Your environment details (OS, Java version, Maven version)
- Any relevant error messages or stack traces

### Suggesting Enhancements

Enhancement suggestions are welcome! When proposing features:

- Clearly describe the feature and its use case
- Explain why it would be useful to most users
- Consider potential implementation approaches

### Pull Requests

1. **Create a new branch** for your feature or bug fix:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following the development guidelines below

3. **Write or update tests** as needed

4. **Ensure the build passes**:
   ```bash
   ./mvnw clean test
   ```

5. **Commit your changes** with clear, descriptive messages:
   ```bash
   git commit -m "Add: Clear description of what changed and why"
   ```

6. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Open a Pull Request** against the `master` branch

## Development Guidelines

### Project Structure

This is a Spring Boot web application with the following key components:

- `src/main/java/com/forceFilesEditor/` - Main application source code
  - `controller/` - REST API controllers
  - `dao/` - Data Access Objects (MongoDB repositories)
  - `model/` - Domain models/entities
  - `pmd/` - PMD integration for code review
  - `ruleSets/` - Rule set handling and XML conversion
  - `algo/` - Utility classes for metadata operations
  - `exception/` - Custom exception classes

### Coding Standards

- **Java 8** features are encouraged (lambdas, streams, etc.)
- Follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Keep methods focused and concise
- Add meaningful comments for complex logic
- Use existing patterns in the codebase

### Code Style

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Import organization: java.*, javax.*, then other imports
- Remove unused imports before committing

### Testing

- Write unit tests for new functionality using JUnit
- Run all tests before submitting:
  ```bash
  ./mvnw test
  ```
- Ensure tests pass on your local environment

### Dependencies

The project uses the following key dependencies:

- **Spring Boot 1.5.4** - Application framework
- **Spring Data MongoDB** - Database access
- **PMD 6.7.0** - Code analysis (Apex, Visualforce, JavaScript)
- **Force.com APIs** - Salesforce integration
- **Google Gson** - JSON processing
- **Apache Commons** - Utility libraries

When adding new dependencies, update `pom.xml` and provide justification in your PR.

## Submitting Changes

### Commit Message Guidelines

Use clear, meaningful commit messages:

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move button left..." not "Moves button left...")
- Limit the first line to 72 characters
- Reference issues and pull requests where appropriate

Examples:
```
Add: Support for custom PMD rulesets

Fix: Null pointer exception in PMDController when input is empty

Update: README with new build instructions
```

### Pull Request Process

1. Update the README.md if your changes affect usage or setup
2. Ensure all tests pass
3. Update documentation if you're changing functionality
4. Your PR will be reviewed by maintainers - be responsive to feedback
5. Once approved, your PR will be merged

## Reporting Issues

When reporting issues, please include:

- **Issue type**: Bug, Feature Request, Question
- **Description**: Clear description of the issue
- **Steps to reproduce** (for bugs)
- **Expected behavior** vs **Actual behavior**
- **Environment**: Java version, OS, MongoDB version
- **Logs/Screenshots**: Any relevant error messages

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/)
- [PMD Documentation](https://pmd.github.io/)
- [Maven Documentation](https://maven.apache.org/guides/)

## Questions?

If you have questions about contributing, feel free to open an issue with your question.

---

Thank you for contributing to ApexEditorIDE!
