package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnBehavior implements PieceBehavior {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pawnMoves = new ArrayList<>();
        ChessGame.TeamColor color = board.getPiece(myPosition).getTeamColor();
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;

        if (myPosition.getRow() == startRow) {
            moveTwo(board, myPosition, direction, pawnMoves);
        } else {
            moveOne(board, myPosition, direction, pawnMoves);
        }

        addDiagonalCaptures(board, myPosition, direction, pawnMoves, color);
        return pawnMoves;
    }

    private void moveOne(ChessBoard board, ChessPosition myPosition, int direction, Collection<ChessMove> pawnMoves) {
        addMoveIfValid(board, myPosition, myPosition.getRow() + direction, myPosition.getColumn(), pawnMoves);
    }

    private void moveTwo(ChessBoard board, ChessPosition myPosition, int direction, Collection<ChessMove> pawnMoves) {
        int row = myPosition.getRow() + direction;
        if (addMoveIfValid(board, myPosition, row, myPosition.getColumn(), pawnMoves)) {
            addMoveIfValid(board, myPosition, row + direction, myPosition.getColumn(), pawnMoves);
        }
    }

    private void addDiagonalCaptures(ChessBoard board, ChessPosition myPosition, int direction, Collection<ChessMove> pawnMoves, ChessGame.TeamColor color) {
        int[][] diagonals = {{direction, 1}, {direction, -1}};
        for (int[] diagonal : diagonals) {
            int row = myPosition.getRow() + diagonal[0];
            int col = myPosition.getColumn() + diagonal[1];
            ChessPosition nextPosition = new ChessPosition(row, col);
            if (inBounds(row, col)) {
                ChessPiece piece = board.getPiece(nextPosition);
                if (piece != null && piece.getTeamColor() != color) {
                    addPawnMove(myPosition, nextPosition, pawnMoves);
                }
            }
        }
    }

    private boolean addMoveIfValid(ChessBoard board, ChessPosition myPosition, int row, int col, Collection<ChessMove> pawnMoves) {
        if (!inBounds(row, col)) return false;
        ChessPosition nextPosition = new ChessPosition(row, col);
        if (board.getPiece(nextPosition) == null) {
            addPawnMove(myPosition, nextPosition, pawnMoves);
            return true;
        }
        return false;
    }

    private void addPawnMove(ChessPosition start, ChessPosition end, Collection<ChessMove> moves) {
        if (endOfBoard(end.getRow())) {
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
            moves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
        } else {
            moves.add(new ChessMove(start, end, null));
        }
    }

    private boolean endOfBoard(int row) {
        return row == 8 || row == 1;
    }

    private boolean inBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}

