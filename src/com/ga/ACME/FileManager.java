package com.ga.ACME;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class FileManager {
    //private static final String data = "data";
    private final Path dataDirectory;
    private final passwordEncryptor passwordManager;

//    private  IPasswordManager passwordManager;

//    public FileManager(IPasswordManager passwordManager){
//        this.passwordManager = passwordManager;
//    }

    public FileManager(Path dataDirectory, passwordEncryptor passwordManager){
        this.dataDirectory = dataDirectory;
        this.passwordManager = passwordManager;
    }

    public void initialize() throws IOException{
        Files.createDirectories(dataDirectory);
    }

    private String safeName(String name){
        return name.replaceAll("[^a-zA-Z0-9_-]","_");
    }

    private Path personFile(String prefix, String name, String id){
//        String safeName = name.replaceAll("[^a-zA-Z0-9]","_");
        return dataDirectory.resolve(prefix+"-"+safeName(name)+"-"+id+".txt");
    }

    private Optional<Path> findFile(String prefix, String id) throws IOException {
        try (var stream = Files.list(dataDirectory)) {
            return stream
                    .filter(p -> p.getFileName().toString().startsWith(prefix + "-"))
                    .filter(p -> p.getFileName().toString().endsWith("-" + id + ".txt"))
                    .sorted()
                    .findFirst();
        }
    }

    public void saveBanker(Banker b) throws IOException{
        Path p= personFile("Banker", b.getName(), b.getId());
        List<String> line = List.of(
                "Id: " + b.getId(),
                "BankerId: " + b.getBankerId(),
                "Name: " + b.getName(),
                "Email: " + b.getEmail(),
                "PhoneNumber: " + b.getPhoneNumber(),
                "PasswordHash: " + b.getPasswordHash(),
                "FailedLoginAttempts: " + b.getFailedLoginAttempts(),
                "Locked: " + b.isLocked(),
                "LockTime: " + (b.getLockTime() == null ? "" : b.getLockTime())
        );

        Files.write(p,line);
    }

    private Map<String,String> readValues(Path p) throws IOException{
        return Files.readAllLines(p).stream().filter(x -> x.contains(":")).map(x-> x.split(":",2)).collect(Collectors.toMap(x->x[0].trim(), x->x[1].trim(),(a,b)->b));
    }


    public Optional<Banker> loadBanker(String id) throws IOException {
        Optional<Path> found = findFile("Banker", id);
        if (found.isEmpty()) return Optional.empty();
        Map<String, String> r = readValues(found.get());
        String hash = r.get("PasswordHash");
        if (hash == null || !hash.startsWith("PBKDF2$")) return Optional.empty();
        Banker b = new Banker(r.get("Id"), r.getOrDefault("BankerId", r.get("Id")),
                r.get("Name"), r.get("Email"), r.getOrDefault("PhoneNumber", ""),
                "temporary", passwordManager);
        b.setPasswordHash(hash);
        b.restoreLoginState(
                Integer.parseInt(r.getOrDefault("FailedLoginAttempts", "0")),
                Boolean.parseBoolean(r.getOrDefault("Locked", "false")),
                parseDateTime(r.get("LockTime")));
        return Optional.of(b);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value);
    }

    public String serializeAccount(Account a){
        String cardType = a.getCard().getClass().getSimpleName();

        return String.join("|",
                a.getClass().getSimpleName(),
                a.getAccountNumber(),
                Double.toString(a.getBalance()),
                a.getPasswordHash(),
                cardType,
                a.getCard().getCardNumber(),
                Boolean.toString(a.isActive()),
                Integer.toString(a.getOverdraftCount()),
                Double.toString(a.getUnpaidFees()),
                Integer.toString(a.getFailedLoginAttempts()), //THIS
                Boolean.toString(a.isLocked()),
                a.getLockTime() == null ? "" : a.getLockTime().toString() //THIS
        );

    }

    public void saveTransaction(Customer c) throws IOException{
        Path p= dataDirectory.resolve("Customer-"+safeName(c.getName())+"-"+c.getId()+"-Transactions.txt");
        List<String> line = c.getAccounts().stream().flatMap(a->a.getTransactionList().stream()).map(Transaction::toFile).toList();
        Files.write(p,line);
    }

    public List<Transaction> loadTransaction(String customerId) throws IOException{
        try(var stream = Files.list(dataDirectory)){
            Optional<Path> p = stream.filter(f->f.getFileName().toString().startsWith("Customer-")).filter(f->f.getFileName().toString().endsWith("-"+customerId+"-Transactions.txt")).findFirst();

            if(p.isEmpty()){
                return List.of();
            }
            return Files.readAllLines(p.get()).stream().filter(f->!f.isBlank()).map(Transaction::fromFile).toList();

        }
    }

    public void saveCustomer(Customer c) throws IOException{
        Path p = personFile("Customer", c.getName(),c.getId());
        List<String> line = new ArrayList<>();
        line.add("Id: "+c.getId());
        line.add("Name: "+c.getName());
        line.add("Email: "+c.getEmail());
        line.add("PhoneNumber: "+c.getPhoneNumber());
        line.add("PasswordHash: "+c.getPasswordHash());
        line.add("FailedLoginAttempts: " + c.getFailedLoginAttempts()); //
        line.add("Locked: " + c.isLocked()); //
        line.add("LockTime: " + (c.getLockTime() == null ? "" : c.getLockTime())); //
        for(Account a: c.getAccounts()){
            line.add("Account: "+serializeAccount(a));
        }
        Files.write(p,line);
        saveTransaction(c);
    }

    public Optional<Customer> loadCustomer(String id) throws IOException {

        // Find the customer's file using the customer ID.
        Optional<Path> found = findFile("Customer", id);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        // Read all lines from the customer file.
        List<String> lines = Files.readAllLines(found.get());

        // Read the customer's basic information.
        Map<String, String> r = lines.stream().filter(line -> line.contains(":")).filter(line -> !line.startsWith("Account:")).map(line -> line.split(":", 2)).collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim(), (a, b) -> b));

        // A customer must have a valid stored password hash.
        String customerPasswordHash = r.get("PasswordHash");

        if (customerPasswordHash == null || !customerPasswordHash.startsWith("PBKDF2$")) {
            return Optional.empty();
        }

        // Create the customer using a temporary password.
        // The real password hash is restored immediately after.
        Customer customer = new Customer(
                r.get("Id"),
                r.getOrDefault("CustomerId", r.get("Id")),
                r.get("Name"),
                r.get("Email"),
                r.getOrDefault("PhoneNumber", ""),
                "temporary",
                passwordManager
        );

        // Restore the customer's actual password hash.
        customer.setPasswordHash(customerPasswordHash);

        // Restore customer's login state if those values exist.
        customer.restoreLoginState(Integer.parseInt(r.getOrDefault("FailedLoginAttempts", "0")), Boolean.parseBoolean(r.getOrDefault("Locked", "false")), parseDateTime(r.get("LockTime")));

        // Load all transactions belonging to this customer.
        List<Transaction> transactions = loadTransaction(customer.getId());

        // Load each account stored in the customer file.
        for (String line : lines) {
            if (!line.startsWith("Account:")) {
                continue;
            }


            String accountData = line.substring("Account:".length()).trim();

            String[] a = accountData.split("\\|", -1);

            //  serializeAccount()  fields:
            if (a.length !=12) {
                throw new IllegalStateException("Invalid account record in " + found.get().getFileName());
            }
            String accountType = a[0];
            String accountNumber = a[1];
            double balance = Double.parseDouble(a[2]);
            String accountPasswordHash = a[3];
            String cardType = a[4];
            String cardNumber = a[5];
            boolean active = Boolean.parseBoolean(a[6]);
            int overdraftCount = Integer.parseInt(a[7]);
            double unpaidFees = Double.parseDouble(a[8]);
            int failedAttempts = Integer.parseInt(a[9]);
            boolean locked = Boolean.parseBoolean(a[10]);
            LocalDateTime lockTime = a[11].isBlank() ? null : LocalDateTime.parse(a[11]);

            // card type.
            Card card = switch (cardType) {
                case "MastercardPlatinum" -> new MastercardPlatinum(cardNumber);
                case "MastercardTitanium" -> new MastercardTitanium(cardNumber);
                case "Mastercard" -> new Mastercard(cardNumber);
                default -> throw new IllegalStateException("Unknown card type: " + cardType);
            };

            Account account;
            if (accountType.equals("SavingAccount")) {
                account = new SavingAccount(accountNumber, balance, "temporary", card, passwordManager);
            } else if (accountType.equals("CheckingAccount")) {
                account = new CheckingAccount(accountNumber, balance, "temporary", card, passwordManager);
            } else {
                throw new IllegalStateException("Unknown account type: " + accountType);
            }

            // Restore the account's real password hash.
            account.setPasswordHash(accountPasswordHash);

            /*
             * Restore account status and transaction history.
             * The current Account.statement() method restores:
             * - active status
             * - overdraft count
             * - unpaid fees
             * - transactions
             */
            List<Transaction> accountTransactions = transactions.stream().filter(t -> t.getAccountNumber().equals(accountNumber)).toList();
            account.statement(active, overdraftCount, unpaidFees, accountTransactions);

            // Restore the account's lock state.
            // restoreLoginState() will clear the expired/invalid lock.
            account.restoreLoginState(failedAttempts, locked, lockTime);

            // Add the reconstructed account to the customer.
            customer.addAccount(account);
        }

        return Optional.of(customer);
    }


    public void removeMalformedDemoFiles(String bankerId, String customerId) throws IOException {
        try (var stream = Files.list(dataDirectory)) {
            stream.filter(p -> {
                String n = p.getFileName().toString();
                boolean demoId = n.endsWith("-" + bankerId + ".txt") || n.endsWith("-" + customerId + ".txt")
                        || n.endsWith("-" + customerId + "-Transactions.txt");
                boolean canonical = n.equals("Banker-DemoBanker-" + bankerId + ".txt")
                        || n.equals("Customer-DemoCustomer-" + customerId + ".txt")
                        || n.equals("Customer-DemoCustomer-" + customerId + "-Transactions.txt");
                return demoId && !canonical;
            }).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException e) { throw new RuntimeException(e); }
            });
        }
    }



}
