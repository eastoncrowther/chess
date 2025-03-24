package client;

import org.junit.jupiter.api.*;
import requestResultRecords.RegisterRequest;
import server.Server;
import server.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade("http://127.0.0.1:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void register() throws Exception {
        var authData = facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        Assertions.assertTrue(authData.authToken().length() > 10);
    }


}
