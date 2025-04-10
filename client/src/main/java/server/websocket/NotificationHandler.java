package server.websocket;

import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void handleError (ErrorMessage errorMessage);
    void handleLoadGame (LoadGameMessage loadGameMessage);
    void handleNotification (NotificationMessage notificationMessage);

}
