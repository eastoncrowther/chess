package SqlDataAccessTests;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.SqlAuthDao;
import dataaccess.SqlUserDAO;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SqlUserDAOTest {
    SqlUserDAO sqlUserDao;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.createDatabase();
        sqlUserDao = new SqlUserDAO();

        // Clear the userTable before each test
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE userTable")) {
                statement.executeUpdate();
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE userTable")) {
                statement.executeUpdate();
            }
        }
    }

    @Test
    void clear() {
        // Perform the clear operation
        sqlUserDao.clear();

        // Ensure the authTable is empty
        String query = "SELECT COUNT(*) FROM userTable";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(query);
             var resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                Assertions.assertEquals(0, count, "userTable should be empty after clear() but contains records.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createUser() throws DataAccessException, SQLException {
        UserData newUser = new UserData("easton", "0000", "easton.crowther@gmail.com");

        sqlUserDao.createUser(newUser);

        String retrievedUserName = null;
        String retrievedPassword = null;
        String retrievedEmail = null;

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement("SELECT username, password, email FROM userTable WHERE username = ?")) {
            statement.setString(1, newUser.username());
            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    retrievedUserName = results.getString("username");
                    retrievedPassword = results.getString("password");
                    retrievedEmail = results.getString("email");
                }
            }
        }

        // Compare expected and actual values
        Assertions.assertEquals(newUser.username(), retrievedUserName, "Usernames do not match.");
        Assertions.assertEquals(newUser.email(), retrievedEmail, "emails do not match.");
    }
    @Test
    void createDuplicateUser() throws DataAccessException {
        UserData newUser = new UserData("easton", "0000", "easton.crowther@gmail.com");

        sqlUserDao.createUser(newUser);

        // Attempting to create another user with the same username should throw an exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            sqlUserDao.createUser(newUser);
        });

        Assertions.assertEquals("User already exists", exception.getMessage(), "Duplicate user should not be inserted.");
    }

    @Test
    void getUser() throws DataAccessException {
        UserData newUser = new UserData("easton", "0000", "easton.crowther@gmail.com");

        sqlUserDao.createUser(newUser);

        UserData retrievedUser = sqlUserDao.getUser("easton");

        Assertions.assertEquals(newUser.username(), retrievedUser.username());
    }
    @Test
    void getNonExistentUser() {
        UserData retrievedUser = sqlUserDao.getUser("nonexistentuser");

        assertNull(retrievedUser, "Fetching a non-existent user should return null.");
    }
}