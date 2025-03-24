package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import requestResultRecords.JoinRequest;
import requestResultRecords.ListResult;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    @Test
    void list() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);

        // create some chess games
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();

        try {
            gameDB.createGame(new GameData(5, "Easton", "Canon", "Match1", game1));
            gameDB.createGame(new GameData(6, "Canon", "Easton", "Match2", game2));
            authDB.createAuth(new AuthData("1234", "Easton"));
        } catch (DataAccessException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }


        ListResult actualGames = null;
        try {
            actualGames = gameService.list("1234");
        } catch(UnauthorizedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

        Collection<GameData> expectedGames = new HashSet<>(Arrays.asList(
                new GameData(5, "Easton", "Canon", "Match1", game1),
                new GameData(6, "Canon", "Easton", "Match2", game2)
        ));

        Assertions.assertEquals(actualGames, new ListResult(expectedGames));
    }

    @Test
    void createGame() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);
        authDB.createAuth(new AuthData("1234", "easton"));

        try {
            gameService.createGame("match1", "1234");
        } catch (DataAccessException | UnauthorizedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

        ListResult gameList = new ListResult(new HashSet<>());
        try {
            gameList = gameService.list("1234");
        } catch (UnauthorizedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

        Collection<GameData> gameListUnwrapped = gameList.games();

        assertNotNull(gameListUnwrapped, "Game list should not be null");
        assertFalse(gameListUnwrapped.isEmpty(), "Game list should not be empty");
        assertTrue(gameListUnwrapped.stream().anyMatch(game -> "match1".equals(game.gameName())),
                "Game list should contain a game with the name 'match1'");
    }

    @Test
    void join() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);
        authDB.createAuth(new AuthData("1234", "easton"));

        // create some chess games
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();

        try {
            gameDB.createGame(new GameData(5, null, "Canon", "Match1", game1));
            gameDB.createGame(new GameData(6, "Canon", null, "Match2", game2));
        } catch (DataAccessException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

        try {
            gameService.join(new JoinRequest("WHITE", 5), "1234");
            gameService.join(new JoinRequest("BLACK", 6), "1234");
        } catch (DataAccessException | UnauthorizedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

        Collection<GameData> expectedGames = new HashSet<>(Arrays.asList(
                new GameData(5, "easton", "Canon", "Match1", game1),
                new GameData(6, "Canon", "easton", "Match2", game2)
        ));

        try {
            Assertions.assertEquals(gameService.list("1234"), new ListResult(expectedGames));
        } catch (UnauthorizedException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void clear() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();

        // add things to the databases
        authDB.createAuth(new AuthData("1234", "easton"));
        authDB.createAuth(new AuthData("3456", "james"));
        authDB.createAuth(new AuthData("78910", "josh"));

        // add new games
        try {
            gameDB.createGame(new GameData(5, null, "Canon", "Match1", new ChessGame()));
            gameDB.createGame(new GameData(6, "Canon", null, "Match2", new ChessGame()));
        } catch (DataAccessException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }

        GameService gameService = new GameService(gameDB, authDB);

        gameService.clear();

        Assertions.assertTrue(gameDB.isEmpty());
        Assertions.assertTrue(authDB.isEmpty());
    }
    @Test
    void listUnauthorized() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);

        assertThrows(UnauthorizedException.class, () -> gameService.list("invalidToken"));
    }

    @Test
    void createGameWithInvalidToken() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);

        assertThrows(UnauthorizedException.class, () -> {
            try {
                gameService.createGame("match1", "invalidToken");
            } catch (DataAccessException e) {
                fail("Unexpected DataAccessException: " + e.getMessage());
            }
        });
    }

    @Test
    void joinGameWithInvalidGameID() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);

        authDB.createAuth(new AuthData("1234", "easton"));

        assertThrows(BadRequestException.class, () -> {
            try {
                gameService.join(new JoinRequest("WHITE", 9999), "1234");
            } catch (UnauthorizedException e) {
                fail("Unexpected UnauthorizedException: " + e.getMessage());
            }
        });
    }

    @Test
    void joinGameWithInvalidToken() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);

        try {
            gameDB.createGame(new GameData(5, null, "Canon", "Match1", new ChessGame()));
        } catch (DataAccessException e) {
            fail("Unexpected DataAccessException: " + e.getMessage());
        }

        assertThrows(UnauthorizedException.class, () -> {
            try {
                gameService.join(new JoinRequest("WHITE", 5), "invalidToken");
            } catch (DataAccessException e) {
                fail("Unexpected DataAccessException: " + e.getMessage());
            }
        });
    }

    @Test
    void joinGameWithInvalidColor() {
        MemoryGameDAO gameDB = new MemoryGameDAO();
        MemoryAuthDAO authDB = new MemoryAuthDAO();
        GameService gameService = new GameService(gameDB, authDB);

        authDB.createAuth(new AuthData("1234", "easton"));
        try {
            gameDB.createGame(new GameData(5, null, "Canon", "Match1", new ChessGame()));
        } catch (DataAccessException e) {
            fail("Unexpected DataAccessException: " + e.getMessage());
        }

        assertThrows(BadRequestException.class, () -> {
            try {
                gameService.join(new JoinRequest("GREEN", 5), "1234");
            } catch (DataAccessException | UnauthorizedException e) {
                fail("Unexpected Exception: " + e.getMessage());
            }
        });
    }
}