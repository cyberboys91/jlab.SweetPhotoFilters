package jlab.SweetPhotoFilters.Filter;

import android.graphics.Bitmap;
import com.zomato.photofilters.imageprocessors.SubFilter;

/**
 * Created by Javier on 7/12/2020.
 */

public class JlabSubFilter implements SubFilter {
    protected String tag = "JlabSubFilter";
    @Override
    public Bitmap process(Bitmap inputImage) {
        return null;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = (String) tag;
    }
}
