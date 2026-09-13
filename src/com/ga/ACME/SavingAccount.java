package com.ga.ACME;

public class SavingAccount extends Account{
    public SavingAccount(String accountNumber, String password, IPasswordManager passwordManager){
        super(accountNumber,password,passwordManager);
    }

    @Override
    public String getAccountType() {
        return "Saving";
    }
}
