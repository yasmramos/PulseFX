# PulseFX

A modern JavaFX framework for state management, navigation, and async data handling in desktop applications.

## Purpose

PulseFX solves common challenges in JavaFX application development:
- **Reactive State Management**: Bind UI components to validated state with automatic updates
- **Type-Safe Validation**: Composable validation rules using sealed interfaces
- **Form Handling**: Track dirty state, field errors, and form-wide validity
- **Thread Safety**: All APIs are safe to call from the JavaFX Application Thread

## Quick Start (5 lines)

```java
StringProperty email = new SimpleStringProperty();
StringProperty password = new SimpleStringProperty();

FormState form = FormState.builder()
    .field("email", email, Rules.email().and(Rules.nonEmpty()))
    .field("password", password, Rules.minLength(8))
    .build();

submitButton.disableProperty().bind(form.validProperty().not());
```

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.yasmramos.pulsefx</groupId>
    <artifactId>pulsefx-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Or use the BOM for version management:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>dev.yasmramos.pulsefx</groupId>
            <artifactId>pulsefx-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>dev.yasmramos.pulsefx</groupId>
        <artifactId>pulsefx-core</artifactId>
    </dependency>
</dependencies>
```

## Features

### Reactive Validation Engine

- **Sealed Result Types**: `ValidationResult.Valid<T>` and `ValidationResult.Invalid<T>` provide type-safe error handling
- **Composable Rules**: Chain validators with `.and()`, `.or()`, and `.negate()`
- **Observable Bindings**: Validation results bind directly to JavaFX properties
- **Async Support**: Built-in support for async validation (e.g., server-side checks)

### FormState Management

- **Aggregate Validation**: Track validity across multiple fields
- **Dirty Tracking**: Know when users have modified form data
- **Field Errors**: Access per-field error messages via `fieldErrors()` map
- **Reset & Clean**: Reset state or mark as clean after successful saves

### Built-in Validation Rules

```java
Rules.nonEmpty()           // String not null/empty
Rules.minLength(8)         // Minimum string length
Rules.maxLength(50)        // Maximum string length
Rules.email()              // Email format validation
Rules.matches(pattern)     // Regex pattern matching
Rules.numberRange(1, 100)  // Numeric range validation
Rules.notNull()            // Null check for any type
```

### Custom Validators

Create custom validators using functional interfaces:

```java
Validator<String> customValidator = value -> {
    if (value.startsWith("invalid")) {
        return new ValidationResult.Invalid<>(List.of("Cannot start with 'invalid'"));
    }
    return new ValidationResult.Valid<>(value);
};

// Combine with built-in rules
Validator<String> combined = Rules.nonEmpty().and(customValidator);
```

## Project Structure

```
pulsefx/
├── pulsefx-core/          # Layer 1: State & Validation (✅ Complete)
├── pulsefx-navigation/    # Layer 2: Navigation (🚧 Coming soon)
├── pulsefx-data/          # Layer 3: Async Data (🚧 Coming soon)
└── pulsefx-bom/           # Bill of Materials for versioning
```

## Requirements

- Java 21+
- JavaFX 21+
- Maven 3.8+

## Build & Test

```bash
# Build all modules
mvn clean install

# Run tests (headless mode configured)
mvn clean verify

# Generate JavaDoc
mvn javadoc:jar
```

## Roadmap

### Layer 1: State & Validation (Current)
- ✅ Sealed validation result types
- ✅ Composable validator API
- ✅ FormState with dirty tracking
- ✅ Observable bindings for UI
- ✅ Headless test support with TestFX/Monocle

### Layer 2: Navigation (Planned)
- View routing and transitions
- Parameter passing between views
- Browser-style back/forward stack
- Deep linking support

### Layer 3: Async Data (Planned)
- Reactive data loading with progress
- Automatic retry policies
- Cache strategies
- WebSocket integration

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT License - see [LICENSE](LICENSE) for details.

## Author

**yasmramos**  
Email: yasmramos95@gmail.com  
GitHub: [@yasmramos](https://github.com/yasmramos)