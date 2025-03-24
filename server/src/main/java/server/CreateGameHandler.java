package server;

import com.google.gson.Gson;
import model.GameData;
import requestResultRecords.CreateResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
    private final GameService gameService;

    public CreateGameHandler (GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        GameData gameData = serializer.fromJson(request.body(), GameData.class);

        String authToken = request.headers("authorization");

        CreateResult createResult = gameService.createGame(gameData.gameName(), authToken);
        return serializer.toJson(createResult);
    }
}
