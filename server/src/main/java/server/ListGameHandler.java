package server;

import com.google.gson.Gson;
import model.GameData;
import service.GameService;
import service.ListResult;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collection;

public class ListGameHandler implements Route {
    private final GameService gameService;

    ListGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        String authToken = serializer.fromJson(request.headers("authorization"), String.class);

        ListResult games = gameService.list(authToken);

        return serializer.toJson(games);
    }
}
