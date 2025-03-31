package server.websocket;
import javax.websocket.*;
import java.net.URI;


public class WebSocketFacade extends Endpoint {

    Session session;

    public WebSocketFacade (String url) throws Exception {
//        try {
//            url = url.replace("http", "ws");
//            URI socketURI = new URI(url + "/ws");
//
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            this.session = container.connectToServer(this, socketURI);
//        }
    }



    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {}
}
