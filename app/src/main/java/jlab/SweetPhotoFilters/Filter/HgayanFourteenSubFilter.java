package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import jlab.SweetPhotoFilters.Utils;

/**
 * Created by Javier on 7/12/2020.
 */

public class HgayanFourteenSubFilter extends HgayanSubFilter {

    public HgayanFourteenSubFilter() {
        this.tag = "HgayanFourteenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicConvolve3x3 convolve2 = ScriptIntrinsicConvolve3x3.create(renderScript, Element.U8_4(renderScript));
        convolve2.setInput(inputAllocation);
        convolve2.setCoefficients(new float[]
                {
                        .2f, .3f, .2f, .1f, .1f, .1f, .2f, .3f, .2f,

                });
        convolve2.forEach(outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
