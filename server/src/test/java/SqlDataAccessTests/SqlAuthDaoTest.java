package SqlDataAccessTests;

import dataaccess.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
                assertEquals(0, count, "authTable should be empty after clear() but contains records.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createAuth() {
        // Implement test logic here
    }

    @Test
    public void getAuth() {
        // Implement test logic here
    }

    @Test
    public void deleteAuth() {
        // Implement test logic here
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
