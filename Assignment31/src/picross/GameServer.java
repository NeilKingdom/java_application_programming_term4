package picross;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static picross.Game.X_START_POS;
import static picross.Game.Y_START_POS;

/**
 * @author Neil Kingdom
 * @version 1.2
 * @since 2021-11-28
 *
 * GameServer provides a GUI which represents a
 * server within a C/S architecture. The server
 * may connect to multiple clients using multi/
 * hyper threading. It can recieve client
 * requests and respond to them accordingly
 * */
public class GameServer extends JFrame {

    /* ---------------------- Constants -------------------- */

    final static char PROTOCOL_SEP = '#';
    final static char DATA_SEP = '%';

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
    private static int numOfClients;
    private static boolean serverOn;
    private static boolean finalize;
    private static String globalConfig;
    private static JTextPane dataLog;
    private static JLabel splashLabel;
    private static Hashtable<String, String> userRecordTable;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    protected static BufferedImage bufServerSplash;
    protected static ImageIcon serverSplashIcon;

    /**
     * @since 2021-11-28
     *
     * Default constructor for GameServer
     */
    private GameServer() {
        portNum = 0;
        numOfClients = 0;
        serverOn = true;
        finalize = false;
        globalConfig = "";
        userRecordTable = new Hashtable<>(10);
        serverSplash();
    }

    /**
     * @author Neil Kingdom
     * @version 1.0
     * @since 2021-11-28
     *
     * ClientThread extends Thread and overrides the run() method
     * from the Runnable interface. This allows the code for each
     * client to be run on a separate thread, allowing for
     * parallel execution. Aside from that, each client thread
     * listens for messages from their respective clients and
     * handles actions according to the protocolID send in the
     * datagram (See genDatagram() in GameClient)
     * */
    class ClientThread extends Thread {

        Socket clientSocket;
        BufferedReader dataInStream;
        PrintWriter dataOutStream;
        String[] datagram;

        ClientThread(Socket socket) {
            clientSocket = socket;
        }

        @Override
        public void run() {

            /* ---------------------- Initialize socket/streams -------------------- */
            try {
                dataInStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                dataOutStream = new PrintWriter(clientSocket.getOutputStream());

                //We want to generate a unique identifier (UUID) for each client
                UUID clientUUID = UUID.randomUUID();
                String clientID = String.valueOf(clientUUID);
                addUserRecord(clientID, "");

                //Send UUID to client
                dataOutStream.println(clientID);
                dataOutStream.flush();

                String clientMsg;
                while ((clientMsg = dataInStream.readLine()) != null) {
                    datagram = clientMsg.split(String.valueOf(PROTOCOL_SEP));

                    switch(Protocols.values()[Character.getNumericValue(datagram[1].charAt(0))]) { //"Casting" protocol id to an enum
                        //End connection to this client
                        case END_GAME -> {
                            outputEvent(clientMsg);
                            outputEvent("(Client: Initiated a disconnection request...)\n");
                            dataOutStream.println("Disconnected from host: " + serverSocket.getLocalSocketAddress() + "\n");
                            dataOutStream.flush();
                            try {
                                dataInStream.close();
                                dataOutStream.close();
                                clientSocket.close();
                            } catch(IOException ioe) {
                                System.err.println("Server: Something went wrong while disconnecting");
                                ioe.printStackTrace();
                            }

                            userRecordTable.remove(datagram[0]); //Remove user
                            Set<String> users = userRecordTable.keySet();
                            numOfClients = users.size();
                            outputEvent("Disconnected from client " + datagram[0] + "\n");

                            //End server if no clients are left
                            if(finalize && numOfClients == 0) {
                                outputEvent("There are no more clients left. Quiting server...\n");
                                serverOn = false;

                                //Close server socket
                                try {
                                    serverSocket.close();
                                } catch (IOException e) {
                                    System.err.println("Unable to close one or more streams/sockets");
                                    e.printStackTrace();
                                }

                                System.out.println("Ended server");
                                dispose();
                                System.exit(0);
                            }
                        }
                        //Receive client game configuration
                        case SEND_GAME-> {
                            String config = datagram[2];
                            outputEvent(clientMsg);
                            outputEvent("(Client: Sent configuration string - " + config + ")\n");
                            outputEvent("Saving configuration...\n");

                            /* TODO: Turns out that this was unecesarry for the assignment. Only one string needs to be stored globally for all users. No need to keep a record in hash map

                            //Update stored configuration associated with user
                            String[] oldClientData = userRecordTable.get(datagram[0]).split(String.valueOf(DATA_SEP));
                            StringBuilder sb = new StringBuilder(config);
                            sb.append(DATA_SEP);
                            if(oldClientData.length > 1) //Client data may not exist
                                sb.append(oldClientData[1]);
                            userRecordTable.replace(datagram[0], userRecordTable.get(datagram[0]), sb.toString());
                            */

                            globalConfig = datagram[2];
                            outputEvent("Configuration saved\n");
                        }
                        //Send client game configuration
                        case RECEIVE_GAME -> {
                            outputEvent(clientMsg);
                            outputEvent("(Client: Requesting configuration string)\n");
                            outputEvent("Sending sequence to client...\n");

                            /* TODO: Once again, unecesarry
                            String config = userRecordTable.get(datagram[0]).split(String.valueOf(DATA_SEP))[0];
                            dataOutStream.println(config);
                            */

                            String returnDatagram = "";
                            StringBuilder sb = new StringBuilder(returnDatagram);
                            sb.append(datagram[0]).append(PROTOCOL_SEP).append(GameClient.Protocols.RECEIVE_GAME.getValue()).append(PROTOCOL_SEP).append(globalConfig);

                            dataOutStream.println(sb);
                            dataOutStream.flush();
                            outputEvent("Sequence sent");
                        }
                        //Receive client data eg. name, score, and time
                        case SEND_DATA -> {
                            String gameData = datagram[2];
                            outputEvent(clientMsg);
                            outputEvent("(Client: Sent game data - " + gameData + ")\n");
                            outputEvent("Saving data...\n");

                            //Update stored data associated with user
                            String[] oldClientData = userRecordTable.get(datagram[0]).split(String.valueOf(DATA_SEP));
                            StringBuilder sb = new StringBuilder(oldClientData[0]);
                            sb.append(DATA_SEP).append(gameData);
                            userRecordTable.replace(datagram[0], userRecordTable.get(datagram[0]), sb.toString());
                            outputEvent("Data saved\n");
                        }
                    }
                }
            } catch (IOException ioe) {
                //Ignore when client disconnects
            }
        }
    }

