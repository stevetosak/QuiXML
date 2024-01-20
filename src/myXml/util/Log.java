package myXml.util;

public class Log {
    public static void initSuccessMsg(){
        System.out.println("Initialization successful");
        System.out.println("To see the list of available commands type \"showcommands\" :)");
    }
    public static void emptyDocumentMsg(){
        System.out.println("Document is empty, add an element to get started");
        System.out.println("Type \"addroot (tagName)\"  to add a tag to the document");
    }
    public static void currentNodeMsg(String currentNodeTag){
        System.out.println("Current node is: " + currentNodeTag);
    }
    public static void leafAddedMsg(String name, String value){
        System.out.println("Added leaf element: " + name + " with the content inside: " + value);
    }
    public static void attributeAddedMsg(String name, String value){
        System.out.println("Added attribute: " + name + " with value of: " + value);
    }

    public static void containerAddedMsg(String tagName){
        System.out.println("Added container: " + tagName);
    }
    public static void rootAdded(String tagName){
        System.out.println("Added root: " + tagName);
    }

}
