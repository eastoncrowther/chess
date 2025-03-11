package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;


public class SqlGameDAO implements GameDAO {
    public SqlGameDAO () throws Exception {
        configureDatabase();
    }

    @Override
    public void clear() {
        String statementString = "TRUNCATE gameTable";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {
            statement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing gameTable", e);
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        var statementString = "INSERT INTO gameTable (gameID, whiteUsername, blackUsername, gameName, chessGame)" +
                "VALUES (?, ?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                String gameJson = new Gson().toJson(game.game());

                statement.setInt(1, game.gameID());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, game.gameName());
                statement.setString(5, gameJson);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Game insert failed: missing fields");
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error inserting game data");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var statementString = "SELECT * FROM gameTable WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                statement.setInt(1, gameID);

                try (var results = statement.executeQuery()) {
                    results.next();
                    return readGameData(results);
                }
            }
        } catch (SQLException | DataAccessException e) {
            return null;
        }
    }

    @Override
    public Collection<GameData> listGames() {
        Collection<GameData> retrievedGames = new HashSet<>();

        var statementString = "SELECT * FROM gameTable;";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                try (var results = statement.executeQuery()) {
                    while (results.next()) {
                        retrievedGames.add(readGameData(results));
                    }
                    return retrievedGames;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error listing games");
        }
    }

    private GameData readGameData(ResultSet results) throws SQLException {

        int gameID = results.getInt("gameID");
        String whiteUsername = results.getString("whiteUsername");
        String blackUsername = results.getString("blackUsername");
        String gameName = results.getString("gameName");

        Gson serializer = new Gson();
        ChessGame chessGame = serializer.fromJson(results.getString("chessGame"), ChessGame.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }


    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statementString = "UPDATE gameTable SET whiteUsername = ?, blackUsername = ?, gameName = ?, chessGame = ? WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement(statementString)) {
                String gameJson = new Gson().toJson(game.game());

                statement.setString(1, game.whiteUsername());
                statement.setString(2, game.blackUsername());
                statement.setString(3, game.gameName());
                statement.setString(4, gameJson);
                statement.setInt(5, game.gameID());

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Game update failed, no game found with gameID: " + game.gameID());
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error updating game data");
        }
    }

    // function to help with generating new gameIDs
    @Override
    public boolean gameIDinUse(int gameID) {
        var statementString = "SELECT COUNT(*) FROM gameTable WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection();
             var statement = conn.prepareStatement(statementString)) {

            statement.setInt(1, gameID);

            try (var results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1) > 0;
                }
            }
        } catch (SQLException | DataAccessException e) {
            System.err.println("Error checking if gameID is in use: " + e.getMessage());
        }

        return false;
    }



    private final String[] createStatements = {
            """
            CREATE TABLE if NOT EXISTS gameTable
            (
            gameID INT NOT NULL,
            whiteUsername VARCHAR(255) NOT NULL,
            blackUsername VARCHAR(255) NOT NULL,
            gameName VARCHAR(255) NOT NULL,
            chessGame TEXT
            )
            """
    };

    private void configureDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
