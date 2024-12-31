package picross;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * @author Neil Kingdom
 * @since 2021-09-15
 * @version 1.0
 *
 * GameController creates the actual gameplay screen. The screen is
 * split into regions for both layout management and gameplay.
 * This screen is persistent until the user decides to quit.
 * */

public class GameController extends JFrame {

    private static final int MIN_WIN_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width/3);
    private static final int MIN_WIN_HEIGHT = (Toolkit.getDefaultToolkit().getScreenSize().height/5);
    private static final int PADDING = 1;
    private static final Insets INSETS = new Insets(1,1, 1,1);
    private static final int TILE_DIMENSION = 60;
    private static final boolean DEBUG=false;
    private static int matrixSize;

    private static final Color BKGRD = new Color(55, 61, 103);
    private static final Color FRGRD = new Color(35, 160, 255);

    /**
     * @author Neil Kingdom
     * @since 2021-10-09
     * @version 1.0
     * @param matrixSize - The height/width of the playing tiles
     *
     * Parameterized constructor for GameController
     * */
    public GameController(int matrixSize) {

        super("Picross - Neil Kingdom");
        this.matrixSize = matrixSize;
        Container mainPanel = getContentPane();

        /* ---------------------- JFrame Setup -------------------- */

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(false);
        setForeground(Color.WHITE);
        setMinimumSize(new Dimension(MIN_WIN_WIDTH, MIN_WIN_HEIGHT));
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(BKGRD);

        /* ---------------------- Resource Config -------------------- */

        Font mcRegular;
        Font mcBold;

        BufferedImage bufBanner;
        ImageIcon bannerIcon = null;

        try {
            bufBanner = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossBanner.jpg"));
            bannerIcon = new ImageIcon(bufBanner.getScaledInstance(150, 25, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load image: \"/res/img/PicrossBanner.jpg\"");
        }

        try {
            mcRegular = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/res/fonts/MinecraftRegular.otf")).deriveFont(Font.PLAIN, 16);
            mcBold = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/res/fonts/MinecraftBold.otf")).deriveFont(Font.BOLD, 16);
            setFont(mcRegular);
        } catch (IOException | FontFormatException | IllegalArgumentException e) {
            // Only using MinecraftRegular.otf for now
            System.err.println("Could not load font: \"/res/fonts/MinecraftRegular.otf\"");
        }

        /* ---------------------- Init JPanels -------------------- */

        JPanel markPanel = new JPanel();
        JPanel topPanel = new JPanel();
        JPanel leftPanel = new JPanel();
        JPanel centerPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        /* ---------------------- Define Layouts -------------------- */

        GroupLayout grRight = new GroupLayout(rightPanel);
        GridLayout glCenter = new GridLayout(matrixSize, matrixSize, PADDING, PADDING);
        BoxLayout blLeft = new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS);
        BoxLayout blTop = new BoxLayout(topPanel, BoxLayout.LINE_AXIS);
//        BorderLayout blTop = new BorderLayout(PADDING,0);
//        FlowLayout flTop = new FlowLayout(FlowLayout.CENTER, PADDING, 0);

        /* ---------------------- Mark Panel -------------------- */

        JCheckBox markBox = new JCheckBox("Mark");
        markBox.setPreferredSize(new Dimension(TILE_DIMENSION, TILE_DIMENSION/2));
        markBox.setBackground(Color.ORANGE);
        markBox.setFont(super.getFont());
        markBox.setBorder(BorderFactory.createRaisedBevelBorder());
        markBox.setToolTipText("Check the Mark box to select tiles");
        new Controller(markBox, "Mark box pressed");

        markPanel.setBackground(new Color(250, 163, 49));
        if(DEBUG)
            markPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA,3));
        markPanel.add(markBox);

        // Mark Panel GridBagConstraints
        GridBagConstraints gbcMark = new GridBagConstraints();
        gbcMark.gridx = 0;
        gbcMark.gridy = 0;
        gbcMark.weightx = 0.2;
        gbcMark.weighty = 0.2;
        gbcMark.insets = INSETS;
        gbcMark.anchor = GridBagConstraints.CENTER;
        mainPanel.add(markPanel, gbcMark);

        /* ---------------------- Top Panel -------------------- */

        ArrayList<Integer> randHeights = new ArrayList<>(matrixSize);
        ArrayList<Integer> randWidths = new ArrayList<>(matrixSize);

        for(int i = 0; i < matrixSize; i++) {
            Random rand = new Random();
            randHeights.add(i, (rand.nextInt(3-0) + 1));
            randWidths.add(i, (rand.nextInt(3-0) + 1));
        }

        topPanel.setPreferredSize(new Dimension((TILE_DIMENSION * matrixSize) + ((PADDING * matrixSize) + PADDING), TILE_DIMENSION*Collections.max(randHeights)));
        topPanel.setLayout(blTop);
        topPanel.setBackground(BKGRD);
        if(DEBUG)
            topPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN,3));

        for(int x = 0; x < matrixSize; x++) {

            int variableHeight = (TILE_DIMENSION/2) * randHeights.get(x);

            JTextPane topTextPane = new JTextPane();
            topTextPane.setMaximumSize(new Dimension(TILE_DIMENSION, variableHeight));
            topTextPane.setEditable(false);
            topTextPane.setFont(super.getFont());
            topTextPane.setBackground(FRGRD);
            topTextPane.setBorder(BorderFactory.createRaisedBevelBorder());
            topTextPane.setAlignmentY(Component.BOTTOM_ALIGNMENT);

            // Mega bloat just to center text!!!
            StyledDocument doc = topTextPane.getStyledDocument();
            try {
                doc.insertString(0,x + "", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
                dispose();
                System.exit(-1);
            }
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);

            topPanel.add(topTextPane);
        }

        // Top Panel GridBagConstraints
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.gridx = 1;
        gbcTop.gridy = 0;
        gbcTop.gridwidth = 2;
        gbcTop.gridheight = 1;
        gbcTop.weightx = 0.2;
        gbcTop.weighty = 0.2;
        gbcTop.insets = INSETS;
        mainPanel.add(topPanel, gbcTop);

        /* ---------------------- Left Panel -------------------- */

        leftPanel.setPreferredSize(new Dimension(TILE_DIMENSION*Collections.max(randWidths), TILE_DIMENSION * matrixSize));
        leftPanel.setLayout(blLeft);
        leftPanel.setBackground(BKGRD);
        if(DEBUG)
            leftPanel.setBorder(BorderFactory.createLineBorder(Color.RED,3));

        for(int y = 0; y < matrixSize; y++) {

            int variableWidth = (TILE_DIMENSION/2) * randWidths.get(y);

            JTextPane leftTextPane = new JTextPane();
            leftTextPane.setMaximumSize(new Dimension(variableWidth, TILE_DIMENSION));
            leftTextPane.setEditable(false);
            leftTextPane.setFont(super.getFont());
            leftTextPane.setBackground(FRGRD);
            leftTextPane.setBorder(BorderFactory.createRaisedBevelBorder());
            leftTextPane.setAlignmentX(Component.RIGHT_ALIGNMENT);

            StyledDocument doc = leftTextPane.getStyledDocument();
            try {
                doc.insertString(0,y + "", null);
            } catch (BadLocationException e) {
                e.printStackTrace();
                dispose();
                System.exit(-1);
            }
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);

            leftPanel.add(leftTextPane);
        }

        // Left Panel GridBagConstraints
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 1;
        gbcLeft.gridwidth = 1;
        gbcLeft.gridheight = 2;
        gbcLeft.weightx = 0.2;
        gbcLeft.weighty = 0.2;
        gbcLeft.insets = INSETS;
        mainPanel.add(leftPanel, gbcLeft);

        /* ---------------------- Center Panel -------------------- */

        centerPanel.setLayout(glCenter);
        centerPanel.setPreferredSize(new Dimension(TILE_DIMENSION * matrixSize, TILE_DIMENSION * matrixSize));
        centerPanel.setBackground(BKGRD);
        if(DEBUG)
            centerPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE,3));

        for(int y = 0; y < matrixSize; y++) {
            for(int x = 0; x < matrixSize; x++) {
                // Add Picross game tiles to central JPanel
                JLabel tile = new JLabel();
                tile.setOpaque(true);
                tile.setBackground(Color.WHITE);
                tile.setPreferredSize(new Dimension(TILE_DIMENSION, TILE_DIMENSION));
                tile.setBorder(BorderFactory.createRaisedBevelBorder());

                // Set mouse listener
                new Controller(tile, "You clicked tile at pos: " + (x+1) + ", " + (y+1));

                centerPanel.add(tile);
            }
        }

        // Center Panel GridBagConstraints
        GridBagConstraints gbcCenter = new GridBagConstraints();
        gbcCenter.gridx = 1;
        gbcCenter.gridy = 1;
        gbcCenter.gridwidth = 2;
        gbcCenter.gridheight = 2;
        gbcCenter.weightx = 0.2;
        gbcCenter.weighty = 0.2;
        gbcCenter.insets = INSETS;
        mainPanel.add(centerPanel, gbcCenter);

        /* ---------------------- Right Panel -------------------- */

        // Define components for rightPanel
        JLabel bannerLogo;
        if(bannerIcon != null)
             bannerLogo = new JLabel(bannerIcon);
        else
            bannerLogo = new JLabel("Missing Image");

        JLabel pointsLabel = new JLabel("Points: ");
        pointsLabel.setForeground(super.getForeground());
        pointsLabel.setFont(super.getFont());

        JTextField pointsField = new JTextField("0000000");
        pointsField.setFont(super.getFont());
        pointsField.setEditable(false);

        JLabel timeLabel = new JLabel("Time: ");
        timeLabel.setForeground(super.getForeground());
        timeLabel.setFont(super.getFont());

        JTextField timeField = new JTextField("00:00:00");
        timeField.setFont(super.getFont());
        timeField.setEditable(false);

        JButton bReset = new JButton("Reset");
        bReset.setFont(super.getFont());
        bReset.setPreferredSize(new Dimension(50, 20));
        String bResetMessage = "You clicked the reset button";
        new Controller(bReset, bResetMessage);

        JTextPane rightTextField = new JTextPane();
        rightTextField.setEditable(false);
        rightTextField.setFont(super.getFont());
        rightTextField.setPreferredSize(new Dimension(TILE_DIMENSION * 2, TILE_DIMENSION * 4));
        rightTextField.setBackground(Color.WHITE);

