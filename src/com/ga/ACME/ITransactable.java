package com.ga.ACME;

public interface ITransactable {
    void withdraw(double amount) throws CardLimitExceededException,OverdraftLimitException,AccountDeactivatedException,InvalidAmountException;
    void deposit(double amount) throws CardLimitExceededException,OverdraftLimitException,AccountDeactivatedException,InvalidAmountException;
//    void transfer(double amount, boolean ownAccount) throws CardLimitExceededException,OverdraftLimitException,AccountDeactivatedException,InvalidAmountException;
}
