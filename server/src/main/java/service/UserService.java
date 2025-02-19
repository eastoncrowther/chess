package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    MemoryUserDAO userDAO;
    MemoryAuthDAO authDAO;

    public UserService (MemoryUserDAO userDAO, MemoryAuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (userDAO.getUser(registerRequest.username()) != null) {
            // the user already exists
            throw new DataAccessException("User already exists");
        }
        // create the new user
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        userDAO.createUser(user);

        // create Auth token
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, user.username());
        authDAO.createAuth(auth);

        return new RegisterResult(user.username(), authToken);
    }
    public LoginResult login(LoginRequest loginRequest) {}
    public void logout (LogoutRequest logoutRequest) {}



}
