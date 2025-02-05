package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor team;
    private ChessBoard chessBoard;

    public ChessGame() {

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
        ChessBoard tempBoard = getBoard();


        // create a new instance of ChessPiece
        ChessPiece piece = tempBoard.getPiece(startPosition);
        // get all the moves that piece can make
        Collection<ChessMove> moves = piece.pieceMoves(getBoard(), startPosition);
        // return the moves.
        return moves;
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
        Collection<ChessMove> validMoves = piece.pieceMoves(getBoard(), start);
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move for " + piece + " from " + start + " to " + end);
        }


        // move the piece from move.startPosition() to move.endPosition()
        this.chessBoard.addPiece(start, null);
        this.chessBoard.addPiece(end, piece);

        // update which team makes the next move
        this.team = switch (currentTeam) {
            case BLACK -> TeamColor.WHITE;
            case WHITE -> TeamColor.BLACK;
        };
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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
        return chessBoard.clone();
    }
}
