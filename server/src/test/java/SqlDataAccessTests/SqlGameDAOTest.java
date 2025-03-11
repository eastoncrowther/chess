package SqlDataAccessTests;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.SqlGameDAO;
import model.GameData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.Collection;

class SqlGameDAOTest {
    SqlGameDAO sqlGameDao;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.createDatabase();
        sqlGameDao = new SqlGameDAO();

        // Clear the authTable before each test
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE gameTable")) {
                statement.executeUpdate();
            }
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE gameTable")) {
                statement.executeUpdate();
            }
        }
    }

    @Test
    void clear() {

        // Perform the clear operation
        sqlGameDao.clear();

        // Ensure the authTable is empty
        String query = "SELECT COUNT(*) FROM gameTable";
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(query);
             var resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                Assertions.assertEquals(0, count, "gameTable should be empty after clear() but contains records.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createGame() throws DataAccessException, SQLException {
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(1, "easton", "canon", "match 1", newGame);

        sqlGameDao.createGame(newGameData);

        // Ensure the game data was added
        int retrievedGameID = 0;
        String retrievedWhiteUsername = null;
        String retrievedBlackUsername = null;
        String retrievedGameName = null;
        ChessGame retrievedChessGame = null;


        try (var conn = DatabaseManager.getConnection();
            var statement = conn.prepareStatement("SELECT * FROM gameTable WHERE gameID = ?")) {

            statement.setInt(1, 1);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    retrievedGameID = results.getInt("gameID");
                    retrievedWhiteUsername = results.getString("whiteUsername");
                    retrievedBlackUsername = results.getString("blackUsername");
                    retrievedGameName = results.getString("gameName");

                    Gson serializer = new Gson();
                    retrievedChessGame = serializer.fromJson(results.getString("chessGame"), ChessGame.class);
                }
            }
        }

        Assertions.assertEquals(1, retrievedGameID);
        Assertions.assertEquals("easton", retrievedWhiteUsername);
        Assertions.assertEquals("canon", retrievedBlackUsername);
        Assertions.assertEquals("match 1", retrievedGameName);
        Assertions.assertEquals(newGame, retrievedChessGame);
    }

    @Test
    void getGame() throws DataAccessException{
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(1, "easton", "canon", "match 1", newGame);

        sqlGameDao.createGame(newGameData);

        GameData retrievedGame = sqlGameDao.getGame(1);

        Assertions.assertEquals(newGameData, retrievedGame);
    }

    @Test
    void listGames() throws DataAccessException {
        GameData game1 = new GameData(1, "easton", "canon", "match 1", new ChessGame());
        GameData game2 = new GameData(2, "easton", "canon", "match 2", new ChessGame());

        sqlGameDao.createGame(game1);
        sqlGameDao.createGame(game2);

        // Retrieve the list of games
        Collection<GameData> listResult = sqlGameDao.listGames();

        // Ensure the list contains the two added games
        Assertions.assertNotNull(listResult, "The game list should not be null");
        Assertions.assertEquals(2, listResult.size(), "There should be exactly 2 games in the list");

        // Check if both games exist in the list
        Assertions.assertTrue(listResult.contains(game1), "Game 1 should be in the list");
        Assertions.assertTrue(listResult.contains(game2), "Game 2 should be in the list");
    }


    @Test
    void updateGame() throws DataAccessException {
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(1, "easton", "canon", "match 1", newGame);
        GameData updatedGameData = new GameData(1, "charles", "canon", "match 1", newGame);

        sqlGameDao.createGame(newGameData);

        sqlGameDao.updateGame(updatedGameData);

        Assertions.assertEquals(sqlGameDao.getGame(1), updatedGameData);
    }
}