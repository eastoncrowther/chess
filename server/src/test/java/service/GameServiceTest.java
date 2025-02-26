package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    @Test
    void list() {
        var gameDB = new MemoryGameDAO();
        var authDB = new MemoryAuthDAO();

        // create some chess games
        var game1 = new ChessGame();
        var game2 = new ChessGame();

        try {
            gameDB.createGame(new GameData(5, "Easton", "Canon", "Match1", game1));
            gameDB.createGame(new GameData(6, "Canon", "Easton", "Match2", game2));
            authDB.createAuth(new AuthData("1234", "Easton"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        var gameService = new GameService(gameDB, authDB);
        Collection<GameData> actualGames = gameService.list("1234");

        Collection<GameData> expectedGames = new HashSet<>(Arrays.asList(
                new GameData(5, "Easton", "Canon", "Match1", game1),
                new GameData(6, "Canon", "Easton", "Match2", game2)
        ));
        Assertions.assertEquals(actualGames, expectedGames);
    }

    @Test
    void createGame() {
        var gameDB = new MemoryGameDAO();
        var authDB = new MemoryAuthDAO();



    }

    @Test
    void join() {
    }

    @Test
    void clear() {
    }
}