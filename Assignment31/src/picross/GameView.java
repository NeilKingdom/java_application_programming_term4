package picross;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import static picross.Game.*;

/**
 * @author Neil Kingdom
 * @version 1.1
 * @since 2021-09-15
 *
 * GameView handles any visual components of the Picross implementation.
 * It extends JFrame so that we can add components to it.
 */
public class GameView extends JFrame {

    /* ---------------------- Constants -------------------- */

    protected static final int PADDING = 3;
    private static final Insets INSETS = new Insets(1, 1, 1, 1);
    private static final boolean DEBUG = false;
    private static final GameController gameController = new GameController();

    //Background colors
    private static final Color BKGRD = new Color(55, 61, 103);
    private static final Color FRGRD = new Color(35, 160, 255);

    /* ---------------------- Member Variables -------------------- */

    //Default color scheme
    protected static Color PICROSS_RED = new Color(229, 47, 74);
    protected static Color PICROSS_YELLOW = new Color(250, 163, 49);
    protected static Color PICROSS_GREEN = new Color(26, 173, 117);

    //JComponents
    private static JPanel markPanel, topPanel, leftPanel, centerPanel, rightPanel;
    private static JTextPane rightTextPane;
    private static JTextField pointsField;
    private static JTextField timeField;
    private static JCheckBox markBox;
    private static JLabel bannerLogo;
    private static JLabel gameTextLabel;
    private static JLabel splashBackground;
    private static ArrayList<JLabel> boardMatrix;
    private static ArrayList<JTextPane> topTextPaneArr;
    private static ArrayList<JTextPane> leftTextPaneArr;
    private static ArrayList<ArrayList<Integer>> topHintRow;
    private static ArrayList<ArrayList<Integer>> leftHintCol;

    //Variables
    private static int tileDimension;
    private static int points;
    private static short mins;
    private static short seconds;
    private static boolean markBoxState;
    private static int endSplashWidth;
    private static int endSplashHeight;

    //Resources
    protected static BufferedImage bufGameOverSplash;
    protected static BufferedImage bufWinnerSplash;
    protected static BufferedImage bufSelected;
    protected static BufferedImage bufBanner;
    protected static BufferedImage bufXTile;
    protected static BufferedImage bufSplash;
    protected static BufferedImage bufStartGameText;

    protected static ImageIcon gameOverIcon;
    protected static ImageIcon winnerIcon;
    protected static ImageIcon tileSelectedIcon;
    protected static ImageIcon bannerIcon;
    protected static ImageIcon xTileIcon;
    protected static ImageIcon splashIcon;
    protected static ImageIcon startGameTextIcon;

    protected static Font mcRegular;
    protected static Font openSans;

    /**
     * @since 2021-10-09
     *
     * Parameterized constructor for GameView.
     */
    public GameView() {
        super("Picross - Neil Kingdom");
        tileDimension = MIN_WIN_HEIGHT / dimension;
        points = 0;
        mins = 0;
        seconds = 0;
        markBoxState = false;
        endSplashWidth = 300;
        endSplashHeight = 125;
    }

    /**
     * @since 2021-10-09
     *
     * Simply calls revalidate and repaint on the calling JFrame.
     */
    public void refresh() {
        revalidate();
        repaint();
    }

    /**
     * @since 2021-10-09
     * @param newSize New dimension for the JLabel to be scaled to
     * @param bufImg  The buffered image of the JLabel prior to being resized
     *
     * This method is in charge of resizing an ImageIcon. Used for animations
     * on the splash screen as well as a few instances where I reuse Icons.
     */
    public ImageIcon resizeImageIcon(Dimension newSize, BufferedImage bufImg) {
        return (new ImageIcon(bufImg.getScaledInstance(newSize.width, newSize.height, Image.SCALE_SMOOTH)));
    }

