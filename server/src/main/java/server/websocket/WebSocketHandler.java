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

    public WebSocketHandler (GameService gameService,
                             UserService userService,
                             ConcurrentHashMap<Session, Connect> gameSessions) {
        this.gameService = gameService;
        this.userService = userService;
        this.gameSessions = gameSessions;
    }

    @OnWebSocketMessage
    public void onMessage (Session session, String message) throws IOException {
        System.out.println("Recieved: " + message);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(UserGameCommand.class, new UserGameCommandDeserializer())
                .create();

        UserGameCommand userGameCommand = gson.fromJson(message, UserGameCommand.class);

        System.out.println("User game command: " + userGameCommand);
        switch (userGameCommand.getCommandType()) {
            // make a connection as a player or observer
            case CONNECT -> connect(session, (Connect) userGameCommand);
            // used to request to make a move in a game
            case MAKE_MOVE -> makeMove(session, (MakeMove) userGameCommand);
            // tells the server you are leaving the game so it will stop sending you notifications
            case LEAVE -> leave(session, (Leave) userGameCommand);
            // forfeits the match and ends the game (no more moves can be made)
            case RESIGN -> resign(session, (Resign) userGameCommand);
        }
    }
    private void connect (Session session, Connect command) throws IOException {
        try {
            System.out.println("In connect function...");
            int gameID = command.getGameID();
            String auth = command.getAuthToken();
            GameData game = gameService.fetchGameData(gameID);
            AuthData authData = userService.fetchAuthData(auth);

            String userName = authData.username();
            gameSessions.put(session, command);

            NotificationMessage message;
            switch (getTeamColor(game, userName)) {
                case WHITE -> message = new NotificationMessage("%s has joined as white".formatted(userName));
                case BLACK -> message = new NotificationMessage("%s has joined as black".formatted(userName));
                case null -> message = new NotificationMessage("%s has joined as an observer".formatted(userName));
            };
            broadcast(session, message, true);


            LoadGameMessage loadGameMessage = new LoadGameMessage(game.game());
            broadcast(session, loadGameMessage, true);

        } catch (DataAccessException e) {
            ErrorMessage errorMessage = new ErrorMessage("No game data found");
            broadcastError(session, errorMessage);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage("A problem occurred");
            broadcastError(session, errorMessage);
        }
    }
    private void makeMove (Session session, MakeMove command) throws IOException {
        try {
            System.out.println("In makeMove function...");
            int gameID = command.getGameID();
            String auth = command.getAuthToken();
            System.out.println("Attempting to fetch GameData for GameID: " + gameID);
            GameData game = gameService.fetchGameData(gameID);
            System.out.println("GameData fetched successfully: " + game);

            AuthData authData = userService.fetchAuthData(auth);
            ChessMove move = command.getChessMove();
            System.out.println("ChessMove from command: " + move);
            String userName = authData.username();

            // if participant role is observer, they shouldn't be able to make moves
            ChessGame.TeamColor teamColor = getTeamColor(game, userName);
            if (teamColor == null) {
                ErrorMessage errorMessage = new ErrorMessage("Failed to make move: observer cannot make move");
                broadcastError(session, errorMessage);
            }
            // if it isn't the player's turn
            if (teamColor != game.game().getTeamTurn()) {
                ErrorMessage errorMessage = new ErrorMessage("Failed to make move: move out of turn");
            }
            // check to make sure the move is valid
            if (game.game().validMoves(move.getStartPosition()).contains(move)) {
                game.game().makeMove(move);
                String message = userName + " made move: " + move;
                NotificationMessage notificationMessage = new NotificationMessage(message);
                broadcast(session, notificationMessage, false);
            }
            else {
                ErrorMessage errorMessage = new ErrorMessage("Failed to make move: invalid move");
                broadcastError(session, errorMessage);
            }

        } catch (DataAccessException e) {
            System.err.println("DataAccessException fetching game data: " + e.getMessage());
            ErrorMessage errorMessage = new ErrorMessage("No game data found");
            broadcastError(session, errorMessage);
        } catch (Exception e) {
            System.err.println("Unexpected exception fetching game data:");
            ErrorMessage errorMessage = new ErrorMessage("Internal server error fetching game.");
            broadcastError(session, errorMessage);
        }
    }
    private void leave (Session session, Leave command) throws IOException {
        System.out.println("In leave function...");
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
}
