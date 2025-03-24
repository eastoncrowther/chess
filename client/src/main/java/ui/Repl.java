package ui;

import java.util.Scanner;

public class Repl {
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;
    private State state;

    public Repl(String serverUrl, State state) {
        preLoginClient = new PreLoginClient(serverUrl, state);
        postLoginClient = new PostLoginClient(serverUrl, state);
        this.state = state;
    }

    public void run() {
        // things to print on startup
        System.out.println("Welcome to chess. Sign in to start.");
        System.out.println(preLoginClient.help());

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            String line = scanner.nextLine();
            try {
                if (state.equals(State.INCHESSGAME)) {
                    System.out.println("IN CHESSGAME REPL");
                }
                else if (state.equals(State.LOGGEDIN)) {
                    result = postLoginClient.eval(line);

                }
                else {
                    result = preLoginClient.eval(line);
                }
                System.out.print(result);
                state = preLoginClient.getState();

            } catch (Throwable e) {
                String message = e.toString();
                System.out.print(message);
            }
            System.out.println();
        }
    }
}
