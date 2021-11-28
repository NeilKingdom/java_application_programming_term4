package picross;

import java.util.ArrayList;
import java.util.Random;

public class Vec2D {

    private float i;
    private float j;

    public Vec2D() {
        i = 0.0f;
        j = 0.0f;
    }

    public Vec2D(float i, float j) {
        this.i = i;
        this.j = j;
    }

    public void setVec2D(Vec2D newVec) {

        this.setI(newVec.getI());
        this.setJ(newVec.getJ());
    }

    public void addVec2D(Vec2D dest) {

        this.setI(this.getI() + dest.getI());
        this.setJ(this.getJ() + dest.getJ());
    }

    public void scaleVec2D(float scalar) {

        this.setI(this.getI() * scalar);
        this.setJ(this.getJ() * scalar);
    }

    //TODO: Very buggy. Sometimes values are 0
    public void genRandVec2D(int winWidth, int winHeight) {

        Random rand = new Random();
        this.setI(rand.nextFloat() * (winWidth/2) - winWidth);
        this.setJ(rand.nextFloat() * (winHeight/2) - winHeight);
    }

    public static ArrayList<Float> calcWeights(float t) {

        ArrayList<Float> w = new ArrayList<>(4);

        for(int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    w.add((float) (-Math.pow(t, 3) + 3 * Math.pow(t, 2) - 3 * t + 1));
                    break;
                case 1:
                    w.add((float) (3 * Math.pow(t, 3) - 6 * Math.pow(t, 2) + 3 * t));
                    break;
                case 2:
                    w.add((float) (-3 * Math.pow(t, 3) + 3 * Math.pow(t, 2)));
                    break;
                case 3:
                    w.add((float) (Math.pow(t, 3)));
                    break;
                default:
                    System.err.println("Don't know how I got here");
                    break;
            }
        }

        return w;
    }

    public static ArrayList<Float> calcVelocity(float t) {

        ArrayList<Float> vel = new ArrayList<>(4);

        for(int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    vel.add((float) (-3 * Math.pow(t, 2) + 6 * t - 3));
                    break;
                case 1:
                    vel.add((float) (9 * Math.pow(t, 2) - 12 * t + 3));
                    break;
                case 2:
                    vel.add((float) (-9 * Math.pow(t, 2) + 6 * t));
                    break;
                case 3:
                    vel.add((float) (3 * Math.pow(t, 2)));
                    break;
                default:
                    System.err.println("Don't know how I got here");
                    break;
            }
        }

        return vel;
    }

    public static ArrayList<Float> calcAcceleration(float t) {

        ArrayList<Float> accel = new ArrayList<>(4);

        for(int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    accel.add(-6 * t + 6);
                    break;
                case 1:
                    accel.add(18 * t - 12);
                    break;
                case 2:
                    accel.add(-18 * t + 6);
                    break;
                case 3:
                    accel.add(6 * t);
                    break;
                default:
                    System.err.println("Don't know how I got here");
                    break;
            }
        }

        return accel;
    }

    public void setI(float i) {
        this.i = i;
    }

    public void setJ(float j) {
        this.j = j;
    }

    public float getI() {
        return i;
    }

    public float getJ() {
        return j;
    }

    @Override
    public String toString() {
        return ("[i: " + this.getI() + ", j: " + this.getJ() + "]");
    }
}
