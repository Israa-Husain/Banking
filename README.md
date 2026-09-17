Main Features
Banker Features

Banker login and authentication
Customer registration
Customer search
Account creation
View customer accounts and statements
Customer Features

Customer and account authentication
Checking and Saving accounts
Deposit money
Withdraw money
Transfer between own accounts
Transfer to another customer’s account
View account statements
Filter transaction history
Change account password

Banking Features:
Daily card transaction limits
Different Mastercard types and limits
Overdraft handling
$35 overdraft fee
Account deactivation after repeated overdrafts
Transaction history
Account balance tracking
Account Types

The system supports two account types:
Checking Account
Saving Account
Both inherit common functionality from the abstract Account class.

Card Types:
Mastercard
Mastercard Titanium
Mastercard Platinum
Each card type has different daily limits for withdrawals and transfers. 
The system also distinguishes between transactions involving the customer’s own accounts and transactions involving other accounts.

Password Security:
Passwords are not stored as plain text.
The system uses PBKDF2 with HMAC-SHA256 for password hashing. A random salt is generated for each password, and the hashing process uses 120,000 iterations.
The stored password contains the algorithm information, iteration count, salt, and generated hash.
During login, the entered password is hashed using the stored salt and compared with the stored hash. The original password does not need to be stored or decrypted.
The system also includes login protection. After three failed login attempts, the user or account is temporarily locked for 60 seconds.

Overdraft Handling:
Overdraft functionality is implemented mainly in the Account class.

When a withdrawal or outgoing transfer causes the balance to become negative:
The overdraft count is increased.
A $35 overdraft fee is charged.
The unpaid fee is recorded.
The transaction is added to the transaction history.
After the second overdraft, the account is deactivated.
Deposits can be used to repay outstanding overdraft fees. The account can become active again when the required conditions are satisfied.
Transaction Management

Every transaction contains:
Transaction ID
Account number
Transaction type
Amount
Date and time
Resulting account balance

Supported transaction types include:
Deposit
Withdrawal
Transfer
Overdraft fee
Transaction history can also be filtered by predefined periods such as today, yesterday, the last 7 days, and the last 30 days, as well as by a custom date range.


File Handling
The FileManager class is responsible for saving and loading application data.

Customer details:
Password hash
Login state
Account information
Balance
Card information
Overdraft information
Transactions are stored separately in transaction files.
When a customer is loaded, the program reconstructs the Customer, Account, and Card objects and restores the appropriate transaction history for each account.


Object-Oriented Programming:
Abstraction:
Person, Account, and Card provide abstract structures for their subclasses.
Inheritance:
Customer and Banker inherit from Person. CheckingAccount and SavingAccount inherit from Account. The different Mastercard classes inherit from Card.
Polymorphism:
The program works with general types such as Account and Card while allowing different subclasses to provide their own implementations.
Encapsulation:
Class fields are kept private where appropriate and accessed through methods.
Interfaces:
Interfaces such as IAuthenticatable, IPasswordManager, and ITransactable define common behavior and help separate responsibilities.

Exception Handling:
Invalid credentials
Invalid transaction amounts
Account not found
User not found
Account deactivation
Account lockout
Card limit exceeded
Overdraft limit exceeded

Main Classes:
Main – Console user interface and menus
BankSystem – Coordinates banking operations
Person – Base class for people in the system
Banker – Represents a banker
Customer – Represents a bank customer
Account – Base class containing common account functionality
CheckingAccount – Represents a checking account
SavingAccount – Represents a saving account
Card – Base class for bank cards
CardLimits – Defines card transaction limits
Mastercard – Standard Mastercard
MastercardTitanium – Titanium Mastercard
MastercardPlatinum – Platinum Mastercard
Transaction – Represents an individual banking transaction
TransactionFilter – Filters transactions by date
FileManager – Handles file persistence
passwordEncryptor – Handles password hashing and verification


Testing:
Successful login
Invalid login credentials
Account lockout after failed attempts
Login during the lockout period
Login after the lockout period
Resetting failed login attempts
Authentication of different account types

Technologies Used:
Java
Java Collections
Java Streams
PBKDF2WithHmacSHA256
JUnit
Object-Oriented Programming

https://lucid.app/lucidchart/a1d860e0-f00c-461d-bf45-9330ddacd834/edit?viewport_loc=-4024%2C-2300%2C4751%2C2472%2C0_0&invitationId=inv_67efe1be-fb6c-446e-a68d-9e5be45232f7

