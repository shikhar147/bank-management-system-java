package com.bank;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Bank {
    private static final String DATA_FILE = "bank_data.ser";

    private Map<Integer, Account> accounts = new TreeMap<>();
    private int nextAccountNumber = 1001;

    public Account openAccount(boolean savings, String name, String phone, String pin, double opening) {
        if (savings && opening < SavingsAccount.MIN_BALANCE) {
            throw new IllegalArgumentException("Savings account needs a minimum opening balance of "
                    + SavingsAccount.MIN_BALANCE);
        }
        if (opening < 0) throw new IllegalArgumentException("Opening balance cannot be negative.");

        int no = nextAccountNumber++;
        Account acc = savings ? new SavingsAccount(no, name, phone, pin, opening)
                              : new CurrentAccount(no, name, phone, pin, opening);
        accounts.put(no, acc);
        return acc;
    }

    public Account find(int accountNumber) {
        return accounts.get(accountNumber);
    }

    public Account login(int accountNumber, String pin) {
        Account acc = find(accountNumber);
        return (acc != null && acc.checkPin(pin)) ? acc : null;
    }

    public void transfer(Account from, int toAccNo, double amount) throws InsufficientFundsException {
        Account to = find(toAccNo);
        if (to == null) throw new IllegalArgumentException("Destination account not found.");
        if (to == from) throw new IllegalArgumentException("Cannot transfer to the same account.");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        from.transferOut(amount, toAccNo);
        to.transferIn(amount, from.getAccountNumber());
    }

    public boolean closeAccount(int accountNumber) {
        return accounts.remove(accountNumber) != null;
    }

    public Collection<Account> getAllAccounts() { return accounts.values(); }

    public double totalDeposits() {
        double sum = 0;
        for (Account a : accounts.values()) sum += a.getBalance();
        return sum;
    }

    // ---------- Persistence ----------
    @SuppressWarnings("unchecked")
    public void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            nextAccountNumber = in.readInt();
            accounts = (Map<Integer, Account>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load saved data: " + e.getMessage());
        }
    }

    public void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeInt(nextAccountNumber);
            out.writeObject(accounts);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }
}
