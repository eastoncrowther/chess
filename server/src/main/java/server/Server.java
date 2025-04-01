package server;

import dataaccess.*;
import server.websocket.WebSocketHandler;
import service.BadRequestException;
import service.GameService;
import service.UnauthorizedException;
import service.UserService;
import spark.Spark;

public class Server {
    AuthDAO auths;
    GameDAO games;
    UserDAO users;

    UserService userService;
    GameService gameService;

    public Server () {
        try {
            auths = new SqlAuthDao();
            games = new SqlGameDAO();
            users = new SqlUserDAO();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        userService = new UserService(users, auths);
        gameService = new GameService(games, auths);

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // websocket endpoint
        Spark.webSocket("/connect", new WebSocketHandler());

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


