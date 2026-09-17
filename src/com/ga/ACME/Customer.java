package com.ga.ACME;

import java.util.ArrayList;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class Customer extends Person{
    private String customerId;
    private List<Account> accounts = new ArrayList<>();

    public Customer(String id, String customerId, String name, String email, String phoneNumber, String password, IPasswordManager passwordManager){
        super(id,name,email,phoneNumber,password,passwordManager);
        this.customerId = customerId;
    }

    @Override
    public String getRole() {
        return "Customer";
    }


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


    public String getCustomerId() {
        return customerId;
    }

}