//        Timer timer = new Timer(5, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Random rand = new Random();
//                char randChar = (char)(rand.nextInt() % ('z'-'a') + 'a');
//                StyledDocument document = (StyledDocument) rightTextField.getDocument();
//                try {
//                    document.insertString(document.getLength(), randChar  + "\n", null);
//                } catch (BadLocationException ex) {
//                    ex.printStackTrace();
//                }
//                // Update caret position to bottom of textarea
//                rightTextField.setCaretPosition(rightTextField.getDocument().getLength());
//            }
//        });
//        timer.setInitialDelay(5);
//        timer.start();

        rightPanel.setBackground(BKGRD);
        rightPanel.setLayout(grRight);
        if(DEBUG)
            rightPanel.setBorder(BorderFactory.createLineBorder(Color.YELLOW,3));

        // Create scroll bar and make it never visible
        int v_scrollPolicyEnum = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
        int h_scrollPolicyEnum = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
        JScrollPane v_scrollBar = new JScrollPane(rightTextField);
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
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 3;
        gbcRight.gridy = 0;
        gbcRight.gridwidth = 1;
        gbcRight.gridheight = 3;
        gbcRight.weightx = 0.2;
        gbcRight.weighty = 0.2;
        gbcRight.insets = INSETS;
        mainPanel.add(rightPanel, gbcRight);
    }

    /**
     * @author Neil Kingdom
     * @since 2021-10-09
     * @version 1.0
     *
     * Controller is an inner class used for applying action listeners.
     * The constructor must be overloaded for each kind of JComponent and
     * apply the appropriate listeners.
     * */
    class Controller extends JFrame {

        /**
         * @author Neil Kingdom
         * @since 2021-10-09
         * @version 1.0
         * @param comp - JButton to add action listener to.
         * @param message - A message to print to console when action handler fires.
         *
         * Constructor overloaded for JButtons
         * */
        public Controller(JButton comp, String message) {
            comp.addActionListener(al -> System.out.println(message));
        }

        /**
         * @author Neil Kingdom
         * @since 2021-10-09
         * @version 1.0
         * @param comp - JButton to add action listener to.
         * @param message - A message to print to console when action handler fires.
         *
         * Constructor overloaded for JCheckBox
         * */
        public Controller(JCheckBox comp, String message) {
            comp.addActionListener(al -> System.out.println(message));
        }

        /**
         * @author Neil Kingdom
         * @since 2021-10-09
         * @version 1.0
         * @param comp - JButton to add action listener to.
         * @param message - A message to print to console when action handler fires.
         *
         * Constructor overloaded for JLabel
         * */
        public Controller(JLabel comp, String message) {
            comp.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println(message);
                }

                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
            });
        }
    }
}
