package com.pnap.sqlapi.Database;


import java.sql.SQLException;
import java.sql.Statement;



/**
 * A barebones private API layer for STable to use.
 * This helps simplify actions that STable may attempt.
 * Can be used for more specific queries, however STable is encouraged.
 */
public class SQLayer {
    public static void insert(Statement stat, String table, String columns, String values) throws SQLException{
        stat.execute(String.format("INSERT INTO %s (%s) VALUES (%s)", table, columns, values));
    }

    public static void drop(Statement stat, String table) throws SQLException{
        stat.execute(String.format("DROP TABLE IF EXISTS %s", table));
    }

    public static void create(Statement stat, String table, String columnsAndTypes) throws SQLException{
        stat.execute(String.format("CREATE TABLE IF NOT EXISTS %s (%s)", table, columnsAndTypes).replace("VARCHAR", "TEXT"));
    }

    public static void update(Statement stat, String table, String set, String where) throws SQLException{
        stat.execute(String.format("UPDATE %s SET %s WHERE %s", table, set, where));
    }

    public static void delete(Statement stat, String table, String where) throws SQLException{
        stat.execute(String.format("DELETE FROM %s WHERE %s", table, where));
    }

    public static void select(Statement stat, String table, String columns, String where) throws SQLException{
        if (where == null) where = "1=1";
        stat.execute(String.format("SELECT %s FROM %s WHERE %s", columns, table, where));
    }
}
