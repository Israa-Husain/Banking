package com.ga.ACME;

public class MastercardTitanium extends Card{
    private static CardLimits limits = new CardLimits(10_000, 20_000, 40_000, 100_000, 200_000);

    public MastercardTitanium(String cardNumber){
        super(cardNumber);
    }

    @Override
    public String getCardType() {
        return "Mastercard Titanium";
    }

    @Override
    public CardLimits getDailyLimits() {
        return limits;
    }
}
