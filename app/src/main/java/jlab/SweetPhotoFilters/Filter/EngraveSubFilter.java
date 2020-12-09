package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;

/**
 * Created by Javier on 7/12/2020.
 */

public class EngraveSubFilter extends JlabSubFilter {

    public EngraveSubFilter() {
        setTag("Engrave");
    }

    @Override
    public Bitmap process(Bitmap input) {
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.setAll(0);
        convMatrix.Matrix[0][0] = -2;
        convMatrix.Matrix[1][1] = 2;
        convMatrix.Factor = 1;
        convMatrix.Offset = 95;
        return ConvolutionMatrix.computeConvolution3x3(input, convMatrix);
    }
}
