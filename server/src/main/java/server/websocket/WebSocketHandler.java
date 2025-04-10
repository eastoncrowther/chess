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
    ConcurrentHashMap<Session, Integer> gameSessions;
    private final Gson gson;
    private record ValidationContext(AuthData authData, GameData gameData) {}

    public WebSocketHandler (GameService gameService,
                             UserService userService,
                             ConcurrentHashMap<Session, Integer> gameSessions) {
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
        gameSessions.put(session, command.getGameID());

        ChessGame.TeamColor playerColor = getTeamColor(gameData, userName);
        String roleDescription;
        if (playerColor == ChessGame.TeamColor.WHITE) {
            roleDescription = "as White";
        } else if (playerColor == ChessGame.TeamColor.BLACK) {
            roleDescription = "as Black";
        } else {
            roleDescription = "as an observer";
        }

        var notification = new NotificationMessage(String.format("%s joined the game %s.", userName, roleDescription));
        broadcast(gameData.gameID(), session, notification, false);

        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(loadGameMsg));

        System.out.println("Session " + session.hashCode() + " connected successfully.");
    }
    private void makeMove (Session session, MakeMove command) throws IOException {
        System.out.println("Processing MAKE_MOVE for session " + session.hashCode() + " game " + command.getGameID());
        int gameID = command.getGameID();

        ValidationContext context = fetchAndValidateAuthAndGame(session, command);
        if (context == null) {
            return;
        }
        AuthData authData = context.authData();
        GameData initialGameData = context.gameData();
        ChessGame game = initialGameData.game();
        String userName = authData.username();
        ChessMove move = command.getChessMove();

        ChessGame.TeamColor playerColor = getTeamColor(initialGameData, userName);
        if (game.isGameEnded()) {
            broadcastError(session, new ErrorMessage("Failed to make move: the game has already ended."));
            return;
        }
        if (playerColor == null) {
            broadcastError(session, new ErrorMessage("Failed to make move: observer cannot make move."));
            return;
        }
        if (game.getTeamTurn() != playerColor) {
            broadcastError(session, new ErrorMessage("Failed to make move " + game.getTeamTurn() + " turn."));
            return;
        }

        try {
            game.makeMove(move);
            System.out.printf("%s (%s) made move (in memory): %s%n", userName, playerColor, move);

            String notificationText = null;


            ChessGame.TeamColor opponentColor = getOpponentColor(playerColor);

            if (game.isInCheckmate(opponentColor)) {
                notificationText = String.format("Checkmate! %s (%s) wins!", userName, playerColor);
                if (!game.isGameEnded()) {
                    game.setGameEnded(true);
                }
            } else if (game.isInStalemate(opponentColor)) {
                notificationText = "Stalemate! The game is a draw.";
                if (!game.isGameEnded()) {
                    game.setGameEnded(true);
                }
            }
            else if (game.isInCheck(opponentColor)) {
                notificationText = String.format("%s (%s) put %s in check.", userName, playerColor, opponentColor);
            }

            gameService.updateGame(initialGameData);

            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            broadcast(gameID, session, loadGameMessage, true);

            if (notificationText != null) {
                NotificationMessage notification = new NotificationMessage(notificationText);
                broadcast(gameID, session, notification, true);
            } else {
                String moveNotificationText = String.format("%s (%s) made move: %s", userName, playerColor, move);
                NotificationMessage moveNotification = new NotificationMessage(moveNotificationText);
                broadcast(gameID, session, moveNotification, false);
            }

        } catch (InvalidMoveException e) {
            System.err.println("Invalid move attempted by " + userName + ": " + e.getMessage());
            broadcastError(session, new ErrorMessage("Error: Invalid move. " + e.getMessage()));
        }
    }


    private void leave (Session session, Leave command) throws IOException {
        System.out.println("Processing LEAVE for session " + session.hashCode() + " game " + command.getGameID());

        ValidationContext context = fetchAndValidateAuthAndGame(session, command);
        if (context == null) {
            return;
        }
        String userName = context.authData.username();
        broadcast(command.getGameID(), session, new NotificationMessage(userName + " left the game."), false);
    }
    private void resign (Session session, Resign command) throws IOException {
        System.out.println("Processing RESIGN for session " + session.hashCode() + " game " + command.getGameID());

        ValidationContext context = fetchAndValidateAuthAndGame(session, command);
        if (context == null) {
            return;
        }
        AuthData authData = context.authData();
        GameData gameData = context.gameData();
        ChessGame game = gameData.game();
        String userName = authData.username();
        ChessGame.TeamColor playerColor = getTeamColor(gameData, userName);

        if (playerColor == null) {
            broadcastError(session, new ErrorMessage("Failed to resign: Observers cannot resign."));
            return;
        }

        if (game.isGameEnded()) {
            broadcastError(session, new ErrorMessage("Failed to resign. The game is already over."));
            return;
        }

        String opponentName = switch (playerColor) {
            case WHITE -> gameData.blackUsername();
            case BLACK -> gameData.whiteUsername();
        };

        game.setGameEnded(true);
        gameService.updateGame(gameData);
        broadcast(gameData.gameID(), session,
                new NotificationMessage(opponentName + " wins! " + userName + " resigned."),
                true);
    }
    private ChessGame.TeamColor getTeamColor (GameData gameData, String userName) {
        if (Objects.equals(gameData.blackUsername(), userName)) {
            return ChessGame.TeamColor.BLACK;
        } else if (Objects.equals(gameData.whiteUsername(), userName)) {
            return ChessGame.TeamColor.WHITE;
        }
        return null;
    }
    private ChessGame.TeamColor getOpponentColor (ChessGame.TeamColor teamColor) {
        return switch (teamColor) {
            case BLACK -> ChessGame.TeamColor.WHITE;
            case WHITE -> ChessGame.TeamColor.BLACK;
        };
    }

    public void broadcast(int targetGameID, Session senderSession, ServerMessage message, boolean includeSender) {
        String messageJson = gson.toJson(message);
        var removeList = new ArrayList<Session>();

        System.out.println("Broadcasting [" + message.getServerMessageType() + "] to game " + targetGameID);

        for (ConcurrentHashMap.Entry<Session, Integer> entry : gameSessions.entrySet()) { // Level 1
            Session currentSession = entry.getKey();
            Integer sessionGameID = entry.getValue();

            if (!currentSession.isOpen() || sessionGameID == null) {
                removeList.add(currentSession);
                continue;
            }

            if (sessionGameID != targetGameID) {
                continue;
            }

            boolean isSender = (currentSession.equals(senderSession));
            if (isSender && !includeSender) {
                continue;
            }

            try {

                currentSession.getRemote().sendString(messageJson);
            } catch (IOException | IllegalStateException e) {
                removeList.add(currentSession);
            }
        }

        for (Session sessionToRemove : removeList) {
            gameSessions.remove(sessionToRemove);
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
