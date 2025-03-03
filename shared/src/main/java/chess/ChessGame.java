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
        WHITE, BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) return null;

        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : piece.pieceMoves(chessBoard, startPosition)) {
            if (safeMove(move, piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private boolean safeMove(ChessMove move, TeamColor teamColor) {
        return !moveCausesCheck(teamColor, simulateMove(move));
    }

    private ChessBoard simulateMove(ChessMove move) {
        ChessBoard tempBoard = chessBoard.clone();
        tempBoard.addPiece(move.getEndPosition(), tempBoard.getPiece(move.getStartPosition()));
        tempBoard.addPiece(move.getStartPosition(), null);
        return tempBoard;
    }

    private boolean moveCausesCheck(TeamColor teamColor, ChessBoard board) {
        return isPositionThreatened(board.getKing(teamColor), board, teamColor);
    }

    private boolean isPositionThreatened(ChessPosition position, ChessBoard board, TeamColor teamColor) {
        for (ChessPiece piece : board.getAllPieces()) {
            ChessPosition piecePosition = board.getPosition(piece);
            if (piece.getTeamColor() != teamColor && piecePosition != null) {
                if (movesReachPosition(piece.pieceMoves(board, piecePosition), position)) {
                    return true;
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
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        if (team != piece.getTeamColor()) {
            throw new InvalidMoveException("Wrong team's turn");
        }
        if (!validMoves(move.getStartPosition()).contains(move))  {
            throw new InvalidMoveException("Invalid move");
        }

        if (move.getPromotionPiece() == null) {
            chessBoard.addPiece(move.getEndPosition(), piece);
        } else {
            chessBoard.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        chessBoard.addPiece(move.getStartPosition(), null);
        team = (team == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private boolean movesReachPosition(Collection<ChessMove> moves, ChessPosition position) {
        for (ChessMove move : moves) {
            if (move.getEndPosition().equals(position)) {
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
        return moveCausesCheck(teamColor, chessBoard);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidEscape(teamColor);
    }

    private boolean hasNoValidEscape(TeamColor teamColor) {
        for (ChessPiece piece : chessBoard.getAllPieces()) {
            ChessPosition piecePosition = chessBoard.getPosition(piece);
            if (piece.getTeamColor() == teamColor && piecePosition != null) {
                Collection<ChessMove> moves = validMoves(piecePosition);
                for (ChessMove move : moves) {
                    if (safeMove(move, teamColor)) {
                        return false;
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
        return !isInCheck(teamColor) && hasNoValidEscape(teamColor);
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
