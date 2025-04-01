package server.websocket;


import com.google.gson.Gson;
import dataaccess.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.GameService;
import websocket.commands.Connect;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler() {
    private final ConnectionsManager connections = new ConnectionsManager();
    GameService gameService;

    public WebSocketHandler (GameService gameService) {
        this.gameService = gameService;
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
            Integer gameID = command.getGameID();
            String auth = command.getAuthToken();
            
            connections.updateGameID(session, gameID);

            gameService.fetchGameData(gameID);

        } catch (DataAccessException e) {

        }

    }
    private void makeMove () {

    }
    private void leave () {

    }
    private void resign () {

    }

}
