package com.pnap.sqlapi.Database;


import com.mysql.cj.xdevapi.Column;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class JTable implements Serializable{

    private static final Logger logger = LoggerFactory.getLogger(JTable.class);

    private List<JColumn> columns = new ArrayList<>();
    private boolean finalized = false;
    private String name;
    private Set<Class> acceptable;
    
    {
        acceptable = new HashSet<>();
        acceptable.add(Integer.class);
        acceptable.add(Byte.class);
        acceptable.add(Short.class);
        acceptable.add(Long.class);
        acceptable.add(Float.class);
        acceptable.add(Double.class);
        acceptable.add(BigDecimal.class);
        acceptable.add(Boolean.class);
        acceptable.add(String.class);
        acceptable.add(Date.class);
        acceptable.add(Time.class);
        acceptable.add(Timestamp.class);
    }

    public JTable(String name){
        this.name = name;
    }

    public JTable addColumn(String name, Class type){
        if(finalized){
            logger.error("STable has been finalised, no more columns can be added.");
        }
        else if(!acceptable.contains(type)){
            logger.error("Type passed ({}) not currently supported.", type.getName());
            logger.error("SColumn not added.");
        }
        else{
            this.columns.add(new JColumn(name, type));
            logger.debug("SColumn \"{}\" with type \"{}\" has been added.", name, type.getName());
        }

        return this;
    }

    public void assemble(){
        columns = Collections.unmodifiableList(columns);
        finalized = true;
        logger.debug("STable finalised");
    }

    public JTable insert(Object... things){

        if(!finalized){
            logger.error("Cannot add rows to non-finalised table - call assemble() first.");
            logger.error("Row not added.");
        }

        else if(things.length != columns.size()){
            logger.error("Amount of columns : {} , Parameters passed : {}", columns.size(), things.length);
            logger.error("Row not added.");
            return this;
        }

        List<Object> toAdd = new ArrayList<>();

        for (int i = 0; i < things.length; i++) {

            Object possibleCast = new Object();

            Class cType = columns.get(i).columnType;

            if(things[i].getClass() == Integer.class){
                Integer temp = ((Integer) things[i]);
                if(cType == Long.class) possibleCast = temp.longValue();
                else if(cType == Byte.class) possibleCast = temp.byteValue();
                else if(cType == Short.class) possibleCast = temp.shortValue();
            }
            else if(things[i].getClass() == Double.class){
                Double temp = ((Double) things[i]);
                if(cType == Float.class) possibleCast = temp.floatValue();
                else if(cType == BigDecimal.class) possibleCast = BigDecimal.valueOf(temp);
            }

            if(possibleCast.getClass() == Object.class) possibleCast = things[i];


            try{
                cType.cast(possibleCast);
            }catch(ClassCastException ccc){
                logger.error("Invalid parameters given. Please check that parameter types reflect column types.");
                logger.error("{}: {}", String.format("%-10s", "Parameter"), String.format("{%-20s, %-20s}", things[i], things[i].getClass().getName()));
                logger.error("{}: {}", String.format("%-10s", "SColumn"), String.format("{%-20s}", columns.get(i).columnType.getName()));
                logger.error("Row not added.");
                return this;
            }

            toAdd.add(possibleCast);
        }

        for(int i = 0; i < things.length; i++) {
            columns.get(i).add(toAdd.get(i));
        }

        logger.trace("Row added.");

        return this;
    }

    public Object[] selectRow(int index){
        List<Object> row = new ArrayList<>();

        for(JColumn c : columns){
            row.add(c.elements.get(index));
        }

        return row.toArray();
    }

    public JTable delete(int index){
        if(!finalized){
            logger.error("Cannot modify non-finalized table - call assemble() first.");
            logger.error("Row not deleted.");
        }

        else if(columns.size() > index) {
            logger.error("Index beyond size of table.");
            logger.error("Row not deleted.");
        }

        else{
            for (JColumn column : columns){
                column.remove(index);
            }
        }

        return this;
    }

    public JTable update(int index, Object... things){
        if(!finalized){
            logger.error("Cannot update non-finalized table - call assemble() first.");
            logger.error("Row not updated.");
        }
        else if(columns.size() > index) {
            logger.error("Index beyond size of table.");
            logger.error("Row not updated.");
        }
        else {
            int i = 0;
            for (JColumn c : columns) {
                c.elements.set(index, things[i]);
            }
        }

        return this;
    }

    public String getName(){
        return this.name;
    }

    public void displayTable(){
        if(!finalized){
            logger.info("{}Current state of STable ({}) Structure", anc.GREEN, name);
            logger.info("{}{}{}", anc.YELLOW, String.format("%-20s %-20s", "STable Name", "STable Type"), anc.WHITE);
            for(JColumn c : columns){
                logger.info("{}", String.format("%-20s %-20s", c.columnName, c.columnType.getName()));
            }
        }
        else{
            logger.info("{}Contents of STable \"{}\"", anc.CYAN, name);
            StringBuilder sb = new StringBuilder();

            for(JColumn c : columns){
                sb.append(String.format("%-35s", c.columnName));
            }

            logger.info("{}{}", anc.BLUE, sb.toString());

            for(int i = 0; i < columns.get(0).elements.size(); i++){
                StringBuilder sbn = new StringBuilder();
                for(JColumn c : columns){
                        sbn.append(String.format("%-35s", c.elements.get(i)));
                }
                logger.info("{}{}", anc.WHITE, sbn.toString());
            }
        }

        logger.info("{}", "\u001B[0m");
    }

    public boolean isFinalized(){
        return finalized;
    }

    public int size(){
        return this.columns.get(0).elements.size();
    }

    public STable toSTable(SDatabase host){
        Set<Map.Entry<String, Class>> entries = this.getColumnMetaData().entrySet();

        host.createTable(SColumn.generateColumns(entries), this.name);

        return host.getTable(this.name);
    }

    @Override public boolean equals(Object obj) {
        return obj!=null && obj instanceof JTable && this.name.equals(((JTable) obj).name);
    }


    /**
     * Returns a simple map of columns.
     * The KEY is the name of the column. The VALUE is the Classtype of the column.
     * @return The map of columns.
     */
    public Map<String, Class> getColumnMetaData(){
        Map<String, Class> columni = new LinkedHashMap<>();
        for (JColumn jc : this.columns){
            logger.debug("Putting JColumn in Map<> : [Name : {}] , [Class : {}]", jc.columnName, jc.columnType.getName());
            columni.put(jc.columnName, jc.columnType);
        }

        return columni;
    }


    private class JColumn implements Serializable{
        private String columnName;
        private List<Object> elements = new ArrayList<>();
        private Class columnType;

        //    public JColumn(String name){
        //        this.columnName = name;
        //        this.columnType = ((Class<T>) ((ParameterizedType) getClass()
        //                    .getGenericSuperclass()).getActualTypeArguments()[0]);
        //    }

        public JColumn(String name, Class type){
            this.columnName = name;
            this.columnType = type;
        }

        public void add(Object element){
            elements.add(columnType.cast(element));
        }

        public boolean remove(){
            return (elements.remove(elements.size()-1) != null);
        }

        public boolean remove(int index){
            if(index >= elements.size()) return false;
            else{
                return (elements.remove(index) != null);
            }
        }

        public String getColumnName() {return this.columnName;}
        public Class getColumnType(){
            return this.columnType;
        }
    }

}
