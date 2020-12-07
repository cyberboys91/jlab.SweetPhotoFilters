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

public class HgayanThreeSubFilter extends HgayanSubFilter {

    public HgayanThreeSubFilter() {
        this.tag = "HgayanThreeSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix3 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix3.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        0f, 0f, 0f, 0f,
                        0f, 0.78f, 0f, 0f,
                        0f, 0f, 1f, 0f,
                        0f, 0f, 0f, 1f,
                }));
        colorMatrix3.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
