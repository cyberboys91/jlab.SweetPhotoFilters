package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;

/**
 * Created by Javier on 7/12/2020.
 */

public class SmoothSubFilter extends JlabSubFilter {

    private double value = 100;

    public SmoothSubFilter() {
        setTag("MeanRemoval");
    }

    @Override
    public Bitmap process(Bitmap input) {
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.setAll(1);
        convMatrix.Matrix[1][1] = value;
        convMatrix.Factor = value + 8;
        convMatrix.Offset = 1;
        return ConvolutionMatrix.computeConvolution3x3(input, convMatrix);
    }
}
