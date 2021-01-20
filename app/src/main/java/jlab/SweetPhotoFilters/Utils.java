package jlab.SweetPhotoFilters;

import java.io.File;

import android.net.Uri;
import android.os.Build;
import android.os.Parcel;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.View;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.os.Parcelable;
import android.graphics.Point;
import android.content.Intent;
import android.os.Environment;
import java.text.DecimalFormat;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.database.Cursor;
import android.content.Context;
import java.io.FileOutputStream;
import android.widget.ImageView;
import android.app.Notification;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.provider.MediaStore;
import android.media.ThumbnailUtils;
import android.content.ContentValues;

import java.util.concurrent.Semaphore;
import android.content.pm.PackageInfo;
import android.graphics.BitmapFactory;
import android.content.DialogInterface;
import android.content.ContentResolver;
import jlab.SweetPhotoFilters.Filter.*;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AlertDialog;
import android.media.MediaScannerConnection;
import android.media.MediaMetadataRetriever;
import com.google.android.material.snackbar.Snackbar;
import androidx.documentfile.provider.DocumentFile;
import android.graphics.drawable.BitmapDrawable;
import jlab.SweetPhotoFilters.Resource.Resource;
import jlab.SweetPhotoFilters.Resource.Directory;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import jlab.SweetPhotoFilters.db.FavoriteDbManager;
import jlab.SweetPhotoFilters.Resource.FileResource;
import com.zomato.photofilters.imageprocessors.Filter;
import jlab.SweetPhotoFilters.Resource.LocalStorageDirectories;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubFilter;

