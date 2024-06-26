package myXml.commands.help;

import myXml.util.Messenger;

public class InvalidCommandException extends Exception {
    public InvalidCommandException() {
        super();
        Messenger.invalidCommandMsg();
    }
}
