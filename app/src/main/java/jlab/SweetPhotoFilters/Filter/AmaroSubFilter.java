/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;

/**
 * Created by mordonez on 1/14/14.
 */
public class AmaroSubFilter extends Filter {

    @Override
    public Bitmap process(Bitmap inputImage) {
        width = inputImage.getWidth();
        height = inputImage.getHeight();
        int[] pixels = new int[width * height];
        inputImage.getPixels(pixels, 0, width, 0, 0, width, height);

        mColors = getLevelsFilter().filter(pixels, width, height);
        levels = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);

        Bitmap gradient = createRadialGradient();

        return combineGrandientAndImage(gradient, levels, PorterDuff.Mode.OVERLAY);
    }

    private CurvesFilter getLevelsFilter(){
        /*This method contains Photoshop values, since values are 0 to 1 range we need to
        find the value based on the scale (that's the reason of for loops).*/

        final CurvesFilter curvesFilter = new CurvesFilter();

        Curve[] curves = new Curve[3];
        Curve r = new Curve();
        Curve g = new Curve();
        Curve b = new Curve();

        curves[0] = r;
        curves[1] = g;
        curves[2] = b;

        r.y = new float[] {26.0f, 43.f, 64f, 114f, 148f, 177f, 193f, 202f, 208f, 218f, 229f, 241f, 251f};
        r.x = new float[] {0f, 15f, 32f, 65f, 83f, 109f, 127f, 146f, 170f, 195f, 215f, 234f, 255f };

        for(int i = 0 ;i<r.x.length;i++) {
            r.x[i] = (r.x[i]*100)/255/100;
            r.y[i] = (r.y[i]*100)/255/100;
        }

        g.y = new float[] {0f, 32f, 72f, 123f, 147f, 188f, 205f, 210f, 222f, 224f, 235f, 246f, 255f };
        g.x = new float[] {0f, 26f, 49f, 72f, 89f, 115f, 147f, 160f, 177f, 189f, 215f, 234f,255f };

        for(int i = 0 ;i<g.x.length;i++) {
            g.x[i] = (g.x[i]*100)/255/100;
            g.y[i] = (g.y[i]*100)/255/100;
        }

        b.y = new float[] {29f, 72f, 124f, 147f, 162f, 175f, 184f, 189f, 195f, 203f, 216f, 237f, 247f};
        b.x = new float[] {1f, 30f, 57f, 74f, 87f, 108f, 130f, 152f, 172f, 187f, 215f, 239f, 255f};

        for(int i = 0 ;i<b.x.length;i++) {
            b.x[i] = (b.x[i]*100)/255/100;
            b.y[i] = (b.y[i]*100)/255/100;
        }

        curvesFilter.setCurves(curves);
        return curvesFilter;
    }


}
