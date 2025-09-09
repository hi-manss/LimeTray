

# Order Management API

A scalable backend service for processing food delivery orders using Java, Spring Boot, MySQL, and Kafka.

## Features

- Place an order
- Fetch all orders (with pagination)
- Fetch order status
- Manually update order status
- Asynchronous order processing via Kafka
- Validation and exception handling
- DTOs and service layer abstraction
- MySQL data storage and optimized queries

## Technologies

- Java 17+
- Spring Boot
- MySQL
- Kafka (or in-memory queue)
- Maven

## Getting Started

1. Clone the repository.
2. Configure `src/main/resources/application.properties` for MySQL and Kafka.
3. Build: `mvn clean install`
4. Run: `mvn spring-boot:run`

## API Endpoints

### 1. Place an Order

**POST** `/api/orders`

**Request Body**
```json
{
    "customerName": "Himanshu Chauhan",
    "items": [
        {
            "name": "Margherita Pizza",
            "quantity": 1,
            "price": 450.00
        },
        {
            "name": "Cold Coffee",
            "quantity": 2,
            "price": 120.00
        }
    ],
    "totalAmount": 690.00,
    "orderTime": "2025-09-08T18:30:00Z"
}
```

**Response**
```json
{
    "id": 2,
    "customerName": "Himanshu Chauhan",
    "items": [
        {
            "id": 3,
            "name": "Margherita Pizza",
            "qty": 1,
            "price": 450.00
        },
        {
            "id": 4,
            "name": "Cold Coffee",
            "qty": 2,
            "price": 120.00
        }
    ],
    "totalAmount": 690.00,
    "orderTime": "2025-09-08T18:30:00Z",
    "status": "CREATED"
}
```

### 2. Fetch All Orders (with Pagination)

**GET** `/api/orders?page=0&size=10`

**Response**
```json
{
  "content": [
    {
      "id": 1,
      "customerName": "Himanshu Chauhan",
      "items": [
        {
          "id": 1,
          "name": "Margherita Pizza",
          "qty": 1,
          "price": 450.00
        },
        {
          "id": 2,
          "name": "Cold Coffee",
          "qty": 2,
          "price": 120.00
        }
      ],
      "totalAmount": 690.00,
      "orderTime": "2025-09-08T18:30:00Z",
      "status": "PROCESSING"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 1,
  "empty": false
}
```

###  Fetch  Orders (with with id)

**GET** `/api/order/2`

**Response**
```json
{
    "id": 2,
    "customerName": "Dd",
    "items": [
        {
            "id": 3,
            "name": "Margherita Pizza",
            "description": null,
            "quantity": 1,
            "price": 450.00
        },
        {
            "id": 4,
            "name": "Cold Coffee",
            "description": null,
            "quantity": 2,
            "price": 120.00
        }
    ],
    "totalAmount": 690.00,
    "orderTime": "2025-09-08T18:30:00Z",
    "status": "PROCESSING",
    "createdAt": "2025-09-09T05:33:54.000+00:00",
    "updatedAt": "2025-09-09T05:34:59.000+00:00",
    "orderStatusDetails": [
        {
            "id": 3,
            "status": "CREATED",
            "updatedBy": "User",
            "updatedAt": "2025-09-09T05:33:54.000+00:00",
            "notes": "Order created"
        },
        {
            "id": 4,
            "status": "PROCESSING",
            "updatedBy": "System",
            "updatedAt": "2025-09-09T05:33:59.000+00:00",
            "notes": "Order is being processed"
        }
    ],
    "notes": "Order is being processed"
}
```

### 3. Fetch Order Status

**GET** `/api/orders/{orderId}/status`

**Response**
```json
{
  "orderId": 123,
  "orderStatus": "PROCESSED"
}
```

### 4. Manually Update Order Status

**PATCH** `api/order?id=2&customerName=test&status=COMPLETED)`


**Response**
```json
{
  Order updated successfully
}
```

### Error Response

```json
{
  "timestamp": "2024-06-10T12:01:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Customer name is required",
  "path": "/api/orders"
}
```

## Asynchronous Order Processing

- When an order is placed, it is published to a Kafka topic (or in-memory queue).
- A background consumer listens for new orders and updates their status to 'PROCESSED' after handling.
- This enables scalable, non-blocking order processing.

## Best Practices

- DTOs are used for request/response payloads.
- Service layer abstracts business logic.
- Validation and exception handling are implemented using Spring's mechanisms.
- MySQL is used for persistent storage; queries are optimized for performance.
- Configuration is managed via `application.properties`.

## License

MIT
