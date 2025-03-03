package server;

import com.google.gson.Gson;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import service.BadRequestException;
import service.GameService;
import service.UnauthorizedException;
import service.UserService;
import spark.Spark;

import java.util.Map;

public class Server {
    private final MemoryAuthDAO auths = new MemoryAuthDAO();
    private final MemoryGameDAO games = new MemoryGameDAO();
    private final MemoryUserDAO users = new MemoryUserDAO();

    private final UserService userService = new UserService(users, auths);
    private final GameService gameService = new GameService(games, auths);
    private final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // user endpoints
        Spark.post("/session", new LoginRequestHandler(userService));
        Spark.post("/user", new RegisterRequestHandler(userService));
        Spark.delete("/session", new LogoutRequestHandler(userService));
        Spark.delete("/db", new ClearHandler(userService, gameService));

        // game endpoints
        Spark.post("/game", new CreateGameHandler(gameService));
        Spark.get("/game", new ListGameHandler(gameService));
        Spark.put("/game", new JoinGameHandler(gameService));


        // Exception handling
        Spark.exception(BadRequestException.class, new HandleExceptions());

        Spark.exception(UnauthorizedException.class, new HandleExceptions());

        Spark.exception(Exception.class, new HandleExceptions());

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}


