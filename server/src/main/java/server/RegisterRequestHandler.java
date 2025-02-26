package server;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterRequestHandler implements Route {
    UserService userService;

    public RegisterRequestHandler (UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        RegisterRequest registerRequest = serializer.fromJson(request.body(), RegisterRequest.class);

        RegisterResult registerResult = userService.register(registerRequest);

        return serializer.toJson(registerResult);
    }
}
