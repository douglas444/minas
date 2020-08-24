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

    public static String getXMLStringAttribute(Node node, String attributeName) {
        return node.getAttributes().getNamedItem(attributeName).getNodeValue();
    }

    public static boolean getXMLBooleanAttribute(Node node, String attributeName) {
        String stringValue = node.getAttributes().getNamedItem(attributeName).getNodeValue();
        return toBoolean(attributeName, stringValue);
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

    public static List<Node> getXMLNodeChildNodes(Document document, String elementName) {
        final NodeList nodeList = document.getElementsByTagName(elementName).item(0).getChildNodes();
        final List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                nodes.add(node);
            }
        }
        return nodes;
    }
}
