package ui;

import server.websocket.NotificationHandler;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {

    private final ChessClient chessClient; // Single client manages all logic

    public Repl(String serverUrl) {
        this.chessClient = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to chess. Sign in to start.");
        Scanner scanner = new Scanner(System.in);

        while (chessClient.getState() != State.QUIT) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                String result = chessClient.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                String message = e.toString();
                System.out.print(message);
            }
        }
        scanner.close();
    }

    private void printPrompt() {
        String prompt = switch (chessClient.getState()) {
            case LOGGEDOUT -> SET_TEXT_COLOR_BLUE + "[logged out] > " + RESET_TEXT_COLOR;
            case LOGGEDIN -> SET_TEXT_COLOR_RED + "[logged in] > " + RESET_TEXT_COLOR; // Could add username if ChessClient exposes it
            case INCHESSGAME -> SET_TEXT_COLOR_YELLOW  + "[in game] > " + RESET_TEXT_COLOR;
            case QUIT -> "";
        };
        System.out.print(prompt);
    }
}
