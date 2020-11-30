package jlab.SweetPhotoFilters.Filter;

import android.graphics.Color;
import android.graphics.Bitmap;
import com.zomato.photofilters.imageprocessors.SubFilter;

import static java.lang.Math.pow;

/*
 * Created by Javier on 29/11/2020.
 */

public class GammaCorrectionSubFilter implements SubFilter {
    private static String tag = "GammaCorrection";
    private int[] gammaTable;
    private final int GAMMA_TABLE_SIZE = 256;
    private final float COLOR_UPPER_BOUND = 255f;
    private float gamma;

    public GammaCorrectionSubFilter (float gamma) {
        this.gamma = gamma;
        this.gammaTable = new int[GAMMA_TABLE_SIZE];
        initGammaTable();
    }

    private void initGammaTable() {
        float inverseGamma = 1f / gamma;
        for (int i = 0; i < GAMMA_TABLE_SIZE; i++)
            gammaTable[i] = (int) (pow((double) (i / COLOR_UPPER_BOUND), inverseGamma) * COLOR_UPPER_BOUND);
    }

    @Override
    public Bitmap process(Bitmap inputImage) {
        int width = inputImage.getWidth(),
                height = inputImage.getHeight();
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width * height; i++) {
            int color = pixels[i];
            int r = gammaTable[Color.red(color)];
            int g = gammaTable[Color.green(color)];
            int b = gammaTable[Color.blue(color)];
            pixels[i] = Color.rgb(r, g, b);
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
        GammaCorrectionSubFilter.tag = (String) tag;
    }
}
