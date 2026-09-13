package com.ga.ACME;

public class OverdraftLimitException extends Exception{
    public OverdraftLimitException(String message){
        super(message);
    }
}
