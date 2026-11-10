# EBanking - Net Banking Portal

A Java EE net banking web application built with JSP, Servlets and JDBC on MySQL, developed in Eclipse for academic submission. It follows an MVC + DAO structure and covers customer banking, admin operations, fixed and recurring deposits, and background processing of maturities.

## Features

### Customer
- Register (the bank auto-generates the Customer ID, e.g. `CUST0001`)
- Log in with **Customer ID or Email** plus password
- View profile and update phone, email and password. Name, PAN and Aadhaar are read-only KYC fields.
- Deposit, withdraw and transfer funds, choosing from a dropdown of your own accounts
- Check balance and view transaction history per account
- Transfers show a soft warning if the balance falls below the minimum of 5000.00; they are not blocked
- **Fixed Deposits:** book, list, and withdraw early with a penalty
- **Recurring Deposits:** book, with automatic monthly debits, interest and a maturity payout

### Admin
- Separate admin login and session
- Create customer accounts (account numbers are auto-generated, e.g. `ACC00001`)
- Update customer contact details and delete customers (blocked while they still own accounts)
- Adjust an account balance manually, logged as an `ADMIN_ADJUSTMENT` transaction

### Background processing
- A scheduler runs every 30 seconds to:
  - mature Fixed Deposits and credit principal plus interest
  - debit due Recurring Deposit installments and pay out on the final one

## Tech Stack

| Layer | Technology |
|---|---|
| View | JSP |
| Controller | Java Servlets (`javax.servlet`, Servlet 4.0) |
| Data access | JDBC with the DAO pattern |
| Database | MySQL |
| Server | Apache Tomcat 9.0 |
| IDE | Eclipse (Dynamic Web Project) |
| Libraries | MySQL Connector/J, jBCrypt 0.4 |

> Tomcat 9 uses the `javax.servlet.*` namespace. Tomcat 10+ uses `jakarta.servlet.*` and would need imports changed.

## Project Structure

```
EBanking
├── src/main/java
│   ├── controller          Customer servlets (login, register, deposit, withdraw, transfer, FD, RD, profile...)
│   ├── controller.admin    Admin servlets (login, logout, add account, manage customers, update balance)
│   ├── dao                 JDBC data access (CustomerDao, AccountDao, TransactionDao, FixedDepositDao, ...)
│   ├── model               POJOs (Customer, Account, Transaction, FixedDeposit, RecurringDeposit, ...)
│   ├── filter              AuthFilter (customers), AdminAuthFilter (admin)
│   ├── listener            MaturityCheckerListener (FD/RD background job)
│   ├── exception           InsufficientBalanceException
│   └── utilities           ConnectionFactory, PasswordUtil (BCrypt), ValidationUtil
└── src/main/webapp
    ├── *.jsp               Customer pages and *Result.jsp result pages
    ├── admin/              Admin pages
    ├── css/ js/ images/ includes/
    └── WEB-INF
        ├── web.xml
        └── lib/            mysql-connector-j.jar, jbcrypt-0.4.jar
```

### Request flow
```
Browser -> Filter (auth check) -> Servlet -> DAO -> MySQL
                                     |
                                     +-> forward to JSP (view)
```
Pages that need data are reached through their servlet's `doGet()` (for example `/deposit`, `/profile`, `/bookFD`), not by opening the `.jsp` directly.

## Database Setup

1. Create the database:
```sql
CREATE DATABASE ebanking_db;
USE ebanking_db;
```

2. Create the tables:
```sql
CREATE TABLE customer (
    cid VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100),
    panno VARCHAR(20),
    aadhaarno VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_email UNIQUE (email)
);

CREATE TABLE account (
    accno VARCHAR(20) PRIMARY KEY,
    cid VARCHAR(20) NOT NULL,
    opendate DATE,
    balance DECIMAL(15,2) DEFAULT 0.00,
    accounttype VARCHAR(20),
    FOREIGN KEY (cid) REFERENCES customer(cid)
);

CREATE TABLE transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    saccno VARCHAR(20) NOT NULL,
    benaccno VARCHAR(20),
    amount DECIMAL(15,2) NOT NULL,
    transdt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(50),
    FOREIGN KEY (saccno) REFERENCES account(accno)
);

CREATE TABLE admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE fixed_deposit (
    fd_id INT AUTO_INCREMENT PRIMARY KEY,
    cid VARCHAR(20) NOT NULL,
    accno VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    no_of_years INT NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    book_date DATETIME NOT NULL,
    maturity_date DATETIME NOT NULL,
    status VARCHAR(25) DEFAULT 'ACTIVE',
    FOREIGN KEY (cid) REFERENCES customer(cid),
    FOREIGN KEY (accno) REFERENCES account(accno)
);

CREATE TABLE recurring_deposit (
    rd_id INT AUTO_INCREMENT PRIMARY KEY,
    cid VARCHAR(20) NOT NULL,
    accno VARCHAR(20) NOT NULL,
    monthly_amount DECIMAL(15,2) NOT NULL,
    no_of_months INT NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL DEFAULT 6.00,
    installments_paid INT DEFAULT 0,
    book_date DATETIME NOT NULL,
    next_debit_date DATETIME NOT NULL,
    status VARCHAR(25) DEFAULT 'ACTIVE',
    test_mode BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (cid) REFERENCES customer(cid),
    FOREIGN KEY (accno) REFERENCES account(accno)
);
```

