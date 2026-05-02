# ProjectWeb - Microservices Backend

## Giới thiệu

Hệ thống backend thương mại điện tử được xây dựng theo kiến trúc **Microservices** sử dụng **Spring Boot 3** và **Java 21**. Hệ thống bao gồm 7 service độc lập, giao tiếp qua REST API và Apache Kafka.

## Kiến trúc hệ thống

```
React Frontend (port 5173)
        │
        ▼
  API Gateway (8181)  ◄── Redis (rate limiting + response cache)
        │
        ├── /v1/api/user/**       → User Service (8081)     ◄── MongoDB + Redis
        ├── /v1/api/products/**   → Product Service (8082)  ◄── MongoDB + Redis
        ├── /v1/api/order/**      → Order Service (8083)    ◄── MongoDB + Kafka + Feign→Product
        └── /v1/api/payment/**    → Payment Service (8085)  ◄── VNPay Sandbox
                                                    
Kafka (notificationTopic)
        │
        ▼
Notification Service (8084) ── Mailtrap SMTP ── Email

Tất cả service đăng ký với:
  Eureka Server (8761)

Observability:
  Zipkin (9411)      ← Distributed tracing
  Prometheus (9090)  ← Metrics
  Kafka UI (8080)    ← Kafka monitoring
```

## Công nghệ sử dụng

| Thành phần          | Công nghệ                          |
|---------------------|------------------------------------|
| Ngôn ngữ            | Java 21                            |
| Framework           | Spring Boot 3.4.4                  |
| Service Discovery   | Spring Cloud Netflix Eureka        |
| API Gateway         | Spring Cloud Gateway (WebFlux)     |
| Database            | MongoDB                            |
| Cache & Token Store | Redis 6.2                          |
| Message Broker      | Apache Kafka 3.7.0 (KRaft mode)   |
| Authentication      | JWT (HS256) + BCrypt               |
| Thanh toán          | VNPay Sandbox                      |
| Email               | JavaMailSender + Mailtrap SMTP     |
| Tracing             | Micrometer + Zipkin                |
| Metrics             | Micrometer + Prometheus            |
| Build Tool          | Apache Maven                       |
| CI/CD               | GitHub Actions                     |
| Containerization    | Docker (multi-stage build)         |
| Orchestration       | Kubernetes                         |

## Cấu trúc thư mục

```
ProjectWeb-BE/
├── pom.xml                        # Parent Maven POM
├── docker-compose.yml             # Docker Compose cho local
├── .github/workflows/deploy.yml   # CI/CD pipeline
├── k8s/                           # Kubernetes manifests
│   ├── api-gateway-deployment.yaml
│   ├── eureka-server-deployment.yaml
│   ├── user-service-deployment.yaml
│   ├── product-service-deployment.yaml
│   ├── order-service-deployment.yaml
│   ├── notification-service-deployment.yaml
│   └── payment-service-deployment.yaml
├── api-gateway/                   # API Gateway (port 8181)
├── eureka-server/                 # Service Discovery (port 8761)
├── user-service/                  # Quản lý user (port 8081)
├── product-service/               # Quản lý sản phẩm (port 8082)
├── order-service/                 # Quản lý đơn hàng (port 8083)
├── notification-service/          # Gửi email thông báo (port 8084)
├── payment-service/               # Thanh toán VNPay (port 8085)
└── README.md
```

Mỗi service có cấu trúc chuẩn Spring Boot:

```
<service>/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/vn/tt/practice/<service>/
    │   ├── config/         # Cấu hình (Security, Redis, Kafka...)
    │   ├── controller/     # REST Controller
    │   ├── dto/            # Data Transfer Objects
    │   ├── mapper/         # Entity ↔ DTO mapper
    │   ├── model/          # MongoDB Entity
    │   ├── repository/     # MongoDB Repository
    │   ├── service/        # Business Logic
    │   └── <Service>Application.java
    └── resources/
        └── application.properties
```

## Yêu cầu hệ thống

Trước khi bắt đầu, đảm bảo máy bạn đã cài đặt:

| Phần mềm     | Phiên bản tối thiểu | Kiểm tra                |
|---------------|----------------------|-------------------------|
| Java JDK      | 21                   | `java -version`         |
| Apache Maven   | 3.9+                 | `mvn -version`          |
| Docker         | 20.10+               | `docker --version`      |
| Docker Compose | 2.0+                 | `docker compose version`|
| Git            | 2.30+                | `git --version`         |

## Hướng dẫn cài đặt và chạy

### Cách 1: Chạy bằng Docker Compose (Khuyến nghị)

Đây là cách đơn giản nhất, chỉ cần 1 lệnh duy nhất:

```bash
# 1. Clone repo
git clone https://github.com/Taun0813/ProjectWeb.git
cd ProjectWeb/ProjectWeb-BE

# 2. Khởi chạy toàn bộ hệ thống
docker compose up --build
```

Docker Compose sẽ tự động khởi chạy tất cả các thành phần:

