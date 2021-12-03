package picross;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

import static picross.Game.*;
import static picross.Game.Y_START_POS;

public class GameClient extends JFrame {

    private static int portNum;
    private static String clientID;
    private static String userName;
    private static String serverIP;
    private static JTextArea dataLog;
    private static JLabel splashLabel;

    protected static BufferedImage bufClientSplash;
    protected static ImageIcon clientSplashIcon;

    private GameClient() {
        portNum = 0;
        clientID = "";
        userName = "";
        serverIP = "";
        clientSplash();
    }

    /**
     * @since 2021-11-28
     *
     * Splash is in charge of creating the splash screen for the main
     * game. It uses a separate JFrame than the main game, which uses
     * a KeyListener to dispose itself and begin the main game.
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

        dataLog = new JTextArea();
        JScrollPane dataScroll = new JScrollPane(dataLog);

        /* ---------------------- Configure Components -------------------- */

        userField.setPreferredSize(new Dimension(80, 20));
        serverField.setPreferredSize(new Dimension(80, 20));
        portField.setPreferredSize(new Dimension(60, 20));

        int v_scrollPolicyEnum = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        int h_scrollPolicyEnum = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
        dataScroll.setVerticalScrollBarPolicy(v_scrollPolicyEnum);
        dataScroll.setHorizontalScrollBarPolicy(h_scrollPolicyEnum);
        dataLog.setPreferredSize(new Dimension(clientSplashBkgrndWidth, clientSplashBkgrndHeight));
        dataLog.setEditable(false);

        /* ---------------------- Action Listeners -------------------- */

        bConnect.addActionListener(actionEvent -> {
            if(userField.getText().isEmpty()) {
                dataLog.append("Please enter your user name before attempting to connect to the server...\n");
            }
            if(serverField.getText().isEmpty()) {
                dataLog.append("Please enter the IP address of the server you're trying to connect to before pressing connect...\n");
            }
            if(portField.getText().isEmpty()) {
                dataLog.append("Please enter the port that you are attempting to connect to before pressing connect...\n");
            }
            else {
                bConnect.setEnabled(false);

                userName = userField.getText();
                serverIP = serverField.getText();
                portNum = Integer.valueOf(portField.getText());

                dataLog.append("Creating new MVC game...\n");

                connectToHost();
            }
        });

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

    public boolean connectToHost() {

        final Socket clientSocket;
        final DataInputStream dataInStream;
        final DataOutputStream dataOutStream;

        try {
            clientSocket = new Socket(serverIP, portNum);
            dataInStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream())); //Receive input as a stream of bits
            dataOutStream = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

            //Client send thread
            Thread send = new Thread(() -> {
                try {
                    dataOutStream.writeUTF("Hello from client!");
                } catch (IOException e) {
                    System.err.println("Error writing UTF chars");
                    e.printStackTrace();
                }
                try {
                    dataOutStream.flush();
                } catch (IOException e) {
                    System.err.println("Error flushing dataOutStream");
                    e.printStackTrace();
                }
            });
            send.start();

            //Client receive thread
            Thread receive = new Thread(() -> {
                try {
                    dataLog.append("Connection with " + serverIP + " on port " + portNum + "\n");

                    try {
                        String serverMsg = dataInStream.readUTF();
                        while (serverMsg != null) {
                            dataLog.append("Received message from server: " + serverMsg + "\n");
                            serverMsg = dataInStream.readUTF();
                        }

                        clientID = serverMsg;

                    } catch(EOFException e) {
                        System.err.println("EOF reached in input stream");
                        e.printStackTrace();
                    }

                    dataLog.append("Connection to server lost...\n");
                    dataInStream.close();
                    dataOutStream.close();
                    clientSocket.close();
                } catch(IOException e) {
                    System.err.println("IOExeption while reading input stream");
                    e.printStackTrace();
                }
            });
            receive.start();

        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void genDatagram() {

        //Protocol: <client_id><separator><protocol_id><separator>{data}
        //Data: <user_name><separator><config_string><separator><points><separator><time>

        final char SEPARATOR = '%';
    }

    public static void main(String[] args) {
        //Start GUI
        SwingUtilities.invokeLater(() -> new GameClient());
    }
}
