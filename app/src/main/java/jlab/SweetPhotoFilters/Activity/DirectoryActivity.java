package jlab.SweetPhotoFilters.Activity;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ImageView;
import jlab.SweetPhotoFilters.View.*;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.util.DisplayMetrics;
import jlab.SweetPhotoFilters.Resource.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import android.content.res.Configuration;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.res.ColorStateList;
import android.text.SpannableStringBuilder;
import jlab.SweetPhotoFilters.LoaderImageTask;
import androidx.appcompat.widget.SearchView;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import jlab.SweetPhotoFilters.db.FavoriteDbManager;
import androidx.core.view.GravityCompat;
import android.view.animation.AnimationUtils;
import androidx.core.app.ActivityCompat;
import jlab.SweetPhotoFilters.Activity.Fragment.*;
import androidx.drawerlayout.widget.DrawerLayout;
import android.text.style.BackgroundColorSpan;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
import static java.lang.Math.max;
import static jlab.SweetPhotoFilters.Utils.favoriteDbManager;
import static jlab.SweetPhotoFilters.Utils.getDimensionScreen;
import static jlab.SweetPhotoFilters.Utils.specialDirectories;
import static jlab.SweetPhotoFilters.Utils.stackVars;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DirectoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Interfaces.ILoadThumbnailForFile,
        Interfaces.IRemoteResourceClickListener, ResourceDetailsAdapter.OnGetSetViewListener,
        LoaderImageTask.OnSetImageIconUIThread, Interfaces.IGetDirectoryListener, Interfaces.ICopyRefresh,
        Interfaces.IElementRefreshListener, Interfaces.ICloseListener, Interfaces.IRefreshListener {

    public static int iconSize, swipeColor = R.color.accent, countColumns;
    private FloatingActionButton mfbSearch;
    private TextView mtvEmptyFolder;
    public static Interfaces.IListContent mlcResourcesDir;
    private LayoutInflater mlinflater;
    private NavigationView mnavMenuExplorer;
    private SwipeRefreshLayout msrlRefresh;
    private DrawerLayout mdrawer;
    private LinearLayout llDirectory;
    private Toolbar toolbar;
    private SearchView msvSearch;
    private String relUrlDirRoot;
    private boolean isRemoteDirectory = false, isPortrait, isMoving = false;
    private static final short TIME_WAIT_FBUTTON_ANIM = 300,
            CAMERA_REQUEST_CODE = 9101, PERMISSION_REQUEST_CODE = 9102;
    public static final String STACK_VARS_KEY = "STACK_VARS_KEY",
            NAME_DOWNLOAD_DIR_KEY = "NAME_DOWNLOAD_DIR_KEY",
            LOST_CONNECTION_KEY = "LOST_CONNECTION_KEY",
            SHOW_HIDDEN_FILES_KEY = "SHOW_HIDDEN_FILES_KEY";
    public static Uri treeUri;
    private Semaphore mutexLoadDataSpecial = new Semaphore(1),
            mutexLoadDirectory = new Semaphore(1),
            semaphoreLoadFavoriteState = new Semaphore(2);
    public static Point fromPoint = new Point(0, 0);
    private Handler handler = new Handler(Looper.getMainLooper()) {
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
        if (favoriteDbManager == null)
            favoriteDbManager = new FavoriteDbManager(this);
        Bundle extras = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        boolean haveExtras = extras != null && extras.containsKey(Utils.SERVER_DATA_KEY);
        if (haveExtras)
            this.isRemoteDirectory = extras.getBoolean(Utils.IS_REMOTE_DIRECTORY);
        loadFromBundle(savedInstanceState);
        setContentView(R.layout.activity_directory);
        LoaderImageTask.monSetImageIcon = this;
        loadViews();
        if (haveExtras) {
            relUrlDirRoot = extras.getString(Utils.RELATIVE_URL_DIRECTORY_ROOT);
            mlcResourcesDir.setRelUrlDirectoryRoot(this.isRemoteDirectory ?
                    Utils.NAME_REMOTE_DIRECTORY_ROOT : "", relUrlDirRoot);
        }
        setOnListeners();
        this.mlinflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        fromPoint = new Point(0, 0);
        reloadSpecialDir();
        requestPermission();
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        refreshConfiguration();
        loadDirectory();
    }

    private void reloadSpecialDir() {
        specialDirectories = new LocalStorageDirectories();
        specialDirectories.openSynchronic(null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PERMISSION_REQUEST_CODE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            treeUri = data.getData();
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            //TODO: Testing GPU filters
//            Intent intent = new Intent(this, GPUImageViewActivity.class);
            Intent intent = new Intent(this, ImageViewActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Utils.DIRECTORY_KEY, getString(R.string.camera_folder));
            FileResource resource = getResourceForName(new CameraImagesDirectory(getString(R.string.camera_folder))
                    , DocumentFile.fromSingleUri(this, data.getData()).getName());
            if (resource != null) {
                intent.setDataAndType(Uri.parse(resource.getAbsUrl()), resource.getMimeType());
                intent.putExtra(Utils.INDEX_CURRENT_KEY, resource.getIndexPattern());
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }
    }

    @Nullable
    private FileResource getResourceForName(CameraImagesDirectory cameraDirectory, String name) {
        cameraDirectory.openSynchronic(null);
        for (int i = 0; i < cameraDirectory.getCountElements(); i++) {
            Resource current = cameraDirectory.getResource(i);
            if (!current.isDir() && current.getName().equals(name)) {
                current.setIndexPattern(i);
                return (FileResource) current;
            }
        }
        return null;
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
        final RequestBuilder<Drawable> req = Glide.with(imageView).load(file.getRelUrl()).apply(
                new RequestOptions().signature(new MediaStoreSignature(file.getMimeType(),
                        file.getModificationDate(), 0)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                req.into(imageView);
            }
        });
    }

    @Override
    public void setImage(final ImageView imageView, final Bitmap image) {
        final RequestBuilder<Bitmap> req = Glide.with(imageView).asBitmap().load(image);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            barColor = R.color.gray;
            pathColor = R.color.gray_bright;
            statusBarColor = R.color.gray_dark;
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
        fromPoint = position;
        createOptionActivity(res, index);
    }

    @Override
    public void onDirectoryClick(String name, String relurlDir, int index, Point position) {
        fromPoint = position;
        handler.sendEmptyMessage(Utils.SCROLLER_PATH);
    }

    @Override
    public void onDirectoryClick(String name, String relurlDir) {
        fromPoint = new Point(0, 0);
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
        fromPoint = position;
        CharSequence[] items = resource.isDir()
                ? new CharSequence[]{getString(R.string.open), getString(R.string.details)}
                : new CharSequence[]{getString(R.string.open), getString(R.string.share),
                getString(R.string.set_image_as), getString(R.string.details)};
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setItems(items, null)
                .setTitle(resource.getName()).create();
        alertDialog.show();
        alertDialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                if (resource.isDir() && i > 0)
                    i += 2;
                switch (i) {
                    case 0:
                        //Open
                        startAnimationMenu(alertDialog, position, true, new Runnable() {
                            @Override
                            public void run() {
                                mlcResourcesDir.openResource(resource, index, position);
                            }
                        }, index);
                        break;
                    case 1:
                        //Share
                        startAnimationMenu(alertDialog, position, true, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType(((FileResource) resource).getMimeType());
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(resource.getAbsUrl()));
                                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                } catch (Exception ignored) {
                                    ignored.printStackTrace();
                                }
                            }
                        }, index);
                        break;
                    case 2:
                        //Set image as
                        startAnimationMenu(alertDialog, position, true, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                                    intent.putExtra(((FileResource) resource).getExtension(), ((FileResource) resource).getMimeType());
                                    intent.setDataAndType(Uri.parse(resource.getAbsUrl()), ((FileResource) resource).getMimeType());
                                    startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)));
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                } catch (Exception ignored) {
                                    ignored.printStackTrace();
                                }
                            }
                        }, index);
                        break;
                    case 3:
                        //Details
                        startAnimationMenu(alertDialog, position, true, new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DetailsFragment details = new DetailsFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(Utils.RESOURCE_FOR_DETAILS_KEY, resource);
                                    bundle.putBoolean(Utils.OPEN_RESOURCE_ON_CLICKED, true);
                                    details.setArguments(bundle);
                                    details.show(getFragmentManager(), "jlab.Details");
                                } catch (Exception exp) {
                                    exp.printStackTrace();
                                }
                            }
                        }, index);
                        break;
                }
            }
        });
        startAnimationMenu(alertDialog, position, false, new Runnable() {
            @Override
            public void run() {
            }
        }, index);
        return true;
    }

    private void startAnimationMenu(final AlertDialog dialog, final Point position,
                                    final boolean reverse, final Runnable onEndListener,
                                    final int index) {
        final Window window = dialog.getWindow();
        if(window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = getDimensionScreen().widthPixels;
            wlp.flags &= -WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
        View view = (View) dialog.getListView().getParent().getParent();
        ViewParent parent = view.getParent();
        view.setBackgroundResource(R.color.white);
        while (parent.getParent() != null) {
            parent = parent.getParent();
            if(parent instanceof View) {
                ((View) parent).setBackgroundResource(R.color.transparent);
                view = (View) parent;
                view.setPadding(0, 0, 0, 0);
            }
            else
                break;
        }
        dialog.show();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final View finalView = view;
            view.post(new Runnable() {
                @Override
                public void run() {
                    DisplayMetrics dimen = Utils.getDimensionScreen();
                    int radius = max(dimen.widthPixels, dimen.heightPixels),
                            fromRadius = reverse ? radius : 0,
                            toRadius = reverse ? 0 : radius,
                            posY = mlcResourcesDir.getView().getHeight() - finalView.getHeight();
                    Animator animator = ViewAnimationUtils.createCircularReveal(finalView,
                            position.x + iconSize / 2, position.y <= posY - iconSize / 2
                                    ? 0 : position.y - posY + iconSize / 2,
                            fromRadius, toRadius);
                    animator.setDuration(800);
                    final View resView = mlcResourcesDir.getChildAt(index - mlcResourcesDir.getFirstVisiblePosition());
                    final Animation animator1 = new AlphaAnimation(.2f, 1f);
                    animator1.setDuration(200);
                    final Animation animator2 = new AlphaAnimation(1f, .2f);
                    animator2.setDuration(200);

                    animator1.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (reverse) {
                                resView.startAnimation(animator2);
                                onEndListener.run();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (!reverse)
                                resView.startAnimation(animator1);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (reverse) {
                                dialog.dismiss();
                                resView.startAnimation(animator1);
                            }
                            else
                                onEndListener.run();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            if (reverse) {
                                dialog.dismiss();
                                resView.startAnimation(animator1);
                            }
                            else
                                onEndListener.run();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                }
            });
        }
        else
            onEndListener.run();
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
    public void setView(final View view, final Resource resource, int position) {
        final ImageView icon = view.findViewById(R.id.ivResourceIcon),
                ivfavorite = view.findViewById(R.id.ivFavorite);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setDefaultView(icon, view, resource);
                    }
                });
                if (!mlcResourcesDir.scrolling()) {
                    if (resource.isDir()) {
                        Directory dir = (Directory) resource;
                        if (!dir.isEmpty() && dir.getResource(0) instanceof FileResource)
                            loadThumbnailForFile((FileResource) dir.getResource(0), icon, ivfavorite, true, true);
                    } else
                        loadThumbnailForFile((FileResource) resource, icon, ivfavorite, true, false);
                }
            }
        }).start();
    }

    private void setOnListeners() {
        mlcResourcesDir.setHandler(handler);
        mlcResourcesDir.setListeners(this);
        //No quitar
        mlcResourcesDir.loadItemClickListener();
        //.
    }

    public void loadThumbnailForFile(final FileResource file, final ImageView ivIcon, final ImageView ivfavorite,
                                     boolean setBackground, boolean isAlbum) {
        if (file.isThumbnailer())
            new LoaderImageTask(ivIcon, ivfavorite).load(file, isAlbum);
        else if (!isAlbum && !file.getFavoriteStateLoad()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphoreLoadFavoriteState.acquire();
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
                    }catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                    finally {
                        semaphoreLoadFavoriteState.release();
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
            countColumns = favoriteDbManager.getNumColumns();
            if (!isPortrait)
                countColumns += 2;
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
            final boolean showCircleAnimator = android.os.Build.VERSION.SDK_INT
                    >= android.os.Build.VERSION_CODES.LOLLIPOP;

            mlcResourcesDir.startAnimation(AnimationUtils.loadAnimation(
                    mlcResourcesDir.getView().getContext()
                    , showCircleAnimator ? R.anim.alpha_in_grid : R.anim.scale_in_grid));

            mlcResourcesDir.post(new Runnable() {
                @Override
                public void run() {
                    if (showCircleAnimator) {
                        View view = mlcResourcesDir.getView();
                        Animator animator = ViewAnimationUtils.createCircularReveal(view,
                                fromPoint.x + iconSize / 2, fromPoint.y + iconSize / 2, 0,
                                max(view.getWidth(), view.getHeight()));
                        animator.setInterpolator(AnimationUtils.loadInterpolator(view.getContext()
                                , android.R.interpolator.fast_out_linear_in));
                        animator.setDuration(500);
                        animator.start();
                    }
                    int first = mlcResourcesDir.getFirstVisiblePosition();
                    for (int i = 0; i <= mlcResourcesDir.getLastVisiblePosition() - first; i++) {
                        final View current = mlcResourcesDir.getChildAt(i);
                        if (current != null) {
                            final TranslateAnimation animation = new TranslateAnimation(
                                    (fromPoint.x - current.getX()) / 2, 0, (fromPoint.y - current.getY()) / 2, 0);
                            animation.setStartOffset(showCircleAnimator ? -100 : -200);
                            animation.setDuration(500);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            current.startAnimation(animation);
                                        }
                                    });
                                }
                            }).start();
                        } else
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
        //TODO: Testing GPU filters
