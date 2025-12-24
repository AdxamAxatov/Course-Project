# Course Project Report - Warehouse Management System

## Project Overview
This project is a Warehouse Management System for household appliances (Laptops and Ovens). I built it to allow users to filter inventory by category, search within price ranges, and calculate the total value of all stock in the warehouse.

## Architecture & Layers

## Startup Layer

The startup layer was implemented using a configuration file (app.properties). This made it easier to control how the application starts without changing code. Over time, I refactored the initialization logic and used factory classes to keep object creation clean and organized.

## View Layer

The view is a simple console interface. It runs in a loop and waits for user commands until exit is entered. During development, error handling was added so that the application shows meaningful messages instead of crashing.

## Controller Layer

The controller uses the Command Pattern. User input is mapped to commands like find, cost, or exit. This part was improved gradually to handle invalid commands safely and avoid runtime errors caused by incorrect input.

## Service Layer

The service layer contains the main business logic, such as filtering appliances and calculating inventory cost. A service factory was added later to reduce coupling between the controller and service implementation.

## Data Access Layer

The data access layer reads appliance data from CSV files. To avoid repeating code, a generic DAO was implemented and reused for different appliance types. Separate CSV source classes were created for laptops and ovens.

## Unit Testing

Basic unit testing was implemented to verify that key parts of the system work correctly. Tests focus mainly on entity creation, CSV parsing, and DAO logic. A manual test runner was also used to confirm correct behavior when running the application from the terminal.

## Challenges & Improvements

The project structure was refactored several times to better follow layered architecture rules.

Input validation was improved to prevent crashes from incorrect commands.

Configuration and file loading issues were fixed to make the application more stable.
