package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;

class UserServiceTest {

    @Test
    @DisplayName("Test register service")
    public void register() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();

        var userService = new UserService(userDB, authDB);

        RegisterResult actual = userService.register(new RegisterRequest("Easton", "123", "easton.crowther@gmail.com"));
        // Assert the username is correct
        Assertions.assertEquals("Easton", actual.username());

        // Assert the authToken is not null or empty
        Assertions.assertNotNull(actual.authToken());
        Assertions.assertFalse(actual.authToken().isEmpty());
    }

    @Test
    @DisplayName("Test login")
    public void login() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        
        try {
            // add me to the database
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        
        var userService = new UserService(userDB, authDB);

        LoginResult successTest = userService.login(new LoginRequest("Easton", "123"));
        LoginResult noUser = userService.login(new LoginRequest("harry", "123"));
        LoginResult badPassword = userService.login(new LoginRequest("Easton", "124"));

        // the login was successful
        Assertions.assertEquals("Easton", successTest.username());
        Assertions.assertNull(noUser);
        Assertions.assertNull(badPassword);
    }
    @Test
    @DisplayName("Test unsuccessful login")
    public void loginFail () {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        var userService = new UserService(userDB, authDB);

    }

    @Test
    @DisplayName("Test logout")
    public void logout() {

    }
    @Test
    @DisplayName("Test clear")
    public void clear () {

    }
}