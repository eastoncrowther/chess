import chess.*;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        // default location if another is not specified in the command line
        var serverUrl = "http://localhost:3030";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        new Repl(serverUrl);
    }
}