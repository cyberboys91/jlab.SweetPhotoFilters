package jlab.SweetPhotoFilters.Filter;
/*
 * Created by Javier on 29/11/2020.
 */

import android.graphics.Bitmap;
import android.graphics.Color;

import com.zomato.photofilters.imageprocessors.SubFilter;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class LightSubFilter implements SubFilter {

    private static String tag = "Light";

    @Override
    public Bitmap process(Bitmap inputImage) {
        int width = inputImage.getWidth(), height = inputImage.getHeight();
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);

        int pixR, pixG, pixB,newR, newG, newB,
                centerX = width / 2, centerY = height / 2,
                radius = (int) max(width / 1.5f, height / 1.5f);

        float strength = 150;
        int pos;
        for (int i = 1; i < height - 1; i++) {
            for (int k = 1; k < width - 1; k++) {
                pos = i * width + k;
                if (pos < width * height) {
                    int pixelColor = pixels[pos];

                    pixR = Color.red(pixelColor);
                    pixG = Color.green(pixelColor);
                    pixB = Color.blue(pixelColor);

                    newR = pixR;
                    newG = pixG;
                    newB = pixB;

                    int distance = (int) (pow((centerY - i), 2) + pow(centerX - k, 2));
                    if (distance < radius * radius) {
                        int result = (int) (strength * (1.0 - sqrt(distance) / radius));
                        newR = pixR + result;
                        newG = pixG + result;
                        newB = pixB + result;
                    }
                    newR = min(255, max(0, newR));
                    newG = min(255, max(0, newG));
                    newB = min(255, max(0, newB));

                    pixels[pos] = Color.argb(255, newR, newG, newB);
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
        LightSubFilter.tag = (String) tag;
    }
}