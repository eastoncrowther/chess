package server.websocket;


import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import service.UserService;
import websocket.commands.Connect;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {
    private final ConnectionsManager connections = new ConnectionsManager();
    GameService gameService;
    UserService userService;

    public WebSocketHandler (GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage (Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            // make a connection as a player or observer
            case CONNECT -> connect(session, (Connect) userGameCommand);
            // used to request to make a move in a game
            case MAKE_MOVE -> makeMove();
            // tells the server you are leaving the game so it will stop sending you notifications
            case LEAVE -> leave();
            // forfeits the match and ends the game (no more moves can be made)
            case RESIGN -> resign();
        }
    }
    private void connect (Session session, Connect command) {
        try {
            int gameID = command.getGameID();
            String auth = command.getAuthToken();

            GameData game = gameService.fetchGameData(gameID);
            AuthData authData = userService.fetchAuthData(auth);

            String userName = authData.username();
            ChessGame.TeamColor teamColor = getTeamColor(game, userName);


            connections.updateGameID(session, gameID);

            LoadGameMessage loadGameMessage = new LoadGameMessage(game.game());
            // send this message.

        } catch (DataAccessException e) {
            ErrorMessage errorMessage = new ErrorMessage("No game data found");
            // send this error message.
        }

    }
    private void makeMove () {

    }
    private void leave () {

    }
    private void resign () {

    }

    private ChessGame.TeamColor getTeamColor (GameData gameData, String userName) {
        if (Objects.equals(gameData.blackUsername(), userName)) {
            return ChessGame.TeamColor.BLACK;
        } else if (Objects.equals(gameData.whiteUsername(), userName)) {
            return ChessGame.TeamColor.WHITE;
        }
        return null;
    }
}