    /**
     * @since 2021-10-09
     * @param comp JLabel to be moved
     * @param dest A 2-Dimensional vector which represents the new location of the JLabel
     *
     * This method sets the new positions of the background of the splash screen.
     */
    public void moveBkgrndFromOrigin(JLabel comp, Vec2D dest) {
        comp.setLocation((int) ((comp.getX() + dest.getI() % MIN_WIN_WIDTH)), (int) ((comp.getY() + dest.getJ()) % MIN_WIN_HEIGHT));
        refresh();
    }

    /**
     * @since 2021-10-09
     *
     * initGame is responsible for creating all JComponents that
     * will be added to the JFrame when the game is launched.
     */
    public void initGame() {

        Container mainPanel = getContentPane();

        /* ---------------------- JFrame Setup -------------------- */

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(false);
        setForeground(Color.WHITE);
        setMinimumSize(new Dimension(MIN_WIN_WIDTH, MIN_WIN_HEIGHT));
        mainPanel.setBackground(BKGRD);
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc= new GridBagConstraints();
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;

        /* ---------------------- Resource Config -------------------- */

        int menuIconDimension = 50;

        BufferedImage bufNewIcon;
        BufferedImage bufExitIcon;
        BufferedImage bufSolutionIcon;
        BufferedImage bufColorIcon;
        BufferedImage bufAboutIcon;

        ImageIcon menuNewIcon = null;
        ImageIcon menuExitIcon = null;
        ImageIcon menuSolutionIcon = null;
        ImageIcon menuColorIcon = null;
        ImageIcon menuAboutIcon = null;


        //Menu Icons
        try {
            bufNewIcon = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossNew.gif"));
            menuNewIcon = new ImageIcon(bufNewIcon.getScaledInstance(menuIconDimension, menuIconDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossNew.gif\"");
        }

        try {
            bufExitIcon = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossExit.gif"));
            menuExitIcon = new ImageIcon(bufExitIcon.getScaledInstance(menuIconDimension, menuIconDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossExit.gif\"");
        }

        try {
            bufSolutionIcon = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossSolution.gif"));
            menuSolutionIcon = new ImageIcon(bufSolutionIcon.getScaledInstance(menuIconDimension, menuIconDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossSolution.gif\"");
        }

        try {
            bufColorIcon = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossColor.gif"));
            menuColorIcon = new ImageIcon(bufColorIcon.getScaledInstance(menuIconDimension, menuIconDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossColor.gif\"");
        }

        try {
            bufAboutIcon = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossAbout.gif"));
            menuAboutIcon = new ImageIcon(bufAboutIcon.getScaledInstance(menuIconDimension, menuIconDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossAbout.gif\"");
        }

        //Other Icons
        try {
            bufSelected = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossSelected.png"));
            tileSelectedIcon = new ImageIcon(bufSelected.getScaledInstance(tileDimension, tileDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/ res / img / PicrossSelected.png\"");
        }

        try {
            bufXTile = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossXTile.png"));
            xTileIcon = new ImageIcon(bufXTile.getScaledInstance(tileDimension, tileDimension, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossXTile.png\"");
        }

        try {
            bufWinnerSplash = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossWinnerSplash.jpg"));
            winnerIcon = new ImageIcon(bufWinnerSplash.getScaledInstance(endSplashWidth, endSplashHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load image: \"/res/img/PicrossWinnerSplash.jpg\"");
        }

        try {
            bufGameOverSplash = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossGameOver.jpg"));
            gameOverIcon = new ImageIcon(bufGameOverSplash.getScaledInstance(endSplashWidth, endSplashHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load image: \"/res/img/PicrossPicrossGameOver.jpg\"");
        }

        //Fonts
        try {
            mcRegular = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/res/fonts/MinecraftRegular.otf")).deriveFont(Font.PLAIN, 32);
            setFont(mcRegular);
        } catch (IOException | FontFormatException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load font: \"/res/fonts/MinecraftRegular.otf\"");
        }

        try {
            openSans = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/res/fonts/OpenSans-Regular.ttf")).deriveFont(Font.PLAIN, 24);
            setFont(mcRegular);
        } catch (IOException | FontFormatException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load font: \"/res/fonts/OpenSans-Regular.ttf\"");
        }

        /* ---------------------- Init JPanels -------------------- */

        markPanel = new JPanel();
        topPanel = new JPanel();
        leftPanel = new JPanel();
        centerPanel = new JPanel();
        rightPanel = new JPanel();

        /* ---------------------- Define Layouts -------------------- */

        GroupLayout grRight = new GroupLayout(rightPanel);
        GridLayout glCenter = new GridLayout(dimension, dimension, PADDING, PADDING);
        GridBagLayout grLeft = new GridBagLayout();
        GridBagLayout grTop = new GridBagLayout();

        /* ---------------------- Menu -------------------- */

        //Initialize components
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenu help = new JMenu("Help");
        JMenu subMenu_NewGame = new JMenu("New Game");
        subMenu_NewGame.setIcon(menuNewIcon);

        JMenuItem subMenu_Solution = new JMenuItem("Solution", menuSolutionIcon);
        JMenuItem subMenu_ExitGame = new JMenuItem("Exit Game", menuExitIcon);
        JMenuItem subHelp_ColorSwatch = new JMenuItem("Change Color Scheme", menuColorIcon);
        JMenuItem subHelp_AboutPage = new JMenuItem("About", menuAboutIcon);
        JMenuItem[] newGame_Dimensions = {new JMenuItem("3x3"), new JMenuItem("4x4"),
                new JMenuItem("5x5"), new JMenuItem("6x6"),
                new JMenuItem("7x7"), new JMenuItem("8x8")};

        //Fonts
        menu.setFont(openSans);
        help.setFont(openSans);
        subMenu_NewGame.setFont(openSans);
        subMenu_Solution.setFont(openSans);
        subMenu_ExitGame.setFont(openSans);
        subHelp_ColorSwatch.setFont(openSans);
        subHelp_AboutPage.setFont(openSans);

        //Add action listeners
        for (int i = 0; i < newGame_Dimensions.length; i++)
            gameController.addMenuItemListener(newGame_Dimensions[i], GameController.Functions.SCALE_BOARD, i + 3);

        gameController.addMenuItemListener(subMenu_Solution, GameController.Functions.SHOW_SOLUTION, 0);
        gameController.addMenuItemListener(subMenu_ExitGame, GameController.Functions.EXIT_GAME, 0);
        gameController.addMenuItemListener(subHelp_ColorSwatch, GameController.Functions.CHOOSE_COLOR, 0);
        gameController.addMenuItemListener(subHelp_AboutPage, GameController.Functions.ABOUT, 0);

        //Add components
        for (JMenuItem item : newGame_Dimensions)
            subMenu_NewGame.add(item);

        menu.add(subMenu_NewGame);
        menu.add(subMenu_Solution);
        menu.add(subMenu_ExitGame);
        help.add(subHelp_ColorSwatch);
        help.add(subHelp_AboutPage);
        menuBar.add(menu);
        menuBar.add(help);
        setJMenuBar(menuBar);

        /* ---------------------- Mark Panel -------------------- */

        markBox = new JCheckBox("Mark");
        markBox.setPreferredSize(new Dimension(120, 60));
        markBox.setBackground(Color.ORANGE);
        markBox.setFont(mcRegular);
        markBox.setToolTipText("Check the Mark box to select tiles that you might think are incorrect");
        gameController.addCheckBoxListener(markBox);

        markPanel.setBackground(BKGRD);
        markPanel.setPreferredSize(new Dimension(tileDimension, tileDimension));
        if (DEBUG) markPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 3));
        markPanel.add(markBox);

        // Mark Panel GridBagConstraints
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(tileDimension/2,0,0,0);
        mainPanel.add(markPanel, gbc);

        /* ---------------------- Top Panel -------------------- */

        topTextPaneArr = new ArrayList<>(dimension);
        topPanel.setLayout(grTop);
        topPanel.setBackground(BKGRD);
        topPanel.setPreferredSize(new Dimension(tileDimension * dimension, tileDimension));
        if (DEBUG) topPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));

        addTopHintRow();

        // Top Panel GridBagConstraints
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.SOUTH;
        mainPanel.add(topPanel, gbc);

        /* ---------------------- Left Panel -------------------- */

        leftTextPaneArr = new ArrayList<>(dimension);
        leftPanel.setLayout(grLeft);
        leftPanel.setBackground(BKGRD);
        leftPanel.setPreferredSize(new Dimension(tileDimension, tileDimension * dimension));
        if (DEBUG) leftPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));

        addLeftHintCol();

        // Left Panel GridBagConstraints
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(leftPanel, gbc);

        /* ---------------------- Center Panel -------------------- */

        boardMatrix = new ArrayList<>(dimension * dimension);
        centerPanel.setLayout(glCenter);
        centerPanel.setPreferredSize(new Dimension(tileDimension * dimension, tileDimension * dimension));
        centerPanel.setBackground(BKGRD);
        if (DEBUG) centerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));

