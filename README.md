<div align="center">

<img src="docs/images/banner.png" alt="PayPal Wallet Banner" width="100%"/>

# 💳 PayPal Digital Wallet

### A Full-Stack, Enterprise-Grade Digital Wallet & Payment System

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.10-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Security-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

</div>

---

## 📖 Overview

**PayPal Digital Wallet** is a fully-featured, production-ready digital wallet and payment platform built with Spring Boot 3 and a modern glassmorphism dark-mode frontend. It simulates the core functionality found in real digital banking applications — from secure JWT-based authentication with Two-Factor Authentication (2FA), real-time money transfers with transaction limits, multi-currency support, to on-chain blockchain transaction recording and AI-powered fraud detection.

> **Built for:** Learning, portfolio demonstration, and as a solid foundation for real-world fintech applications.

---

## ✨ Features at a Glance

| Category | Features |
|---|---|
| 🔐 **Authentication** | JWT login/register, Two-Factor Authentication (TOTP), Password encryption (BCrypt) |
| 💸 **Transactions** | Send money, receive money, transaction history, daily & single-transaction limits |
| 🌍 **Multi-Currency** | Live currency exchange rates, USD / EUR / GBP / INR support |
| ⛓️ **Blockchain** | On-chain transaction recording, blockchain hash verification, wallet address |
| 🛡️ **Fraud Detection** | Real-time fraud scoring on transactions, alert system, admin fraud dashboard |
| 👑 **Admin Panel** | User management, fraud report overview, system-wide transaction stats |
| 🎨 **Modern UI** | Glassmorphism dark mode, responsive design, animated micro-interactions |
| 📧 **Email Service** | OTP emails for 2FA, transaction notifications via Spring Mail |

---

## 🏗️ Architecture

<div align="center">
<img src="docs/images/architecture.png" alt="System Architecture" width="90%"/>
</div>

### System Design

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client Layer (Browser)                      │
│   HTML5  │  Vanilla CSS (Glassmorphism)  │  JavaScript (ES6+)   │
└──────────────────────────┬──────────────────────────────────────┘
                           │  REST API (JSON)
┌──────────────────────────▼──────────────────────────────────────┐
│                   Spring Boot Application                        │
│                                                                  │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │ Controllers │  │   Services   │  │  Security Config (JWT) │  │
│  │  (REST API) │→ │(Business Lgc)│→ │  + Filter Chain        │  │
│  └─────────────┘  └──────────────┘  └────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────▼───────────────────────────────────┐  │
│  │              Repository Layer (Spring Data JPA)            │  │
│  └───────────────────────┬───────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────┘
                           │
        ┌──────────────────┼─────────────────┐
        │                  │                  │
