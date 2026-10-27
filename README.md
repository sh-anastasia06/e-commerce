# E-commerce microservice application

## Endpoints

### Product Catalog Service (`/api/v1`)

| Method | Endpoint       | Description                                        | Security |
| ------ | -------------- | -------------------------------------------------- | -------- |
| GET    | /products      | Get list of products (with filtering & pagination) | Public   |
| POST   | /products      | Create a new product                               | Public   |
| GET    | /products/{id} | Get product by ID                                  | Public   |
| PUT    | /products/{id} | Update product                                     | Public   |
| DELETE | /products/{id} | Delete product by ID                               | Public   |

### User Service (`/api/v1`)

| Method | Endpoint               | Description                         | Security   |
| ------ | ---------------------- | ----------------------------------- | ---------- |
| POST   | /auth/register         | Register a new user                 | Public     |
| POST   | /auth/login            | Authenticate user & return JWT      | Public     |
| POST   | /auth/refresh          | Refresh access token                | Public     |
| GET    | /.well-known/jwks.json | Get JSON Web Key Set for validation | Public     |
| GET    | /users/me              | Get current user profile            | Bearer JWT |
| PUT    | /users/me              | Update current user profile         | Bearer JWT |

### Order Service (`/api/v1`)

| Method | Endpoint            | Description                       | Security   |
| ------ | ------------------- | --------------------------------- | ---------- |
| POST   | /orders             | Create a new order                | Bearer JWT |
| GET    | /orders             | Get user's orders with pagination | Bearer JWT |
| GET    | /orders/{id}        | Get order by ID                   | Bearer JWT |
| PATCH  | /orders/{id}/status | Update order status               | Bearer JWT |

### Notification Service (`/api/v1`)

| Method | Endpoint                        | Description                            | Security |
| ------ | ------------------------------- | -------------------------------------- | -------- |
| GET    | /notifications                  | Get notification history               | Public   |
| POST   | /notifications/simulate-payment | Simulate payment response for an order | Public   |