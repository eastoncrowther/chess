package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.fail;

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
        MemoryUserDAO userDB = new MemoryUserDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();

        try {
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        UserService userService = new UserService(userDB, authDB);

        userService.clear();

        // check if the userDB and authDB are empty
        Assertions.assertTrue(userDB.isEmpty());
        Assertions.assertTrue(authDB.isEmpty());
    }
    @Test
    @DisplayName("Test register service with duplicate username")
    public void registerDuplicateUsername() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        var userService = new UserService(userDB, authDB);

        try {
            userService.register(new RegisterRequest("Easton", "123", "easton.crowther@gmail.com"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Attempt to register with the same username
        Assertions.assertThrows(DataAccessException.class, () ->
                userService.register(new RegisterRequest("Easton", "456", "different.email@gmail.com"))
        );
    }

    @Test
    @DisplayName("Test login with incorrect password")
    public void loginIncorrectPassword() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        var userService = new UserService(userDB, authDB);

        try {
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.login(new LoginRequest("Easton", "wrongpassword"))
        );
    }

    @Test
    @DisplayName("Test login with non-existent username")
    public void loginNonExistentUser() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        var userService = new UserService(userDB, authDB);

        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.login(new LoginRequest("NonExistentUser", "password"))
        );
    }

    @Test
    @DisplayName("Test logout with invalid auth token")
    public void logoutInvalidAuthToken() {
        var userDB = new MemoryUserDAO();
        var authDB = new MemoryAuthDAO();
        var userService = new UserService(userDB, authDB);

        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.logout("invalidToken")
        );
    }
}