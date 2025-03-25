package server;

import com.google.gson.Gson;
import service.BadRequestException;
import requestresult.RegisterRequest;
import requestresult.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class RegisterRequestHandler implements Route {
    private final UserService userService;

    public RegisterRequestHandler (UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        RegisterRequest registerRequest = serializer.fromJson(request.body(), RegisterRequest.class);
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new BadRequestException("One of the fields is empty");
        }

        RegisterResult registerResult = userService.register(registerRequest);
        return serializer.toJson(registerResult);
    }
}
