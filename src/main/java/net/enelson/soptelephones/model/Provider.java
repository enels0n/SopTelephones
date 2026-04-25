package net.enelson.soptelephones.model;

public final class Provider {
    private final String id;
    private String displayName;
    private double smsPrice;
    private double balance;

    public Provider(String id, String displayName, double smsPrice, double balance) {
        this.id = id;
        this.displayName = displayName;
        this.smsPrice = smsPrice;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public double getSmsPrice() {
        return smsPrice;
    }

    public void setSmsPrice(double smsPrice) {
        this.smsPrice = smsPrice;
    }

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }
}

