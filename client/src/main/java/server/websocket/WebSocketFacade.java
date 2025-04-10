package server.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    private final Gson gson;

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





}
