package myXml.util;

public class Log {
    public static void initSuccessMsg() {
        System.out.println("Initialization successful");
        System.out.println("To see the list of available commandList type \"showcommands\" :)");
    }

    public static void currentNodeMsg(String currentNodeTag) {
        System.out.println("Current node is: " + currentNodeTag);
    }

    public static void leafAddedMsg(String name, String value) {
        System.out.println("Added leaf element: " + name + " with the content inside: " + value);
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

    public static void documentClearedMsg() {
        System.out.println("Document cleared");
        emptyDocumentMsg();
        System.out.println("You can revert to the previous state of the document by typing \"revert\"");
    }

    public static void emptyDocumentMsg() {
        System.out.println("Document is empty ");
        System.out.println("To get started you can either:");
        System.out.println("\t- Load from a template using \"template (name)\" ");
        System.out.println("\t- Type \"addroot (tagName)\"  to add a tag to the document");
    }

}
