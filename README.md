# Wex - Currency Exchange API
This is a REST API for currency exchange built with Spring Boot and Java 17. It allows users to create and manage currency exchange transactions.

This project uses an H2 in memory database, but could be easily switched to a real database if needed.

The project also provides a postman collection for easy testing. (`Wex.postman_collection.json`)

The unit tests and integration tests are written using JUnit and Mockito.

The project also runs with Nginx to handle port forwarding and load balancing.

## Nginx Configuration

The application is set up to use Nginx as a reverse proxy to forward requests to the backend service. Below is a brief explanation of the Nginx configuration used in this project.

### Configuration File: `nginx/default.conf`

This project also runs using Nginx server.

- **Server Block**: Specifies the server configuration.
  - `listen 80;`: Nginx listens on port 80 for incoming HTTP requests.
  - **Location Block**: Defines how requests to the root URL (`/`) are handled.
    - `proxy_pass http://app:8080;`: Forwards requests to the backend service running on `app` at port `8080`.
    - `proxy_set_header Host $host;`: Passes the original `Host` header from the client to the backend service.
    - `proxy_set_header X-Real-IP $remote_addr;`: Passes the client’s IP address to the backend service.

We can also define which load balancer algorithm to use on nginx as follows:
 - round-robin (default): alternate between servers;
 - least_conn: sends requests to the least busy server;
 - ip_hash: always send requests to the same server.

This setup allows Nginx to act as a gateway and load balancer, directing HTTP traffic to the appropriate backend service while preserving important header information.

## Swagger UI

The Swagger UI is available at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Running project with docker-compose

- **Steps**:	
	- Run `docker-compose up --build`
	* To access the H2 database console, go to `http://localhost:8083`
	+ In the postman collection, use the `* Docker` requests
    + The 2 instances running in cluster share the same H2 database
    + Access the Swagger UI at `http://localhost/swagger-ui/index.html`

## Running project as a spring application

- **Steps**:
    - Uncomment the 'spring.datasource.url' property for local deployment in `application.properties` (Docker deployment is configured by default)
	- Run `mvn spring-boot:run`
	* To access the H2 database console, go to `http://localhost:8083`
	+ In the postman collection, use the `* Local` requests
    + Access the Swagger UI at `http://localhost:8080/swagger-ui/index.html`

## Features

* Create and manage currency exchange transactions
* Support for multiple currencies
* Swagger UI for API documentation and testing
* Docker image for easy deployment

## Technologies

* Spring Boot 2.7.0
* Java 17
* Gradle 7.4
* Docker
* OpenAPI 3.0 (Swagger)
* Nginx
* Lombok

## Setup

### Local Development

1. Clone the repository
2. Configure the `application.properties` file to point to your preferred exchange rates provider
3. Uncomment the 'spring.datasource.url' property for local deployment in `application.properties` (Docker deployment is configured by default)
4. Run the application
5. Access the Swagger UI at `http://localhost:8080/swagger-ui/index.html`

### Docker

1. Build the Docker image with `docker build -t wex-app .`
2. Run the Docker container with `docker run -p 8080:8080 wex-app`
3. Access the Swagger UI at `http://localhost:8080/swagger-ui/index.html`

## API Endpoints

### POST /transactions

Create a new Transaction

* Request body:
	+ `description`: Short transaction description
	+ `transactionDate`: Date of transaction
	+ `amount`: Amount of transaction
* Response:
	+ `id`: The ID of the created transaction
	+ `description`: Short transaction description
	+ `transactionDate`: Date of transaction
	+ `purchaseAmount`: Amount of transaction

### GET /transactions/{id}

Get a currency exchange transaction by ID

* Path parameter:
	+ `id`: The ID of the transaction
* Response:
	+ `id`: The ID of the transaction
	+ `amount`: The amount of money exchanged
	+ `sourceCurrency`: The source currency
	+ `targetCurrency`: The target currency
	+ `exchangeRate`: The exchange rate used
	+ `result`: The result of the exchange (e.g. EUR 10.00)
