package picross;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Random;

import static picross.Game.*;
import static picross.Game.Y_START_POS;

/**
 * @author Neil Kingdom
 * @version 1.2
 * @since 2021-11-28
 *
 * GameClient provides a GUI for clients to connect to a central server
 * via socket connections. The interface allows user actions such as
 * requesting and/or sending game configuration strings, ending the
 * connection, generating new random configurations, and playing a new
 * game
 * */
public class GameClient extends JFrame {

    /* ---------------------- Constants -------------------- */

    final static char PROTOCOL_SEP = '#';

    enum Protocols {
        END_GAME(0), SEND_GAME(1), RECEIVE_GAME(2), SEND_DATA(3);
        private final int value;
        private Protocols(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /* ---------------------- Member Variables -------------------- */

    private static int portNum;
    private static int randDimension;
    private static String clientID;
    private static String userData;
    private static String userName;
    private static String serverIP;
    private static String config;
    private static JTextPane dataLog;
    private static JLabel splashLabel;

    private Socket clientSocket;
    private BufferedReader dataInStream;
    private PrintWriter dataOutStream;

    private GameModel gameModel;
    private GameView gameView;
    private GameController gameController;

    protected static BufferedImage bufClientSplash;
    protected static ImageIcon clientSplashIcon;

    /**
     * @since 2021-11-28
     *
     * Default constructor for GameClient
     */
    private GameClient() {
        portNum = 0;
        randDimension = 0;
        clientID = "";
        userName = "";
        userData = "";
        serverIP = "";
        config = "";

        gameModel = new GameModel();
        gameView = new GameView();

        clientSplash();
    }

    /**
     * @since 2021-11-28
     *
     * ClientSplash is in charge of creating the splash screen for the client
     * GUI. 3 fields must be filled out before a connection can be made to the
     * server: Username, Server IP, and Port Number
     */
    private void clientSplash() {

        int clientSplashBkgrndWidth = 538;
        int clientSplashBkgrndHeight = 120;

        JFrame jf = new JFrame("Neil Kingdom - Picross Client");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel splashPanel = new JPanel();
        JPanel formPanel = new JPanel();
        JPanel submitPanel = new JPanel();
        JPanel logPanel = new JPanel();

        /* ---------------------- Resource Config -------------------- */

        try {
            bufClientSplash = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossClientSplash.png"));
            clientSplashIcon = new ImageIcon(bufClientSplash.getScaledInstance(clientSplashBkgrndWidth, clientSplashBkgrndHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossClientSplash.png\"");
        }

        //clientSplashIcon error handling
        if (clientSplashIcon != null)
            splashLabel = new JLabel(clientSplashIcon);
        else
            splashLabel = new JLabel("Missing Icon");
        splashLabel.setPreferredSize(new Dimension(clientSplashBkgrndWidth, clientSplashBkgrndHeight));

        /* ---------------------- Initialize Components -------------------- */

        JLabel userLabel = new JLabel("User: ");
        JLabel serverLabel = new JLabel("Server: ");
        JLabel portLabel = new JLabel("Port: ");

        JTextField userField = new JTextField();
        JTextField serverField = new JTextField();
        JTextField portField = new JTextField();

        JButton bConnect = new JButton("Connect");
        JButton bEnd = new JButton("End");
        JButton bNewGame = new JButton("New Game");
        JButton bSendGame = new JButton("Send Game");
        JButton bReceiveGame = new JButton("Receive Game");
        JButton bSendData = new JButton("Send Data");
        JButton bPlay = new JButton("Play");

        dataLog = new JTextPane();
        JScrollPane dataScroll = new JScrollPane(dataLog);

        /* ---------------------- Configure Components -------------------- */

        userField.setPreferredSize(new Dimension(80, 20));
        userField.setText("John Doe");
        serverField.setPreferredSize(new Dimension(80, 20));
        serverField.setText("localhost");
        portField.setPreferredSize(new Dimension(60, 20));
        portField.setText("8080");

        int v_scrollPolicyEnum = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        int h_scrollPolicyEnum = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
        dataScroll.setVerticalScrollBarPolicy(v_scrollPolicyEnum);
        dataScroll.setHorizontalScrollBarPolicy(h_scrollPolicyEnum);
        dataLog.setPreferredSize(new Dimension(clientSplashBkgrndWidth, clientSplashBkgrndHeight));
        dataLog.setEditable(false);

        /* ---------------------- Action Listeners -------------------- */

        bConnect.addActionListener(actionEvent -> {
            if (userField.getText().isEmpty()) {
                outputEvent("Please enter your user name before attempting to connect to the server...\n");
            }
            if (serverField.getText().isEmpty()) {
                outputEvent("Please enter the IP address of the server you're trying to connect to before pressing connect...\n");
            }
            if (portField.getText().isEmpty()) {
                outputEvent("Please enter the port that you are attempting to connect to before pressing connect...\n");
            } else {
                bConnect.setEnabled(false);

                userName = userField.getText();
                serverIP = serverField.getText();
                portNum = Integer.valueOf(portField.getText());

                outputEvent("Creating new MVC game...\n");

                connectToHost();
            }
        });

        bEnd.addActionListener(actionEvent -> {

            JDialog endGame = new JDialog();

            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(endGame, "Are you sure you'd like to end the connection?\n"
                            + "Connection to the server will be lost...", "End client connection",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

            if (n == 0) {

                genDatagram(Protocols.END_GAME, "");

                try {
                    String serverMsg;
                    while((serverMsg  = dataInStream.readLine()) != null) {
                        outputEvent(serverMsg);
                    }
                } catch (IOException ioe) {
                    errorPopup("Connection Error", "Communications error with host");
                }

                try {
                    dataInStream.close();
                    dataOutStream.close();
                    clientSocket.close();
                } catch(IOException ioe) {
                    System.err.println("Failed to close client socket");
                }

                System.out.println("Client ended");
                endGame.dispose();
                dispose();
                System.exit(0);
            }
        });

        bNewGame.addActionListener(actionEvent -> {
            if(gameView.getJFrame() == null) {
                //Set game to have a random dimension
                Random rand = new Random();
                randDimension = rand.nextInt((8 + 1) - 3) + 3;
                Game.setDimension(randDimension);
                gameModel.genRandBitStream();
                config = gameModel.toString();

                //Add commas for aesthetics...
                StringBuffer sBuff = new StringBuffer(config);
                for(int i = randDimension-1, j = 1; i < (randDimension * randDimension)-randDimension; i+=randDimension) {
                    sBuff.insert(i + j, ",");
                    j++; //String size changes once comma is added
                }

                config = sBuff.toString();
                outputEvent("Current game configuration loaded: " + config + "\n");
            }
            else {
                warningPopup("Warning", "Instance of game is running. Cannot load a new configuration");
            }
        });

        bSendData.addActionListener(actionEvent -> {
            if(gameView.getJFrame() != null) {
                userData = userName + "," + gameView.getTime()[0] + ":" + gameView.getTime()[1] + "," + gameView.getPoints();
                genDatagram(Protocols.SEND_DATA, userData);
            }
            else {
                warningPopup("Warning", "Cannot send data because no active game is running");
            }
        });

        bSendGame.addActionListener(actionEvent -> {
            if(!config.isEmpty())
                genDatagram(Protocols.SEND_GAME, config);
            else
                warningPopup("Warning", "No configuration has been loaded. Nothing will be sent");
        });

        bPlay.addActionListener(actionEvent -> SwingUtilities.invokeLater(() -> {
            if(config.isEmpty()) {
                warningPopup("Warning", "No configuration has been loaded. Starting game with default configuration");
                config = gameModel.toString();
            }

            //If client received config string from server, dimensions and hints need to be updated
            else {
                config = config.replace(",", ""); //Unsure why this occurs, but sometimes commas persist after removing them when receiving serverMsg
                Game.setDimension((int) Math.round(Math.sqrt(config.length())));
                gameModel.setBoard(gameModel.tokenizeBitStream(config));
                gameModel.setTopHintRow(gameModel.genTopHints(gameModel.getBoard()));
                gameModel.setLeftHintCol(gameModel.genLeftHints(gameModel.getBoard()));
            }

            gameController = new GameController(gameModel, gameView);
            gameController.rescaleBoard(dimension, false);
        }));

        bReceiveGame.addActionListener(actionEvent -> genDatagram(Protocols.RECEIVE_GAME, ""));

        /* ---------------------- Add Components -------------------- */

        splashPanel.add(splashLabel);
        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(serverLabel);
        formPanel.add(serverField);
        formPanel.add(portLabel);
        formPanel.add(portField);
        formPanel.add(bConnect);
        formPanel.add(bEnd);
        submitPanel.add(bNewGame);
        submitPanel.add(bSendGame);
        submitPanel.add(bReceiveGame);
        submitPanel.add(bSendData);
        submitPanel.add(bPlay);
        logPanel.add(dataScroll);

        mainPanel.add(splashPanel);
        mainPanel.add(formPanel);
        mainPanel.add(submitPanel);
        mainPanel.add(logPanel);

        jf.add(mainPanel);
        jf.pack();
        jf.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jf.setLocation(X_START_POS - jf.getWidth() / 2, Y_START_POS - jf.getHeight() / 2);
        jf.setResizable(false);
        jf.setVisible(true);
    }

    /**
     * @since 2022-12-08
     *
     * This method establishes the connection between the client and the host.
     * A thread is created which listens for incoming messages from the server
     * */
    public void connectToHost() {

        try {
            clientSocket = new Socket(serverIP, portNum);
            dataInStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            dataOutStream = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.err.println("Client: Error initializing socket/stream");
            ioe.printStackTrace();
        }

        //Perpetually listen for messages from server
        Thread listenThread = new Thread(() -> {
            try {
                String serverMsg = dataInStream.readLine();
                clientID = serverMsg;
                outputEvent("Connection with " + serverIP + " on port " + portNum + "\n");
                outputEvent("Server: Your client ID is " + serverMsg + "\n"); //First message that client will receive
                while (serverMsg != null) {
                    serverMsg = dataInStream.readLine();
                    String[] datagram = serverMsg.split(String.valueOf(PROTOCOL_SEP));
                    if (datagram.length > 0) {
                        switch (GameServer.Protocols.values()[Character.getNumericValue(datagram[1].charAt(0))]) { //"Casting" protocol id to an enum
                            case RECEIVE_GAME -> {
                                outputEvent(serverMsg);
                                if (datagram.length > 2) {
                                    outputEvent("(Server: Sent configuration string - " + datagram[2] + ")\n");
                                    config = datagram[2].replace(",", "");
                                } else {
                                    outputEvent("(Server: No configuration has been stored yet...)\n");
                                }
                            }
                            default -> outputEvent("Server disconnected\n");
                        }
                    }
                }
            } catch (IOException ioe) {
                errorPopup("Connection Error", "Communications error with host");
            }
        });
        listenThread.start();
    }

    /**
     * @since 2022-12-08
     *
     * Generates a datagram to be sent to the server. The datagram must comply
     * with the specification for a made up protocol which follows the following
     * formatting: <ClientID><Separator: #><ProtocolID><Separator: #><Data>
     * The data section can technically be thought of as it's own datagram as well
     * as it contains to parts: <ConfigStr><Separator: %><ClientData> where configStr
     * is the configuration string and clientData is the username, time, and score of
     * the player/client.
     * */
    public void genDatagram(Protocols protocol, String data) {

        String datagram = "";
        StringBuilder sb = new StringBuilder(datagram);

        switch (protocol) {
            case END_GAME -> sb.append(clientID).append(PROTOCOL_SEP).append(Protocols.END_GAME.getValue());
            case SEND_GAME -> sb.append(clientID).append(PROTOCOL_SEP).append(Protocols.SEND_GAME.getValue()).append(PROTOCOL_SEP).append(data);
            case RECEIVE_GAME -> sb.append(clientID).append(PROTOCOL_SEP).append(Protocols.RECEIVE_GAME.getValue());
            case SEND_DATA -> sb.append(clientID).append(PROTOCOL_SEP).append(Protocols.SEND_DATA.getValue()).append(PROTOCOL_SEP).append(data);
        }

        try {
            dataOutStream.println(sb);
            dataOutStream.flush();
        } catch (NullPointerException e) {
            errorPopup("Error", "Client is not connected to the server");
        }
    }

    /**
     * @since 2022-12-08
     * @param text The text to output to the text pane/console
     *
     * OutputEvent simply automates message output to the "console" ie.
     * JTextPane. It is capable of manually scrolling the JScrollPane
     * */
    public void outputEvent(String text) {
        try {
            dataLog.getDocument().insertString(dataLog.getDocument().getLength(), text + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        // Update caret position to bottom of text pane
        dataLog.setCaretPosition(dataLog.getDocument().getLength());
    }

    /**
     * @since 2022-12-08
     * @param title The header for the JDialog
     * @param message The error message
     *
     * Displays a JDialog ERROR_MESSAGE
     * */
    public void errorPopup(String title, String message) {
        JDialog errorDialog = new JDialog();
        JOptionPane.showMessageDialog(errorDialog, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @since 2022-12-08
     * @param title The header for the JDialog
     * @param message The warning message
     *
     * Displays a JDialog WARNING_MESSAGE
     * */
    public void warningPopup(String title, String message) {
        JDialog warningDialog = new JDialog();
        JOptionPane.showMessageDialog(warningDialog, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * @since 2022-12-08
     *
     * Secondary main function which gets called if the
     * user supplies the command line argument 'c'
     * */
    public static void main(String[] args) {
        //Start GUI
        SwingUtilities.invokeLater(() -> new GameClient());
    }
}
