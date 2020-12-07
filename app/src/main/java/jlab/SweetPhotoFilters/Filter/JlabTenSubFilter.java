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

public class JlabTenSubFilter extends JlabSubFilter {

    public JlabTenSubFilter() {
        this.tag = "JlabTenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final   ScriptIntrinsicColorMatrix colorMatrix10 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix10.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        1f, 0f, 0.1f, -0.1f,
                        0f, 1f, 0.2f, 0f,
                        0f, 0f, 1.3f, 0f,
                        0f, 0f, 0f, 1

                }));
        colorMatrix10.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
