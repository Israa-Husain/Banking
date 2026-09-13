package com.ga.ACME;

public class MasterCardPlatinum extends Card{
    private static CardLimits limits = new CardLimits(20_000, 40_000, 80_000, 100_000, 200_000);

    public MasterCardPlatinum(String cardNumber){
        super(cardNumber);
    }

    @Override
    public String getCardType() {
        return "MasterCard Platinum";
    }

    @Override
    public CardLimits getDailyLimits() {
        return limits;
    }
}
