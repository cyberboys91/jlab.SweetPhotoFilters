package jlab.SweetPhotoFilters.View;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.os.Handler;
import jlab.SweetPhotoFilters.R;
import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.widget.AdapterView;

import jlab.SweetPhotoFilters.Resource.AlbumDirectory;
import jlab.SweetPhotoFilters.Resource.Directory;
import jlab.SweetPhotoFilters.Resource.FileResource;
import jlab.SweetPhotoFilters.Utils;
import jlab.SweetPhotoFilters.Interfaces;
import jlab.SweetPhotoFilters.Resource.Resource;
import jlab.SweetPhotoFilters.Activity.DirectoryActivity;

import static java.lang.Math.abs;
import static jlab.SweetPhotoFilters.Activity.DirectoryActivity.fromPoint;
import static jlab.SweetPhotoFilters.Activity.DirectoryActivity.iconSize;
import static jlab.SweetPhotoFilters.Utils.LOADING_VISIBLE;
import static jlab.SweetPhotoFilters.Utils.REFRESH_LISTVIEW;
import static jlab.SweetPhotoFilters.Utils.TIME_WAIT_LOADING;
import static jlab.SweetPhotoFilters.Utils.favoriteDbManager;
import static jlab.SweetPhotoFilters.Utils.stackVars;

/*
 * Created by Javier on 26/9/2016.
 */

public class GridDirectoryView extends GridView implements Interfaces.IListContent, AbsListView.OnScrollListener {

    private ScaleGestureDetector mScaleDetector;
    private int first, antFirst, last;
    public boolean scrolling = false, up;
    protected Handler handler = new Handler();
    protected Directory mdirectory;
    protected String relUrlDirectoryRoot, nameDirectoryRoot;
    protected ResourceDetailsAdapter mAdapter;
    protected DirectoryActivity mListener;

