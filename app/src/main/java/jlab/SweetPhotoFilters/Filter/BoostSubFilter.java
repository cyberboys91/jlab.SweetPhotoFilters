package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Javier on 7/12/2020.
 */

public class BoostSubFilter extends JlabSubFilter {

    private int type = 1; //2 //3
    private int percent = 40; //30 // 67

    public BoostSubFilter() {
        setTag("Boost");
    }

    @Override
    public Bitmap process(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int A, R, G, B;
        int pixel;

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                if(type == 1) {
                    R = R * (1 + percent);
                    if(R > 255) R = 255;
                }
                else if(type == 2) {
                    G = G * (1 + percent);
                    if(G > 255) G = 255;
                }
                else if(type == 3) {
                    B = B * (1 + percent);
                    if(B > 255) B = 255;
                }
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }
}
