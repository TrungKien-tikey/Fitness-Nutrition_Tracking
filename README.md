# Fitness & Nutrition Tracking API

[![CI](https://github.com/TrungKien-tikey/Fitness-Nutrition_Tracking/actions/workflows/ci.yml/badge.svg)](https://github.com/TrungKien-tikey/Fitness-Nutrition_Tracking/actions/workflows/ci.yml)

Hệ thống backend theo dõi chỉ số sức khỏe, tính toán nhu cầu dinh dưỡng và tạo thực đơn cá nhân hóa bằng AI dựa trên mục tiêu thể hình của người dùng.

## 1. Giới thiệu

Fitness & Nutrition Tracking API cung cấp nền tảng theo dõi sức khỏe và dinh dưỡng, trong đó:

- Người dùng có thể đăng ký, đăng nhập và xác thực bằng JWT.
- Hệ thống tự động tính BMI, BMR và TDEE từ chiều cao, cân nặng, tuổi, giới tính và mức vận động.
- Người dùng có thể tạo thực đơn 7 ngày bằng AI (Google Gemini) theo mục tiêu giảm cân, duy trì hoặc tăng cơ sau khi đã tính chỉ số sức khỏe.
- Người dùng check-in lượng calo nạp vào và cân nặng hằng ngày, xem lại lịch sử gần đây.
- Một tiến trình nền tự động phát hiện người dùng chưa check-in vào 20:00 mỗi ngày và ghi log nhắc nhở.
- Tài liệu API được cung cấp qua OpenAPI/Swagger UI và công bố trực tuyến qua GitHub Pages.

## 2. Chức năng chính

- Đăng ký tài khoản người dùng mới.
- Đăng nhập và xác thực người dùng.
- Bảo vệ các chức năng nghiệp vụ, chỉ người dùng đã đăng nhập mới sử dụng được.
- Tính toán và lưu chỉ số sức khỏe: BMI, BMR, TDEE.
- Tạo thực đơn 7 ngày bằng AI theo mục tiêu thể hình sau khi đã tính chỉ số sức khỏe.
- Lưu và xem lại thực đơn mới nhất của từng người dùng.
- Check-in dinh dưỡng hằng ngày.
- Xem lịch sử check-in theo số ngày gần đây.
- Tự động phát hiện người dùng chưa check-in vào 20:00 hằng ngày và ghi log nhắc nhở.
- Phân quyền người dùng và quản trị viên (`USER` / `ADMIN`), bảo vệ API quản trị.
- Quản trị viên xem danh sách người dùng, đổi vai trò và khóa/mở khóa tài khoản.
- Thống kê tổng quan hệ thống: người dùng, lượt check-in và thực đơn AI cho quản trị viên.

### Kiểm thử và tài liệu

- Kiểm thử đơn vị cho các xử lý nghiệp vụ và tiến trình nhắc nhở.
- Kiểm thử tích hợp cho luồng đăng ký/đăng nhập, bảo mật, nghiệp vụ chính và phân quyền quản trị.
- Bộ kiểm thử API bằng Postman cho toàn bộ chức năng, bao gồm trường hợp lỗi và xác thực.
- Tài liệu API hỗ trợ xem và thử gọi trực tiếp.

## 3. Công nghệ sử dụng

### Backend

- Java 17
- Spring Boot 3.4.1
- Spring Web
- Spring Data JPA
- Spring Security
- JWT (jjwt 0.12.5)
- Bean Validation
- Lombok
- Springdoc OpenAPI (Swagger UI)

### AI

- LangChain4j 0.36.2
- Google Gemini (`gemini-3.5-flash`)

### Hạ tầng và công cụ

- MySQL 8
- Docker Compose
- phpMyAdmin
- Maven Wrapper
- JUnit 5 và Mockito
- H2 (integration test)
- Postman (kiểm thử API)

### Frontend

- Không có frontend trong repository này; dự án cung cấp REST API.

## 4. Kiến trúc hệ thống

Hệ thống là một ứng dụng Spring Boot dạng monolith phân tầng. Request đi qua JWT filter, vào controller, xuống service, rồi tới repository và MySQL. Riêng tính năng tạo thực đơn, service gọi Google Gemini để sinh JSON thực đơn 7 ngày.

![Sơ đồ kiến trúc hệ thống](./docs/images/KTHTFitness.png)

### Cấu trúc package

| Package | Chức năng |
|---|---|
| `config` | Cấu hình Spring Security, JWT, OpenAPI, LangChain4j |
| `controller` | REST Controllers cho auth, health metrics, diet plans, daily logs, admin |
| `dto` | Request/Response và enum (`ActivityLevel`, `FitnessGoal`) |
| `entity` | Thực thể JPA: `User`, `HealthMetric`, `DietPlan`, `DailyLog`; enum `Role` |
| `repository` | Spring Data JPA repositories |
| `security` | JWT filter và token provider |
| `service` | Xử lý nghiệp vụ và tích hợp AI |
| `scheduler` | Tiến trình nhắc nhở check-in hằng ngày |
| `exception` | Xử lý lỗi toàn cục |

### Mô hình dữ liệu

| Bảng | Quan hệ | Mô tả |
|---|---|---|
| `users` | 1:1 `health_metrics`, 1:N `diet_plans`, 1:N `daily_logs` | Tài khoản, vai trò `USER`/`ADMIN`, trạng thái khóa |
| `health_metrics` | thuộc 1 `user` | Chiều cao, cân nặng, BMI, BMR, TDEE |
| `diet_plans` | thuộc 1 `user` | Mục tiêu và thực đơn 7 ngày (JSON) |
| `daily_logs` | thuộc 1 `user` | Calo nạp vào và cân nặng theo ngày |

## 5. Hướng dẫn cài đặt và chạy

### Yêu cầu

- Java 17
- Docker Desktop (tùy chọn, để chạy MySQL và ứng dụng bằng container)
- Git

### Cấu hình

Các giá trị nhạy cảm được tách khỏi code và nạp từ file `.env` (đã nằm trong `.gitignore`):

1. Sao chép file mẫu:

   ```bash
   copy .env.example .env      # Windows
   cp .env.example .env        # macOS/Linux
   ```

2. Điền giá trị vào `.env` (để trống nếu chưa có key):

   ```properties
   DB_URL=jdbc:mysql://localhost:3306/fitness_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   DB_USERNAME=root
   DB_PASSWORD=your_password
   MYSQL_ROOT_PASSWORD=your_password
   GEMINI_API_KEY=your_gemini_api_key
   GEMINI_MODEL=gemini-3.5-flash
   JWT_SECRET=your_jwt_secret
   JWT_EXPIRATION=86400000
   ```

3. Nếu chưa có `application.properties`, sao chép từ file mẫu:

   ```bash
   copy src\main\resources\application.properties.example src\main\resources\application.properties
   ```

`application.properties` đọc các biến từ `.env` qua dòng `spring.config.import=optional:file:.env[.properties]`. Khi chạy bằng Docker Compose, các biến `DB_USERNAME`, `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `GEMINI_API_KEY`, `GEMINI_MODEL`, `JWT_SECRET`, `JWT_EXPIRATION` được truyền vào container qua `docker-compose.yml`. Riêng `DB_URL` không được truyền vào container vì Compose cố định `SPRING_DATASOURCE_URL` trỏ tới service `fitness-db`; `DB_URL` chỉ dùng khi chạy backend trực tiếp bằng Maven.

> Không commit `.env` hoặc `application.properties`. Chỉ commit các file `.example`.

### Khởi chạy hạ tầng

Chỉ khởi chạy hạ tầng (MySQL và phpMyAdmin) để chạy backend trực tiếp bằng Maven:

```bash
docker compose up -d fitness-db fitness-phpmyadmin
```

Hoặc khởi chạy toàn bộ hệ thống bằng Docker (cần build JAR trước, xem mục "Chạy bằng Docker"):

```bash
docker compose up -d --build
```

Khi volume MySQL được khởi tạo lần đầu, database được tạo từ [`src/main/resources/init-db.sql`](src/main/resources/init-db.sql). Nếu volume đã tồn tại, script không chạy lại. Tài khoản demo có sẵn: `intern_test` / `secret123` (USER) và `admin` / `secret123` (ADMIN).

Các địa chỉ truy cập:

| Thành phần | Địa chỉ |
|---|---|
| Backend API | http://localhost:8081 |
| phpMyAdmin | http://localhost:8085 |
| MySQL | `localhost:3306` |

`docker-compose.yml` không đặt mật khẩu mặc định. Cần khai báo trong `.env`:

```text
MySQL database: fitness_db
MySQL username: root
MYSQL_ROOT_PASSWORD: mật khẩu root MySQL
DB_PASSWORD: mật khẩu backend dùng để kết nối MySQL
             (nên đặt giống MYSQL_ROOT_PASSWORD)
```

### Chạy backend trực tiếp bằng Maven

Trên Windows:

```bash
mvnw.cmd spring-boot:run
```

Trên macOS/Linux:

```bash
./mvnw spring-boot:run
```

Ứng dụng chạy tại:

```text
http://localhost:8081
```

### Chạy bằng Docker

Dockerfile sử dụng file JAR đã build sẵn, nên cần build trước:

```bash
mvnw.cmd clean package
docker compose up -d --build
```

### Chạy kiểm thử

```bash
mvnw.cmd verify
```

`verify` chạy unit test bằng Surefire và integration test bằng Failsafe. Muốn chỉ chạy unit test, dùng `mvnw.cmd test`.

Cấu trúc test:

- `src/test/java/.../unit/`: unit test, chạy bởi Surefire.
- `src/test/java/.../integration/`: integration test, chạy bởi Failsafe.

### Kiểm thử API bằng Postman

Bộ collection nằm tại [`postman/Fitness_Nutrition.postman_collection.json`](postman/Fitness_Nutrition.postman_collection.json), gồm 5 folder: `Auth`, `Health Metrics`, `Diet Plans`, `Daily Logs`, `Admin`.

1. Mở Postman, chọn **Import** và trỏ tới file collection trên, cùng file environment [`postman/Local.postman_environment.json`](postman/Local.postman_environment.json).
2. Chọn environment **Fitness Local** ở góc trên phải Postman. Nếu không chọn environment, token chỉ lưu tạm trong collection variable và không hiện ở mục Environment.
3. Đảm bảo backend đang chạy tại `base_url` (mặc định `http://localhost:8081`).
4. Chạy toàn bộ collection (Runner) hoặc từng request/folder tùy ý.

Mỗi folder cần xác thực (`Health Metrics`, `Diet Plans`, `Daily Logs`) có request `Login` đầu tiên. Folder `Admin` có `Login_Admin` và `Login_NormalUser` để kiểm tra cả trường hợp được phép và bị từ chối. Sau khi Login thành công, token được lưu vào Collection Variable và Environment Variable tương ứng, nên các request sau tự dùng token mà không cần copy tay. Collection cũng có auto-login dự phòng khi `bearer_token` đang trống.

Thông tin tài khoản kiểm thử (giá trị giả, chỉ dùng cho môi trường local):

```text
username: test_user
password: Test@12345
email:    test_user@example.com

admin username: admin
admin password: secret123
```

> Mật khẩu trên là tài khoản test cục bộ, không phải thông tin thật nên không cần ẩn. Nếu muốn tách khỏi collection, có thể chuyển `test_username`, `test_password`, `test_email` sang Postman Environment và không commit file environment đó.

> Folder `Diet Plans` cần `GEMINI_API_KEY` hợp lệ trong `.env` để tạo thực đơn. Test `Generate` kỳ vọng `200` kèm thực đơn đúng 7 ngày; nếu key sai/hết hạn hoặc model không còn hỗ trợ, test sẽ fail để phát hiện lỗi cấu hình.

### Tài liệu API

- **Tài liệu tương tác trực tuyến (không cần chạy server):**
  https://trungkien-tikey.github.io/Fitness-Nutrition_Tracking/
- **OpenAPI specification:** [`docs/openapi.yaml`](docs/openapi.yaml)
- **Swagger UI khi backend đang chạy:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON khi backend đang chạy:** http://localhost:8081/v3/api-docs

> Trang GitHub Pages được phục vụ từ thư mục `docs/` và nạp `openapi.yaml` vào Swagger UI. Nút "Try it out" chỉ gọi được API khi backend được deploy công khai và cấu hình CORS cho domain Pages.

### Danh sách API

| Method | Endpoint | Xác thực | Chức năng |
|---|---|---|---|
| `POST` | `/api/auth/register` | Không | Đăng ký tài khoản |
| `POST` | `/api/auth/login` | Không | Đăng nhập, nhận JWT |
| `POST` | `/api/health-metrics/calculate` | Bearer | Tính BMI, BMR, TDEE |
| `POST` | `/api/diet-plans/generate` | Bearer | Tạo thực đơn 7 ngày bằng AI (cần có health metrics trước) |
| `GET` | `/api/diet-plans/latest` | Bearer | Lấy thực đơn mới nhất |
| `POST` | `/api/daily-logs/check-in` | Bearer | Check-in dinh dưỡng hằng ngày |
| `GET` | `/api/daily-logs/history?days=7` | Bearer | Xem lịch sử check-in |
| `GET` | `/api/admin/users?page=0&size=10` | Bearer + ADMIN | Danh sách người dùng, phân trang/tìm kiếm |
| `PUT` | `/api/admin/users/{id}/role` | Bearer + ADMIN | Đổi vai trò người dùng |
| `PUT` | `/api/admin/users/{id}/status` | Bearer + ADMIN | Khóa/mở khóa tài khoản |
| `GET` | `/api/admin/stats` | Bearer + ADMIN | Thống kê tổng quan hệ thống |

## 6. Kết quả, hạn chế và hướng phát triển

### Kết quả đạt được

- Xây dựng backend Spring Boot với đầy đủ luồng đăng ký, đăng nhập và xác thực JWT.
- Hoàn thành chức năng tính toán và lưu chỉ số BMI, BMR, TDEE.
- Tích hợp Google Gemini để sinh thực đơn 7 ngày dạng JSON.
- Hoàn thành chức năng check-in dinh dưỡng và xem lịch sử.
- Tích hợp scheduler phát hiện người dùng chưa check-in lúc 20:00 và ghi log nhắc nhở.
- Phân quyền `USER` / `ADMIN`, bảo vệ API quản trị bằng `@PreAuthorize`.
- Quản trị viên xem danh sách người dùng có phân trang/tìm kiếm, đổi vai trò và khóa/mở khóa tài khoản.
- Thống kê tổng quan hệ thống: người dùng, lượt check-in, thực đơn AI và số người chưa check-in hôm nay.
- Có 58 unit test cho service, bảo mật, scheduler và xử lý lỗi bằng JUnit 5 và Mockito.
- Có 13 integration test cho luồng đăng ký/đăng nhập, bảo mật, nghiệp vụ chính và phân quyền quản trị bằng H2 và MockMvc.
- Có workflow CI chạy test tự động trên GitHub Actions.
- Có xử lý lỗi toàn cục và validate dữ liệu đầu vào.
- Có tài liệu OpenAPI/Swagger UI, công bố trực tuyến qua GitHub Pages.
- Đóng gói được ứng dụng bằng Docker và Docker Compose.

### Hạn chế hiện tại

- Ứng dụng vẫn là monolith, chưa tách thành microservices.
- Chưa có frontend; người dùng cần dùng Swagger UI hoặc client riêng.
- Chưa có chức năng xem/cập nhật hồ sơ cá nhân; thông tin tài khoản chỉ được tạo khi đăng ký.
- Kênh nhắc nhở mới dừng ở ghi log, chưa gửi email/thông báo thực tế.
- Chức năng AI phụ thuộc vào API key và dịch vụ Google Gemini bên ngoài.
- Chưa có refresh token và phân quyền chi tiết theo permission.
- Chưa có audit log cho các thao tác quản trị.
- Chưa phân biệt rõ mã lỗi 401/403/404; phần lớn lỗi nghiệp vụ trả về 400.
- Dockerfile cần file JAR build sẵn, chưa build đa tầng (multi-stage build).
- Chưa có quy trình CD (tự động deploy); CI mới dừng ở chạy test.

### Hướng phát triển trong tương lai

- Bổ sung frontend để người dùng thao tác trực quan.
- Bổ sung chức năng xem và cập nhật hồ sơ cá nhân.
- Gửi nhắc nhở qua email hoặc thông báo thay vì chỉ ghi log.
- Mở rộng phân quyền chi tiết theo permission và bổ sung audit log cho quản trị viên.
- Thêm refresh token và thu hồi token.
- Chuyển các giá trị nhạy cảm sang secret manager.
- Tăng độ phủ unit test và bổ sung integration test cho luồng tạo thực đơn bằng AI.
- Bổ sung CD (tự động deploy) và mở rộng quy trình CI.
- Dùng multi-stage build cho Docker image.
- Bổ sung phân trang cho các danh sách còn lại, cache và giới hạn tần suất request.
- Hoàn thiện phân tích tiến độ và gợi ý dinh dưỡng bằng AI.
