package com.ga.ACME;

public class CardLimitExceededException extends Exception{
    public CardLimitExceededException(String message){
        super(message);
    }
}
