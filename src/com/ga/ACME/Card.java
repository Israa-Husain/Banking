package com.ga.ACME;

import java.util.HashMap;
import java.util.Map;

public abstract class Card {
    private String cardNumber;
    private Map<String, Double> dailyUsage;
    public static final String withdraw = "WITHDRAW";
    public static final String transfer = "TRANSFER";
    public static final String ownAccountTransfer = "OWN_ACCOUNT_TRANSFER";
    public static final String deposit = "DEPOSIT";
    public static final String ownAccountDeposit = "OWN_ACCOUNT_DEPOSIT";

    protected Card(String cardNumber){ //ADD LIMITS? CREATE THE VARIABLE: PROTECTED FINAL CARDLIMITS LIMITS AND ASSIGN: THIS.LIMIT = LIMIT;
        this.cardNumber = cardNumber;
        this.dailyUsage = new HashMap<>();
    }

    public abstract String getCardType();
    public abstract CardLimits getDailyLimits();

    public String getCardNumber() {
        return cardNumber;
    }

    private boolean withinLimit(String operation, double amount){
        double usageInADay = dailyUsage.getOrDefault(operation, 0.0);
        double total = usageInADay + amount;
        return getDailyLimits().checkLimit(operation, total);
    }

    public boolean canWithdraw(double amount){
        return withinLimit(withdraw, amount);
    }

    public boolean canTransfer(double amount, boolean ownAccount){
        String operation = ownAccount ? ownAccountTransfer: transfer;
        return withinLimit(operation, amount);
    }

    public boolean canDeposit(double amount, boolean ownAccount){
        String operation = ownAccount ? ownAccountDeposit: deposit;
        return withinLimit(operation, amount);
    }

    public void recordUsage(String operation, double amount){
        dailyUsage.merge(operation, amount, Double::sum);
    }

    public void resetDailyUsage(){
        dailyUsage.clear();
    }
}
