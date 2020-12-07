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

public class JlabTwoSubFilter extends JlabSubFilter {

    public JlabTwoSubFilter() {
        this.tag = "JlabTwoSubFilter";
    }
    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final ScriptIntrinsicColorMatrix colorMatrix2=ScriptIntrinsicColorMatrix.create(renderScript,Element.U8_4(renderScript));
        colorMatrix2.setGreyscale();
        colorMatrix2.forEach(inputAllocation,outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
