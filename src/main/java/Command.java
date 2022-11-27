import java.util.Arrays;

public enum Command {
    AUTHOK("/authok"){

    },
    LS("ls") { //список файлов и папок

    },
    MKDIR("mkdir") { // создает папку

    },
    CD("cd") { //переход в другую папку

    },
    CAT("cat") { //открывает файл

    },
    TOUCH("touch"){ //создает файл

    },

    SEND("send"){

    };

    private final String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand(){
        return command;
    }

    public static Command byCommand(String command){
        return Arrays.stream(values())
                .filter(cmd -> cmd.getCommand().equals(command))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Command not found"));

    }
}
