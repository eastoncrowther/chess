package client;

import org.junit.jupiter.api.*;
import requestresult.*;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    static String authToken;
    static int gameId;

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

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    void register() throws Exception {
        var authData = facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertTrue(authData.authToken().length() > 10);

        authToken = authData.authToken();
    }

    @Test
    void negativeRegister() {
       assertThrows(Exception.class, () ->
                facade.register(new RegisterRequest(null, null, null))
       );
    }



    @Test
    void login() throws Exception {
        facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        var loginResult = facade.login(new LoginRequest("username", "password"));

        assertNotNull(loginResult);
        assertNotNull(loginResult.authToken());
        assertTrue(loginResult.authToken().length() > 10);

        authToken = loginResult.authToken();
    }

    @Test
    void negativeLogin() {
        assertThrows(Exception.class, () ->
                facade.login(new LoginRequest(null, null))
        );
    }

    @Test
    void logout() throws Exception {
        facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        var loginResult = facade.login(new LoginRequest("username", "password"));
        assertNotNull(loginResult.authToken());

        facade.logout(loginResult.authToken());
    }

    @Test
    void negativeLogout () {
        assertThrows(Exception.class, () ->
                facade.logout(null)
        );
    }

    @Test
    void listGames() throws Exception {
        facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        var loginResult = facade.login(new LoginRequest("username", "password"));

        var listResult = facade.list(loginResult.authToken());
        assertNotNull(listResult);
        assertNotNull(listResult.games());
        assertEquals(0, listResult.games().size());  // Initially empty
    }

    @Test
    void negativeListGames() {
        assertThrows(Exception.class, () ->
                facade.list(null)
        );
    }

    @Test
    void createGame() throws Exception {
        facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        var loginResult = facade.login(new LoginRequest("username", "password"));

        var createResult = facade.createGame(new CreateRequest("Test Game"), loginResult.authToken());
        assertNotNull(createResult);
        assertTrue(createResult.gameID() > 0);

        gameId = createResult.gameID();
    }

    @Test
    void negativeCreateGame() {
        assertThrows(Exception.class, () ->
                facade.createGame(new CreateRequest("newGame"), null)
        );
    }

    @Test
    void joinGame() throws Exception {
        facade.register(new RegisterRequest("username", "password", "email@gmail.com"));
        var loginResult = facade.login(new LoginRequest("username", "password"));

        var createResult = facade.createGame(new CreateRequest("Test Game"), loginResult.authToken());
        assertTrue(createResult.gameID() > 0);

        JoinRequest joinRequest = new JoinRequest("WHITE", createResult.gameID());
        facade.join(joinRequest, loginResult.authToken());
    }

    @Test
    void negativeJoinGame() {
        assertThrows(Exception.class, () ->
                facade.join(new JoinRequest("purple", 1), null)
        );
    }
}
