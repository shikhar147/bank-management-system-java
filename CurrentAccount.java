package com.bank;

public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    public static final double OVERDRAFT_LIMIT = 10000.0;

    public CurrentAccount(int no, String name, String phone, String pin, double opening) {
        super(no, name, phone, pin, opening);
    }

    @Override public String getType() { return "CURRENT"; }

    @Override protected double getMinimumAllowedBalance() { return -OVERDRAFT_LIMIT; }
}
