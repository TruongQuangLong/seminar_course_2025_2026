# BÁO CÁO XÂY DỰNG MODULE PRODUCT-SERVICE

**Bài 7 – Seminar Chuyên Đề**
**Sinh viên:** Đỗ Gia Huy – MSSV: 3122411062 – Lớp: DCT122C2

---

## 1. Giới thiệu

### 1.1. Bối cảnh

Trong kiến trúc Microservices, mỗi service đảm nhiệm một nghiệp vụ riêng biệt và giao tiếp với nhau thông qua các giao thức nhẹ như REST API hoặc Message Queue. Hệ thống thương mại điện tử **ProjectWeb** được xây dựng theo mô hình này, bao gồm 7 service độc lập: `eureka-server`, `api-gateway`, `user-service`, `product-service`, `order-service`, `notification-service` và `payment-service`.

Trong đó, **product-service** giữ vai trò quản lý danh mục sản phẩm — một trong những nghiệp vụ cốt lõi của bất kỳ hệ thống thương mại điện tử nào. Service này cần đáp ứng các yêu cầu: cung cấp API tạo mới và truy vấn sản phẩm, lưu trữ dữ liệu trên cơ sở dữ liệu quan hệ, đăng ký với Service Discovery, và tích hợp các thành phần giám sát hệ thống.

### 1.2. Mục tiêu

Báo cáo này trình bày quá trình thiết kế và triển khai module `product-service` với các mục tiêu cụ thể:

- Xây dựng RESTful API phục vụ thao tác CRUD sản phẩm sử dụng **Spring Boot 3** và **Java 21**.
- Tích hợp **PostgreSQL** làm hệ quản trị cơ sở dữ liệu quan hệ thông qua **Spring Data JPA**.
- Áp dụng mẫu thiết kế **Circuit Breaker** (Resilience4j) để nâng cao khả năng chịu lỗi.
- Tích hợp hệ sinh thái giám sát: **Spring Boot Actuator**, **Prometheus** (metrics), **Zipkin** (distributed tracing).
- Đăng ký service với **Eureka Server** để hỗ trợ cơ chế Service Discovery.
- Đóng gói ứng dụng bằng **Docker** với chiến lược multi-stage build.

### 1.3. Công nghệ sử dụng

| Thành phần | Công nghệ | Phiên bản |
|---|---|---|
| Ngôn ngữ lập trình | Java | 21 |
| Framework chính | Spring Boot | 3.4.4 |
| Tầng truy cập dữ liệu | Spring Data JPA (Hibernate) | 3.4.4 |
| Cơ sở dữ liệu | PostgreSQL | 15+ |
| Chịu lỗi hệ thống | Resilience4j Circuit Breaker | 3.2.0 |
| Service Discovery | Spring Cloud Netflix Eureka Client | 4.2.0 |
| Giám sát sức khỏe | Spring Boot Actuator | 3.4.4 |
| Thu thập metrics | Micrometer + Prometheus | (managed) |
| Distributed Tracing | Micrometer Tracing + Zipkin | (managed) |
| Giảm boilerplate code | Lombok | 1.18.30 |
| Build tool | Apache Maven | 3.9+ |
| Container hóa | Docker (multi-stage build) | 20.10+ |

---

## 2. Thiết kế kiến trúc

### 2.1. Vị trí trong hệ thống tổng thể

```
Client (React - port 5173)
    │
    ▼
API Gateway (port 8181)
    │
    ├── /api/product/** ──→ Product Service (port 8082) ◄── PostgreSQL
    ├── /v1/api/order/**  ──→ Order Service (port 8083)
    └── /v1/api/user/**   ──→ User Service (port 8081)
                                        
Tất cả service đăng ký với Eureka Server (port 8761)
Observability: Zipkin (9411) + Prometheus (9090)
```

`product-service` hoạt động như một microservice độc lập, nhận request từ API Gateway thông qua cơ chế load-balanced routing (`lb://product-service`). Service đăng ký định danh `product-service` với Eureka Server để các service khác (như `order-service`) có thể gọi thông qua tên logic thay vì địa chỉ IP cứng.

### 2.2. Kiến trúc phân tầng (Layered Architecture)

Module được tổ chức theo mô hình phân tầng chuẩn của Spring Boot, đảm bảo tính tách biệt trách nhiệm (Separation of Concerns):

