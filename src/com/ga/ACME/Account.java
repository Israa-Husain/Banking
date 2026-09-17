package com.ga.ACME;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private Card card;

    private List<Transaction> transactionList = new ArrayList<>();

    protected Account(String accountNumber, double balance, String password, Card card,IPasswordManager passwordManager){
        this.accountNumber = accountNumber;
        this.card = card;
        this.balance = balance;
        this.passwordManager = passwordManager;
        this.passwordHash = passwordManager.hashPassword(password);
//        this.balance = 0;
//        this.isActive = true;
    }

    public void statement(boolean isActive, int overdraftCount, double unpaidFees, List<Transaction> transactions){
        this.isActive = isActive;
        this.overdraftCount = overdraftCount;
        this.unpaidFees = unpaidFees;
        this.transactionList.clear();
        this.transactionList.addAll(transactions);
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
    public void lockStatus() throws AccountLockedException {
        if(isLocked()){
            long remaining = Math.max(1, Duration.between(LocalDateTime.now(), lockTime.plusSeconds(lockoutDuration)).getSeconds());
            throw new AccountLockedException("Account locked. Try again in "+remaining+" seconds");
        }
    }

    @Override
    public void recordFailedAttempts() {
        failedLoginAttempts++;
        if(failedLoginAttempts>=maxFailedAttempts){
            isLocked = true;
            lockTime = LocalDateTime.now();
        }
    }

    public boolean login(String password) throws AccountLockedException,InvalidCredentialsException{
        lockStatus();
        if(authenticate(password)){
            resetFailedAttempt();
            return true;
        }
        recordFailedAttempts();
        throw new InvalidCredentialsException("Incorrect password for account "+accountNumber);
    }

    @Override
    public boolean isLocked() {
        if (isLocked && lockTime != null && !LocalDateTime.now().isBefore(lockTime.plusSeconds(lockoutDuration))) {
            resetFailedAttempt();
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
        transactionList.add(new Transaction(UUID.randomUUID().toString(),accountNumber,type,amount,LocalDateTime.now(),balance));
    }


    @Override
    public void withdraw(double amount) throws CardLimitExceededException,OverdraftLimitException,InvalidAmountException,OverdraftLimitException,AccountDeactivatedException {
        checkAccountActivity();
        amountValidation(amount);

        if(balance<0 && amount>100){
            throw new OverdraftLimitException("you can not withdraw more than $100 while having a negative balance");
        }

        if(!card.canWithdraw(amount)){
            throw new CardLimitExceededException("Daily withdrawal limit exceeded");
        }

        double money = balance - amount;
        if(money<0){
            overdraftCount++;
            balance = money;
            unpaidFees +=35;
            addTransaction(TransactionType.withdraw, amount);
            card.recordUsage(Card.withdraw, amount);
            balance-=35;
            addTransaction(TransactionType.overdraftFee,35);

            if(overdraftCount>=2){
                isActive = false;
            }
            return;
        }
        balance=money;
        card.recordUsage(Card.withdraw,amount);
        addTransaction(TransactionType.withdraw,amount);
    }


    public void deposit(double amount, boolean ownAccount) throws CardLimitExceededException, OverdraftLimitException, AccountDeactivatedException,InvalidAmountException {
        //checkAccountActivity();
        amountValidation(amount);

        String operation = ownAccount? Card.ownAccountDeposit : Card.deposit;
        if(!card.canDeposit(amount, ownAccount)){
            throw new CardLimitExceededException("Daily deposit limit exceeded");
        }
        balance+=amount;
        card.recordUsage(operation,amount);

        if(balance>0 && unpaidFees>0){
            double payment = Math.min(balance, unpaidFees);
            balance-=payment;
            unpaidFees-=payment;
            addTransaction(TransactionType.overdraftFee,payment);
        }
        if(balance>=0 && unpaidFees==0){
            isActive = true;
        }
        addTransaction(TransactionType.deposit, amount);
    }

    @Override
    public void deposit(double amount) throws CardLimitExceededException, OverdraftLimitException, AccountDeactivatedException, InvalidAmountException {
        deposit(amount,false);
    }

    public void chargeOverdraftFees() throws OverdraftLimitException{
        if(balance>0 && unpaidFees>0){
            double payment = Math.min(balance, unpaidFees);
            balance-=payment;
            unpaidFees-=payment;
            addTransaction(TransactionType.overdraftFee,payment);
        }
    }


    public void transferOut(double amount, boolean ownAccount) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException {
//        withdraw(amount);
        checkAccountActivity();
        amountValidation(amount);

        if(!card.canTransfer(amount,ownAccount)){
            throw new CardLimitExceededException("Daily transfer limit exceede");
        }

        if(balance<0 && amount>100){
            throw new OverdraftLimitException("You can not transfer more than $100 with a negative balance");
        }

        double money = balance-amount;
        if(money<0){
            overdraftCount++;
            balance= money;
            unpaidFees+=35;
            addTransaction(TransactionType.transfer,amount);
            card.recordUsage(ownAccount? Card.ownAccountTransfer: Card.transfer, amount);
            balance-=35;
            addTransaction(TransactionType.overdraftFee, 35);
            if(overdraftCount>=2){
                isActive=false;
            }
            return;
        }
        balance=money;
        card.recordUsage(ownAccount? Card.ownAccountTransfer: Card.transfer,amount);
        addTransaction(TransactionType.transfer,amount);

    }
    public void transferIn(double amount,boolean ownAccount) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException {
//        deposit(amount);
        checkAccountActivity();
        amountValidation(amount);

        if(!card.canDeposit(amount,ownAccount)){
            throw new CardLimitExceededException("Daily deposit limit exceede");
        }

        balance+=amount;
        card.recordUsage(ownAccount? Card.ownAccountDeposit:Card.deposit,amount);
        if(balance>0 && unpaidFees>0){
            double payment= Math.min(balance,unpaidFees);
            balance-=payment;
            unpaidFees-=payment;
            addTransaction(TransactionType.overdraftFee,payment);
        }

        if(balance>0 && unpaidFees==0){
            isActive=true;
        }
        addTransaction(TransactionType.transfer,amount);
    }

    public void addTransferOutTransaction(double amount){
        transactionList.add(new Transaction(UUID.randomUUID().toString(),accountNumber,TransactionType.transfer,amount,LocalDateTime.now(),balance));
    }

    public void addTransferInTransaction(double amount){
        transactionList.add(new Transaction(UUID.randomUUID().toString(),accountNumber,TransactionType.transfer,amount,LocalDateTime.now(),balance));
    }

    public String accountState(){
        StringBuilder sb = new StringBuilder();
        sb.append("Account: ").append(accountNumber).append("\n");
        sb.append(getAccountType());
        sb.append("\nBalance: $").append(balance);
        sb.append("\nStatus: ").append(isActive? "Active":"Deactivated");
        sb.append("\nTransactions: ");
        transactionList.forEach(t-> sb.append(" ").append(t).append("\n"));

        return sb.toString();
    }

    void restoreLoginState(int failedLoginAttempts, boolean isLocked, LocalDateTime savedLockTime){
        this.failedLoginAttempts = Math.max(0,failedLoginAttempts);
        this.isLocked=isLocked;
        this.lockTime = savedLockTime;
        if(this.isLocked && (savedLockTime==null || !LocalDateTime.now().isBefore(savedLockTime.plusSeconds(lockoutDuration)))){
            resetFailedAttempt();
        }
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

    public Card getCard() {
        return card;
    }

    public int getOverdraftCount() {
        return overdraftCount;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public double getUnpaidFees() {
        return unpaidFees;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
