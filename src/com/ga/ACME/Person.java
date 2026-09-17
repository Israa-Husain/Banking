package com.ga.ACME;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Person implements IAuthenticatable{
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private boolean isLocked;
    private int failedLoginAttempts;
    private LocalDateTime lockTime;
    protected static final long lockoutDuration=60;
    protected static final int maxFailedAttempts=3;
    private IPasswordManager passwordManager;

    protected Person(String id, String name, String email, String phoneNumber, String password, IPasswordManager passwordManager){
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordManager = passwordManager;
        this.passwordHash = passwordManager.hashPassword(password);
//        this.isLocked = false;
//        this.failedLoginAttempts = 0;
//        this.lockTime = null;
    }

    public abstract String getRole(); //return Banker or Customer


    @Override
    public boolean authenticate(String password) {
        return passwordManager.verifyPassword(password, passwordHash);
    }

    public void changePassword(String newPassword){
        this.passwordHash = passwordManager.hashPassword(newPassword);
    }

    public boolean login(String password) throws AccountLockedException,InvalidCredentialsException{
        lockStatus();
        if(authenticate(password)){
            resetFailedAttempt();
            return true;
        }

        recordFailedAttempts();
        throw new InvalidCredentialsException("Incorrect password for "+id);
    }

    @Override
    public boolean isLocked() {
        if(isLocked&&lockTime!=null){
            LocalDateTime unlock = lockTime.plusSeconds(lockoutDuration);
            if(!LocalDateTime.now().isBefore(unlock)){ //Unlock when the full 60 seconds have passed
                resetFailedAttempt();
            }
        }
        return isLocked;
    }

    @Override
    public void recordFailedAttempts() {
        failedLoginAttempts++;
        if(failedLoginAttempts>=maxFailedAttempts){
            isLocked = true;
            lockTime = LocalDateTime.now();
        }
    }

    @Override
    public void resetFailedAttempt() {
        failedLoginAttempts = 0;
        isLocked = false;
        lockTime = null;
    }

    @Override
    public void lockStatus() throws AccountLockedException {
        if(isLocked()){
            long remaining = Math.max(1, Duration.between(LocalDateTime.now(), lockTime.plusSeconds(lockoutDuration)).getSeconds());
            throw new AccountLockedException("Account locked. Try again in "+remaining+" seconds");
        }
    }

    void restoreLoginState(int failedAttempts, boolean locked, LocalDateTime savedLockTime) {
        this.failedLoginAttempts = Math.max(0, failedAttempts);
        this.isLocked = locked;
        this.lockTime = savedLockTime;
        if (this.isLocked && (savedLockTime == null || !LocalDateTime.now().isBefore(savedLockTime.plusSeconds(lockoutDuration)))) {
            resetFailedAttempt();
        }
    }

    //GETTERS
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    } //NOT SURE TO KEEP IT OR NOT

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }
}