3. Set your MySQL credentials in `utilities/ConnectionFactory.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/ebanking_db";
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

### Creating the first admin
Passwords are BCrypt-hashed, so a plain-text `INSERT` will not work. There is no admin self-registration page, so:
1. Register a temporary customer through `register.jsp` using the password you want for the admin.
2. Copy that customer's hash: `SELECT password FROM customer WHERE cid = 'CUST0001';`
3. Insert it as the admin: `INSERT INTO admin (username, password) VALUES ('admin1', '<paste hash>');`
4. Delete the temporary customer row.

## Running the Project

1. Import the project into Eclipse as a **Dynamic Web Project** (source folder `src/main/java`, content folder `src/main/webapp`).
2. Place `mysql-connector-j-x.x.x.jar` and `jbcrypt-0.4.jar` in `src/main/webapp/WEB-INF/lib`, then add both via **Build Path -> Add JARs** (not *External* JARs, which would not deploy with the app).
3. Configure an Apache Tomcat 9.0 runtime and run the project on the server. Do a full server restart if the background listener changes.
4. Open:
   - Customer: `http://localhost:8080/EBanking/register.jsp`, then `login.jsp`
   - Admin: `http://localhost:8080/EBanking/admin/adminLogin.jsp`

Typical first run: register a customer, log in as admin, create an account for that customer's CID, then log in as the customer to deposit, transfer and book deposits.

## How Deposits Work

### Fixed Deposit
- The booking amount is debited immediately and the FD is stored with a computed `maturity_date`.
- On maturity: `interest = principal x rate x years / 100` is credited back with the principal.
- **Early withdrawal:** the rate is reduced by a flat 1.00 percentage point (floored at 0), and interest is prorated to the time actually held.

### Recurring Deposit
- A fixed amount is debited every month. If the balance is short, that cycle is skipped and retried.
- On the final installment: `interest = P x n(n+1)/2 x r/1200`, and `(P x n) + interest` is credited.

### Test mode (for demos)
A checkbox on the FD and RD forms speeds up time-based behavior:
- FD: 1 year becomes 5 minutes
- RD: each monthly installment becomes 5 minutes

The mode is stored per record, so test and real deposits can run side by side.

## Security

- Passwords hashed with BCrypt, with salt included
- SQL injection prevention through `PreparedStatement` everywhere
- **Server-side ownership checks** (`isAccountOwnedByCustomer`) on all money-moving actions. Dropdowns are a UX convenience; the server check is the real security boundary.
- Separate customer and admin sessions and filters
- `FOR UPDATE` row locking and commit/rollback transactions so balance changes and transaction logs stay atomic
- `BigDecimal` for all money values

## Transaction Types

`DEPOSIT`, `WITHDRAW`, `TRANSFER`, `ADMIN_ADJUSTMENT`, `FD_BOOKED`, `FD_MATURED`, `FD_PREMATURE_WITHDRAWAL`, `RD_INSTALLMENT`, `RD_MATURED`

## Known Limitations / Roadmap

- `ValidationUtil` (email, phone, PAN, Aadhaar, amount parsing) is implemented but so far wired into registration only; the money servlets still rely on client-side checks
- Database connections are opened per call; connection pooling is not implemented
- Database credentials are hardcoded in `ConnectionFactory`
- Result pages use generic failure messages in most flows
- Planned features: OTP verification on login/transfer, beneficiary management, email/SMS notification simulation, mini statement / PDF download, bill payment simulation, loan module

## License

Academic project. Add a license of your choice before publishing.
