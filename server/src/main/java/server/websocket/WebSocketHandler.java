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
        gameSessions.put(session, command);

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
        broadcast(session, notification, false);

        var loadGameMsg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(loadGameMsg));

        System.out.println("Session " + session.hashCode() + " connected successfully.");
    }
    private void makeMove (Session session, MakeMove command) throws IOException {

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

        ChessGame.TeamColor teamColor = getTeamColor(gameData, userName);
        if (teamColor == null) {
            broadcastError(session, new ErrorMessage("Failed to make move: observer cannot make move."));
            return;
        }

        if (game.isGameEnded()) {
            broadcastError(session, new ErrorMessage("Failed to make move: the game has already ended."));
        }

        if (teamColor != game.getTeamTurn()) {
            broadcastError(session, new ErrorMessage("Failed to make move: move out of turn."));
            return;
        }

        try {
            game.makeMove(move);
            System.out.printf("%s (%s) made move: %s%n", userName, teamColor, move);

            gameService.updateGame(gameData);

            endGameCheck(session, gameData, teamColor, userName, move);

            LoadGameMessage loadGameMessage = new LoadGameMessage(game);
            broadcast(session, loadGameMessage, true);

        } catch (InvalidMoveException e) {
            broadcastError(session, new ErrorMessage("Failed to make move: invalid move"));
        }
    }

    private void endGameCheck (Session session, GameData gameData,
                               ChessGame.TeamColor lastPlayerColor,
                               String userName, ChessMove move) throws IOException {

        ChessGame.TeamColor opponentColor = switch (lastPlayerColor) {
                case BLACK -> ChessGame.TeamColor.WHITE;
                case WHITE -> ChessGame.TeamColor.BLACK;
        };
        String message;
        if (gameData.game().isInCheckmate(opponentColor)) {
            message = String.format("Checkmate! %s wins!", userName);
            gameData.game().setGameEnded(true);
        } else if (gameData.game().isInStalemate(opponentColor)) {
            message = "Stalemate!";
            gameData.game().setGameEnded(true);
        } else if (gameData.game().isInCheck(opponentColor)) {
            message = opponentColor + " is in check.";
        } else {
            message = String.format("%s made move: %s", userName, move);
        }
        broadcast(session, new NotificationMessage(message), true);
        gameService.updateGame(gameData);
    }


    private void leave (Session session, Leave command) throws IOException {
        System.out.println("Processing LEAVE for session " + session.hashCode() + " game " + command.getGameID());

        ValidationContext context = fetchAndValidateAuthAndGame(session, command);
        if (context == null) {
            return;
        }
        String userName = context.authData.username();
        broadcast(session, new NotificationMessage(userName + " left the game."), false);
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
        broadcast(session, new NotificationMessage(opponentName + " wins! " + userName + " resigned."), true);
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
