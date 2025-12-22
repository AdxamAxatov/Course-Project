# Course Project Report - Warehouse Management System

## Project Overview
This project is a Warehouse Management System for household appliances (Laptops and Ovens). I built it to allow users to filter inventory by category, search within price ranges, and calculate the total value of all stock in the warehouse.

## Architecture & Layers

### 1. Startup Layer
I implemented the startup process to be flexible. Using `PropertiesConfigImpl`, the app loads settings from an `app.properties` file. I used the **Factory Pattern** to handle dependencies, which keeps the initialization clean and organized.

### 2. View Layer
The user interface is a CLI implemented in `ConsoleViewImpl`. It uses a loop that stays active until the user types the `exit` command. I also added a `crash()` method to handle errors so the app doesn't just close without an explanation.

### 3. Controller Layer
I used the **Command Pattern** for the controller. The `CommandProvider` translates user input into specific logic like `FindCommand` or `CostCommand`. I used `Request` and `Response` DTOs to make sure the View and the Service layers are decoupled.

### 4. Service Layer
The `ApplianceServiceImpl` handles the business rules, like calculating inventory value and filtering prices. I used a `ServiceFactory` to make sure the Controller doesn't depend on a specific implementation.

### 5. DAL & Data Source Layer
To avoid writing the same code twice, I implemented a **Generic DAO** (`ApplianceDaoImpl<T>`). For data storage, I used CSV files with specific implementations for Laptops and Ovens.



## Unit Testing
I developed unit tests for the following:
* **Entities**: Checking data integrity.
* **DAO Factory**: Testing object creation.
* **CSV Sources**: Testing file reading logic.
* I used a test-specific file (`laptops1-test.csv`) to keep production data clean. 
* *Note:* The tests are in the `src/test` folder. Running them via terminal requires JUnit libraries in the classpath, but the logic is fully implemented.

## Challenges & Solutions
* **Avoiding Copy-Paste:** I refactored the startup logic to be dynamic instead of just copying code for each setting.
* **Input Validation:** To prevent crashes from invalid user input, I added a `WrongCommand` handler and `try-catch` blocks in the controller to provide meaningful error messages.

## Conclusion
The project follows the required layered architecture and uses design patterns to keep coupling low. The system is stable, easy to configure via properties, and meets all the course requirements.