```
product-service/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/hdbank/productservice/
    │   ├── ProductServiceApplication.java      ← Điểm khởi chạy ứng dụng
    │   ├── controller/
    │   │   └── ProductController.java          ← Tầng Presentation (REST API)
    │   ├── service/
    │   │   └── ProductService.java             ← Tầng Business Logic
    │   ├── repository/
    │   │   └── ProductRepository.java          ← Tầng Data Access
    │   ├── model/
    │   │   └── Product.java                    ← Entity (ánh xạ bảng CSDL)
    │   ├── dto/
    │   │   ├── ProductRequest.java             ← DTO nhận dữ liệu đầu vào
    │   │   └── ProductResponse.java            ← DTO trả dữ liệu đầu ra
    │   └── util/                               ← Lớp tiện ích (mở rộng)
    └── resources/
        └── application.properties              ← Cấu hình ứng dụng
```

Luồng xử lý tuần tự: **Controller → Service → Repository → Database**, trong đó dữ liệu được chuyển đổi giữa các tầng thông qua đối tượng DTO (Data Transfer Object) và Entity.

---

## 3. Triển khai chi tiết

### 3.1. Tầng Model — Entity `Product`

Entity `Product` được ánh xạ đến bảng `products` trong PostgreSQL thông qua các annotation của Jakarta Persistence API (JPA):

```java
@Entity
@Table(name = "products")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String description;
    private BigDecimal price;
}
```

**Các quyết định thiết kế quan trọng:**

| Quyết định | Giải thích |
|---|---|
| `@GeneratedValue(strategy = GenerationType.UUID)` | Hibernate 6+ (đi kèm Spring Boot 3) hỗ trợ tự sinh UUID dạng chuỗi. Giúp tránh xung đột ID trong môi trường phân tán — phù hợp với kiến trúc Microservices. |
| Kiểu `BigDecimal` cho `price` | Đảm bảo độ chính xác tuyệt đối cho phép tính tiền tệ, tránh lỗi làm tròn của `double`/`float`. |
| Lombok `@Data`, `@Builder` | Tự động sinh getter/setter, `equals()`, `hashCode()`, `toString()` và Builder Pattern — giảm boilerplate code đáng kể. |

### 3.2. Tầng Repository

```java
public interface ProductRepository extends JpaRepository<Product, String> {
}
```

Kế thừa `JpaRepository<Product, String>` cung cấp sẵn các phương thức CRUD tiêu chuẩn: `save()`, `findAll()`, `findById()`, `deleteById()`, v.v. mà không cần viết bất kỳ câu SQL nào. Spring Data JPA sẽ tự động tạo implementation tại thời điểm runtime.

### 3.3. Tầng DTO — Tách biệt Request và Response

Việc sử dụng DTO Pattern giúp tách biệt cấu trúc dữ liệu bên trong (Entity) khỏi dữ liệu trao đổi với bên ngoài (API), tuân thủ nguyên tắc đóng gói (Encapsulation).

**ProductRequest** — Dữ liệu client gửi lên khi tạo sản phẩm:

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
}
```

**ProductResponse** — Dữ liệu server trả về cho client:

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
}
```

> **Lưu ý:** `ProductRequest` không chứa trường `id` vì ID được server tự động sinh bằng UUID. `ProductResponse` bao gồm `id` để client có thể định danh sản phẩm.

### 3.4. Tầng Service — Business Logic và Circuit Breaker

Đây là tầng chứa toàn bộ logic nghiệp vụ và là nơi tích hợp mẫu thiết kế **Circuit Breaker**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @CircuitBreaker(name = "productService", fallbackMethod = "createProductFallback")
    public void createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product saved successfully: {}", product.getId());
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getAllProductsFallback")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .build())
                .toList();
    }

    // Fallback khi Circuit Breaker kích hoạt
    public void createProductFallback(ProductRequest request, Throwable t) {
        log.error("Fallback - Cannot create product: {}", t.getMessage());
        throw new RuntimeException("Product service is temporarily unavailable.");
    }

    public List<ProductResponse> getAllProductsFallback(Throwable t) {
        log.error("Fallback - Cannot fetch products: {}", t.getMessage());
        return Collections.emptyList();
    }
}
```

**Cơ chế Circuit Breaker hoạt động như sau:**

```
        Requests
            │
            ▼
    ┌──────────────┐     Thành công     ┌──────────┐
    │    CLOSED     │ ──────────────────→│ Xử lý   │
    │ (Hoạt động    │                    │ bình     │
    │  bình thường) │                    │ thường   │
    └──────┬───────┘                    └──────────┘
           │ Tỷ lệ lỗi ≥ 50%
           ▼
    ┌──────────────┐     Mọi request    ┌──────────┐
    │     OPEN      │ ──────────────────→│ Fallback │
    │ (Ngắt mạch)   │                    │ Method   │
    └──────┬───────┘                    └──────────┘
           │ Sau 10 giây
           ▼
    ┌──────────────┐     Cho phép       ┌──────────┐
    │  HALF-OPEN    │ ──5 request thử──→│ Kiểm tra │
    │ (Thử lại)     │                    │ kết quả  │
    └──────────────┘                    └──────────┘
