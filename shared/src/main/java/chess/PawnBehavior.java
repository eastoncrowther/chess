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

    // white pawns start at the bottom and go up
    public void whitePawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        if (moveOneSpace(1, board, myPosition, myPosition, pawnMoves)) {
            if (myPosition.getRow() == 2) {
                moveOneSpace(1, board, new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn()), myPosition, pawnMoves);
            }
        }
        int[][] whitePawnDiagonal = {
                {1, 1},
                {1, -1}
        };
        moveDiagonally(whitePawnDiagonal, board, myPosition, pawnMoves, ChessGame.TeamColor.WHITE);
    }

    // black pawns start at the top and go down
    public void blackPawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        if (moveOneSpace(-1, board, myPosition, myPosition, pawnMoves)) {
            if (myPosition.getRow() == 7) {
                moveOneSpace(-1, board, new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn()), myPosition, pawnMoves);
            }
        }
        int[][] blackPawnDiagonal = {
                {-1, 1},
                {-1, -1}
        };
        moveDiagonally(blackPawnDiagonal, board, myPosition, pawnMoves, ChessGame.TeamColor.BLACK);
    }

    public void moveDiagonally (int[][] moves, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves, ChessGame.TeamColor teamColor) {
        for (int[] move : moves) {
            // on the scale 1 - 8
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            if(inBounds(row + move[0], col + move[1])) {
                // check if there is a piece
                ChessPosition nextPosition = new ChessPosition(row + move[0], col + move[1]);

                ChessGame.TeamColor diagonalPiece = null;
                if (board.getPiece(nextPosition) != null) {
                    diagonalPiece = board.getPiece(nextPosition).getTeamColor();
                }

                if (diagonalPiece != null && diagonalPiece != teamColor) {
                    // add move
                    if (endOfBoard(nextPosition.getRow())) {
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.QUEEN));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.KNIGHT));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.ROOK));
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.BISHOP));
                    }
                    else {
                        pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                }
            }
        }
    }



    // direction should be int -1 for black and +1 for white
    public boolean moveOneSpace (int direction, ChessBoard board, ChessPosition currentPosition, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        // on the scale 1 - 8
        int row = currentPosition.getRow();
        int col = currentPosition.getColumn();

        // check the space 1 up from the current position
        if (inBounds(row + direction, col)) {
            // check if there is piece blocking
            ChessPosition nextPosition = new ChessPosition(row + direction, col);

            if (board.getPiece(nextPosition) == null) {
                // add move
                if (endOfBoard(nextPosition.getRow())) {
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.QUEEN));
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.KNIGHT));
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.ROOK));
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, ChessPiece.PieceType.BISHOP));
                    return true;
                }
                else {
                    pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
                    return true;
                }
            }
        }
        return false;
    }

    // piece at end of board (on the scale 1 - 8)
    public boolean endOfBoard (int row) {
        if (row == 1 || row == 8) {
            return true;
        }
        return false;
    }

    // check to see if a position is in the bounds
    public boolean inBounds (int row, int col) {
        if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
            return true;
        }
        return false;
    }
}
