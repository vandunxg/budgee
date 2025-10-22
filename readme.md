# Budgee Backend (Spring Boot)

A personal finance and group expense management system designed to help users track transactions, budgets, and goals — featuring wallet management, category-based tracking, and group expense sharing with sponsor/advance logic.

---

## 🚀 Overview

**Budgee** is a multi-module Java Spring Boot application that provides a robust backend API for personal and shared finance tracking. The project is built with scalability, modularity, and clean architecture principles — suitable for real-world fintech or expense management systems.

Current build phase: **Up to Group Management Module**

---

## 🧩 Core Features Implemented

### 1. **User & Authentication**

* User registration & login (JWT-based security planned)
* Audit tracking with `BaseEntity` (createdAt, updatedAt)
* Ownership validation with `OwnerEntity` and helper utilities (`Helpers.checkIsOwner`)

### 2. **Wallet Management**

* Multiple wallet types: `CASH`, `BANK_ACCOUNT`, `DIGITAL_WALLET`, `CREDIT_CARD`
* Track balance changes with automatic recalculation logic
* Utility methods to update balance on transaction create/update/delete
* Entity: `Wallet`

### 3. **Transaction System**

* Create/update/delete personal transactions
* Support for both `INCOME` and `EXPENSE` types
* Source classification via enum `ExpenseSource`: `PERSONAL`, `GROUP`
* Automatic balance adjustments on wallet updates

### 4. **Category Management**

* Categorize transactions (e.g., Food, Bills, Salary)
* Each category linked to the user for personalization
* Default system categories (non-deletable) planned for next phase

### 5. **Goal Tracking (Savings)**

* Track savings goals by amount and target date
* Link goals to specific wallets and categories (`GoalWallet`, `GoalCategory`)
* Automatic current amount update based on transactions

### 6. **Group Management (Completed)**

* Create groups with name, creator, and joined members
* `GroupMember` entity includes:

  * `GroupRole` (CREATOR, MEMBER)
  * `balanceOwed`, `joinedAt`, `memberName`
* Group transactions with split logic by members
* Flags for each transaction user share:

  * `isSponsor` → paid on behalf of group
  * `isAdvance` → fronted money for others
* Validation logic: only **one** of `isSponsor` or `isAdvance` can be true

---

## 🧱 Technologies Used

| Layer          | Tech Stack                                         |
| -------------- | -------------------------------------------------- |
| **Language**   | Java 17                                            |
| **Framework**  | Spring Boot 3.x (Spring MVC, JPA, Validation, AOP) |
| **Database**   | MySQL 8.x                                          |
| **ORM**        | Hibernate / JPA                                    |
| **Build Tool** | Maven                                              |
| **Logging**    | SLF4J + Logback                                    |
| **Validation** | Jakarta Validation API                             |

---

## 🧠 Architecture Overview

```
com.budgee
├── controller       # REST Controllers (Transaction, Wallet, Group)
├── service          # Business logic + Transactional methods
├── model            # Entities & Enums
├── payload          # Request & Response DTOs
├── repository       # Spring Data JPA Repositories
├── util             # Utility classes (ResponseUtil, Helpers, etc.)
└── exception        # Custom exception & global handler
```

---

## 🧰 Utilities & Helpers

* **`ResponseUtil`** → Unified response format (`DataResponse`, `ErrorResponse`)
* **`BudgeeException`** → Custom runtime exception
* **`GlobalExceptionHandler`** → Centralized error handling
* **`Helpers.checkIsOwner()`** → Ownership enforcement per entity

---

## 🔮 Next Development Phases

* JWT Authentication & Refresh Token flow
* Budget Limit per Category
* Shared Expense Settlement Logic
* Recurring Transactions
* Subscription-based premium features
* Docker & CI/CD deployment setup

---

## 📁 Setup & Run

### Prerequisites

* Java 17+
* MySQL 8+
* Maven 3.9+

### Steps

```bash
git clone https://github.com/vandunxg/budgee.git
cd budgee
cp .env.example .env   # configure DB credentials
mvn clean install
mvn spring-boot:run
```

### Default Local Server

```
http://localhost:8080
```

---

## 🧑‍💻 Author

**Nguyễn Văn Dũng**
Java Backend Developer | Spring Boot | MySQL | REST API Design
📧 [[vandunxg@duck.com](mailto:vandunxg@duck.com)]
