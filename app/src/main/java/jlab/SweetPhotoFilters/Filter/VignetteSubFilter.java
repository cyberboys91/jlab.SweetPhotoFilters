package jlab.SweetPhotoFilters.Filter;

import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.LinearGradient;

/**
 * Created by Javier on 7/12/2020.
 */

public class VignetteSubFilter extends JlabSubFilter {

    public VignetteSubFilter () {
        setTag("Vignette");
    }

    @Override
    public Bitmap process(Bitmap inputImage) {
        final int width = inputImage.getWidth();
        final int height = inputImage.getHeight();
        final Canvas canvas = new Canvas(inputImage);
        canvas.drawBitmap(inputImage, 0, 0, null);

        final int tenthLeftRight = width / 5;
        final int tenthTopBottom = height / 5;

        // Gradient left - right
        final Shader linGradLR = new LinearGradient(0, height / 2, tenthLeftRight / 2, height / 2,
                Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        // Gradient top - bottom
        final Shader linGradTB = new LinearGradient(width / 2, 0, width / 2, tenthTopBottom, Color.BLACK,
                Color.TRANSPARENT, Shader.TileMode.CLAMP);
        // Gradient right - left
        final Shader linGradRL = new LinearGradient(width, height / 2, (width - tenthLeftRight),
                height / 2, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        // Gradient bottom - top
        final Shader linGradBT = new LinearGradient(width / 2, height, width / 2,
                (height - tenthTopBottom), Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);

        final Paint paint = new Paint();
        paint.setShader(linGradLR);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setAlpha(125);
        // Rect for Grad left - right
        Rect rect = new Rect(0, 0, tenthLeftRight, height);
        RectF rectf = new RectF(rect);
        canvas.drawRect(rectf, paint);

        // Rect for Grad top - bottom
        paint.setShader(linGradTB);
        rect = new Rect(0, 0, width, tenthTopBottom);
        rectf = new RectF(rect);
        canvas.drawRect(rectf, paint);

        // Rect for Grad right - left
        paint.setShader(linGradRL);
        rect = new Rect(width, 0, width - tenthLeftRight, height);
        rectf = new RectF(rect);
        canvas.drawRect(rectf, paint);

        // Rect for Grad bottom - top
        paint.setShader(linGradBT);
        rect = new Rect(0, height - tenthTopBottom, width, height);
        rectf = new RectF(rect);
        canvas.drawRect(rectf, paint);

        return inputImage;
    }
}
