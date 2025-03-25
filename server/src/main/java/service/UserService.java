package service;
import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requestresult.LoginRequest;
import requestresult.LoginResult;
import requestresult.RegisterRequest;
import requestresult.RegisterResult;

import java.util.UUID;

public class UserService {
    UserDAO userDAO;
    AuthDAO authDAO;

    public UserService (UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws BadRequestException, DataAccessException {
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new DataAccessException("user already exists");
        }
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        try {
            userDAO.createUser(user);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
        return new RegisterResult(user.username(), createAuth(user.username()));
    }

    public LoginResult login(LoginRequest loginRequest) throws UnauthorizedException, DataAccessException {
        UserData user = userDAO.getUser(loginRequest.username());
        if(user == null) {
            throw new UnauthorizedException("user doesn't exist");
        }

        if (!BCrypt.checkpw(loginRequest.password(), user.password())) {
            throw new UnauthorizedException("passwords don't match");
        }
        return new LoginResult(user.username(), createAuth(user.username()));
    }

    public void logout (String authToken) throws UnauthorizedException{
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("auth token is null");
        }
        try {
            authDAO.deleteAuth(auth);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    public void clear () {
        userDAO.clear();
        authDAO.clear();
    }

    private String createAuth (String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        authDAO.createAuth(auth);
        return authToken;
    }
}
