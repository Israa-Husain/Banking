package com.ga.ACME;

public class MasterCard extends Card{
    private static CardLimits limits = new CardLimits(5_000, 10_000, 20_000, 100_000, 200_000);

    public MasterCard(String cardNumber){
        super(cardNumber);
    }

    @Override
    public String getCardType() {
        return "MasterCard";
    }

    @Override
    public CardLimits getDailyLimits() {
        return limits;
    }
}
