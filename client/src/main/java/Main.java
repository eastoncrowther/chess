import chess.*;
import ui.Repl;
import ui.State;

public class Main {
    public static void main(String[] args) {
        // default location if another is not specified in the command line
        var serverUrl = "http://localhost:3030";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        var loop = new Repl(serverUrl, State.LOGGEDOUT);
        loop.run();
    }
}