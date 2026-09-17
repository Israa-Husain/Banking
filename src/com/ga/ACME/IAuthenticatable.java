package com.ga.ACME;

public interface IAuthenticatable {
    boolean isLocked();
    void resetFailedAttempt();
    void recordFailedAttempts();
    boolean authenticate(String password);
    boolean login(String password) throws AccountLockedException,InvalidCredentialsException;
    void lockStatus() throws AccountLockedException;

}
