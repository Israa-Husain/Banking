package com.ga.ACME;

public class CheckingAccount extends Account{
    public CheckingAccount(String accountNumber, String password, IPasswordManager passwordManager){
        super(accountNumber,password,passwordManager);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }
}
