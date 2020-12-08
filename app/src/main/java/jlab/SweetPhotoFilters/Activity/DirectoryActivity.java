package jlab.SweetPhotoFilters.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import jlab.SweetPhotoFilters.*;
import android.view.Surface;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.text.Selection;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.ImageView;
import jlab.SweetPhotoFilters.View.*;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.util.DisplayMetrics;
import android.app.DownloadManager;
import jlab.SweetPhotoFilters.Resource.*;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.v7.widget.Toolbar;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.content.res.ColorStateList;
import android.text.SpannableStringBuilder;
import jlab.SweetPhotoFilters.DownloadImageTask;
import android.support.v7.widget.SearchView;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import jlab.SweetPhotoFilters.db.FavoriteDbManager;
import android.support.v4.view.GravityCompat;
import android.view.animation.AnimationUtils;
import android.support.v4.app.ActivityCompat;
import jlab.SweetPhotoFilters.Activity.Fragment.*;
import android.support.v4.widget.DrawerLayout;
import android.text.style.BackgroundColorSpan;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.design.widget.NavigationView;

import static jlab.SweetPhotoFilters.Utils.specialDirectories;
import static jlab.SweetPhotoFilters.Utils.stackVars;

import android.support.design.widget.FloatingActionButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Interfaces.ILoadThumbnailForFile,
        Interfaces.IRemoteResourceClickListener, ResourceDetailsAdapter.OnGetSetViewListener,
        DownloadImageTask.OnSetImageIconUIThread, Interfaces.IGetDirectoryListener, Interfaces.ICopyRefresh,
        ActivityCompat.OnRequestPermissionsResultCallback, Interfaces.IElementRefreshListener,
        Interfaces.ICloseListener, Interfaces.IRefreshListener {

    private int iconSize, swipeColor = R.color.accent, countColumns;
    private FloatingActionButton mfbSearch;
    private TextView mtvEmptyFolder;
    private static Interfaces.IListContent mlcResourcesDir;
    private LayoutInflater mlinflater;
    private DownloadManager mdMgr;
    private NavigationView mnavMenuExplorer;
    private SwipeRefreshLayout msrlRefresh;
    private DrawerLayout mdrawer;
    private LinearLayout llDirectory;
    private Toolbar toolbar;
    private SearchView msvSearch;
    private boolean isRemoteDirectory = false, isPortrait, isMoving = false;
    private static final int TIME_WAIT_FBUTTON_ANIM = 300, PERMISSION_REQUEST_CODE = 2901;
    public static final String STACK_VARS_KEY = "STACK_VARS_KEY",
            NAME_DOWNLOAD_DIR_KEY = "NAME_DOWNLOAD_DIR_KEY",
            LOST_CONNECTION_KEY = "LOST_CONNECTION_KEY",
            SHOW_HIDDEN_FILES_KEY = "SHOW_HIDDEN_FILES_KEY";
    public static Uri treeUri;
    private Semaphore mutexLoadDataSpecial = new Semaphore(1),
            mutexLoadDirectory = new Semaphore(1);
    private Point fromPoint;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what != Utils.SCROLLER_PATH) {
                mtvEmptyFolder.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case Utils.LOADING_INVISIBLE:
                        loadingInvisible();
                        Utils.freeUnusedMemory();
                        break;
                    case Utils.LOADING_VISIBLE:
                        if (!msrlRefresh.isRefreshing()) {
                            msrlRefresh.setRefreshing(true);
                            invalidateOptionsMenu();
                        }
                        break;
                    case Utils.LOST_CONNECTION:
                        Utils.freeUnusedMemory();
                        lostConnection();
                        break;
                }
            }
        }
    };

    public boolean requestPermission() {
        boolean request = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> requestPermissions = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                request = true;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                request = true;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.INTERNET);
                request = true;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(Manifest.permission.WAKE_LOCK);
                request = true;
            }
            if (request)
                requestAllPermission(requestPermissions);
        }
        return request;
    }

    private void requestAllPermission(ArrayList<String> requestPermissions) {
        String[] permission = new String[requestPermissions.size()];
        for (int i = 0; i < permission.length; i++)
            permission[i] = requestPermissions.get(i);
        ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.currentActivity = this;
        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        boolean haveExtras = extras != null && extras.containsKey(Utils.SERVER_DATA_KEY);
        if (haveExtras)
            this.isRemoteDirectory = extras.getBoolean(Utils.IS_REMOTE_DIRECTORY);
        loadFromBundle(savedInstanceState);
        setContentView(R.layout.activity_directory);
        DownloadImageTask.monSetImageIcon = this;
        loadViews();
        if (haveExtras)
            mlcResourcesDir.setRelUrlDirectoryRoot(this.isRemoteDirectory ?
                    Utils.NAME_REMOTE_DIRECTORY_ROOT : "", extras.getString(Utils.RELATIVE_URL_DIRECTORY_ROOT));
        setOnListeners();
        this.mdMgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        this.mlinflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        DisplayMetrics displayMetrics = Utils.getDimensionScreen();
        fromPoint = new Point(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2);
        reloadSpecialDir();
        requestPermission();
    }

    private void reloadSpecialDir() {
        specialDirectories = new LocalStorageDirectories();
        specialDirectories.openSynchronic(null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            treeUri = data.getData();
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }

    @Override
    public Directory getDirectory(String name, String relUrl) {
        int size = stackVars.size();
        Directory dir;
        if (name.equals(Utils.NAME_SEARCH)) {
            showOrHideSearchFButton(false);
            showOrHideSearchView(true);
            dir = new SearchDirectory(relUrl, isRemoteDirectory);
            reloadDir(dir);
            if (msvSearch.getQuery().toString().equals(""))
                msvSearch.setQuery(((SearchDirectory) dir).getPattern(), false);
            ((SearchDirectory) dir).setAddListener(new Interfaces.IAddResourceListener() {

                @Override
                public void add(String name, String path, String comment, String album, long size, long modification) {

                }

                @Override
                public void add(final Resource resource) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mlcResourcesDir.getResourceDetailsAdapter().add(resource);
                        }
                    });
                }

                @Override
                public void clear() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mlcResourcesDir.getResourceDetailsAdapter().clear();
                        }
                    });
                }
            });
            return dir;
        } else if (!isRemoteDirectory && size == 1 && relUrl.equals(Utils.RELURL_SPECIAL_DIR)) {
            showOrHideSearchFButton(true);
            showOrHideSearchView(false);
            dir = getSpecialDirectory(name);
            reloadDir(dir);
            return dir;
        }
        showOrHideSearchView(false);

        showOrHideSearchFButton(true);
        dir = new AlbumDirectory(name, relUrl, null, 0, false);
        this.toolbar.setTitle(name);
        reloadDir(dir);
        return dir;
    }

    private void showOrHideSearchFButton(boolean show) {
        if (!show && this.mfbSearch.getVisibility() == View.VISIBLE) {
            mfbSearch.setVisibility(View.INVISIBLE);
        }
        else if (show && this.mfbSearch.getVisibility() == View.INVISIBLE) {
            mfbSearch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setImage(final ImageView imageView, final FileResource file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final RequestBuilder<Drawable> req = Glide.with(imageView).load(file.getRelUrl()).apply(
                        new RequestOptions().signature(new MediaStoreSignature(file.getMimeType(),
                                file.getModificationDate(), 0))
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        req.into(imageView);
                    }
                });
            }
        }).start();
    }

    @Override
    public void setImage(final ImageView imageView, final Bitmap image) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final RequestBuilder<Bitmap> req = Glide.with(imageView).asBitmap().load(image);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        req.into(imageView);
                    }
                });
            }
        });
    }

    @Override
    public void setImage(final ImageView imageView, final int idRes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(idRes);
            }
        });
    }

    @Override
    public void runOnUserInterfaceThread(Runnable runnable) {
        runOnUiThread(runnable);
    }

    protected void loadViews() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar.setTitleTextAppearance(this, R.style.ToolBarApparence);
        this.toolbar.setTitle(R.string.all_images);
        setSupportActionBar(toolbar);
        this.mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, this.mdrawer, this.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.mdrawer.addDrawerListener(toggle);
        this.mdrawer.addDrawerListener(new NavigationBarChangeListener());
        toggle.syncState();
        this.mfbSearch = (FloatingActionButton) findViewById(R.id.fbSearch);
        this.mfbSearch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Utils.showSnackBar(R.string.search);
                return true;
            }
        });
        this.mfbSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrHideSearchView(true);
            }
        });
        refreshFloatingButton(false);
        this.llDirectory = (LinearLayout) findViewById(R.id.llDirectory);
        this.mtvEmptyFolder = (TextView) findViewById(R.id.tvEmptyFolder);
        mlcResourcesDir = (Interfaces.IListContent) findViewById(android.R.id.list);
        this.mnavMenuExplorer = (NavigationView) findViewById(R.id.nvServerDetails);
        this.mnavMenuExplorer.setNavigationItemSelectedListener(this);
        this.msvSearch = (SearchView) findViewById(R.id.svSearch);
        this.msvSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newPattern) {
                if (!newPattern.equals("") || getDirectory() instanceof SearchDirectory) {
                    try {
                        int indexLast = stackVars.size() - 1;
                        if (getDirectory() instanceof SearchDirectory) {
                            Utils.Variables var1 = stackVars.get(indexLast - 1),
                                    var2 = stackVars.get(indexLast);
                            if (var2.NameDirectory.equals(Utils.NAME_SEARCH))
                                var2.RelUrlDirectory = String.format("%s?%s=%s&%s=%s&%s=%s", Utils.RELURL_SEARCH,
                                        Utils.NAME_KEY, var1.NameDirectory, Utils.PATH_KEY,
                                        Resource.strEncode(var1.RelUrlDirectory),
                                        Utils.PATTERN_KEY, Resource.strEncode(newPattern.toLowerCase()));
                            ((SearchDirectory) getDirectory()).resetPattern(newPattern.toLowerCase(), handler);
                        } else {
                            if (indexLast > 0 && stackVars.get(indexLast).NameDirectory.equals(Utils.NAME_SEARCH)) {
                                stackVars.remove(indexLast);
                                indexLast--;
                            }
                            Utils.Variables var = stackVars.get(indexLast);
                            String searchRelUrl = String.format("%s?%s=%s&%s=%s&%s=%s", Utils.RELURL_SEARCH,
                                    Utils.NAME_KEY, var.NameDirectory, Utils.PATH_KEY,
                                    Resource.strEncode(var.RelUrlDirectory), Utils.PATTERN_KEY,
                                    Resource.strEncode(newPattern.toLowerCase()));
                            stackVars.add(new Utils.Variables(searchRelUrl, Utils.NAME_SEARCH, 0));
                            msrlRefresh.setRefreshing(false);
                            loadDirectory();
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
                return true;
            }
        });
        loadSwipe(true);
        loadServerData();
    }

    private void loadSwipe(boolean find) {
        if (find)
            this.msrlRefresh = (SwipeRefreshLayout) findViewById(R.id.srlRefresh);
        this.msrlRefresh.setColorSchemeResources(swipeColor);
        this.msrlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadSpecialDir();
                msrlRefresh.setRefreshing(true);
                handler.removeMessages(Utils.LOADING_VISIBLE);
                updateBeginPosition(0);
                loadDirectory();
            }
        });
    }

    private Directory getSpecialDirectory(String name) {
        Directory result = null;
        int theme = R.style.AppDefaultTheme, pathColor = R.color.accent, barColor = R.color.primary,
                progressDrawable = R.drawable.progressbar_primary, statusBarColor = R.color.primary_dark;
        if (stackVars.size() == 1) {
            if (name.equals(getString(R.string.downloads_folder))) {
                result = specialDirectories.getDownloadDirectory();
                mnavMenuExplorer.setCheckedItem(R.id.navDownloadVideos);
                toolbar.setTitle(R.string.downloads_folder);
            } else if (name.equals(getString(R.string.camera_folder))) {
                result = specialDirectories.getCameraDirectory();
                mnavMenuExplorer.setCheckedItem(R.id.navCameraVideos);
                toolbar.setTitle(R.string.camera_folder);
            }
            else if(name.equals(getString(R.string.favorite_folder)))
            {
                result = specialDirectories.getFavoritesDirectory();
                mnavMenuExplorer.setCheckedItem(R.id.navFavoriteVideos);
                toolbar.setTitle(R.string.favorite_folder);
            }
            else if(name.equals(getString(R.string.albums_folder)))
            {
                result = specialDirectories.getAlbumsDirectory();
                mnavMenuExplorer.setCheckedItem(R.id.navAlbumsVideos);
                toolbar.setTitle(R.string.albums_folder);
            }
            else {
                result = specialDirectories.getImagesDirectory();
                mnavMenuExplorer.setCheckedItem(R.id.navImages);
                toolbar.setTitle(R.string.all_images);
            }
            theme = R.style.AppImageTheme;
            barColor = R.color.green;
            pathColor = R.color.green_bright;
            statusBarColor = R.color.green_dark;
            progressDrawable = R.drawable.progressbar_blue;
        }
        swipeColor = barColor;
        setTheme(theme);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(statusBarColor));
        toolbar.setBackgroundResource(barColor);
        mfbSearch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(swipeColor)));
        msrlRefresh.setColorSchemeResources(swipeColor);
        return result;
    }

    private void loadServerData() {
        //TODO: implementar
    }

    @Override
    public void onFileClick(FileResource res, int index, Point position) {
        this.fromPoint = position;
        createOptionActivity(res, index);
    }

    @Override
    public void onDirectoryClick(String name, String relurlDir, int index, Point position) {
        this.fromPoint = position;
        handler.sendEmptyMessage(Utils.SCROLLER_PATH);
    }

    @Override
    public void onDirectoryClick(String name, String relurlDir) {
        DisplayMetrics displayMetrics = Utils.getDimensionScreen();
        this.fromPoint = new Point(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2);
        handler.sendEmptyMessage(Utils.SCROLLER_PATH);
    }

    private void refreshFloatingButton(boolean wait) {
        if (Utils.clipboardRes != null)
            toAddMode(false, wait);
        if (Utils.clipboardRes == null)
            toAddMode(true, wait);
    }

    private void toAddMode(final boolean addMode, final boolean wait) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (wait)
                    try {
                        Thread.sleep(TIME_WAIT_FBUTTON_ANIM);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }).start();
    }

    @Override
    public boolean onResourceLongClick(final Resource resource, final int index, final Point position) {
        CharSequence[] items = resource.isDir()
                ? new CharSequence[]{getString(R.string.open), getString(R.string.details)}
                : new CharSequence[]{getString(R.string.open), getString(R.string.share),
                getString(R.string.set_image_as), getString(R.string.details)};
        new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (resource.isDir() && i > 0)
                    i+=2;
                switch (i) {
                    case 0:
                        //Open
                        mlcResourcesDir.openResource(resource, index, position);
                        break;
                    case 1:
                        //Share
                        try {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType(((FileResource) resource).getMimeType());
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(resource.getAbsUrl()));
                            startActivity(Intent.createChooser(intent, getString(R.string.share)));
                        }catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        break;
                    case 2:
                        //Set image as
                        try{
                            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                            intent.putExtra(((FileResource)resource).getExtension(),((FileResource)resource).getMimeType());
                            intent.setDataAndType(Uri.parse(resource.getAbsUrl()), ((FileResource)resource).getMimeType());
                            startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)));
                        }catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        break;
                    case 3:
                        //Details
                        try {
                            DetailsFragment details = new DetailsFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Utils.RESOURCE_FOR_DETAILS_KEY, resource);
                            details.setArguments(bundle);
                            details.show(getFragmentManager(), "jlab.Details");
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                        break;
                }
            }
        }).show();
        return true;
    }

    @Override
    public View getView(ViewGroup parent, int position, boolean isDir) {
        View view = mlinflater.inflate(getDirectory().getResource(position) instanceof AlbumDirectory
                ? R.layout.album_details_grid
                : R.layout.resource_details_grid, parent, false);
        ViewGroup.LayoutParams newParams = view.getLayoutParams();
        newParams.height = iconSize;
        newParams.width = iconSize;
        view.setLayoutParams(newParams);
        return view;
    }

    @Override
    public void setView(View view, Resource resource, int position) {
        ImageView icon = (ImageView) view.findViewById(R.id.ivResourceIcon),
                ivfavorite = (ImageView) view.findViewById(R.id.ivFavorite);
        setDefaultView(icon, view, resource, mdMgr);
        if (resource.isDir()) {
            Directory dir = (Directory) resource;
            if (dir.getCountElements() > 0 && dir.getResource(0) instanceof FileResource)
                loadThumbnailForFile((FileResource) dir.getResource(0), icon, ivfavorite, true, true);
        } else
            loadThumbnailForFile((FileResource) resource, icon, ivfavorite, true, false);
    }

    private void setOnListeners() {
        mlcResourcesDir.setHandler(handler);
        mlcResourcesDir.setListeners(this);
        //No quitar
        mlcResourcesDir.loadItemClickListener();
        //.
    }

    private int getDrawableForFile(FileResource file, boolean isAlbum) {
        return R.color.transparent;
    }

    public void loadThumbnailForFile(final FileResource file, final ImageView ivIcon, final ImageView ivfavorite,
                                     boolean setBackground, boolean isAlbum) {
        if (file.isThumbnailer()) {
            DownloadImageTask downloadImageTask = new DownloadImageTask(ivIcon, ivfavorite,
                    getDrawableForFile(file, isAlbum), setBackground, getDirectory().isMultiColumn());
            downloadImageTask.load(file, isAlbum);
        }
        else if (!isAlbum && !file.getFavoriteStateLoad()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ivfavorite != null) {
                        final boolean isFavorite = Utils.isFavorite(file);
                        setImage(ivfavorite, isFavorite
                                ? R.drawable.img_favorite_checked
                                : R.drawable.img_favorite_not_checked);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivfavorite.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (file.isFavorite())
                                            file.setIsFavorite(false, Utils.deleteFavoriteData(file.getIdFavorite()));
                                        else
                                            file.setIsFavorite(true, Utils.saveFavoriteData(new FavoriteDetails(file.getRelUrl(),
                                                    file.getComment(), file.getParentName(), file.mSize, file.getModificationDate())));
                                        setImage(ivfavorite, file.isFavorite()
                                                ? R.drawable.img_favorite_checked
                                                : R.drawable.img_favorite_not_checked);
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
                        });
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private void reloadDir(Directory directory) {
        if (directory.isMultiColumn()) {
            reload(R.layout.grid_view_directory, directory);
            this.countColumns = isPortrait ? 2 : 4;
            mlcResourcesDir.setNumColumns(countColumns);
        } else if (mlcResourcesDir.getNumColumns() > 1)
            reload(R.layout.list_view_directory, directory);
    }

    private void reload(int layout, Directory directory) {
        View view = View.inflate(this, layout, null);
        this.msrlRefresh = (SwipeRefreshLayout) view.findViewById(R.id.srlRefresh);
        llDirectory.addView(view, 0);
        view = this.msrlRefresh.findViewById(android.R.id.list);
        mlcResourcesDir = (Interfaces.IListContent) view;
        setOnListeners();
        mlcResourcesDir.setDirectory(directory);
        Utils.viewForSnack = view;
        loadSwipe(false);
    }

    private void loadingInvisible() {
        if (!isRemoteDirectory && this.mdrawer.isDrawerOpen(GravityCompat.START))
            updateLocalStorageDescription(false);
        mlcResourcesDir.loadContent();
        this.msrlRefresh.setRefreshing(false);
        int size = stackVars.size();
        this.handler.removeMessages(Utils.LOADING_VISIBLE);
        final int pos = size == 0 ? 0 : stackVars.get(size - 1).BeginPosition;
        mlcResourcesDir.setSelection(pos);
        if (!Utils.lostConnection && mlcResourcesDir.isEmpty()) {
            this.mtvEmptyFolder.setText(R.string.empty_folder);
            this.mtvEmptyFolder.setVisibility(View.VISIBLE);
        } else if (Utils.lostConnection)
            lostConnection();
        else {
            mlcResourcesDir.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in));
            mlcResourcesDir.post(new Runnable() {
                @Override
                public void run() {
                    int first = mlcResourcesDir.getFirstVisiblePosition();
                    for (int i = 0; i <= mlcResourcesDir.getLastVisiblePosition() - first; i++) {
                        final View current = mlcResourcesDir.getChildAt(i);
                        if (current != null) {
                            final TranslateAnimation animation = new TranslateAnimation(
                                    fromPoint.x - current.getX(), 0, fromPoint.y - current.getY(), 0);
                            animation.setStartOffset(-200);
                            animation.setDuration(400);
                            current.startAnimation(animation);
                        }
                        else
                            break;
                    }
                }
            });
        }
        loadServerData();
        invalidateOptionsMenu();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Utils.freeUnusedMemory();
    }

    private void createOptionActivity(final FileResource file, final int position) {
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(file.getAbsUrl()), file.getMimeType());
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Utils.INDEX_CURRENT_KEY, position);
        intent.putExtra(Utils.DIRECTORY_KEY, getDirectory().getName());
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STACK_VARS_KEY, stackVars);
        outState.putString(NAME_DOWNLOAD_DIR_KEY, Utils.pathStorageDownload);
        outState.putBoolean(LOST_CONNECTION_KEY, Utils.lostConnection);
        outState.putBoolean(SHOW_HIDDEN_FILES_KEY, Utils.showHiddenFiles);
        outState.putBoolean(Utils.IS_REMOTE_DIRECTORY, this.isRemoteDirectory);
        outState.putString(Utils.RELATIVE_URL_DIRECTORY_ROOT, isRemoteDirectory ? "/" : Utils.STORAGE_DIRECTORY_PHONE);
        outState.putSerializable(Utils.CLIPBOARD_RESOURCE_KEY, Utils.clipboardRes);
        outState.putBoolean(Utils.IS_MOVING_RESOURCE_KEY, isMoving);
    }

    private void loadDirectory() {
        try {
            mutexLoadDirectory.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mlcResourcesDir.loadDirectory();
        Utils.viewForSnack = (View) mlcResourcesDir;
        mutexLoadDirectory.release();
    }

    private void showOrHideSearchView(boolean show) {
        if (show) {
            msvSearch.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT));
            msvSearch.startAnimation(AnimationUtils.loadAnimation(this, R.anim.up_in));
            msvSearch.onActionViewExpanded();
        } else {
            msvSearch.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            msvSearch.onActionViewCollapsed();
            if (!msvSearch.getQuery().equals(""))
                msvSearch.setQuery("", false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.viewForSnack = (View) mlcResourcesDir;
        Utils.currentActivity = this;
        refreshConfiguration();
        loadDirectory();
    }

    private void refreshConfiguration() {
        DisplayMetrics displayMetrics = Utils.getDimensionScreen();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        isPortrait = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180;
        this.countColumns = isPortrait ? 2 : 4;
        this.iconSize = (displayMetrics.widthPixels / countColumns);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshConfiguration();
        if (getDirectory().isMultiColumn()) {
            mlcResourcesDir.setSelection(mlcResourcesDir.getFirstVisiblePosition());
            mlcResourcesDir.setNumColumns(countColumns);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateBeginPosition(mlcResourcesDir.getFirstVisiblePosition());
    }

    @Override
    public void onBackPressed() {
        if (this.mdrawer.isDrawerOpen(GravityCompat.START))
            this.mdrawer.closeDrawer(GravityCompat.START);
        else {
            if (stackVars.size() == 1) {
                super.onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            else {
                if (!stackVars.isEmpty())
                    handler.sendEmptyMessage(Utils.SCROLLER_PATH);
                mlcResourcesDir.loadParentDirectory();
            }
        }
    }

    private void updateBeginPosition(int position) {
        if (!stackVars.isEmpty())
            stackVars.get(stackVars.size() - 1).BeginPosition = position;
    }

    private void lostConnection() {
        this.msrlRefresh.setRefreshing(false);
        mtvEmptyFolder.setText(R.string.lost_connection);
        mtvEmptyFolder.setVisibility(View.VISIBLE);
    }

    private void loadFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(STACK_VARS_KEY)) {
            stackVars = savedInstanceState.getParcelableArrayList(STACK_VARS_KEY);
            Utils.pathStorageDownload = savedInstanceState.getString(NAME_DOWNLOAD_DIR_KEY);
            Utils.lostConnection = savedInstanceState.getBoolean(LOST_CONNECTION_KEY);
            Utils.showHiddenFiles = savedInstanceState.getBoolean(SHOW_HIDDEN_FILES_KEY);
            Utils.clipboardRes = (Resource) savedInstanceState.getSerializable(Utils.CLIPBOARD_RESOURCE_KEY);
            isMoving = savedInstanceState.getBoolean(Utils.IS_MOVING_RESOURCE_KEY);
        }
    }

    private void setDefaultView(ImageView rico, View view, final Resource resource, DownloadManager dMgr) {
        final TextView rname = (TextView) view.findViewById(R.id.tvResourceName);
          final TextView mcomment = (TextView) view.findViewById(R.id.tvContentComment);
        final ImageView ivfavorite = (ImageView) view.findViewById(R.id.ivFavorite);
        if (!resource.isDir()) {
            final FileResource file = (FileResource) resource;
            if (file.getFavoriteStateLoad()) {
                ivfavorite.setImageResource(file.isFavorite() ? R.drawable.img_favorite_checked : R.drawable.img_favorite_not_checked);
                Utils.setOnFavoriteClickListener(ivfavorite, file);
            }
        }
        if (resource instanceof AlbumDirectory
                && resource.getComment() != null && mcomment != null)
            mcomment.setText(resource.getComment());
        else if (mcomment != null)
            mcomment.setVisibility(View.INVISIBLE);
        if (getDirectory() instanceof SearchDirectory) {
            BackgroundColorSpan colorSpan = new BackgroundColorSpan(getResources().getColor(R.color.green_bright));
            String pattern = ((SearchDirectory) getDirectory()).getPattern();
            SpannableStringBuilder textBd = new SpannableStringBuilder(resource.getName());
            textBd.setSpan(colorSpan, resource.getIndexPattern(), resource.getIndexPattern() + pattern.length(), 0);
            Selection.selectAll(textBd);
            rname.setText(textBd);
        }
        else
            rname.setText(resource.getName());
        setImageThumbnail(resource, rico);
    }

    public void setImageThumbnail(Resource res, ImageView rico) {
        if (res.isDir()) {
            Directory directory = (Directory) res;
            if (directory.getCountElements() > 0 && !directory.getResource(0).isDir())
                rico.setImageResource(R.color.transparent);
            else
                rico.setImageResource(R.color.transparent);
        } else {
            FileResource fres = (FileResource) res;
            String ext = fres.getExtension();
            switch (ext) {
                case "jpg":
                case "png":
                case "bmp":
                case "jpeg":
                case "ico":
                case "jpe":
                case "jfi":
                case "jfif":
                case "dib":
                case "jif":
                case "apng":
                case "gif":
                    if (mlcResourcesDir.getNumColumns() > 1)
                        rico.setImageResource(R.color.transparent);
                    else
                        rico.setImageResource(R.drawable.icon_image);
                    break;
                default:
                    break;
            }
        }
        if (!isRemoteDirectory && res.isHidden())
            rico.setAlpha(Utils.ALPHA_HIDDEN_FILES);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String nameDirSpecial = "";
        if (id == R.id.navImages)
            nameDirSpecial = getString(R.string.all_images);

        isRemoteDirectory = false;
        stackVars.clear();
        if (id == R.id.navImages) {
            stackVars.add(new Utils.Variables(Utils.RELURL_SPECIAL_DIR, nameDirSpecial, 0));
            this.toolbar.setTitle(R.string.all_images);
        }
        else if(id == R.id.navCameraVideos) {
            stackVars.add(new Utils.Variables(Utils.RELURL_SPECIAL_DIR, getString(R.string.camera_folder), 0));
            this.toolbar.setTitle(R.string.camera_folder);
        }
        else if(id == R.id.navDownloadVideos) {
            stackVars.add(new Utils.Variables(Utils.RELURL_SPECIAL_DIR, getString(R.string.downloads_folder), 0));
            this.toolbar.setTitle(R.string.downloads_folder);
        }
        else if(id == R.id.navFavoriteVideos)
        {
            stackVars.add(new Utils.Variables(Utils.RELURL_SPECIAL_DIR, getString(R.string.favorite_folder), 0));
            this.toolbar.setTitle(R.string.favorite_folder);
        }
        else if(id == R.id.navAlbumsVideos)
        {
            stackVars.add(new Utils.Variables(Utils.RELURL_SPECIAL_DIR, getString(R.string.albums_folder), 0));
            this.toolbar.setTitle(R.string.albums_folder);
        }
        this.mdrawer.closeDrawer(GravityCompat.START);
        invalidateOptionsMenu();
        FavoriteDbManager manager = new FavoriteDbManager(getApplicationContext());
        loadServerData();
        manager.close();
        msrlRefresh.setRefreshing(false);
        loadDirectory();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.mnShowHiddenFiles).setTitle(Utils.showHiddenFiles ? R.string.hidden_hidden_files : R.string.show_hidden_files);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnShowCamera:
                try {
                    startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                }catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnShowHiddenFiles:
                Utils.showHiddenFiles = !Utils.showHiddenFiles;
                item.setTitle(Utils.showHiddenFiles ? R.string.hidden_hidden_files : R.string.show_hidden_files);
                updateBeginPosition(0);
                if (this.mdrawer.isDrawerOpen(GravityCompat.START))
                    updateLocalStorageDescription(true);
                else {
                    reloadSpecialDir();
                    loadDirectory();
                }
                break;
            case R.id.mnRateApp:
                try {
                    Utils.rateApp();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnAbout:
                Utils.showAboutDialog();
                break;
            case R.id.mnClose:
                finish();
                break;
        }
        return true;
    }

    public static Directory getDirectory() {
        return mlcResourcesDir != null? mlcResourcesDir.getDirectory() : null;
    }

    @Override
    public void refresh(Runnable run) {
        runOnUiThread(run);
    }

    private void updateLocalStorageDescription(final boolean loadDir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mutexLoadDataSpecial.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                reloadSpecialDir();
                specialDirectories.loadData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Menu menu = mnavMenuExplorer.getMenu();
                        for (int i = 0; i < menu.size(); i++) {
                            MenuItem elem = menu.getItem(i);
                            TextView tv = (TextView) elem.getActionView().findViewById(R.id.tvMenuCount);
                            switch (elem.getItemId()) {
                                case R.id.navImages:
                                    tv.setText(String.valueOf(specialDirectories.getVideosDirDetails().getCountElements()));
                                    break;
                                case R.id.navAlbumsVideos:
                                    tv.setText(String.valueOf(specialDirectories.getAlbumsDirDetails().getCountElements()));
                                    break;
                                case R.id.navFavoriteVideos:
                                    tv.setText(String.valueOf(specialDirectories.getFavoritesDirDetails().getCountElements()));
                                    break;
                                case R.id.navCameraVideos:
                                    tv.setText(String.valueOf(specialDirectories.getCameraDirDetails().getCountElements()));
                                    break;
                                case R.id.navDownloadVideos:
                                    tv.setText(String.valueOf(specialDirectories.getDownloadDirDetails().getCountElements()));
                                    break;
                            }
                        }
                        if(loadDir)
                            loadDirectory();
                    }
                });
                mutexLoadDataSpecial.release();
            }
        }).start();
    }

    @Override
    public void refresh(final FileResource resource, final int position, final boolean isplaying) {

    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void close() {
    }

    @Override
    public void loaded(FileResource fsFile) {
    }

    @Override
    public void refresh() {
    }

    class NavigationBarChangeListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            updateLocalStorageDescription(false);
        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    }

    private class SeekListener {
        private boolean finish = false;

        private SeekListener() {
        }

        public void setFinish() {
            this.finish = true;
        }
    }
}