    /**
     * @since 2021-11-28
     *
     * ServerSplash is in charge of creating the splash screen for the server
     * GUI.
     */
    private void serverSplash() {

        int clientSplashBkgrndWidth = 538;
        int clientSplashBkgrndHeight = 120;

        JFrame jf = new JFrame("Neil Kingdom - Picross Server");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        JPanel splashPanel = new JPanel();
        JPanel controlPanel = new JPanel();
        JPanel logPanel = new JPanel();

        /* ---------------------- Resource Config -------------------- */

        try {
            bufServerSplash = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossServerSplash.png"));
            serverSplashIcon = new ImageIcon(bufServerSplash.getScaledInstance(clientSplashBkgrndWidth, clientSplashBkgrndHeight, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Could not load image: \"/res/img/PicrossServerSplash.png\"");
        }

        //serverSplashIcon error handling
        if (serverSplashIcon != null)
            splashLabel = new JLabel(serverSplashIcon);
        else
            splashLabel = new JLabel("Missing Icon");
        splashLabel.setPreferredSize(new Dimension(clientSplashBkgrndWidth, clientSplashBkgrndHeight));

        /* ---------------------- Initialize Components -------------------- */

        JLabel portLabel = new JLabel("Port: ");
        JTextField portField = new JTextField();
        JCheckBox checkFinal = new JCheckBox("Finalize");

        JButton bExecute = new JButton("Execute");
        JButton bResults = new JButton("Results");
        JButton bEnd = new JButton("End");

        dataLog = new JTextPane();
        JScrollPane dataScroll = new JScrollPane(dataLog);

        /* ---------------------- Configure Components -------------------- */

        portField.setPreferredSize(new Dimension(60, 20));

        portField.setText("8080");
        int v_scrollPolicyEnum = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        int h_scrollPolicyEnum = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
        dataScroll.setVerticalScrollBarPolicy(v_scrollPolicyEnum);
        dataScroll.setHorizontalScrollBarPolicy(h_scrollPolicyEnum);
        dataLog.setPreferredSize(new Dimension(clientSplashBkgrndWidth, clientSplashBkgrndHeight));
        dataLog.setEditable(false);

        /* ---------------------- Action Listeners -------------------- */

        bExecute.addActionListener(actionEvent -> {
            if (portField.getText().isEmpty()) {
                outputEvent("Please enter a port number to connect to before attempting to execute the server...\n");
            } else {
                bExecute.setEnabled(false);

                portNum = Integer.valueOf(portField.getText());

                outputEvent("Beginning execution\n");
                outputEvent("port = " + portNum + "\n");
                outputEvent("Waiting for clients to connect...\n");

                connectToClient();
            }
        });

        bResults.addActionListener(actionEvent -> {
            JDialog scoreBoard = new JDialog();
            //Not a very elegant way of checking that no scores exist but it works
            Enumeration clients = userRecordTable.elements();
            boolean isEmpty = true;
            while(clients.hasMoreElements()) {
                if(!clients.nextElement().equals("")) {
                    isEmpty = false;
                    break;
                }
            }

            if(isEmpty) {
                JOptionPane.showMessageDialog(scoreBoard, "No scores to display :(");
            }
            else {
                clients = userRecordTable.elements();
                String entry = "";
                StringBuilder sb = new StringBuilder(entry);

                while(clients.hasMoreElements()) {

                    String rawData = clients.nextElement().toString();
                    String[] data = rawData.split(String.valueOf(DATA_SEP));

                    if(data.length > 1) {
                        String clientData = data[1];

                        String[] dataFields = clientData.split(",");

                        sb.append("Username: " + dataFields[0] + ", ");
                        sb.append("Time: " + dataFields[1] + ", ");
                        sb.append("Score: " + dataFields[2] + "\n");
                    }
                }
                JOptionPane.showMessageDialog(scoreBoard, sb.toString());
            }
        });

        checkFinal.addActionListener(actionEvent -> {
            finalize = (finalize) ? false : true;
        });

        bEnd.addActionListener(actionEvent -> {
            JDialog endGame = new JDialog();

            Object[] options = {"Yes", "No"};
            int n = JOptionPane.showOptionDialog(endGame, "Are you sure you'd like to end the connection?\n"
                            + "All clients will be disconnected...", "End server hosting",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

            if (n == 0) {
                serverOn = false;

                //Close all sockets and streams
                try {
                    if (clientSocket != null)
                        clientSocket.close();
                    if (serverSocket != null)
                        serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Unable to close one or more streams/sockets");
                    e.printStackTrace();
                }

                System.out.println("Server ended");
                endGame.dispose();
                dispose();
                System.exit(0);
            }
        });

        /* ---------------------- Add Components -------------------- */

        splashPanel.add(splashLabel);
        controlPanel.add(portLabel);
        controlPanel.add(portField);
        controlPanel.add(bExecute);
        controlPanel.add(bResults);
        controlPanel.add(checkFinal);
        controlPanel.add(bEnd);
        logPanel.add(dataScroll);

        mainPanel.add(splashPanel);
        mainPanel.add(controlPanel);
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
     * A thread is created which calls .accept() in a for loop. The .accept()
     * method is a blocking method, which means that the code is effectively
     * halted until a client request is recieved. For each new client, a new
     * thread is started so that the server can communicate with multiple
     * clients simultaneously
     * */
    public boolean connectToClient() {

        //Perpetually accept new client connections (must be on new thread so that server can still perform other operations)
        Thread listenThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(portNum);

                while (serverOn) {
                    try {
                        clientSocket = serverSocket.accept(); //Blocking function
                        outputEvent("Current number of clients: " + (++numOfClients) + "\n");
                        ClientThread cliThread = new ClientThread(clientSocket);
                        cliThread.start(); //Create new thread for client
                    } catch (IOException ioe) {
                        errorPopup("Connection Error", "Failed to connect to client");
                    }
                }

            } catch (IOException e) {
                errorPopup("Error", "Could not create server socket on port " + portNum);
            }
        });
        listenThread.start();

        return true;
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
     * @param clientID UUID of the client
     * @param clientData Data supplied by the client
     *
     * Adds a new entry to the userRecordTable (HashMap)
     * */
    public void addUserRecord(String clientID, String clientData) {
        userRecordTable.put(clientID, clientData);
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
     *
     * Secondary main function which gets called if the
     * user supplies the command line argument 's'
     * */
    public static void main(String[] args) {
        //Start GUI
        SwingUtilities.invokeLater(() -> new GameServer());
    }
}
