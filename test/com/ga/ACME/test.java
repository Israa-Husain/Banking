package com.ga.ACME;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.Assert;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class test {
    private IPasswordManager passwordManager = new passwordEncryptor();
    private passwordEncryptor pe = new passwordEncryptor();

    @Test
    @DisplayName("login succeed when the correct password is entered")
    public final void loginSucceedWhenTheCorrectPasswordIsEntered(){
        passwordEncryptor pe = new passwordEncryptor();
        Banker banker = new Banker("B101","B101","Test","Test@acme.com","33221144","Test@123",pe);

        Assert.assertTrue(banker.authenticate("Test@123"));
        Assert.assertFalse(!banker.authenticate("Test@123"));
    }

    @Test
    public final void loginWithInvalidCredentials(){
        Banker banker = new Banker("B101","B101","Test","Test@acme.com","33221144","Test@123",pe);

        assertThrows(InvalidCredentialsException.class, ()-> banker.login("wrong"));
        assertEquals(1,banker.getFailedLoginAttempts());
    }



    @Test
    @DisplayName("accounts locked after 3 failed login attempts")
    public final void accountLockedAfter3FailedLoginAttempts(){
        Banker banker = new Banker("B101","B101","Test","Test@acme.com","33221144","Test@123",pe);

        for(int i=0;i<3;i++){
            assertThrows(InvalidCredentialsException.class, ()->banker.login("wrong"));
        }
        assertTrue(banker.isLocked());
        assertEquals(3,banker.getFailedLoginAttempts());

    }

    @Test
    public final void attemptLoginDuringLock(){
        Banker banker = new Banker("B101","B101","Test","Test@acme.com","33221144","Test@123",pe);

        banker.recordFailedAttempts();
        banker.recordFailedAttempts();
        banker.recordFailedAttempts();
        assertThrows(AccountLockedException.class, ()->banker.login("Test@123"));
    }

    @Test
    public final void loginAllowedAfter1Minute() throws AccountLockedException, InvalidCredentialsException {
        Banker banker = new Banker("B101","B101","Test","Test@acme.com","33221144","Test@123",pe);

        banker.restoreLoginState(3,true, LocalDateTime.now().minusSeconds(61));
        assertTrue(banker.login("Test@123"));
        assertFalse(banker.isLocked());
    }

    @Test
    public final void resetFailedAttemptsAfterSuccessLogin() throws AccountLockedException, InvalidCredentialsException {
        Banker banker = new Banker("B101","B101","Test","Test@acme.com","33221144","Test@123",pe);

        assertThrows(InvalidCredentialsException.class, ()->banker.login("wrong"));
        assertThrows(InvalidCredentialsException.class, ()->banker.login("wrong2"));
        assertEquals(2,banker.getFailedLoginAttempts());
        assertTrue(banker.login("Test@123"));
        assertEquals(0,banker.getFailedLoginAttempts());
    }

    @Test
    public final void authenticateCheckingAnsSavingAccountWithDifferentPassword(){
        Account checking = new CheckingAccount("CA101",500,"Checking@123",new Mastercard("1"),pe);
        Account saving = new SavingAccount("SA101",100,"Saving@123",new Mastercard("1"),pe);

        assertTrue(checking.authenticate("Checking@123"));
        assertTrue(saving.authenticate("Saving@123"));

        assertFalse(checking.authenticate("Saving@123"));
        assertFalse(saving.authenticate("Checking@123"));
    }


    @Test
    public final void cannotWithdrawMoreThan100WithNegativeBalance(){
        Account account = new CheckingAccount("CA1",-30,"Password@123",new Mastercard("1"),pe);
        assertThrows(OverdraftLimitException.class,()-> account.withdraw(200));
    }

    @Test
    public final void deactivateAccountWithTheSecondOverdraft() throws Exception{
        Account account = new CheckingAccount("CA2",10,"Password@123",new Mastercard("2"),pe);
        account.withdraw(20);
        account.withdraw(60);
        assertFalse(account.isActive());
        assertEquals(2,account.getOverdraftCount());
    }

}
