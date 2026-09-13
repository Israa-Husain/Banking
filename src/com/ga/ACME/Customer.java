package com.ga.ACME;

import java.util.ArrayList;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class Customer extends Person{
    private String customerId;
//    private Optional<CheckingAccount> checkingAccount;
//    private Optional<SavingAccount> savingAccount;
    private List<Account> accounts = new ArrayList<>();

    public Customer(String id, String name, String email, String phoneNumber, String password, String customerId, IPasswordManager passwordManager){
        super(id,name,email,phoneNumber,password,passwordManager);
        this.customerId = customerId;
//        this.checkingAccount = Optional.empty();
//        this.savingAccount = Optional.empty();
    }

    @Override
    public String getRole() {
        return "Customer";
    }

//    public void addCheckingaccount(CheckingAccount account){
//        if(checkingAccount.isPresent()){
////            throw new Exception("Customer already have an account");
//        }
//        checkingAccount = Optional.of(account);
//    }

//    public void addSavingAccount

    public void addAccount(Account account){
        accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Optional<Account> findAccount(String id){
        return accounts.stream().filter(a -> a.getAccountNumber().equals(id)).findFirst();
    }

    public String statement(String id){
        return findAccount(id).map(Account::accountState).orElse("Account not found");
    }



    @Override
    public boolean authenticate(String password) {
        return false; //CHANGE
    }

    public String getCustomerId() {
        return customerId;
    }

//    public Optional<CheckingAccount> getCheckingAccount() {
//        return checkingAccount;
//    }
//
//    public Optional<SavingAccount> getSavingAccount() {
//        return savingAccount;
//    }
}
