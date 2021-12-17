package picross;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import static picross.Game.*;
import static picross.GameView.*;

/**
 * @author Neil Kingdom
 * @version 1.1
 * @since 2021-09-15
 *
 * GameController is responsible for handling action events and any game logic
 * which includes the menu options.
 */
public class GameController {

    /* ---------------------- Constants -------------------- */

    private static final String BUTTON_RESET_MESSAGE = "Resetting game...";
    private static final String MB_CHECKED_MESSAGE = "Switching to Mark Mode";
    private static final String MB_UNCHECKED_MESSAGE = "Switching to Normal Mode";
    private static final String CORRECT_TILE = "+1 Point";
    private static final String INCORRECT_TILE = "-1 Point";

    protected enum Functions {
        SCALE_BOARD, SHOW_SOLUTION, EXIT_GAME, CHOOSE_COLOR, ABOUT
    }

    /* ---------------------- Member Variables -------------------- */

    private static GameModel gameModel;
    private static GameView gameView;

    private static boolean perfectScore = true;
    private static int tilesSelected = 0;

    /* ---------------------- Initialization Methods -------------------- */

    public GameController() {
    }

    /**
     * @param gameModel Instance of GameModel class
     * @param gameView  Instance of GameView class
     * @since 2021-09-15
     *
     * Parameterized constructor. Takes in a GameModel object and a
     * GameView object in order to access public methods from each class.
     */
    public GameController(GameModel gameModel, GameView gameView) {
        this.gameModel = gameModel;
        this.gameView = gameView;
        this.gameView.splash();
    }

    /**
     * @since 2021-09-15
     *
     * Essentially starts the actual game by calling pack() and
     * setVisible() of GameView's inherited JFrame
     */
    public void initBoard() {
        gameView.getJFrame().pack();
        gameView.getJFrame().setLocation(X_START_POS - gameView.getJFrame().getWidth() / 2, Y_START_POS - gameView.getJFrame().getHeight() / 2);
        gameView.getJFrame().setResizable(false);
        gameView.getJFrame().setVisible(true);
    }

    /**
     * @since 2021-09-15
     *
     * Begins a timer that repeats every second. The timer will
     * update the timer field in the game, as well as refresh the
     * screen.
     */
    public void setTimer() {
        Timer timer = new Timer(1000, e -> {
            short mins = gameView.getTime()[0];
            short seconds = gameView.getTime()[1];

            if (seconds == 59)
                mins = (short) ((++mins) % 60);
            seconds = (short) ((++seconds) % 60);
            gameView.setTime(new short[]{mins, seconds});

            String timeField = "";
            StringBuilder sb = new StringBuilder(timeField);
            if (mins < 10) sb.append("0");
            sb.append(mins + ":");
            if (seconds < 10) sb.append("0");
            sb.append(seconds);
            timeField = sb.toString();

            gameView.getTimeField().setText(timeField);
        });
        timer.setInitialDelay(1000);
        timer.start();
        gameView.refresh();
    }

    /* ---------------------- Menu Methods -------------------- */

