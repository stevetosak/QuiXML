package myXml.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.nio.file.*;
import java.util.Stack;
import java.util.stream.Stream;

// ke pram undo za ako izbrisis template po greska
// druga klasa za cuvanje dosega komandi ova klasa samo za poraki

public class Log {
    private static final Stack<LinkedList<RawCommand>> previousLogState = new Stack<>();
    private static LinkedList<RawCommand> commandLog = new LinkedList<>();

    public static void logCommand(String commandName, String[] params) {
        commandLog.add(new RawCommand(commandName, params));
    }

    public static void initSuccessMsg() {
        System.out.println("Initialization successful");
        System.out.println("To see the all of the commands type \"cmd-all\" :)");
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
        System.out.println("\t - Type \"help add\" ");
        System.out.println("\t- Load from a template using \"load-t (name)\" ");
    }

    public static void showLoggedCommands() {
        commandLog.forEach(System.out::println);
    }

    private static String createFileTemplate(String name) {
        String dirPath = "templates";
        String fileName = name + ".txt";

        Path directoryPath = Paths.get(dirPath);
        Path filePath = Paths.get(dirPath, fileName);

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                System.out.println("Template " + name + " was created");
            }
        } catch (Exception ignored) {
        }

        return filePath.toString();
    }


    public static void saveTemplate(String name) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(createFileTemplate(name)));
        StringBuilder sb = new StringBuilder();
        commandLog.forEach(command -> {
            String commandName = command.getName();
            if (!commandName.equals("load")
                    && !commandName.equals("save-t")
                    && !commandName.equals("ptog")
                    && !commandName.equals("delete-t")) {
                sb.append(command.commandFormat()).append('\n');
            }
        });
        sb.append("top").append("\n");
        sb.append("print-a").append("\n");

        sb.deleteCharAt(sb.length() - 1);
        bw.write(sb.toString());
        bw.close();
    }

    public static void deleteTemplate(String name) throws IOException {
        Path filePath = Path.of("templates/" + name + ".txt");
        Files.delete(filePath);
        System.out.println("Template " + name + " successfully deleted");
    }

    public static void showTemplates() {
        Path directory = Path.of("templates");
        System.out.println("Available templates: ");
        try {
            try (Stream<Path> files = Files.list(directory)) {
                files.forEach(file -> System.out.println(file.getFileName()));
            }
        } catch (IOException ignored) {
        }
    }

    public static void clearLog() {
        previousLogState.push(commandLog);
        commandLog = new LinkedList<>();
        System.out.println("Command log successfully cleared");
    }

    public static void revertLog() {
        if (previousLogState.empty()) {
            System.out.println("No previous logs");
            return;
        }
        commandLog = previousLogState.pop();
    }

    // save template (ime)

}
