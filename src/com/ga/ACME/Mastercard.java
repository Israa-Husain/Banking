package com.ga.ACME;

public class Mastercard extends Card{
    private static CardLimits limits = new CardLimits(5_000, 10_000, 20_000, 100_000, 200_000);

    public Mastercard(String cardNumber){
        super(cardNumber);
    }

    @Override
    public String getCardType() {
        return "Mastercard";
    }

    @Override
    public CardLimits getDailyLimits() {
        return limits;
    }
}