//        Intent intent = new Intent(this, GPUImageViewActivity.class);
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
        Utils.viewForSnack = (View) mlcResourcesDir;
        mlcResourcesDir.loadDirectory();
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
        Utils.currentActivity = this;
        refreshConfiguration();
        loadDirectory();
    }

    public void refreshConfiguration() {
        DisplayMetrics displayMetrics = Utils.getDimensionScreen();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        isPortrait = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180;
        countColumns = favoriteDbManager.getNumColumns();
        if (!isPortrait)
            countColumns += 2;
        iconSize = (displayMetrics.widthPixels / countColumns);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshConfiguration();
        mlcResourcesDir.setSelection(mlcResourcesDir.getFirstVisiblePosition());
        mlcResourcesDir.setNumColumns(countColumns);
        DisplayMetrics displayMetrics = Utils.getDimensionScreen();
        fromPoint = new Point((displayMetrics.widthPixels - iconSize) / 2,
                (displayMetrics.heightPixels - iconSize) / 2 - getResources()
                        .getDimensionPixelOffset(R.dimen.tool_bar_height));
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

    private void setDefaultView(ImageView rico, View view, final Resource resource) {
        final TextView rName = view.findViewById(R.id.tvResourceName);
        final TextView mComment = view.findViewById(R.id.tvContentComment);
        final ImageView ivFavorite = view.findViewById(R.id.ivFavorite);
        if(countColumns < 3) {
            int padding = getResources().getDimensionPixelSize(R.dimen.margin);
            rName.setPadding(padding, padding, padding, padding);
        }
        else {
            if(ivFavorite != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.favorite_short_size);
                ivFavorite.getLayoutParams().height = size;
                ivFavorite.getLayoutParams().width = size;
            }
            rName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
        if (!resource.isDir()) {
            final FileResource file = (FileResource) resource;
            if (file.getFavoriteStateLoad()) {
                ivFavorite.setImageResource(file.isFavorite()
                        ? R.drawable.img_favorite_checked
                        : R.drawable.img_favorite_not_checked);
                Utils.setOnFavoriteClickListener(ivFavorite, file);
            }
        }
        if (resource instanceof AlbumDirectory && resource.getComment() != null && mComment != null) {
            mComment.setText(resource.getComment());
        }
        if (getDirectory() instanceof SearchDirectory) {
            BackgroundColorSpan colorSpan = new BackgroundColorSpan(getResources().getColor(R.color.blue_bright));
            String pattern = ((SearchDirectory) getDirectory()).getPattern();
            SpannableStringBuilder textBd = new SpannableStringBuilder(resource.getName());
            textBd.setSpan(colorSpan, resource.getIndexPattern(), resource.getIndexPattern() + pattern.length(), 0);
            Selection.selectAll(textBd);
            rName.setText(textBd);
        } else
            rName.setText(resource.getName());
        if (resource.isHidden())
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
                    /*Intent intent = new Intent(this, CameraActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent);*/
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                            TextView tv = elem.getActionView().findViewById(R.id.tvMenuCount);
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
}