    public GridDirectoryView(Context context) {
        super(context);
        mAdapter = new ResourceDetailsAdapter();
        setAdapter(mAdapter);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public GridDirectoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new ResourceDetailsAdapter();
        setAdapter(mAdapter);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public GridDirectoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new ResourceDetailsAdapter();
        setAdapter(mAdapter);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void loadItemClickListener() {
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getAnimation() == null || getAnimation().hasEnded())
                    openResource(mdirectory.getResource(position), position, new Point((int) view.getX(), (int) view.getY()));
            }
        });
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                return (getAnimation() == null || getAnimation().hasEnded())
                        && mListener.onResourceLongClick(mdirectory.getResource(position),
                        position, new Point((int) view.getX(), (int) view.getY()));
            }
        });
        setOnScrollListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        try {
            if (child != null) {
                ImageView ivIcon = child.findViewById(R.id.ivResourceIcon);
                if (ivIcon != null)
                    ivIcon.setImageDrawable(null);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setRelUrlDirectoryRoot(String nameRoot, String relUrlRoot) {
        this.relUrlDirectoryRoot = relUrlRoot;
        this.nameDirectoryRoot = nameRoot;
    }

    public void loadParentDirectory() {
        if (!stackVars.isEmpty()) {
            Utils.Variables var = stackVars.remove(stackVars.size() - 1);
            fromPoint = var.posFather;
            loadDirectory();
        }
    }

    @Override
    public int getFirstVisiblePosition() {
        return first;
    }

    @Override
    public int getLastVisiblePosition() {
        return last;
    }

    @Override
    public boolean scrolling() {
        return scrolling;
    }

    @Override
    public ResourceDetailsAdapter getResourceDetailsAdapter() {
        return this.mAdapter;
    }

    public void loadDirectory() {
        if (stackVars.isEmpty()) {
            Utils.stackVars.add(new Utils.Variables(Utils.RELURL_SPECIAL_DIR, getContext().getString(R.string.albums_folder), 0));
            mListener.onDirectoryClick(nameDirectoryRoot, relUrlDirectoryRoot);
        }

        Utils.Variables vars = stackVars.get(stackVars.size() - 1);
        mdirectory = mListener.getDirectory(vars.NameDirectory, vars.RelUrlDirectory);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TIME_WAIT_LOADING);
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
                synchronized (Directory.monitor) {
                    if (!mdirectory.loaded())
                        handler.sendEmptyMessage(LOADING_VISIBLE);
                }
            }
        }).start();
        mAdapter.clear();
        synchronized (Directory.monitor) {
            handler.sendEmptyMessage(REFRESH_LISTVIEW);
        }
        mdirectory.openAsynchronic(handler);
    }

    @Override
    public void loadContent() {
        mAdapter.clear();
        mAdapter.addAll(mdirectory.getContent());
    }

    public final boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    public Directory getDirectory() {
        return mdirectory;
    }

    @Override
    public void setDirectory(Directory directory) {
        this.mdirectory = directory;
    }

    @Override
    public void openResource(Resource res, int index, Point position) {
        scrolling = false;
        try {
            Utils.Variables var = stackVars.get(stackVars.size() - 1);
            var.BeginPosition = getFirstVisiblePosition();
            if (res.isDir()) {
                int posYFirstView = (int) abs(getChildAt(0).getY());
                stackVars.add(new Utils.Variables(res.getRelUrl(), res.getName(), 0,
                        new Point(position.x, position.y + (posYFirstView % iconSize))));
                loadDirectory();
                mListener.onDirectoryClick(res.getName(), res.getRelUrl(), index, position);
            } else
                mListener.onFileClick((FileResource) res, index, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mdirectory != null && mdirectory.loaded()) {
            scrollingStop(absListView);
            scrolling = false;
        }
        scrolling = scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        antFirst = first != firstVisibleItem ? first : antFirst;
        first = firstVisibleItem;
        last = firstVisibleItem + visibleItemCount - 1;
        up = antFirst < first;
    }

    private void scrollingStop(AbsListView view) {
        try {
            int length = getChildCount();
            int index = up ? first : last;
            for (int i = up ? 0 : length - 1; (up ? i < length : i >= 0)
                    && (up ? index <= last : index >= 0); i += up ? 1 : -1) {
                Resource elem = mdirectory.getResource(index);
                index += up ? 1 : -1;
                if (!elem.isDir() && ((FileResource) elem).isThumbnailer()) {
                    View child = view.getChildAt(i);
                    mListener.loadThumbnailForFile((FileResource) elem,
                            (ImageView) child.findViewById(R.id.ivResourceIcon),
                            (ImageView) view.getChildAt(i).findViewById(R.id.ivFavorite),true, false);
                }
                else if (elem instanceof AlbumDirectory && ((AlbumDirectory) elem).getCountElements() > 0)
                    mListener.loadThumbnailForFile((FileResource) ((AlbumDirectory) elem).getResource(0),
                            (ImageView) view.getChildAt(i).findViewById(R.id.ivResourceIcon),
                            (ImageView) view.getChildAt(i).findViewById(R.id.ivFavorite),true, true);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    @Override
    public View getView() {
        return this;
    }

    public void setListeners(DirectoryActivity activityDirectory) {
        this.mAdapter.setonGetSetViewListener(activityDirectory);
        this.mListener = activityDirectory;
    }

    @Override
    public void setNumColumns(final int numColumns) {
        int col = getNumColumns();
        if (col != numColumns)
            super.setNumColumns(numColumns);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            int newColumns = detector.getScaleFactor() < 1
                    ? favoriteDbManager.incrementNumColumns()
                    : favoriteDbManager.decremtNumColuns();
            if (!stackVars.isEmpty())
                stackVars.get(stackVars.size() - 1).BeginPosition = getFirstVisiblePosition();
            setNumColumns(newColumns);
            mListener.refreshConfiguration();
            loadDirectory();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.getScaleFactor() != 1 && (getAnimation() == null
                    || getAnimation().hasEnded()))
                startAnimation(AnimationUtils.loadAnimation(getContext(),
                        detector.getScaleFactor() < 1
                                ? R.anim.beat_increment_col
                                : R.anim.beat_decrement_col));
            return super.onScale(detector);
        }
    }
}