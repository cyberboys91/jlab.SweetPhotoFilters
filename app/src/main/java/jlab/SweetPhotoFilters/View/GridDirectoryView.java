package jlab.SweetPhotoFilters.View;

<<<<<<< HEAD
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
import android.view.View;
import android.os.Handler;
import jlab.SweetPhotoFilters.R;
import android.content.Context;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.widget.AdapterView;
<<<<<<< HEAD
=======

import jlab.SweetPhotoFilters.Resource.AlbumDirectory;
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
import jlab.SweetPhotoFilters.Resource.Directory;
import jlab.SweetPhotoFilters.Resource.FileResource;
import jlab.SweetPhotoFilters.Utils;
import jlab.SweetPhotoFilters.Interfaces;
import jlab.SweetPhotoFilters.Resource.Resource;
import jlab.SweetPhotoFilters.Activity.DirectoryActivity;

/*
 * Created by Javier on 26/9/2016.
 */

public class GridDirectoryView extends GridView implements Interfaces.IListContent, AbsListView.OnScrollListener {

<<<<<<< HEAD
    private ScaleGestureDetector mScaleDetector;
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
    private int first, antFirst;
    public boolean scrolling = false;
    protected Handler handler = new Handler();
    protected Directory mdirectory;
    protected String relUrlDirectoryRoot, nameDirectoryRoot;
    protected ResourceDetailsAdapter mAdapter;
    protected DirectoryActivity mListener;

    public GridDirectoryView(Context context) {
        super(context);
        mAdapter = new ResourceDetailsAdapter();
        setAdapter(mAdapter);
<<<<<<< HEAD
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
    }

    public GridDirectoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new ResourceDetailsAdapter();
        setAdapter(mAdapter);
<<<<<<< HEAD
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
    }

    public GridDirectoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new ResourceDetailsAdapter();
        setAdapter(mAdapter);
<<<<<<< HEAD
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
    }

    public void loadItemClickListener() {
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               openResource(mdirectory.getResource(position), position);
            }
        });
        setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long id) {
                return mListener.onResourceLongClick(mdirectory.getResource(position), position);
            }
        });
        setOnScrollListener(this);
    }

    @Override
<<<<<<< HEAD
    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mdirectory != null && mdirectory.loaded()) {
            scrollingStop(absListView);
            scrolling = false;
        }
        scrolling = scrollState == SCROLL_STATE_FLING;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        antFirst = first != firstVisibleItem ? first : antFirst;
        first = firstVisibleItem;
    }

    private void scrollingStop(AbsListView view) {

    }

    @Override
    public void onViewRemoved(View child) {
        if(child != null) {
            ImageView ivIcon = (ImageView) child.findViewById(R.id.ivResourceIcon);
            if(ivIcon != null)
            ivIcon.setImageDrawable(null);
        }
        super.onViewRemoved(child);
    }

    public void setHandler(Handler handler) {
         this.handler = handler;
    }

    public void setRelUrlDirectoryRoot(String nameRoot, String relUrlRoot)
    {
        this.relUrlDirectoryRoot = relUrlRoot;
        this.nameDirectoryRoot = nameRoot;
    }

    public void loadParentDirectory() {
        if (!Utils.stackVars.isEmpty()) {
            Utils.stackVars.remove(Utils.stackVars.size() - 1);
            loadDirectory();
        }
    }

    @Override
    public int getFirstVisiblePosition() {
        return first;
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
        if (Utils.stackVars.isEmpty())
            mListener.onDirectoryClick(nameDirectoryRoot, relUrlDirectoryRoot);

        Utils.Variables vars = Utils.stackVars.get(Utils.stackVars.size() - 1);
        mdirectory = mListener.getDirectory(vars.NameDirectory, vars.RelUrlDirectory);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Utils.TIME_WAIT_LOADING);
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
                synchronized (Directory.monitor) {
                    if (!mdirectory.loaded())
                        handler.sendEmptyMessage(Utils.LOADING_VISIBLE);
                }
            }
        }).start();
        mAdapter.clear();
        synchronized (Directory.monitor) {
            handler.sendEmptyMessage(Utils.REFRESH_LISTVIEW);
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

    public Directory getDirectory()
    {
        return mdirectory;
    }

    @Override
    public void setDirectory(Directory directory) {
        this.mdirectory = directory;
    }

    @Override
    public void openResource(Resource res, int position) {
        scrolling = false;
        try {
            Utils.Variables var = Utils.stackVars.get(Utils.stackVars.size() - 1);
            var.BeginPosition = getFirstVisiblePosition();
            if (res.isDir()) {
                Utils.stackVars.add(new Utils.Variables(res.getRelUrl(), res.getName(), 0));
                loadDirectory();
                mListener.onDirectoryClick(res.getName(), res.getRelUrl());
            } else
                mListener.onFileClick((FileResource) res, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListeners(DirectoryActivity activityDirectory)
    {
        this.mAdapter.setonGetSetViewListener(activityDirectory);
        this.mListener = activityDirectory;
    }
<<<<<<< HEAD

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
//            setNumColumns(getNumColumns() + 1);
            return true;
        }
    }
=======
>>>>>>> bfcd75c0ca8134830d6fe14bd0803dfa931d3892
}