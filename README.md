# basic-mysql-api-java
A really basic API layer between a MySQL server and Java.

It has an abstraction layer of sorts so that a user can manipulate a MySQL database without knowing any SQL syntax.
This layer is primarily split between 3 different objects.

## SDatabase
An SDatabase object has a direct connection to a MySQL Database. To create one, you need to pass the name of the database, and 
a username/password that's authorized to manipulate said database.

Functions here will manipulate the database directly, so one should be careful before doing something like 
calling `dropAll()`.

## JDatabase
A JDatabase object is one that emulates a database. Manipulating it will not change the MySQL database, only the instance you're
directly using. This is useful for when you'd like to save the contents or structure of a database to a file.

## .to Methods
These methods can easily convert from JDatabase to SDatabase and vice versa. This is incredibly useful for certain cases, such as
converting an SDatabase to JDatabase for temporary modification, before converting back to SDatabase once one is satisfied with
the changes. This is as simple as doing `<JDatabase>.toSDatabase()` or `<SDatabase>.toJDatabase()`.

## SyncedDatabase
This effectively creates an SDatabase and JDatabase instance that are synced with each other. An insert would modify both the
SDatabase and the JDatabase. This is useful for when you want to save a MySQL database, or be able to utilise JDatabase
functions for a SDatabase.
