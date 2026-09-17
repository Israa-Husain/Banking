package com.ga.ACME;

public class CheckingAccount extends Account{
    public CheckingAccount(String accountNumber, double balance, String password, Card card, IPasswordManager passwordManager){
        super(accountNumber,balance,password,card,passwordManager);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }
}
