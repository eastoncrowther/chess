package ui;

import java.util.Scanner;

public class Repl {
    private final PreLoginClient preLoginClient;
    private State state;

    public Repl(String serverUrl, State state) {
        preLoginClient = new PreLoginClient(serverUrl, state);
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

                }
                else if (state.equals(State.LOGGEDIN)) {

                }
                else {
                    result = preLoginClient.eval(line);
                    System.out.print(result);

                    // update the state of the program
                    state = preLoginClient.getState();
                }

            } catch (Throwable e) {
                String message = e.toString();
                System.out.print(message);
            }
            System.out.println();
        }
    }
}
