package com.ga.ACME;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BankSystem {
    private final Map<String, Banker> bankers = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();
    private  IPasswordManager passwordManager;
    private final FileManager fileManager;


    public BankSystem(FileManager fileManager){
        this.fileManager = fileManager;
        this.passwordManager = new passwordEncryptor();
    }

    public Optional<Banker> findBanker(String id) throws IOException {
        return fileManager.loadBanker(id);
    }

    public Optional<Customer> findCustomer(String id) throws IOException{
        return fileManager.loadCustomer(id);
    }

    public Banker loginBanker(String id, String password) throws IOException, InvalidCredentialsException, AccountLockedException {
        Banker banker = fileManager.loadBanker(id).orElseThrow(()->new InvalidCredentialsException("Invalid banker Id or password"));
        try{
            banker.login(password);
        } catch (InvalidCredentialsException | AccountLockedException e){
            fileManager.saveBanker(banker);
            throw e;
        }

        fileManager.saveBanker(banker);
        bankers.put(banker.getId(),banker);
        return banker;
    }

    public Customer loginCustomer(String customerId, String accountId, String password) throws IOException, InvalidCredentialsException, AccountLockedException {
        Customer c = fileManager.loadCustomer(customerId).orElseThrow(() -> new InvalidCredentialsException("Customer does not exist"));
        Account account = c.findAccount(accountId).orElseThrow(()-> new InvalidCredentialsException("Account does not exist for this customer"));

//        account.login(password);
        try {

            account.login(password);

        } catch (InvalidCredentialsException |
                 AccountLockedException e) {

            fileManager.saveCustomer(c);

            throw e;
        }

        fileManager.saveCustomer(c);
        customers.put(c.getId(),c);
        return c;
    }

    private String generateId(String prefix,boolean banker) throws IOException {
//        String id;
//        do{
//            id =prefix +(100+(int)(Math.random()*900));
//        } while (fileManager.loadCustomer(id).isPresent());

        for (int i = 100; i < 1000; i++) {
            String id = prefix + i;
            if ((banker ? fileManager.loadBanker(id) : fileManager.loadCustomer(id)).isEmpty()) return id;
        }

        throw new IOException("No available Id");
    }

    private String generateAccountNumber(Customer customer, String prefix) {
        for (int i = 100; i < 1000; i++) {
            String id = prefix + i;
            if (customer.findAccount(id).isEmpty()) return id;
        }
        return prefix + System.currentTimeMillis();
    }

    public Customer registerCustomer(String name, String email, String phoneNumber, String password) throws IOException {
        String id= generateId("C",false);
        Customer customer = new Customer(id,id,name,email,phoneNumber,password,passwordManager);
        fileManager.saveCustomer(customer);
        customers.put(id,customer);
        return customer;
    }

    public Account createAccount(Customer customer, String type, double balance, String password, Card card) throws InvalidAmountException, IOException {
        if(balance < 0){
            throw new InvalidAmountException("Opening balance can not be negative");
        }
        if(!type.equalsIgnoreCase("Checking") && !type.equalsIgnoreCase("Saving")){
            throw new InvalidAmountException("Unknown account type");
        }
        if(customer.getAccounts().stream().anyMatch(a->a.getAccountType().equalsIgnoreCase(type))){
            throw new InvalidAmountException("Customer already has "+type+" account");
        }

        String prefix = type.equalsIgnoreCase("Checking")? "CA":"SA";
        String number = generateAccountNumber(customer,prefix);
        Account account = type.equalsIgnoreCase("Checking")? new CheckingAccount(number,balance,password,card,passwordManager): new SavingAccount(number,balance,password,card,passwordManager);
        customer.addAccount(account);
        fileManager.saveCustomer(customer);
        return account;
    }


    public void withdraw(Customer c, String accountId, double amount) throws AccountNotFoundException, AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException, IOException {
        Account a = c.findAccount(accountId).orElseThrow(()->new AccountNotFoundException("Account not found"));
        a.withdraw(amount);
        fileManager.saveCustomer(c);
    }

    public void deposit(Customer c, String accountId, double amount) throws AccountNotFoundException, AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException, IOException {
//        Account a = c.findAccount(accountId).orElseThrow(()->new AccountNotFoundException("Account not found"));
//        a.deposit(amount);
//        fileManager.saveCustomer(c);
        depositOwnAccount(c,accountId,amount);
    }

    public void depositOwnAccount(Customer customer, String destinationAccountId, double amount)
            throws AccountNotFoundException, AccountDeactivatedException,
            InvalidAmountException, OverdraftLimitException,
            CardLimitExceededException, IOException {

        Account destination = customer.findAccount(destinationAccountId)
                .orElseThrow(() ->
                        new AccountNotFoundException("Destination account not found"));

        // true = deposit to own account
        destination.deposit(amount, true);

        fileManager.saveCustomer(customer);
    }


    public void depositToAnotherAccount(String destinationCustomerId, String destinationAccountId, double amount) throws UserNotFoundException, AccountNotFoundException, AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException, IOException {
        Customer destinationCustomer = fileManager.loadCustomer(destinationCustomerId).orElseThrow(() -> new UserNotFoundException("Destination customer not found"));

        Account destination = destinationCustomer.findAccount(destinationAccountId).orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

        // false = deposit to another customer's account
        destination.deposit(amount, false);

        fileManager.saveCustomer(destinationCustomer);

        customers.put(
                destinationCustomer.getId(),
                destinationCustomer
        );
    }

    public void transfer(Customer cSource, String idSource, Customer cDestination, String idDestination, double amount) throws AccountNotFoundException, CardLimitExceededException, AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, IOException {
        Account from = cSource.findAccount(idSource).orElseThrow(()->new AccountNotFoundException("Source account not found"));
        Account to = cDestination.findAccount(idDestination).orElseThrow(()->new AccountNotFoundException("Destination account not found"));

        if(amount<=0 || !Double.isFinite(amount)){
            throw new InvalidAmountException("Invalid amount");
        }
        boolean ownAccount = cSource.getId().equals(cDestination.getId());
        double limit = ownAccount? from.getCard().getDailyLimits().getOwnAccountTransferLimit() : from.getCard().getDailyLimits().getTransferLimit();
        double used = from.getTransactionList().stream().filter(t -> t.getTimestamp().toLocalDate().equals(LocalDateTime.now().toLocalDate())).filter(t-> t.getType()==TransactionType.transfer).mapToDouble(Transaction::getAmount).sum();

        if(used + amount > limit){
            throw new CardLimitExceededException("Daily transfer limit exceede");
        }

        from.transferOut(amount,ownAccount);
        to.transferIn(amount,ownAccount);
        fileManager.saveCustomer(cSource);
        if(!ownAccount){
            fileManager.saveCustomer(cDestination);
        }
    }

    public String statement(Customer c, String accountId){
        return c.statement(accountId);
    }

    public List<Transaction> filterTransaction(Customer c, String accountId, FilterType type){
        return c.findAccount(accountId).map(a->TransactionFilter.filter(a.getTransactionList(), type, LocalDateTime.now())).orElse(List.of());
    }

    public List<Transaction> filterTransaction(Customer c, String accountId, LocalDateTime start, LocalDateTime end){
        return c.findAccount(accountId).map(a-> TransactionFilter.custom(a.getTransactionList(), start, end)).orElse(List.of());
    }

    public void saveCustomer(Customer customer) throws IOException {
        fileManager.saveCustomer(customer);
    }


}
