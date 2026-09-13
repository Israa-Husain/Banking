package com.ga.ACME;

public interface IPasswordManager {
    String hashPassword(String password);
    boolean verifyPassword(String password, String hash);
}
