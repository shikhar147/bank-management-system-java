package com.bank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int accountNumber;
    private final String holderName;
    private final String phone;
    private String pin;
    protected double balance;
    protected final List<Transaction> transactions = new ArrayList<>();

    protected Account(int accountNumber, String holderName, String phone, String pin, double openingBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.phone = phone;
        this.pin = pin;
        this.balance = openingBalance;
        transactions.add(new Transaction("ACCOUNT OPENED", openingBalance, balance));
    }

    public abstract String getType();

    /** Lowest balance this account may reach after a withdrawal. */
    protected abstract double getMinimumAllowedBalance();

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount, balance));
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (balance - amount < getMinimumAllowedBalance()) {
            throw new InsufficientFundsException(String.format(
                    "Insufficient funds. Available to withdraw: %.2f", balance - getMinimumAllowedBalance()));
        }
        balance -= amount;
        transactions.add(new Transaction("WITHDRAWAL", amount, balance));
    }

    /** Used by Bank.transfer so the history shows transfers, not plain deposits/withdrawals. */
    void transferOut(double amount, int toAcc) throws InsufficientFundsException {
        if (balance - amount < getMinimumAllowedBalance()) {
            throw new InsufficientFundsException(String.format(
                    "Insufficient funds. Available to transfer: %.2f", balance - getMinimumAllowedBalance()));
        }
        balance -= amount;
        transactions.add(new Transaction("TRANSFER OUT >" + toAcc, amount, balance));
    }

    void transferIn(double amount, int fromAcc) {
        balance += amount;
        transactions.add(new Transaction("TRANSFER IN <" + fromAcc, amount, balance));
    }

    public boolean checkPin(String input) { return pin.equals(input); }

    public void changePin(String newPin) { this.pin = newPin; }

    public int getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public String getPhone() { return phone; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }

    @Override
    public String toString() {
        return String.format("A/C No: %d | %-8s | Holder: %-20s | Phone: %s | Balance: %.2f",
                accountNumber, getType(), holderName, phone, balance);
    }
}
