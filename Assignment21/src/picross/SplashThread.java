package picross;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static picross.Game.MIN_WIN_HEIGHT;
import static picross.Game.MIN_WIN_WIDTH;

public class SplashThread extends Thread {

    private GameView gameView;

    public SplashThread(GameView gameView) {
        this.gameView = gameView;
    }

    @Override
    public void run() {

        long start = System.currentTimeMillis();
        int labelHeight = gameView.getGameTextLabel().getPreferredSize().height;
        int labelWidth = gameView.getGameTextLabel().getPreferredSize().width;

        Vec2D P0 = new Vec2D();
        Vec2D P1 = new Vec2D();
        Vec2D P2 = new Vec2D();
        Vec2D P3 = new Vec2D();
        Vec2D P0Rand = new Vec2D();
        Vec2D P1Rand = new Vec2D();
        Vec2D P2Rand = new Vec2D();
        Vec2D P3Rand = new Vec2D();
        Vec2D resultant = new Vec2D();

        //Calculate 3 initial random points
        P0Rand.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);
        P1Rand.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);
        P2Rand.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);
        P3Rand.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);

        //TODO: I don't think timer works properly...
        //Every 10 seconds generate new random positions
        Timer genNewCoords = new Timer(10000, e -> {

            System.out.println("Working");
            //P3 is the new start position
            P0.setVec2D(P3);
            P1.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);
            P2.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);
            P3.genRandVec2D(MIN_WIN_WIDTH, MIN_WIN_HEIGHT);
        });
        genNewCoords.start();

        //Note: Timers are not designed for heavy calculations. Slowdown is expected to occur
        Timer frames = new Timer(1000/25, e -> {

            /* ---------------------- TextLabel Calculations -------------------- */

            long stop = System.currentTimeMillis();
            long deltaT = Math.abs(start-stop);
            double normalizedScale = (0.15 * Math.cos((double)(deltaT)/300)) + 1.5;
            Dimension newDimension = new Dimension((int)(normalizedScale * labelWidth), (int)(normalizedScale * labelHeight));
            gameView.getGameTextLabel().setIcon(gameView.resizeImageIcon(newDimension, gameView.bufStartGameText));

            /* ---------------------- BackgroundImg Calculations -------------------- */

            //Reset Vectors
            P0.setVec2D(P0Rand);
            P1.setVec2D(P1Rand);
            P2.setVec2D(P2Rand);
            P3.setVec2D(P3Rand);
            resultant.setVec2D(new Vec2D());

            //int nDigits = String.valueOf(deltaT).length();
            //Scale vectors by weights
            ArrayList<Float> w = Vec2D.calcWeights((float) ((deltaT*0.0001) % 1.5));
            P0.scaleVec2D(w.get(0));
            P1.scaleVec2D(w.get(1));
            P2.scaleVec2D(w.get(2));
            P3.scaleVec2D(w.get(3));

            //Scale vectors by velocity
            ArrayList<Float> vel = Vec2D.calcVelocity((float) ((deltaT*0.0001) % 1.5));
            P0.scaleVec2D(vel.get(0));
            P1.scaleVec2D(vel.get(1));
            P2.scaleVec2D(vel.get(2));
            P3.scaleVec2D(vel.get(3));

            //Scale vectors by acceleration
//            ArrayList<Float> accel = Vec2D.calcVelocity((float) ((deltaT*0.0001) % 1.5));
//            P0.scaleVec2D(accel.get(0));
//            P1.scaleVec2D(accel.get(1));
//            P2.scaleVec2D(accel.get(2));
//            P3.scaleVec2D(accel.get(3));

            //Add vectors
            resultant.addVec2D(P0);
            resultant.addVec2D(P1);
            resultant.addVec2D(P2);
            resultant.addVec2D(P3);

            //Hinder Acceleration
            resultant.scaleVec2D(0.001f);
            //System.out.println("Resultant - " + resultant);

            //Reposition screen
            gameView.moveBkgrndFromOrigin(gameView.getSplashBackground(), resultant);
        });
        frames.start();
    }
}
