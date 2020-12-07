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

public class HgayanEightSubFilter extends HgayanSubFilter {

    public HgayanEightSubFilter() {
        this.tag = "HgayanEightSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final  ScriptIntrinsicColorMatrix colorMatrix8 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix8.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        1.44f, 0f, 0f, 0f,
                        0f, 1.44f, 0f, 0f,
                        0f, 0f, 1.44f, 0f,
                        0f, 0f, 0f, 1f,


                }));
        colorMatrix8.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
