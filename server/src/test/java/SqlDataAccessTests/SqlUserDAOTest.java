package SqlDataAccessTests;

import dataaccess.DatabaseManager;
import dataaccess.SqlAuthDao;
import dataaccess.SqlUserDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void createUser() {
    }

    @Test
    void getUser() {
    }
}