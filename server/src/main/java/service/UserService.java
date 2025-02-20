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

    public RegisterResult register(RegisterRequest registerRequest) {
        if (userDAO.getUser(registerRequest.username()) != null) {
            // the user already exists
            throw new RuntimeException("User already exists");
        }
        // create the new user
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());

        try {
            userDAO.createUser(user);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        return new RegisterResult(user.username(), createAuth(user.username()));
    }

    public LoginResult login(LoginRequest loginRequest) {
        UserData user = userDAO.getUser(loginRequest.username());
        if(user == null) {
            // the user doesn't exist
            return null;
        }
        // check to make sure user password matches the saved password
        if (!user.password().equals(loginRequest.password())) {
            return null;
        }

        return new LoginResult(user.username(), createAuth(user.username()));
    }

    public void logout (String authToken) {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            return;
        }
        try {
            authDAO.deleteAuth(auth);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void clear () {
        userDAO.clear();
        authDAO.clear();
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
