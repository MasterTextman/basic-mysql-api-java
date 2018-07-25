package com.pnap.sqlapi.Database;


import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



public class SyncedDatabase {
    private SDatabase sqlDB;
    private JDatabase jDB;
    private List<SyncedTable> syncedTables = new ArrayList<>();
    private String name;

    private static final Logger logger = LoggerFactory.getLogger(SyncedDatabase.class);

    public SyncedDatabase(SDatabase sqldb){
        this.sqlDB = sqldb;
        this.jDB = sqldb.toJDatabase();
        this.syncedTables = generateSyncedTables();
    }

    public SyncedDatabase(JDatabase jdb, SDatabase emptySchema){
        emptySchema.dropAll();

        this.sqlDB = jdb.toDatabase(emptySchema);
        this.jDB = jdb;
        this.syncedTables = generateSyncedTables();
    }

    public SyncedDatabase addTable(STable sqltbl){
        this.sqlDB.addTable(sqltbl);
        this.jDB.addTable(sqltbl.toJTable());
        return this;
    }

    public SyncedDatabase addTable(JTable jtbl){
        this.jDB.addTable(jtbl);
        this.sqlDB.addTable(jtbl.toSTable(sqlDB));
        return this;
    }

    public SyncedDatabase removeTable(String name){
        this.sqlDB.dropTable(name);
        this.jDB.deleteTable(name);
        return this;
    }

    public SyncedDatabase dropAll(){
        sqlDB.dropAll();
        jDB.dropAll();
        return this;
    }

    public JDatabase getJDatabase(){
        return this.jDB.copy();
    }
    public SDatabase getSDatabase() {
        return this.sqlDB;
    }

    public SyncedTable getSyncedTable(String name){
        logger.debug("syncedTables.toString : {}", syncedTables);
        for (SyncedTable s : syncedTables){
            logger.debug("individual syncedTable toString : {}", s);
            if(s.getName().equals(name)) return s;
        }

        logger.error("SyncedTable with name \"{}\" does not exist", name);
        return null;
    }

    private List<SyncedTable> generateSyncedTables(){
        SDatabase current = sqlDB;

        List<SyncedTable> synctbls = new ArrayList<>();

        for (int i = 0; i < current.size(); i++) {
            synctbls.add(new SyncedTable(sqlDB.getTable(i), this));
        }

        return synctbls;
    }
}
