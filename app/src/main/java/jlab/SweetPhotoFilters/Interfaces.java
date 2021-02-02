package jlab.SweetPhotoFilters;

import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.content.ContentResolver;
import jlab.SweetPhotoFilters.Activity.*;
import jlab.SweetPhotoFilters.Filter.Filter;
import jlab.SweetPhotoFilters.Resource.*;
import jlab.SweetPhotoFilters.View.ResourceDetailsAdapter;

/*
 * Created by Javier on 30/07/2017.
 */
public class Interfaces {

    public interface ICloseListener {

        int getId();

        void close();

        void loaded(FileResource fsFile);
    }

    public interface IElementRefreshListener {
        void refresh(FileResource resource, int position, boolean isplaying);
    }

    public interface ILoadThumbnailForFile {
        void loadThumbnailForFile(FileResource file, ImageView ivIcon, ImageView ivFavorite, boolean setBackground, boolean isAlbum);
    }

    public interface IDetailsThumbnailerResource {
        Uri getUriThumbnails();

        int getColumnMicroKind();

        Bitmap getThumbnail(ContentResolver resolver, int id);
    }

    public interface IDetailsSpecialDirectory {

        int getCountElements();

        long getOcupedSpace();
    }

    public interface IGetDirectoryListener {
        Directory getDirectory(String name, String relUrl);
    }

    public interface IRemoteResourceClickListener {
        void onFileClick(FileResource res, int index, Point position);

        void onDirectoryClick(String name, String relurlDir, int index, Point position);

        void onDirectoryClick(String name, String relurlDir);

        boolean onResourceLongClick(Resource resource, int index, Point position);
    }

    public interface IListContent {

        void loadContent();

        boolean isEmpty();

        void setListeners(DirectoryActivity activityDirectory);

        void setRelUrlDirectoryRoot(String s, String string);

        void setHandler(Handler handler);

        int getFirstVisiblePosition();

        int getLastVisiblePosition();

        View getChildAt(int index);

        void loadDirectory();

        int getNumColumns();

        void loadItemClickListener();

        void setNumColumns(int i);

        void setSelection(int pos);

        void loadParentDirectory();

        boolean scrolling();

        ResourceDetailsAdapter getResourceDetailsAdapter();

        Directory getDirectory();

        void setDirectory(Directory directory);

        void openResource(Resource res, int index, Point position);

        void startAnimation(Animation animation);

        boolean post(Runnable runnable);

        View getView();

    }

    public interface IRefreshListener {
        void refresh();
    }

    public interface ICopyRefresh {
        void refresh(Runnable run);
    }

    public interface IAddResourceListener {
        void add(String name, String path, String comment, String album, long size, long modification);

        void add(Resource resource);

        void clear();
    }

    public interface ISelectListener {
        boolean select(String mimetype, String name, String path);
    }

    public interface ILoadImageListener {
        boolean loadImage();
    }

    public interface IPostOnSave {
        void run (String path, String name);
    }

    public interface IImageContent {
        void setImageBitmap (final Bitmap bm);
        void setFilter (Filter filter);
        void setLayoutParams(ViewGroup.LayoutParams params);
        ViewGroup.LayoutParams getLayoutParams();
    }
}
