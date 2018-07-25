package com.pnap.sqlapi.Database;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;



public class STable {
    private List<SColumn> columns;
    private String name;

    private Statement statement;
    private static final Logger logger = LoggerFactory.getLogger(STable.class);

    public STable(Connection connection, List<SColumn> columns, String name) throws SQLException {
        this.statement = connection.createStatement();
        this.columns = columns;
        this.name = name;

        SQLayer.create(statement, name, columnsToString(columns));
    }

    public String getName() {
        return name;
    }

    public boolean insert(String... values){
        if (values.length != columns.size())
            return false;

        try {
            SQLayer.insert(statement, name, getColumnNames(columns), groupValues(values));
        } catch (SQLException sql){
            logger.error("SQL Insert Error : " + sql.getMessage());
            logger.error("SQL Error Code : " + sql.getErrorCode());
            return false;
        }

        return true;
    }

    public void update(String set, String where){
        try {
            SQLayer.update(statement, name, set, where);
        }catch (SQLException sql){
            logger.error("Could not update table : {}", sql.getSQLState());
        }
    }

    public void delete(String whereCondition){
        try {
            SQLayer.delete(statement, name, whereCondition);
        }catch(SQLException sql){
            logger.error("Could not delete entries : {}", sql.getSQLState());
        }
    }

    public String project(String columnQ, String whereQ) throws SQLException{

        StringBuilder output = new StringBuilder();

        int resultColumns;

        StringBuilder sb = new StringBuilder();
        sb.append("%s");

        ResultSet rs = select(columnQ, whereQ);
        ResultSetMetaData rsmd = rs.getMetaData();

        output.append("\n");

        for (resultColumns = 1; resultColumns < rsmd.getColumnCount(); resultColumns++) {
            sb.append(" - %s");
            output.append(rsmd.getColumnName(resultColumns)).append(" - ");
        }

        output.append(rsmd.getColumnName(resultColumns)).append("\n");
        output.append("--------------------------------------------------------------").append("\n");

        String displayer = sb.toString();

        while(rs.next()){

            List<String> details = new ArrayList<>();

            for (int i = 0; i < resultColumns; i++) {
                details.add(rs.getString(i+1));
            }

            output.append(String.format(displayer, details.toArray())).append("\n");
        }

        return output.toString();
    }

    public ResultSet select(String column, String where) throws SQLException{
        SQLayer.select(statement, name, column, where);
        return statement.getResultSet();
    }


    private List<List<Object>> dumpContents(){
        try {
            ResultSet rawDump = this.select("*", null);
            ResultSetMetaData tableData = rawDump.getMetaData();

            int columns = tableData.getColumnCount();

            List<List<Object>> fullTable = new ArrayList<>();

            while (rawDump.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 0; i < columns; i++) {
                    Object obj = rawDump.getObject(i + 1);
                    row.add(obj);
                }
                fullTable.add(row);
            }

            return fullTable;
        } catch(SQLException sql){
            logger.error("Error during dumping contents.");
            return new ArrayList<>();
        }
    }

    public List<SColumn> getColumns() {
        return columns;
    }

    private static String columnsToString(List<SColumn> columns){
        StringBuilder sb = new StringBuilder();

        String prefix = "";

        for(SColumn c : columns){
            sb.append(prefix);
            prefix = ",";
            sb.append(c);
        }

        return sb.toString();
    }

    public static String[] objectArgsToStringArgs(Object... values){
        List<String> vals = new ArrayList<>();

        for(Object obj : values){
            String add;
            if(obj.getClass() == String.class){
                add = "'".concat((String) obj).concat("'");
            }
            else{
                add = obj.toString();
            }
            vals.add(add);
        }

        return vals.toArray(new String[0]);
    }

    private static String getColumnNames(List<SColumn> columns){
        StringBuilder sb = new StringBuilder();

        String prefix = "";

        for(SColumn c : columns){
            sb.append(prefix);
            prefix = ",";
            sb.append(c.getColumnName());
        }

        return sb.toString();
    }

    public JTable toJTable(){
        JTable jtbl = new JTable(this.name);
        columns.forEach(column -> jtbl.addColumn(column.getColumnName(), column.getTypeClass()));

        jtbl.assemble();

        List<List<Object>> full = this.dumpContents();

        full.forEach(objects -> jtbl.insert(objects.toArray()));

        return jtbl;
    }

    private static String groupValues(String[] values){
        return String.join(",", values);
    }
}