    /**
     * @param newDimension The new dimension of the board.
     * @param genNewConfig Boolean that decides if a random
     *                     layout should be generated upon rescaling.
     * @since 2021-11-12
     *
     * The name of this function is slightly decieving. It is used
     * for rescaling the board to newDimension, however, it is also
     * used for resetting the game by passing in the same dimension
     * and putting genNewConfig as false.
     */
    public void rescaleBoard(int newDimension, boolean genNewConfig) {

        tilesSelected = 0;
        Game.setDimension(newDimension);
        gameView.setTileDimension(MIN_WIN_HEIGHT / dimension);

        //Reset text components
        gameView.getRightTextPane().setText("");
        gameView.setPoints(0);
        gameView.getPointsField().setText("00000");
        gameView.setTime(new short[]{0, 0});
        gameView.getTimeField().setText("00:00");

        if (genNewConfig) {
            gameView.getTopHintRow().clear();
            gameView.getLeftHintCol().clear();
            gameModel.genRandBitStream();
        }

        //Resize Icons
        gameView.xTileIcon = gameView.resizeImageIcon(new Dimension(gameView.getTileDimension(), gameView.getTileDimension()), bufXTile);
        gameView.tileSelectedIcon = gameView.resizeImageIcon(new Dimension(gameView.getTileDimension(), gameView.getTileDimension()), bufSelected);

        //Clear existing components
        gameView.getTopPanel().removeAll();
        gameView.getLeftPanel().removeAll();
        gameView.getCenterPanel().removeAll();
        gameView.getCenterPanel().setLayout(new GridLayout(dimension, dimension, PADDING, PADDING));

        //Reset ArrayLists
        gameView.getBoardMatrix().clear();
        gameView.getTopTextPaneArr().clear();
        gameView.getLeftTextPaneArr().clear();

        gameView.addLeftHintCol();
        gameView.addTopHintRow();
        gameView.addCenterTiles();

        gameView.refresh();
    }

    /**
     * @since 2021-11-12
     *
     * Displays the solution for the current board.
     */
    public void showSolution() {

        outputEvent("Showing solution...\n", PICROSS_YELLOW);
        String bitMatrix = "";
        StringBuilder sb = new StringBuilder(bitMatrix);

        for (int y = 0; y < dimension; y++) {
            for (int x = 0; x < dimension; x++) {
                JLabel tile = gameView.getBoardMatrix().get((y * dimension) + x);
                int bit = (gameModel.getBoard()[y][x]) ? 1 : 0;
                sb.append(bit);

                if (gameModel.getBoard()[y][x])
                    tile.setBackground(PICROSS_GREEN);
                else
                    tile.setBackground(PICROSS_YELLOW);

                tile.setIcon(null);
                tile.setBorder(BorderFactory.createRaisedBevelBorder());
                //Assumes that only one action listener exists per tile
                if (tile.getMouseListeners().length > 0)
                    tile.removeMouseListener(tile.getMouseListeners()[0]);
            }
            sb.append("\n");
        }

        outputEvent(sb.toString(), PICROSS_GREEN);
    }

