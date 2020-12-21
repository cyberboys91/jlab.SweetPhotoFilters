package jlab.SweetPhotoFilters.Filter;
/*
 * Created by Javier on 11/12/2020.
 */

import android.graphics.Color;

import static java.lang.Math.abs;

public class OthersubFilter extends Filter {
    @Override
    public int[] filter(int[] src, int w, int h) {
        if (src.length > 2) {
            int pix0, pix1, aux_pix1, r0, g0, b0, a0,
             r1, g1, b1, a1, r2, g2, b2, a2 ;
            for (int j = 0; j < h; j++) {
                pix0 = src[j * w];
                for (int i = 1; i < w - 1; i++) {
                    aux_pix1 = src[j * w + i];
                    pix1 = src[j * w + i + 1];
                    r0 = Color.red(pix0);
                    g0 = Color.green(pix0);
                    b0 = Color.blue(pix0);
                    a0 = Color.alpha(pix0);
                    r1 = Color.red(pix1);
                    g1 = Color.green(pix1);
                    b1 = Color.blue(pix1);
                    a1 = Color.alpha(pix1);

                    r2 = Color.red(aux_pix1);
                    g2 = Color.green(aux_pix1);
                    b2 = Color.blue(aux_pix1);
                    a2 = Color.alpha(aux_pix1);
                    src[j * w + i] = Color.argb((a0 + a1 + a2) / 3, (r0 + r1 + r2) / 3, (g0 + g1 + g2) / 3, (b0 + b1 + b2) / 3);
                    pix0 = aux_pix1;
                }
            }
        }
        return src;
    }
}
