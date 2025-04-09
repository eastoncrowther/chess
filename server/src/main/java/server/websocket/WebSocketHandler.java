package server.websocket;


import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {
    GameService gameService;
    UserService userService;
    ConcurrentHashMap<Session, Connect> gameSessions;
    private final Gson gson;
    private record ValidationContext(AuthData authData, GameData gameData) {}

    public WebSocketHandler (GameService gameService,
                             UserService userService,
                             ConcurrentHashMap<Session, Connect> gameSessions) {
        this.gameService = gameService;
        this.userService = userService;
        this.gameSessions = gameSessions;

        gson = new GsonBuilder()
                .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
                .create();

    }

    @OnWebSocketMessage
    public void onMessage (Session session, String message) throws IOException {
        System.out.println("Received: " + message);
        UserGameCommand userGameCommand = gson.fromJson(message, UserGameCommand.class);
        
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(session, (Connect) userGameCommand);
            case MAKE_MOVE -> makeMove(session, (MakeMove) userGameCommand);
            case LEAVE -> leave(session, (Leave) userGameCommand);
            case RESIGN -> resign(session, (Resign) userGameCommand);
            default -> broadcastError(session, new ErrorMessage("Error: Unknown command type."));
        }
    }
    private void connect (Session session, Connect command) throws IOException {
        System.out.println("Connecting session " + session.hashCode() + " for game " + command.getGameID());
        AuthData authData;
        GameData gameData;

        try {
            authData = userService.fetchAuthData(command.getAuthToken());
            if (authData == null) {
                throw new DataAccessException("Authentication token is invalid or expired.");
            }
            gameData = gameService.fetchGameData(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Game not found with ID: " + command.getGameID());
            }
        } catch (DataAccessException e) {
            broadcastError(session, new ErrorMessage("Error: " + e.getMessage()));
            return;
        }

        String userName = authData.username();
        gameSessions.put(session, command); // Store session info

        // Determine player role
        ChessGame.TeamColor playerColor = getTeamColor(gameData, userName);
        String roleDescription;
        if (playerColor == ChessGame.TeamColor.WHITE) {
            roleDescription = "as White";
        } else if (playerColor == ChessGame.TeamColor.BLACK) {
            roleDescription = "as Black";
        } else {
            roleDescription = "as an observer";
        }

        // Notify others
        var notification = new NotificationMessage(String.format("%s joined the game %s.", userName, roleDescription));
        broadcast(session, notification, false);

        // Send current game state to the connecting client
        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(loadGameMsg));

        System.out.println("Session " + session.hashCode() + " connected successfully.");
    }
    private void makeMove (Session session, MakeMove command) throws IOException {
        try {
            System.out.println("In makeMove function...");

            ValidationContext context = fetchAndValidateAuthAndGame(session, command);
            if (context == null) {
                return;
            }
            AuthData authData = context.authData();
            GameData gameData = context.gameData();
            ChessGame game = gameData.game();
            String userName = authData.username();
            ChessMove move = command.getChessMove();

            // if participant role is observer, they shouldn't be able to make moves
            ChessGame.TeamColor teamColor = getTeamColor(gameData, userName);
            if (teamColor == null) {
                ErrorMessage errorMessage = new ErrorMessage("Failed to make move: observer cannot make move");
                broadcastError(session, errorMessage);
            }
            // if it isn't the player's turn
            if (teamColor != gameData.game().getTeamTurn()) {
                ErrorMessage errorMessage = new ErrorMessage("Failed to make move: move out of turn");
                broadcastError(session, errorMessage);
            }
            // check to make sure the move is valid
            if (gameData.game().validMoves(move.getStartPosition()).contains(move)) {
                gameData.game().makeMove(move);
                String message = userName + " made move: " + move;
                NotificationMessage notificationMessage = new NotificationMessage(message);
                broadcast(session, notificationMessage, false);
            }
            else {
                ErrorMessage errorMessage = new ErrorMessage("Failed to make move: invalid move");
                broadcastError(session, errorMessage);
            }

        } catch (Exception e) {
            System.err.println("Unexpected exception fetching game data:");
            ErrorMessage errorMessage = new ErrorMessage("Internal server error fetching game.");
            broadcastError(session, errorMessage);
        }
    }
    private void leave (Session session, Leave command) throws IOException {
        System.out.println("Processing LEAVE for session " + session.hashCode() + " game " + command.getGameID());

        ValidationContext context = fetchAndValidateAuthAndGame(session, command);
        if (context == null) {
            return;
        }
        AuthData authData = context.authData();
        GameData gameData = context.gameData();

        String userName = authData.username();





    }
    private void resign (Session session, Resign command) throws IOException {
        System.out.println("In resign function...");
    }
    private ChessGame.TeamColor getTeamColor (GameData gameData, String userName) {
        if (Objects.equals(gameData.blackUsername(), userName)) {
            return ChessGame.TeamColor.BLACK;
        } else if (Objects.equals(gameData.whiteUsername(), userName)) {
            return ChessGame.TeamColor.WHITE;
        }
        return null;
    }

    public void broadcast(Session sender, ServerMessage message, boolean includeSender) throws IOException {
        System.out.println("In broadcast...");

        var removeList = new ArrayList<Session>();
        Connect senderConnect = gameSessions.get(sender);

        for (Session session : gameSessions.keySet()) {
            Connect connect = gameSessions.get(session);

            boolean sameGame = senderConnect != null
                    && connect != null
                    && Objects.equals(senderConnect.getGameID(), connect.getGameID());
            boolean isSender = session == sender;

            if (session.isOpen()) {
                if ((includeSender || !isSender) && sameGame) {
                    session.getRemote().sendString(message.toJson());
                    System.out.println("Sent message to session: " + session);
                }
            } else {
                removeList.add(session);
            }
        }

        // Remove disconnected sessions
        for (Session session : removeList) {
            gameSessions.remove(session);
            System.out.println("Removed closed session: " + session);
        }
    }

    public void broadcastError(Session sender, ErrorMessage errorMessage) throws IOException {
        sender.getRemote().sendString(errorMessage.toJson());
    }


    private ValidationContext fetchAndValidateAuthAndGame(Session session, UserGameCommand command) throws IOException {
        AuthData authData = null;
        GameData gameData = null;
        String errorMessage = null;

        try {

            authData = userService.fetchAuthData(command.getAuthToken());
            if (authData == null) {
                errorMessage = "Authentication token is invalid or missing.";
            } else {
                gameData = gameService.fetchGameData(command.getGameID());
                if (gameData == null) {
                    errorMessage = "Game not found with ID: " + command.getGameID();
                }
            }
        } catch (DataAccessException e) {
            System.err.println("Data access error during validation: " + e.getMessage());
            errorMessage = "Database error fetching game/auth data: " + e.getMessage();
        } catch (Exception e) { // Catch unexpected errors during fetch
            System.err.println("Unexpected error during validation: " + e.getMessage());
            errorMessage = "An unexpected server error occurred during data retrieval.";
        }

        if (errorMessage != null) {
            broadcastError(session, new ErrorMessage("Error: " + errorMessage));
            return null;
        }

        return new ValidationContext(authData, gameData);
    }




}
