package picross;

import picross.GameServer;
import picross.GameClient;
import java.awt.*;

/**
 * @author Neil Kingdom
 * @version 1.1
 * @since 2021-09-15
 *
 * Game contains the main method, as well as global constants
 * for the game. It creates a new GameController object, passing references
 * to GameModel and GameView.
 */
public class Game {

    /* ---------------------- Constants -------------------- */

    public static final int MIN_WIN_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width / 2);
    public static final int MIN_WIN_HEIGHT = (Toolkit.getDefaultToolkit().getScreenSize().height / 2);
    public static final int X_START_POS = ((Toolkit.getDefaultToolkit().getScreenSize().width / 2));
    public static final int Y_START_POS = ((Toolkit.getDefaultToolkit().getScreenSize().height / 2));

    /* ---------------------- Member Variables -------------------- */

    protected static int dimension = 5;

    public static void main(String[] args) {

        if(args.length >= 1) {
            //Create server instance
            if(args[0].toLowerCase().equals("s")) {
                GameServer.main(args);
            }
            //Create client instance
            else if(args[0].toLowerCase().equals("c")) {
                GameClient.main(args);
            }
            else {
                System.err.println("Unrecognized option - " + args[0]);
            }
        }
        //Regular MVC implementation
        else {
            GameModel gameModel = new GameModel();
            GameView gameView = new GameView();
            new GameController(gameModel, gameView);
        }
    }

    /* ---------------------- Setters -------------------- */

    public static void setDimension(int newDimension) {
        dimension = newDimension;
    }
}
