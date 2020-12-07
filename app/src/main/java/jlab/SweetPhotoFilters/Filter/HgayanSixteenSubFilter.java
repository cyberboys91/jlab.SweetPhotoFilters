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

public class HgayanSixteenSubFilter extends HgayanSubFilter {

    public HgayanSixteenSubFilter() {
        this.tag = "HgayanSixteenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix14 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix14.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {

                        1.27488526960083f, -0.228511311848763f,   0.441088688151237f,  0,
                        0.323664244263542f,  0.955140825713134f,  -0.705935755736458f,  0,
                        -0.698549513864371f, 0.173370486135629f ,  1.16484706758522f  ,0,
                        0,0,0,1


                }));
        colorMatrix14.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
