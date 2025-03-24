package ui;

import requestResultRecords.LoginRequest;
import requestResultRecords.RegisterRequest;
import server.ServerFacade;

import java.util.Arrays;

public class PostLoginClient {
    private State state;
    private final ServerFacade server;

    public PostLoginClient (String serverUrl, State state) {
        this.server = new ServerFacade(serverUrl);
        this.state = state;
    }

    public String eval (String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "quit" -> "quit";
                default -> help();
            };

        } catch (Exception e) {
            return e.getMessage();
        }
    }
    public String help () {
        return "\nlogout to logout\n" +
                "create <NAME> to create a new game of chess\n" +
                "list to list all games currently running on the server\n" +
                "join <ID> [WHITE|BLACK] to join a game of chess\n" +
                "observe <ID> to observe a game of chess\n";
    }


    public State getState () {
        return state;
    }
}