| Container             | Port  | Mô tả                         |
|-----------------------|-------|--------------------------------|
| `mongo`               | 27017 | MongoDB database               |
| `redis`               | 6379  | Redis cache & token store      |
| `kafka-1`, `kafka-2`, `kafka-3` | 9092, 9094, 9096 | Kafka cluster (KRaft) |
| `kafka-ui`            | 8080  | Giao diện quản lý Kafka        |
| `zipkin`              | 9411  | Distributed tracing UI         |
| `prometheus`          | 9090  | Metrics dashboard              |
| `eureka-server`       | 8761  | Service Discovery dashboard    |
| `user-service`        | 8081  | User API                       |
| `product-service`     | 8082  | Product API                    |
| `order-service`       | 8083  | Order API                      |
| `notification-service`| 8084  | Email notification (Kafka)     |
| `payment-service`     | 8085  | VNPay payment API              |
| `api-gateway`         | 8181  | API Gateway (entry point)      |

Sau khi chạy xong, truy cập:
- **API Gateway:** http://localhost:8181
- **Eureka Dashboard:** http://localhost:8761 (user: `admin`, password: `password`)
- **Kafka UI:** http://localhost:8080
- **Zipkin:** http://localhost:9411
- **Prometheus:** http://localhost:9090

Dừng hệ thống:

```bash
docker compose down
```

### Cách 2: Chạy thủ công từng service (Development)

Cách này phù hợp khi bạn muốn debug hoặc chỉ chạy một vài service.

#### Bước 1: Cài đặt infrastructure

Bạn cần chạy MongoDB, Redis và Kafka trước. Có thể dùng Docker:

```bash
# Chạy MongoDB
docker run -d --name mongo -p 27017:27017 mongo

# Chạy Redis
docker run -d --name redis -p 6379:6379 redis:6.2-alpine

# Chạy Kafka (KRaft mode - single node cho dev)
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  apache/kafka:3.7.0

# (Tùy chọn) Chạy Zipkin
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin
```

#### Bước 2: Build toàn bộ project

```bash
cd ProjectWeb-BE

# Build tất cả các module
./mvnw clean package -DskipTests
```

#### Bước 3: Khởi chạy các service theo thứ tự

**Quan trọng:** Phải chạy theo đúng thứ tự vì các service phụ thuộc lẫn nhau.

```bash
# 1. Eureka Server (phải chạy đầu tiên)
cd eureka-server
../mvnw spring-boot:run
# Đợi cho đến khi thấy "Started EurekaServerApplication"

# 2. User Service
cd ../user-service
../mvnw spring-boot:run

# 3. Product Service
cd ../product-service
../mvnw spring-boot:run

# 4. Order Service
cd ../order-service
../mvnw spring-boot:run

# 5. Notification Service
cd ../notification-service
../mvnw spring-boot:run

# 6. Payment Service
cd ../payment-service
../mvnw spring-boot:run

# 7. API Gateway (chạy cuối cùng)
cd ../api-gateway
../mvnw spring-boot:run
```

> **Mẹo:** Mở mỗi service trong một terminal riêng để dễ theo dõi log.

## Biến môi trường

Tất cả biến môi trường đều có giá trị mặc định cho local development. Chỉ cần thay đổi khi deploy lên server:

| Biến môi trường              | Service sử dụng                      | Mặc định                              |
|------------------------------|---------------------------------------|----------------------------------------|
| `SPRING_DATA_MONGODB_URI`    | user, product, order                  | `mongodb://localhost:27017/<database>` |
| `EUREKA_SERVER_URL`          | Tất cả service                        | `http://localhost:8761/eureka/`        |
| `SPRING_DATA_REDIS_HOST`     | product, order, api-gateway           | `localhost`                            |
| `SPRING_DATA_REDIS_PORT`     | product, order, api-gateway           | `6379`                                 |
| `KAFKA_BOOTSTRAP_SERVERS`    | product, order, notification          | `localhost:9092,9094,9096`             |
| `ZIPKIN_URL`                 | user, product, order, api-gateway     | `http://localhost:9411/api/v2/spans`   |
| `FRONTEND_ALLOWED_ORIGINS`   | api-gateway                           | `http://localhost:5173`                |
| `USER_SERVICE_URL`           | notification-service                  | `http://localhost:8081`                |

## API Endpoints

Tất cả request đi qua **API Gateway** tại `http://localhost:8181`.

### User Service (`/v1/api/user`)

| Method | Endpoint                  | Mô tả                              |
|--------|---------------------------|-------------------------------------|
| POST   | `/v1/api/user/register`   | Đăng ký tài khoản mới              |
| POST   | `/v1/api/user/login`      | Đăng nhập (trả về JWT token)       |
| GET    | `/v1/api/user/logout`     | Đăng xuất (xóa token khỏi Redis)   |
| GET    | `/v1/api/user/{id}`       | Lấy thông tin user theo ID         |
| GET    | `/v1/api/user`            | Lấy danh sách tất cả user         |
| GET    | `/v1/api/user/{id}/email` | Lấy email user (dùng nội bộ)      |

### Product Service (`/v1/api/products`)

