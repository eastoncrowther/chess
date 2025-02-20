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

        return new RegisterResult(user.username(), createAuth(user.username()));
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException{
        UserData user = userDAO.getUser(loginRequest.username());
        if(user == null) {
            // the user doesn't exist
            throw new DataAccessException("User doesn't exist");
        }

        return new LoginResult(user.username(), createAuth(user.username()));
    }
    
    public void logout (String authToken) {
        try {
            AuthData auth = authDAO.getAuth(authToken);
            try {
                authDAO.deleteAuth(auth);
            } catch (DataAccessException e) {
                throw new RuntimeException(e);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String createAuth (String username) {
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, username);
        try {
            authDAO.createAuth(auth);
        } catch (DataAccessException e){
            throw new RuntimeException(e);
        }
        return authToken;
    }



}
