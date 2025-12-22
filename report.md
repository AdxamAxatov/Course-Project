# Project Progress Report: Warehouse Management System

## 1. Project Overview
This project implements a multi-tier (n-tier) Warehouse Management System for household appliances (Laptops and Ovens). The application allows users to search for appliances by category, find items within specific price ranges, and calculate total inventory costs.

## 2. Completed Architecture Layers

### 🟢 Startup Layer (Initialization)
- Implemented `PropertiesConfigImpl` which reads `app.properties`.
- Used **Factory Pattern** to initialize and inject dependencies across all layers.
- Handled command-line arguments to support different configuration profiles.

### 🟢 View Layer
- Implemented `ConsoleViewImpl` to handle user interaction via the CLI.
- Designed a robust loop that stays active until the `exit` command is received.
- Included error notification (`crash()` method) for system failures.

### 🟢 Controller Layer
- Implemented the **Command Pattern**.
- Created a `CommandProvider` to map user strings to specific executable logic (`FindCommand`, `CostCommand`, `ExitCommand`).
- Successfully decoupled user input from business logic using `Request` and `Response` DTOs.

### 🟢 Service Layer
- Implemented `ApplianceServiceImpl` to handle business rules.
- Added logic for inventory value calculation and price-range filtering.
- Used a `ServiceFactory` to ensure the controller remains independent of the implementation.

### 🟢 DAL & Data Source Layer
- Implemented a **Generic DAO** (`ApplianceDaoImpl<T>`) to support multiple appliance types without code duplication.
- Created specific CSV source implementations (`LaptopCsvSourceImpl`, `OvenCsvSourceImpl`) for file-based persistence.

## 3. Unit Testing
- Created unit tests for **Entities**, **DAO Factory**, and **CSV Data Sources**.
- Verified data integrity using test-specific CSV files (`laptops1-test.csv`) to ensure the production data remains clean.

## 4. Challenges & Solutions
- **Challenge:** Avoided "copy-paste" code in the Startup layer.
- **Solution:** Refactored the configuration logic to use a more dynamic initialization approach.
- **Challenge:** Handling invalid user input without crashing.
- **Solution:** Implemented a `WrongCommand` and try-catch blocks in the controller to return meaningful error messages.

## 5. Conclusion
The project meets all mandatory requirements for layered architecture, low coupling, and the use of design patterns. The application is stable, tested, and configurable via external property files.