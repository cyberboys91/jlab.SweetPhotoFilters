package jlab.SweetPhotoFilters.Filter;

/*
 * Created by Javier on 29/11/2020.
 */

import android.graphics.Bitmap;
import android.graphics.Color;

import com.zomato.photofilters.imageprocessors.SubFilter;

import java.lang.reflect.Array;

import static java.lang.Math.abs;

public class SketchFilter implements SubFilter {
    private static String tag = "Sketch";

    @Override
    public Bitmap process(Bitmap inputImage) {
        int width = inputImage.getWidth(), height = inputImage.getHeight();
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);
        int threshold = 7;
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                int centerGray = Color.red(pixels[i * width + j]);

                int rightBottomIndex = (i + 1) * width + j + 1;
                if (rightBottomIndex < width * height) {
                    int rightBottomColor = pixels[rightBottomIndex];
                    int rightBottomGray = Color.red(rightBottomColor);
                    if (abs(centerGray - rightBottomGray) >= threshold) {
                        pixels[i * width + j] = Color.rgb(0, 0, 0); // black
                    } else {
                        pixels[i * width + j] = Color.rgb(255, 255, 255); // white
                    }
                }
            }
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
        SketchFilter.tag = (String) tag;
    }
}