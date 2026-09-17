package com.ga.ACME;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String transactionId;
    private String accountNumber;
    private TransactionType type;
    private double amount;
    private LocalDateTime timestamp;
    private double resultingBalance;

    public Transaction(String transactionId, String accountNumber, TransactionType type, double amount, LocalDateTime timestamp, double resultingBalance){
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.resultingBalance = resultingBalance;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getResultingBalance() {
        return resultingBalance;
    }

    public String toFile() {
        return String.join("|", transactionId, accountNumber, type.name(), String.valueOf(amount), timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), String.valueOf(resultingBalance));
    }

    public static Transaction fromFile(String line) {
        String[] l = line.split("\\|", -1);
        if (l.length != 6){
            throw new IllegalArgumentException("Invalid transaction record");
        }
        return new Transaction(l[0], l[1], TransactionType.valueOf(l[2]), Double.parseDouble(l[3]), LocalDateTime.parse(l[4], DateTimeFormatter.ISO_LOCAL_DATE_TIME), Double.parseDouble(l[5]));
    }

    @Override
    public String toString() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " | " + type + " | $" + String.format("%.2f", amount) + " | Balance: $" + String.format("%.2f", resultingBalance);
    }

}
