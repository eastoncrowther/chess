package chess;

import java.util.Collection;

public interface PieceBehavior {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}
