package chess;

import java.util.ArrayList;
import java.util.Collection;


public class QueenBehavior implements PieceBehavior {

    @Override
        public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> queenMoves = new ArrayList<>();

        int[][] moves = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
        };

        for (int[] move : moves) {
            int row = myPosition.getRow() + move[0];
            int col = myPosition.getColumn() + move[1];

            while (inBounds(row, col)) {
                ChessPosition nextPosition = new ChessPosition(row, col);
                if (board.getPiece(nextPosition) != null) {
                    if (board.getPiece(nextPosition).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                        // add the move then break
                        queenMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    break;
                }
                else {
                    // add the move
                    queenMoves.add(new ChessMove(myPosition, nextPosition, null));
                    row += move[0];
                    col += move[1];
                }
            }
        }
        return queenMoves;
    }
    public boolean inBounds (int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }
        return true;
    }
}
