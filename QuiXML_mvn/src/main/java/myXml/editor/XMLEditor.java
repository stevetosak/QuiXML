package myXml.editor;

import myXml.commands.help.CommandHelper;
import myXml.commands.help.InvalidCommandException;
import myXml.commands.manager.CommandManager;
import myXml.main.XMLMain;
import myXml.util.DocumentStateWrapper;
import myXml.util.Log;
import myXml.util.Messenger;
import myXml.components.XMLComponent;
import myXml.components.XMLContainer;
import myXml.components.XMLLeaf;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XMLEditor {
    //region Data and Initialization
    private final static int MAX_STACK_CAPACITY = 100;
    public static String documentName = "XMLDocument";
    private final Deque<DocumentStateWrapper> undoStack;
    private final Deque<DocumentStateWrapper> redoStack;
    private final Map<String, Consumer<String[]>> commandMap = new HashMap<>();
    private final DocumentStateWrapper document = new DocumentStateWrapper(documentName);
    private boolean toPrint = true;
    CommandManager manager;
    public XMLEditor() throws IOException {
        undoStack = new ArrayDeque<>(MAX_STACK_CAPACITY);
        redoStack = new ArrayDeque<>(MAX_STACK_CAPACITY);
        document.init();
        //CommandHelper.init();
        loadCommands();
    }

    public void processCommands(String commandName, String[] params, boolean logStateAndPrint) {
        Consumer<String[]> commandHandler = commandMap.get(commandName);
        if (commandHandler != null) {
            Log.logCommand(commandName, params); // zaradi clear
            if (logStateAndPrint) {
                logDocumentState(commandName, undoStack, "undo");
            }
            commandHandler.accept(params);
            if (toPrint) printEditor();
        } else Messenger.invalidCommandMsg();
    }

    private void commandHelp(String[] params){
        if(params.length == 0) CommandHelper.displayAllCommands();
        else {
            try {
                CommandHelper.getCommandHelp(params); //todo
            } catch (InvalidCommandException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void loadCommands() {
        commandMap.put("add", this::addNode);
        commandMap.put("atb", this::addAttribute);
        commandMap.put("leaf", this::addLeaf);
        commandMap.put("cont", this::addContainer);
        commandMap.put("root", this::addRoot);
        commandMap.put("help", this::commandHelp);
        commandMap.put("up", (params) -> stepOut());
        commandMap.put("down", (params) -> stepIn());
        commandMap.put("next", (params) -> nextInLevel());
        commandMap.put("rmc", (params) -> removeCurrent());
        commandMap.put("pr", (params) -> printCurrentRoot());
        commandMap.put("pc", (params) -> printCurrent());
        commandMap.put("pa", (params) -> printEditor());
        commandMap.put("nav", this::nav);
        commandMap.put("tpc", (params) -> top());
        commandMap.put("clear", (params) -> clear());
        commandMap.put("swap", this::swap);
        commandMap.put("ldtmp", this::loadTemplate);
        commandMap.put("write", (params) -> writeToFile());
        commandMap.put("svtmp", this::createTemplate);
        commandMap.put("rmtmp", this::deleteTemplate);
        commandMap.put("shtmp", (params) -> Log.showTemplates());
        commandMap.put("pt", (params) -> togglePrint());
        commandMap.put("tpd", (params) -> resetToTop());
        commandMap.put("cl", (params) -> Log.clearLog());
        commandMap.put("rl", (params) -> Log.revertLog());
        commandMap.put("sl", (params) -> Log.showLoggedCommands());
        commandMap.put("back", (params) -> previousInLevel());
        commandMap.put("rma", this::removeAttrib);
        commandMap.put("undo", (params) -> undo());
        commandMap.put("redo", (params) -> redo());
        commandMap.put("clog", (params) -> clearLog());
    }
    //endregion

    //region Add Methods
    private void addAttribute(String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        document.currentNode().addAttribute(params[0], params[1]);
        Messenger.attributeAddedMsg(params[0], params[1]);
    }

    private void addLeaf(String[] params) {
        if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
        document.currentNode().addChild(new XMLLeaf(params[0], val));
        XMLComponent node = findByTag(document.currentNode(), params[0]);
        if (document.currentNode() == document.mainRoot()) document.setNode("cr",node);
        document.setNode("cn",node);
        Messenger.leafAddedMsg(params[0], val);
    }

    private void addContainer(String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        document.currentNode().addChild(new XMLContainer(params[0]));
        XMLComponent node = findByTag(document.currentNode(), params[0]);
        if (document.currentNode() == document.mainRoot()) document.setNode("cr",node);
        document.setNode("cn",node);
        Messenger.containerAddedMsg(params[0]);
    }

    private void addRoot(String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        XMLContainer element = new XMLContainer(params[0]);
        element.setParent(document.mainRoot());
        document.mainRoot().addChild(element);
        Messenger.rootAdded(params[0]);
    }

    private void addNode(String[] params) {
        if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
        if (params.length == 1) addContainer(params);
        else addLeaf(params);
    }
    //endregion

    //region Helper and Search Methods

    private XMLComponent findByTag(XMLComponent node, String targetTag) {
        if (node.getTag().equals(targetTag)) return node;
        Queue<XMLComponent> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            XMLComponent curr = queue.poll();
            if (curr.getTag().equals(targetTag)) return curr;
            queue.addAll(curr.getChildren());
        }
        return null;
    }

    private boolean documentEmpty() {
        return document.mainRoot().getChildren().isEmpty();
    }

    private void findRoot(XMLComponent node) {
        if (node.getParent().equals(document.mainRoot()) || node.getParent() == null) {
            document.setNode("cr",node);
            return;
        }
        findRoot(node.getParent());
    }

    private void swap(String[] tags) {
        if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
        XMLComponent node1 = findByTag(document.mainRoot(), tags[0]);
        XMLComponent node2 = findByTag(document.mainRoot(), tags[1]);

        if (node1 == null || node2 == null) return;

        node1.setTag(tags[1]);
        node2.setTag(tags[0]);
    }

    private void findFromMainRoot(String tagName) { //Niz cel dokument bfs
        XMLComponent target = findByTag(document.mainRoot(), tagName);
        if (target != null) {
            document.setNode("cn",target);
            findRoot(document.currentNode());
        } else {
            System.out.println("Element not found");
        }
    }

    private void findFromRoot(String targetNodeTagName, String rootTagName) { // od daden root node bfs
        XMLComponent targetRoot = findByTag(document.mainRoot(), rootTagName);
        if (targetRoot != null) {
            XMLComponent targetNode = findByTag(targetRoot, targetNodeTagName);
            if (targetNode != null) {
                document.setNode("cn",targetNode);
                document.setNode("cr",targetRoot);
            } else {
                System.out.println("Target node not found");
            }
        } else {
            System.out.println("Target root not found");
        }
    }

    //endregion

    //region Printing and ToString
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (XMLComponent child : document.mainRoot().getChildren()){
            sb.append(child.generateXml(0,document.currentNode()));
        }
        return sb.toString();
    }

    public void printEditor() {
        System.out.println("------------------------------");
        System.out.print(this);
        System.out.println("------------------------------");
        Messenger.currentNodeMsg(document.currentNode().getTagNameFormatted(),
                (document.currentRoot().equals(document.currentNode()) ? document.mainRoot().getTagNameFormatted() : document.currentRoot().getTagNameFormatted()));
    }

    public void printCurrent() {
        System.out.println(document.currentNode().getTagNameFormatted());
    }

    public void printCurrentRoot() {
        System.out.println("Current root is: " + document.currentRoot().getTag());
    }

    private void writeToFile() {
        File output = new File("C:\\Users\\stefa\\OneDrive\\Desktop\\xmlOutput.txt");
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));
            bufferedWriter.write(this.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void togglePrint() {
        toPrint = !toPrint;
        System.out.println("Printing the document after every command is: " + toPrint);
    }

    //endregion

    //region Backtracking

    void logDocumentState(String commandName, Deque<DocumentStateWrapper> stack, String dontLog) {
        if (redoStack.isEmpty() && commandName.equals("redo") || commandName.equals(dontLog)) return;

        if (stack.size() == MAX_STACK_CAPACITY) stack.removeLast();
        DocumentStateWrapper wrapper = new DocumentStateWrapper(document.mainRoot().deepCopy(), document.currentRoot().deepCopy(), document.currentNode().deepCopy(), Log.getCommandLog());
        stack.push(wrapper);

    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            logDocumentState("undo", redoStack, "");
            loadState(undoStack);
        }
    }

    private void loadState(Deque<DocumentStateWrapper> undoStack) {
        DocumentStateWrapper prevState = undoStack.pop();
        document.setNode("mr",prevState.mainRoot());
        document.setNode("cr",findByTag(document.mainRoot(), prevState.currentRoot().getTag()));
        document.setNode("cn",findByTag(document.mainRoot(), prevState.currentNode().getTag()));// optimize
        Log.updateCommandLog(prevState.commandLog());
    }

    private void redo() {
        if (Log.getLastCommandName().equals("undo") || Log.getLastCommandName().equals("redo")) {
            if (!redoStack.isEmpty() && !undoStack.isEmpty()) {
                loadState(redoStack);
            }
        } else {
            redoStack.clear();
        }
    }

    private void clearLog() {
        System.out.println("Are you sure you want to delete all templates? This action can not be undone.\nType 'y' to confirm.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            char ans = (char) br.read();
            if (ans == 'y') {
                Log.clearTemplates();
                System.out.println("Deleted all templates.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    //endregion

    //region Template functionality
    private void createTemplate(String[] name) {
        try {
            Log.saveTemplate(name[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTemplate(String[] name) {
        try (FileInputStream fileIs = new FileInputStream("src/main/resources/templates/" + name[0] + ".txt")) {
            clear();
            XMLMain.commandLoop(XMLEditor.this, fileIs); // ova jako e hehe
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteTemplate(String[] name) {
        try {
            Log.deleteTemplate(name[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //endregion

    //region Traversal Methods

    private void resetToTop() {
        document.setNode("cn",document.mainRoot());
        document.setNode("cr",document.mainRoot());
    }

    private void stepOut() {
        document.setNode("cn",document.currentNode().getParent());
        findRoot(document.currentNode());
    }

    private void stepIn() {
        document.setNode("cn",document.currentNode().getFirstChild());
        findRoot(document.currentNode());
    }

    private void nextInLevel() {
        if (!documentEmpty()) {
            document.setNode("cn",document.currentNode().getNext());
            findRoot(document.currentNode());
        } else {
            System.out.println("Document is empty");
        }
    }

    private void previousInLevel() {
        if (!documentEmpty()) {
            document.setNode("cn",document.currentNode().getPrev());
            findRoot(document.currentNode());
        } else {
            System.out.println("Document is empty");
        }
    }

    private void top() {
        document.setNode("cn",document.currentRoot());
    }

    private void nav(String[] params) {
        if (params.length == 0) throw new InvalidParameterException("Invalid number of parameters");
        else if (params.length == 1) findFromMainRoot(params[0]);
        else findFromRoot(params[0], params[1]);

    }


    //endregion

    //region Removal Utility


    private void removeAttrib(String[] name) {
        if (name.length == 0) {
            document.currentNode().removeLastAttribute();
        } else {
            if (!document.currentNode().removeAttributeWithName(name[0])) {
                System.out.println("No attribute with name: " + name[0] + " found");
            }
        }
    }


    private void removeCurrent() {
        document.currentNode().getParent().removeChildNode(document.currentNode());
        document.setNode("cn",document.currentNode().getParent());
    }


    private void clear() {
        if (!documentEmpty()) {
            document.setNode("mr", new XMLContainer(documentName));
            document.setNode("cn", document.mainRoot());
            document.setNode("cr",document.mainRoot());
            Log.clearLog();
            Messenger.emptyDocumentMsg();
        }
    }


    //endregion


}