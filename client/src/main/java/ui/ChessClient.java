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

import java.io.Console;
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

    private record GameContext(ChessGame.TeamColor playerColor, int gameID, ChessBoard board) {}

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
            return "\nAn error occurred. Please try again.\n";
        }
    }

    private String evalLoggedOut(String command, String[] params) {
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


    private String login(String[] params) {
        if (params.length != 2) {
            return "Usage: login <USERNAME> <PASSWORD>\n";
        }
        String username = params[0];
        String password = params[1];
        try {
            LoginResult result = serverFacade.login(new LoginRequest(username, password));
            this.authToken = result.authToken();
            this.username = result.username();
            this.currentState = State.LOGGEDIN;
            return "\nLogged in successfully as " + this.username + ".\n";
        } catch (Exception e) {
            return "\nLogin failed: Incorrect username/password.\n";
        }
    }

    private String register(String[] params) {
        if (params.length != 3) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>\n";
        }
        String username = params[0];
        String password = params[1];
        String email = params[2];
        try {
            RegisterResult result = serverFacade.register(new RegisterRequest(username, password, email));
            this.authToken = result.authToken();
            this.username = result.username();
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

    private String logout() {
        if (this.authToken == null) {
            return "\nAlready logged out.\n";
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

    private String create(String[] params) {
        if (params.length != 1 || params[0].isEmpty()) {
            return "Usage: create <GAME_NAME>\n";
        }
        String gameName = params[0];
        try {
            CreateResult result = serverFacade.createGame(new CreateRequest(gameName), this.authToken);
            return "\nGame '" + gameName + "' created successfully.\n";
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return "\nError: Game name '" + gameName + "' might already exist or be invalid.\n";
            } else if (e.getMessage() != null && e.getMessage().contains("401")) {
                return "\nError: Not authorized to create game (check login status).\n";
            }
            return "\nFailed to create game: " + e.getMessage() + "\n";
        }
    }

    private String list() {
        try {
            ListResult result = serverFacade.list(this.authToken);
            if (result == null || result.games() == null || result.games().isEmpty()) {
                return "\nNo games available.\n";
            }

            StringBuilder response = new StringBuilder("\nAvailable Games:\n");
            this.gameIndexToID.clear();
            int displayIndex = 1;

            for (GameData game : result.games()) {
                this.gameIndexToID.put(displayIndex, game.gameID());

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

    private String join(String[] params) {
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
        Integer gameID = this.gameIndexToID.get(displayIndex);

        if (gameID == null) {
            return "\nInvalid game index '" + displayIndex + "'. Use 'list' first.\n";
        }

        GameContext gameContext = new GameContext(getColor(colorStr), gameID, null);
        // http join
        try {
            serverFacade.join(new JoinRequest(colorStr, gameID), this.authToken);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                return "\nFailed to join: Color '" + colorStr + "' might be taken or game already started/full.\n";
            } else if (e.getMessage() != null && e.getMessage().contains("401")) {
                return "\nError: Not authorized to join game (check login status).\n";
            } else if (e.getMessage() != null && e.getMessage().contains("400")) {
                return "\nFailed to join: Invalid request (game might not exist).\n";
            }
            return "\nFailed HTTP join request: " + e.getMessage() + "\n";
        }
        // websocket join
        boolean connected = connectWebSocket(gameID);
        if (connected) {
            this.gameContext = gameContext;
            return "\nJoin request sent. Connecting to game...\n";
        } else {
            return "\nJoined game but failed to connect.\n";
        }
    }
    private ChessGame.TeamColor getColor(String colorStr) {
        if (colorStr.equals("WHITE")) {
            return ChessGame.TeamColor.WHITE;
        } else if (colorStr.equals("BLACK")) {
            return ChessGame.TeamColor.BLACK;
        }
        else {
            return null;
        }
    }

    private String observe(String[] params) {
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
            this.gameContext = new GameContext(null, gameID, null);
            return "\nConnecting to observe game " + gameID + "...\n";
        } else {
            return "\nFailed to connect WebSocket to observe game.\n";
        }
    }

    private String leave() {
        if (this.webSocketFacade == null) {
            return "\nNot currently in a game.\n";
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
        // get the position from the command
        ChessPosition startPos = getPosition(params[0]);
        ChessPosition endPos = getPosition(params[1]);
        if (startPos == null || endPos == null) {
            return "\nMake sure move is formatted correctly <a1 a2>.\n";
        }
        // add a function to get the promotion piece
        ChessMove chessMove = new ChessMove(startPos, endPos, null);
        this.webSocketFacade.makeMove(chessMove);
        return "\n";
    }
    private ChessPosition getPosition(String position) {
        if (position.length() != 2) {
            return null;
        }
        char colChar = Character.toUpperCase(position.charAt(0));
        int row = Character.getNumericValue(position.charAt(1));

        if (row < 1 || row > 8 || colChar < 'A' || colChar > 'H') {
            return null;
        }

        int col = colChar - 'A' + 1;

        return new ChessPosition(row, col);
    }

    private String resign() {
        if (gameContext == null || gameContext.playerColor() == null) {
            return "\nObservers cannot resign.\n";
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        Console console = System.console();
        String confirmation = "";
        if (console != null) {
            confirmation = console.readLine().trim().toLowerCase();
        } else {
            try (Scanner scanner = new Scanner(System.in)) {
                confirmation = scanner.nextLine().trim().toLowerCase();
            } catch(Exception e){
                return "\nCould not read confirmation input.\n";
            }
        }


        if (confirmation.equals("yes") || confirmation.equals("y")) {
            try {
                if (this.webSocketFacade != null && this.webSocketFacade.isOpen()) {
                    this.webSocketFacade.resign();
                    return "\n";
                } else {
                    return handleUnexpectedDisconnection(); // Handle case where connection dropped
                }
            } catch (Exception e) {
                return "\nFailed to send resignation command: " + e.getMessage() + "\n";
            }
        } else {
            return "\nResignation cancelled.\n";
        }
    }

    private String highlightMoves(String[] params) {
        return null;
    }
    private String redraw() {
        if (gameContext == null || gameContext.board == null) {
            return "\nGame state not available. Cannot redraw board.\n";
        }

        printBoard.setChessBoard(gameContext.board);

        ChessGame.TeamColor perspective = (gameContext.playerColor() != null) ? gameContext.playerColor() : ChessGame.TeamColor.WHITE;

        if (perspective == ChessGame.TeamColor.WHITE) {
            return printBoard.printWhiteBoard();
        } else {
            return printBoard.printBlackBoard();
        }
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
        this.gameContext = new GameContext(gameContext.playerColor(), gameContext.gameID(), game.getBoard());

        this.printBoard.setChessBoard(game.getBoard());
        System.out.print("\n");
        switch (gameContext.playerColor()) {
            case BLACK -> System.out.print(printBoard.printBlackBoard());
            default -> System.out.print(printBoard.printWhiteBoard());
        }
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
