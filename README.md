# EBanking - Net Banking Portal

A Java EE net banking web application built with JSP, Servlets and JDBC on MySQL, developed in Eclipse. It follows an MVC + DAO structure and covers customer banking, admin operations, Fixed and Recurring Deposits, and automatic maturity processing.

## Features

### Customer
- **Register:** the bank generates the Customer ID (`CUST0001`, `CUST0002`, ...)
- **Log in** with Customer ID **or** email plus password, and log out
- **Profile:** update phone, email and (optionally) password. Name, PAN and Aadhaar are read-only KYC fields.
- **Multiple accounts per customer**, chosen from dropdowns
- **Deposit, withdraw and transfer.** A transfer that leaves less than the minimum balance of 5,000.00 shows a warning but is not blocked.
- **Balance and history:** dashboard account cards and per-account transaction history
- **Fixed Deposits:** book, list, and withdraw early with a penalty
- **Recurring Deposits:** book, automatic monthly debit, interest and a maturity payout

### Admin
- Separate admin login, logout, dashboard and session
- Create accounts (`ACC00001`, `ACC00002`, ...)
- Update customer phone and email; delete customers (blocked while they still own accounts)
- Manual balance adjustment, logged as an `ADMIN_ADJUSTMENT` transaction

### Background processing
A scheduler runs every 30 seconds to:
- mature Fixed Deposits and credit principal plus interest
- debit due Recurring Deposit installments and pay out on the final one

## Tech Stack

| Layer | Technology |
|---|---|
| View | JSP, HTML5, CSS3, a little JavaScript |
| Controller | Java Servlets (`javax.servlet`, Servlet 4.0) |
| Data access | JDBC with the DAO pattern |
| Database | MySQL (InnoDB) |
| Server | Apache Tomcat 9.0 |
| Java | JavaSE-21 (as configured in the Eclipse project) |
| Libraries | MySQL Connector/J, jBCrypt 0.4 |
| IDE | Eclipse (Dynamic Web Project) |

> Tomcat 9 uses the `javax.servlet.*` namespace. Tomcat 10+ uses `jakarta.servlet.*` and would need every import changed.

## Project Structure

```
EBanking
├── src/main/java
│   ├── controller          Customer servlets (login, register, deposit, withdraw, transfer, FD, RD, profile, ...)
│   ├── controller.admin    Admin servlets (login, logout, add account, manage customers, update balance)
│   ├── dao                 JDBC data access: Customer, Account, Transaction, FixedDeposit, RecurringDeposit, Admin
│   ├── model               POJOs: Customer, Account, Transaction, FixedDeposit, RecurringDeposit, Admin, TransferResult
│   ├── filter              AuthFilter (customers), AdminAuthFilter (admin)
│   ├── listener            MaturityCheckerListener (FD/RD background job)
│   ├── exception           InsufficientBalanceException
│   └── utilities           ConnectionFactory, PasswordUtil (BCrypt), ValidationUtil, ViewUtil
├── src/main/webapp
│   ├── *.jsp               Customer pages and *Result.jsp result pages
│   ├── admin/              Admin pages
│   ├── includes/           header.jsp, footer.jsp, result.jsp (shared layout)
│   ├── css/style.css       Single stylesheet for the whole UI
│   ├── images/ js/
│   └── WEB-INF
│       ├── web.xml
│       └── lib/            mysql-connector-j.jar, jbcrypt-0.4.jar
└── docs/                   Report diagrams (architecture, ER, DFD)
```

### Request flow
```
Browser -> Filter (session check) -> Servlet -> DAO -> MySQL
                                       |
                                       +-> forward to JSP (view)
```

Pages that need data are opened through their servlet's `doGet()` (for example `/deposit`, `/profile`, `/bookFD`), **not** by requesting the `.jsp` directly. A JSP opened directly redirects back to its servlet.

### UI structure
Every page is **guard -> header include -> content -> footer include**. The header takes `title`, `nav` (`public`, `customer` or `admin`) and `active` (which menu item to highlight) as parameters, so one header serves every page. Result pages (`depositResult.jsp`, `fdResult.jsp`, ...) all render through `includes/result.jsp`. User-supplied text is always printed through `ViewUtil.escape()`.

## Main Routes

| Customer | Purpose |
|---|---|
| `/register.jsp`, `/login.jsp` | Public pages |
| `/dashboard.jsp` | Accounts and quick actions |
| `/deposit`, `/withdraw`, `/transfer` | Money operations |
| `/viewTransactions`, `/checkBalance` | History and balance |
| `/bookFD`, `/myFDs`, `/prematureWithdrawFD` | Fixed Deposits |
| `/bookRD`, `/myRDs` | Recurring Deposits |
| `/profile`, `/logout` | Profile and logout |

| Admin | Purpose |
|---|---|
| `/admin/adminLogin.jsp` | Admin login |
| `/admin/adminDashboard.jsp` | Admin home |
| `/admin/account.jsp` | Create account (posts to `/addAccount`) |
| `/admin/accountUpd.jsp` | Update balance (posts to `/updateAccount`) |
| `/admin/users.jsp` | Update or delete customer (posts to `/manageCustomer`) |
| `/adminLogout` | Admin logout |

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
Do not commit a real password.

