package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.Random;

/**
 * Created by Javier on 7/12/2020.
 */

public class FleaSubFilter extends JlabSubFilter {

    public static final int COLOR_MAX = 0xFF;

    public FleaSubFilter() {
        setTag("Flea");
    }

    @Override
    public Bitmap process(Bitmap source) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        // a random object
        Random random = new Random();

        int index;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // get random color
                int randColor = Color.rgb(random.nextInt(COLOR_MAX),
                        random.nextInt(COLOR_MAX), random.nextInt(COLOR_MAX));
                // OR
                pixels[index] |= randColor;
            }
        }
        source.setPixels(pixels, 0, width, 0, 0, width, height);
        return source;
    }
}
