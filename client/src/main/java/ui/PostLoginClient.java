package ui;

import model.GameData;
import requestResultRecords.CreateRequest;
import requestResultRecords.ListResult;
import requestResultRecords.LoginRequest;
import requestResultRecords.RegisterRequest;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Collection;

public class PostLoginClient {
    private State state;
    private final ServerFacade server;
    private String authToken;

    public PostLoginClient (String serverUrl, String authToken) {
        this.server = new ServerFacade(serverUrl);
        this.authToken = authToken;
    }

    public String eval (String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "quit" -> "quit";
                case "create" -> create(params[0]);
                case "list" -> list();
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
                "observe <ID> to observe a game of chess\n" +
                "quit - exit the program\n" +
                "help - with possible commands\n";
    }
    public String create (String gameName) {
        try {
            server.createGame(new CreateRequest(gameName), this.authToken);
            return gameName + " successfully created\n";
        } catch (Exception e) {
            return gameName + " already exists\n";
        }
    }
    public String list () {
        try {
            ListResult games = server.list(this.authToken);
            String response = "";

            int gameIndex = 1;
            for (GameData game : games.games()) {
                response += gameIndex + ": " + game.gameName() + ", " + game.gameID() + ", " + game.whiteUsername() + ", " + game.blackUsername() + "\n";
            }
            return response;
        } catch (Exception e) {
            return "Error: unauthorized\n";
        }
    }

    public State getState () {
        return state;
    }
    public void setState (State state) {
        this.state = state;
    }


    public void setAuth (String authToken) {
        this.authToken = authToken;
    }
}
