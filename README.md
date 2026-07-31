# Spring Data JPA Fetch Strategy Benchmark

This repository contains the source code developed as part of the Bachelor's thesis:

**Performance Analysis of Fetch Strategies in Spring Data JPA**

The project provides a controlled benchmark environment for evaluating the performance of different fetch strategies in Spring Boot applications using Spring Data JPA and Hibernate.

---

## Purpose

The purpose of this project is to compare different JPA fetch strategies under identical conditions and to analyse their impact on application performance.

The benchmark evaluates the following performance metrics:

- SQL query count
- Execution time
- Memory consumption
- Memory-Time Product

The application was developed exclusively for the empirical study presented in the corresponding Bachelor's thesis.

---

## Technologies

- Java 21
- Spring Boot 3
- Spring Data JPA
- Hibernate ORM
- PostgreSQL
- Maven

---

## Project Structure

```
src
├── main
│   ├── java
│   │   └── at
│   │       └── ba
│   │           └── test
│   │               └── project
│   │                   ├── config
│   │                   ├── controller
│   │                   ├── dto
│   │                   ├── entity
│   │                   ├── repository
│   │                   └── service
│   └── resources
│       └── application.yml
└── test
```

---

## Domain Model

The benchmark uses a simplified e-commerce domain model consisting of the following entities:

- Customer
- CustomerOrder
- OrderItem
- Product
- Category
- Review

The performance evaluation focuses primarily on the following entity relationships:

```
CustomerOrder
      │
      ▼
 OrderItem
      │
      ▼
 Product
```

The repository also contains the entity relationship diagram (`entity_diagram.drawio` and `entity_diagram.png`) used during the development of the benchmark application.

---

## Implemented Benchmark Scenarios

The application implements the following benchmark scenarios:

| Scenario | Description |
|----------|-------------|
| S1 | Lazy Loading (Order Summary) |
| S2 | DTO Projection |
| S3 | N+1 Problem (Order → Items) |
| S4 | Nested N+1 (Order → Items → Product) |
| S5 | JOIN FETCH (Order → Items) |
| S6 | EntityGraph (Order → Items) |
| S7 | JOIN FETCH (Order → Items → Product) |
| S8 | EntityGraph (Order → Items → Product) |
| S9 | Overfetching |

---

## Benchmark Methodology

Each benchmark scenario is executed under identical conditions.

For every scenario the application performs:

- 4 warm-up runs
- 10 measured runs

Before each measurement, Hibernate statistics are reset to ensure consistent query counting.

After the benchmark execution, the application automatically calculates:

- average values,
- variance,
- standard deviation,
- average Memory-Time Product.

---

## Test Data

The application generates the required test data automatically during startup.

The dataset size is controlled in:

```
src/main/java/at/ba/test/project/config/DataLoader.java
```

by changing the constant:

```java
private static final int ORDER_COUNT = ...
```

The experiments presented in the Bachelor's thesis were performed using datasets containing:

- 100 orders
- 1,000 orders
- 5,000 orders

After changing the dataset size, restart the application so that a new database is generated.

If the database already contains data, no new dataset is created.

---

## Running the Application

Configure the PostgreSQL connection in:

```
src/main/resources/application.yml
```

Then start the application using Maven:

```bash
mvn spring-boot:run
```

Alternatively, the project can be started directly from IntelliJ IDEA or another Java IDE.

---

## Benchmark Endpoints

Run all benchmark scenarios:

```
GET /benchmark/run-all
```

Run the averaged benchmark:

```
GET /benchmark/run-all-averaged
```

Export the averaged benchmark results as CSV:

```
GET /benchmark/run-all-averaged/csv
```

Run the benchmark and save the generated CSV file:

```
GET /benchmark/run-all-save
```

---

## Collected Metrics

For every benchmark scenario the application records:

- executed SQL statements,
- execution time,
- memory usage before execution,
- memory usage after execution,
- memory consumption (delta),
- Memory-Time Product.

Average values, variance and standard deviation are calculated automatically.

---

## Reproducibility

This repository contains the complete source code required to verify the experiments presented in the Bachelor's thesis.

It includes:

- the domain model,
- repository implementations,
- benchmark scenarios,
- fetch strategy implementations,
- test data generation,
- measurement logic,
- CSV export.

The benchmark scenarios can be executed and verified under the same experimental conditions described in the thesis.

---

## License

This repository is published for scientific and educational purposes as supporting material for the corresponding Bachelor's thesis.