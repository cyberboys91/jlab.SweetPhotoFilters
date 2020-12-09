package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Random;

/**
 * Created by Javier on 7/12/2020.
 */

public class ShadingSubFilter extends JlabSubFilter {

    private int shadingColor = Color.CYAN; //Color.YELLOW //Color.GREEN

    public ShadingSubFilter() {
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

        int index;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // AND
                pixels[index] &= shadingColor;
            }
        }
        source.setPixels(pixels, 0, width, 0, 0, width, height);
        return source;
    }
}
