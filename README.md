# MediBook - Hệ thống Đặt lịch Khám Bệnh Online

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-green)
![Next.js](https://img.shields.io/badge/Next.js-13.5.1-black)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

**Nền tảng đặt lịch khám bệnh trực tuyến toàn diện**

[Features](#chức-năng) • [Tech Stack](#tech-stack) • [Getting Started](#cách-chạy) • [Documentation](#swagger)

</div>

---

## Giới thiệu

MediBook là hệ thống đặt lịch khám bệnh trực tuyến cho phép bệnh nhân dễ dàng tìm kiếm bác sĩ, đặt lịch hẹn, và quản lý lịch sử khám bệnh. Hệ thống cung cấp giao diện thân thiện cho cả bệnh nhân và bác sĩ, với các tính năng quản lý lịch làm việc, hồ sơ bệnh án, và đánh giá bác sĩ.

### Tính năng nổi bật

- 🔍 **Tìm kiếm bác sĩ** theo chuyên khoa, kinh nghiệm, chi phí
- 📅 **Đặt lịch hẹn** trực tuyến với thời gian thực
- 🏥 **Quản lý lịch làm việc** linh hoạt cho bác sĩ
- 📋 **Hồ sơ bệnh án** điện tử an toàn
- ⭐ **Đánh giá bác sĩ** sau khi khám
- 🔐 **Xác thực JWT** bảo mật cao
- 📧 **Thông báo email** tự động
- 📊 **Báo cáo thống kê** cho admin

---

## Tech Stack

### Backend

- **Java 17** - Ngôn ngữ lập trình
- **Spring Boot 3.5.3** - Framework chính
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - ORM với Hibernate
- **MySQL 8.0** - Database
- **JWT (jjwt 0.12.6)** - Xác thực token
- **Redis** - Caching
- **Flyway** - Database migration
- **MapStruct 1.6.3** - Object mapping
- **Lombok 1.18.38** - Reduce boilerplate
- **SpringDoc OpenAPI** - API documentation
- **Spring Boot Mail** - Email service

### Frontend

- **Next.js 13.5.1** - React framework
- **TypeScript** - Type safety
- **TailwindCSS 3.3.3** - Styling
- **Radix UI** - Component library
- **Axios** - HTTP client
- **React Hook Form** - Form management
- **TanStack Query** - Data fetching
- **Zod** - Schema validation
- **Lucide React** - Icons

### Testing

- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **JaCoCo** - Code coverage
- **Spring Security Test** - Security testing

---

## Kiến trúc

### Architecture Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (Next.js)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │ Pages    │  │ Components│  │ Hooks    │  │ Services │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/REST API
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Controller Layer                          │  │
│  │  - Request/Response handling                         │  │
│  │  - Validation                                        │  │
│  │  - Security annotations                              │  │
│  └──────────────────────────────────────────────────────┘  │
│                              │                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Service Layer                             │  │
│  │  - Business logic                                    │  │
│  │  - Transaction management                            │  │
│  │  - External service integration                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                              │                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Repository Layer                          │  │
│  │  - Data access                                       │  │
│  │  - JPA/Hibernate                                     │  │
│  │  - Custom queries                                   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│     MySQL      │  │     Redis       │  │  Email Service │
│   (Primary DB) │  │    (Cache)      │  │   (SMTP)       │
└────────────────┘  └─────────────────┘  └────────────────┘
```

### Design Patterns

- **Facade Pattern** - Simplify complex subsystems
- **Strategy Pattern** - Business rule validation
- **Specification Pattern** - Dynamic query building
- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Data transfer objects
- **Mapper Pattern** - Object mapping with MapStruct

---

## Chức năng

### Cho Bệnh nhân (Customer)

- **Đăng ký & Đăng nhập**
  - Đăng ký tài khoản với xác thực email
  - Đăng nhập bằng email/password
  - Quên mật khẩu & Reset mật khẩu
  - Refresh token & Logout all devices

- **Tìm kiếm Bác sĩ**
  - Tìm kiếm theo chuyên khoa
  - Lọc theo kinh nghiệm, chi phí, đánh giá
  - Xem hồ sơ bác sĩ chi tiết

- **Đặt lịch Hẹn**
  - Xem lịch trống của bác sĩ
  - Đặt lịch hẹn theo slot
  - Hủy lịch hẹn (trước 24h)
  - Đổi lịch hẹn (trước 24h)
  - Xem lịch sử đặt lịch

- **Hồ sơ Bệnh án**
  - Xem hồ sơ bệnh án cá nhân
  - Xem lịch sử khám bệnh

- **Đánh giá**
  - Đánh giá bác sĩ sau khi khám
  - Xem đánh giá của bác sĩ

### Cho Bác sĩ (Doctor)

- **Quản lý Lịch làm việc**
  - Thiết lập lịch làm việc hàng tuần
  - Đăng ký ngày nghỉ
  - Xem lịch hẹn của mình
  - Xem lịch hẹn hôm nay/tuần này

- **Quản lý Lịch hẹn**
  - Xác nhận lịch hẹn
  - Hoàn thành lịch hẹn
  - Đánh dấu không đến (No-show)
  - Xem lịch sử bệnh nhân

- **Hồ sơ Bệnh án**
  - Tạo hồ sơ bệnh án
  - Cập nhật hồ sơ bệnh án
  - Xem hồ sơ bệnh án bệnh nhân

- **Hồ sơ Cá nhân**
  - Cập nhật thông tin bác sĩ
  - Quản lý chuyên khoa

### Cho Quản trị viên (Admin)

- **Quản lý Người dùng**
  - Xem danh sách người dùng
  - Kích hoạt/Vô hiệu hóa tài khoản
  - Quản lý vai trò & quyền hạn

- **Quản lý Bác sĩ**
  - Tạo hồ sơ bác sĩ mới
  - Nâng cấp user thành bác sĩ
  - Xóa/Vô hiệu hóa bác sĩ

- **Quản lý Chuyên khoa**
  - Tạo/Cập nhật/Xóa chuyên khoa
  - Khôi phục chuyên khoa đã xóa

- **Quản lý Lịch hẹn**
  - Xem tất cả lịch hẹn
  - Tìm kiếm lịch hẹn nâng cao
  - Xem báo cáo thống kê

- **Audit Log**
  - Xem lịch sử thay đổi hệ thống

---

## Project Structure

```
medibook/
├── backend/                          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/medibook/
│   │   │   │   ├── common/          # Shared utilities
│   │   │   │   │   ├── constant/    # Constants
│   │   │   │   │   ├── enums/       # Enumerations
│   │   │   │   │   ├── exception/    # Custom exceptions
│   │   │   │   │   └── response/    # API responses
│   │   │   │   ├── config/          # Configuration
│   │   │   │   │   ├── security/    # Security config
│   │   │   │   │   └── cache/       # Cache config
│   │   │   │   ├── modules/         # Business modules
│   │   │   │   │   ├── appointment/  # Appointment module
│   │   │   │   │   ├── auth/         # Authentication module
│   │   │   │   │   ├── doctor/       # Doctor module
│   │   │   │   │   ├── medicalrecord/# Medical records
│   │   │   │   │   ├── review/       # Review module
│   │   │   │   │   ├── schedule/     # Schedule module
│   │   │   │   │   ├── specialty/    # Specialty module
│   │   │   │   │   ├── token/        # Refresh token module
│   │   │   │   │   ├── user/         # User module
│   │   │   │   │   ├── audit/        # Audit logging
│   │   │   │   │   ├── notification/  # Email service
│   │   │   │   │   └── reporting/    # Reports
│   │   │   │   └── security/        # Security components
│   │   │   │       ├── jwt/          # JWT utilities
│   │   │   │       ├── filter/       # Security filters
│   │   │   │       └── handler/      # Security handlers
│   │   │   └── resources/
│   │   │       ├── application.yml   # App configuration
│   │   │       └── db/migration/    # Flyway migrations
│   │   └── test/                    # Test files
│   │       └── java/com/medibook/
│   │           ├── modules/         # Module tests
│   │           └── MedibookApplicationTests.java
│   ├── pom.xml                      # Maven configuration
│   └── .env                         # Environment variables
├── frontend/                         # Next.js Frontend
│   ├── app/                         # Next.js app directory
│   │   ├── (auth)/                  # Auth pages
│   │   ├── dashboard/               # Dashboard pages
│   │   ├── doctor/                  # Doctor pages
│   │   ├── patient/                 # Patient pages
│   │   └── admin/                   # Admin pages
│   ├── components/                  # Reusable components
│   ├── lib/                         # Utility functions
│   ├── hooks/                       # Custom hooks
│   ├── services/                    # API services
│   ├── types/                       # TypeScript types
│   ├── package.json                 # NPM dependencies
│   ├── tailwind.config.ts           # Tailwind config
│   └── next.config.js               # Next.js config
├── docker/                          # Docker configurations
│   └── docker-compose.yml          # Redis container
├── .gitignore                       # Git ignore rules
└── README.md                        # This file
```

---

## Database

### Schema Overview

Hệ thống sử dụng MySQL với các bảng chính:

- **users** - Thông tin người dùng
- **roles** - Vai trò (CUSTOMER, DOCTOR, ADMIN)
- **permissions** - Quyền hạn
- **refresh_tokens** - Refresh tokens
- **specialties** - Chuyên khoa y tế
- **doctors** - Hồ sơ bác sĩ
- **doctor_working_patterns** - Lịch làm việc bác sĩ
- **doctor_time_offs** - Ngày nghỉ bác sĩ
- **appointments** - Lịch hẹn khám bệnh
- **appointment_status_history** - Lịch sử trạng thái lịch hẹn
- **medical_records** - Hồ sơ bệnh án
- **reviews** - Đánh giá bác sĩ
- **audit_logs** - Nhật ký thay đổi hệ thống

### Migration

Database migration được quản lý bằng **Flyway**:

```bash
# Migrations nằm ở: backend/src/main/resources/db/migration/
# V1__init_schema.sql
# V2__align_schema_with_jpa_entities.sql
# V3__add_unique_work_pattern.sql
# V4__remove_appointment_unique_and_create_lock.sql
```

### Soft Delete

Các bảng quan trọng sử dụng **soft delete** với cột `deleted_at`:
- users
- doctors
- specialties
- appointments
- medical_records
- doctor_working_patterns
- doctor_time_offs

---

## Environment

### Backend Environment Variables

Tạo file `.env` trong thư mục `backend/`:

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/medibook?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_super_secret_key_minimum_32_characters_long
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Server
SERVER_PORT=8080

# Frontend URL
FRONTEND_URL=http://localhost:3000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Email (optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### Frontend Environment Variables

Tạo file `.env.local` trong thư mục `frontend/`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

---

## Cách chạy

### Prerequisites

- **Đã cài đặt:** Java 17+, Maven, Node.js 18+, MySQL 8.0, Redis
- **IDE:** IntelliJ IDEA (khuyên dùng) hoặc VS Code

### Backend Setup

1. **Clone repository**
```bash
git clone https://github.com/your-username/medibook.git
cd medibook/backend
```

2. **Cấu hình database**
```bash
# Tạo database MySQL
mysql -u root -p
CREATE DATABASE medibook;
```

3. **Cấu hình environment**
```bash
# Copy file example
cp application-example.yml application.yml

# Chỉnh sửa application.yml với thông tin database của bạn
```

4. **Chạy Redis**
```bash
cd docker
docker-compose up -d
```

5. **Build & Run**
```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Hoặc run file JAR
java -jar target/medibook-0.0.1-SNAPSHOT.jar
```

Backend sẽ chạy tại: `http://localhost:8080`

### Frontend Setup

1. **Cài đặt dependencies**
```bash
cd frontend
npm install
```

2. **Cấu hình environment**
```bash
# Copy file example
cp .env.example .env.local

# Chỉnh sửa .env.local với API URL
```

3. **Run development server**
```bash
npm run dev
```

Frontend sẽ chạy tại: `http://localhost:3000`

### Production Build

**Backend:**
```bash
mvn clean package -Pprod
java -jar target/medibook-0.0.1-SNAPSHOT.jar
```

**Frontend:**
```bash
npm run build
npm start
```

---

## Swagger

API documentation có sẵn tại:

```
http://localhost:8080/swagger-ui.html
```

**Features:**
- Interactive API testing
- Request/Response examples
- Authentication support (Click "Authorize" button)
- Schema definitions
- Try it out functionality

---

## Docker

### Redis Container

Chạy Redis bằng Docker Compose:

```bash
cd docker
docker-compose up -d
```

**Services:**
- **Redis** - Caching server (port 6379)

**Volumes:**
- **redis-data** - Persistent Redis data

### Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f redis

# Restart services
docker-compose restart
```

---

## Testing

### Test Coverage

Project có **230+ tests** bao gồm:

- **150 Unit Tests** - Test business logic, service layer
- **80 Integration Tests** - Test API endpoints, database operations, security

### Run Tests

**Backend:**
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Frontend:**
```bash
# Run tests (nếu có)
npm test

# Type check
npm run typecheck

# Lint
npm run lint
```

### Test Structure

```
backend/src/test/java/com/medibook/
├── modules/
│   ├── auth/
│   │   ├── service/
│   │   │   └── AuthServiceTest.java
│   │   └── controller/
│   │       └── AuthControllerIntegrationTest.java
│   ├── appointment/
│   │   ├── service/
│   │   │   └── AppointmentServiceTest.java
│   │   └── controller/
│   │       └── AppointmentControllerIntegrationTest.java
│   ├── token/
│   │   └── service/
│   │       └── RefreshTokenServiceTest.java
│   ├── user/
│   │   └── service/
│   │       └── UserServiceTest.java
│   ├── doctor/
│   │   └── service/
│   │       └── DoctorServiceTest.java
│   ├── schedule/
│   │   └── service/
│   │       └── ScheduleServiceTest.java
│   ├── medicalrecord/
│   │   └── service/
│   │       └── MedicalRecordServiceTest.java
│   ├── review/
│   │   └── service/
│   │       └── ReviewServiceTest.java
│   └── specialty/
│       └── service/
│           └── SpecialtyServiceTest.java
└── MedibookApplicationTests.java
```

### Test Coverage Report

JaCoCo plugin đã được cấu hình trong `pom.xml`. Sau khi chạy tests:

```bash
mvn clean test jacoco:report
```

Report sẽ được tạo tại: `target/site/jacoco/index.html`

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Đăng ký tài khoản |
| POST | `/api/v1/auth/login` | Đăng nhập |
| POST | `/api/v1/auth/refresh-token` | Làm mới access token |
| POST | `/api/v1/auth/logout` | Đăng xuất |
| POST | `/api/v1/auth/logout-all` | Đăng xuất tất cả thiết bị |
| POST | `/api/v1/auth/change-password` | Đổi mật khẩu |
| POST | `/api/v1/auth/forgot-password` | Quên mật khẩu |
| POST | `/api/v1/auth/reset-password` | Reset mật khẩu |
| POST | `/api/v1/auth/verify-email` | Xác thực email |
| GET | `/api/v1/auth/me` | Lấy thông tin user hiện tại |

### Appointments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/appointments` | Đặt lịch hẹn |
| GET | `/api/v1/appointments/{id}` | Lấy chi tiết lịch hẹn |
| GET | `/api/v1/appointments/me` | Lấy lịch hẹn của tôi |
| PUT | `/api/v1/appointments/{id}/cancel` | Hủy lịch hẹn |
| PATCH | `/api/v1/appointments/{id}/confirm` | Xác nhận lịch hẹn |
| PATCH | `/api/v1/appointments/{id}/complete` | Hoàn thành lịch hẹn |
| PATCH | `/api/v1/appointments/{id}/no-show` | Đánh dấu không đến |
| PUT | `/api/v1/appointments/{id}/reschedule` | Đổi lịch hẹn |

### Doctors

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/doctors` | Tìm kiếm bác sĩ |
| GET | `/api/v1/doctors/{id}` | Lấy chi tiết bác sĩ |
| POST | `/api/v1/doctors` | Tạo hồ sơ bác sĩ (Admin) |
| PUT | `/api/v1/doctors/{id}` | Cập nhật bác sĩ |
| DELETE | `/api/v1/doctors/{id}` | Xóa bác sĩ (Admin) |

### Schedules

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/schedules/working-patterns` | Tạo lịch làm việc |
| PUT | `/api/v1/schedules/working-patterns/{id}` | Cập nhật lịch làm việc |
| DELETE | `/api/v1/schedules/working-patterns/{id}` | Xóa lịch làm việc |
| POST | `/api/v1/schedules/time-offs` | Đăng ký ngày nghỉ |
| GET | `/api/v1/schedules/doctor/{doctorId}` | Lấy lịch bác sĩ |
| POST | `/api/v1/schedules/slots` | Lấy slot trống |

---

## Troubleshooting

### Common Issues

**1. Database connection error**
```
Solution: Kiểm tra MySQL đang chạy và credentials trong application.yml
```

**2. Redis connection error**
```
Solution: Chạy Redis bằng docker-compose up -d
```

**3. JWT token expired**
```
Solution: Kiểm tra JWT_SECRET và expiration time trong config
```

**4. Flyway migration failed**
```
Solution: Kiểm tra version migration và clean database nếu cần
```

**5. Port already in use**
```
Solution: Thay đổi port trong application.yml hoặc kill process đang dùng port
```

---

## Contributing

1. Fork repository
2. Tạo branch feature (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

---

## License

Dự án này được cấp phép theo MIT License - xem file [LICENSE](LICENSE) để biết chi tiết.

---

## Contact

- **Project Name:** MediBook
- **Version:** 0.0.1-SNAPSHOT
- **Author:** [Your Name]
- **Email:** [your.email@example.com]

---

## Acknowledgments

- Spring Boot team
- Next.js team
- Radix UI components
- Open source community
