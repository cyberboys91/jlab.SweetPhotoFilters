package jlab.SweetPhotoFilters.Filter;

/*
 * Created by Javier on 29/11/2020.
 */

import android.graphics.Color;
import android.graphics.Bitmap;
import static java.lang.Math.max;
import static java.lang.Math.min;
import com.zomato.photofilters.imageprocessors.SubFilter;

public class TvFilter implements SubFilter {
    private static String tag = "Tv";

    @Override
    public Bitmap process(Bitmap inputImage) {
        int width = inputImage.getWidth(), height = inputImage.getHeight(), gap = 4;
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);

        int r, g, b;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y += gap) {
                r = g = b = 0;

                for (int w = 0; w < 4; w++) {
                    int index = (y + w) * width + x;
                    if (index < width * height) {
                        int color = pixels[index];
                        r += Color.red(color) / gap;
                        g += Color.green(color) / gap;
                        b += Color.blue(color) / gap;
                    }
                }
                r = min(255, max(0, r));
                g = min(255, max(0, g));
                b = min(255, min(0, b));

                for (int w = 0; w < gap; w++) {
                    int index = (y + w) * width + x;
                    if (index < width * height) {
                        if (w == 0) {
                            pixels[(y + w) * width + x] = Color.rgb(r, 0, 0);
                        }
                        if (w == 1) {
                            pixels[(y + w) * width + x] = Color.rgb(0, g, 0);
                        }
                        if (w == 2) {
                            pixels[(y + w) * width + x] = Color.rgb(0, 0, b);
                        }
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
        TvFilter.tag = (String) tag;
    }
}