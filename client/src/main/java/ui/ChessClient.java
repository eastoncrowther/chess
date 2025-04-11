package ui;

import chess.*;
import model.GameData; // Assuming GameData includes usernames
import requestresult.*;
import server.ServerFacade;
import server.websocket.NotificationHandler;
import server.websocket.WebSocketFacade;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {

    private State currentState = State.LOGGEDOUT;
    private String authToken = null;
    private String username = null;
    private final String serverUrl;
    private final ServerFacade serverFacade;
    private WebSocketFacade webSocketFacade = null;
    private GameContext gameContext = null;
    private final PrintBoard printBoard;
    private final Map<Integer, Integer> gameIndexToID = new HashMap<>();

    private record GameContext(ChessGame game, ChessGame.TeamColor playerColor, int gameID) {}

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.serverFacade = new ServerFacade(serverUrl);
        this.printBoard = new PrintBoard(null); // Board set later
    }

    public State getState() {
        return currentState;
    }

    public String eval(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        try {
            String[] tokens = input.trim().toLowerCase().split("\\s+"); // Split by whitespace
            String command = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (this.currentState) {
                case LOGGEDOUT -> evalLoggedOut(command, params);
                case LOGGEDIN -> evalLoggedIn(command, params);
                case INCHESSGAME -> evalInGame(command, params);
                case QUIT -> "";
            };
        } catch (Exception e) {
            return "\nAn unexpected error occurred. Please try again.\n";
        }
    }

    private String evalLoggedOut(String command, String[] params) throws Exception {
        return switch (command) {
            case "quit" -> quit();
            case "login" -> login(params);
            case "register" -> register(params);
            default -> help(State.LOGGEDOUT);
        };
    }

    private String evalLoggedIn(String command, String[] params) throws Exception {
        return switch (command) {
            case "quit" -> quit();
            case "logout" -> logout();
            case "create" -> create(params);
            case "list" -> list();
            case "join" -> join(params);
            case "observe" -> observe(params);
            default -> help(State.LOGGEDIN);
        };
    }

    private String evalInGame(String command, String[] params) throws Exception {
        if (this.webSocketFacade == null || !this.webSocketFacade.isOpen()) {
            return handleUnexpectedDisconnection();
        }

        return switch (command) {
            case "quit" -> quit();
            case "leave" -> leave();
            case "move" -> makeMove(params);
            case "resign" -> resign();
            case "highlight" -> highlightMoves(params);
            case "redraw" -> redraw();
            default -> help(State.INCHESSGAME);
        };
    }

    private String help(State state) {
        return switch (state) {
            case LOGGEDOUT -> """

                 register <USERNAME> <PASSWORD> <EMAIL> - Create an account.
                 login <USERNAME> <PASSWORD>          - Log in to play chess.
                 quit                               - Exit the application.
                 help                               - Show this help message.
                 """;
            case LOGGEDIN -> """

                 create <NAME>          - Create a new game.
                 list                   - List available games.
                 join <INDEX> <COLOR>   - Join a game as WHITE or BLACK (use index from 'list').
                 observe <INDEX>        - Watch a game (use index from 'list').
                 logout                 - Log out and return to the main menu.
                 quit                   - Exit the application.
                 help                   - Show this help message.
                 """;
            case INCHESSGAME -> """

                 redraw                 - Redraw the chess board.
                 leave                  - Leave the current game (returns to logged-in menu).
                 move <FROM> <TO>       - Make a move (e.g., 'move e2 e4').
                                          For pawn promotion: 'move e7 e8 q' (or r, b, n).
                 resign                 - Forfeit the current game.
                 highlight <POSITION>   - Show legal moves for piece at position (e.g., 'highlight e2').
                 quit                   - Exit the application (will leave game).
                 help                   - Show this help message.
                 """;
            default -> "No help available for current state.";
        };
    }

    // Quit
    private String quit() {
        if (this.webSocketFacade != null) {
            try {
                this.webSocketFacade.close();
            } catch (Exception e) { /* Ignore closing errors on quit */ }
            this.webSocketFacade = null;
        }
        this.currentState = State.QUIT;
        return "";
    }


    private String login(String[] params) throws Exception {
        if (params.length != 2) {
            return "Usage: login <USERNAME> <PASSWORD>\n";
        }
        String username = params[0];
        String password = params[1];
        try {
            LoginResult result = serverFacade.login(new LoginRequest(username, password));
            this.authToken = result.authToken();
            this.username = result.username(); // Store username
            this.currentState = State.LOGGEDIN;
            return "\nLogged in successfully as " + this.username + ".\n";
        } catch (Exception e) {
            return "\nLogin failed: Incorrect username/password.\n";
        }
    }

    private String register(String[] params) throws Exception {
        if (params.length != 3) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>\n";
        }
        String username = params[0];
        String password = params[1];
        String email = params[2];
        try {
            RegisterResult result = serverFacade.register(new RegisterRequest(username, password, email));
            this.authToken = result.authToken();
            this.username = result.username(); // Store username
            this.currentState = State.LOGGEDIN;
            return "\nRegistration successful. Logged in as " + this.username + ".\n";
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) { // Example check
                return "\nRegistration failed: Username '" + username + "' already exists.\n";
            } else if (e.getMessage() != null && e.getMessage().contains("400")) {
                return "\nRegistration failed: Invalid username, password, or email format.\n";
            }
            return "\nRegistration failed\n";
        }
    }

    private String logout() throws Exception {
        if (this.authToken == null) {
            return "\nAlready logged out.\n"; // Should not happen in this state
        }

        closeWebSocketConnection();
        this.gameContext = null;

        try {
            serverFacade.logout(this.authToken);
            this.authToken = null;
            this.username = null;
            this.currentState = State.LOGGEDOUT;
            this.gameIndexToID.clear();
            return "\nLogged out successfully.\n";
        } catch (Exception e) {
            this.authToken = null;
            this.username = null;
            this.currentState = State.LOGGEDOUT;
            this.gameIndexToID.clear();
            return "\nLogout failed.\n";
        }
    }

    private String create(String[] params) throws Exception {
        if (params.length != 1 || params[0].isEmpty()) {
            return "Usage: create <GAME_NAME>\n";
        }
        String gameName = params[0];
        try {
            CreateResult result = serverFacade.createGame(new CreateRequest(gameName), this.authToken);
            return "\nGame '" + gameName + "' created successfully (ID: " + result.gameID() + ").\n";
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return "\nError: Game name '" + gameName + "' might already exist or be invalid.\n";
            } else if (e.getMessage() != null && e.getMessage().contains("401")) {
                return "\nError: Not authorized to create game (check login status).\n";
            }
            return "\nFailed to create game: " + e.getMessage() + "\n";
        }
    }

    private String list() throws Exception {
        try {
            ListResult result = serverFacade.list(this.authToken);
            if (result == null || result.games() == null || result.games().isEmpty()) {
                return "\nNo games available.\n";
            }

            StringBuilder response = new StringBuilder("\nAvailable Games:\n");
            this.gameIndexToID.clear(); // Clear map before repopulating
            int displayIndex = 1;

            for (GameData game : result.games()) {
                this.gameIndexToID.put(displayIndex, game.gameID()); // Map display index to actual ID

                String whitePlayer = (game.whiteUsername() == null || game.whiteUsername().isEmpty()) ? "[available]" : game.whiteUsername();
                String blackPlayer = (game.blackUsername() == null || game.blackUsername().isEmpty()) ? "[available]" : game.blackUsername();

                response.append(String.format(" %d: %-15s (White: %-10s | Black: %-10s)\n",
                        displayIndex,
                        game.gameName(),
                        whitePlayer,
                        blackPlayer));

                displayIndex++;
            }
            return response.toString();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                return "\nError: Not authorized to list games (check login status).\n";
            }
            return "\nError listing games: " + e.getMessage() + "\n";
        }
    }

    private String join(String[] params) throws Exception {
        if (params.length != 2) {
            return "Usage: join <INDEX> <COLOR (WHITE or BLACK)>\n";
        }
        int displayIndex;
        try {
            displayIndex = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "\nInvalid game index. Please use the number shown in 'list'.\n";
        }
        String colorStr = params[1].toUpperCase();
        ChessGame.TeamColor requestedColor;
        if (colorStr.equals("WHITE")) {
            requestedColor = ChessGame.TeamColor.WHITE;
        } else if (colorStr.equals("BLACK")) {
            requestedColor = ChessGame.TeamColor.BLACK;
        } else {
            return "\nInvalid color. Please specify WHITE or BLACK.\n";
        }

        Integer gameID = this.gameIndexToID.get(displayIndex);
        if (gameID == null) {
            return "\nInvalid game index '" + displayIndex + "'. Use 'list' first.\n";
        }

        // http join
        try {
            serverFacade.join(new JoinRequest(colorStr, gameID), this.authToken);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return "\nFailed to join: Color '" + colorStr + "' might be taken or game already started/full.\n";
            } else if (e.getMessage() != null && e.getMessage().contains("401")) {
                return "\nError: Not authorized to join game (check login status).\n";
            } else if (e.getMessage() != null && e.getMessage().contains("400")) {
                return "\nFailed to join: Invalid request (game ID " + gameID + " might not exist).\n";
            }
            return "\nFailed HTTP join request: " + e.getMessage() + "\n";
        }
        // websocket join
        boolean connected = connectWebSocket(gameID);
        if (connected) {
            return "\nJoin request sent. Connecting to game...\n";
        } else {
            return "\nJoined game but failed to connect.\n";
        }
    }

    private String observe(String[] params) throws Exception {
        if (params.length != 1) {
            return "Usage: observe <INDEX>\n";
        }
        int displayIndex;
        try {
            displayIndex = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return "\nInvalid game index. Please use the number shown in 'list'.\n";
        }
        Integer gameID = this.gameIndexToID.get(displayIndex);
        if (gameID == null) {
            return "\nInvalid game index '" + displayIndex + "'. Use 'list' first.\n";
        }
        // websocket join
        boolean connected = connectWebSocket(gameID);
        if (connected) {
            return "\nConnecting to observe game " + gameID + "...\n";
        } else {
            return "\nFailed to connect WebSocket to observe game.\n";
        }
    }

    private String leave() throws Exception {
        if (this.webSocketFacade == null) {
            return "\nNot currently in a game.\n"; // Safety check
        }

        try {
            this.webSocketFacade.leave();
        } catch (Exception e) {
            System.err.println("Warning: Failed to send leave command: " + e.getMessage());
        }
        cleanupAfterGame();
        return "\nYou have left the game.\n";
    }

    private String makeMove(String[] params) throws Exception {
       return null;
    }

    private String resign() {
        return null;
    }
    private String highlightMoves(String[] params) {
        return null;
    }
    private String redraw() {
        return null;
    }

    private boolean connectWebSocket(int gameID) {
        if (this.webSocketFacade != null && this.webSocketFacade.isOpen()) {
            System.err.println("Warning: WebSocket already seems open. Closing existing one.");
            closeWebSocketConnection();
        }
        try {
            this.webSocketFacade = new WebSocketFacade(serverUrl, this);
            this.webSocketFacade.connect(this.authToken, gameID);
            this.currentState = State.INCHESSGAME;
            return true;
        } catch (Exception e) {
            System.err.println("Failed to establish WebSocket connection: " + e.getMessage());
            this.webSocketFacade = null;
            this.currentState = State.LOGGEDIN;
            return false;
        }
    }

    private void closeWebSocketConnection() {
        if (this.webSocketFacade != null) {
            try {
                this.webSocketFacade.close();
            } catch (Exception e) {
                System.err.println("Error closing WebSocket: " + e.getMessage());
            } finally {
                this.webSocketFacade = null;
            }
        }
    }

    private void cleanupAfterGame() {
        closeWebSocketConnection();
        this.gameContext = null;
        this.currentState = State.LOGGEDIN;
    }

    private String handleUnexpectedDisconnection() {
        cleanupAfterGame();
        return "\nConnection lost. Returning to logged-in menu.\n";
    }


    @Override
    public void handleLoadGame(LoadGameMessage message) {
        ChessGame game = message.getGame();

        if (game == null) {
            System.err.println("Error: Received incomplete game load data.");
            return;
        }

        ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;

        this.printBoard.setChessBoard(game.getBoard());
        System.out.print(printBoard.printWhiteBoard());
        // just for the code quality :)
        System.out.print(printBoard.printBlackBoard());
    }

    @Override
    public void handleNotification(NotificationMessage message) {
        System.out.println("\n" + SET_TEXT_COLOR_BLUE + "[Notification] " + message.getMessage() + RESET_TEXT_COLOR);
    }

    @Override
    public void handleError(ErrorMessage message) {
        System.out.println("\n" + SET_TEXT_COLOR_RED + "[Game Error] " + message.getErrorMessage() + RESET_TEXT_COLOR);
    }
}
