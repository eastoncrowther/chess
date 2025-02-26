package server;
import com.google.gson.Gson;
import service.LoginRequest;
import service.LoginResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginRequestHandler implements Route {
    UserService userService;

    public LoginRequestHandler (UserService userService) {
        this.userService = userService;
    }
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Gson serializer = new Gson();

        LoginRequest loginRequest = serializer.fromJson(request.body(), LoginRequest.class);

        LoginResult loginResult = userService.login(loginRequest);

        return serializer.toJson(loginResult);
    }
}
