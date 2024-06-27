package myXml.commands.help;

import myXml.commands.Command;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHelper {
    //private static final Map<String, InfoCommand> commandToInfoMap = new HashMap<>();
    private static final String path = "src/main/resources/command_list.ssv";
    private final static List<Command> commandList;

    static {
        try {
            commandList = initCommands(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//    public static void init() throws IOException {
//        commandList = initCommands(path);
//
//    }

//    public static List<InfoCommand> getCommandList() {
//        return commandToInfoMap.values().stream().collect(Collectors.toUnmodifiableList());
//    }

    public static void getCommandHelp(String[] name) throws InvalidCommandException {
       Command cmd = containsCommand(name[0]);
       if(cmd != null) System.out.println(cmd.infoCommandFormat());

    }

    public static Command containsCommand(String name){
        for(Command cmd : commandList){
            if(cmd.contains(name)){
                return cmd;
            }
        }
        return null;
    }

    public static void displayAllCommands() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandList.size(); i++) {
            sb.append(commandList.get(i).getShortName()).append(" | ");
            if (i > 0 && i % 4 == 0) sb.append("\n");
        }
        sb.append("\n");
        System.out.println(sb);
        System.out.println("To get info about a command type \"help (command name)\"");
    }


    private static List<Command> initExec(InputStream is) throws IOException {
        List<Command> commands = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            String[] parts = line.split(";");
            if(parts.length < 2){
                System.out.println("Zgrese negde gojdo proveri go fajlot");
            }

            String name = parts[0];
            String [] aliases = Arrays.copyOfRange(parts,1,parts.length -1);
            String info = parts[parts.length-1];

            if (parts.length >= 2) commands.add(new Command(name,aliases,info));
            line = br.readLine();
        }
        return commands.stream().sorted(Comparator.comparing(Command::getShortName)).collect(Collectors.toList());
    }

    private static List<Command> initCommands(String path) throws IOException {
        try (InputStream is = new FileInputStream(path)) {
            return initExec(is);
        }
    }

}

