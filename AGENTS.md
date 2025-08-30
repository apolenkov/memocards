# 🚀 Project Instructions - Senior+ Level
## Modern Stack: Java 21+ + Vaadin 24+ + Spring Boot 3.x + PostgreSQL

## 🎯 Core AI Behavior
- **Autonomous Execution**: Complete tasks end-to-end without confirmation
- **Enterprise Level**: Work at senior/enterprise level without interruptions
- **Quality First**: Apply all quality standards automatically
- **Proactive Fixes**: Fix issues proactively without user intervention

## ☕ Modern Java 21+ Standards
- **Virtual Threads**: Use for I/O operations and database calls
- **Pattern Matching**: Switch expressions, instanceof patterns, record patterns
- **Sealed Classes**: For restricted inheritance hierarchies and type safety
- **Records**: Use for immutable DTOs, responses, and data carriers
- **Text Blocks**: For SQL queries, JSON, and long strings
- **Structured Concurrency**: Modern async programming patterns
- **String Templates**: For dynamic string construction
- **Unnamed Patterns**: For better readability in complex patterns

## 🏗️ Code Quality & Architecture
- **SOLID Principles**: Follow all five object-oriented design principles
- **Clean Architecture**: Strict separation into layers with correct dependency direction
- **DRY**: No code duplication, extract common functionality
- **KISS**: Solution simplicity without unnecessary complexity
- **Constructor Injection**: Always use constructor injection for Spring beans (never @Autowired on fields)
- **Immutable Design**: Prefer records and final classes
- **Functional Programming**: Use Stream API, Optional, and functional interfaces

## 🔒 Security Standards
- **OWASP Top 10**: Implement proper authorization, input validation, secure design
- **Input Validation**: Validate all external inputs at boundaries (UI, API, database)
- **SQL Injection Prevention**: Use parameterized queries for all database operations
- **XSS Prevention**: Encode all user-generated content before rendering
- **Secure by Default**: Fail securely, deny by default

## ⚡ Performance & Monitoring
- **Caching Strategy**: Implement appropriate caching layers
- **Async Processing**: Use non-blocking operations where possible
- **Database Optimization**: Optimize queries and connection management
- **APM**: Implement distributed tracing, monitor response times and throughput
- **Memory Management**: Avoid memory leaks, use object pooling for expensive objects

## 🧪 Modern Testing Strategy
- **Testing Pyramid**: 70% Unit Tests, 20% Integration Tests, 10% E2E Tests
- **Coverage**: Minimum 80% for business logic, 70% branch coverage
- **Test Data Builders**: Use builders for consistent test data
- **Mocking Strategy**: Mock external dependencies, not internal logic
- **Test Containers**: Real PostgreSQL for integration tests
- **Virtual Threads Testing**: Test concurrent operations
- **Mutation Testing**: Use PIT for code quality validation
- **Performance Testing**: Load, stress, and endurance testing

## 🎨 Vaadin 24+ UI/UX Standards
- **Vaadin 24+ DSL**: Prefer native Vaadin DSL over JavaScript
- **TypeScript Support**: For custom components and type safety
- **i18n**: Never use hardcoded strings - always use getTranslation()
- **Lumo Theme**: Use Lumo theme tokens for all styling
- **Progressive Web App**: PWA features for mobile experience
- **Virtual Scrolling**: For large datasets and better performance
- **Modern Components**: Use latest Vaadin component patterns
- **Accessibility**: Implement proper ARIA labels and contrast
- **Responsive Design**: Mobile-first approach with CSS Grid and Flexbox

## 🌱 Spring Boot 3.x + JPA
- **Spring Boot 3.x**: Jakarta EE 10, Native compilation, AOT compilation
- **Virtual Threads Support**: Better scalability for I/O operations
- **Enhanced Security**: Spring Security 6.x with modern authentication
- **Spring Data JPA 3.x**: New repository methods and projections
- **Entity Design**: Use @Id with @GeneratedValue, include audit fields
- **Repository Layer**: Use @Query for complex operations, implement pagination
- **DTOs**: Keep DTOs separate from entities, use records for immutable design
- **Transactions**: Use @Transactional at service level, mark read operations as read-only
- **Performance**: Use connection pooling, batch operations, query optimization

## 🐘 PostgreSQL & Database Optimization
- **PostgreSQL Features**: JSONB, arrays, full-text search, partitioning
- **Advanced Indexing**: B-tree, GIN, GiST, BRIN for different query types
- **Materialized Views**: For complex aggregations and reports
- **Foreign Data Wrappers**: For external data integration
- **Connection Pooling**: HikariCP with optimal configuration
- **Database Migrations**: Flyway for schema versioning
- **Virtual Threads**: For database operations and better concurrency

## 📚 Documentation & Communication
- **JavaDoc**: Concise and substantive, focus on purpose not implementation
- **English Code**: All code must use English language for names, comments, messages
- **Russian Chat**: Always respond in Russian for chat communication
- **Professional Tone**: Maintain professional tone in all communications

## 🚫 Critical Prohibitions
- ❌ **NEVER use System.out.println** - use SLF4J/Logback
- ❌ **NEVER use FQN** - always add proper imports
- ❌ **NEVER use hardcoded strings in UI** - use getTranslation()
- ❌ **NEVER use @Autowired on fields** - only constructor injection
- ❌ **NEVER use primitive types in nullable fields** - use wrapper types
- ❌ **NEVER leave dead code** - remove unused methods, classes, variables
- ❌ **NEVER leave commented code** - delete or implement properly
- ❌ **NEVER leave unreachable code** - remove or fix logic
- ❌ **NEVER duplicate code** - extract common functionality, follow DRY principle
- ❌ **NEVER copy-paste code** - create reusable methods and utilities

## 🔄 Error Handling
- **Fail Fast**: Detect and handle errors early
- **Fail Gracefully**: Provide meaningful error messages
- **Fail Securely**: Don't expose sensitive information
- **Centralized Handling**: Use @ControllerAdvice for REST APIs
- **Business Exceptions**: Domain-specific exceptions with meaningful error codes

## 🎯 Result
With these instructions, I will:
- **Automatically apply** all rules without reminders
- **Fix all issues** proactively
- **Suggest improvements** constantly
- **Follow enterprise standards** always
- **Work at senior+ level** continuously
- **Maintain maximum efficiency** in chat
- **Use modern Java 21+ features** appropriately
- **Implement Vaadin 24+ best practices** automatically
- **Optimize PostgreSQL queries** and performance
- **Apply Spring Boot 3.x patterns** correctly

## 🚀 Maximum Efficiency Commands
- **"Apply all rules"** → automatic rule application
- **"Check quality"** → comprehensive quality check
- **"Optimize performance"** → performance optimization
- **"Add tests"** → complete test coverage
- **"Fix all issues"** → proactive issue resolution
- **"Java 21+ features"** → modern Java patterns check
- **"Vaadin standards"** → Vaadin 24+ best practices validation
- **"PostgreSQL optimization"** → database performance check
- **"Virtual threads"** → virtual threads implementation check

> **Note**: For detailed rules and commands, see `.cursor/rules/` folder
