package picross;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Neil Kingdom
 * @since 2021-10-3
 * @version 1.0
 *
 * GameSplash creates the splash screen for the Picross game.
 * It uses Thread.sleep to prolong the time it's active before disposing itself.
 * */

public class GameSplash extends JFrame {

    private final int T_AUTOSTART;
    private static final int MIN_WIN_WIDTH = (Toolkit.getDefaultToolkit().getScreenSize().width/3);
    private static final int MIN_WIN_HEIGHT = (Toolkit.getDefaultToolkit().getScreenSize().height/5);

    /**
     * @author Neil Kingdom
     * @since 2021-10-09
     * @version 1.0
     * @param tAutostart - Time in seconds before frame is disposed
     *
     * Parameterized constructor for GameSplash
     * */
    public GameSplash(int tAutostart) {
        T_AUTOSTART = tAutostart*1000;
        initSplash();
    }

    public void initSplash() {

        JPanel splash = new JPanel();
        splash.setMinimumSize(new Dimension(MIN_WIN_WIDTH, MIN_WIN_HEIGHT));

        BufferedImage bufSplash;
        ImageIcon splashIcon = null;

        try {
            bufSplash = ImageIO.read(this.getClass().getResourceAsStream("/res/img/PicrossSplash.jpg"));
            splashIcon = new ImageIcon(bufSplash.getScaledInstance(MIN_WIN_WIDTH, MIN_WIN_HEIGHT, Image.SCALE_SMOOTH));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load image: \"/res/img/PicrossSplash.jpg\"");
        }

        JLabel splashBackground;
        if(splashIcon != null)
            splashBackground = new JLabel(splashIcon);
        else
            splashBackground = new JLabel("Missing Image");
        splashBackground.setPreferredSize(new Dimension(MIN_WIN_WIDTH, MIN_WIN_HEIGHT));

        splash.add(splashBackground, BorderLayout.CENTER);
        add(splash);
        pack();
        setVisible(true);

        // kill splash screen after T_AUTOSTART seconds
        try {
            Thread.sleep(T_AUTOSTART);
            dispose();
        } catch(InterruptedException e) {
            System.err.println("Sleep thread was interrupted");
            dispose();
            System.exit(-1);
        }
    }
}
