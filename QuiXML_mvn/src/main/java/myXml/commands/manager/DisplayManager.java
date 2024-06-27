package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.commands.help.CommandHelper;
import myXml.commands.help.InvalidCommandException;
import myXml.components.XMLComponent;
import myXml.util.DocumentStateWrapper;
import myXml.util.Messenger;

public class DisplayManager implements CommandManager {

    public static boolean toPrint = true;
    public String serializeDocument(DocumentStateWrapper document){
        final StringBuilder sb = new StringBuilder();
        for (XMLComponent child : document.mainRoot().getChildren()){
            sb.append(child.generateXml(0,document.currentNode()));
        }
        return sb.toString();
    }

    @CommandHandler(names = {"pd","print document"})
    public void printEditor(DocumentStateWrapper document,String [] params) {
        System.out.println("------------------------------");
        System.out.print(serializeDocument(document));
        System.out.println("------------------------------");
        Messenger.currentNodeMsg(document.currentNode().getTagNameFormatted(),
                (document.currentRoot().equals(document.currentNode()) ? document.mainRoot().getTagNameFormatted() : document.currentRoot().getTagNameFormatted()));
    }

    @CommandHandler(names = {"pc","print current"})
    public void printCurrent(DocumentStateWrapper document,String [] params) {
        System.out.println(document.currentNode().getTagNameFormatted());
    }

    @CommandHandler(names = {"pr","print root"})
    public void printCurrentRoot(DocumentStateWrapper document,String [] params) {
        System.out.println("Current root is: " + document.currentRoot().getTag());
    }

    @CommandHandler(names = "help")
    public void commandHelp(DocumentStateWrapper document,String[] params){
        if(params.length == 0) CommandHelper.displayAllCommands();
        else {
            try {
                CommandHelper.getCommandHelp(params); //todo
            } catch (InvalidCommandException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    @CommandHandler(names = {"pt","ptog"})
    public void togglePrint(DocumentStateWrapper document,String [] args) {
        toPrint = !toPrint;
        System.out.println("Printing the document after every command is: " + toPrint);
    }




}
