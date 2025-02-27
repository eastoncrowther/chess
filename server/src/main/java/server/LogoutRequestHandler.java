package server;

import com.google.gson.Gson;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutRequestHandler implements Route {
    private final UserService userService;

    LogoutRequestHandler (UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        // get the AuthToken
        String authToken = serializer.fromJson(request.headers("authorization"), String.class);

        // userService will verify the AuthToken
        userService.logout(authToken);

        return "{}";
    }
}
