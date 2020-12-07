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

public class HgayanFiveSubFilter extends HgayanSubFilter {

    public HgayanFiveSubFilter() {
        this.tag = "HgayanFiveSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final  ScriptIntrinsicColorMatrix colorMatrix5 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix5.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        -0.359705309629158f, 0.377252728606377f, 0.663841667303255f, 0f,
                        1.56680818833214f, 0.456668209492391f, 1.12613917506705f, 0f,
                        -0.147102878702981f, 0.226079061901232f, -0.729980842370303f, 0f,
                        0f, 0f, 0f, 1f
                }));
        colorMatrix5.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
