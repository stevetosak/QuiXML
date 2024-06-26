package myXml.commands;

import myXml.util.Messenger;

public class InvalidCommandException extends Exception {
    public InvalidCommandException() {
        super();
        Messenger.invalidCommandMsg();
        Messenger.emptyDocumentMsg();
    }
}
