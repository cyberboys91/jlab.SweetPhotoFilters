package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicColorMatrix;
import jlab.SweetPhotoFilters.Utils;

/**
 * Created by Javier on 7/12/2020.
 */

public class HgayanSixSubFilter extends HgayanSubFilter {

    public HgayanSixSubFilter() {
        this.tag = "HgayanSixSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix6 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix6.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        1.2f, 0.1f, 0.2f, 0.7f,

                        0.7f, 1f, 0f, -0.5f,
                        -0.7f, 0.2f, 0.5f, 1.3f,
                        0, -0.1f, 0f, 0.9f
                }));
        colorMatrix6.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