        addCenterTiles();

        // Center Panel GridBagConstraints
        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(centerPanel, gbc);

        /* ---------------------- Right Panel -------------------- */

        // Define components for rightPanel
        bannerLogo.setIcon(resizeImageIcon(new Dimension((int)(tileDimension * 1.5), tileDimension/3), bufBanner));

        JLabel pointsLabel = new JLabel("Points: ");
        pointsLabel.setForeground(Color.WHITE);
        pointsLabel.setFont(mcRegular);

        pointsField = new JTextField("00000");
        pointsField.setFont(mcRegular);
        pointsField.setEditable(false);
        pointsField.setMaximumSize(new Dimension(tileDimension, mcRegular.getSize()));

        JLabel timeLabel = new JLabel("Time: ");
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(mcRegular);

        timeField = new JTextField("00:00");
        timeField.setFont(mcRegular);
        timeField.setEditable(false);
        timeField.setMaximumSize(new Dimension(tileDimension, mcRegular.getSize()));

        JButton bReset = new JButton("Reset");
        bReset.setFont(mcRegular);
        bReset.setPreferredSize(new Dimension(50, 20));
        bReset.setToolTipText("Reset the game (current layout remains the same)");
        gameController.addButtonListener(bReset);

        rightTextPane = new JTextPane();
        rightTextPane.setEditable(false);
        rightTextPane.setFont(mcRegular);
        rightTextPane.setBackground(Color.WHITE);
        rightTextPane.setPreferredSize(new Dimension(tileDimension * 2, tileDimension * 4));

