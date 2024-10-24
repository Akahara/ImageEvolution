package fr.wonder.iev;

import java.util.List;
import java.util.Random;

public class Mathr {

    private static final Random random = new Random();
    public static final float PI = (float) Math.PI;
    public static final float TWOPI = 2*PI;

    public static int positionOfMostSignificantBit(int x) {
        if(x == 0)
            return -1;
        int p;
        for (p = 0; x != 0; p++)
            x >>= 1;
        return p;
    }

    public static float clamp(float x, float min, float max) { return Math.min(max, Math.max(min, x)); }

    public static float rand() {
        return random.nextFloat();
    }

    public static float fract(float f) {
        return mod(f, 1);
    }

    public static float mod(float f, float m) {
        f %= m;
        if(f < 0)
            f += m;
        return f;
    }

    private static class SinTableHolder {

        private static float[] sinTable = sinTable(360);

        public static float[] sinTable(int tableSize) {
            tableSize = (tableSize+1)/2*2;
            float[] table = new float[tableSize];
            // initialize the sine table
            for(int i = 0; i < tableSize/2; i++) {
                for(int n = 0; n < 7; n++) {
                    table[i] += pow(-1, n%2)*pow(i/(tableSize/2f)*PI, 2*n+1)/fact(2*n+1);
                }
                table[i+tableSize/2] = - table[i];
            }
            // correct absurd values to be in-bounds, these values can come
            // from the approximation of the sin function
            table[0] = 0f;
            table[tableSize/4] = 1f;
            table[tableSize/2] = 0f;
            table[tableSize*3/4] = -1f;
            return table;
        }
    }

    public static float pow(float base, float exp) {
        return (float) Math.pow(base, exp);
    }

    public static int fact(int i) {
        int l = 1;
        for(int j = 2; j <= i; j++)
            l *= j;
        return l;
    }

    public static float sin(float rad) {
        return SinTableHolder.sinTable[(int) mod(rad/PI*180f, 360)];
    }
    public static float cos(float rad) {
        return SinTableHolder.sinTable[(int) mod(rad/PI*180f+90f, 360)];
    }

    public static float rand1(float s) {
        return fract(sin(s*12.9898f) * 43758.5453f);
    }

    public static float rand2(float s) {
        return fract(sin(s*78.233f) * 43758.5453f);
    }

    public static int randSign() {
        return rand() < .5f ? -1 : +1;
    }

    public static float randSigned() {
        return 2*rand()-1;
    }

    public static float randAngle() {
        return rand()*TWOPI;
    }

    /**
     * Returns a random number between 0 and PI
     * @return a random angle in radians
     */
    public static float randHalfAngle() {
        return rand()*PI;
    }

    public static int randRange(int min, int max) {
        return (int) (rand()*(max-min)+min);
    }

    public static float randRange(float min, float max) {
        return rand()*(max-min)+min;
    }

    public static <T> T randIn(List<T> list) {
        return list.get(randRange(0, list.size()));
    }

}