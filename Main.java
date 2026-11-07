package com.bank;

import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Bank bank = new Bank();

    public static void main(String[] args) {
        bank.load();
        boolean running = true;
        while (running) {
            System.out.println("\n=========== BANK MANAGEMENT SYSTEM ===========");
            System.out.println("1. Open New Account");
            System.out.println("2. Customer Login");
            System.out.println("3. Admin: View All Accounts");
            System.out.println("4. Admin: Close Account");
            System.out.println("5. Exit");
            switch (readInt("Choose: ")) {
                case 1: openAccount(); break;
                case 2: customerLogin(); break;
                case 3: viewAll(); break;
                case 4: closeAccount(); break;
                case 5: running = false; break;
                default: System.out.println("Invalid choice.");
            }
            bank.save();
        }
        System.out.println("Thank you for banking with us. Goodbye!");
    }

    // ---------- Main menu actions ----------
    private static void openAccount() {
        System.out.print("Full name: ");
        String name = sc.nextLine().trim();
        System.out.print("Phone: ");
        String phone = sc.nextLine().trim();
        if (name.isEmpty() || !phone.matches("\\d{10}")) {
            System.out.println("Name required and phone must be 10 digits.");
            return;
        }
        String pin = readPin("Set 4-digit PIN: ");
        int type = readInt("Account type (1 = Savings, 2 = Current): ");
        double opening = readDouble("Opening balance: ");
        try {
            Account acc = bank.openAccount(type == 1, name, phone, pin, opening);
            System.out.println("Account created! Your account number is " + acc.getAccountNumber());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void customerLogin() {
        int no = readInt("Account number: ");
        System.out.print("PIN: ");
        String pin = sc.nextLine().trim();
        Account acc = bank.login(no, pin);
        if (acc == null) {
            System.out.println("Invalid account number or PIN.");
            return;
        }
        System.out.println("Welcome, " + acc.getHolderName() + "!");
        accountMenu(acc);
    }

    private static void viewAll() {
        if (bank.getAllAccounts().isEmpty()) {
            System.out.println("No accounts yet.");
            return;
        }
        for (Account a : bank.getAllAccounts()) System.out.println(a);
        System.out.printf("Total deposits held: %.2f%n", bank.totalDeposits());
    }

    private static void closeAccount() {
        int no = readInt("Account number to close: ");
        System.out.println(bank.closeAccount(no) ? "Account closed." : "Account not found.");
    }

    // ---------- Customer menu ----------
    private static void accountMenu(Account acc) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- " + acc.getType() + " ACCOUNT " + acc.getAccountNumber() + " ---");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer Money");
            System.out.println("5. Mini Statement");
            System.out.println("6. Change PIN");
            if (acc instanceof SavingsAccount) System.out.println("7. Credit Annual Interest");
            System.out.println("0. Logout");
            int ch = readInt("Choose: ");
            try {
                switch (ch) {
                    case 1:
                        System.out.printf("Balance: %.2f%n", acc.getBalance());
                        break;
                    case 2:
                        acc.deposit(readDouble("Amount: "));
                        System.out.printf("Deposited. New balance: %.2f%n", acc.getBalance());
                        break;
                    case 3:
                        acc.withdraw(readDouble("Amount: "));
                        System.out.printf("Withdrawn. New balance: %.2f%n", acc.getBalance());
                        break;
                    case 4:
                        int to = readInt("Destination account number: ");
                        bank.transfer(acc, to, readDouble("Amount: "));
                        System.out.printf("Transfer successful. New balance: %.2f%n", acc.getBalance());
                        break;
                    case 5:
                        System.out.println("---------------- STATEMENT ----------------");
                        for (Transaction t : acc.getTransactions()) System.out.println(t);
                        break;
                    case 6:
                        acc.changePin(readPin("New 4-digit PIN: "));
                        System.out.println("PIN changed.");
                        break;
                    case 7:
                        if (acc instanceof SavingsAccount) {
                            double i = ((SavingsAccount) acc).addInterest();
                            System.out.printf("Interest credited: %.2f%n", i);
                            break;
                        }
                        System.out.println("Invalid choice.");
                        break;
                    case 0:
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (InsufficientFundsException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
            bank.save();
        }
    }

    // ---------- Input helpers ----------
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private static String readPin(String prompt) {
        while (true) {
            System.out.print(prompt);
            String pin = sc.nextLine().trim();
            if (pin.matches("\\d{4}")) return pin;
            System.out.println("PIN must be exactly 4 digits.");
        }
    }
}
