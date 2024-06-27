package myXml.util;

/**
 * This class is responsible for, as the name suggests, providing messages to the user.
 */
public class Messenger {
    public static void currentNodeMsg(String currentNodeTag, String currentRootTag) {
        System.out.println("Current node is: " + currentNodeTag + " in " + currentRootTag);
    }

    public static void leafAddedMsg(String name, String value) {
        System.out.println("Added leaf node: " + name + " with the content inside: " + value);
    }

    public static void attributeAddedMsg(String name, String value) {
        System.out.println("Added attribute: " + name + " with value of: " + value);
    }

    public static void containerAddedMsg(String tagName) {
        System.out.println("Added container: " + tagName);
    }

    public static void rootAdded(String tagName) {
        System.out.println("Added root: " + tagName);
    }

    public static void invalidCommandMsg() {
        System.out.println("Invalid command");
    }


    public static void emptyDocumentMsg() {
        System.out.println("Document is empty");
        System.out.println("\t- Type \"add (tagName) ?(textContent)? to add an XML node to the document.\"");
        System.out.println("\t- Type \"lt (templateName)\" to load a document from a template.");
        System.out.println("\t- Type \"help\" to see the list of commands.");
        System.out.println("To toggle printing on/off type: \"pt\"");
    }
}
