package org.plateaubuilder.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AttributeValidator {

    public static Boolean checkValue(String value, String type) {
        switch (type) {
            case "xs:boolean":
                return checkBoolean(value);
            case "xs:date":
                return checkDate(value);
            case "xs:gYear":
                return checkGYear(value);
            case "xs:integer":
                return checkInteger(value);
            case "xs:nonNegativeInteger":
                return checkNonNegativeInteger(value);
            default:
                return true;
        }
    }

    private static Boolean checkBoolean(String value) {
        return value.equals("true") || value.equals("false");
    }

    private static Boolean checkDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        try {
            format.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private static Boolean checkGYear(String value) {
        return value.matches("\\d{4}");
    }

    private static Boolean checkInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Boolean checkNonNegativeInteger(String value) {
        try {
            return Integer.parseInt(value) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}