        rightPanel.setBackground(BKGRD);
        rightPanel.setLayout(grRight);
        if (DEBUG) rightPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));

        // Create scroll bar and make it never visible
        int v_scrollPolicyEnum = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
        int h_scrollPolicyEnum = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
        JScrollPane v_scrollBar = new JScrollPane(rightTextPane);
        v_scrollBar.setPreferredSize(new Dimension(tileDimension * 2, tileDimension * 4));
        v_scrollBar.setVerticalScrollBarPolicy(v_scrollPolicyEnum);
        v_scrollBar.setHorizontalScrollBarPolicy(h_scrollPolicyEnum);

        // Add components to rightPanel (GroupLayout)
        grRight.setAutoCreateGaps(true);
        grRight.setAutoCreateContainerGaps(true);

        // Horizontal Group
        GroupLayout.ParallelGroup hGroup = grRight.createParallelGroup(GroupLayout.Alignment.CENTER);

        hGroup.addComponent(bannerLogo);
        hGroup.addGroup(grRight.createSequentialGroup().addComponent(pointsLabel).addComponent(pointsField));
        hGroup.addComponent(v_scrollBar);
        hGroup.addGroup(grRight.createSequentialGroup().addComponent(timeLabel).addComponent(timeField));
        hGroup.addComponent(bReset);

        grRight.setHorizontalGroup(hGroup);

        // Vertical Group
        GroupLayout.SequentialGroup vGroup = grRight.createSequentialGroup();

        vGroup.addComponent(bannerLogo);
        vGroup.addGroup(grRight.createParallelGroup(GroupLayout.Alignment.CENTER).
                addComponent(pointsLabel).addComponent(pointsField));
        vGroup.addComponent(v_scrollBar);
        vGroup.addGroup(grRight.createParallelGroup(GroupLayout.Alignment.CENTER).
                addComponent(timeLabel).addComponent(timeField));
        vGroup.addComponent(bReset);

        grRight.setVerticalGroup(vGroup);

        // Right Panel GridBagConstraints
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.insets = INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(rightPanel, gbc);
    }

    /**
     * @since 2021-10-09
     *
     * Splash is in charge of creating the splash screen for the main
     * game. It uses a separate JFrame than the main game, which uses
     * a KeyListener to dispose itself and begin the main game.
     */
    public void splash() {

        int gameTextWidth = 100;
        int gameTextHeight = 25;
        int bannerWidth = 225;
        int bannerHeight = 40;
        int splashBackgroundWidth = 2560;
        int splashBackgroundHeight = 1600;

        JFrame jf = new JFrame("Neil Kingdom - Picross Splash");
        //Warning: null layout managers are ill advised. Need it for custom positioning of the background
        JPanel bkgrndPane = new JPanel(null);

        JLayeredPane layeredSplash = new JLayeredPane();
        layeredSplash.setLayout(new LayeredPaneLayout(layeredSplash));

        /* ---------------------- Resource Config -------------------- */

        try {
            bufBanner = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossBanner.jpg"));
            bannerIcon = new ImageIcon(bufBanner.getScaledInstance(bannerWidth, bannerHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossBanner.jpg\"");
        }

        try {
            bufStartGameText = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossStartGame.png"));
            startGameTextIcon = new ImageIcon(bufStartGameText.getScaledInstance(gameTextWidth, gameTextHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load image: \"/res/img/PicrossStartGame.png\"");
        }

        try {
            bufSplash = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossSplash.jpg"));
            splashIcon = new ImageIcon(bufSplash.getScaledInstance(splashBackgroundWidth, splashBackgroundHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load image: \"/res/img/PicrossSplash.jpg\"");
        }

        //picrossBanner error handling
        if (bannerIcon != null)
            bannerLogo = new JLabel(bannerIcon);
        else
            bannerLogo = new JLabel("Missing Icon");
        bannerLogo.setPreferredSize(new Dimension(gameTextWidth, gameTextHeight));

        //gameTextLabel error handling
        if (startGameTextIcon != null)
            gameTextLabel = new JLabel(startGameTextIcon);
        else
            gameTextLabel = new JLabel("Missing Icon");
        gameTextLabel.setPreferredSize(new Dimension(gameTextWidth, gameTextHeight));

        //splashIcon error handling
        if (splashIcon != null)
            splashBackground = new JLabel(splashIcon);
        else
            splashBackground = new JLabel("Missing Icon");
        //Shouldn't normally force sizing, however, in this case the null layout manager was forcing it to 0,0. setSize() fixes this
        splashBackground.setSize(new Dimension(splashBackgroundWidth, splashBackgroundHeight));

        /* ---------------------- Add Components to Splash -------------------- */

        //Center splashBackground
        splashBackground.setLocation(MIN_WIN_WIDTH / 2 - splashBackground.getWidth() / 2,
                MIN_WIN_HEIGHT / 2 - splashBackground.getHeight() / 2);

        //Add Components
        bkgrndPane.add(splashBackground);

        Integer zBuf0 = 0;
        Integer zBuf1 = 1;
        Integer zBuf2 = 2;
        layeredSplash.add(bkgrndPane, zBuf0);

        layeredSplash.add(bannerLogo, zBuf1);
        //TODO: As of now, I see no way to add the oscilating text to the JFrame
        //layeredSplash.add(gameTextLabel, zBuf2);

        jf.add(layeredSplash);
        jf.setMinimumSize(new Dimension(MIN_WIN_WIDTH, MIN_WIN_HEIGHT));
        jf.setLocation(X_START_POS - jf.getWidth() / 2, Y_START_POS - jf.getHeight() / 2);
        jf.setVisible(true);
        jf.setResizable(false);

        //Start Animations
        SplashThread st = new SplashThread(this);
        st.start();

        gameController.addJFrameListener(jf, st);
    }

    /**
     * @since 2021-10-09
     * @param perfectScore Boolean which evaluates if user obtained a perfect score or not
     *
     * endSplash is in charge of creating the splash screen for when
     * the user finishes the game. It uses a separate JFrame than the
     * main game, which uses a KeyListener to dispose itself on close.
     */
    public void endSplash(boolean perfectScore) {

        JFrame endScreen = new JFrame("Game Over");
        endScreen.setLayout(new BorderLayout());

        JLabel endSplashIcon;

        if (perfectScore) {
            endScreen.setTitle("Perfect Score!");

            //winnerLabel error handling
            if (winnerIcon != null)
                endSplashIcon = new JLabel(winnerIcon);
            else
                endSplashIcon = new JLabel("Missing Icon");
            endSplashIcon.setPreferredSize(new Dimension(endSplashWidth, endSplashHeight));
        }
        else {
            endScreen.setTitle("Game Over");

            //gameOverLabel error handling
            if (gameOverIcon != null)
                endSplashIcon = new JLabel(gameOverIcon);
            else
                endSplashIcon = new JLabel("Missing Icon");
            endSplashIcon.setPreferredSize(new Dimension(endSplashWidth, endSplashHeight));
        }

        /* ---------------------- Add Components to End Splash -------------------- */

        JLabel pointsLabel = new JLabel("Points: " + points);
        pointsLabel.setPreferredSize(new Dimension(75, 20));
        JButton gameOverButton = new JButton("Continue");
        gameOverButton.setPreferredSize(new Dimension(75, 20));

        gameOverButton.addActionListener(e1 -> endScreen.dispose());

        endScreen.add(pointsLabel, BorderLayout.NORTH);
        endScreen.add(endSplashIcon, BorderLayout.CENTER);
        endScreen.add(gameOverButton, BorderLayout.SOUTH);

        endScreen.pack();
        endScreen.setVisible(true);
        endScreen.setResizable(false);
    }

    /**
     * @since 2021-11-12
     *
     * This function adds the "hint tiles" to the top row
     * of the game.
     */
    public void addTopHintRow() {

        topHintRow = GameModel.getTopHintRow();
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.weightx = 0.2;
        gbcTop.weighty = 0.2;
        gbcTop.fill = GridBagConstraints.HORIZONTAL;
        gbcTop.anchor = GridBagConstraints.SOUTH;

        for (int x = 0; x < dimension; x++) {

            JTextPane topTextPane = new JTextPane();
            topTextPane.setEditable(false);
            topTextPane.setFont(mcRegular);
            topTextPane.setForeground(Color.WHITE);
            topTextPane.setBackground(BKGRD);
            if(DEBUG) topTextPane.setBorder(BorderFactory.createLineBorder(FRGRD, 3));

            int variableHeight = topHintRow.get(x).size() * (tileDimension / 2);
            topTextPane.setSize(new Dimension(tileDimension, variableHeight));

            StyledDocument doc = topTextPane.getStyledDocument();
            try {
                String hintCellStr = "";
                StringBuilder sb = new StringBuilder(hintCellStr);

                for (Integer i : topHintRow.get(x))
                    sb.append(i).append("\n");

                hintCellStr = sb.toString();
                doc.insertString(0, hintCellStr, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);

            gbcTop.gridy = 0;
            gbcTop.gridx = x;
            topTextPaneArr.add(topTextPane);
            topPanel.add(topTextPane, gbcTop);
        }
    }

    /**
     * @since 2021-11-12
     *
     * This function adds the "hint tiles" to the left column
     * of the game.
     */
    public void addLeftHintCol() {

        leftHintCol = GameModel.getLeftHintCol();
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.weightx = 0.2;
        gbcLeft.weighty = 0.2;
        gbcLeft.fill = GridBagConstraints.VERTICAL;
        gbcLeft.anchor = GridBagConstraints.EAST;

        for (int y = 0; y < dimension; y++) {

            JTextPane leftTextPane = new JTextPane();
            leftTextPane.setEditable(false);
            leftTextPane.setFont(mcRegular);
            leftTextPane.setForeground(Color.WHITE);
            leftTextPane.setBackground(BKGRD);
            if(DEBUG) leftTextPane.setBorder(BorderFactory.createLineBorder(FRGRD, 3));

            StyledDocument doc = leftTextPane.getStyledDocument();
            try {
                String hintCellStr = "";
                StringBuilder sb = new StringBuilder(hintCellStr);

                //Hacky way of centering text vertically according to scaling
                for(int i = 0; i < (Math.abs(dimension-8)-1); i++) {
                    sb.append("\n");
                }

                for (Integer i : leftHintCol.get(y))
                    sb.append(i).append(" ");

                hintCellStr = sb.toString();
                doc.insertString(0, hintCellStr, null);

            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);

            gbcLeft.gridx = 0;
            gbcLeft.gridy = y;
            leftTextPaneArr.add(leftTextPane);
            leftPanel.add(leftTextPane, gbcLeft);
        }
    }

    /**
     * @since 2021-11-12
     *
     * This function adds the picross tiles to the
     * center of the board
     */
    public void addCenterTiles() {

        for (int y = 0; y < dimension; y++) {
            for (int x = 0; x < dimension; x++) {

                JLabel tile = new JLabel();
                tile.setOpaque(true);
                tile.setBackground(Color.WHITE);
                gameController.addJLabelListener(tile);

                boardMatrix.add(tile);
                centerPanel.add(tile);
            }
        }
    }

    /* -------------------- Getters --------------------- */

    public JPanel getTopPanel() {
        return topPanel;
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public JPanel getCenterPanel() {
        return centerPanel;
    }

    public JTextPane getRightTextPane() {
        return rightTextPane;
    }

    public JTextField getPointsField() {
        return pointsField;
    }

    public int getPoints() {
        return points;
    }

    public JTextField getTimeField() {
        return timeField;
    }

    public short[] getTime() {
        return new short[]{mins, seconds};
    }

    public JLabel getGameTextLabel() {
        return gameTextLabel;
    }

    public JLabel getSplashBackground() {
        return splashBackground;
    }

    public ArrayList<JLabel> getBoardMatrix() {
        return boardMatrix;
    }

    public ArrayList<JTextPane> getTopTextPaneArr() {
        return topTextPaneArr;
    }

    public ArrayList<JTextPane> getLeftTextPaneArr() {
        return leftTextPaneArr;
    }

    public ArrayList<ArrayList<Integer>> getTopHintRow() {
        return topHintRow;
    }

    public ArrayList<ArrayList<Integer>> getLeftHintCol() {
        return leftHintCol;
    }

    public int getTileDimension() {
        return tileDimension;
    }

    public boolean getMarkBoxState() {
        return markBoxState;
    }

    /* -------------------- Setters --------------------- */

    public void setPoints(int points) {
        this.points = (points < 0) ? 0 : points;
    }

    public void setTime(short[] time) {
        mins = time[0]; seconds = time[1];
    }

    public void setTileDimension(int tileDimension) {
        this.tileDimension = tileDimension;
    }

    public void setMarkBoxState(boolean state) {
        markBoxState = state;
    }
}