```

Cấu hình Circuit Breaker trong `application.properties`:

| Tham số | Giá trị | Ý nghĩa |
|---|---|---|
| `sliding-window-size` | 10 | Xét 10 request gần nhất để tính tỷ lệ lỗi |
| `failure-rate-threshold` | 50% | Ngắt mạch khi ≥ 50% request thất bại |
| `wait-duration-in-open-state` | 10s | Đợi 10 giây trước khi chuyển sang HALF-OPEN |
| `permitted-number-of-calls-in-half-open-state` | 5 | Cho phép 5 request thử trong trạng thái HALF-OPEN |

### 3.5. Tầng Controller — REST API

```java
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest request) {
        productService.createProduct(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }
}
```

**Bảng tổng hợp API:**

| Method | Endpoint | Mô tả | Request Body | Response |
|---|---|---|---|---|
| `POST` | `/api/product` | Tạo sản phẩm mới | `{ name, description, price }` | HTTP 201 Created |
| `GET` | `/api/product` | Lấy danh sách tất cả sản phẩm | — | JSON Array `[{ id, name, description, price }]` |

---

## 4. Cấu hình hệ thống

### 4.1. Quản lý Dependencies (`pom.xml`)

Module kế thừa từ Parent POM (`vn.tt.practice:BE:0.0.1-SNAPSHOT`) để tận dụng các dependency chung đã khai báo sẵn (Lombok, Actuator, Eureka Client, Micrometer, Spring Security). Riêng module này khai báo thêm:

| Dependency | Scope | Vai trò |
|---|---|---|
| `spring-boot-starter-web` | compile | Xây dựng REST API |
| `spring-boot-starter-data-jpa` | compile | ORM framework (Hibernate) |
| `postgresql` | runtime | JDBC Driver kết nối PostgreSQL |
| `spring-cloud-starter-circuitbreaker-resilience4j` | compile | Circuit Breaker Pattern |

### 4.2. Cấu hình ứng dụng (`application.properties`)

```properties
# ── Thông tin service ──
spring.application.name=product-service      # Tên đăng ký với Eureka
server.port=8082                             # Cổng lắng nghe HTTP

