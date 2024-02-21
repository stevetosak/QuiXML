package myXml.util;

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
        return name + " " + String.join(" ", params);
    }
}
