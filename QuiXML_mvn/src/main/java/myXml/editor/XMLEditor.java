package myXml.editor;


import myXml.commands.help.Command;
import myXml.commands.help.CommandHelper;
import myXml.commands.manager.*;
import myXml.commands.manager.ClassIdentifier;
import myXml.util.DocumentStateWrapper;
import myXml.util.Log;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.*;

public class XMLEditor {
    public static String documentName = "XMLDocument";
    private DocumentStateWrapper document = new DocumentStateWrapper(documentName);
    private final DisplayManager display = new DisplayManager();
    private final StateManager state = new StateManager();
    private final Map<ClassIdentifier, Class<? extends CommandManager>> identifierMap = new HashMap<>();

    public XMLEditor() throws IOException {
        identifierMap.put(ClassIdentifier.NODE, NodeManager.class);
        identifierMap.put(ClassIdentifier.DISPLAY, DisplayManager.class);
        identifierMap.put(ClassIdentifier.MOVEMENT, MovementManager.class);
        identifierMap.put(ClassIdentifier.STATE, StateManager.class);
        identifierMap.put(ClassIdentifier.TEMPLATE,TemplateManager.class);
        document.init();
    }

    public DocumentStateWrapper getDocument(){
        return document;
    }

    public void processCommands(String commandName, String[] params, boolean logStateAndPrint) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Command cmd = CommandHelper.getCommand(commandName);
        if(cmd != null){
            if(identifierMap.containsKey(cmd.classId)){
                Class<? extends CommandManager> cmdClass = identifierMap.get(cmd.classId);
                CommandManager manager = cmdClass.getDeclaredConstructor().newInstance();
                Method method = CommandManager.processAnnotations(manager,commandName);
                if(method != null){
                    Log.logCommand(commandName, params); // zaradi clear
                    if (logStateAndPrint) state.logDocumentState(document,commandName, "undo", "undo");
                    XMLEditor editor = (XMLEditor) method.invoke(manager,document,params);
                    if(editor != null){
                        this.document = editor.getDocument();
                        Log.clearLog();
                    }
                    if (DisplayManager.toPrint) display.printEditor(document,params);
                }
            }
        }
        // Star nacin
//        Consumer<String[]> function = commandMap.get(commandName);
//        if (function != null) {
//            Log.logCommand(commandName, params); // zaradi clear
//            if (logStateAndPrint) {
//                logDocumentState(commandName, undoStack, "undo");
//            }
//            function.accept(params);
//            if (toPrint) printEditor();
//        } else Messenger.invalidCommandMsg();
    }
}