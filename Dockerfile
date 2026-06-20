# Bước 1: Sử dụng Image JDK 21 để chạy ứng dụng (Sử dụng Eclipse Temurin chuẩn doanh nghiệp)
FROM eclipse-temurin:21-jre-alpine

# Bước 2: Thiết lập thư mục làm việc bên trong Container
WORKDIR /app

# Bước 3: Copy file .jar đã được build từ thư mục target vào trong container
COPY target/fitness-nutrition-api-0.0.1-SNAPSHOT.jar app.jar

# Bước 4: Khai báo port mà ứng dụng sẽ chạy bên trong container
EXPOSE 8081

# Bước 5: Lệnh để khởi chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]