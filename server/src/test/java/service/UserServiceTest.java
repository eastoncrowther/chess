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

        RegisterResult actual = null;
        try {
            actual = userService.register(new RegisterRequest("Easton", "123", "easton.crowther@gmail.com"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        LoginResult successTest = null;
        var userService = new UserService(userDB, authDB);
        try {
            successTest = userService.login(new LoginRequest("Easton", "123"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals("Easton", successTest.username());

    }

    @Test
    @DisplayName("Test logout")
    public void logout() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();

        try {
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        var userService = new UserService(userDB, authDB);
        LoginResult loggedIn = null;

        try {
            loggedIn = userService.login(new LoginRequest("Easton", "123"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // test logout
        try {
            userService.logout(loggedIn.authToken());
        } catch (UnauthorizedException e) {
            throw new RuntimeException(e);
        }

        // check if the  auth token is in the database
        Assertions.assertNull(authDB.getAuth(loggedIn.authToken()));
    }
    @Test
    @DisplayName("Test clear")
    public void clear () {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();

        try {
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        var userService = new UserService(userDB, authDB);

        userService.clear();

        // check if the userDB and authDB are empty
        Assertions.assertTrue(userDB.isEmpty());
        Assertions.assertTrue(authDB.isEmpty());
    }
}