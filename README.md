# Inventory Management System

A simple, yet powerful, desktop application for managing inventory. Built with JavaFX, this application provides a clean and intuitive user interface to handle items, track stock, and manage inventory data efficiently.

## Features

- **CRUD Operations**: Easily Add, Update, and Delete inventory items.
- **Real-time Data Display**: View all inventory items in a clear, sortable table.
- **Live Search/Filter**: Instantly search for items by name with a real-time filtering feature.
- **Data Persistence**: All inventory data is saved locally in a SQLite database (`inventory.db`).
- **Input Validation**: Ensures data integrity by validating user input (e.g., preventing empty names and negative values for quantity/price).

## Technologies Used

- **Frontend**: JavaFX 21
- **Database**: SQLite
- **Build Tool**: Apache Maven

## Prerequisites

Before you begin, ensure you have the following installed:

- Java Development Kit (JDK) 17 or later.
- Apache Maven.

## How to Run the Application

1.  Clone the repository or open the project folder.
2.  Open a terminal or command prompt in the root directory of the project (where the `pom.xml` file is located).
3.  Build and run the application using Maven:

<!-- end list -->

```bash
mvn clean javafx:run
```

This command will compile the project, download the necessary dependencies, and launch the application.

This project is being actively developed to include more advanced features for real-world business use cases.
