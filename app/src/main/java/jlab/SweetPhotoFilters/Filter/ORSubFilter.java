package jlab.SweetPhotoFilters.Filter;
/*
 * Created by Javier on 29/11/2020.
 */

import android.graphics.Color;
import android.graphics.Bitmap;
import com.zomato.photofilters.imageprocessors.SubFilter;

public class ORSubFilter implements SubFilter {
    private static String tag = "OR";

    @Override
    public Bitmap process(Bitmap inputImage) {
        int width = inputImage.getWidth(), height = inputImage.getHeight();
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);
        int newR, newG, newB;
        for (int index = 0; index < pixels.length; index++) {
            int color = pixels[index];
            newR = Color.red(color) | 32;
            newG = Color.green(color) | 32;
            newB = Color.blue(color) | 32;
            pixels[index] = Color.rgb(newR, newG, newB);
        }
        inputImage.setPixels(pixels, 0, width, 0, 0, width, height);
        return inputImage;
    }

    @Override
    public Object getTag() {
        return tag;
    }

    @Override
    public void setTag(Object tag) {
        ORSubFilter.tag = (String) tag;
    }
}