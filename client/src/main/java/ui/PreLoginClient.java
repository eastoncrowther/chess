package ui;

import requestresult.LoginRequest;
import requestresult.LoginResult;
import requestresult.RegisterRequest;
import requestresult.RegisterResult;
import server.ServerFacade;

import java.util.Arrays;

public class PreLoginClient {
    private State state;
    private final ServerFacade server;
    private String authToken;

    public PreLoginClient (String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.state = State.LOGGEDOUT;
        authToken = null;
    }

    public String eval (String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "quit" -> "quit";
                case "login" -> {
                    if (params.length < 2) {
                        yield "Please enter both username and password.\n";
                    }
                    yield login(params[0], params[1]);
                }
                case "register" -> register(params);
                default -> help();
            };

        } catch (Exception e) {
            return e.getMessage();
        }
    }
    public String help () {
        return """
                
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;
    }


    public String login(String username, String password) {
        try {
            LoginResult result = server.login(new LoginRequest(username, password));
            this.state = State.LOGGEDIN;
            this.authToken = result.authToken();
            return username + " successfully logged in\n";
        } catch (Exception e) {
            this.state = State.LOGGEDOUT;
            return "Wrong username or password. Please try again\n";
        }
    }

    public String register(String[] registerInfo) {
        if (registerInfo.length < 3) {
            return "Please enter username, password, and email\n";
        }
        try {
            RegisterResult result = server.register(new RegisterRequest(registerInfo[0], registerInfo[1], registerInfo[2]));
            this.state = State.LOGGEDIN;
            this.authToken = result.authToken();
            return registerInfo[0] + " successfully registered. Logged in\n";
        } catch (Exception e) {
            this.state = State.LOGGEDOUT;
            if (e.getMessage().contains("403")) {
                return "User already exists. Please try again\n";
            } else if (e.getMessage().contains("400")) {
                return "Invalid registration details. Please check your input.\n";
            } else {
                return "An error occurred\n";
            }
        }
    }
    public State getState () {
        return state;
    }
    public void setState (State state) {
        this.state = state;
    }
    public String getAuth () {
        return authToken;
    }
}
