package myXml.util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

record Command(String name, String info) {

    @Override
    public String toString() {
        return name + " - " + info;
    }
}

class CommandInitializer {
    public static final String path = "C:\\Users\\stefa\\IdeaProjects\\XMLEditor_v1\\src\\myXml\\commands";
    static List<Command> initCommands(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        List<Command> commandList = new ArrayList<>();
        while (line != null) {
            String[] parts = line.split(";");
            commandList.add(new Command(parts[0], parts[1]));
            line = br.readLine();
        }
        return commandList;
    }
}

public class CommandHelper {
    private final List<Command> commandList;
    private final Map<String,Command> commandToInfoMap;
    private boolean wasInit;

    public CommandHelper() throws IOException {
        commandToInfoMap = new HashMap<>();
        commandList = CommandInitializer.initCommands(new FileInputStream(CommandInitializer.path));
        commandList.forEach(command -> commandToInfoMap.put(command.name(),command));
    }

    public void getCommandHelp(String name){
        System.out.println(commandToInfoMap.get(name));
    }

    public void displayAllCommands(){
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandList.size(); i++) {
            sb.append(commandList.get(i).name()).append(", ");
            if(i > 0 && i % 4 == 0) sb.append("\n");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("\n");
        System.out.println(sb);
        System.out.println("To get info about a command type \"help (command name)\"");
    }

}

