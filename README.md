# Thin Bank example

Ultra-simple bank account application. Supports create account, view account & transfer balance methods

## Deploy it on Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

## Build Locally

To build & run the app use the following command:

    git clone https://github.com/cikasfm/revolut-backend.git
    
    mvn clean compile exec:java

Open the address [http://localhost:4567](http://localhost:4567) in your browser to see the Swagger API definition test UI

## Libraries used

- [Spark](http://sparkjava.com/) - A micro framework for creating web applications in Kotlin and Java 8 with minimal effort
- [H2](http://www.h2database.com/html/main.html) Database Engine
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - A solid, high-performance, JDBC connection pool at last.
- [SLF4J](http://www.slf4j.org/) Simple Logging Facade for Java