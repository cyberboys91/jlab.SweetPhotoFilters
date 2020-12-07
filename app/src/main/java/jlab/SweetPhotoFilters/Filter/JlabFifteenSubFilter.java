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

public class JlabFifteenSubFilter extends JlabSubFilter {

    public JlabFifteenSubFilter() {
        this.tag = "JlabFifteenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix13 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix13.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {

                        2.10279132254252f,  -0.298212630531356f,       0.42128146417712f,   0f,
                        0.222897572029231f,     1.68701190285368f,     -0.883421304780577f,   0f,
                        -0.765688894571747f,   0.171200727677677f,    2.02213984060346f,     0f,
                        0                          ,  0                 ,0                ,1f


                }));
        colorMatrix13.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
