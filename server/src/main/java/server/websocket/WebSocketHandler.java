package server.websocket;


import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage (Session session, String message) throws IOException {

    }
}
