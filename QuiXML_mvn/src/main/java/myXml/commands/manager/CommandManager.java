package myXml.commands.manager;

import myXml.util.DocumentStateWrapper;

public interface CommandManager {
    void exec(DocumentStateWrapper document,String [] params);
}
