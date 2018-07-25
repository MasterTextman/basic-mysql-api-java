package com.pnap.sqlapi.Database;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class SDatabase {
    private static final String SELECT_EVERYTHING_FROM =
            "SELECT * FROM ";
    private String name;
    private String user;
    private String pass;
    private static final Logger logger = LoggerFactory.getLogger(SDatabase.class);
    private Connection connection;
    private Map<String, STable> mapOTables = new LinkedHashMap<>();

    private long serialVersionUID = 1L;

    public SDatabase(String name, String user, String pass) throws SQLException{
        this.name = name;
        this.user = user;
        this.pass = pass;
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost/" + name, user, pass);
    }

    public void syncWithMySQL(){
        mapOTables = new HashMap<>();

        try(Statement statement = connection.createStatement()){

            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(name, null, "%", null);

            List<String> tableNames = new ArrayList<>();

            while(rs.next()){
                tableNames.add(rs.getString(3));
            }

            for (String tablename : tableNames){
                try(ResultSet rsT = statement.executeQuery(SELECT_EVERYTHING_FROM + tablename)) {

                    ResultSetMetaData rstmd = rsT.getMetaData();

                    List<SColumn> columns = new ArrayList<>();

                    int columnsInTable = rstmd.getColumnCount();
                    for (int i = 0; i < columnsInTable; i++) {
                        columns.add(new SColumn(rstmd.getColumnName(i + 1), rstmd.getColumnTypeName(i + 1)));
                    }

//                    tables.add(new STable(connection, columns, tablename));\
                    mapOTables.put(tablename, new STable(connection, columns, tablename));
                }
            }


        }catch(SQLException sql){
            logger.error("Error while syncing with MySQL : " + sql.getMessage());
            logger.error("SQL Error : " + sql.getErrorCode());
        }
        logger.info("Syncing done.");
    }

    public boolean createTable(List<SColumn> columns, String tableName){
        if(getTable(tableName) != null){
            logger.error("STable already exists.");
            return false;
        }

        logger.debug("Attempting to create STable \"{}\"", tableName);

        try {
            STable newTable = new STable(connection, columns, tableName);
            mapOTables.put(tableName, newTable);
        }catch(SQLException sql){
            logger.error("Error in creating table : {}", sql.getMessage());
            logger.error("SQL ERRORCODE : {}", sql.getErrorCode());
            return false;
        }
        return true;
    }

    public STable getTable(String tableName){
        return mapOTables.get(tableName);
    }

    public STable getTable(int index){
        return mapOTables.values().toArray(new STable[0])[index];
    }

    public JDatabase toJDatabase(){
        final JDatabase jdbskeleton = new JDatabase(this.name);

        mapOTables.values()
                .forEach(table -> jdbskeleton.addTable(table.toJTable()));

        return jdbskeleton;
    }

    public void showTableNames(){
        mapOTables.keySet().stream().forEach(logger::info);
    }

    public void addTable(STable newTable){
        mapOTables.put(newTable.getName(), newTable);
    }

    public boolean dropTable(String tableName){
        if (null == mapOTables.remove(tableName)) {
            return false;
        }
        else{
            try(Statement tempStat = connection.createStatement()) {
                tempStat.execute(String.format("DROP TABLE %s", tableName));
            }catch(SQLException sql){
                logger.error("Error dropping table : " + sql.getMessage());
                logger.error("SQL Error Code : " + sql.getErrorCode());
                return false;
            }
        }

        return true;
    }

    public SDatabase copy(String otherName, String user, String pass){
        if(otherName.equals(name)) {
            logger.error("Copy cannot have same name as original.");
            return null;
        }

        try{
            SDatabase cpy = new SDatabase(otherName, user, pass);
            mapOTables.values().forEach(cpy::addTable);
            return cpy;
        }catch(SQLException sql){
            logger.error("Error while connecting to database \"{}\"", otherName);
            logger.error(sql.toString());
            return null;
        }
    }

    public SDatabase finalizedCopy(){
        try {
            SDatabase fix = new SDatabase(name, user, pass);
            mapOTables.values().forEach(fix::addTable);
            fix.makeFinal();
            return fix;
        } catch(SQLException sql){
            logger.error("Sql error : {}", sql.getMessage());
        }
    }

    public void makeFinal(){
        this.mapOTables = Collections.unmodifiableMap(mapOTables);
    }

    public void dropAll(){
        this.mapOTables = new HashMap<>();
    }

    public int size(){
        return this.mapOTables.size();
    }


    @Override public String toString() {
        final StringBuilder database = new StringBuilder();

        mapOTables.values().stream().forEach(table -> {
            try {
                database.append(table.project("*", null));
            } catch(SQLException sql){
                logger.error("Error returning database : " + sql.getMessage());
                logger.error("SQL Error code : " + sql.getErrorCode());
            }
            database.append("\n");
        });

        return database.toString();
    }
}
