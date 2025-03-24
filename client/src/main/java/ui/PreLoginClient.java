package ui;

import requestResultRecords.LoginRequest;
import server.ServerFacade;

import java.util.Arrays;

public class PreLoginClient {
    private State state;
    private final ServerFacade server;

    public PreLoginClient (String serverUrl, State state) {
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


    public String login(String username, String password) {
        try {
            server.login(new LoginRequest(username, password));
            return username + " successfully logged in\n";
        } catch (Exception e) {
            return "Wrong username or password. Please try again\n";
        }
    }

    public String register(String[] registerInfo) throws Exception {
        return "";
    }
    // this class will update the state
    public State getState () {
        return state;
    }
}
