# Backend Assignment - JSON Data API

A RESTful API built with Spring Boot for storing and querying JSON records in datasets with support for grouping and sorting operations.

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Backend_Assignment
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE backend_assignment;
```

### 3. Configure Database Connection

Update the database credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/backend_assignment?user=root&password=YOUR_PASSWORD
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Base URL

```
http://localhost:8080/api
```

---

### 1. Insert Record

Insert a JSON record into a specified dataset.

**Endpoint:** `POST /dataset/{datasetName}/record`

**Path Parameters:**

- `datasetName` (string, required) - Name of the dataset

**Request Body:**

- JSON object with any structure

**Example Request:**

```bash
curl -X POST http://localhost:8080/api/dataset/users/record \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "age": 30,
    "city": "New York",
    "email": "john@example.com"
  }'
```

**Success Response:**

```json
{
	"message": "Record added successfully",
	"dataset": "users",
	"recordId": 1
}
```

**Error Responses:**

- **400 Bad Request** - Empty JSON body

  ```json
  {
  	"message": "JSON body cannot be empty"
  }
  ```

- **500 Internal Server Error** - JSON processing error
  ```json
  {
  	"message": "Error processing JSON data"
  }
  ```

---

### 2. Query Records

Query records from a dataset with optional grouping and sorting.

**Endpoint:** `GET /dataset/{datasetName}/query`

**Path Parameters:**

- `datasetName` (string, required) - Name of the dataset

**Query Parameters:**

- `groupBy` (string, optional) - Field name to group records by
- `sortBy` (string, optional) - Field name to sort records by
- `order` (string, optional) - Sort order: `asc` or `desc` (default: `asc`)

#### 2.1. Query All Records

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/dataset/users/query"
```

**Success Response:**

```json
{
	"sortedRecords": [
		{
			"name": "John Doe",
			"age": 30,
			"city": "New York",
			"email": "john@example.com"
		},
		{
			"name": "Jane Smith",
			"age": 25,
			"city": "London",
			"email": "jane@example.com"
		}
	],
	"groupedRecords": null
}
```

#### 2.2. Query with Sorting (Ascending)

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/dataset/users/query?sortBy=age&order=asc"
```

**Success Response:**

```json
{
	"sortedRecords": [
		{
			"name": "Jane Smith",
			"age": 25,
			"city": "London"
		},
		{
			"name": "John Doe",
			"age": 30,
			"city": "New York"
		},
		{
			"name": "Bob Wilson",
			"age": 35,
			"city": "Paris"
		}
	],
	"groupedRecords": null
}
```

#### 2.3. Query with Sorting (Descending)

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/dataset/users/query?sortBy=name&order=desc"
```

**Success Response:**

```json
{
	"sortedRecords": [
		{
			"name": "John Doe",
			"age": 30,
			"city": "New York"
		},
		{
			"name": "Jane Smith",
			"age": 25,
			"city": "London"
		},
		{
			"name": "Bob Wilson",
			"age": 35,
			"city": "Paris"
		}
	],
	"groupedRecords": null
}
```

#### 2.4. Query with Grouping

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/dataset/users/query?groupBy=city"
```

**Success Response:**

```json
{
	"groupedRecords": {
		"New York": [
			{
				"name": "John Doe",
				"age": 30,
				"city": "New York"
			},
			{
				"name": "Alice Brown",
				"age": 28,
				"city": "New York"
			}
		],
		"London": [
			{
				"name": "Jane Smith",
				"age": 25,
				"city": "London"
			}
		],
		"Paris": [
			{
				"name": "Bob Wilson",
				"age": 35,
				"city": "Paris"
			}
		]
	},
	"sortedRecords": null
}
```

**Error Responses:**

- **404 Not Found** - No records found in dataset

  ```json
  {
  	"message": "No records found for dataset: users"
  }
  ```

- **400 Bad Request** - Invalid order parameter

  ```json
  {
  	"message": "Order must be 'asc' or 'desc'"
  }
  ```

- **400 Bad Request** - Invalid field name for grouping

  ```json
  {
  	"message": "Group by field 'invalidField' not found in record"
  }
  ```

- **400 Bad Request** - Invalid field name for sorting
  ```json
  {
  	"message": "Sort by field 'invalidField' not found in record"
  }
  ```

---

## Running Tests

Execute the test suite:

```bash
mvn test
```

Run tests for a specific class:

```bash
mvn test -Dtest=DataServiceImplTest
```

---

## Technology Stack

- **Spring Boot 4.0.2**
- **Java 21**
- **MySQL 8.0**
- **Maven**
- **JPA/Hibernate**
- **Lombok**
- **Jackson (JSON processing)**
- **JUnit 5 & Mockito (Testing)**

---

## Notes

- The API supports storing any valid JSON structure
- Sorting works with both numeric and string values
- When grouping is applied, the response contains `groupedRecords`
- When sorting is applied (or no parameters), the response contains `sortedRecords`
- All JSON data is stored as TEXT in the MySQL database
- The application automatically creates/updates database tables on startup
