package com.bank;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime time;

    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("%-20s | %-16s | %12.2f | Balance: %12.2f",
                time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                type, amount, balanceAfter);
    }
}
