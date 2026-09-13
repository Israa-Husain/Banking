package com.ga.ACME;

public class AccountLockedException extends Exception{
    public AccountLockedException(String message){
        super(message);
    }
}
