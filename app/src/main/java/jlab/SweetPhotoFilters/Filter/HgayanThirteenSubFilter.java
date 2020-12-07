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

public class HgayanThirteenSubFilter extends HgayanSubFilter {

    public HgayanThirteenSubFilter() {
        this.tag = "HgayanThirteenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicConvolve3x3 convolve1 = ScriptIntrinsicConvolve3x3.create(renderScript, Element.U8_4(renderScript));
        convolve1.setInput(inputAllocation);
        convolve1.setCoefficients(new float[]
                {
                        -2, -1, 0,
                        -1, 1, 1,
                        0, 1, 2
                });
        convolve1.forEach(outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
