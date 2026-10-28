# Event Catalog & Order Lifecycle

## 1. Order Status Transitions

The lifecycle of an order follows a strict state machine:
* `CREATED` $\rightarrow$ `PAID` (successful payment simulation) or `CANCELLED`
* `PAID` $\rightarrow$ `PROCESSING` (order being packed/prepared)
* `PROCESSING` $\rightarrow$ `SHIPPED` (dispatched to customer)
* `SHIPPED` $\rightarrow$ `DELIVERED` (received by customer)

---

## 2. Event Catalog (Kafka Topics)

### 1. `order-created`
* **Producer:** `Order Service`
* **Consumers:** `Notification & Processing Service`
* **Message Key:** `orderId`
* **Description:** Triggered when a new order is successfully created.
* **Payload Fields:**
    * `eventId` (string, UUID)
    * `orderId` (string)
    * `userId` (string)
    * `userEmail` (string)
    * `totalAmount` (number)
    * `currency` (string)
    * `createdAt` (string, date-time)

### 2. `order-status-updated`
* **Producer:** `Notification & Processing Service`
* **Consumers:** `Order Service`
* **Message Key:** `orderId`
* **Description:** Triggered when an order status is updated (payment success/failure).
* **Payload Fields:**
    * `eventId` (string, UUID)
    * `orderId` (string)
    * `newStatus` (string: `PAID` | `CANCELLED`)
    * `reason` (string, optional)
    * `occurredAt` (string, date-time)

### 3. `user-registered`
* **Producer:** `User Service`
* **Consumers:** `Notification & Processing Service`
* **Message Key:** `userId`
* **Description:** Triggered when a new user registers in the system.
* **Payload Fields:**
    * `eventId` (string, UUID)
    * `userId` (string)
    * `email` (string)
    * `registeredAt` (string, date-time)