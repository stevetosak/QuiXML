package myXml.util;

import myXml.commands.RawCommand;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.stream.Stream;

// druga klasa za cuvanje dosega komandi ova klasa samo za poraki

/**
 * The log class is responsible for keeping track (logging)
 * all the input commands and for creation and storage of templates.
 */

public class Log {
    private static final Stack<LinkedList<RawCommand>> previousLogState = new Stack<>();
    private static LinkedList<RawCommand> commandLog = new LinkedList<>();

    public static void logCommand(String commandName, String[] params) {
        if (!commandName.equals("show-logs"))
            commandLog.add(new RawCommand(commandName, params));
    }

    public static void updateCommandLog(LinkedList<RawCommand> commands) {
        commandLog = commands;
    }

    public static LinkedList<RawCommand> getCommandLog() {
        return commandLog;
    }


    public static void showLoggedCommands() {
        StringBuilder sb = new StringBuilder();
        sb.append("Logged commands: [");
        commandLog.forEach(command -> sb.append("\"").append(command.commandFormat()).append("\","));
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
        System.out.println(sb);

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

    public static void clearTemplates() {
        Path templatesDir = Paths.get("templates");
        if (!Files.exists(templatesDir)) return;

        try (Stream<Path> filesInDir = Files.walk(templatesDir)) {
            filesInDir.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLastCommandName() {
        return commandLog.get(commandLog.size() - 2).getName();
    }

    private static boolean isLoggable(String commandName) {
        String[] unLoggableCommands = {
                "lt", "pt", "pr", "pc", "pa",
                "write", "cmd", "st", "rmt", "help", "clear",
                "save"
        };

        for (String cmd : unLoggableCommands) {
            if (commandName.equals(cmd)) {
                return false;
            }
        }
        return true;
    }

    public static void saveTemplate(String templateName) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(createFileTemplate(templateName)));
        StringBuilder sb = new StringBuilder();
        commandLog.forEach(command -> {
            String commandName = command.getName();
            if (isLoggable(commandName)) {
                sb.append(command.commandFormat()).append('\n');
            }
        });
        sb.append("td").append("\n");

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


}
