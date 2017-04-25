package com.youzan.wagon.filter.utils;

import com.youzan.wagon.filter.exception.FilterException;

public class TypeConverter {

    // java数据类型
    public final static String STRING = "string";
    public final static String INTEGER = "integer";
    public final static String LONG = "long";
    public final static String FLOAT = "float";
    public final static String DOUBLE = "double";
    public final static String DATE = "date";

    public static String mysql2JavaType(String mysqlType) {
        String type = mysqlType.toLowerCase();

        if (type.startsWith("int") || type.startsWith("integer")) {
            return LONG;
        } else if (type.startsWith("varchar") || type.startsWith("char") || type.startsWith("text")) {
            return STRING;
        } else if (type.startsWith("tinyint") || type.startsWith("smallint") || type.startsWith("mediumint")) {
            return INTEGER;
        } else if (type.startsWith("float")) {
            return FLOAT;
        } else if (type.startsWith("double")) {
            return DOUBLE;
        } else if (type.startsWith("datetime")) {
            return DATE;
        } else {
            throw new FilterException(String.format("not supported mysql type %s", mysqlType));
        }
    }

}