public class Utils {
    public static ArrayList<Variables> stackVars = new ArrayList<>();
    public static Activity currentActivity;
    public static View viewForSnack;
    public static String pathStorageDownload;
    public static final int LOADING_INVISIBLE = 0;
    public static final int LOADING_VISIBLE = 1;
    public static final int LOST_CONNECTION = 2;
    public static final int REFRESH_LISTVIEW = 3;
    public static final int SCROLLER_PATH = 5;
    private static final int THUMB_SIZE = 100;
    private static final int THUMB_WITH_MINI = 200;
    static final int THUMB_HEIGHT_MINI = 125;
    public static final int ALPHA_HIDDEN_FILES = 140;
    public static final int TIME_WAIT_LOADING = 500;
    public static final int MAX_PORT_VALUE = 65535;
    public static final int FIVE_SECONDS = 5000;
    public static final String EXTERNAL_VOLUMEN_NAME = "external";
    public static final String AUDIO = "audio";
    public static final String VIDEO = "video";
    public static final String FILE_SCHEME = "file";
    public static final String ID = "ID";
    public static final String STORAGE_DIRECTORY_PHONE = "/";
    public static final String NAME_REMOTE_DIRECTORY_ROOT = "/";
    public static final String FIRST_POSITION = "FIRST_POSITION";
    public static final String OPEN_RESOURCE_ON_CLICKED = "OPEN_RESOURCE_ON_CLICKED";
    public static final String INDEX_CURRENT_KEY = "jlab.IMAGEEXPLORER_INDEX_CURRENT_KEY";
    public static final String DIRECTORY_KEY = "jlab.IMAGEEXPLORER_DIRECTORY_KEY";
    public static final String RESOURCE_PATH_KEY = "jlab.IMAGEEXPLORER_RESOURCE_PATH_KEY";
    public static final String RELURL_SPECIAL_DIR = "special";
    public static final String RELURL_SEARCH = "search";
    public static final String NAME_SEARCH = ":s";
    public static final String PATH_KEY = "path";
    public static final String NAME_KEY = "name";
    public static final String PATTERN_KEY = "pattern";
    public static final String IS_REMOTE_DIRECTORY = "IS_REMOTE_DIRECTORY";
    public static final String HOST_SERVER_KEY = "jlab.IMAGEEXPLORER_HOST_SERVER_KEY";
    public static final String PORT_SERVER_KEY = "jlab.IMAGEEXPLORER_PORT_SERVER_KEY";
    public static final String RESOURCE_FOR_DELETE = "RESOURCE_FOR_DELETE";
    public static final String CLIPBOARD_RESOURCE_KEY = "CLIPBOARD_RESOURCE_KEY";
    public static final String IS_MOVING_RESOURCE_KEY = "IS_MOVING_RESOURCE_KEY";
    public static final String SERVER_DATA_KEY = "SERVER_DATA_KEY";
    public static final String RESOURCE_FOR_DETAILS_KEY = "RESOURCE_FOR_DETAILS_KEY";
    public static final String RELATIVE_URL_DIRECTORY_ROOT = "RELATIVE_URL_DIRECTORY_ROOT";
    public static boolean lostConnection = true;
    public static boolean showHiddenFiles;
    public static Resource clipboardRes;
    private static final String ANDROID_DATA_PATH = "/Android/data";
    private static Semaphore mutexAdd = new Semaphore(1);
    private static Semaphore mutexUpdate = new Semaphore(1);
    private static Semaphore mutexDelete = new Semaphore(1);
    private static Semaphore semaphoreLoadThumbnail = new Semaphore(4);
    private static CharSequence[] invalidChars = {"?", "|", "*", "\\", "/", "<", ">", ":", "\""};
    public static FavoriteDbManager favoriteDbManager;
    public static LocalStorageDirectories specialDirectories;
    public static String saveFolderPath = String.format("%s/SweetPhotoFilters", Environment.getExternalStorageDirectory());
    private static DocumentFile parentDir = DocumentFile.fromFile(new File(String.format("%s/SweetPhotoFilters"
            , Environment.getExternalStorageDirectory().getPath())))
            , rootDir = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
    private static Interfaces.IDetailsThumbnailerResource videoThumbnailerDetails = new Interfaces.IDetailsThumbnailerResource() {
        @Override
        public Uri getUriThumbnails() {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        @Override
        public int getColumnMicroKind() {
            return MediaStore.Video.Thumbnails.MINI_KIND;
        }

        @Override
        public Bitmap getThumbnail(ContentResolver resolver, int id) {
            return MediaStore.Video.Thumbnails.getThumbnail(resolver, id, getColumnMicroKind(), null);
        }
    };

    private static Interfaces.IDetailsThumbnailerResource imageThumbnailerDetails = new Interfaces.IDetailsThumbnailerResource() {
        @Override
        public Uri getUriThumbnails() {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        @Override
        public int getColumnMicroKind() {
            return MediaStore.Images.Thumbnails.MINI_KIND;
        }

        @Override
        public Bitmap getThumbnail(ContentResolver resolver, int id) {
            return MediaStore.Images.Thumbnails.getThumbnail(resolver, id, getColumnMicroKind(), null);
        }
    };

    public static String getSizeString(double mSize, int dec) {
        String type = "bytes";
        if (mSize == -1d)
            return "";
        if (mSize > 1024) {
            mSize /= 1024;
            type = "KB";
        }
        if (mSize > 1024) {
            mSize /= 1024;
            type = "MB";
        }
        if (mSize > 1024) {
            mSize /= 1024;
            type = "GB";
        }
        if (mSize > 1024) {
            mSize /= 1024;
            type = "TB";
        }
        StringBuilder format = new StringBuilder("###").append(dec > 0 ? "." : "");
        while (dec-- > 0)
            format.append("#");
        return new DecimalFormat(format.toString()).format(mSize) + " " + type;
    }

    public static boolean existAndMountDir(String path) {
        File dir = new File(path);
        return dir.isDirectory() && dir.exists() && dir.canRead() && dir.getTotalSpace() != 0;
    }

    public static boolean existAndMountDir(File dir) {
        return dir.isDirectory() && dir.exists() && dir.canRead() && dir.getTotalSpace() != 0;
    }

    public static void setBackground(ImageView ivIconRes, Bitmap bmBack, int resFront) {
        if (bmBack != null)
            ivIconRes.setImageBitmap(bmBack);
    }

    public static void setBackground(ImageView ivIconRes, int back, int resFront) {
        ivIconRes.setBackgroundResource(back);
        ivIconRes.setImageResource(resFront);
    }

    public static void setOnFavoriteClickListener(final ImageView ivfavorite, final FileResource file)
    {
        ivfavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.isFavorite())
                    file.setIsFavorite(false, Utils.deleteFavoriteData(file.getIdFavorite()));
                else
                    file.setIsFavorite(true, Utils.saveFavoriteData(new FavoriteDetails(file.getRelUrl(),
                            file.getComment(), file.getParentName(), file.mSize, file.getModificationDate())));
                currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivfavorite.setImageResource(file.isFavorite()
                                ? R.drawable.img_favorite_checked
                                : R.drawable.img_favorite_not_checked);
                    }
                });
            }
        });
        ivfavorite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Utils.showSnackBar(file.isFavorite()
                        ? R.string.remove_of_favorite_folder
                        : R.string.add_to_favorite_folder);
                return true;
            }
        });
    }

    public static class Variables implements Parcelable {
        public int BeginPosition;
        public String RelUrlDirectory = "";
        public String NameDirectory = "";
        public Point posFather = new Point(0, 0);

        public Variables(String relUrlDirectory, String nameDirectory, int beginPosition) {
            this.RelUrlDirectory = relUrlDirectory;
            this.NameDirectory = nameDirectory;
            this.BeginPosition = beginPosition;
        }

        public Variables(String relUrlDirectory, String nameDirectory, int beginPosition, Point posFather) {
            this(relUrlDirectory, nameDirectory, beginPosition);
            this.posFather = posFather;
        }

        Variables(Parcel in) {
            this.BeginPosition = in.readInt();
            this.RelUrlDirectory = in.readString();
            this.NameDirectory = in.readString();
            this.posFather = new Point(in.readInt(), in.readInt());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.BeginPosition);
            out.writeString(this.RelUrlDirectory);
            out.writeString(this.NameDirectory);
            out.writeInt(posFather.x);
            out.writeInt(posFather.y);
        }

        public static final Creator<Variables> CREATOR
                = new Creator<Variables>() {
            public Variables createFromParcel(Parcel in) {
                return new Variables(in);
            }

            public Variables[] newArray(int size) {
                return new Variables[size];
            }
        };
    }

    private static Snackbar createSnackBar(int message) {
        if (viewForSnack == null)
            return null;
        return Snackbar.make(viewForSnack, message, Snackbar.LENGTH_LONG);
    }

    public static Snackbar createSnackBar(String message) {
        if (viewForSnack == null)
            return null;
        Snackbar result = Snackbar.make(viewForSnack, message, Snackbar.LENGTH_LONG);
        ((TextView) result.getView().findViewById(R.id.snackbar_text)).setTextColor(currentActivity.getResources().getColor(R.color.white));
        return result;
    }

    public static void showSnackBar(int msg) {
        Snackbar snackbar = createSnackBar(msg);
        if (snackbar != null) {
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text))
                    .setTextColor(currentActivity.getResources().getColor(R.color.white));
            snackbar.setActionTextColor(viewForSnack.getResources().getColor(R.color.accent));
            snackbar.show();
        }
    }

    public static void showSnackBar(String msg) {
        Snackbar snackbar = Utils.createSnackBar(msg);
        if (snackbar != null) {
            ((TextView) snackbar.getView().findViewById(R.id.snackbar_text))
                    .setTextColor(currentActivity.getResources().getColor(R.color.white));
            snackbar.show();
        }
    }


    public static boolean isValidFileName(String fileName) {
        for (CharSequence c : invalidChars)
            if (fileName.contains(c))
                return false;
        return true;
    }

    public static void freeUnusedMemory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.gc();
            }
        }).start();
    }

    public static Bitmap getThumbnailForUriFile(String path, FileResource file) {
        try {
            semaphoreLoadThumbnail.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (currentActivity == null)
            return null;
        Bitmap bm = getThumbnailForUriFile(currentActivity.getContentResolver(), path, file);
        semaphoreLoadThumbnail.release();
        return bm;
    }

    public static Bitmap getThumbnailForUriFile(ContentResolver resolver, String path, FileResource file) {
        Bitmap bm, copy = null;
        try {
            if (file.isVideo() || file.isImage()) {
                Interfaces.IDetailsThumbnailerResource details = getDetailsThumbFromType(file);
                Cursor ca = resolver.query(details.getUriThumbnails(),
                        new String[]{MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + "=?", new String[]{path}, null);
                copy = getBitmap(ca, resolver, details);
            } else if (file.isAudio()) {
                bm = getArtThumbnailFromAudioFile(path, THUMB_SIZE, THUMB_SIZE);
                return bm;
            }
            if (copy == null && file.isImage()) {
                copy = BitmapFactory.decodeFile(path);
                bm = ThumbnailUtils.extractThumbnail(copy, THUMB_SIZE, THUMB_SIZE);
                recycleBitmap(copy);
                return bm;
            } else if (copy == null && file.isVideo())
                copy = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
            else if (copy == null)
                return null;
        } catch (Exception | OutOfMemoryError ignored) {
            ignored.printStackTrace();
        }
        //TODO: Si se comenta hay más consumo de memoria
        //bm = ThumbnailUtils.extractThumbnail(copy, THUMB_WITH_MINI, THUMB_HEIGHT_MINI);
        //recycleBitmap(copy);
        return copy;
    }

    public static void recycleBitmap(final Bitmap bm) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bm != null && !bm.isRecycled())
                    bm.recycle();
            }
        }).start();
    }

    public static Bitmap getArtThumbnailFromAudioFile(String path, int width, int height) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            Bitmap result, copy;
            byte[] embeddedPict = mmr.getEmbeddedPicture();
            mmr.release();
            if (embeddedPict != null)
                copy = BitmapFactory.decodeByteArray(embeddedPict, 0, embeddedPict.length);
            else
                return null;
            if (width >= 0 && height >= 0) {
                result = ThumbnailUtils.extractThumbnail(copy, width, height);
                recycleBitmap(copy);
            } else result = copy;
            return result;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    private static Interfaces.IDetailsThumbnailerResource getDetailsThumbFromType(FileResource file) {
        if (file.isVideo())
            return videoThumbnailerDetails;
        else
            return imageThumbnailerDetails;
    }

    private static Bitmap getBitmap(Cursor ca, ContentResolver resolver, Interfaces.IDetailsThumbnailerResource details) {
        Bitmap bm = null;
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            bm = details.getThumbnail(resolver, id);
        }
        if (ca != null)
            ca.close();
        return bm;
    }

    public static BitmapDrawable getIconApk(String path) {
        try {
            PackageManager pm = currentActivity.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(path, 0);
            pi.applicationInfo.sourceDir = path;
            pi.applicationInfo.publicSourceDir = path;

            return (BitmapDrawable) pi.applicationInfo.loadIcon(pm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> getStoragesPath() {
        ArrayList<String> result = new ArrayList<>();
        result.add(Environment.getExternalStorageDirectory().getPath());
        final String secondaryStorages = String.format("%s:%s", System.getenv("SECONDARY_STORAGE")
                , System.getenv("USBHOST_STORAGE"));
        final String[] secondaryStoragesArray =
                secondaryStorages != null ? secondaryStorages.split(":") : new String[0];
        for (String path : secondaryStoragesArray)
            if (path != null && !result.contains(path))
                result.add(path);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] files = currentActivity.getExternalFilesDirs(null);
            for (File file : files) {
                if (file == null)
                    continue;
                String appSpecificAbsolutePath = file.getAbsolutePath();
                String emulatedRootPath = appSpecificAbsolutePath.substring(0, appSpecificAbsolutePath.indexOf(ANDROID_DATA_PATH));
                if (!result.contains(emulatedRootPath))
                    result.add(emulatedRootPath);
            }
        }
        return result;
    }

    public static void getAllAudioOrVideoLocalFiles(Uri uri, Interfaces.IAddResourceListener addResListener,
                                                    Interfaces.ISelectListener selectListener) {
        Cursor ca = currentActivity.getContentResolver().query(uri,
                new String[]{MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.MIME_TYPE, MediaStore.Audio.AudioColumns.DURATION,
                        MediaStore.Video.VideoColumns.ALBUM}, null, null, null);
        if (ca != null && ca.moveToFirst()) {
            String path = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA)),
                    name = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)),
                    album = ca.getString(ca.getColumnIndex(MediaStore.Video.VideoColumns.ALBUM));
            long size = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                    modification = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)),
                    duration = -1;
            int index = ca.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION);
            if (index != -1)
                duration = ca.getLong(index);
            if (selectListener.select(ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)), name, path)) {
                if (name == null)
                    name = FileResource.getNameFromUrl(path);
                addResListener.add(name, path, duration > 0 ? getDurationString(duration) : null,
                        album,size, modification * 1000);
            }
            while (ca.moveToNext()) {
                path = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA));
                album = ca.getString(ca.getColumnIndex(MediaStore.Video.VideoColumns.ALBUM));
                size = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.SIZE));
                name = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                modification = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
                index = ca.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION);
                duration = index != -1 ? ca.getLong(index) : -1;
                if (selectListener.select(ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)), name, path)) {
                    if (name == null)
                        name = FileResource.getNameFromUrl(path);
                    addResListener.add(name, path, duration > 0 ? getDurationString(duration) : null,
                            album,size, modification * 1000);
                }
            }
        }
        if (ca != null)
            ca.close();
    }

    public static Point getDimentionsOfImage(Uri uri, String path) {
        Cursor ca = currentActivity.getContentResolver().query(uri,
                new String[]{MediaStore.Images.ImageColumns.HEIGHT
                        , MediaStore.Images.ImageColumns.WIDTH}, String.format("%s LIKE ?",
                        MediaStore.Images.ImageColumns.DATA), new String[]{path}, null);
        if (ca != null && ca.moveToFirst()) {
            int index = ca.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
            if (index != -1)
                return new Point(ca.getInt(ca.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH)),
                        ca.getInt(index));
        }
        if (ca != null)
            ca.close();
        return null;
    }

    public static void getAllImagesLocalFiles(Uri uri, Interfaces.IAddResourceListener addResListener,
                                              Interfaces.ISelectListener selectListener) {
        Cursor ca = currentActivity.getContentResolver().query(uri,
                new String[]{MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.MIME_TYPE, MediaStore.Images.ImageColumns.HEIGHT
                        , MediaStore.Images.ImageColumns.WIDTH, MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME}, null, null, null);
        if (ca != null && ca.moveToFirst()) {
            String path = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA)),
                    name = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)),
                    album = ca.getString(ca.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
            long size = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                    modification = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)),
                    height = -1, width = -1;
            int index = ca.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
            if (index != -1) {
                height = ca.getLong(index);
                index = ca.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
                width = ca.getLong(index);
            }
            if (selectListener.select(ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)), name, path)) {
                if (name == null)
                    name = FileResource.getNameFromUrl(path);
                addResListener.add(name, path, height > 0 ? String.format("%sx%s", width, height) : null,
                        album, size, modification * 1000);
            }
            while (ca.moveToNext()) {
                path = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA));
                album = ca.getString(ca.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                size = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.SIZE));
                name = ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                modification = ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
                index = ca.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
                if (index != -1) {
                    height = ca.getLong(index);
                    index = ca.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
                    width = ca.getLong(index);
                }
                if (selectListener.select(ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)), name, path)) {
                    if (name == null)
                        name = FileResource.getNameFromUrl(path);
                    addResListener.add(name, path, height > 0 ? String.format("%sx%s", width, height) : null,
                            album, size, modification * 1000);
                }
            }
        }
        if (ca != null)
            ca.close();
    }

    public static String getDurationString(long duration) {
        String result = "";
        duration /= 1000;
        int temp;
        if ((temp = (int) duration / 86400) > 0) {
            duration %= 86400;
            result += temp;
        }
        if ((temp = (int) duration / 3600) > 0 || !result.equals("")) {
            duration %= 3600;
            result += (!result.equals("") ? ":" : "") + ((temp <= 9 ? "0" : "") + temp);
        }
        if ((temp = (int) duration / 60) > 0 || !result.equals("")) {
            duration %= 60;
            result += (!result.equals("") ? ":" : "") + ((temp <= 9 ? "0" : "") + temp);
        }
        result += (!result.equals("") ? ":" : "00:") + ((duration <= 9 ? "0" : "") + duration);
        return result;
    }

    public static Interfaces.IDetailsSpecialDirectory getCountAllLocalFiles
            (Uri uri, Interfaces.ISelectListener selectListener, boolean load_ids) {
        final ArrayList<Integer> ids = new ArrayList<>();
        int count = 0;
        long totalSpace = 0;
        try {
            Cursor ca = currentActivity.getContentResolver().query(uri,
                    new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.SIZE,
                            MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DISPLAY_NAME,
                            MediaStore.MediaColumns.DATA}, null, null, null);
            if (ca != null && ca.moveToFirst()) {
                if (selectListener.select(ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                        ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)),
                        ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA)))) {
                    count = 1;
                    totalSpace += ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.SIZE));
                    if (load_ids)
                        ids.add(ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID)));
                }
                while (ca.moveToNext()) {
                    if (selectListener.select(ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                            ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)),
                            ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA)))) {
                        count++;
                        totalSpace += ca.getLong(ca.getColumnIndex(MediaStore.MediaColumns.SIZE));
                        if (load_ids)
                            ids.add(ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID)));
                    }
                }
            }
            if (ca != null)
                ca.close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
            count = 0;
            totalSpace = 0;
        }
        final int finalCount = count;
        final long finalTotalSpace = totalSpace;
        return new Interfaces.IDetailsSpecialDirectory() {
            @Override
            public int getCountElements() {
                return finalCount;
            }

            @Override
            public long getOcupedSpace() {
                return finalTotalSpace;
            }
        };
    }

    public static void deleteFile(final Uri uri, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mutexDelete.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentActivity.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=?", new String[]{path});
                mutexDelete.release();
            }
        }).start();
    }

    public static DisplayMetrics getDimensionScreen() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }

    public static void addFileToContentThread(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mutexAdd.acquire();
                    addFileToContent(currentActivity, path);
                    mutexAdd.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void addFileToContentThread(final Context context, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mutexAdd.acquire();
                    addFileToContent(context, path);
                    mutexAdd.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void addFileToContent(Context context, String path) {
        MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {

                    }
                });
    }

    public static void updateFileInContent(final Uri uri, final String oldPath, final String name,
                                           final String path, final String mimetype) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mutexUpdate.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, path);
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimetype);
                currentActivity.getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=?", new String[]{oldPath});
                mutexUpdate.release();
            }
        }).start();
    }

    public static Uri getUriForFile(FileResource file) {
        if (file.isImage())
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (file.isVideo())
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        if (file.isAudio())
            return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return MediaStore.Files.getContentUri(Utils.EXTERNAL_VOLUMEN_NAME);
    }

    public static void updatePathMediaStore(String oldDirPath, int lenghtNewDirPath, Directory directory) {
        directory.openSynchronic(null);
        int size = directory.getContent().size();
        for (int i = 0; i < size; i++) {
            Resource elem = directory.getResource(i);
            if (!elem.isDir()) {
                FileResource file = ((FileResource) elem);
                if (!file.isApk() && file.isThumbnailer()) {
                    String oldPath = String.format("%s%s", oldDirPath, elem.getRelUrl().substring(lenghtNewDirPath));
                    updateFileInContent(getUriForFile(file), oldPath, file.getName(),
                            file.getRelUrl(), file.getMimeType());
                }
            } else
                updatePathMediaStore(String.format("%s%s", oldDirPath,
                        elem.getRelUrl().substring(lenghtNewDirPath)), elem.getRelUrl().length(),
                        (Directory) elem);
        }
    }

    public static String getPackageName() {
        if (currentActivity == null)
            return null;
        return currentActivity.getPackageName();
    }

    public static String getString(int id) {
        return currentActivity.getString(id);
    }

    public static byte getPercent(double part, double total) {
        return (byte) ((part * 100) / total);
    }

    public static Notification getNotification(Notification.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return builder.build();
        else
            return builder.getNotification();
    }

    public static void showAboutDialog() {
        try {
            new AlertDialog.Builder(currentActivity)
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_content)
                    .setPositiveButton(R.string.accept, null)
                    .setNegativeButton(getString(R.string.contact), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                currentActivity.startActivity(new Intent(Intent.ACTION_SENDTO)
                                        .setData(Uri.parse(String.format("mailto:%s", getString(R.string.mail)))));
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                                Utils.showSnackBar(R.string.app_mail_not_found);
                            }
                        }
                    })
                    .show();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    public static void rateApp() {
        Uri uri = Uri.parse(String.format("market://details?id=%s", getPackageName()));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            currentActivity.startActivity(goToMarket);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            currentActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s"
                            , getPackageName()))));
        }
    }

    public static void requestWritePermission() {
        if (currentActivity != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            currentActivity.startActivityForResult(intent, 1);
        }
    }

    public static boolean isPrefix(String prefix, String cad) {
        if (prefix.length() > cad.length())
            return false;
        for (int i = prefix.length() - 1; i >= 0; i--)
            if (prefix.charAt(i) != cad.charAt(i))
                return false;
        return true;

    }

    public static boolean isEqual(String s1, String s2) {
        if (s1.length() != s2.length())
            return false;
        for (int i = s1.length() - 1; i >= 0; i--)
            if (s1.charAt(i) != s2.charAt(i))
                return false;
        return true;
    }

    public static boolean isFavorite(FileResource resource) {
        if (resource.isFavorite())
            return true;
        if (favoriteDbManager == null)
            favoriteDbManager = new FavoriteDbManager(currentActivity);
        ArrayList<FavoriteDetails> favoriteData = favoriteDbManager.getFavoriteData();
        for (int i = 0; i < favoriteData.size(); i++)
            if (favoriteData.get(i).getSize() == resource.mSize
                    && favoriteData.get(i).getModification() == resource.getModificationDate()
                    && isEqual(favoriteData.get(i).getPath(), resource.getRelUrl())) {
                resource.setIsFavorite(true, favoriteData.get(i).getId());
                break;
            }
        resource.setFavoriteStateLoad();
        return resource.isFavorite();
    }

    public static long deleteFavoriteData(long idFavorite) {
        if (favoriteDbManager == null)
            favoriteDbManager = new FavoriteDbManager(currentActivity);
        return favoriteDbManager.deleteFavoriteData(idFavorite);
    }

    public static void updateFavoriteData(long id, String newPath, String parent) {
        if (favoriteDbManager == null)
            favoriteDbManager = new FavoriteDbManager(currentActivity);
        File file = new File(newPath);
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(newPath);
            favoriteDbManager.updateFavoriteData(id, new FavoriteDetails(newPath, Utils.getDurationString(mp.getDuration()),
                    parent, file.length(), file.lastModified()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long saveFavoriteData(FavoriteDetails favoriteDetails) {
        if (favoriteDbManager == null)
            favoriteDbManager = new FavoriteDbManager(currentActivity);
        return favoriteDbManager.saveFavoriteData(favoriteDetails);
    }

    public static ArrayList<FavoriteDetails> getFavorites()
    {
        if (favoriteDbManager == null)
            favoriteDbManager = new FavoriteDbManager(currentActivity);
        return favoriteDbManager.getFavoriteData();
    }


    static {
        System.loadLibrary("NativeImageProcessor");
    }

    public static void ApplyFilter (Filter filter, FilterType type, float ...params) {
        switch (type) {
            case Brightness:
                filter.addSubFilter(new BrightnessSubFilter((int) params[0]));
                break;
            case Contrast:
                filter.addSubFilter(new ContrastSubFilter(params[0]));
                break;
            case ColorOverlay:
                filter.addSubFilter(new ColorOverlaySubFilter((int) params[0], params[1], params[2], params[3]));
                break;
            case Saturation:
                filter.addSubFilter(new SaturationSubFilter(params[0]));
                break;
            case Vignette:
                filter.addSubFilter(new VignetteSubFilter());
                break;
            case AverageSmooth:
                filter.addSubFilter(new AverageSmoothSubFilter((int) params[0]));
                break;
            case GammaCorrection:
                filter.addSubFilter(new GammaCorrectionSubFilter(params[0]));
                break;
            case XOR:
                filter.addSubFilter(new XORSubFilter());
                break;
            case OR:
                filter.addSubFilter(new ORSubFilter());
                break;
            case Light:
                filter.addSubFilter(new LightSubFilter());
                break;
            case Pixelate:
                filter.addSubFilter(new PixelateSubFilter());
                break;
            case Sketch:
                filter.addSubFilter(new SketchSubFilter());
                break;
            case Tv:
                filter.addSubFilter(new TvSubFilter());
                break;
            case Weave:
                filter.addSubFilter(new WeaveSubFilter());
                break;
            case Unsharp:
                filter.addSubFilter(new UnsharpSubSubFilter());
                break;
            case Threshold:
                filter.addSubFilter(new ThresholdSubFilter());
                break;
            case Stamp:
                filter.addSubFilter(new StampSubFilter());
                break;
            case Solarize:
                filter.addSubFilter(new SolarizeSubFilter());
                break;
            case Smear:
                filter.addSubFilter(new SmearSubFilter());
                break;
            case Sharpen:
                filter.addSubFilter(new SharpenSubFilter());
                break;
            case Ripple:
                filter.addSubFilter(new RippleSubFilter());
                break;
            case Rescale:
                filter.addSubFilter(new RescaleSubFilter(1.3f));
                break;
            case Quantize:
                filter.addSubFilter(new QuantizeSubFilter((int) params[0]));
                break;
            case Posterize:
                filter.addSubFilter(new PosterizeSubFilter());
                break;
            case Pointillize:
                filter.addSubFilter(new PointillizeSubFilter());
                break;
            case Oil:
                filter.addSubFilter(new OilSubFilter());
                break;
            case Offset:
                filter.addSubFilter(new OffsetSubFilter());
                break;
            case Noise:
                filter.addSubFilter(new NoiseSubFilter());
                break;
            case Minimum:
                filter.addSubFilter(new MinimumSubFilter());
                break;
            case Mask:
                filter.addSubFilter(new MaskSubFilter());
                break;
            case Marble:
                filter.addSubFilter(new MarbleSubFilter());
                break;
            case Invert:
                filter.addSubFilter(new InvertSubFilter());
                break;
            case Gaussian:
                filter.addSubFilter(new GaussianSubFilter(5));
                break;
            case Flip:
                filter.addSubFilter(new FlipSubFilter(FlipSubFilter.FLIP_180));
                break;
            case Expose:
                filter.addSubFilter(new ExposureSubFilter());
                break;
            case Emboss:
                filter.addSubFilter(new EmbossSubFilter());
                break;
            case Edge:
                filter.addSubFilter(new EdgeSubFilter());
                break;
            case Displace:
                filter.addSubFilter(new DisplaceSubFilter());
                break;
            case Diffuse:
                filter.addSubFilter(new DiffuseSubFilter());
                break;
            case Crystallize:
                filter.addSubFilter(new CrystallizeSubFilter());
                break;
            case Contour:
                filter.addSubFilter(new ContourSubFilter());
                break;
            case ColorHalftone:
                filter.addSubFilter(new ColorHalftoneSubFilter());
                break;
            case Bump:
                filter.addSubFilter(new BumpSubFilter());
                break;
            case HgayanOne:
                filter.addSubFilter(new JlabOneSubFilter());
                break;
            case HgayanTwo:
                filter.addSubFilter(new JlabTwoSubFilter());
                break;
            case HgayanThree:
                filter.addSubFilter(new JlabThreeSubFilter());
                break;
            case HgayanFour:
                filter.addSubFilter(new JlabFourSubFilter());
                break;
            case HgayanFive:
                filter.addSubFilter(new JlabFiveSubFilter());
                break;
            case HgayanSix:
                filter.addSubFilter(new JlabSixSubFilter());
                break;
            case HgayanSeven:
                filter.addSubFilter(new JlabSevenSubFilter());
                break;
            case HgayanEight:
                filter.addSubFilter(new JlabEightSubFilter());
                break;
            case HgayanNine:
                filter.addSubFilter(new JlabNineSubFilter());
                break;
            case HgayanTen:
                filter.addSubFilter(new JlabTenSubFilter());
                break;
            case HgayanEleven:
                filter.addSubFilter(new JlabElevenSubFilter());
                break;
            case HgayanTwelve:
                filter.addSubFilter(new JlabTwelveSubFilter());
                break;
            case HgayanThirteen:
                filter.addSubFilter(new JlabThirteenSubFilter());
                break;
            case HgayanFourteen:
                filter.addSubFilter(new JlabFourteenSubFilter());
                break;
            case HgayanFifteen:
                filter.addSubFilter(new JlabFifteenSubFilter());
                break;
            case HgayanSixteen:
                filter.addSubFilter(new JlabSixteenSubFilter());
                break;
            case Vintage:
                filter.addSubFilter(new VintageSubFilter());
                break;
            case Sepia:
                filter.addSubFilter(new SepiaSubFilter());
                break;
            case Brownish:
                filter.addSubFilter(new BrownishSubFilter());
                break;
            case Tint:
                filter.addSubFilter(new TintSubFilter((int) params[0]));
                break;
            case Other:
                filter.addSubFilter(new OthersubFilter());
                break;
        }
    }

    public static boolean ApplyFilter (Bitmap bm, Filter filter, boolean onlyLast) {
        try {
            if (onlyLast) {
                Filter aux = new Filter();
                aux.addSubFilter(filter.getSubFilters().get(filter.getSubFilters().size() - 1));
                aux.processFilter(bm);
            }
            else
                filter.processFilter(bm);
            return true;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return false;
        } finally {
            System.gc();
        }
    }

    public static String saveBitmapToAppFolder(Bitmap current, String name) {
        name = getNameForImageFile(name);
        final String pathImage = String.format("%s/%s"
                , saveFolderPath
                , name);
        try {
            if ((parentDir.exists() || rootDir.createDirectory("SweetPhotoFilters").exists())
                    && (new File(pathImage).exists() || parentDir.createFile("", name).exists())) {
                String ext = FileResource.getExtension(name).toLowerCase();
                current.compress(ext.length() > 2 && ext.substring(0, 2).equals("jpg")
                                ? Bitmap.CompressFormat.JPEG
                                : Bitmap.CompressFormat.PNG,
                        100, new FileOutputStream(pathImage));
                Utils.addFileToContent(currentActivity, pathImage);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
        return name;
    }

    private static String getNameForImageFile(String name) {
        int max = -1;
        boolean exist = false;
        String ext = FileResource.getExtension(name);
        name = name.substring(0, name.length() - ext.length() - 1);
        for (DocumentFile child : parentDir.listFiles()) {
            if (child.getName().indexOf(name) == 0) {
                String childName = child.getName();
                String extChild = FileResource.getExtension(childName);
                if (extChild.equals(ext)) {
                    childName = childName.substring(0, childName.length() - extChild.length() - 1);
                    if (childName.length() == name.length())
                        exist = true;
                    String numberStr = "";
                    for (int i = name.length() + 1; i < childName.length() && childName.charAt(i) != ')'
                            && Character.isDigit(childName.charAt(i)); i++)
                        numberStr += childName.charAt(i);
                    if (!numberStr.equals("")) {
                        int number = Integer.parseInt(numberStr);
                        if (number > max)
                            max = number;
                    }
                }
            }
        }
        return exist
                ? String.format("%s(%s)%s", name, max + 1, (ext.length() > 0 ? "." : "") + ext)
                : String.format("%s%s", name, (ext.length() > 0 ? "." : "") + ext);
    }

    public static int getStatusBarHeight() {
        int result = 0;
        if(currentActivity != null) {
            int resourceId = currentActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0)
                result = currentActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}