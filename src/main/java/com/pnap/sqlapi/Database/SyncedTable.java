package com.pnap.sqlapi.Database;


public class SyncedTable {
    private STable connected;
    private JTable syncedTBL;
    private SyncedDatabase syncedDatabase;
    private String name;
    private boolean synced;

    public SyncedTable(STable connected, SyncedDatabase syncedDatabase){
        this.connected = connected;
        this.syncedTBL = connected.toJTable();
        this.syncedDatabase = syncedDatabase;
        this.name = connected.getName();
    }

    public SyncedTable addRow(Object... values){
        this.syncedTBL.insert(values);
        this.connected.insert(STable.objectArgsToStringArgs(values));
        return this;
    }

    public SyncedTable delete(String condition){
        this.connected.delete(condition);
        this.syncedTBL = this.connected.toJTable();
        return this;
    }

    public SyncedTable delete(int index){
        this.syncedTBL.delete(index);
        this.connected = this.syncedTBL.toSTable(syncedDatabase.getSDatabase());
        return this;
    }

    public SyncedTable update(String set, String where){
        this.connected.update(set, where);
        this.syncedTBL = connected.toJTable();
        return this;
    }

    public void syncTables(STable tbl){
        this.connected = tbl;
        this.syncedTBL = tbl.toJTable();
    }

    public JTable copyJTable(){
        return connected.toJTable();
    }

    public String getName(){
        return this.name;
    }
}
