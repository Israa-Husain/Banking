package com.ga.ACME;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    static Scanner kbd = new Scanner(System.in);
    static Console console = System.console();

    public static void main(String[] args) throws Exception {
        passwordEncryptor pe = new passwordEncryptor();
        FileManager fm = new FileManager(Path.of("data"), pe);
        fm.initialize();
        demo(fm,pe);

        BankSystem bank = new BankSystem(fm);


        while (true){
            System.out.println("1. Banker Login");
            System.out.println("2. Customer Login");
            System.out.println("3. Exit");
            //System.out.print("Choose the number of operation");
            String choice = read("Choose: ");

            try{
                switch (choice){
                    case "1" -> bankerLogin(bank);
                    case "2" -> customerLogin(bank);
                    case "3" -> {System.out.println("Goodbye!"); return;}
                    default -> System.out.println("Invalid option. Please choose 1,2,3");
                }
            } catch (Exception e){
                System.out.println("Error: "+e.getMessage());
            }

        }

    }

    private static void bankerMenu(BankSystem bank) throws Exception{
        while (true){
            System.out.println("BANKER MENU");
            System.out.println("1. Register new customer");
            System.out.println("2. Find customer");
            System.out.println("3. Create customer account");
            System.out.println("4. View customer account");
            System.out.println("5. View customer statement");
            System.out.println("6. Logout");
            String choice = read("Choose: ");

            try{
                switch (choice){
                    case "1" -> registerCustomer(bank);
                    case "2" -> findCustomer(bank);
                    case "3" -> createAccount(bank);
                    case "4" -> viewCustomerAccounts(bank);
                    case "5" -> viewStatement(bank);
                    case "6" -> {return;}
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e){
                System.out.println("Error: "+e.getMessage());
            }


        }
    }

    private static void customerMenu(BankSystem bank, Customer customer, String accountId) throws Exception{
        while (true){
            System.out.println("CUSTOMER MENU");
            System.out.println("Logged in account: "+accountId);
            System.out.println("1. View my accounts");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. View statement");
            System.out.println("6. Filter transaction");
            System.out.println("7. Change account password");
            System.out.println("8. Logout");
            String choice = read("Choice: ");

            try{
                switch (choice){
                    case "1" -> viewAccounts(customer);
                    case "2" -> deposit(bank, customer, accountId);
                    case "3" -> withdraw(bank, customer, accountId);
                    case "4" -> transfer(bank, customer, accountId);
                    case "5" -> System.out.println(bank.statement(customer,accountId));
                    case "6" -> filterTransactions(bank,customer,accountId);
                    case "7" -> changeAccountPassword(bank,customer,accountId);
                    case "8" -> {return;}
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception e){
                System.out.println("Error: "+e.getMessage());
            }

        }
    }

    private static void bankerLogin(BankSystem bank) throws AccountLockedException, InvalidCredentialsException, Exception {
        String id= read("Banker id: ");
        String password = readPassword("Password: ");
        Banker banker = bank.loginBanker(id, password);
        System.out.println("\nWelcome, " + banker.getName() + "!");
        bankerMenu(bank);
    }

    private static void customerLogin(BankSystem bank) throws Exception{
        String customerId = read("Customer Id: ");
        String accountId = read("Account Id: ");
        String password = readPassword("Account Password: ");
        Customer customer = bank.loginCustomer(customerId,accountId,password);
        System.out.println("\nWelcome, "+customer.getName()+"!");
        customerMenu(bank,customer,accountId);

    }

    private static void registerCustomer(BankSystem bank) throws IOException {
        String name = readRequired("Customer name: ");
        String email = readRequired("Email: ");
        String phoneNumber = readRequired("Phone number: ");
        String password = newPassword();
        Customer customer = bank.registerCustomer(name,email,phoneNumber,password);
        System.out.println("Customer registered successfully!");
    }

    private static void findCustomer(BankSystem bank) throws IOException, UserNotFoundException {
        String id = read("Customer Id: ");
        Customer customer = bank.findCustomer(id).orElseThrow(()-> new UserNotFoundException("Customer not found"));
        System.out.println("Customer: "+customer.getName()+ "\nId: "+customer.getCustomerId() +"\nEmail: "+customer.getEmail()+"\nPhone number: "+customer.getPhoneNumber());
    }

    private static void createAccount(BankSystem bank) throws IOException, InvalidAmountException, UserNotFoundException {
        String customerId = read("Customer Id");
        Customer customer = bank.findCustomer(customerId).orElseThrow(()-> new UserNotFoundException("Customer not found"));
        String type = readAccountType();
        double balance = readAmount("Balance: ");
        String password = newPassword();
        Card card = readCard();
        Account account = bank.createAccount(customer,type,balance,password,card);
        System.out.println("Account created: "+account.getAccountNumber());
    }

    private static void viewAccounts(Customer customer){
        if(customer.getAccounts().isEmpty()){
            System.out.println("No accounts");
            return;
        }
        customer.getAccounts().forEach(a -> System.out.printf("%s | %s | Balance: $%.2f | Card: %s | %s%n",
                a.getAccountNumber(), a.getAccountType(), a.getBalance(), a.getCard().getCardType(),
                a.isActive() ? "ACTIVE" : "DEACTIVATED"));
    }

    private static void viewCustomerAccounts(BankSystem bank) throws IOException, UserNotFoundException {
        String id = read("Customer Id: ");
        Customer customer = bank.findCustomer(id).orElseThrow(()-> new UserNotFoundException("Customer not found"));
        viewAccounts(customer);
    }

    private static void viewStatement(BankSystem bank) throws IOException, UserNotFoundException {
        String id = read("Customer Id: ");
        Customer customer = bank.findCustomer(id).orElseThrow(()-> new UserNotFoundException("Customer not found"));
        String accountId = read("Account Id: ");
        System.out.println(bank.statement(customer,accountId));
    }

    private static void withdraw(BankSystem bank, Customer customer, String accountId) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException, IOException, AccountNotFoundException {
        double amount = readAmount("Withdrawal amount: ");
        bank.withdraw(customer,accountId,amount);
        System.out.println("Withdrawal successful");
    }

    private static void deposit(BankSystem bank, Customer customer, String accountId) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException, IOException, AccountNotFoundException, UserNotFoundException {
        System.out.println("1. Deposit to my own account");
        System.out.println("2. Deposit to another account");
        System.out.println("3. Back");

        String choice = read("Choose: ");

        if (choice.equals("1")) {
            viewAccounts(customer);

            String destinationAccountId = read("Destination account Id: ");
            double amount = readAmount("Deposit amount: ");

            bank.depositOwnAccount(customer, destinationAccountId, amount);

            System.out.println("Deposit successful");

        } else if (choice.equals("2")) {

            String destinationCustomerId = read("Destination customer Id: ");
            String destinationAccountId = read("Destination account Id: ");

            double amount = readAmount("Deposit amount: ");
            bank.depositToAnotherAccount(destinationCustomerId, destinationAccountId, amount);
            System.out.println("Deposit successful");

        } else if (!choice.equals("3")) {
            System.out.println("Invalid deposit option");
        }
    }

    private static void transfer(BankSystem bank, Customer customer, String accountId) throws AccountDeactivatedException, InvalidAmountException, OverdraftLimitException, CardLimitExceededException, IOException, AccountNotFoundException, UserNotFoundException {
        System.out.println("1. Transfer to my own account \n2. Transfer to another account");
        String choice = read("Choose: ");
        double amount = readAmount("Transfer amount: ");
        if(choice.equals("1")){
            String toAccount = read("Destination account Id: ");
            bank.transfer(customer,accountId,customer,toAccount,amount);
        } else if (choice.equals("2")) {
            String customerId = read("Destination customer Id: ");
            String toAccount = read("Destination account Id: ");
            Customer destination = bank.findCustomer(customerId).orElseThrow(()-> new UserNotFoundException("Customer not found"));
            bank.transfer(customer,accountId,destination,toAccount,amount);
        } else throw new InvalidAmountException("Invalid transfer type");

        System.out.println("Transfer successful");
    }

    private static void filterTransactions(BankSystem bank, Customer customer, String accountId){
        System.out.println("1. Today \n2. Yesterday \n3. Last week \n4.Last 7 days \n5.Last month \n6.Last 30 days \n7.Custom range");
        String choice = read("Choose: ");
        List<Transaction> filterTransaction;
        if(choice.equals("7")){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(read("Start (yyyy-MM-dd): "), formatter);
            LocalDate endDate = LocalDate.parse(read("End (yyyy-MM-dd): "),formatter);
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            filterTransaction = bank.filterTransaction(customer,accountId,start,end);
        } else{
            FilterType[] filteredData= FilterType.values();
            int index = Integer.parseInt(choice)-1;
            if(index<0 || index>=filteredData.length){
                throw new IllegalArgumentException("Invalid filter");
            }
            filterTransaction = bank.filterTransaction(customer,accountId,filteredData[index]);
        }
        if(filterTransaction.isEmpty()){
            System.out.println("No transaction found");
        } else {
            filterTransaction.forEach(System.out::println);
        }
    }

    private static String read(String r){
        System.out.println(r);
        return kbd.nextLine();
    }

    private static String readRequired(String r){
        while(true){
            String s = read(r).trim();
            if(!s.isEmpty()){
                return s;
            }
            System.out.println("Field Required");
        }
    }

    private static String readPassword(String r){
        if(console!= null){
            char[] chars = console.readPassword(r);
            return chars==null? "" : new String(chars);
        }
        System.out.println(r);
        return kbd.nextLine();
    }

    private static double readAmount(String r){
        while (true){
            double n = Double.parseDouble(read(r));
            if(n > 0){
                return n;
            }
            System.out.println("Enter valid amount");
        }
    }

    private static String readAccountType(){
        while (true){
            System.out.println("1. Checking Account \n2. Saving Account");
            String choice = read("Account type: ");
            if(choice.equals("1")){
                return "Checking";
            }
            if(choice.equals("2")){
                return "Saving";
            }
            System.out.println("Invalid account type");
        }
    }

    private static Card readCard(){
        while (true){
            System.out.println("1. Mastercard \n2.Mastercard Titanium \n3.Mastercard Platinum");
            String choice = read("Card: ");
            String number = readRequired("Card number: ");
            switch (choice){
                case "1" -> {return new Mastercard(number);}
                case "2" -> {return new MastercardTitanium(number);}
                case "3" -> {return new MastercardPlatinum(number);}
                default -> System.out.println("Invalid card type");
            }
        }
    }


    private static String newPassword(){
        while (true){
            String password = readPassword("Password: ");
            String confirmPassword = readPassword("Confirm password: ");
            if(!password.equals(confirmPassword)){
                System.out.println("Password does not match");
                continue;
            }
            if(password.length()<8){
                System.out.println("Password should be at least 8 characters");
                continue;
            }
            return password;
        }
    }


    private static void changeAccountPassword(BankSystem bank, Customer c, String accountId) throws Exception,AccountNotFoundException, InvalidCredentialsException {
        Account account = c.findAccount(accountId).orElseThrow(()-> new AccountNotFoundException("Account not found"));
        String currentPassword = readPassword("Current password: ");
        if(!account.authenticate(currentPassword)){
            throw new InvalidCredentialsException("Incorrect password");
        }

        String newPassword = newPassword();
        account.changePassword(newPassword);
        bank.saveCustomer(c);
        System.out.println("Password changes successfully!");
    }

    private static void demo(FileManager fm, passwordEncryptor pe) throws Exception{
        fm.removeMalformedDemoFiles("B102","C102");

        if (fm.loadBanker("B102").isEmpty()) {
            fm.saveBanker(new Banker("B102", "B102", "DemoBanker", "banker101@acme.com", "33662211", "Banker@123", pe));
        }

        if (fm.loadCustomer("C102").isEmpty()) {
            Customer c = new Customer("C102", "C102", "DemoCustomer", "Customer101@acme.com", "36541236", "Customer@123", pe);
            c.addAccount(new CheckingAccount("CA102", 100, "Checking@123", new MastercardPlatinum("3277598645009898"), pe));
            c.addAccount(new SavingAccount("SA102", 500, "Saving@123", new MastercardPlatinum("1234437711223344"), pe));
            fm.saveCustomer(c);


}}}
