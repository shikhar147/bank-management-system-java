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

```
src/com/bank/
  Backend (logic)
    Account.java                    Abstract base class for all accounts
    SavingsAccount.java             Minimum balance + interest
    CurrentAccount.java             Overdraft support
    Bank.java                       Account management, transfers, file saving
    Transaction.java                One entry in an account's history
    InsufficientFundsException.java Custom exception for failed withdrawals
  Frontend (interface)
    BankGUI.java                    Modern Swing GUI (main application)
    Main.java                       Original console version
```

## Concepts used

Object-oriented programming (inheritance, abstraction, polymorphism, encapsulation), custom exceptions, Java serialization for persistence, Java Swing with custom-painted components.

## How to run (Eclipse)

1. File > New > Java Project, name it `BankApp`.
2. Right-click `src` > New > Package, name it `com.bank`.
3. Copy all `.java` files from `src/com/bank/` into that package.
4. Right-click `BankGUI.java` > Run As > Java Application.

To run the console version instead, run `Main.java`.

Requires JDK 11 or newer.

## Notes

- The admin passcode is set by `ADMIN_PASSCODE` at the top of `BankGUI.java` (default: `admin123`). Change it before real use.
- Account data is stored in `bank_data.ser` next to the program. It is excluded from Git on purpose.
