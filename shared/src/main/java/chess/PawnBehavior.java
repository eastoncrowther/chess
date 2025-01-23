package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnBehavior implements PieceBehavior {

    @Override
    public Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pawnMoves = new ArrayList<>();
        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            whitePawnMoves(board, myPosition, pawnMoves);
        }
        else {
            blackPawnMoves(board, myPosition, pawnMoves);
        }

        return pawnMoves;
    }
    // black pawns start at the top of the board and move down, white pawns start at the bottom and go up

    public void whitePawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        // current position
        int row = myPosition.getRow() - 1;
        int col = myPosition.getColumn() - 1;

        // generate a possible next position
        row += 1;
        col += 1;

        // is the position in the boundries
        if (inBoundries(row, col)) {
            ChessPosition moveOne = new ChessPosition(row + 1, col + 1);
            // can only move foward for an empty space
            if (board.getPiece(moveOne) == null) {
                // check if the piece has reached the end of the board
                if (moveOne.getRow() == 8) {
                    pawnMoves.add(new ChessMove(myPosition, moveOne, ChessPiece.PieceType.QUEEN));
                }
                else {
                    pawnMoves.add(new ChessMove(myPosition, moveOne, null));
                }
            }
        }

        // moves a pawn can make given the presence of an enemy piece.
        int[][] diagonals = {
                {1, 1},
                {1, -1}
        };

        for (int[] diagonal : diagonals) {
            int incrementRow = diagonal[0];
            int incrementCol = diagonal[1];

            // current position
            row = myPosition.getRow() - 1;
            col = myPosition.getColumn() - 1;

            // generate a possible new position
            row += incrementRow;
            col += incrementCol;
            if (inBoundries(row, col)) {
                ChessPosition moveDiagonal = new ChessPosition(row + 1, col + 1);
                // can only move here if there is a black piece
                if (board.getPiece(moveDiagonal) != null) {
                    if (board.getPiece(moveDiagonal).getTeamColor() == ChessGame.TeamColor.BLACK) {
                        if (moveDiagonal.getRow() == 8) {
                            pawnMoves.add(new ChessMove(myPosition, moveDiagonal, ChessPiece.PieceType.QUEEN));
                        }
                        else {
                            pawnMoves.add(new ChessMove(myPosition, moveDiagonal, null));
                        }
                    }
                }
            }
        }
    }

    public void blackPawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        // current position
        int row = myPosition.getRow() - 1;
        int col = myPosition.getColumn() - 1;

        // generate a possible next position
        row -= 1;
        col -= 1;

        // is the position in the boundries
        if (inBoundries(row, col)) {
            ChessPosition moveOne = new ChessPosition(row + 1, col + 1);
            // can only move foward for an empty space
            if (board.getPiece(moveOne) == null) {
                // check if the piece has reached the end of the board
                if (moveOne.getRow() == 1) {
                    pawnMoves.add(new ChessMove(myPosition, moveOne, ChessPiece.PieceType.QUEEN));
                }
                else {
                    pawnMoves.add(new ChessMove(myPosition, moveOne, null));
                }
            }
        }

        // moves a pawn can make given the presence of an enemy piece.
        int[][] diagonals = {
                {-1, -1},
                {-1, 1}
        };

        for (int[] diagonal : diagonals) {
            int incrementRow = diagonal[0];
            int incrementCol = diagonal[1];

            // current position
            row = myPosition.getRow() - 1;
            col = myPosition.getColumn() - 1;

            // generate a possible new position
            row += incrementRow;
            col += incrementCol;
            if (inBoundries(row, col)) {
                ChessPosition moveDiagonal = new ChessPosition(row + 1, col + 1);
                // can only move here if there is a black piece
                if (board.getPiece(moveDiagonal) != null) {
                    if (board.getPiece(moveDiagonal).getTeamColor() == ChessGame.TeamColor.WHITE) {
                        if (moveDiagonal.getRow() == 1) {
                            pawnMoves.add(new ChessMove(myPosition, moveDiagonal, ChessPiece.PieceType.QUEEN));
                        }
                        else {
                            pawnMoves.add(new ChessMove(myPosition, moveDiagonal, null));
                        }
                    }
                }
            }
        }
    }
    public boolean inBoundries (int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return false;
        }
        return true;
    }

}
