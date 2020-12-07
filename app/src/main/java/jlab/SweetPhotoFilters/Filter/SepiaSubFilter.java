package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Vector;

/**
 * Created by Javier on 7/12/2020.
 */

public class SepiaSubFilter extends JlabSubFilter {

    private int intensity = 0;

    private final static int depth = 20;

    public SepiaSubFilter() {
        setTag("Sepia");
    }

    @Override
    public Bitmap process(Bitmap input) {

        final int width = input.getWidth();
        final int height = input.getHeight();
        int red, green, blue, grey;
        final int[] pixels = new int[width * height];
        input.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {

            red = Color.red(pixels[i]);
            green = Color.green(pixels[i]);
            blue = Color.blue(pixels[i]);

            grey = (red + green + blue) / 3;
            red = green = blue = grey;
            red = red + (depth * 2);
            green += depth;

            if (red > 255)
                red = 255;
            if (green > 255)
                green = 255;
            if (blue > 255)
                blue = 255;

            blue -= intensity;

            if (blue > 255)
                blue = 255;
            if (blue < 0)
                blue = 0;

            pixels[i] = Color.argb(Color.alpha(pixels[i]), red, green, blue);
        }
        input.setPixels(pixels, 0, width, 0, 0, width, height);
        return input;
    }

    /**
     * Blend arrays.
     *
     * @param pixels    the pixels
     * @param pixelsHue the pixels hue
     * @param value     the value
     */
    private void blendArrays(int[] pixels, int[] pixelsHue, double value) {

        int r1, r2, r, g1, g2, g, b1, b2, b;

        for (int i = 0; i < pixels.length; i++) {
            r1 = Color.red(pixels[i]);
            r2 = Color.red(pixelsHue[i]);
            r = (int) (r1 * value + r2 * (1.0 - value));

            g1 = Color.green(pixels[i]);
            g2 = Color.green(pixelsHue[i]);
            g = (int) (g1 * value + g2 * (1.0 - value));

            b1 = Color.blue(pixels[i]);
            b2 = Color.blue(pixelsHue[i]);
            b = (int) (b1 * value + b2 * (1.0 - value));

            pixels[i] = Color.argb(0xFF, r, g, b);
        }
    }

    /**
     * Increase saturation.
     *
     * @param hsv the hsv
     */
    private void increaseSaturation(Vector<float[]> hsv) {
        for (float[] hsvPixel : hsv) {
            hsvPixel[1] *= 1.2;
        }
    }

    /**
     * Adjust channels.
     *
     * @param pixels the pixels
     */
    private void adjustChannels(int[] pixels) {
        int red, green, blue;
        for (int i = 0; i < pixels.length; i++) {
            red = Color.red(pixels[i]);
            red *= 0.9;
            if (red > 240)
                red = 240;

            green = Color.green(pixels[i]);
            if (green > 123)
                green *= 1.05;
            if (green > 255)
                green = 255;

            blue = Color.blue(pixels[i]);
            if (blue < 125) {
                blue *= 1.15;
            } else if (blue >= 125) {
                blue *= 0.85;
            }
            if (blue > 255)
                blue = 255;

            pixels[i] = Color.argb(Color.alpha(pixels[i]), red, green, blue);
        }
    }

    /**
     * Adjust hue and alpha.
     *
     * @param pixels    the pixels
     * @param pixelsHue the pixels hue
     */
    private void adjustHueAndAlpha(int[] pixels, int[] pixelsHue) {

        final Vector<float[]> pixelsHueHsv = new Vector<>();
        for (int pixel : pixels) {
            float[] hsv = {0.0f, 0.0f, 0.0f};
            Color.RGBToHSV(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsv);
            pixelsHueHsv.add(hsv);
        }

        float[] pixelHSV;
        for (int i = 0; i < pixelsHueHsv.size(); i++) {
            pixelHSV = pixelsHueHsv.get(i);
            pixelHSV[0] = 50.0f;
            pixelHSV[1] = 0.25f;
            pixelsHueHsv.set(i, pixelHSV);
        }

        for (int i = 0; i < pixelsHueHsv.size(); i++) {
            pixelsHue[i] = Color.HSVToColor(0xFF, pixelsHueHsv.get(i));
        }
    }
}
