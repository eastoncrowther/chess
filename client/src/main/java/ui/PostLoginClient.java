package ui;

import chess.ChessBoard;
import model.GameData;
import requestResultRecords.*;
import server.ServerFacade;

import java.util.Arrays;

public class PostLoginClient {
    private State state = State.LOGGEDIN;
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
                case "join" -> join(Integer.parseInt(params[0]), params[1].toUpperCase());
                case "observe" -> observe(Integer.parseInt(params[0]));
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
            StringBuilder response = new StringBuilder();

            int gameIndex = 1;
            for (GameData game : games.games()) {
                response.append(gameIndex).append(": ")
                        .append(game.gameName()).append(", ")
                        .append(game.gameID()).append(", ")
                        .append(game.whiteUsername()).append(", ")
                        .append(game.blackUsername())
                        .append("\n");
                gameIndex ++;
            }
            return response.toString();
        } catch (Exception e) {
            return "Error: unauthorized\n";
        }
    }
    public String join(int gameID, String teamColor) {
        try {
            server.join(new JoinRequest(teamColor, gameID), authToken);

            return "Game successfully joined\n" + printBoard(teamColor);
        } catch (Exception e) {
            return "Failed to join game. Try again\n";
        }
    }
    public String observe(int gameID) {
        return "observing game: " + gameID + "\n" + printBoard("WHITE");
    }

    private String printBoard (String teamColor) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        PrintBoard printer = new PrintBoard(board);

        String response = "";
        switch (teamColor) {
            case "BLACK" -> response += printer.printBlackBoard();
            case "WHITE" -> response += printer.printWhiteBoard();
        }
        return response;
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
