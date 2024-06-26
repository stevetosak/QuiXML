package myXml.commands;

/**
 * This interface provides a common link to the two types of commands: {@link RawCommand} and {@link InfoCommand}.
 */
public interface CommandFormat {
    String getName();

    String commandFormat();
}
