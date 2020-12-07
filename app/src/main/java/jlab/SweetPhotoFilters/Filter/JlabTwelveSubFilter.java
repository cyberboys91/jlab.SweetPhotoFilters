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

public class JlabTwelveSubFilter extends JlabSubFilter {

    public JlabTwelveSubFilter() {
        this.tag = "JlabTwelveSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix12 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix12.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        .309f, .409f, .309f, 0f,
                        .609f, .309f, .409f, 0f,
                        0.42f, .42f, .2f, 0f,
                        0f, 0f, 0f, 1f


                }));
        colorMatrix12.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
