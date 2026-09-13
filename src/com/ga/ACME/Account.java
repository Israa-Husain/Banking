package com.ga.ACME;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Account implements IAuthenticatable,ITransactable{
    private String accountNumber;
    private double balance;
    private boolean isActive = true;
    private boolean isLocked;

    private String passwordHash;
    private IPasswordManager passwordManager;
    private int failedLoginAttempts;
    private LocalDateTime lockTime;
    protected static final long lockoutDuration=60;
    protected static final int maxFailedAttempts=3;
    private double unpaidFees;
    private int overdraftCount=0;

    private List<Transaction> transactionList = new ArrayList<>();

    protected Account(String accountNumber, String password, IPasswordManager passwordManager){
        this.accountNumber = accountNumber;
        this.passwordManager = passwordManager;
        this.passwordHash = passwordManager.hashPassword(password);
        this.balance = 0;
        this.isActive = true;
    }

    public abstract String getAccountType();

    public boolean verifyPassword(String password){
        return passwordManager.verifyPassword(password,passwordHash);
    }

    public void changePassword(String newPassword){
        this.passwordHash = passwordManager.hashPassword(newPassword);
    }

    @Override
    public boolean authenticate(String password) {
        return passwordManager.verifyPassword(password,passwordHash);
    }

    @Override
    public void recordFailedAttempts() {
        failedLoginAttempts++;
        if(failedLoginAttempts>=maxFailedAttempts){
            isLocked = true;
            lockTime = lockTime.plusSeconds(lockoutDuration);
        }
    }

    public boolean login(String password) throws AccountLockedException,InvalidCredentialsException{
        if(isLocked()){
//            long remainingSeconds = Long.parseLong(lockTime.format(DateTimeFormatter.ISO_DATE_TIME)) % 60;
            throw new AccountLockedException("Account locked"); //ADD THE REMAINING SECONDS FOR THE LOCKEDTIME
        }
        if(passwordManager.verifyPassword(password, passwordHash)){
            resetFailedAttempt();
            return true;
        }
        recordFailedAttempts();
        throw new InvalidCredentialsException("Incorrect password");
    }

    @Override
    public boolean isLocked() {
        if(isLocked&&lockTime!=null){ //REMOVE THIS CONDITION AND KEEP THE REST, CHECK IF IT WORKS
            LocalDateTime unlock = lockTime.plusSeconds(lockoutDuration);
            if(LocalDateTime.now().isAfter(unlock)){
                isLocked = false;
                lockTime = null;
                failedLoginAttempts = 0;
            }
        }
        return isLocked;
    }

    @Override
    public void resetFailedAttempt() {
        failedLoginAttempts = 0;
        isLocked = false;
        lockTime = null;
    }

    protected void checkAccountActivity() throws AccountDeactivatedException{
        if(!isActive) throw new AccountDeactivatedException("Account is deactivated");
    }

    public void amountValidation(double amount) throws InvalidAmountException{
        if(amount<=0 || !Double.isFinite(amount)) throw new InvalidAmountException("Invalid amount");
    }

    private void addTransaction(TransactionType type, double amount){


        transactionList.add(new Transaction("",accountNumber,type,amount,LocalDateTime.now(),balance));
                                      //String transactionId, String accountNumber, TransactionType type, double amount, LocalDateTime timestamp, double resultingBalance
                                     //FIGURE OUT A WAY TO GET TRANSACTIONID VALUE FROM TRANSACTION
    }


    @Override
    public void withdraw(double amount) throws CardLimitExceededException,OverdraftLimitException,InvalidAmountException,OverdraftLimitException,AccountDeactivatedException {
        checkAccountActivity();
        amountValidation(amount);

        if(balance<0 && amount>100){
            throw new OverdraftLimitException("you can not withdraw more than $100 while having a negative balance");
        }

        double money = balance - amount;
        if(money<0){
            overdraftCount++;
            balance = money;
            unpaidFees +=35;
            addTransaction(TransactionType.withdraw, amount);
            balance-=35;
            addTransaction(TransactionType.overdraftFee,35);

            if(overdraftCount>=2){
                isActive = false;
            }
            return;
        }
        balance=money;
        addTransaction(TransactionType.withdraw,amount);
    }

    @Override
    public void deposit(double amount) throws CardLimitExceededException, OverdraftLimitException, AccountDeactivatedException,InvalidAmountException {
        checkAccountActivity();
        amountValidation(amount);

        balance+=amount;
        if(balance>0 && unpaidFees>0){
            double payment = Math.min(balance, unpaidFees);
            balance-=payment;
            unpaidFees-=payment;
            addTransaction(TransactionType.overdraftFee,payment);
        }
        addTransaction(TransactionType.deposit, amount);
    }

    public void chargeOverdraftFees() throws OverdraftLimitException{
        if(balance>0 && unpaidFees>0){
            double payment = Math.min(balance, unpaidFees);
            balance-=payment;
            unpaidFees-=payment;
            addTransaction(TransactionType.overdraftFee,payment);
        }
    }

//    @Override
//    public void transfer(double amount, boolean ownAccount) throws CardLimitExceededException, OverdraftLimitException, AccountDeactivatedException,InvalidAmountException {
//        checkAccountActivity();
//        amountValidation(amount);
//        String operation= ownAccount? deposit(amount) : withdraw(amount);
//
//
//
//
//    }

    public void transferOut(double amount) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException {
        withdraw(amount);
    }
    public void transferIn(double amount) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException {
        deposit(amount);
    }

    public String accountState(){
        StringBuilder sb = new StringBuilder();
        sb.append("Account: ").append(accountNumber);
        sb.append("\nBalance: ").append(balance);
        sb.append("\nStatus: ").append(isActive? "Active":"Deactivated");
        sb.append("\nTransactions: ");
        transactionList.forEach(t-> sb.append(t));

        return sb.toString();
    }

    //GETTERS
    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
