package com.ga.ACME;

public class SavingAccount extends Account{
    public SavingAccount(String accountNumber, double balance, String password, Card card, IPasswordManager passwordManager){
        super(accountNumber,balance,password,card,passwordManager);
    }

    @Override
    public String getAccountType() {
        return "Saving";
    }
}
