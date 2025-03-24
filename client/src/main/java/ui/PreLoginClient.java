package ui;

import server.ServerFacade;

import java.util.Arrays;

public class PreLoginClient {
    private final String serverUrl;
    private final ServerFacade server;
    private State state;

    public PreLoginClient (String serverUrl, State state) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.state = state;
    }

    public String eval (String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "quit" -> "quit";
                case "login" -> login(params[0], params[1]);
                case "register" -> register(params);
                default -> help();
            };

        } catch (Exception e) {
            return e.getMessage();
        }
    }
    public String help () {
        return "register <USERNAME> <PASSWORD> <EMAIL> - to create an account\n" +
                "login <USERNAME> <PASSWORD> - to play chess\n" +
                "quit - playing chess\n" +
                "help - with possible commands\n";
    }


    public String login(String username, String password) throws Exception {
        return "";
    }

    public String register(String[] registerInfo) throws Exception {
        return "";
    }
    // this class will update the state
    public State getState () {
        return state;
    }
}
