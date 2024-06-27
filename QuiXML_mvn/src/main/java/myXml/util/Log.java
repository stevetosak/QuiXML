package myXml.util;

import myXml.commands.help.Command;

import java.util.LinkedList;
import java.util.Stack;

// druga klasa za cuvanje dosega komandi ova klasa samo za poraki

/**
 * The log class is responsible for keeping track (logging)
 * all the input commands and for creation and storage of templates.
 */

public class Log {
    private static final Stack<LinkedList<Command>> previousLogState = new Stack<>();
    public static LinkedList<Command> commandLog = new LinkedList<>();
    private static final String[] unLoggableCommands = {
            "ldtmp", "pt", "pr", "pc", "pd",
            "save", "help", "shtmp", "rmtmp", "clear",
            "save"
    };

    public static void logCommand(String commandName, String[] params) {
        if (!commandName.equals("show-logs"))
            commandLog.add(new Command(commandName, params));
    }

    public static void updateCommandLog(LinkedList<Command> commands) {
        commandLog = commands;
    }

    public static LinkedList<Command> getCommandLog() {
        return commandLog;
    }


    public static void showLoggedCommands() {
        StringBuilder sb = new StringBuilder();
        sb.append("Logged commands: [");
        commandLog.forEach(command -> sb.append("\"").append(command.rawCommandFormat()).append("\","));
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");
        System.out.println(sb);

    }

    public static String getLastCommandName() {
        return commandLog.get(commandLog.size() - 2).getShortName();
    }

    public static boolean isLoggable(String commandName) {
        for (String cmd : unLoggableCommands) {
            if (commandName.equals(cmd)) {
                return false;
            }
        }
        return true;
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
