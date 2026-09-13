package com.ga.ACME;

public class CardLimits {
    private double withdrawLimit;
    private double transferLimit;
    private double ownAccountTransferLimit;
    private double depositLimit;
    private double ownAccountDepositLimit;

    public CardLimits(double withdrawLimit, double transferLimit, double ownAccountTransferLimit, double depositLimit, double ownAccountDepositLimit){
        this.withdrawLimit = withdrawLimit;
        this.transferLimit = transferLimit;
        this.ownAccountTransferLimit= ownAccountTransferLimit;
        this.depositLimit = depositLimit;
        this.ownAccountDepositLimit = ownAccountDepositLimit;
    }

    public boolean checkLimit(String operation, double amount){
        switch (operation){
            case (Card.withdraw):
                return amount <= withdrawLimit;
            case (Card.transfer):
                return amount <= transferLimit;
            case (Card.ownAccountTransfer):
                return amount <= ownAccountTransferLimit;
            case (Card.deposit):
                return amount <= depositLimit;
            case (Card.ownAccountDeposit):
                return amount <= ownAccountDepositLimit;
            default:
                throw new IllegalArgumentException("Unknown card operation" + operation);
        }
    }


    public double getWithdrawLimit() {
        return withdrawLimit;
    }

    public double getTransferLimit() {
        return transferLimit;
    }

    public double getOwnAccountTransferLimit() {
        return ownAccountTransferLimit;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    public double getOwnAccountDepositLimit() {
        return ownAccountDepositLimit;
    }
}
