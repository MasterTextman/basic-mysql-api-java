package com.pnap.sqlapi.Database;


import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class SColumn {
    private String columnName;
    private String columnType;
    private Class typeClass;
    private Map<String, Class> TYPE;
    private static Map<String, Class> STATICTYPE;

    static {
        STATICTYPE = new HashMap<>();
        STATICTYPE.put("INT", Integer.class);
        STATICTYPE.put("INTEGER", Integer.class);
        STATICTYPE.put("TINYINT", Byte.class);
        STATICTYPE.put("SMALLINT", Short.class);
        STATICTYPE.put("BIGINT", Long.class);
        STATICTYPE.put("REAL", Float.class);
        STATICTYPE.put("FLOAT", Double.class);
        STATICTYPE.put("DOUBLE", Double.class);
        STATICTYPE.put("DECIMAL", BigDecimal.class);
        STATICTYPE.put("NUMERIC", BigDecimal.class);
        STATICTYPE.put("BOOLEAN", Boolean.class);
        STATICTYPE.put("CHAR", String.class);
        STATICTYPE.put("VARCHAR", String.class);
        STATICTYPE.put("TEXT", String.class);
        STATICTYPE.put("LONGVARCHAR", String.class);
        STATICTYPE.put("DATE", Date.class);
        STATICTYPE.put("TIME", Time.class);
        STATICTYPE.put("TIMESTAMP", Timestamp.class);
    }

    public SColumn(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.typeClass = STATICTYPE.get(columnType);
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public Class getTypeClass() {
        return typeClass;
    }

    public static List<SColumn> generateColumns(Set<Map.Entry<String, Class>> columns){
        List<SColumn> columni = new ArrayList<>();

        for (Map.Entry<String, Class> column : columns){

//            String sqlType = null;
//
//            for (Map.Entry<String, Class> entry : STATICTYPE.entrySet()){
//                if(entry.getValue() == column.getValue()){
//                    sqlType = entry.getKey();
//                    break;
//                }
//            }

            String sqlType = STATICTYPE.entrySet().stream()
                .filter(entry -> entry.getValue() == column.getValue())
                    .map(Map.Entry::getKey)
                    .findFirst().get();

            columni.add(new SColumn(column.getKey(), sqlType));
        }

        return columni;
    }


    @Override public String toString() {
        return String.format("%s %s", columnName, columnType);
    }
}
