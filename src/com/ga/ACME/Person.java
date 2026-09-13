package com.ga.ACME;

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
        this.passwordHash = passwordManager.hashPassword(password);
        this.passwordManager=passwordManager;
        this.isLocked = false;
        this.failedLoginAttempts = 0;
        this.lockTime = null;
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
        if(isLocked()){
//            long remainingSeconds = Long.parseLong(lockTime.format(DateTimeFormatter.ISO_DATE_TIME)) % 60;
            throw new AccountLockedException("Account locked"); //ADD THE REMAINING SECONDS FOR THE LOCKEDTIME
        }
        if(passwordManager.verifyPassword(password, passwordHash)){
            resetFailedAttempt();
            return true;
        }
        recordFailedAttempts();
        throw new InvalidCredentialsException("Incorrect password for "+id);
    }

    @Override
    public boolean isLocked() {
        if(isLocked&&lockTime!=null){ //REMOVE THIS CONDITION AND KEEP THE REST, CHECK IF IT WORKS
            LocalDateTime unlock = lockTime.plusSeconds(lockoutDuration);
            if(LocalDateTime.now().isAfter(unlock)){
                isLocked = false;
                lockTime = null;
                failedLoginAttempts = 0;
            }
        }
        return isLocked;
    }

    @Override
    public void recordFailedAttempts() {
        failedLoginAttempts++;
        if(failedLoginAttempts>=maxFailedAttempts){
            isLocked = true;
            lockTime = lockTime.plusSeconds(lockoutDuration); //LocalDateTime.now() ???
        }
    }

    @Override
    public void resetFailedAttempt() {
        failedLoginAttempts = 0;
        isLocked = false;
        lockTime = null;
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
