package myXml.editor;

import myXml.util.*;
import myXml.xmlComponents.XMLContainer;
import myXml.xmlComponents.XMLLeaf;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XMLEditor {

    public static String documentName = "XMLDocument";
    //region Data and Constructor
    private final Deque<XMLWrapper> previousRootState;// where you were in the document
    private final int ROOT_STATE_CAPACITY = 5;
    private final int DOCUMENT_STATE_CAPACITY = 5;
    private final Deque<RootStateWrapper> previousDocumentState;
    private XMLContainer mainRoot;// points to all the root nodes;
    private XMLContainer currentNode;
    private XMLContainer currentRoot;
    private boolean initialized;
    private boolean toPrint = false;

    public XMLEditor() throws IOException {
        previousRootState = new ArrayDeque<>(ROOT_STATE_CAPACITY);
        previousDocumentState = new ArrayDeque<>(DOCUMENT_STATE_CAPACITY);
        this.mainRoot = new XMLContainer(documentName);
        this.currentNode = new XMLContainer();
        CommandHelper.init();
        initialized = false;
    }
    //endregion

    //region Initialization and Helper Methods

    private void initEditor(String command, String[] params) {
        while (!initialized) {
            if (params.length < 1 || !command.equals(CommandHelper.INIT_COMMAND)) {
                Log.invalidCommandMsg();
                Log.emptyDocumentMsg();
                break;
            }

            addRootUtil(params[0]);
            currentNode = currentRoot = getFirstRoot();
            initialized = true;
            toPrint = true;
            Log.logCommand(command, params);
            Log.initSuccessMsg();
            Log.currentNodeMsg(currentNode.getTag());
        }
    }

    private XMLContainer findNode(XMLContainer crnt, String targetTag) {
        if (crnt.getTag().equals(targetTag)) return crnt;
        Queue<XMLContainer> queue = new LinkedList<>();
        queue.add(crnt);
        while (!queue.isEmpty()) {
            XMLContainer curr = queue.poll();
            if (curr.getTag().equals(targetTag)) return curr;
            queue.addAll(curr.getChildren());
        }
        return null;
    }

    private XMLContainer getNextRoot() {
        List<XMLContainer> children = mainRoot.getChildren();
        int index = children.indexOf(currentRoot);
        if (++index > children.size() - 1) index = 0;
        return children.get(index);
    }

    //todo da ne sa vrakjat ako si vo istiot elem
    private XMLWrapper getPreviousRootState() {
        if (previousRootState.isEmpty()) throw new EmptyStackException();
        System.out.println("Went back");
        return previousRootState.pop();

    }

    private XMLContainer getFirstRoot() {
        XMLContainer result = mainRoot.getFirstChild();
        if (result.equals(mainRoot)) return null;
        return result;
    }

    public void run(String command, String[] params) {
        CommandProcessor processor = new CommandProcessor();
        if (CommandHelper.getAvailableCommands().contains(command)) {
            processor.processCommand(command, params);
            return;
        }

        if (!initialized) {
            initEditor(command, params);
            return;
        }

        processor.processCommand(command, params);

    }

    private void addRootUtil(String tagName) {
        XMLContainer element = new XMLContainer(tagName);
        element.setParent(mainRoot);
        mainRoot.addChild(element);
    }
    //endregion

    //region Printing and ToString
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        mainRoot.getChildren().forEach(child -> sb.append(child.toString()));
        return sb.toString();
    }

    public void printEditor() {
        System.out.println("------------------------------");
        System.out.print(this);
        System.out.println("------------------------------");
        Log.currentNodeMsg(currentNode.getTag());
    }

    public void printCurrent() {
        System.out.println(currentNode);
    }

    public void printCurrentRoot() {
        System.out.println(currentRoot);
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

    //endregion

    private class CommandProcessor {

        //region Data and Constructor
        private final Map<String, Consumer<String[]>> commandMap;

        public CommandProcessor() {
            commandMap = new HashMap<>();
            initializeCommandMap();
        }
        //endregion

        //region Add Methods
        private void addAttribute(String[] params) {
            if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addAttribute(params[0], params[1]);
            Log.attributeAddedMsg(params[0], params[1]);
        }

        private void addLeaf(String[] params) {
            if (params.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            String val = params.length > 2 ? Arrays.stream(params, 1, params.length).collect(Collectors.joining(" ")) : params[1];
            currentNode.addValue(new XMLLeaf(params[0], val));
            Log.leafAddedMsg(params[0], val);
        }

        private void addContainer(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            currentNode.addChild(new XMLContainer(params[0]));
            stepIn();
            Log.containerAddedMsg(params[0]);
        }

        private void addRoot(String[] params) {
            if (params.length < 1) throw new InvalidParameterException("Invalid number of parameters");
            addRootUtil(params[0]);
            nextRoot();
            Log.rootAdded(params[0]);
        }
        //endregion

        //region Command Processing and Help

        private void processCommand(String command, String[] params) {
            Consumer<String[]> commandHandler = commandMap.get(command);
            if (commandHandler != null) {
                Log.logCommand(command, params);
                commandHandler.accept(params);
                if (toPrint) printEditor();
            } else Log.invalidCommandMsg();
        }

        private void createTemplate(String[] name) {
            try {
                Log.saveTemplate(name[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        //endregion


        //region Traversal Methods

        private void nextRoot() {
            XMLWrapper currentWrap = new XMLWrapper(currentRoot, currentNode);
            if (previousRootState.size() == ROOT_STATE_CAPACITY) previousRootState.pollFirst();
            previousRootState.push(currentWrap);
            currentNode = currentRoot = getNextRoot();
        }

        private void prevRoot() {
            XMLWrapper prevState = getPreviousRootState();
            currentRoot = prevState.root();
            currentNode = prevState.element();
        }

        private void stepOut() {
            currentNode = currentNode.getParent();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void stepIn() {
            currentNode = currentNode.getFirstChild();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void nextInLevel() {
            currentNode = currentNode.getNext();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void top() {
            currentNode = currentRoot;
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void changeCurrentNode(String[] tagName) {
            XMLContainer target = findNode(currentRoot, tagName[0]);
            if (target == null) {
                System.out.println("Element not found");
                return;
            }
            currentNode = target;
            Log.currentNodeMsg(currentNode.getTag());
        }


        //endregion


        //region Initialization and Removal Utility

        private void initializeCommandMap() {
            commandMap.put("atrib", this::addAttribute);
            commandMap.put("leaf", this::addLeaf);
            commandMap.put("container", this::addContainer);
            commandMap.put("root", this::addRoot);
            commandMap.put("help", CommandHelper::getCommandHelp);
            commandMap.put("next-root", (params) -> nextRoot());
            commandMap.put("prev-root", (params) -> prevRoot());
            commandMap.put("up", (params) -> stepOut());
            commandMap.put("down", (params) -> stepIn());
            commandMap.put("next", (params) -> nextInLevel());
            commandMap.put("remove-c", (params) -> removeCurrent());
            commandMap.put("print-r", (params) -> printCurrentRoot());
            commandMap.put("print-c", (params) -> printCurrent());
            commandMap.put("print-a", (params) -> printEditor());
            commandMap.put("commands-all", (params) -> CommandHelper.displayAllCommands());
            commandMap.put("commands-avb", (params) -> CommandHelper.displayAvailableCommands());
            commandMap.put("change-c", this::changeCurrentNode);
            commandMap.put("top", (params) -> top());
            commandMap.put("clear", (params) -> clear());
            commandMap.put("revert", (params) -> revert());
            commandMap.put("swap", this::swap);
            commandMap.put("template", this::loadTemplate);
            commandMap.put("write", (params) -> writeToFile());
            commandMap.put("save-template", this::createTemplate);
            commandMap.put("delete-template", this::deleteTemplate);
            commandMap.put("show-templates", (params) -> Log.showTemplates());
            commandMap.put("ptog", (params) -> togglePrint());
        }

        private void revert() {
            if (!previousDocumentState.isEmpty()) {
                RootStateWrapper prevstate = previousDocumentState.pop();
                mainRoot = prevstate.root();
                initialized = prevstate.initialized();
                Log.revertLog();
                System.out.println("Reverted");
                printEditor();
            } else System.out.println("Can't revert");
        }

        private void swap(String[] tags) {
            if (tags.length < 2) throw new InvalidParameterException("Invalid number of parameters");
            XMLContainer node1 = findNode(mainRoot, tags[0]);
            XMLContainer node2 = findNode(mainRoot, tags[1]);

            if (node1 == null || node2 == null) return;

            int id1 = node1.getParent().getChildren().indexOf(node1);
            int id2 = node2.getParent().getChildren().indexOf(node2);

            node1.getParent().getChildren().remove(id1);
            node1.getParent().getChildren().add(id1, node2);
            node2.getParent().getChildren().remove(id2);
            node2.getParent().getChildren().add(id2, node1);
        }

        //privremeno
        // todo da sa sredit za pojke templates
        private void loadTemplate(String[] name) {
            clear();
            BufferedReader br;
            try {
                br = new BufferedReader(new InputStreamReader
                        (new FileInputStream("C:\\Users\\stefa\\IdeaProjects\\XMLEditor_v1\\templates\\" + name[0] + ".txt")));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            String line;
            try {
                line = br.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (line != null && !line.isEmpty()) {
                String[] parts = line.split("\\s+");
                String command = parts[0];
                String[] params = Arrays.copyOfRange(parts, 1, parts.length);
                processCommand(command, params);
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            initialized = true;
            System.out.println("Generated HTML template");
            printEditor();
        }


        private void removeCurrent() {
            currentNode.getParent().remove(currentNode);
            currentNode = currentNode.getParent();
            Log.currentNodeMsg(currentNode.getTag());
        }

        private void deleteTemplate(String[] name) {
            try {
                Log.deleteTemplate(name[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        private void clear() {
            if (previousDocumentState.size() == DOCUMENT_STATE_CAPACITY) previousDocumentState.pollFirst();
            previousDocumentState.push(new RootStateWrapper(mainRoot, initialized));
            initialized = false;
            mainRoot = new XMLContainer(documentName);
            currentNode = currentRoot = mainRoot;
            Log.clearLog();
            Log.documentClearedMsg();
        }

        private void togglePrint() {
            toPrint = !toPrint;
            if (toPrint) printEditor();
            System.out.println("Printing the document after every command is: " + toPrint);
        }

        //endregion

    }

}