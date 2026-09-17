package com.ga.ACME;

public class Banker extends Person{

    private final String bankerId;
    public Banker(String id, String bankerId,String name, String email, String phoneNumber, String password, IPasswordManager passwordManager){
            super(id, name, email, phoneNumber, password, passwordManager);
            this.bankerId = bankerId;
    }

    @Override
    public String getRole() {
        return "Banker";
    }

    public String getBankerId() {
        return bankerId;
    }
}
