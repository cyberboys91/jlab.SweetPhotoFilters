package jlab.SweetPhotoFilters.Filter;

/*
 * Created by Javier on 29/11/2020.
 */

import android.graphics.Color;
import android.graphics.Bitmap;
import com.zomato.photofilters.imageprocessors.SubFilter;

public class PixelateFilter implements SubFilter {
    private static String tag = "Pixelate";

    @Override
    public Bitmap process(Bitmap inputImage) {
        int width = inputImage.getWidth(), height = inputImage.getHeight();
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);
        int pixelSize = 3;
        for (int i = 0; i < width; i += pixelSize) {
            for (int j = 0; j < height; j += pixelSize) {
                int rectColor = getRectColor(i, j, width, height, pixels, pixelSize);
                fillRectColor(rectColor, i, j, width, height, pixels, pixelSize);
            }
        }
        inputImage.setPixels(pixels, 0, width, 0, 0, width, height);
        return inputImage;
    }

    private int getRectColor(int col, int row, int width, int height, int[] pixels, int pixelSize) {
        int r = 0, g = 0, b = 0, sum;
        for (int x = col; x < col + pixelSize; x++) {
            for (int y = row; y < row + pixelSize; y++) {
                int index = x + y * width;
                if (index < width * height) {
                    int pixColor = pixels[x + y * width];
                    r += Color.red(pixColor);
                    g += Color.green(pixColor);
                    b += Color.blue(pixColor);
                }

            }
        }
        sum = pixelSize * pixelSize;
        return Color.rgb(r / sum, g / sum, b / sum);
    }

    private void fillRectColor(int color, int col, int row, int width, int height, int[] pixels, int pixelSize) {
        for (int x = col; x < col + pixelSize; x++) {
            for (int y = row; y < row + pixelSize; y++) {
                int index = x + y * width;
                if (x < width && y < height && index < width * height) {
                    pixels[x + y * width] = color;
                }

            }
        }
    }

    @Override
    public Object getTag() {
        return tag;
    }

    @Override
    public void setTag(Object tag) {
        PixelateFilter.tag = (String) tag;
    }
}