| Method | Endpoint                                     | Mô tả                                 |
|--------|----------------------------------------------|----------------------------------------|
| GET    | `/v1/api/products?page=0&size=9`             | Lấy danh sách sản phẩm (phân trang)   |
| GET    | `/v1/api/products/{id}`                      | Lấy chi tiết sản phẩm                 |
| POST   | `/v1/api/products`                           | Thêm sản phẩm mới                     |
| PUT    | `/v1/api/products/{id}/decrease-quantity?amount=N` | Giảm số lượng tồn kho            |
| PUT    | `/v1/api/products/{id}/add-to-cart`          | Thêm sản phẩm vào giỏ hàng            |
| PUT    | `/v1/api/products/{id}/remove-from-cart`     | Xóa sản phẩm khỏi giỏ hàng           |

### Order Service (`/v1/api/order`)

| Method | Endpoint                             | Mô tả                                    |
|--------|--------------------------------------|-------------------------------------------|
| POST   | `/v1/api/order/place-order`          | Đặt hàng (giảm stock + gửi Kafka event)  |
| GET    | `/v1/api/order/{user_id}/get-orders` | Lấy danh sách đơn hàng của user          |
| POST   | `/v1/api/order/cancel-order`         | Hủy đơn hàng                             |

### Payment Service (`/v1/api/payment`)

| Method | Endpoint                              | Mô tả                                   |
|--------|---------------------------------------|------------------------------------------|
| GET    | `/v1/api/payment/create-payment?amount=N` | Tạo URL thanh toán VNPay            |
| GET    | `/v1/api/payment/payment-return`      | Callback từ VNPay (redirect về frontend) |

### Notification Service (port 8084)

- Không có HTTP endpoint công khai
- Lắng nghe Kafka topic **`notificationTopic`** (group: `notification-group`)
- Tự động gửi email khi nhận được event đặt hàng

### Eureka Server (port 8761)

- Dashboard: http://localhost:8761
- Đăng nhập: `admin` / `password`

## Ví dụ sử dụng API

### Đăng ký tài khoản

```bash
curl -X POST http://localhost:8181/v1/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nguyenvana",
    "email": "nguyenvana@example.com",
    "password": "123456"
  }'
```

### Đăng nhập

```bash
curl -X POST http://localhost:8181/v1/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@example.com",
    "password": "123456"
  }'
```

### Lấy danh sách sản phẩm

```bash
curl http://localhost:8181/v1/api/products?page=0&size=9
```

### Đặt hàng

```bash
curl -X POST http://localhost:8181/v1/api/order/place-order \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "<user_id>",
    "items": [
      {
        "productId": "<product_id>",
        "quantity": 2
      }
    ]
  }'
```

## Testing & Build

```bash
# Chạy unit test
./mvnw test

# Build toàn bộ project (tạo file JAR)
./mvnw clean package

# Build bỏ qua test (nhanh hơn)
./mvnw clean package -DskipTests

# Chạy test cho một service cụ thể
cd user-service
../mvnw test
```

## CI/CD với GitHub Actions

Workflow tự động kích hoạt khi push vào nhánh `BE`:

1. **Phát hiện thay đổi** trong các thư mục service
2. **Maven build** cho các service có thay đổi
3. **Docker build & push** image lên Docker Hub
4. **Kubectl apply** cập nhật deployment trên Kubernetes

### Secrets cần thiết trong GitHub

| Secret            | Mô tả                             |
|-------------------|------------------------------------|
| `DOCKER_USERNAME` | Tài khoản Docker Hub               |
| `DOCKER_PASSWORD` | Mật khẩu hoặc access token Docker  |
| `KUBECONFIG`      | Nội dung file `~/.kube/config`     |

## Triển khai Kubernetes

File manifest nằm trong thư mục `k8s/`:

```bash
# Apply tất cả deployment
kubectl apply -f k8s/

# Kiểm tra trạng thái pods
kubectl get pods

# Xem logs của một service
kubectl logs -f deployment/<service-name>
```

## Xử lý sự cố thường gặp

### Service không kết nối được Eureka

- Đảm bảo Eureka Server đã chạy trước tất cả service khác
- Kiểm tra biến `EUREKA_SERVER_URL` đúng địa chỉ

### Lỗi kết nối MongoDB

- Kiểm tra MongoDB đang chạy: `docker ps | grep mongo`
- Kiểm tra port 27017 không bị chiếm: `netstat -an | grep 27017`

### Kafka không nhận message

- Đảm bảo Kafka cluster đã chạy và healthy
- Kiểm tra trên Kafka UI: http://localhost:8080
- Xem topic `notificationTopic` đã được tạo chưa

### Port bị chiếm

- Kiểm tra port: `netstat -an | grep <port>`
- Dừng process đang dùng port hoặc thay đổi port trong `application.properties`

### Docker build lỗi

- Đảm bảo Docker daemon đang chạy
- Kiểm tra dung lượng ổ đĩa: `docker system df`
- Xóa cache cũ: `docker system prune`

## Liên hệ

- **Tác giả:** [@Taun0813](https://github.com/Taun0813)
- **Repository:** [https://github.com/Taun0813/ProjectWeb](https://github.com/Taun0813/ProjectWeb)