# ── PostgreSQL ──
spring.datasource.url=jdbc:postgresql://localhost:5432/product_service_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# ── JPA / Hibernate ──
spring.jpa.hibernate.ddl-auto=update         # Tự tạo/cập nhật schema
spring.jpa.show-sql=true                     # Log câu SQL ra console
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Eureka ──
eureka.client.service-url.defaultZone=${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true

# ── Actuator & Monitoring ──
management.endpoints.web.exposure.include=health,prometheus
management.tracing.sampling.probability=1.0  # Trace 100% request
management.zipkin.tracing.endpoint=${ZIPKIN_URL:http://localhost:9411/api/v2/spans}
```

> Các giá trị nhạy cảm (URL, credential) đều hỗ trợ ghi đè bằng biến môi trường theo cú pháp `${ENV_VAR:default_value}`, giúp linh hoạt khi triển khai trên Docker hoặc Kubernetes.

### 4.3. Đóng gói Docker (`Dockerfile`)

Sử dụng chiến lược **multi-stage build** để tối ưu kích thước image:

```dockerfile
# ── Giai đoạn 1: Build ──
FROM maven:3.9-eclipse-temurin-21 AS build      # Image build (~800MB)
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml                  # Parent POM
COPY product-service/pom.xml /workspace/product-service/pom.xml
COPY product-service/src /workspace/product-service/src
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/product-service/pom.xml clean package -DskipTests

# ── Giai đoạn 2: Runtime ──
FROM eclipse-temurin:21-jre-alpine               # Image runtime (~190MB)
WORKDIR /app
COPY --from=build .../product-service-0.0.1-SNAPSHOT-exec.jar /app/product-service.jar
ENTRYPOINT ["java", "-jar", "/app/product-service.jar"]
EXPOSE 8082
```

**Ưu điểm của multi-stage build:**
- Image cuối cùng chỉ chứa JRE (không có JDK, Maven, source code) → kích thước giảm đáng kể (~190MB thay vì ~800MB).
- Mã nguồn không bị lộ ra trong image production.
- Reproducible build — kết quả build nhất quán bất kể môi trường host.

---

## 5. Tích hợp giám sát (Observability)

### 5.1. Health Check (Actuator)

Endpoint `/actuator/health` cung cấp trạng thái sức khỏe real-time của service, bao gồm:
- Kết nối PostgreSQL (DataSource health indicator)
- Trạng thái Circuit Breaker (Resilience4j health indicator)
- Dung lượng ổ đĩa (Disk Space health indicator)

```bash
GET http://localhost:8082/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": { "status": "UP" },
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP" }
  }
}
```

### 5.2. Prometheus Metrics

Endpoint `/actuator/prometheus` xuất các metrics theo định dạng Prometheus, bao gồm:
- **JVM metrics:** Heap memory, GC pause, thread count.
- **HTTP metrics:** Request count, latency (p50, p95, p99) per endpoint.
- **Database metrics:** Connection pool (HikariCP) active/idle/pending.
- **Circuit Breaker metrics:** State transitions, failure rate, call count.

### 5.3. Distributed Tracing (Zipkin)

Với `management.tracing.sampling.probability=1.0`, 100% request được gắn Trace ID và Span ID. Dữ liệu tracing được gửi đến Zipkin Server (`http://localhost:9411`), cho phép theo dõi luồng request xuyên suốt các microservice.

---

## 6. Kiểm thử và xác minh

### 6.1. Yêu cầu môi trường

Trước khi chạy, cần đảm bảo:

| Thành phần | Yêu cầu |
|---|---|
| PostgreSQL | Chạy tại `localhost:5432`, đã tạo database `product_service_db` |
| Eureka Server | Chạy tại `localhost:8761` (tùy chọn — service vẫn hoạt động nếu không có Eureka) |

### 6.2. Khởi chạy

```bash
# Từ thư mục gốc ProjectWeb-BE
mvn spring-boot:run -pl product-service
```

### 6.3. Kiểm thử API

**Tạo sản phẩm mới:**

```bash
curl -X POST http://localhost:8082/api/product \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 16 Pro",
    "description": "Smartphone Apple thế hệ mới",
    "price": 28990000
  }'
# → HTTP 201 Created
```

**Truy vấn danh sách sản phẩm:**

```bash
curl http://localhost:8082/api/product
# → HTTP 200 OK
```

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "iPhone 16 Pro",
    "description": "Smartphone Apple thế hệ mới",
    "price": 28990000
  }
]
```

**Kiểm tra health:**

```bash
curl http://localhost:8082/actuator/health
# → { "status": "UP", ... }
```

---

## 7. Kết luận

Module `product-service` đã được xây dựng hoàn chỉnh với kiến trúc phân tầng rõ ràng, tuân thủ các best practices của Spring Boot và Microservices:

1. **Tách biệt trách nhiệm:** Controller xử lý HTTP, Service chứa logic, Repository truy xuất dữ liệu.
2. **DTO Pattern:** Tách biệt cấu trúc dữ liệu nội bộ (Entity) và bên ngoài (API contract).
3. **Fault Tolerance:** Circuit Breaker đảm bảo service không bị cascading failure khi database gặp sự cố.
4. **Observability:** Health check, metrics và tracing sẵn sàng cho môi trường production.
5. **Container-ready:** Dockerfile multi-stage build cho phép triển khai nhanh chóng trên Docker/Kubernetes.

Service sẵn sàng tích hợp với các thành phần khác trong hệ thống thông qua Eureka Service Discovery và API Gateway.

---

**Công cụ hỗ trợ phát triển:** Claude AI (Vibe Coding)
**Ngày hoàn thành:** Tháng 4/2026
