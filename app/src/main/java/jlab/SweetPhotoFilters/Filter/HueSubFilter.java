package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Javier on 7/12/2020.
 */

public class HueSubFilter extends JlabSubFilter {


    public HueSubFilter() {
        setTag("Hue");
    }

    @Override
    public Bitmap process(Bitmap source) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        float[] HSV = new float[3];
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // convert to HSV
                Color.colorToHSV(pixels[index], HSV);
                // increase Saturation level
                HSV[0] *= 2;
                HSV[0] = (float) Math.max(0.0, Math.min(HSV[0], 360f));
                // take color back
                pixels[index] |= Color.HSVToColor(HSV);
            }
        }
        source.setPixels(pixels, 0, width, 0, 0, width, height);
        return source;
    }
}