┌───────▼──────┐  ┌────────▼───────┐  ┌──────▼───────┐
│  MySQL DB    │  │   Blockchain   │  │  SMTP Server  │
│ (Persistent) │  │    Ledger      │  │  (2FA Email)  │
└──────────────┘  └────────────────┘  └──────────────┘
```

---

## 🔐 Security Features

<div align="center">
<img src="docs/images/security.png" alt="Security Features" width="90%"/>
</div>

### JWT Authentication
- Stateless JWT tokens issued on login
- Token validation via `JwtAuthFilter` on every request
- Secure token signing with HMAC-SHA key

### Two-Factor Authentication (2FA)
- TOTP-based one-time passwords sent via email
- Enable/disable 2FA from the Security Settings page
- OTP expiry and validation handled server-side

### Password Security
- BCrypt password hashing with `PasswordEncoder`
- Spring Security's `SecurityFilterChain` protecting all endpoints
- Role-based access: `ROLE_USER` and `ROLE_ADMIN`

### Transaction Limits
- Configurable **daily transaction limit** per user
- Configurable **single transaction limit**
- Real-time progress tracking on the dashboard

---

## ⛓️ Blockchain Integration

Every transaction is cryptographically recorded on an internal blockchain ledger:

- Each block contains: `sender`, `receiver`, `amount`, `timestamp`, `previousHash`, `currentHash`
- SHA-256 proof-of-work hashing
- Blockchain hash is returned to the user after every successful transfer
- Wallet addresses are generated and stored per user

```java
// Each transaction is hashed and chained
Block block = new Block(sender, receiver, amount, previousHash);
block.setHash(SHA256(block.calculateData()));
blockchain.addBlock(block);
```

---

## 🌍 Multi-Currency Support

| Currency | Symbol | Description |
|----------|--------|-------------|
| USD | $ | US Dollar (base) |
| EUR | € | Euro |
| GBP | £ | British Pound |
| INR | ₹ | Indian Rupee |

- Live exchange rate conversion at transfer time
- Currency selector on the Transfer page
- Balances display converted amounts in real-time

---

## 🛡️ Fraud Detection System

The platform uses a **real-time transaction fraud scoring engine**:

- Rule-based risk scoring on every transaction
- Flags suspicious patterns (large single transfers, rapid repeated transfers)
- Fraud alerts visible to both user and admin
- Admin dashboard shows fraud statistics and flagged accounts

---

## 🖥️ Frontend Pages

| Page | Route | Description |
|------|-------|-------------|
| 🏠 Login | `/index.html` | JWT login + 2FA OTP entry |
| 📝 Register | `/register.html` | New user registration |
| 📊 Dashboard | `/dashboard.html` | Balance, limits, quick actions |
| 💸 Transfer | `/transfer.html` | Send money with currency selector |
| 📜 History | `/history.html` | Full transaction history with filters |
| 👤 Profile | `/profile.html` | User info and wallet address |
| 🔒 Security | `/security.html` | 2FA toggle, password change |
| 👑 Admin | `/admin.html` | Admin stats, fraud alerts, user mgmt |

---

## 📁 Project Structure

```
wallet/
├── src/
│   ├── main/
│   │   ├── java/com/paypal/wallet/
│   │   │   ├── config/          # Security, Password Encoder config
│   │   │   ├── controller/      # REST Controllers (Auth, Wallet, Admin...)
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── model/           # JPA Entities (User, Transaction, Block...)
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── security/        # JWT Filter & Token Util
│   │   │   ├── service/         # Business Logic Services
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── static/          # Frontend (HTML, CSS, JS)
│   │       │   ├── css/         # Glassmorphism stylesheets
│   │       │   ├── js/          # API calls, Auth, UI handlers
│   │       │   ├── dashboard.html
│   │       │   ├── transfer.html
│   │       │   ├── security.html
│   │       │   └── admin.html
│   │       └── application.properties
├── docs/
│   └── images/                  # README images
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** — [Download](https://openjdk.org/)
- **Maven 3.8+** — Bundled via `mvnw`
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)

### 1. Clone the Repository

```bash
git clone https://github.com/Dhilshan-codebox/Wallet_Process.git
cd Wallet_Process
```

### 2. Configure the Database

Create a MySQL database and update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wallet_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# JWT Secret
app.jwt.secret=your-256-bit-secret-key-here
app.jwt.expiration=86400000

# Mail (for 2FA OTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 3. Build & Run

```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

### 4. Access the Application

Open your browser and navigate to:

```
http://localhost:8080
```

**Default Admin Account** — Register first, then update the role in the database:
```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'your@email.com';
```

---

## 🔗 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login (returns JWT) |
| `POST` | `/api/auth/verify-otp` | Verify 2FA OTP |

### Wallet
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/wallet/balance` | Get user balance |
| `POST` | `/api/wallet/transfer` | Transfer funds |
| `GET` | `/api/wallet/transactions` | Transaction history |
| `GET` | `/api/wallet/limits` | Get transaction limits |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/stats` | System statistics |
| `GET` | `/api/admin/fraud-alerts` | All fraud alerts |
| `GET` | `/api/admin/users` | User management |

### Security
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/security/enable-2fa` | Enable 2FA |
| `POST` | `/api/security/disable-2fa` | Disable 2FA |
| `POST` | `/api/security/change-password` | Update password |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.5, Spring Security, Spring Data JPA |
| **Database** | MySQL 8, Hibernate ORM |
| **Security** | JWT (JJWT 0.11.5), BCrypt, Spring Security Filter Chain |
| **Frontend** | HTML5, Vanilla CSS (Glassmorphism), JavaScript ES6+ |
| **Email** | Spring Boot Mail (SMTP / Gmail) |
| **Build Tool** | Apache Maven |
| **Utilities** | Lombok, Bean Validation |

---

## 📸 Screenshots

| Dashboard | Transfer | Security Settings |
|-----------|----------|-------------------|
| Balance, limits & quick actions | Multi-currency transfer UI | 2FA enable/disable + password |

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ by [Dhilshan](https://github.com/Dhilshan-codebox)**

⭐ If you found this project useful, please give it a star!

</div>
