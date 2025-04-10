package ui;

import server.ServerFacade;
import server.websocket.NotificationHandler;
import server.websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Arrays;

public class InGameClient {
    private State state = State.INCHESSGAME;
    private final ServerFacade server;
    private String authToken;



    public InGameClient (String serverUrl, String authToken) {
        this.server = new ServerFacade(serverUrl);
        this.authToken = authToken;
    }
    public String eval (String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> makeMove();
                case "resign" -> resign();
                case "highlight" -> highlightMoves();
                default -> help();
            };
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    public String help () {
        return """
                
                help - Displays this help message with available commands.
                redraw Chess Board - Redraws the chess board on your screen.
                leave - Leave the current game (as player or observer).
                move - Input your move in algebraic notation (e.g., e2 e4).
                resign - Forfeit the game after confirmation. You will remain in the game view.
                highlight - Input a piece position (e.g., e2) to see its legal moves.
                """;


    }
    public String redraw () {
        return null;
    }
    public String leave () {
        return null;
    }
    public String makeMove () {
        return null;
    }
    public String resign () {
        return null;
    }
    public String highlightMoves () {
        return null;
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
