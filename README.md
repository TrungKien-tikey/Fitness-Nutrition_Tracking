# Fitness & Nutrition Tracking API

## 📝 Description
A RESTful Backend API system designed to track body metrics, automatically calculate nutritional requirements, and integrate AI to generate personalized diet plans based on users' fitness goals.

## 🚀 Core Features
* **Auth & Security:** Implements stateless registration, login, and security authorization using JSON Web Tokens (JWT).
* **Health Metrics Engine:** Receives physical data (age, height, weight, activity level) via API to automatically calculate and store BMI (Body Mass Index), BMR (Basal Metabolic Rate), and TDEE (Total Daily Energy Expenditure).
* **GenAI Diet Generation:** Packages body metrics and user goals (e.g., Muscle Gain / Weight Loss) into prompts, sending them to an AI model to retrieve a structured 7-day meal plan in standard JSON format.
* **Daily Tracking:** Provides APIs for users to "Check-in" daily meals and update their weight over time to generate progress charts.
* **Automated Scheduler:** A background process (Cron Job) that automatically scans the database at 20:00 daily to send notification reminders to users who haven't updated their data.

## 🛠️ Tech Stack
* **Core Platform & Language:** Java 25 (OpenJDK Eclipse Temurin 25.0.1 LTS), Spring Boot 3.x.
* **System Security:** Spring Security, JSON Web Token (JWT).
* **Database:** MySQL, Spring Data JPA, Hibernate (ORM).
* **GenAI Integration:** LangChain4j library, Google Gemini API.
* **Software Testing:** JUnit 5 and Mockito (for writing robust Unit Test scenarios).
* **Development Environment:** Visual Studio Code, Maven.

## 📂 Folder Structure
```text
src/main/java/com/yourname/fitnessapi
├── config/           # Configuration for Spring Security, JWT, LangChain4j, etc.
├── controller/       # REST Controllers (AuthController, MetricController, DietController)
├── dto/              # Data Transfer Objects (Request/Response payloads)
├── exception/        # Global Exception Handling mechanisms
├── model/            # Database Entities (User, HealthMetric, DietPlan, etc.)
├── repository/       # Spring Data JPA Repositories
├── security/         # Security filters, JWT utilities, and custom UserDetailsService
├── service/          # Core business logic (HealthCalculatorService, AiDietService)
└── scheduler/        # Background execution tasks (Cron Jobs)
