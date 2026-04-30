# E-Commerce Microservices (Spring Boot + Oracle + Vue)

Production-oriented microservices reference implementation:
- API Gateway (`Spring Cloud Gateway`)
- Service Discovery (`Eureka`)
- Auth, User, Product, Inventory, Order, Payment services
- Hybrid communication:
  - Synchronous: REST APIs for command flow
  - Asynchronous: Kafka events for order status propagation
- JWT stateless authentication
- Oracle SQL with explicit DDL and seed scripts (no auto DDL)
- Vue 3 frontend (login, products, cart, checkout)

## Prerequisites
- JDK 17
- Maven 3.9+
- Node.js 20+
- Oracle DB (local or Docker)

## Run Oracle (Docker)
```bash
docker compose up -d oracle-db zookeeper kafka
```

## Apply schema and data
Run SQL files in `sql/` in this order:
1. `sql/schema.sql`
2. `sql/data.sql`

## Run backend services
From project root:
```bash
mvn -pl discovery-service spring-boot:run
mvn -pl api-gateway spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl user-service spring-boot:run
mvn -pl product-service spring-boot:run
mvn -pl inventory-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl payment-service spring-boot:run
```

## Run frontend
```bash
cd frontend
npm install
npm run dev
```

## API flow example
1. `POST /auth/api/v1/auth/login` -> get JWT
2. `GET /products/api/v1/products` with `Authorization: Bearer ...`
3. `POST /orders/api/v1/orders` creates order
4. Order service synchronously calls inventory + payment by REST
5. Order service publishes Kafka `order-status-topic` event
6. Inventory and Payment services consume order events asynchronously

