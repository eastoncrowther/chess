package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ui.EscapeSequences.*;

public class PrintBoard {
    private ChessBoard chessBoard;
    private ChessPosition highlightStart = null;
    private Set<ChessPosition> highlightEnds = null;


    public PrintBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void setHighlights(ChessPosition start, Collection<ChessMove> validMoves) {
        this.highlightStart = start;
        this.highlightEnds = new HashSet<>();
        if (validMoves != null) {
            for (ChessMove move : validMoves) {
                this.highlightEnds.add(move.getEndPosition());
            }
        }
    }

    public void clearHighlights() {
        this.highlightStart = null;
        this.highlightEnds = null;
    }


    public String printWhiteBoard() {
        return printBoard(ChessGame.TeamColor.WHITE);
    }

    public String printBlackBoard() {
        return printBoard(ChessGame.TeamColor.BLACK);
    }

    private String printBoard(ChessGame.TeamColor perspective) {
        StringBuilder boardString = new StringBuilder();
        boardString.append(ERASE_SCREEN);
        boardString.append(SET_TEXT_COLOR_WHITE);
        boardString.append(EMPTY);
        if (perspective == ChessGame.TeamColor.WHITE) {
            for (char c = 'a'; c <= 'h'; c++) {
                boardString.append(" ").append(c).append("\u2003");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                boardString.append(" ").append(c).append("\u2003");
            }
        }
        boardString.append(EMPTY);
        boardString.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");


        int startRow = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int rowIncr = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        int startCol = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int endCol = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int colIncr = (perspective == ChessGame.TeamColor.WHITE) ? 1 : -1;

        for (int row = startRow; (rowIncr > 0) ? row <= endRow : row >= endRow; row += rowIncr) {
            boardString.append(SET_TEXT_COLOR_WHITE);
            boardString.append(" ").append(row).append(" ");
            boardString.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);

            for (int col = startCol; (colIncr > 0) ? col <= endCol : col >= endCol; col += colIncr) {
                placePiece(row, col, boardString);
            }

            boardString.append(SET_TEXT_COLOR_WHITE);
            boardString.append(" ").append(row).append(" ");
            boardString.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");

        }

        boardString.append(SET_TEXT_COLOR_WHITE);
        boardString.append(EMPTY);
        if (perspective == ChessGame.TeamColor.WHITE) {
            for (char c = 'a'; c <= 'h'; c++) {
                boardString.append(" ").append(c).append("\u2003");
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                boardString.append(" ").append(c).append("\u2003");
            }
        }
        boardString.append(EMPTY);
        boardString.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");


        return boardString.toString();
    }

    private void placePiece(int row, int col, StringBuilder boardString) {
        ChessPosition currentPosition = new ChessPosition(row, col);
        ChessPiece piece = (chessBoard != null) ? chessBoard.getPiece(currentPosition) : null;

        String bgColor = getCellColor(row, col, currentPosition);

        boardString.append(bgColor);
        boardString.append(getPieceRepresentation(piece));
        boardString.append(RESET_BG_COLOR);
    }

    private String getPieceRepresentation(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;

        return textColor + switch (piece.getPieceType()) {
            case KING -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KING : BLACK_KING;
            case QUEEN -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_QUEEN : BLACK_QUEEN;
            case BISHOP -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_ROOK : BLACK_ROOK;
            case PAWN -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PAWN : BLACK_PAWN;
        } + RESET_TEXT_COLOR;
    }


    private String getCellColor(int row, int col, ChessPosition currentPosition) {

        if (highlightStart != null && highlightStart.equals(currentPosition)) {
            return SET_BG_COLOR_YELLOW;
        } else if (highlightEnds != null && highlightEnds.contains(currentPosition)) {
            ChessPiece targetPiece = (chessBoard != null) ? chessBoard.getPiece(currentPosition) : null;
            if (targetPiece == null) {
                return SET_BG_COLOR_GREEN;
            } else {
                return SET_BG_COLOR_DARK_GREEN;
            }
        }
        boolean isLightSquare = (row + col) % 2 != 0;
        return isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;

    }

    void setChessBoard(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }
}



