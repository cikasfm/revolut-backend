# Thin Bank example

Ultra-simple bank account application. Supports create account, view account & transfer balance methods

## Deploy it on Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

## Build Locally

To build & run the app use the following command:

    git clone https://github.com/cikasfm/revolut-backend.git
    cd revolut-backend
    mvn clean compile exec:java

Open the address [http://localhost:4567](http://localhost:4567) in your browser to see the Swagger API definition test UI

## Typical API commands

### Create new account

One for the Bank
```shell script
curl -X PUT "https://thin-bank.herokuapp.com/api/account?accountName=Bank" -H "accept: application/json"
```

Output:
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "accountNumber": 1,
    "accountName": "Bank",
    "balance": 0
  }
}
```

One for a user named John
```shell script
curl -X PUT "https://thin-bank.herokuapp.com/api/account?accountName=John" -H "accept: application/json"
```

Output:
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "accountNumber": 2,
    "accountName": "John",
    "balance": 0
  }
}
```

### Deposit cash to the account

```shell script
curl -X POST "https://thin-bank.herokuapp.com/api/balance/deposit" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d "{ \"toAcct\": 1, \"amount\": 999999999}"
```

Output:

```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "accountNumber": 1,
    "accountName": "Bank",
    "balance": 999999999.00
  }
}
```

### Transfer balance

```shell script
curl -X POST "https://thin-bank.herokuapp.com/api/balance/transfer" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d "{ \"fromAcct\": 1, \"toAcct\": 2, \"amount\": 100}"
```

Output
```json
{
  "status": 200,
  "message": "OK"
}
```

### View all account balances

```shell script
curl -X GET "https://thin-bank.herokuapp.com/api/account/all?pageNum=0&pageSize=20" -H "accept: application/json"
```

Output
```json
{
  "status": 200,
  "message": "OK",
  "data": [
    {
      "accountNumber": 1,
      "accountName": "Bank",
      "balance": 999999899
    },
    {
      "accountNumber": 2,
      "accountName": "John",
      "balance": 100
    }
  ]
}

```

## Libraries used

- [Spark](http://sparkjava.com/) - A micro framework for creating web applications in Kotlin and Java 8 with minimal effort
- [H2](http://www.h2database.com/html/main.html) Database Engine
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - A solid, high-performance, JDBC connection pool at last.
- [SLF4J](http://www.slf4j.org/) Simple Logging Facade for Java