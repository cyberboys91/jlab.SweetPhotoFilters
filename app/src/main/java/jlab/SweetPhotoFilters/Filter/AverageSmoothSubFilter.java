package jlab.SweetPhotoFilters.Filter;

import android.graphics.Color;
import android.graphics.Bitmap;
import com.zomato.photofilters.imageprocessors.SubFilter;

/*
 * Created by Javier on 29/11/2020.
 */

public class AverageSmoothSubFilter implements SubFilter {
    private static String tag = "Block";
    private int maskSize;

    public AverageSmoothSubFilter(int maskSize) {
        this.maskSize = maskSize;
    }

    @Override
    public Bitmap process(Bitmap inputImage) {
        int sumR, sumG, sumB,
                width = inputImage.getWidth(),
                height = inputImage.getHeight();
        int div = maskSize * maskSize, halfMaskSize = maskSize / 2;
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);
        for(int row = halfMaskSize; row < height - halfMaskSize; row++) {
            for(int col = halfMaskSize; col < width - halfMaskSize; col++){
                sumR = sumG = sumB = 0;
                for (int m = -halfMaskSize; m <= halfMaskSize; m++) {
                    for (int n = -halfMaskSize; n <= halfMaskSize; n++) {
                        int index = (row + m) * width + col + n;
                        if (index < width * height) {
                            int color = pixels[index];
                            sumR += Color.red(color);
                            sumG += Color.green(color);
                            sumB += Color.blue(color);
                        }
                    }
                }
                pixels[row * width + col] = Color.rgb(sumR / div, sumG / div, sumB / div);
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
        AverageSmoothSubFilter.tag = (String) tag;
    }
}
