package com.youzan.wagon.common.util;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {
    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);
    public static String LINE_SEPARATOR = "\r\n";
    public static Set<String> objSet;

    static {
        objSet = new HashSet<String>(3);
        objSet.add("string");
        objSet.add("integer");
        objSet.add("long");
    }

    /**
     * string to unicode
     * 
     * @param str
     * @return string
     */
    public static String toUnicode(String str) {
        return StringEscapeUtils.escapeJava(str);
        /**
         * StringBuilder retStr = new StringBuilder(); for(int i=0;
         * i<str.length(); i++) { int cp = Character.codePointAt(str, i); int
         * charCount = Character.charCount(cp); if (charCount > 1) { i +=
         * charCount - 1; // 2. if (i >= str.length()) { throw new
         * IllegalArgumentException("truncated unexpectedly"); } }
         * 
         * if (cp < 128) { retStr.appendCodePoint(cp); } else {
         * retStr.append(String.format("\\u%x", cp)); } } return
         * retStr.toString();
         **/
    }

    public static boolean isBlank(String str) {
        return org.apache.commons.lang.StringUtils.isBlank(str);
    }

    public static boolean isNotBlank(String str) {
        return org.apache.commons.lang.StringUtils.isNotBlank(str);
    }

    public static String capitalize(String str) {
        return org.apache.commons.lang.StringUtils.capitalize(str);
    }

    /**
     * 暂只支持嵌套list
     * 
     * @param obj
     * @return
     * @throws Exception
     */
    public static String obj2Json(Object obj) {
        try {
            Class<?> clazz = obj.getClass();
            Field[] allFields = clazz.getDeclaredFields();
            StringBuilder sb = new StringBuilder("{");
            for (Field field : allFields) {
                String fieldName = field.getName();
                String typeName = field.getType().getSimpleName().toLowerCase();

                Object value = clazz.getMethod(getMethod(fieldName)).invoke(obj);

                if (value == null)
                    continue;
                switch (typeName) {
                    case "string":
                        sb.append("\"").append(fieldName).append("\":\"").append(toUnicode((String) value)).append("\",");
                        break;
                    case "integer":
                    case "long":
                        sb.append("\"").append(fieldName).append("\":").append(value).append(",");
                        break;
                    case "list":
                    case "arraylist":
                        sb.append("\"").append(fieldName).append("\":").append(list2Json((List<?>) value)).append(",");
                        break;
                    default:
                        break;
                }
                // if (typeName.equalsIgnoreCase("String"))
                // sb.append("\"").append(fieldName).append("\":\"").append(toUnicode((String)value)).append("\",");
                // else
                // sb.append("\"").append(fieldName).append("\":").append(value).append(",");
            }

            int len = sb.length() - 1;
            char ch = sb.charAt(len);
            if (ch == ',') {
                sb.deleteCharAt(len);
            }
            return sb.append("}").toString();
        } catch (Exception e) {
            log.error("##ERROR StringUtils.obj2Json goes wrong at {}\n", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    public static String list2Json(List<?> list) {
        try {
            StringBuilder sb = new StringBuilder("[");
            for (Object obj : list) {
                Class<?> clazz = obj.getClass();
                String clazzName = clazz.getSimpleName().toLowerCase();
                if (objSet.contains(clazzName)) {
                    switch (clazzName) {
                        case "string":
                            sb.append("\"").append(toUnicode((String) obj)).append("\",");
                            break;
                        case "integer":
                        case "long":
                            sb.append(obj).append(",");
                            break;
                        default:
                            break;
                    }
                } else {
                    sb.append("{");
                    Field[] allFields = clazz.getDeclaredFields();
                    for (Field field : allFields) {
                        String fieldName = field.getName();
                        String typeName = field.getType().getSimpleName();

                        Object value = clazz.getMethod(getMethod(fieldName)).invoke(obj);

                        if (value == null)
                            continue;
                        if (typeName.equalsIgnoreCase("String"))
                            sb.append("\"").append(fieldName).append("\":\"").append(toUnicode((String) value)).append("\",");
                        else
                            sb.append("\"").append(fieldName).append("\":").append(value).append(",");
                    }

                    int len = sb.length() - 1;
                    char ch = sb.charAt(len);
                    if (ch == ',') {
                        sb.deleteCharAt(len);
                    }
                    sb.append("},");
                }
            }

            int len = sb.length() - 1;
            char ch = sb.charAt(len);
            if (ch == ',') {
                sb.deleteCharAt(len);
            }
            return sb.append("]").toString();
        } catch (Exception e) {
            log.error("##ERROR StringUtils.obj2Json goes wrong at {}\n", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    private static String getMethod(String name) {
        return new StringBuilder("get").append(capitalize(name)).toString();
    }
}
