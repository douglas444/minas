package br.com.douglas444.minas.util;

import org.w3c.dom.Document;

public class XMLUtils {

    public static boolean getXMLBooleanElementValue(final Document document, final String elementName)
            throws Exception {

        final String stringValue = document
                .getElementsByTagName(elementName)
                .item(0)
                .getTextContent();

        return toBoolean(elementName, stringValue);
    }

    public static int getXMLNumericElementValue(final Document document, final String elementName)
            throws Exception {

        final String stringValue = document
                .getElementsByTagName(elementName)
                .item(0)
                .getTextContent();

        int integerValue = -1;

        try {
            integerValue = Integer.parseInt(stringValue);
        } catch (Exception e) {
            throw new Exception("Value for attribute "
                    + elementName + " must be a integer", e);
        }

        return integerValue;
    }

    private static boolean toBoolean(final String attributeName, final String stringValue)
            throws Exception {

        boolean booleanValue;

        if (stringValue.equals("false") || stringValue.equals("true")) {
            booleanValue = Boolean.parseBoolean(stringValue);
        } else {
            throw new Exception("Value for attribute "
                    + attributeName + " must be a integer");
        }
        return booleanValue;
    }
}
