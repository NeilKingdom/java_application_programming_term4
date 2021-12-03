package picross;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import static picross.Game.X_START_POS;
import static picross.Game.Y_START_POS;

public class GameServer extends JFrame {

    private static int portNum;
    private static int numOfClients;
    private static JTextArea dataLog;
    private static JLabel splashLabel;
    private static Hashtable<String, ArrayList<Object>> userRecordTable;

    protected static BufferedImage bufServerSplash;
    protected static ImageIcon serverSplashIcon;

    private GameServer() {
        portNum = 0;
        numOfClients = 0;
        userRecordTable = new Hashtable<>(100);
        serverSplash();
    }

    /**
     * @since 2021-11-28
     * <p>
     * Splash is in charge of creating the splash screen for the main
     * game. It uses a separate JFrame than the main game, which uses
     * a KeyListener to dispose itself and begin the main game.
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
        JCheckBox finalize = new JCheckBox("Finalize");

        JButton bExecute = new JButton("Execute");
        JButton bResults = new JButton("Results");
        JButton bEnd = new JButton("End");

        dataLog = new JTextArea();
        JScrollPane dataScroll = new JScrollPane(dataLog);

        /* ---------------------- Configure Components -------------------- */

        portField.setPreferredSize(new Dimension(60, 20));

        int v_scrollPolicyEnum = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
        int h_scrollPolicyEnum = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
        dataScroll.setVerticalScrollBarPolicy(v_scrollPolicyEnum);
        dataScroll.setHorizontalScrollBarPolicy(h_scrollPolicyEnum);
        dataLog.setPreferredSize(new Dimension(clientSplashBkgrndWidth, clientSplashBkgrndHeight));
        dataLog.setEditable(false);

        /* ---------------------- Action Listeners -------------------- */

        bExecute.addActionListener(actionEvent -> {
            if(portField.getText().isEmpty()) {
                dataLog.append("Please enter a port number to connect to before attempting to execute the server...\n");
            }
            else {
                bExecute.setEnabled(false);

                portNum = Integer.valueOf(portField.getText());

                dataLog.append("Beginning execution\n");
                dataLog.append("port = " + portNum + "\n");
                dataLog.append("Waiting for clients to connect...\n");

                connectToClient();
            }
        });

        /* ---------------------- Add Components -------------------- */

        splashPanel.add(splashLabel);
        controlPanel.add(portLabel);
        controlPanel.add(portField);
        controlPanel.add(bExecute);
        controlPanel.add(bResults);
        controlPanel.add(finalize);
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

    public boolean connectToClient() {

        final ServerSocket serverSocket;
        final Socket clientSocket;
        final DataInputStream dataInStream;
        final DataOutputStream dataOutStream;

        try {
            serverSocket = new ServerSocket(portNum);
            clientSocket = serverSocket.accept();
            dataInStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            dataOutStream = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

            //Server send thread
            Thread send = new Thread(() -> {

                //We want to generate a unique identifier (UUID) for each client
                UUID clientUUID = UUID.randomUUID();
                String clientID = String.valueOf(clientUUID);
                addUserRecord(clientID);

                try {
                    dataOutStream.writeUTF(clientID);
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

            //Server receive thread
            Thread receive = new Thread(() -> {
                try {
                    dataLog.append("Current number of clients: " + (++numOfClients) + "\n");

                    try {
                        String bits = dataInStream.readUTF();
                        while (bits != null) {
                            dataLog.append("Received message from client: " + bits + "\n");
                            bits = dataInStream.readUTF();
                        }
                    } catch(EOFException e) {
                        System.err.println("EOF reached in input stream");
                        e.printStackTrace();
                    }

                    dataOutStream.close();
                    dataInStream.close();
                    clientSocket.close();
                    serverSocket.close();
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

    public void addUserRecord(String clientID) {
        ArrayList<Object> data = new ArrayList<>();
        userRecordTable.put(clientID, data);
    }

    public static void main(String[] args) {
        //Start GUI
        SwingUtilities.invokeLater(() -> new GameServer());
    }
}
