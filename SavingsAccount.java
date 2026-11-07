package com.bank;

public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    public static final double MIN_BALANCE = 500.0;
    public static final double INTEREST_RATE = 0.04; // 4% per annum

    public SavingsAccount(int no, String name, String phone, String pin, double opening) {
        super(no, name, phone, pin, opening);
    }

    @Override public String getType() { return "SAVINGS"; }

    @Override protected double getMinimumAllowedBalance() { return MIN_BALANCE; }

    /** Adds one year of interest. */
    public double addInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
        transactions.add(new Transaction("INTEREST", interest, balance));
        return interest;
    }
}
