package jlab.SweetPhotoFilters.View;

/*
 * Created by Javier on 06/12/2020.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

import jlab.SweetPhotoFilters.Interfaces;
import jlab.SweetPhotoFilters.R;
import jlab.SweetPhotoFilters.Utils;

public class ImageGallery extends Gallery {
    private Interfaces.ILoadImageListener loadImageListener = new Interfaces.ILoadImageListener() {
        @Override
        public boolean loadImage() {
            return false;
        }
    };

    public ImageGallery(Context context) {
        super(context);
    }

    public ImageGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageGallery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLoadImageListener(Interfaces.ILoadImageListener loadImage) {
        this.loadImageListener = loadImage;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() != MotionEvent.ACTION_MOVE || !loadImageListener.loadImage())
            return super.onTouchEvent(event);
        return false;
    }
}
