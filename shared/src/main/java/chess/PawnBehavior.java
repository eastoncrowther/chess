package chess;

import jdk.jshell.spi.ExecutionControl;

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
        moveOneSpace(1, board, myPosition, pawnMoves);

    }

    // black pawns start at the top and go down
    public void blackPawnMoves (ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        moveOneSpace(-1, board, myPosition, pawnMoves);

    }

    // direction should be int -1 for black and +1 for white
    public void moveOneSpace (int direction, ChessBoard board, ChessPosition myPosition, Collection<ChessMove> pawnMoves) {
        // on the scale 0 - 8
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        // check the space 1 up from the current position
        if (inBounds(row - 1 + direction, col - 1)) {
            // check if there is piece blocking
            ChessPosition nextPosition = new ChessPosition(row + direction, col);
            if (pieceOnPosition(board, nextPosition) == null) {
                // add move
                pawnMoves.add(new ChessMove(myPosition, nextPosition, null));
            }
        }
    }

    // returns the team color of the piece on a given position, if no piece exists returns null
    public ChessGame.TeamColor pieceOnPosition(ChessBoard board, ChessPosition position) {
        ChessPiece pieceOnPosition = board.getPiece(position);
        if (pieceOnPosition == null) {
            return null;
        }
        return pieceOnPosition.getTeamColor();
    }

    // check to see if a position is in the bounds -> indicies should be 0 - 7
    public boolean inBounds (int row, int col) {
        if (row >= 0 && row <= 7 && col >= 0 && col <= 7) {
            return true;
        }
        return false;
    }
}
