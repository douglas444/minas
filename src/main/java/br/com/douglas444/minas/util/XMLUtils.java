package br.com.douglas444.minas.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XMLUtils {

    public static boolean getXMLBooleanAttribute(Document document, String elementName, String attributeName) {

        final String stringValue = document
                .getElementsByTagName(elementName)
                .item(0)
                .getAttributes()
                .getNamedItem(attributeName)
                .getNodeValue();

        return toBoolean(attributeName, stringValue);
    }

    public static int getXMLNumericAttribute(Document document, String elementName, String attributeName) {

        final String stringValue = document
                .getElementsByTagName(elementName)
                .item(0)
                .getAttributes()
                .getNamedItem(attributeName)
                .getNodeValue();

        int integerValue = -1;

        try {
            integerValue = Integer.parseInt(stringValue);
        } catch (Exception e) {
            System.out.println("Value for attribute " + attributeName + " must be a integer");
            System.exit(1);
        }

        return integerValue;
    }

    private static boolean toBoolean(String attributeName, String stringValue) {
        boolean booleanValue = false;

        if (stringValue.equals("false") || stringValue.equals("true")) {
            booleanValue = Boolean.parseBoolean(stringValue);
        } else {
            System.out.println("Value for attribute " + attributeName + " must be a integer");
            System.exit(1);
        }
        return booleanValue;
    }
}
