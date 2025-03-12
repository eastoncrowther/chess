package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;


class SqlAuthDaoTest {

    SqlAuthDao sqlAuthDao;

    @BeforeEach
    void construct() throws Exception {
        DatabaseManager.createDatabase();
        sqlAuthDao = new SqlAuthDao();

        // Clear the authTable before each test
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE authTable")) {
                statement.executeUpdate();
            }
        }
    }

    @Test
    public void clear() {

        // Perform the clear operation
        sqlAuthDao.clear();

        // Ensure the authTable is empty
        String query = "SELECT COUNT(*) FROM authTable";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(query);
             var resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                Assertions.assertEquals(0, count, "authTable should be empty after clear() but contains records.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createAuth() throws DataAccessException, SQLException {
        AuthData newAuth = new AuthData("1234", "easton");

        // Insert the new auth data
        sqlAuthDao.createAuth(newAuth);

        // Query the database to check if the auth data was inserted correctly
        String retrievedUsername = null;
        String retrievedToken = null;

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement("SELECT username, authToken FROM authTable WHERE username = ?")) {

            statement.setString(1, newAuth.username());

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    retrievedUsername = results.getString("username");
                    retrievedToken = results.getString("authToken");
                }
            }
        }

        // Ensure data was retrieved correctly
        Assertions.assertNotNull(retrievedUsername, "No data found for the given username.");
        Assertions.assertNotNull(retrievedToken, "No auth token found for the given username.");

        // Compare expected and actual values
        Assertions.assertEquals(newAuth.username(), retrievedUsername, "Usernames do not match.");
        Assertions.assertEquals(newAuth.authToken(), retrievedToken, "Auth tokens do not match.");
    }


    @Test
    public void getAuth() throws DataAccessException {
        AuthData newAuth = new AuthData("1234", "easton");

        sqlAuthDao.createAuth(newAuth);

        AuthData retrievedAuth = sqlAuthDao.getAuth("1234");

        Assertions.assertEquals(newAuth, retrievedAuth);
    }

    @Test
    public void deleteAuth() throws DataAccessException, SQLException {
        AuthData newAuth = new AuthData("1234", "easton");

        sqlAuthDao.createAuth(newAuth);

        sqlAuthDao.deleteAuth(newAuth);

        // make sure the authToken was deleted
        String statementString = "SELECT username, authToken FROM authTable WHERE authToken = 1234";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                try (var results = statement.executeQuery()) {
                    // there should be no results
                    Assertions.assertFalse(results.next());
                }
            }
        }


    }

    @AfterEach
    void destruct() throws Exception {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE authTable")) {
                statement.executeUpdate();
            }
        }
    }
}
