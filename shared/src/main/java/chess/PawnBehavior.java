package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnBehavior implements PieceBehavior {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pawnMoves = new ArrayList<>();
        if (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
            whitePawnMoves(board, myPosition, pawnMoves);
        }
        else {
            blackPawnMoves(board, myPosition, pawnMoves);
        }
        return pawnMoves;
    }

    public void whitePawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        if (myPosition.getRow() == 2) {
            moveTwo(board, myPosition, 1, pawnMoves);
        }
        else {
            moveOne(board, myPosition, myPosition, 1, pawnMoves);
        }
        int[][] diagonals = {
                {1, 1},
                {1, -1}
        };
        for (int[] diagonal : diagonals) {
            int row = myPosition.getRow() + diagonal[0];
            int col = myPosition.getColumn() + diagonal[1];

            // check to see if the position is in bounds
            if (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);

                // check to see if there is a piece
                if (board.getPiece(nextPosition) != null && board.getPiece(nextPosition).getTeamColor() != ChessGame.TeamColor.WHITE) {
                    // add the move
                    if (endOfBoard(nextPosition.getRow())) {
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.QUEEN));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.BISHOP));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.KNIGHT));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.ROOK));
                    }
                    else {
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                }
            }
        }
    }
    public void blackPawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        if (myPosition.getRow() == 7) {
            moveTwo(board, myPosition, -1, pawnMoves);
        }
        else {
            moveOne(board, myPosition, myPosition, -1, pawnMoves);
        }
        int[][] diagonals = {
                {-1, 1},
                {-1, -1}
        };
        for (int[] diagonal : diagonals) {
            int row = myPosition.getRow() + diagonal[0];
            int col = myPosition.getColumn() + diagonal[1];

            // check to see if the position is in bounds
            if (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);

                // check to see if there is a piece
                if (board.getPiece(nextPosition) != null && board.getPiece(nextPosition).getTeamColor() != ChessGame.TeamColor.BLACK) {
                    // add the move
                    if (endOfBoard(nextPosition.getRow())) {
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.QUEEN));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.BISHOP));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.KNIGHT));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.ROOK));
                    }
                    else {
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                }
            }
        }
    }

    public void moveOne (ChessBoard board, ChessPosition myPosition, ChessPosition initialPosition, int direction, Collection<ChessMove> pawnMoves) {
        int row = initialPosition.getRow() + direction;
        int col = initialPosition.getColumn();

        // check to see if the position is in bounds
        if (inBounds(row, col)) {
            // make a new position
            ChessPosition nextPosition = new ChessPosition(row, col);

            // check if there is a piece on the position
            if (board.getPiece(nextPosition) == null) {
                if (endOfBoard(nextPosition.getRow())) {
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.QUEEN));
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.BISHOP));
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.KNIGHT));
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.ROOK));
                }
                else {
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
            }
        }
    }

    public void moveTwo (ChessBoard board, ChessPosition myPosition, int direction, Collection<ChessMove> pawnMoves) {
        int row = myPosition.getRow() + direction;
        int col = myPosition.getColumn();

        // check to see if the position is in bounds
        if (inBounds(row, col)) {
            // make a new position
            ChessPosition nextPosition = new ChessPosition(row, col);

            // check if there is a piece on the position
            if (board.getPiece(nextPosition) == null) {
                pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
                moveOne(board, myPosition, nextPosition, direction, pawnMoves);
            }
        }
    }




    public boolean endOfBoard (int row) {
        if (row == 8 || row == 1) {
            return true;
        }
        return false;
    }

    public boolean inBounds (int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return true;
    }
}
