package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Vector;

/**
 * Created by Javier on 7/12/2020.
 */

public class MeanRemovalSubFilter extends JlabSubFilter {

    public MeanRemovalSubFilter() {
        setTag("MeanRemoval");
    }

    @Override
    public Bitmap process(Bitmap input) {
        double[][] MeanRemovalConfig = new double[][] {
                { -1 , -1, -1 },
                { -1 ,  9, -1 },
                { -1 , -1, -1 }
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(MeanRemovalConfig);
        convMatrix.Factor = 1;
        convMatrix.Offset = 0;
        return ConvolutionMatrix.computeConvolution3x3(input, convMatrix);
    }
}
