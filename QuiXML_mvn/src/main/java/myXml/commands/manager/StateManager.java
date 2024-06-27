package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.util.DocumentStateWrapper;
import myXml.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;

public class StateManager implements CommandManager{

    private final static int MAX_STACK_CAPACITY = 100;
    private static final Deque<DocumentStateWrapper> undoStack = new ArrayDeque<>();
    private static final Deque<DocumentStateWrapper> redoStack = new ArrayDeque<>();
    private final UtilityManager utilityManager = new UtilityManager();

    public void logDocumentState(DocumentStateWrapper document,String commandName,String whichStack, String dontLog) {
        if (redoStack.isEmpty() && commandName.equals("redo") || commandName.equals(dontLog)) return;
        if(whichStack.equals("undo")){
            if (undoStack.size() == MAX_STACK_CAPACITY) undoStack.removeLast();
            DocumentStateWrapper wrapper = new DocumentStateWrapper(document.mainRoot().deepCopy(), document.currentRoot().deepCopy(), document.currentNode().deepCopy(), Log.getCommandLog());
            undoStack.push(wrapper);
        } else if(whichStack.equals("redo")){
            if (redoStack.size() == MAX_STACK_CAPACITY) redoStack.removeLast();
            DocumentStateWrapper wrapper = new DocumentStateWrapper(document.mainRoot().deepCopy(), document.currentRoot().deepCopy(), document.currentNode().deepCopy(), Log.getCommandLog());
            redoStack.push(wrapper);
        }

    }
    @CommandHandler(names = "undo")
    public void undo(DocumentStateWrapper document,String [] params) {
        if (!undoStack.isEmpty()) {
            logDocumentState(document,"undo", "redo", "");
            loadState(document,undoStack);
        }
    }
    @CommandHandler(names = "redo")
    public void redo(DocumentStateWrapper document, String[] params) {
        if (Log.getLastCommandName().equals("undo") || Log.getLastCommandName().equals("redo")) {
            if (!redoStack.isEmpty() && !undoStack.isEmpty()) {
                loadState(document,redoStack);
            }
        } else {
            redoStack.clear();
        }
    }

    private void loadState(DocumentStateWrapper document,Deque<DocumentStateWrapper> undoStack) {
        DocumentStateWrapper prevState = undoStack.pop();
        document.setNode("mr",prevState.mainRoot());
        document.setNode("cr",utilityManager.findByTag(document.mainRoot(), prevState.currentRoot().getTag()));
        document.setNode("cn",utilityManager.findByTag(document.mainRoot(), prevState.currentNode().getTag()));// optimize
        Log.updateCommandLog(prevState.commandLog());
    }




}
