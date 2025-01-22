package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookBehavior implements PieceBehavior{

    @Override
    public Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> rookMoves = new ArrayList<>();

        int[][] steps = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };
        // get team color
        ChessGame.TeamColor myTeamColor = board.getPiece(myPosition).getTeamColor();

        // positions span 1 - 8 while the board is indexed 0 - 7

        for (int[] step : steps) {
            int incrementRow = step[0];
            int incrementCol = step[1];

            // get initial position
            int row = myPosition.getRow() - 1;
            int col = myPosition.getColumn()  - 1;

            while (true) {
                row += incrementRow;
                col += incrementCol;

                // check if position is in bounds
                if (row < 0 || col < 0 || row > 7 || col > 7) {
                    break;
                }

                ChessPosition nextPosition = new ChessPosition(row + 1, col + 1);

                // check if there is a piece at the new position.
                if (board.getPiece(nextPosition) == null) {
                    // there is no piece on the position
                    rookMoves.add(new ChessMove(myPosition, nextPosition, null));
                }
                else {
                    // if there is an enemy piece on the position
                    if (board.getPiece(nextPosition).getTeamColor() != myTeamColor) {
                        rookMoves.add(new ChessMove(myPosition, nextPosition, null));
                    }
                    // the piece at the given position is on my team
                    break;
                }
            }
        }
        return rookMoves;
    }
}
