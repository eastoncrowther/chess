package ui;

import chess.ChessBoard;
import model.GameData;
import requestresult.*;
import server.ServerFacade;
import server.websocket.NotificationHandler;
import server.websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PostLoginClient {
    private Map<Integer, Integer> gameIndexToID = new HashMap<>();


    private State state = State.LOGGEDIN;
    private final ServerFacade server;
    private final WebSocketFacade ws;
    private String authToken;

    public PostLoginClient (String serverUrl, String authToken, NotificationHandler notificationHandler) throws Exception {
        this.server = new ServerFacade(serverUrl);
        this.ws = new WebSocketFacade(serverUrl, notificationHandler);
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
                case "join" -> processGameAction(params, true);
                case "observe" -> processGameAction(params, false);
                case "logout" -> logout();
                default -> help();
            };

        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private String processGameAction(String[] params, boolean isJoin) {
        if (params.length < 1) {
            return "Please provide a game index\n";
        }

        try {
            int gameIndex = Integer.parseInt(params[0]);
            if (isJoin) {
                if (params.length < 2) {
                    return "Please provide both the game index and a team color (WHITE or BLACK)\n";
                }
                return join(gameIndex, params[1].toUpperCase());
            } else {
                return observe(gameIndex);
            }
        } catch (NumberFormatException e) {
            return "Invalid game index. Please enter a valid number\n";
        }
    }

    public String help () {
        return """
                
                logout to logout
                create <NAME> to create a new game of chess
                list to list all games currently running on the server
                join <ID> [WHITE|BLACK] to join a game of chess
                observe <ID> to observe a game of chess
                quit - exit the program
                help - with possible commands
                """;
    }
    public String create (String gameName) {
        try {
            server.createGame(new CreateRequest(gameName), this.authToken);
            return gameName + " successfully created\n";
        } catch (Exception e) {
            return gameName + " already exists\n";
        }
    }
    public String list() {
        try {
            ListResult games = server.list(this.authToken);
            StringBuilder response = new StringBuilder();
            gameIndexToID.clear();
            int gameIndex = 1;

            for (GameData game : games.games()) {
                gameIndexToID.put(gameIndex, game.gameID());

                String whitePlayer = (game.whiteUsername() == null ||
                        game.whiteUsername().isEmpty()) ? "available" : game.whiteUsername();
                String blackPlayer = (game.blackUsername() == null ||
                        game.blackUsername().isEmpty()) ? "available" : game.blackUsername();

                response.append(gameIndex).append(": ")
                        .append(game.gameName()).append(", ")
                        .append(whitePlayer).append("->WHITE, ")
                        .append(blackPlayer).append("->BLACK\n");

                gameIndex++;
            }
            return response.toString();
        } catch (Exception e) {
            return "Error: unauthorized\n";
        }
    }

    public String join(int gameIndex, String teamColor) {
        if (!gameIndexToID.containsKey(gameIndex)) {
            return "Invalid game index. Please check game list\n";
        }
        setState(State.INCHESSGAME);
        int gameID = gameIndexToID.get(gameIndex);
        return joinByID(gameID, teamColor);
    }
    public String joinByID(int gameID, String teamColor) {
        try {
            server.join(new JoinRequest(teamColor, gameID), authToken);
            ws.connect(authToken, gameID);
            return "Game successfully joined\n";
        } catch (Exception e) {
            return "Failed to join game. Try again\n";
        }
    }
    public String observe(int gameID) {
        setState(State.INCHESSGAME);
        try {
            ws.connect(authToken, gameID);
            return "Game successfully joined\n";
        } catch (IOException e) {
            return "Failed to join game. Try again\n";
        }
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
    public String logout () {
        try {
            server.logout(this.authToken);
            this.state = State.LOGGEDOUT;
            return "Logged out\n";
        } catch (Exception e) {
            return "Error logging out\n";
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
    public String getAuth () {
        return this.authToken;
    }
}
