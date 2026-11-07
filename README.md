# Bank Management System

**Author:** Shikhar Gupta

A Java banking application with a modern Swing graphical interface (frontend) and an object-oriented account engine (backend). Accounts and transactions are saved to disk automatically.

## Features

- Open Savings or Current accounts with a 4-digit PIN
- Secure customer login
- Deposit, withdraw and transfer money between accounts
- Full transaction history with money-in / money-out totals
- Savings accounts: minimum balance of 500 and 4% yearly interest
- Current accounts: overdraft limit of 10,000
- Admin tools (passcode protected): view all accounts, close an account
- Auto-logout after 2 minutes of inactivity
- Data saved automatically between runs

## Project structure

All source files are in the root of this repository and belong to the Java package `com.bank`.

| File | Role |
|------|------|
| `Account.java` | Backend: abstract base class for all accounts |
| `SavingsAccount.java` | Backend: minimum balance and interest |
| `CurrentAccount.java` | Backend: overdraft support |
| `Bank.java` | Backend: account management, transfers, saving and loading data |
| `Transaction.java` | Backend: one entry in an account's history |
| `InsufficientFundsException.java` | Backend: custom exception for failed withdrawals |
| `BankGUI.java` | Frontend: modern Swing GUI (main application) |
| `Main.java` | Frontend: original console version |

## Concepts used

Object-oriented programming (inheritance, abstraction, polymorphism, encapsulation), custom exceptions, Java serialization for persistence, Java Swing with custom-painted components.

## How to run (Eclipse)

1. File > New > Java Project, name it `BankApp`.
2. Right-click `src` > New > Package, name it `com.bank`.
3. Copy all `.java` files from this repository into that package.
4. Right-click `BankGUI.java` > Run As > Java Application.

To run the console version instead, run `Main.java`.

Requires JDK 11 or newer.

## Notes

- The admin passcode is set by `ADMIN_PASSCODE` at the top of `BankGUI.java` (default: `admin123`).
- Account data is stored in `bank_data.ser` next to the program. It is excluded from Git on purpose.

## Known limitations and future improvements

- Money is stored as `double`; a production system would use `BigDecimal` to avoid rounding errors.
- PINs are stored as plain text in the saved data file; they should be hashed.
- The admin passcode is hard-coded; it should come from configuration.
- The console version (`Main.java`) does not ask for the admin passcode.
- Data is saved with Java serialization; a database (for example SQLite) would be more robust.
