package myXml.commands.manager;

import myXml.annotations.CommandHandler;
import myXml.util.DocumentStateWrapper;

import java.lang.reflect.Method;

public interface CommandManager {
    static Method processAnnotations(CommandManager impl,String commandName){
        Method[] methods = impl.getClass().getDeclaredMethods();

        for(Method method : methods){
            if(method.isAnnotationPresent(CommandHandler.class)){
                CommandHandler annotation = method.getAnnotation(CommandHandler.class);
                for(String name : annotation.names()){
                    if(name.equals(commandName)){
                        return method;
                    }
                }
            }
        }
        return null;
    };


}
