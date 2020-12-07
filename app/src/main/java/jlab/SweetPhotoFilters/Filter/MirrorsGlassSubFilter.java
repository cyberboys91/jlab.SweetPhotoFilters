package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

/**
 * Created by Javier on 7/12/2020.
 */

public class MirrorsGlassSubFilter extends JlabSubFilter {
    private static final int GAP = 4;

    @Override
    public Bitmap process(Bitmap inputImage) {

        final int width = inputImage.getWidth();
        final int height = inputImage.getHeight();

        final Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);

        final Bitmap mirroredImage = Bitmap.createBitmap(inputImage, 0,
                height / 2, width, height / 2, matrix, false);
        final Bitmap fullImage = Bitmap.createBitmap(width, (height + height / 2),
                Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(fullImage);
        canvas.drawBitmap(inputImage, 0, 0, null);

        final Paint paint = new Paint();
        canvas.drawRect(0, height, width, height + GAP, paint);

        canvas.drawBitmap(mirroredImage, 0, height + GAP, null);

        final Paint paint2 = new Paint();
        final LinearGradient lgrad = new LinearGradient(0, inputImage
                .getHeight(), 0, fullImage.getHeight() + GAP, 0x70ffffff,
                0x00ffffff, Shader.TileMode.CLAMP);
        paint2.setShader(lgrad);
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height, width, fullImage.getHeight() + GAP, paint2);
        mirroredImage.recycle();

        return fullImage;
    }
}
