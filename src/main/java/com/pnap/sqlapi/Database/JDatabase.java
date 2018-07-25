package com.pnap.sqlapi.Database;


import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class JDatabase implements Serializable{
    private Map<String, JTable> mapOTables = new HashMap<>();
    private String name;
    private static final Logger logger = LoggerFactory.getLogger(JDatabase.class);

    public JDatabase(String name){
        this.name = name;
    }

    /**
     * Generated {@link SDatabase} object from JDatabase.
     * @param dbskeleton required to have a connection to the database.
     * @return the SDatabase instance
     */
    public SDatabase toDatabase(SDatabase dbskeleton){

        // A loop in order to generate Tables from the JTable structures.
        for (JTable jTable : this.mapOTables.values()){

            // A set of columns from the table.
            // The entries have a string - the column name - and a Class - the column type.
            Set<Map.Entry<String, Class>> entries = jTable.getColumnMetaData().entrySet();

            // Generates tables.
            dbskeleton.createTable(
                    SColumn.generateColumns(entries), jTable.getName());
        }

        // Iterates over the JTables in order to insert the rows into the database skeleton.
        for (int i = 0; i < this.mapOTables.size(); i++) {
            JTable temp = this.mapOTables.values().toArray(new JTable[0])[i];
            STable temp2 = dbskeleton.getTable(i);

            for (int j = 0; j < temp.size(); j++) {
                Object[] row = temp.selectRow(j);
                temp2.insert(STable.objectArgsToStringArgs(row));
            }
        }

        return dbskeleton;
    }

    public JDatabase addTable(JTable table){
        if(!table.isFinalized()){
            logger.error("STable not finalised - table not added");
        }
        else if(mapOTables.values().contains(table)){
            logger.error("STable already in database - if update is required, call update function.");
        }
        else{
            mapOTables.put(table.getName(), table);
            logger.debug("STable ({}) added.", table.getName());
        }

        return this;
    }

    public JDatabase deleteTable(String name){
        mapOTables.remove(name);
        return this;
    }

    public JDatabase dropAll(){
        this.mapOTables = new HashMap<>();
        return this;
    }

    public JTable getTable(String name){
        JTable found = mapOTables.get(name);
        if(found != null) return found;

        logger.error("No table by that name found - creating temporary \"default\" JTable to avoid crash...");
        return new JTable("default");
    }

    public JDatabase copy(){
        JDatabase cpy = new JDatabase(this.name);

        mapOTables.values().forEach(cpy::addTable);

        return cpy;
    }

    public void displayDatabase(){
        logger.debug("Amount of tables : {}", mapOTables.size());
        mapOTables.values().forEach(JTable::displayTable);
    }

    public void saveJDatabaseToFile(String name){
        logger.debug("Attempting to save object to {}", name.concat(".db"));
        try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(name.concat(".db"))))){
            oos.writeObject(this);
        }catch(IOException io){
            logger.error("Couldn't save database to a file : {}", io.getMessage());
            logger.error("{}", io);
        }
    }

    @NotNull
    public static JDatabase loadJDatabaseFromFile(String name){
        logger.debug("Attempting to load object from {}", name.concat(".db"));
        try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(name.concat(".db"))))){
            return (JDatabase) ois.readObject();
        }catch(IOException|ClassNotFoundException cnio){
            logger.error("Couldn't load database from a file : {}", cnio.getMessage());
            cnio.printStackTrace();
            return new JDatabase("default");
        }
    }
}
