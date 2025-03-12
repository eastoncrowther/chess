package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable{
    private ChessPiece[][] board;
    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * finds the king given a certain team color
     */
    public ChessPosition getKing(ChessGame.TeamColor teamColor) {
        // parse the board and return the position of the king of a given team
        for (int row = 0; row <= 7; row ++) {
            for (int col = 0; col <= 7; col ++) {
                // make sure there is a piece there
                if (board[row][col] != null) {
                    ChessPiece piece = board[row][col];

                    // make sure the piece belongs to the desired team
                    if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                        // chess position is indexed 1 - 8
                        return new ChessPosition(row + 1, col + 1);
                    }
                }
            }
        }
        // if no king is found for the given team
        return null;
    }

    public ChessPosition getPosition(ChessPiece piece) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == piece) {
                    return new ChessPosition(row + 1, col + 1);
                }
            }
        }
        return null;
    }



    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // put black pieces on the board
        addPiece(new ChessPosition(1,1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        addPiece(new ChessPosition(1,2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));

        addPiece(new ChessPosition(1,3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));

        addPiece(new ChessPosition(1,4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));

        addPawnsRow(2, ChessGame.TeamColor.WHITE);

        // put white pieces on the board
        addPiece(new ChessPosition(8,1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));

        addPiece(new ChessPosition(8,2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));

        addPiece(new ChessPosition(8,3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));

        addPiece(new ChessPosition(8,4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));

        addPawnsRow(7, ChessGame.TeamColor.BLACK);
    }
    public void addPawnsRow (int rowIndex, ChessGame.TeamColor teamColor) {
        for (int colIndex = 1; colIndex <= 8; colIndex ++) {
            addPiece(new ChessPosition(rowIndex,colIndex), new ChessPiece(teamColor, ChessPiece.PieceType.PAWN));
        }
    }

    public Collection<ChessPiece> getAllPieces() {
        Collection<ChessPiece> pieces = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    pieces.add(piece);
                }
            }
        }
        return pieces;
    }

    @Override
    public ChessBoard clone() {
        try {
            // clone the board
            ChessBoard clone = (ChessBoard) super.clone();

            ChessPiece[][] cloneBoard = new ChessPiece[8][8];

            // make a copy of the chessboard.
            for (int row = 0; row <= 7; row ++) {
                for (int col = 0; col <= 7; col ++) {
                    if (this.board[row][col] != null) {
                        cloneBoard[row][col] = this.board[row][col].clone();
                    }
                }
            }
            clone.board = cloneBoard;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
    @Override
    public String toString() {
        String boardStr = "Back row white: ";
        for (int i = 0; i <= 7; i ++) {
            boardStr += board[0][i] + " ";
        }
        boardStr += "\n Back row black: ";
        for (int i = 0; i <= 7; i ++) {
            boardStr += board[7][i] + " ";
        }
        return boardStr;
    }
}