### Creating the first admin
Passwords are BCrypt-hashed, so a plain-text `INSERT` will not work. There is no admin self-registration page, so:
1. Register a temporary customer at `register.jsp` using the password you want for the admin (minimum 8 characters).
2. Copy its hash: `SELECT password FROM customer WHERE cid = 'CUST0001';`
3. Insert it as the admin: `INSERT INTO admin (username, password) VALUES ('admin1', '<paste hash>');`
4. Delete the temporary customer row.
5. Check the hash was stored cleanly, with no stray space. A valid hash is exactly 60 characters and starts with `$2a$10$`:
```sql
SELECT LEFT(password, 7) AS starts_with, LENGTH(password) AS len FROM admin;
```

To **reset a forgotten admin password**, generate a new hash the same way and run `UPDATE admin SET password = '<new hash>' WHERE admin_id = 1;`. Hashes cannot be reversed, so the old password cannot be recovered.

## Running the Project

1. Import it into Eclipse as a **Dynamic Web Project** (source folder `src/main/java`, content folder `src/main/webapp`).
2. Put `mysql-connector-j-x.x.x.jar` and `jbcrypt-0.4.jar` in `src/main/webapp/WEB-INF/lib`, then add both with **Build Path -> Add JARs**. Do not use *Add External JARs*, because those are not deployed with the app.
3. Configure an Apache Tomcat 9.0 runtime and run the project on the server. Restart the server fully after changing the listener or adding jars.
4. Open:
   - Home: `http://localhost:8080/EBanking/`
   - Admin: `http://localhost:8080/EBanking/admin/adminLogin.jsp`
5. If pages look unstyled after a change, hard-refresh with **Ctrl+F5** to clear the cached stylesheet.

**Typical first run:** register a customer, log in as admin, create an account for that Customer ID, then log in as the customer to deposit, transfer and book deposits.

## How Deposits Work

### Fixed Deposit
- The amount is debited immediately, and the FD is stored with a computed `maturity_date`.
- **At maturity:** `interest = principal x rate x years / 100`, credited together with the principal.
- **Early withdrawal:** the rate is reduced by a flat 1.00 percentage point (never below 0), and interest is prorated to the time actually held.
- Maturity and early withdrawal use one shared `calculateSimpleInterest()` method, so rounding is identical.

### Recurring Deposit
- A fixed amount is debited every month. If the balance is short, that cycle is skipped and retried on the next run.
- **On the final installment:** `interest = P x n(n+1)/2 x r/1200`, and `(P x n) + interest` is credited.

### Test mode (for demos)
A checkbox on the FD and RD forms compresses time:
- FD: 1 year becomes 5 minutes
- RD: each monthly installment becomes 5 minutes

The mode is stored **per record**, so a test deposit and a real one can run side by side.

## Validation Rules

Checked on the server, so they hold even if the browser checks are bypassed.

| Field | Rule |
|---|---|
| Phone | exactly 10 digits |
| PAN | format `ABCDE1234F` (stored uppercase) |
| Aadhaar | exactly 12 digits |
| Email | valid format, unique across customers |
| New password | at least 8 characters (existing shorter passwords can still log in) |
| Money amounts | above 0, at most 2 decimal places, up to 1,000,000,000 (balance and opening balance may be 0) |
| Interest rate | above 0 and up to 20 percent |
| FD term / RD term | 1 to 30 years / 1 to 120 months |
| Account open date | a real date, not in the future |
| Transfer | the beneficiary must be a valid account number, and different from the sender |

A rejected request shows a specific message on the result page (for example "Enter a valid amount greater than zero, with at most 2 decimal places.").

## Security

- Passwords hashed with salted **BCrypt**; an invalid stored hash fails the login instead of crashing
- **SQL injection** prevented with `PreparedStatement` everywhere
- **XSS** prevented by escaping all user-supplied output (`ViewUtil.escape`)
- **Ownership checks** on every money-moving action (`isAccountOwnedByCustomer`). Dropdowns are a convenience, and the server check is the real boundary.
- Separate customer and admin sessions, each with its own filter
- `FOR UPDATE` row locking plus commit/rollback, so user-initiated balance changes and their audit rows are saved atomically
- `BigDecimal` and `DECIMAL(15,2)` for all money values
- Server-side input validation (`ValidationUtil`)

## Transaction Types

`DEPOSIT`, `WITHDRAW`, `TRANSFER`, `ADMIN_ADJUSTMENT`, `FD_BOOKED`, `FD_MATURED`, `FD_PREMATURE_WITHDRAWAL`, `RD_INSTALLMENT`, `RD_MATURED`

## Known Limitations

- **Scheduler atomicity:** the maturity and installment jobs run their statements in auto-commit, so credit, status change and log are not yet one transaction per deposit. An uncaught exception inside the scheduled task would also stop future runs.
- **Refresh after POST:** result pages are shown by a forward, so refreshing may resubmit the form (the Post/Redirect/Get pattern is not used).
- **Concurrency:** opposite simultaneous transfers could deadlock (MySQL rolls one back). ID generators sort as text, which breaks after `CUST9999`.
- **Database access:** a new connection is opened per call (no pooling), and credentials are hardcoded.
- **Not implemented yet:** CSRF tokens, HTTPS, login-attempt lockout, session timeout configuration.
- **Interest:** simple interest only, with no compounding or TDS.

## Roadmap

- OTP verification on login and transfer
- Beneficiary management, and email/SMS notification simulation
- Mini statement and PDF download
- Bill payment simulation and a loan module
- Admin transaction report and account closure
- Connection pooling, externalised configuration, and automated tests

## License

Academic project. Add a license of your choice before publishing.