    /**
     * @since 2021-11-12
     *
     * Prompts the user with a menu asking if they're sure
     * they want to quit. If so, the game is closed, otherwise,
     * it is resumed.
     */
    public void exitGame() {

        JOptionPane confirmExit = new JOptionPane();
        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(confirmExit, "Are you sure you want to exit?", "Exit Game",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

        if (n == 0) {
            gameView.getJFrame().dispose();
            gameView.setJFrame(null);
        }
    }

    /**
     * @since 2021-11-12
     *
     * Prompts the user with a color swatch for picking
     * a new color palette from the default.
     */
    public void colorChooser() {

        JFrame paletteBar = new JFrame("Color Palette Selector");
        paletteBar.setLayout(new BorderLayout());
        JPanel paletteBarPanel = new JPanel(new GridLayout(2, 3));
        JPanel colorSwatchPanel = new JPanel();

        JColorChooser colorSwatch = new JColorChooser(PICROSS_GREEN);
        JPanel color1 = new JPanel(), color2 = new JPanel(), color3 = new JPanel();
        JButton button1 = new JButton("Color 1: Correct"), button2 = new JButton("Color 2: Marked"), button3 = new JButton("Color 3: Error");
        Dimension sharedDimension = new Dimension(100, 30);

        Color prevPicrossGreen = PICROSS_GREEN;
        Color prevPicrossYellow = PICROSS_YELLOW;
        Color prevPicrossRed = PICROSS_RED;

        color1.setBackground(PICROSS_GREEN);
        color1.setPreferredSize(sharedDimension);
        color2.setBackground(PICROSS_YELLOW);
        color2.setPreferredSize(sharedDimension);
        color3.setBackground(PICROSS_RED);
        color3.setPreferredSize(sharedDimension);

        button1.setPreferredSize(sharedDimension);
        button1.addActionListener(e -> {
            color1.setBackground(colorSwatch.getColor());
            PICROSS_GREEN = colorSwatch.getColor();
        });

        button2.setPreferredSize(sharedDimension);
        button2.addActionListener(e -> {
            color2.setBackground(colorSwatch.getColor());
            PICROSS_YELLOW = colorSwatch.getColor();
        });

        button3.setPreferredSize(sharedDimension);
        button3.addActionListener(e -> {
            color3.setBackground(colorSwatch.getColor());
            PICROSS_RED = colorSwatch.getColor();
        });

        paletteBarPanel.add(color1);
        paletteBarPanel.add(color2);
        paletteBarPanel.add(color3);
        paletteBarPanel.add(button1);
        paletteBarPanel.add(button2);
        paletteBarPanel.add(button3);
        colorSwatchPanel.add(colorSwatch);

        paletteBar.add(colorSwatchPanel, BorderLayout.CENTER);
        paletteBar.add(paletteBarPanel, BorderLayout.SOUTH);

        //Action listener that will update board on close
        paletteBar.addWindowListener(new WindowListener() {
            @Override
            public void windowClosed(WindowEvent e) {
                for (JLabel tile : gameView.getBoardMatrix()) {
                    if (tile.getBackground() == prevPicrossGreen) {
                        tile.setBackground(PICROSS_GREEN);
                    }
                    if (tile.getBackground() == prevPicrossYellow) {
                        tile.setBackground(PICROSS_YELLOW);
                    }
                    if (tile.getBackground() == prevPicrossRed) {
                        tile.setBackground(PICROSS_RED);
                    }
                }
            }

            @Override
            public void windowOpened(WindowEvent e) {
            }
            @Override
            public void windowClosing(WindowEvent e) {
            }
            @Override
            public void windowIconified(WindowEvent e) {
            }
            @Override
            public void windowDeiconified(WindowEvent e) {
            }
            @Override
            public void windowActivated(WindowEvent e) {
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        paletteBar.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        paletteBar.pack();
        paletteBar.setResizable(false);
        paletteBar.setVisible(true);
    }

    /**
     * @since 2021-11-13
     *
     * Prompts the user with an about menu with basic
     * information about the game.
     */
    public void about() {

        String about = "Picross\n" +
                "Version: 1.1\n" +
                "Author: Neil Kingdom";
        JOptionPane.showMessageDialog(gameView.getJFrame(), about);
    }

    /* ---------------------- Action Listeners -------------------- */

    /**
     * @param comp JButton to add action listener to.
     * @since 2021-10-09
     *
     * Adds action listener to a button component
     */
    public void addButtonListener(JButton comp) {

        comp.addActionListener(al -> {
            outputEvent(BUTTON_RESET_MESSAGE, PICROSS_YELLOW);
            rescaleBoard(dimension, false);
        });
    }

    /**
     * @param comp JCheckBox to add action listener to.
     * @since 2021-10-09
     *
     * Adds action listener to a JCheckBox component
     */
    public void addCheckBoxListener(JCheckBox comp) {

        comp.addActionListener(al -> {
            String eventMessage;

            if (gameView.getMarkBoxState()) {
                eventMessage = MB_UNCHECKED_MESSAGE;
                gameView.setMarkBoxState(false);
            } else {
                eventMessage = MB_CHECKED_MESSAGE;
                gameView.setMarkBoxState(true);
            }
            outputEvent(eventMessage, PICROSS_YELLOW);
        });
    }

    /**
     * @param menuItem     JMenuItem to add action listener to.
     * @param funcEnum     An enum which determines which menu function to call
     * @param newDimension Only used for the scale board enum. New dimension
     *                     to scale the board to.
     * @since 2021-10-09
     *
     * Adds action listener to a JMenuItem component
     */
    public void addMenuItemListener(JMenuItem menuItem, Functions funcEnum, int newDimension) {

        switch (funcEnum) {
            case SCALE_BOARD -> menuItem.addActionListener(e -> rescaleBoard(newDimension, true));
            case SHOW_SOLUTION -> menuItem.addActionListener(e -> showSolution());
            case EXIT_GAME -> menuItem.addActionListener(e -> exitGame());
            case CHOOSE_COLOR -> menuItem.addActionListener(e -> colorChooser());
            case ABOUT -> menuItem.addActionListener(e -> about());
        }
    }

    /**
     * @param splashJF The JFrame used for the splash screen to add action listener to
     * @param st       SplashThread object which handles splash screen animations
     * @since 2021-10-09
     *
     * Adds action listener to a JFrame
     */
    public void addJFrameListener(JFrame splashJF, SplashThread st) {

        splashJF.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                splashJF.dispose();
                st.interrupt(); //End animations thread
                gameView.initGame();
                initBoard();
                setTimer();
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    /**
     * @param comp JLabel to add action listener to.
     * @since 2021-10-09
     *
     * Adds action listener to a JLabel component
     */
    public void addJLabelListener(JLabel comp) {

        comp.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                int points = gameView.getPoints();
                int x = gameView.getBoardMatrix().indexOf(comp) % dimension;
                int y = (int) Math.floor(gameView.getBoardMatrix().indexOf(comp) / dimension);
                JLabel tile = gameView.getBoardMatrix().get(x + (y * dimension));

                //If tile was correct
                if (gameModel.getBoard()[y][x]) {
                    //If markBox was unchecked then correct
                    if (!gameView.getMarkBoxState()) {
                        gameView.setPoints(points + 1);
                        outputEvent(CORRECT_TILE, PICROSS_GREEN);
                        tile.setIcon(null);
                        tile.setBackground(PICROSS_GREEN);
                    } else {
                        //If markBox was checked then incorrect
                        perfectScore = false;
                        gameView.setPoints(points - 1);
                        outputEvent(INCORRECT_TILE, PICROSS_RED);
                        tile.setIcon(gameView.xTileIcon);
                        tile.setBackground(PICROSS_RED);
                    }
                } else {
                    //If tile was incorrect but markBox was checked
                    if (gameView.getMarkBoxState()) {
                        gameView.setPoints(points + 1);
                        outputEvent(CORRECT_TILE, PICROSS_GREEN);
                        tile.setIcon(null);
                        tile.setBackground(PICROSS_YELLOW);
                    }
                    //Otherwise, tile must have been incorrect
                    else {
                        perfectScore = false;
                        gameView.setPoints(points - 1);
                        outputEvent(INCORRECT_TILE, PICROSS_RED);
                        tile.setIcon(gameView.xTileIcon);
                        tile.setBackground(PICROSS_RED);
                    }
                }
                tile.removeMouseListener(this);

                //Update pointsField
                points = gameView.getPoints();
                String pointsField = String.valueOf(points);
                int len = pointsField.length();
                StringBuilder sb = new StringBuilder(pointsField);

                for (int i = 1; i < (5 + 1 - len); i++)
                    sb.insert(0, '0');
                pointsField = sb.toString();
                gameView.getPointsField().setText(pointsField);

                //Check if game has ended
                tilesSelected++;
                if (tilesSelected == dimension * dimension)
                    gameView.endSplash(perfectScore);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                comp.setIcon(tileSelectedIcon);
                gameView.refresh();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                comp.setIcon(null);
                gameView.refresh();
            }
        });
    }

    /**
     * @param text      Text to be appended to rightTextPane
     * @param textColor The color of the text that will be output.
     * @since 2021-10-09
     *
     * This method is used for logging events that occur within the game.
     * Most action listeners invoke this method with event messages.
     */
    public void outputEvent(String text, Color textColor) {

        JTextPane rightTextArea = gameView.getRightTextPane();
        StyledDocument document = (StyledDocument) rightTextArea.getDocument();
        Style style = rightTextArea.addStyle("", null);

        try {
            StyleConstants.setForeground(style, textColor);
            document.insertString(document.getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        // Update caret position to bottom of textarea
        rightTextArea.setCaretPosition(rightTextArea.getDocument().getLength());
    }
}
