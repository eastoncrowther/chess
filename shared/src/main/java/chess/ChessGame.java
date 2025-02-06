package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor team;
    private ChessBoard chessBoard = new ChessBoard();

    public ChessGame() {
        chessBoard.resetBoard();
        this.team = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.team = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // make sure there is a piece at that position
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        // get valid moves for the piece
        Collection<ChessMove> possibleMoves = piece.pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        // make sure each move is really valid
        for (ChessMove move : possibleMoves) {
            // does the move put the king in check?
            if (safeMove(move, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    public boolean safeMove (ChessMove move, TeamColor teamColor) {
        ChessBoard tempBoard = chessBoard.clone();
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = tempBoard.getPiece(start);

        // make the move
        tempBoard.addPiece(start, null);
        tempBoard.addPiece(end, piece);

        // check the status
        if (moveCausesCheck(teamColor, tempBoard)) {
            return false;
        } else {
            return true;
        }
    }
    public boolean moveCausesCheck (TeamColor teamColor, ChessBoard tempBoard) {
        ChessPosition kingPosition = tempBoard.getKing(teamColor);

        // If member of the opposing team can reach kingPosition return true, else return false.

        // iterate through the chess positions
        for(int row = 1; row <= 8; row ++) {
            for(int col = 1; col <= 8; col ++) {
                ChessPiece piece = tempBoard.getPiece(new ChessPosition(row, col));
                // make sure there is a piece on the position
                if (piece != null) {
                    if (piece.getTeamColor() != teamColor) {
                        Collection<ChessMove> pieceMoves = piece.pieceMoves(tempBoard, new ChessPosition(row, col));
                        if (movesReachPosition(pieceMoves, kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = chessBoard.getPiece(start);


        // if there is no piece at the start position
        if (piece == null) {
            throw new InvalidMoveException("No piece at the start position: " + start);
        }
        // if it is not the right team's turn
        TeamColor currentTeam = piece.getTeamColor();
        if (this.team != currentTeam) {
            throw new InvalidMoveException("" + currentTeam + " is not" + this.team);
        }
        // if the move is not within the valid moves of the piece
        Collection<ChessMove> validMoves = validMoves(start);
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move for " + piece + " from " + start + " to " + end);
        }
        // move the piece
        this.chessBoard.addPiece(start, null);
        if (move.getPromotionPiece() == null) {
            this.chessBoard.addPiece(end, piece);
        }
        else {
            this.chessBoard.addPiece(end, new ChessPiece(currentTeam, move.getPromotionPiece()));
        }

        // update which team makes the next move
        this.team = switch (currentTeam) {
            case BLACK -> TeamColor.WHITE;
            case WHITE -> TeamColor.BLACK;
        };
    }

    // isInCheck helper function
    public boolean movesReachPosition (Collection<ChessMove> pieceMoves, ChessPosition kingPosition) {
        for (ChessMove move : pieceMoves) {
            if(move.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessBoard tempBoard = chessBoard.clone();
        ChessPosition kingPosition = tempBoard.getKing(teamColor);

        // If member of the opposing team can reach kingPosition return true, else return false.

        // iterate through the chess positions
        for(int row = 1; row <= 8; row ++) {
            for(int col = 1; col <= 8; col ++) {
                ChessPiece piece = tempBoard.getPiece(new ChessPosition(row, col));
                // make sure there is a piece on the position
                if (piece != null) {
                    if (piece.getTeamColor() != teamColor) {
                        Collection<ChessMove> pieceMoves = piece.pieceMoves(tempBoard, new ChessPosition(row, col));
                        if (movesReachPosition(pieceMoves, kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean kingMoveBreakCheck (TeamColor teamColor, ChessBoard tempBoard) {
        ChessPosition kingPosition = tempBoard.getKing(teamColor);
        Collection<ChessMove> kingMoves = validMoves(kingPosition);
        if(kingMoves.isEmpty()) {
            return false;
        }
        return true;
    }
    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // is the king in check
        if (!isInCheck(teamColor)) {
            return false;
        }
        ChessBoard tempBoard = chessBoard.clone();
        // Check if the king can move out of check
        if (kingMoveBreakCheck(teamColor, tempBoard)){
            return false;
        }
        // Check the valid moves for each piece with the same teamColor

        // Parse board for teammates
        for(int row = 1; row <= 8; row ++) {
            for(int col = 1; col <= 8; col ++) {
                ChessPiece piece = tempBoard.getPiece(new ChessPosition(row, col));
                // make sure there is a piece on the position
                if (piece != null) {
                    if (piece.getTeamColor() == teamColor) {
                        Collection<ChessMove> validMoves = validMoves(new ChessPosition(row, col));
                        for (ChessMove move : validMoves) {
                            if (safeMove(move, teamColor)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        for(int row = 1; row <= 8; row ++) {
            for(int col = 1; col <= 8; col ++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, col));
                // make sure there is a piece on the position
                if (piece != null) {
                    if (piece.getTeamColor() == teamColor) {
                        Collection<ChessMove> validMoves = validMoves(new ChessPosition(row, col));
                        if (!validMoves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }
}
