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

public class JlabElevenSubFilter extends JlabSubFilter {

    public JlabElevenSubFilter() {
        this.tag = "JlabElevenSubFilter";
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        RenderScript renderScript = RenderScript.create(Utils.currentActivity);
        Allocation inputAllocation = Allocation.createFromBitmap(renderScript, bitmap),
                outputAllocation=Allocation.createTyped(renderScript,inputAllocation.getType());
        final   ScriptIntrinsicColorMatrix colorMatrix11 = ScriptIntrinsicColorMatrix.create(renderScript, Element.U8_4(renderScript));
        colorMatrix11.setColorMatrix(new android.renderscript.Matrix4f(new float[]
                {
                        1.72814708519562f, -0.412104992562475f, 0.541145007437525f, 0f,
                        0.289378264402959f, 1.18835534216106f, -1.17637173559704f, 0f,
                        -1.01752534959858f, 0.223749650401417f, 1.63522672815952f, 0f,
                        0f, 0f, 0f, 1f


                }));
        colorMatrix11.forEach(inputAllocation, outputAllocation);
        outputAllocation.copyTo(bitmap);
        return bitmap;
    }
}
