package service;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import requestResultRecords.LoginRequest;
import requestResultRecords.LoginResult;
import requestResultRecords.RegisterRequest;
import requestResultRecords.RegisterResult;

import static org.junit.jupiter.api.Assertions.fail;

class UserServiceTest {
    MemoryUserDAO userDB;
    MemoryAuthDAO authDB;
    UserService userService;

    @BeforeEach
    public void configureDAOs() throws Exception {
        userDB = new MemoryUserDAO();
        authDB = new MemoryAuthDAO();
        userService = new UserService(userDB, authDB);
    }

    @Test
    @DisplayName("Test register service")
    public void register() {

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
    @DisplayName("Test login with correct credentials")
    void loginSuccess() {
        try {
            userDB.createUser(new UserData("Easton", hashPassword("123"), "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        LoginResult successTest;
        try {
            successTest = userService.login(new LoginRequest("Easton", "123"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
            return;
        }

        Assertions.assertEquals("Easton", successTest.username(), "Username should match the one used for login.");
        Assertions.assertNotNull(successTest.authToken(), "Auth token should be generated upon successful login.");
    }
    String hashPassword(String password) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
    }

    @Test
    @DisplayName("Test logout")
    void logout() {
        try {
            userDB.createUser(new UserData("Easton", hashPassword("123"), "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        LoginResult loggedIn = null;

        try {
            loggedIn = userService.login(new LoginRequest("Easton", "123"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        Assertions.assertNotNull(loggedIn, "Login should be successful and return a LoginResult.");
        Assertions.assertNotNull(loggedIn.authToken(), "Auth token should be generated upon login.");

        // Test logout
        try {
            userService.logout(loggedIn.authToken());
        } catch (UnauthorizedException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        // Check that the auth token has been removed from the database
        Assertions.assertNull(authDB.getAuth(loggedIn.authToken()), "Auth token should be null after logout.");
    }

    @Test
    @DisplayName("Test clear")
    public void clear() {

        try {
            userDB.createUser(new UserData("Easton", "123", "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        userService.clear();

        // check if the userDB and authDB are empty
        Assertions.assertTrue(userDB.isEmpty());
        Assertions.assertTrue(authDB.isEmpty());
    }

    @Test
    @DisplayName("Test register service with duplicate username")
    public void registerDuplicateUsername() {
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
    void loginIncorrectPassword() {
        try {
            userDB.createUser(new UserData("Easton", hashPassword("123"), "easton.crowther@gmail.com"));
        } catch (DataAccessException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        Assertions.assertThrows(UnauthorizedException.class, () ->
                        userService.login(new LoginRequest("Easton", "wrongpassword")),
                "Logging in with an incorrect password should throw an UnauthorizedException."
        );
    }

    @Test
    @DisplayName("Test login with non-existent username")
    public void loginNonExistentUser() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.login(new LoginRequest("NonExistentUser", "password"))
        );
    }

    @Test
    @DisplayName("Test logout with invalid auth token")
    public void logoutInvalidAuthToken() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.logout("invalidToken")
        );
    }
}