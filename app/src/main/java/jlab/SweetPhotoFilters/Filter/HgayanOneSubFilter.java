package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import jlab.SweetPhotoFilters.Utils;
import android.renderscript.Element;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicColorMatrix;

/**
 * Created by Javier on 7/12/2020.
 */

public class HgayanOneSubFilter extends HgayanSubFilter {

    public HgayanOneSubFilter() {
        this.tag = "HgayanOneSubFilter";
    }
    @Override
    public Bitmap process(Bitmap inputImage) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, inputImage),
                outputAllocation = Allocation.createTyped(renderScript, inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix1=ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix1.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        -0.33f, -0.33f, -0.33f, 1.0f,
                        -0.59f, -0.59f, -0.59f, 1.0f,
                        -0.11f, -0.11f, -0.11f, 1.0f,
                        1.0f, 1.0f, 1.0f, 1.0f
                }));
        colorMatrix1.forEach(inputAllocation,outputAllocation);
        outputAllocation.copyTo(inputImage);
        return inputImage;
    }
}
