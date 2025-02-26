package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {
    MemoryAuthDAO auths = new MemoryAuthDAO();
    MemoryGameDAO games = new MemoryGameDAO();
    MemoryUserDAO users = new MemoryUserDAO();

    UserService userService = new UserService(users, auths);
    GameService gameService = new GameService(games, auths);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/session", new LoginRequestHandler(userService));

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
