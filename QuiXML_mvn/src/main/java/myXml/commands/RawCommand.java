package myXml.commands;

/**
 * This is a Command Format that displays a command in the form that it was inputted.
 * Example: "add node" will be saved as -> "add node", exactly the same as it was typed in.
 */
public class RawCommand implements CommandFormat {
    private final String name;
    private final String[] params;

    public RawCommand(String name, String[] params) {
        this.name = name;
        this.params = params;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String commandFormat() {
        return name + (params.length == 0 ? "" : " " + String.join(" ", params));
    }
}
