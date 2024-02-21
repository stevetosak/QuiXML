package myXml.util;

public class InfoCommand implements CommandFormat {
    private final String name;
    private final String info;
    private final boolean availableWhenUninitialized;

    public InfoCommand(String name, String info, boolean availableWhenUninitialized) {
        this.name = name;
        this.info = info;
        this.availableWhenUninitialized = availableWhenUninitialized;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String commandFormat() {
        return name + " - " + info;
    }

    public boolean isPermittedWhenNotInit() {
        return availableWhenUninitialized;
    }
}
