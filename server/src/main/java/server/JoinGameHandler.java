package server;

import com.google.gson.Gson;
import service.BadRequestException;
import service.GameService;
import requestResultRecords.JoinRequest;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;

    public JoinGameHandler (GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        String authToken = request.headers("authorization");

        JoinRequest joinRequest = serializer.fromJson(request.body(), JoinRequest.class);

        if (joinRequest.playerColor() == null) {
            throw new BadRequestException("request missing arguments");
        }

        gameService.join(joinRequest, authToken);

        response.status(200);
        return "{}";
    }
}
