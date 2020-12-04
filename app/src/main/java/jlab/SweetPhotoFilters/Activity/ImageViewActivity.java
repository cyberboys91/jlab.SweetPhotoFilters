package jlab.SweetPhotoFilters.Activity;

import java.io.File;
import java.util.List;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import java.util.ArrayList;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.graphics.Bitmap;
import jlab.SweetPhotoFilters.R;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import jlab.SweetPhotoFilters.Utils;
import android.graphics.BitmapFactory;
import android.support.v7.widget.Toolbar;
import android.support.annotation.NonNull;
import android.view.animation.AnimationUtils;
import com.zomato.photofilters.geometry.Point;
import jlab.SweetPhotoFilters.Filter.FilterType;
import jlab.SweetPhotoFilters.Resource.Resource;
import android.support.v7.app.AppCompatActivity;
import jlab.SweetPhotoFilters.Resource.Directory;
import jlab.SweetPhotoFilters.View.ZoomImageView;
import jlab.SweetPhotoFilters.Resource.LocalFile;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import static jlab.SweetPhotoFilters.Utils.mutex;
import android.support.design.widget.AppBarLayout;
import jlab.SweetPhotoFilters.Resource.FileResource;
import com.zomato.photofilters.imageprocessors.Filter;
import jlab.SweetPhotoFilters.Resource.LocalDirectory;
import static jlab.SweetPhotoFilters.Utils.ApplyFilter;
import com.zomato.photofilters.imageprocessors.SubFilter;
import jlab.SweetPhotoFilters.View.ResourceDetailsAdapter;
import android.support.design.widget.FloatingActionButton;
import jlab.SweetPhotoFilters.View.ImageSwipeRefreshLayout;
import static jlab.SweetPhotoFilters.Utils.specialDirectories;
import jlab.SweetPhotoFilters.Resource.LocalStorageDirectories;
import jlab.SweetPhotoFilters.Activity.Fragment.DetailsFragment;
import static jlab.SweetPhotoFilters.Utils.saveBitmapToAppFolder;
import jlab.SweetPhotoFilters.View.ResourceDetailsAdapter.OnGetSetViewListener;

/*
 * Created by Javier on 25/08/2018.
 */

