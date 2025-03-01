package server;

import dataaccess.DataAccessException;
import service.BadRequestException;
import service.UnauthorizedException;
import spark.Request;
import spark.Response;
import spark.ExceptionHandler;

public class HandleExceptions implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request request, Response response) {
        if (e instanceof BadRequestException) {
            response.status(400);
            response.body("{ \"message\": \"Error: bad request\" }");
        } else if (e instanceof UnauthorizedException) {
            response.status(401);
            response.body("{ \"message\": \"Error: unauthorized\" }");
        } else if (e instanceof DataAccessException) {
            response.status(403);
            response.body("{ \"message\": \"Error: already taken\" }");
        } else {
            response.status(500);
            response.body("{ \"message\": \"Error: %s\" }".formatted(e.getMessage()));
        }
    }
}
