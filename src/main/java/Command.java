import java.util.Arrays;

public enum Command {
    LS("ls") {

    },
    MKDIR("mkdir") {

    },
    CD("cd") {

    },
    CAT("cat") {

    },
    TOUCH("touch"){

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
