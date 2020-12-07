package jlab.SweetPhotoFilters.Filter;

import android.graphics.Color;
import android.graphics.Bitmap;

/**
 * Created by Javier on 7/12/2020.
 */

public class BrownishSubFilter extends JlabSubFilter {

    public BrownishSubFilter() {
        setTag("Brownish");
    }

    @Override
    public Bitmap process(Bitmap input) {
        final int width = input.getWidth();
        final int height = input.getHeight();
        int alpha, red, green, blue, pixel;
        final int[] pixels = new int[width * height];
        input.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] hsv = new float[3];
        float hue, saturation, value;
        for (int i = 0; i < pixels.length; i++) {

            pixel = pixels[i];
            alpha = Color.alpha(pixel);
            red = Color.red(pixel);
            red *= 1.25;
            if (red > 255)
                red = 255;

            green = Color.green(pixel);

            blue = Color.blue(pixel);
            blue *= 0.5;

            Color.RGBToHSV(red, green, blue, hsv);
            hue = hsv[0];
            saturation = hsv[1];
            value = hsv[2];

            hue *= 0.7f;
            saturation *= 0.9f;
            value *= 0.85f;

            hsv[0] = hue;
            hsv[1] = saturation;
            hsv[2] = value;

            pixel = Color.HSVToColor(alpha, hsv);
            pixels[i] = pixel;
        }
        input.setPixels(pixels, 0, width, 0, 0, width, height);
        return input;
    }
}
