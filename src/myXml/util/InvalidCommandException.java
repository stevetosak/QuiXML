package myXml.util;

public class InvalidCommandException extends Exception{
    public InvalidCommandException(){
        super();
        Log.invalidCommandMsg();
        Log.emptyDocumentMsg();
    }
}
