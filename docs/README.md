# Hospital Management System

A comprehensive Java application for managing hospital operations including patient records, appointments, medical records, billing, and inventory.

## Overview

This Hospital Management System is designed to help medical facilities manage their essential operations efficiently. It provides a modular architecture with clear separation of concerns, comprehensive testing, and robust error handling.

### Key Features

- **Patient Management**: Register, admit, and discharge patients
- **Appointment Scheduling**: Create, reschedule, and cancel appointments
- **Medical Records**: Create and manage patient medical records
- **Billing System**: Generate bills, add items, and process payments
- **Inventory Management**: Track hospital supplies and manage stock levels

## Project Structure

```
├── src/
│   ├── main/java/com/example/hospitalsystemsimpletesting/
│   │   ├── model/             # Domain models
│   │   ├── service/           # Business logic interfaces
│   │   │   └── impl/          # Service implementations
│   │   └── controller/        # Application controllers
│   └── test/java/com/example/hospitalsystemsimpletesting/
│       ├── model/             # Model tests
│       ├── service/           # Service tests
│       └── controller/        # Controller tests
├── docs/                      # Project documentation
└── .cursor/                   # Project rules
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher
- JavaFX (for UI components)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/hospital-management-system.git
cd hospital-management-system
```

2. Build the project with Maven:
```bash
mvn clean install
```

### Running the Application

To start the application:

```bash
mvn javafx:run
```

## Running Tests

To run all tests:

```bash
mvn test
```

To generate a test coverage report with JaCoCo:

```bash
mvn verify
```

The coverage report will be available at `target/site/jacoco/index.html`

## Documentation

- [Project Summary](docs/project_summary.md): Overview of project status and next steps
- [Test Coverage Summary](test_coverage_summary.md): Detailed breakdown of test coverage
- [Errors Detected and Fixed](errors_detected_and_fixed.md): Documentation of issues and their resolutions

## Architecture

The system follows a layered architecture:

1. **Model Layer**: Domain objects for Patient, Appointment, MedicalRecord, Bill, and InventoryItem
2. **Service Layer**: Business logic for each domain area
3. **Controller Layer**: User interface and coordination between services

## Future Enhancements

- Integration testing for service interactions
- User authentication and authorization
- Reporting module for analytics
- Advanced notification system
- Full UI implementation

## Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 