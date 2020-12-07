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

public class JlabSevenSubFilter extends JlabSubFilter {

    public JlabSevenSubFilter() {
        this.tag = "JlabSevenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final  ScriptIntrinsicColorMatrix colorMatrix7 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix7.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        1.22994596833595f, 0.0209523774645382f, 0.383244054685119f, 0f,
                        0.450138899443543f, 1.18737418804171f, -0.106933249401007f, 0f
                        - 0.340084867779496f, 0.131673434493755f, 1.06368919471589f, 0f,
                        0f, 0f, 0f,
                        11.91f, 11.91f, 11.91f, 0f}));
        colorMatrix7.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
