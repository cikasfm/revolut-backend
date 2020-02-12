-- create Account table
CREATE TABLE IF NOT EXISTS account (
  accountNumber IDENTITY NOT NULL PRIMARY KEY,
  accountName VARCHAR NOT NULL,
  balance DECIMAL(20, 2) DEFAULT 0.00
);