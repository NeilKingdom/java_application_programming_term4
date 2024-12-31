package picross;

import java.awt.*;
import java.io.IOException;

/**
 * @author Neil Kingdom
 * @since 2021-09-15
 * @version 1.0
 *
 * The Picross class just instantiates GameSplash, followed by GameController.
 * An additional argument can be passed to change the w/h of the tiles
 * */

public class Picross {

    public static void main(String[] args) throws IOException, FontFormatException {

        // Default puzzle size if no argument is provided
        int matrixSize = 5;

        if(args.length > 1) {
            System.err.println("Error: Too many arguments defined.");
            System.err.println("Usage: <program> [puzzle matrix size]");
            System.exit(1);
        }

        if(args.length > 0) {
            final String arg1 = args[0];
            try {
                matrixSize = Integer.parseInt(arg1);
            } catch (NumberFormatException e) {
                System.err.println("Error: argument \"" + arg1 + "\" is not a valid dimension");
                System.err.println("Usage: <program> [puzzle matrix size]");
                System.exit(1);
            }
        }

        System.out.println(args.length);
        new GameSplash(5);
        GameController main_game = new GameController(matrixSize);

        main_game.pack();
        main_game.setResizable(false);
        main_game.setVisible(true);
    }
}
