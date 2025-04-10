package server.websocket;

import chess.ChessMove;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    private final Gson gson;

    private String authToken;
    private Integer gameID;

    public WebSocketFacade (String url, NotificationHandler notificationHandler) throws Exception {
        gson = new GsonBuilder()
                .registerTypeAdapter(ServerMessage.class, new ServerMessageDeserializer())
                .create();
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {

                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

                    switch (serverMessage.getServerMessageType()) {
                        case LOAD_GAME -> notificationHandler.handleLoadGame((LoadGameMessage) serverMessage);
                        case NOTIFICATION -> notificationHandler.handleNotification((NotificationMessage) serverMessage);
                        case ERROR -> notificationHandler.handleError((ErrorMessage) serverMessage);
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException e) {
            throw new Exception();
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    private void sendCommand(UserGameCommand command) throws Exception {
        if (session == null || !session.isOpen()) {
            throw new Exception("WebSocket session is not active.");
        }
        try {
            String jsonCommand = this.gson.toJson(command);
            this.session.getBasicRemote().sendText(jsonCommand);
        } catch (IOException e) {
            throw new Exception("Failed to send WebSocket command: " + e.getMessage(), e);
        }
    }

    private void assertReady() throws Exception {
        if (this.session == null || !this.session.isOpen()) {
            throw new Exception("Not connected to WebSocket server.");
        }
        if (this.authToken == null || this.gameID == null) {
            throw new Exception("Credentials not set. Call connect(authToken, gameID) first.");
        }
    }

    public void connect(String authToken, Integer gameID) throws Exception {
        if (authToken == null || gameID == null) {
            throw new IllegalArgumentException("AuthToken and GameID cannot be null for connect.");
        }
        this.authToken = authToken;
        this.gameID = gameID;

        try {
            Connect connectCommand = new Connect(this.authToken, this.gameID);
            sendCommand(connectCommand);
            // Success is indicated by receiving messages (e.g., LOAD_GAME or ERROR)
            // via the NotificationHandler.
        } catch (Exception e) {
            // If sending the command fails, clear the stored credentials
            this.authToken = null;
            this.gameID = null;
            throw e; // Re-throw the exception
        }
    }

    public void leave() throws Exception {
        assertReady(); // Ensure we have authToken and gameID
        Leave leaveCommand = new Leave(this.authToken, this.gameID);
        sendCommand(leaveCommand);
    }

    public void makeMove(ChessMove move, ChessPiece promotionPiece) throws Exception {
        assertReady(); // Ensure we have authToken and gameID
        if (move == null) {
            throw new IllegalArgumentException("Move cannot be null.");
        }
        MakeMove makeMoveCommand = new MakeMove(this.authToken, this.gameID, move, promotionPiece);
        sendCommand(makeMoveCommand);
    }

    public void resign() throws Exception {
        assertReady(); // Ensure we have authToken and gameID
        Resign resignCommand = new Resign(this.authToken, this.gameID);
        sendCommand(resignCommand);
    }

    public void close() throws Exception {
        if (this.session != null && this.session.isOpen()) {
            try {
                this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Client requested disconnect"));
            } catch (IOException e) {
                throw new Exception("Failed to close WebSocket session cleanly: " + e.getMessage(), e);
            } finally {
                // Ensure state is cleared even if close throws an error
                this.session = null;
                this.authToken = null;
                this.gameID = null;
            }
        } else {
            // If already closed or null, clear state just in case
            this.session = null;
            this.authToken = null;
            this.gameID = null;
        }
    }
    
    public boolean isOpen() {
        return this.session != null && this.session.isOpen();
    }

    public String getCurrentAuthToken() {
        return this.authToken;
    }

    public Integer getCurrentGameID() {
        return this.gameID;
    }
}
