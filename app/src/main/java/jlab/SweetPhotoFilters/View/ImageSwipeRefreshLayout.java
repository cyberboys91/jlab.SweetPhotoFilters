package jlab.SweetPhotoFilters.View;

import android.content.Context;
import android.view.MotionEvent;
import android.util.AttributeSet;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Created by Javier on 01/12/2020.
 */

public class ImageSwipeRefreshLayout extends SwipeRefreshLayout {

    public ImageSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ImageSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}