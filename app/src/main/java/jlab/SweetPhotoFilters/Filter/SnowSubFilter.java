package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Random;

/**
 * Created by Javier on 7/12/2020.
 */

public class SnowSubFilter extends JlabSubFilter {

    public static final int COLOR_MAX = 0xFF;

    public SnowSubFilter() {
        setTag("Snow");
    }

    @Override
    public Bitmap process(Bitmap source) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        // random object
        Random random = new Random();

        int R, G, B, index, thresHold;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // get color
                R = Color.red(pixels[index]);
                G = Color.green(pixels[index]);
                B = Color.blue(pixels[index]);
                // generate threshold
                thresHold = random.nextInt(COLOR_MAX);
                if(R > thresHold && G > thresHold && B > thresHold) {
                    pixels[index] = Color.rgb(COLOR_MAX, COLOR_MAX, COLOR_MAX);
                }
            }
        }
        source.setPixels(pixels, 0, width, 0, 0, width, height);
        return source;
    }
}