public class ImageViewActivity extends AppCompatActivity implements View.OnTouchListener, OnGetSetViewListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private static final short MAX_WIDTH_SUPPORT = 1080
            , MAX_HEIGHT_SUPPORT = 1080;
    private FileResource resource;
    private Toolbar toolbar;
    private AppBarLayout barImage;
    private Gallery gallery;
    private int currentIndex = 0;
    private Directory directory;
    private LayoutInflater mlInflater;
    private ResourceDetailsAdapter adapter;
    private LinearLayout layoutFilters, layoutActions;
    private ZoomImageView currentView;
    private Filter filter;
    private boolean loadingImage, cancelFilter, savingFilter,
            cancelReset, invalidImage, loadingNewImage;
    private ImageSwipeRefreshLayout msrlRefresh;
    private Bitmap bmCurrent;
    private Thread currentThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        this.msrlRefresh = (ImageSwipeRefreshLayout) findViewById(R.id.srlRefresh);
        int tbHeight = getResources().getDimensionPixelSize(R.dimen.image_swipe_margin);
        this.msrlRefresh.setProgressViewOffset(false, tbHeight, tbHeight);
        this.msrlRefresh.setColorSchemeResources(R.color.red, R.color.blue, R.color.green);
        this.barImage = (AppBarLayout) findViewById(R.id.ablImageBar);
        this.gallery = (Gallery) findViewById(R.id.gallery);
        this.layoutFilters = (LinearLayout) findViewById(R.id.llFilters);
        this.layoutActions = (LinearLayout) findViewById(R.id.llActionButtons);
        FloatingActionButton fbSave = (FloatingActionButton) findViewById(R.id.fbSaveFilter),
                fbCancel = (FloatingActionButton) findViewById(R.id.fbCancelFilter),
                fbUndo = (FloatingActionButton) findViewById(R.id.fbUndoFilter);
        fbSave.setOnClickListener(saveFilterOnClick);
        fbCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
            }
        });
        fbUndo.setOnClickListener(undoFilterOnClick);
        this.mlInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        this.filter = new Filter();
        Utils.currentActivity = this;
        Utils.viewForSnack = gallery;
        Uri uri = savedInstanceState != null && savedInstanceState.containsKey(Utils.RESOURCE_PATH_KEY)
                ? Uri.parse(savedInstanceState.getString(Utils.RESOURCE_PATH_KEY))
                : getIntent().getData();
        loadResource(uri);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar.setTitleTextAppearance(this, R.style.ToolBarApparence);
        this.toolbar.setTitle(resource.getName());
        setSupportActionBar(toolbar);
        loadDirectory(savedInstanceState != null && savedInstanceState.containsKey(Utils.DIRECTORY_KEY)
                ? savedInstanceState : getIntent().getExtras());
        adapter = new ResourceDetailsAdapter();
        adapter.addAll(directory.getContent());
        adapter.setonGetSetViewListener(this);
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
        gallery.setAdapter(adapter);
        gallery.setSelection(currentIndex, true);
        loadFiltersImages();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean loadBitmapForResource (boolean showError) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(resource.getRelUrl(), options);
            Point dim = getImageDimension(options.outWidth, options.outHeight);
            options.inSampleSize = calculateInSampleSize(options, (int) dim.x, (int) dim.y);
            options.outWidth = (int) dim.x;
            options.outHeight = (int) dim.y;
            options.inJustDecodeBounds = false;
            bmCurrent = BitmapFactory.decodeFile(resource.getRelUrl(), options);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.gc();
                }
            }).start();
            return true;
        } catch (Exception | OutOfMemoryError ignored) {
            ignored.printStackTrace();
            if (showError)
                Utils.showSnackBar(R.string.loading_image_error);
            return false;
        }
    }

    private void loadFiltersImages() {
        final ImageView ivOriginalFilter = (ImageView) findViewById(R.id.ivOriginalFilter),
                ivBrightnessFilter = (ImageView) findViewById(R.id.ivDefaultBrightnessFilter),
                ivHightBrightnessFilter = (ImageView) findViewById(R.id.ivHightBrightnessFilter),
                ivBlackAndWhiteFilter = (ImageView) findViewById(R.id.ivBlackAndWhiteFilter),
                ivLowBrightnessFilter = (ImageView) findViewById(R.id.ivLowBrightnessFilter),
                ivLowSaturationFilter = (ImageView) findViewById(R.id.ivLowSaturationFilter),
                ivDefaultSaturationFilter = (ImageView) findViewById(R.id.ivDefaultSaturationFilter),
                ivHightSaturationFilter = (ImageView) findViewById(R.id.ivHightSaturationFilter),
                ivColorOverlayFilter = (ImageView) findViewById(R.id.ivColorOverlayFilter),
                ivContrastFilter = (ImageView) findViewById(R.id.ivContrastFilter),
                ivVignetteFilter = (ImageView) findViewById(R.id.ivVignetteFilter),
                ivAverageSmoothFilter = (ImageView) findViewById(R.id.ivAverageSmoothFilter),
                ivGammaCorrectionAFilter = (ImageView) findViewById(R.id.ivGammaCorrectionAFilter),
                ivGammaCorrectionBFilter = (ImageView) findViewById(R.id.ivGammaCorrectionBFilter),
                ivGammaCorrectionCFilter = (ImageView) findViewById(R.id.ivGammaCorrectionCFilter),
                ivGammaCorrectionDFilter = (ImageView) findViewById(R.id.ivGammaCorrectionDFilter),
                ivXORFilter = (ImageView) findViewById(R.id.ivXORFilter),
                ivORFilter = (ImageView) findViewById(R.id.ivORFilter),
                ivLightFilter = (ImageView) findViewById(R.id.ivLightFilter),
                ivPixelateFilter = (ImageView) findViewById(R.id.ivPixelateFilter),
                ivSketchFilter = (ImageView) findViewById(R.id.ivSketchFilter),
                ivTvFilter = (ImageView) findViewById(R.id.ivTvFilter),
                ivWeaveFilter = (ImageView) findViewById(R.id.ivWeaveFilter),
                ivUnsharpFilter = (ImageView) findViewById(R.id.ivUnsharpFilter),
                ivThresholdFilter = (ImageView) findViewById(R.id.ivThresholdFilter),
                ivStampFilter = (ImageView) findViewById(R.id.ivStampFilter),
                ivSolarizeFilter = (ImageView) findViewById(R.id.ivSolarizeFilter),
                ivSmearFilter = (ImageView) findViewById(R.id.ivSmearFilter),
                ivSharpenFilter = (ImageView) findViewById(R.id.ivSharpenFilter),
                ivOtherFilter = (ImageView) findViewById(R.id.ivOtherFilter);

        ivBrightnessFilter.setImageResource(R.drawable.img_brightness_filter);
        ivHightBrightnessFilter.setImageResource(R.drawable.img_hight_brightness_filter);
        ivLowBrightnessFilter.setImageResource(R.drawable.img_low_brightness_filter);
        ivBlackAndWhiteFilter.setImageResource(R.drawable.img_black_and_white_filter);
        ivLowSaturationFilter.setImageResource(R.drawable.img_low_saturation_filter);
        ivDefaultSaturationFilter.setImageResource(R.drawable.img_default_saturation_filter);
        ivHightSaturationFilter.setImageResource(R.drawable.img_hight_saturation_filter);
        ivColorOverlayFilter.setImageResource(R.drawable.img_color_overlay_filter);
        ivContrastFilter.setImageResource(R.drawable.img_contrast_filter);
        ivVignetteFilter.setImageResource(R.drawable.img_vignette_filter);
        ivAverageSmoothFilter.setImageResource(R.drawable.img_average_smooth_filter);
        ivGammaCorrectionAFilter.setImageResource(R.drawable.img_gamma_correction_a_filter);
        ivGammaCorrectionBFilter.setImageResource(R.drawable.img_gamma_correction_b_filter);
        ivGammaCorrectionCFilter.setImageResource(R.drawable.img_gamma_correction_c_filter);
        ivGammaCorrectionDFilter.setImageResource(R.drawable.img_gamma_correction_d_filter);
        ivXORFilter.setImageResource(R.drawable.img_xor_filter);
        ivORFilter.setImageResource(R.drawable.img_or_filter);
        ivLightFilter.setImageResource(R.drawable.img_light_filter);
        ivPixelateFilter.setImageResource(R.drawable.img_pixelate_filter);
        ivSketchFilter.setImageResource(R.drawable.img_sketch_filter);
        ivTvFilter.setImageResource(R.drawable.img_tv_filter);
        ivWeaveFilter.setImageResource(R.drawable.img_weave_filter);
        ivUnsharpFilter.setImageResource(R.drawable.img_unsharp_filter);
        ivThresholdFilter.setImageResource(R.drawable.img_threshold_filter);
        ivStampFilter.setImageResource(R.drawable.img_stamp_filter);
        ivSolarizeFilter.setImageResource(R.drawable.img_solarize_filter);
        ivSmearFilter.setImageResource(R.drawable.img_smear_filter);
        ivOtherFilter.setImageResource(R.drawable.img_sharpen_filter);
//        ivOtherFilter.setImageResource(R.drawable.img_other_filter);
        ivOriginalFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
            }
        });
        ivBrightnessFilter.setOnClickListener(applyFilterAux(FilterType.Brightness, 60));
        ivHightBrightnessFilter.setOnClickListener(applyFilterAux(FilterType.Brightness, 90));
        ivLowBrightnessFilter.setOnClickListener(applyFilterAux(FilterType.Brightness, 30));
        ivBlackAndWhiteFilter.setOnClickListener(applyFilterAux(FilterType.Saturation, 0f));
        ivLowSaturationFilter.setOnClickListener(applyFilterAux(FilterType.Saturation, .3f));
        ivDefaultSaturationFilter.setOnClickListener(applyFilterAux(FilterType.Saturation, 1f));
        ivHightSaturationFilter.setOnClickListener(applyFilterAux(FilterType.Saturation, 1.7f));
        ivColorOverlayFilter.setOnClickListener(applyFilterAux(FilterType.ColorOverlay, 100, .2f, .2f, .0f));
        ivContrastFilter.setOnClickListener(applyFilterAux(FilterType.Contrast, 1.2f));
        ivVignetteFilter.setOnClickListener(applyFilterAux(FilterType.Vignette, 500));
        ivAverageSmoothFilter.setOnClickListener(applyFilterAux(FilterType.AverageSmooth, 5));
        ivGammaCorrectionAFilter.setOnClickListener(applyFilterAux(FilterType.GammaCorrection, -100f));
        ivGammaCorrectionBFilter.setOnClickListener(applyFilterAux(FilterType.GammaCorrection, -50f));
        ivGammaCorrectionCFilter.setOnClickListener(applyFilterAux(FilterType.GammaCorrection, -5f));
        ivGammaCorrectionDFilter.setOnClickListener(applyFilterAux(FilterType.GammaCorrection, 0.5f));
        ivXORFilter.setOnClickListener(applyFilterAux(FilterType.XOR));
        ivORFilter.setOnClickListener(applyFilterAux(FilterType.OR));
        ivLightFilter.setOnClickListener(applyFilterAux(FilterType.Light));
        ivPixelateFilter.setOnClickListener(applyFilterAux(FilterType.Pixelate));
        ivSketchFilter.setOnClickListener(applyFilterAux(FilterType.Sketch));
        ivTvFilter.setOnClickListener(applyFilterAux(FilterType.Tv));
        ivWeaveFilter.setOnClickListener(applyFilterAux(FilterType.Weave));
        ivUnsharpFilter.setOnClickListener(applyFilterAux(FilterType.Unsharp));
        ivThresholdFilter.setOnClickListener(applyFilterAux(FilterType.Threshold));
        ivStampFilter.setOnClickListener(applyFilterAux(FilterType.Stamp));
        ivSolarizeFilter.setOnClickListener(applyFilterAux(FilterType.Solarize));
        ivSmearFilter.setOnClickListener(applyFilterAux(FilterType.Smear));
        ivSharpenFilter.setOnClickListener(applyFilterAux(FilterType.Sharpen));
        ivOtherFilter.setOnClickListener(applyFilterAux(FilterType.Other));
    }

    private View.OnClickListener applyFilterAux(final FilterType filterType,  final float ...params) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (loadingImage)
                            Utils.showSnackBar(R.string.loading_image);
                        else if(invalidImage)
                            Utils.showSnackBar(R.string.invalid_image);
                        else {
                            try {
                                mutex.acquire();
                                final ZoomImageView aux = currentView;
                                loadingImage = true;
                                cancelFilter = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msrlRefresh.setRefreshing(true);
                                    }
                                });
                                ApplyFilter(filter, filterType, params);
                                final boolean error = !ApplyFilter(bmCurrent, filter, true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (error) {
                                            Utils.showSnackBar(R.string.loading_image_error);
                                            filter.getSubFilters().remove(filter.getSubFilters().size() - 1);
                                            loadActionsLayout();
                                        } else if (!cancelFilter) {
                                            aux.setImageBitmap(bmCurrent);
                                            loadActionsLayout();
                                        } else
                                            loadBitmapForResource(false);
                                        mutex.release();
                                        loadingImage = false;
                                        msrlRefresh.setRefreshing(false);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                mutex.release();
                                loadingImage = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msrlRefresh.setRefreshing(false);
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        };
    }

    private void resetFilter() {
        currentThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (loadingImage)
                    Utils.showSnackBar(R.string.loading_image);
                else if(invalidImage)
                    Utils.showSnackBar(R.string.invalid_image);
                else {
                    try {
                        mutex.acquire();
                        final ZoomImageView aux = currentView;
                        loadingImage = cancelFilter = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msrlRefresh.setRefreshing(true);
                            }
                        });
                        cancelReset = false;
                        final List<SubFilter> subFilters = new ArrayList<>();
                        for (SubFilter subFilter : filter.getSubFilters()) {
                            subFilters.add(subFilter);
                        }
                        filter.clearSubFilters();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layoutActions.clearAnimation();
                            }
                        });
                        final boolean load = loadBitmapForResource(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!load) {
                                    filter.addSubFilters(subFilters);
                                    loadActionsLayout();
                                } else if (!cancelReset)
                                    aux.setImageBitmap(bmCurrent);
                                else
                                    loadBitmapForResource(false);
                                mutex.release();
                                loadingImage = false;
                                msrlRefresh.setRefreshing(false);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mutex.release();
                        loadingImage = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msrlRefresh.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
        currentThread.start();
    }

    private View.OnClickListener undoFilterOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currentThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (loadingImage)
                        Utils.showSnackBar(R.string.loading_image);
                    else if(invalidImage)
                        Utils.showSnackBar(R.string.invalid_image);
                    else {
                        int countFilters = filter.getSubFilters().size();
                        if (countFilters > 0) {
                            if (countFilters == 1)
                                resetFilter();
                            else {
                                try {
                                    mutex.acquire();
                                    cancelFilter = false;
                                    loadingImage = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            msrlRefresh.setRefreshing(true);
                                        }
                                    });
                                    if (loadBitmapForResource(true)) {
                                        final ZoomImageView aux = currentView;
                                        List<SubFilter> subFilters = filter.getSubFilters();
                                        final SubFilter removedSubFilter = subFilters
                                                .remove(filter.getSubFilters().size() - 1);
                                        filter.clearSubFilters();
                                        filter.addSubFilters(subFilters);
                                        final String path = resource.getRelUrl();
                                        final Bitmap copy = Bitmap.createBitmap(bmCurrent);
                                        final boolean error = !ApplyFilter(bmCurrent, filter, false);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (path.equals(resource.getRelUrl())) {
                                                    if (error) {
                                                        Utils.showSnackBar(R.string.loading_image_error);
                                                        filter.addSubFilter(removedSubFilter);
                                                        bmCurrent = copy;
                                                        loadActionsLayout();
                                                    } else if (!cancelFilter) {
                                                        aux.setImageBitmap(bmCurrent);
                                                        Utils.recycleBitmap(copy);
                                                        loadActionsLayout();
                                                    } else {
                                                        loadBitmapForResource(false);
                                                        Utils.recycleBitmap(copy);
                                                    }
                                                }
                                                mutex.release();
                                                loadingImage = false;
                                                msrlRefresh.setRefreshing(false);
                                            }
                                        });
                                    } else {
                                        mutex.release();
                                        loadingImage = false;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                msrlRefresh.setRefreshing(false);
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mutex.release();
                                    loadingImage = false;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            msrlRefresh.setRefreshing(false);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
            currentThread.start();
        }
    };

    private View.OnClickListener saveFilterOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (savingFilter)
                Utils.showSnackBar(R.string.saving_image_wait);
            else if(invalidImage)
                Utils.showSnackBar(R.string.invalid_image);
            else {
                savingFilter = true;
                final String name = resource.getName();
                final Bitmap aux = Bitmap.createBitmap(bmCurrent);
                Utils.showSnackBar(R.string.saving_image);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String saveName = saveBitmapToAppFolder(aux, name);
                        if (saveName != null) {
                            Utils.showSnackBar(String.format("%s \"%s\"", getString(R.string.save_image_complete),
                                    saveName));
                            aux.recycle();
                        } else
                            Utils.showSnackBar(R.string.error_saving_image);
                        savingFilter = false;
                    }
                }).start();
            }
        }
    };

    private Point getImageDimension (int realWidth, int realHeight) {
        int width = realWidth, height = realHeight;
        if (realHeight > MAX_HEIGHT_SUPPORT) {
            height = MAX_HEIGHT_SUPPORT;
            width = (short) ((realWidth * height) / realHeight);
        }
        if (realWidth > MAX_WIDTH_SUPPORT) {
            int auxWidth = MAX_WIDTH_SUPPORT,
                    auxHeight = (short) ((realHeight * auxWidth) / realWidth);
            if (auxHeight * auxWidth < height * width) {
                height = auxHeight;
                width = auxWidth;
            }
        }
        return new Point(width, height);
    }

    private void loadDirectory(Bundle bundle) {
        String name = bundle != null ? bundle.getString(Utils.DIRECTORY_KEY) : "";
        currentIndex = bundle != null ? bundle.getInt(Utils.INDEX_CURRENT_KEY) : 0;
        directory = getSpecialDirectory(name);
        if (directory == null)
            loadParentDirectoryFromResource(name);
    }

    private void loadParentDirectoryFromResource(String name) {
        if (resource.isRemote()) {
            directory.getContent().add(resource);
            currentIndex = 0;
        } else {
            String path = resource.getRelUrl().substring(0, resource.getRelUrl().length() - resource.getName().length());
            LocalDirectory auxDir = new LocalDirectory(name, path, "", false, 0);
            directory = new LocalDirectory(name, path, "", false, 0);
            auxDir.openSynchronic(null);
            int index = 0;
            for (int i = 0; i < auxDir.getCountElements(); i++) {
                Resource current = auxDir.getResource(i);
                boolean add = !current.isDir() && ((FileResource) current).isImage();
                if (add) {
                    directory.getContent().add(current);
                    index++;
                }
                if (Utils.isEqual(current.getRelUrl(), resource.getRelUrl())) {
                    if (!add) {
                        directory.getContent().add(current);
                        currentIndex = index;
                        index++;
                    } else
                        currentIndex = index - 1;
                }
            }
            if (index == 0) {
                directory.getContent().add(resource);
                currentIndex = 0;
            }
        }
    }

    private Directory getSpecialDirectory(String name) {
        Directory result = null;
        if (name != null) {
            specialDirectories = new LocalStorageDirectories();
            specialDirectories.openSynchronic(null);
            if (name.equals(getString(R.string.downloads_folder)))
                result = specialDirectories.getDownloadDirectory();
            else if (name.equals(getString(R.string.camera_folder)))
                result = specialDirectories.getCameraDirectory();
            else if (name.equals(getString(R.string.favorite_folder)))
                result = specialDirectories.getFavoritesDirectory();
            else if (name.equals(getString(R.string.albums_folder)))
                result = specialDirectories.getAlbumsDirectory();
            else if (name.equals(getString(R.string.all_images)))
                result = specialDirectories.getImagesDirectory();
            if (result != null)
                result.openSynchronic(null);
        }
        return result;
    }

    private void loadResource(Uri uri) {
        String name = FileResource.getNameFromUrl(uri.getPath());
        File file = new File(uri.getPath());
        this.resource = new LocalFile(name, uri.getPath(), "", "", file.length(),
                file.lastModified(), file.isHidden());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_image, menu);
        final MenuItem mnfavorite = menu.getItem(0);
        if (!resource.isRemote() && resource.isImage() && resource.getFavoriteStateLoad()) {
            mnfavorite.setIcon(resource.isFavorite()
                    ? R.drawable.img_favorite_checked
                    : R.drawable.img_favorite_not_checked);
            mnfavorite.setTitle(resource.isFavorite()
                    ? R.string.remove_of_favorite_folder
                    : R.string.add_to_favorite_folder);
        } else if (!resource.isRemote() && resource.isImage()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean isFavorite = Utils.isFavorite(resource);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mnfavorite.setIcon(isFavorite
                                    ? R.drawable.img_favorite_checked
                                    : R.drawable.img_favorite_not_checked);
                            mnfavorite.setTitle(resource.isFavorite()
                                    ? R.string.remove_of_favorite_folder
                                    : R.string.add_to_favorite_folder);
                        }
                    });
                }
            }).start();
        } else {
            menu.removeItem(R.id.mnShare);
            menu.removeItem(R.id.mnFavorite);
            menu.removeItem(R.id.mnSetImageAs);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnFavorite:
                if (resource.isImage()) {
                    if (!resource.getFavoriteStateLoad())
                        Utils.isFavorite(resource);

                    if (resource.isFavorite())
                        resource.setIsFavorite(false, Utils.deleteFavoriteData(resource.getIdFavorite()));
                    else
                        resource.setIsFavorite(true, Utils.saveFavoriteData(new FavoriteDetails(resource.getRelUrl(),
                                resource.getComment(), resource.getParentName(), resource.mSize, resource.getModificationDate())));

                    item.setIcon(resource.isFavorite()
                            ? R.drawable.img_favorite_checked
                            : R.drawable.img_favorite_not_checked);
                    item.setTitle(resource.isFavorite()
                            ? R.string.remove_of_favorite_folder
                            : R.string.add_to_favorite_folder);
                }
                break;
            case R.id.mnShare:
                //Share
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(resource.getMimeType());
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(resource.getAbsUrl()));
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnSetImageAs:
                //Set image as
                try {
                    Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                    intent.putExtra(resource.getExtension(), resource.getMimeType());
                    intent.setDataAndType(Uri.parse(resource.getAbsUrl()), resource.getMimeType());
                    startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)));
                } catch (Exception ignored) {
                    ignored.printStackTrace();
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
            case R.id.mnDetails:
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
            case R.id.mnClose:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Utils.DIRECTORY_KEY, directory.getName());
        outState.putInt(Utils.INDEX_CURRENT_KEY, currentIndex);
        outState.putString(Utils.RESOURCE_PATH_KEY, resource.getAbsUrl());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gallery.onTouchEvent(motionEvent);
    }

    @Override
    public View getView(ViewGroup parent, int position, boolean isDir) {
        return mlInflater.inflate(R.layout.image_view, parent, false);
    }

    @Override
    public void setView(View view, Resource resource, int position) {
        view.findViewById(R.id.ivImageContent).setOnTouchListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, final View view, final int index, long l) {
        if (resource.isImage()) {
            if (view != null) {
                msrlRefresh.setRefreshing(true);
                layoutActions.clearAnimation();
                layoutFilters.clearAnimation();
                barImage.clearAnimation();
                loadingNewImage = true;
                currentIndex = index;
                if(currentThread != null && currentThread.isAlive()
                        && !currentThread.isInterrupted())
                    currentThread.interrupt();
                currentThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mutex.acquire();
                            if(currentIndex != index)
                                mutex.release();
                            else {
                                loadingNewImage = loadingImage = cancelFilter = cancelReset = true;
                                resource = (FileResource) directory.getResource(index);
                                filter.clearSubFilters();
                                if (loadBitmapForResource(true)) {
                                    final Bitmap bm = bmCurrent;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            currentView = view.findViewById(R.id.ivImageContent);
                                            currentView.setImageBitmap(bm);
                                            loadingNewImage = invalidImage = loadingImage = false;
                                            msrlRefresh.setRefreshing(false);
                                            toolbar.setTitle(resource.getName());
                                            invalidateOptionsMenu();
                                            mutex.release();
                                        }
                                    });
                                } else {
                                    loadingNewImage = loadingImage = false;
                                    invalidImage = true;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            msrlRefresh.setRefreshing(false);
                                        }
                                    });
                                    mutex.release();
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            loadingNewImage = invalidImage = loadingImage = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    msrlRefresh.setRefreshing(false);
                                }
                            });
                            mutex.release();
                        }
                    }
                });
                currentThread.start();
            }
        } else {
            invalidImage = true;
            ((ZoomImageView) view.findViewById(R.id.ivImageContent)).setImageResource(R.drawable.img_broken_image);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(!loadingNewImage) {
            barImage.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_in_out));
            layoutFilters.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_in_out_filter_layout));
            loadActionsLayout();
        }
    }

    private void loadActionsLayout() {
        int size = filter.getSubFilters().size();
        if (size > 1 || (size == 1 && !loadingImage))
            layoutActions.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.alpha_in_out_filter_layout));
        else
            layoutActions.clearAnimation();
    }
}