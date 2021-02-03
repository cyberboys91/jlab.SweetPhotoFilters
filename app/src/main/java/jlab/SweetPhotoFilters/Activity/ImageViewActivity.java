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
import android.graphics.Matrix;
import android.graphics.Bitmap;
import jlab.SweetPhotoFilters.R;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import jlab.SweetPhotoFilters.Utils;
import android.widget.RelativeLayout;
import android.graphics.BitmapFactory;
import java.util.concurrent.Semaphore;
import jlab.SweetPhotoFilters.Interfaces;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import android.widget.HorizontalScrollView;
import android.view.animation.AnimationUtils;
import com.zomato.photofilters.geometry.Point;
import jlab.SweetPhotoFilters.Filter.FilterType;
import jlab.SweetPhotoFilters.Resource.Resource;
import androidx.appcompat.app.AppCompatActivity;
import jlab.SweetPhotoFilters.Resource.Directory;
import jlab.SweetPhotoFilters.View.ImageGallery;
import jlab.SweetPhotoFilters.View.ZoomImageView;
import jlab.SweetPhotoFilters.Resource.LocalFile;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import com.google.android.material.appbar.AppBarLayout;

import static jlab.SweetPhotoFilters.Utils.OPEN_RESOURCE_ON_CLICKED;
import static jlab.SweetPhotoFilters.Utils.isEqual;
import static jlab.SweetPhotoFilters.Utils.rateApp;
import jlab.SweetPhotoFilters.Resource.FileResource;
import static jlab.SweetPhotoFilters.Utils.stackVars;
import static jlab.SweetPhotoFilters.Utils.isFavorite;
import com.zomato.photofilters.imageprocessors.Filter;
import jlab.SweetPhotoFilters.Resource.LocalDirectory;
import static jlab.SweetPhotoFilters.Utils.ApplyFilter;
import static jlab.SweetPhotoFilters.Utils.showSnackBar;
import static jlab.SweetPhotoFilters.Utils.recycleBitmap;
import static jlab.SweetPhotoFilters.Utils.DIRECTORY_KEY;
import com.zomato.photofilters.imageprocessors.SubFilter;
import static jlab.SweetPhotoFilters.Utils.saveFolderPath;
import jlab.SweetPhotoFilters.View.ResourceDetailsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import static jlab.SweetPhotoFilters.Utils.showAboutDialog;
import jlab.SweetPhotoFilters.View.ImageSwipeRefreshLayout;
import static jlab.SweetPhotoFilters.Utils.saveFavoriteData;
import static jlab.SweetPhotoFilters.Utils.INDEX_CURRENT_KEY;
import static jlab.SweetPhotoFilters.Utils.RESOURCE_PATH_KEY;
import static jlab.SweetPhotoFilters.Utils.deleteFavoriteData;
import static jlab.SweetPhotoFilters.Utils.specialDirectories;
import jlab.SweetPhotoFilters.Resource.LocalStorageDirectories;
import jlab.SweetPhotoFilters.Activity.Fragment.DetailsFragment;
import static jlab.SweetPhotoFilters.Utils.saveBitmapToAppFolder;
import static jlab.SweetPhotoFilters.Utils.RESOURCE_FOR_DETAILS_KEY;
import static jlab.SweetPhotoFilters.Activity.DirectoryActivity.STACK_VARS_KEY;
import jlab.SweetPhotoFilters.View.ResourceDetailsAdapter.OnGetSetViewListener;

/*
 * Created by Javier on 25/08/2018.
 */

public class ImageViewActivity extends AppCompatActivity implements View.OnTouchListener, OnGetSetViewListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    protected static final short MAX_WIDTH_SUPPORT = 1080, MAX_HEIGHT_SUPPORT = 1080;
    protected FileResource resource;
    protected Toolbar toolbar;
    protected AppBarLayout barImage;
    protected ImageGallery gallery;
    protected int currentIndex = 0;
    protected Directory directory;
    protected LayoutInflater mlInflater;
    protected LinearLayout layoutFilters, layoutActions;
    protected Interfaces.IImageContent currentView;
    protected Filter filter;
    protected boolean loadingImage, savingFilter, invalidImage;
    protected ImageSwipeRefreshLayout msrlRefresh;
    protected Bitmap bmCurrent;
    protected Semaphore mutex = new Semaphore(1), mutexSave = new Semaphore(1);
    protected FloatingActionButton fbSave, fbCancel, fbUndo;
    protected RelativeLayout rlContainer;

    protected int getCountFilters () {
        return filter.getSubFilters().size();
    }

    protected void clearSubFilters () {
        this.filter.clearSubFilters();
    }

    protected void removeSubFilter (int index) {
        this.filter.getSubFilters().remove(index);
    }

    protected boolean isEmptySubFiltersList () {
        return getCountFilters() == 0;
    }

    protected void initFilter () {
        this.filter = new Filter();
    }

    protected void setCurrentView (Interfaces.IImageContent view) {
        this.currentView = view;
    }

    public void setContentView() {
        setContentView(R.layout.activity_image_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        this.rlContainer = findViewById(R.id.rlImageViewContainer);
        this.msrlRefresh = findViewById(R.id.srlRefresh);
        int tbHeight = getResources().getDimensionPixelSize(R.dimen.image_swipe_margin);
        this.msrlRefresh.setProgressViewOffset(false, tbHeight, tbHeight);
        this.msrlRefresh.setColorSchemeResources(R.color.red, R.color.blue, R.color.green);
        this.barImage = findViewById(R.id.ablImageBar);
        this.layoutFilters = findViewById(R.id.llFilters);
        HorizontalScrollView hsvFilters = findViewById(R.id.hsvFilters);
        this.layoutActions = findViewById(R.id.llActionButtons);
        fbSave = findViewById(R.id.fbSaveFilter);
        fbCancel = findViewById(R.id.fbCancelFilter);
        fbUndo = findViewById(R.id.fbUndoFilter);
        fbSave.setOnClickListener(saveFilterOnClick());
        fbCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
            }
        });
        fbUndo.setOnClickListener(undoFilterOnClick());
        this.mlInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        initFilter();
        Utils.currentActivity = this;
        Utils.viewForSnack = gallery;

        loadDataAndGallery(savedInstanceState);

        hsvFilters.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE)
                    layoutFilters.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v1));
                return false;
            }
        });
        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE)
                    barImage.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v1));
                return false;
            }
        });
        loadFiltersImages();
    }

    protected void loadDataAndGallery(Bundle savedInstanceState) {
        Uri uri = savedInstanceState != null && savedInstanceState.containsKey(RESOURCE_PATH_KEY)
                ? Uri.parse(savedInstanceState.getString(RESOURCE_PATH_KEY))
                : getIntent().getData();
        loadResource(uri);
        this.toolbar = findViewById(R.id.toolbar);
        this.toolbar.setTitleTextAppearance(this, R.style.ToolBarApparence);
        this.toolbar.setTitle(resource.getName());
        setSupportActionBar(toolbar);
        loadFromBundle(savedInstanceState != null && savedInstanceState.containsKey(DIRECTORY_KEY)
                ? savedInstanceState : getIntent().getExtras());

        ResourceDetailsAdapter adapter = new ResourceDetailsAdapter();
        adapter.addAll(directory.getContent());
        adapter.setonGetSetViewListener(this);
        this.gallery = findViewById(R.id.gallery);
        gallery.setOnItemSelectedListener(this);
        gallery.setOnItemClickListener(this);
        gallery.setAdapter(adapter);
        gallery.setSelection(currentIndex, true);
        gallery.setLoadImageListener(new Interfaces.ILoadImageListener() {
            @Override
            public boolean loadImage() {
                return loadingImage;
            }
        });
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

    protected boolean loadBitmapForResource(boolean showError) {
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
            ExifInterface ei = new ExifInterface(resource.getRelUrl());
            switch (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bmCurrent = rotateBitmap(bmCurrent, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bmCurrent = rotateBitmap(bmCurrent, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bmCurrent = rotateBitmap(bmCurrent, 270);
                    break;
            }
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
                showSnackBar(R.string.loading_image_error);
            return false;
        }
    }

    protected Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        source.recycle();
        return result;
    }

    protected void loadFiltersImages() {
        final ImageView ivBrightnessFilter = (ImageView) findViewById(R.id.ivDefaultBrightnessFilter),
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
//                ivTvFilter = (ImageView) findViewById(R.id.ivTvFilter),
//                ivWeaveFilter = (ImageView) findViewById(R.id.ivWeaveFilter),
                ivUnsharpFilter = (ImageView) findViewById(R.id.ivUnsharpFilter),
                ivThresholdFilter = (ImageView) findViewById(R.id.ivThresholdFilter),
                ivStampFilter = (ImageView) findViewById(R.id.ivStampFilter),
                ivSolarizeFilter = (ImageView) findViewById(R.id.ivSolarizeFilter),
                ivSmearFilter = (ImageView) findViewById(R.id.ivSmearFilter),
                ivSharpenFilter = (ImageView) findViewById(R.id.ivSharpenFilter),
                ivRippleFilter = (ImageView) findViewById(R.id.ivRippleFilter),
                ivRescaleFilter = (ImageView) findViewById(R.id.ivRescaleFilter),
                ivQuantizeFilter = (ImageView) findViewById(R.id.ivQuantizeFilter),
//                ivOtherFilter = (ImageView) findViewById(R.id.ivOtherFilter),
                ivPosterizeFilter = (ImageView) findViewById(R.id.ivPosterizeFilter),
//                ivOilFilter = (ImageView) findViewById(R.id.ivOilFilter),
//                ivPointillizeFilter = (ImageView) findViewById(R.id.ivPointillizeFilter),
//                ivOffsetFilter = (ImageView) findViewById(R.id.ivOffsetFilter),
                ivNoiseFilter = (ImageView) findViewById(R.id.ivNoiseFilter),
//                ivMinimumFilter = (ImageView) findViewById(R.id.ivMinimumFilter),
                ivMaskFilter = (ImageView) findViewById(R.id.ivMaskFilter),
                ivMarbleFilter = (ImageView) findViewById(R.id.ivMarbleFilter),
                ivInvertFilter = (ImageView) findViewById(R.id.ivInvertFilter),
                ivGaussianFilter = (ImageView) findViewById(R.id.ivGaussianFilter),
                ivExposeFilter = (ImageView) findViewById(R.id.ivExposeFilter),
//                ivFlipFilter = (ImageView) findViewById(R.id.ivFlipFilter),
                ivEmbossFilter = (ImageView) findViewById(R.id.ivEmbossFilter),
                ivEdgeFilter = (ImageView) findViewById(R.id.ivEdgeFilter),
                ivDiffuseFilter = (ImageView) findViewById(R.id.ivDiffuseFilter),
//                ivCrystallizeFilter = (ImageView) findViewById(R.id.ivCrystallizeFilter),
                ivDisplaceFilter = (ImageView) findViewById(R.id.ivDisplaceFilter),
                ivContourFilter = (ImageView) findViewById(R.id.ivContourFilter),
//                ivColorHalftoneFilter = (ImageView) findViewById(R.id.ivColorHalftoneFilter),
                ivBumpFilter = (ImageView) findViewById(R.id.ivBumpFilter),
                ivHgayanOneFilter = (ImageView) findViewById(R.id.ivHgayanOneFilter),
//                ivHgayanTwoFilter = (ImageView) findViewById(R.id.ivHgayanTwoFilter),
                ivHgayanThreeFilter = (ImageView) findViewById(R.id.ivHgayanThreeFilter),
                ivHgayanFourFilter = (ImageView) findViewById(R.id.ivHgayanFourFilter),
                ivHgayanFiveFilter = (ImageView) findViewById(R.id.ivHgayanFiveFilter),
                ivHgayanSixFilter = (ImageView) findViewById(R.id.ivHgayanSixFilter),
                ivHgayanSevenFilter = (ImageView) findViewById(R.id.ivHgayanSevenFilter),
                ivHgayanEightFilter = (ImageView) findViewById(R.id.ivHgayanEightFilter),
//                ivHgayanNineFilter = (ImageView) findViewById(R.id.ivHgayanNineFilter),
                ivHgayanTenFilter = (ImageView) findViewById(R.id.ivHgayanTenFilter),
                ivHgayanElevenFilter = (ImageView) findViewById(R.id.ivHgayanElevenFilter),
                ivHgayanTwelveFilter = (ImageView) findViewById(R.id.ivHgayanTwelveFilter),
                ivHgayanFourteenFilter = (ImageView) findViewById(R.id.ivHgayanFourteenFilter),
                ivHgayanFifteenFilter = (ImageView) findViewById(R.id.ivHgayanFifteenFilter),
                ivHgayanSixteenFilter = (ImageView) findViewById(R.id.ivHgayanSixteenFilter),
                ivVintageFilter = (ImageView) findViewById(R.id.ivVintageFilter),
                ivSepiaFilter = (ImageView) findViewById(R.id.ivSepiaFilter),
                ivBrownishFilter = (ImageView) findViewById(R.id.ivBrownishFilter),
                ivTintLowFilter = (ImageView) findViewById(R.id.ivTintLowFilter),
                ivTintMediumFilter = (ImageView) findViewById(R.id.ivTintMediumFilter),
                ivTintHighFilter = (ImageView) findViewById(R.id.ivTintHighFilter);

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
//        ivTvFilter.setOnClickListener(applyFilterAux(FilterType.Tv));
//        ivWeaveFilter.setOnClickListener(applyFilterAux(FilterType.Weave));
        ivUnsharpFilter.setOnClickListener(applyFilterAux(FilterType.Unsharp));
        ivThresholdFilter.setOnClickListener(applyFilterAux(FilterType.Threshold));
        ivStampFilter.setOnClickListener(applyFilterAux(FilterType.Stamp));
        ivSolarizeFilter.setOnClickListener(applyFilterAux(FilterType.Solarize));
        ivSmearFilter.setOnClickListener(applyFilterAux(FilterType.Smear));
        ivSharpenFilter.setOnClickListener(applyFilterAux(FilterType.Sharpen));
        ivRippleFilter.setOnClickListener(applyFilterAux(FilterType.Ripple));
        ivRescaleFilter.setOnClickListener(applyFilterAux(FilterType.Rescale));
        ivQuantizeFilter.setOnClickListener(applyFilterAux(FilterType.Quantize, 50));
        ivPosterizeFilter.setOnClickListener(applyFilterAux(FilterType.Posterize));
//        ivPointillizeFilter.setOnClickListener(applyFilterAux(FilterType.Pointillize));
//        ivOilFilter.setOnClickListener(applyFilterAux(FilterType.Oil));
//        ivOffsetFilter.setOnClickListener(applyFilterAux(FilterType.Offset));
        ivNoiseFilter.setOnClickListener(applyFilterAux(FilterType.Noise));
//        ivMinimumFilter.setOnClickListener(applyFilterAux(FilterType.Minimum));
        ivMaskFilter.setOnClickListener(applyFilterAux(FilterType.Mask));
        ivInvertFilter.setOnClickListener(applyFilterAux(FilterType.Invert));
        ivMarbleFilter.setOnClickListener(applyFilterAux(FilterType.Marble));
        ivGaussianFilter.setOnClickListener(applyFilterAux(FilterType.Gaussian));
//        ivFlipFilter.setOnClickListener(applyFilterAux(FilterType.Flip));
        ivExposeFilter.setOnClickListener(applyFilterAux(FilterType.Expose));
        ivEmbossFilter.setOnClickListener(applyFilterAux(FilterType.Emboss));
        ivEdgeFilter.setOnClickListener(applyFilterAux(FilterType.Edge));
        ivDisplaceFilter.setOnClickListener(applyFilterAux(FilterType.Displace));
        ivDiffuseFilter.setOnClickListener(applyFilterAux(FilterType.Diffuse));
//        ivCrystallizeFilter.setOnClickListener(applyFilterAux(FilterType.Crystallize));
        ivContourFilter.setOnClickListener(applyFilterAux(FilterType.Contour));
//        ivColorHalftoneFilter.setOnClickListener(applyFilterAux(FilterType.ColorHalftone));
        ivBumpFilter.setOnClickListener(applyFilterAux(FilterType.Bump));
        ivHgayanOneFilter.setOnClickListener(applyFilterAux(FilterType.HgayanOne));
        ivHgayanThreeFilter.setOnClickListener(applyFilterAux(FilterType.HgayanThree));
        ivHgayanFourFilter.setOnClickListener(applyFilterAux(FilterType.HgayanFour));
        ivHgayanFiveFilter.setOnClickListener(applyFilterAux(FilterType.HgayanFive));
        ivHgayanSixFilter.setOnClickListener(applyFilterAux(FilterType.HgayanSix));
        ivHgayanSevenFilter.setOnClickListener(applyFilterAux(FilterType.HgayanSeven));
        ivHgayanEightFilter.setOnClickListener(applyFilterAux(FilterType.HgayanEight));
        ivHgayanTenFilter.setOnClickListener(applyFilterAux(FilterType.HgayanTen));
        ivHgayanElevenFilter.setOnClickListener(applyFilterAux(FilterType.HgayanEleven));
        ivHgayanTwelveFilter.setOnClickListener(applyFilterAux(FilterType.HgayanTwelve));
        ivHgayanFourteenFilter.setOnClickListener(applyFilterAux(FilterType.HgayanFourteen));
        ivHgayanFifteenFilter.setOnClickListener(applyFilterAux(FilterType.HgayanFifteen));
        ivHgayanSixteenFilter.setOnClickListener(applyFilterAux(FilterType.HgayanSixteen));
        ivVintageFilter.setOnClickListener(applyFilterAux(FilterType.Vintage));
        ivSepiaFilter.setOnClickListener(applyFilterAux(FilterType.Sepia));
        ivBrownishFilter.setOnClickListener(applyFilterAux(FilterType.Brownish));
        ivTintLowFilter.setOnClickListener(applyFilterAux(FilterType.Tint, 25));
        ivTintMediumFilter.setOnClickListener(applyFilterAux(FilterType.Tint, 50));
        ivTintHighFilter.setOnClickListener(applyFilterAux(FilterType.Tint, 75));
//        ivOtherFilter.setOnClickListener(applyFilterAux(FilterType.Other));
    }

    protected View.OnClickListener applyFilterAux(final FilterType filterType, final float... params) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (loadingImage)
                            showSnackBar(R.string.loading_image);
                        else if (invalidImage)
                            showSnackBar(R.string.invalid_image);
                        else {
                            try {
                                mutex.acquire();
                                final Interfaces.IImageContent aux = currentView;
                                loadingImage = true;
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
                                            showSnackBar(R.string.loading_image_error);
                                            removeSubFilter(filter.getSubFilters().size() - 1);
                                            showActionsLayout(true);
                                        } else {
                                            aux.setImageBitmap(bmCurrent);
                                            showActionsLayout(true);
                                        }
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

    protected void resetFilter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (loadingImage)
                    showSnackBar(R.string.loading_image);
                else if (invalidImage)
                    showSnackBar(R.string.invalid_image);
                else {
                    try {
                        mutex.acquire();
                        final Interfaces.IImageContent aux = currentView;
                        loadingImage = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msrlRefresh.setRefreshing(true);
                            }
                        });
                        final List<SubFilter> subFilters = new ArrayList<>();
                        for (SubFilter subFilter : filter.getSubFilters()) {
                            subFilters.add(subFilter);
                        }
                        clearSubFilters();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideActionsLayout(true);
                            }
                        });
                        final boolean load = loadBitmapForResource(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!load) {
                                    filter.addSubFilters(subFilters);
                                    showActionsLayout(true);
                                } else
                                    aux.setImageBitmap(bmCurrent);
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

    protected View.OnClickListener undoFilterOnClick () {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (loadingImage)
                            showSnackBar(R.string.loading_image);
                        else if (invalidImage)
                            showSnackBar(R.string.invalid_image);
                        else {
                            int countFilters = filter.getSubFilters().size();
                            if (countFilters > 0) {
                                if (countFilters == 1)
                                    resetFilter();
                                else {
                                    try {
                                        mutex.acquire();
                                        final Interfaces.IImageContent aux = currentView;
                                        loadingImage = true;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                msrlRefresh.setRefreshing(true);
                                            }
                                        });
                                        if (loadBitmapForResource(true)) {
                                            List<SubFilter> subFilters = filter.getSubFilters();
                                            final SubFilter removedSubFilter = subFilters
                                                    .remove(filter.getSubFilters().size() - 1);
                                            clearSubFilters();
                                            filter.addSubFilters(subFilters);
                                            final Bitmap copy = Bitmap.createBitmap(bmCurrent);
                                            final boolean error = !ApplyFilter(bmCurrent, filter, false);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (error) {
                                                        showSnackBar(R.string.loading_image_error);
                                                        filter.addSubFilter(removedSubFilter);
                                                        bmCurrent = copy;
                                                        showActionsLayout(true);
                                                    } else {
                                                        aux.setImageBitmap(bmCurrent);
                                                        recycleBitmap(copy);
                                                        showActionsLayout(true);
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
                }).start();
            }
        };
    }

    protected View.OnClickListener saveFilterOnClick () {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(new Interfaces.IPostOnSave() {
                    @Override
                    public void run(String path, String name) {
                        showSnackBar(String.format("%s \"%s\"", getString(R.string.save_image_complete),
                                name));
                    }
                });
            }
        };
    }

    protected void saveImage(final Interfaces.IPostOnSave postOnSave) {
        if (savingFilter)
            showSnackBar(R.string.saving_image_wait);
        else if (invalidImage)
            showSnackBar(R.string.invalid_image);
        else {
            try {
                mutexSave.acquire();
                savingFilter = true;
                showSnackBar(R.string.saving_image);
                final String name = resource.getName();
                final Bitmap aux = Bitmap.createBitmap(bmCurrent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String saveName = saveBitmapToAppFolder(aux, name);
                        if (saveName != null) {
                            postOnSave.run(String.format("file://%s/%s", saveFolderPath, saveName), saveName);
                            aux.recycle();
                        } else
                            showSnackBar(R.string.error_saving_image);
                        savingFilter = false;
                        mutexSave.release();
                    }
                }).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
                showSnackBar(R.string.error_saving_image);
                mutexSave.release();
            }
        }
    }

    private Point getImageDimension(int realWidth, int realHeight) {
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

    protected void loadFromBundle(Bundle bundle) {
        String name = bundle != null ? bundle.getString(DIRECTORY_KEY) : "";
        currentIndex = bundle != null ? bundle.getInt(INDEX_CURRENT_KEY) : 0;
        if (bundle != null && bundle.containsKey(STACK_VARS_KEY))
            stackVars = bundle.getParcelableArrayList(STACK_VARS_KEY);
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
                if (isEqual(current.getRelUrl(), resource.getRelUrl())) {
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

    protected void loadResource(Uri uri) {
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
                    final boolean isFavorite = isFavorite(resource);
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
                        isFavorite(resource);

                    if (resource.isFavorite())
                        resource.setIsFavorite(false, deleteFavoriteData(resource.getIdFavorite()));
                    else
                        resource.setIsFavorite(true, saveFavoriteData(new FavoriteDetails(resource.getRelUrl(),
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
                    if (loadingImage)
                        showSnackBar(R.string.loading_image);
                    else {
                        final String mimeType = resource.getMimeType();
                        Interfaces.IPostOnSave runnable = new Interfaces.IPostOnSave() {
                            @Override
                            public void run(final String path, String name) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        intent.setType(mimeType);
                                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                                        startActivity(Intent.createChooser(intent, getString(R.string.share)));
                                    }
                                });
                            }
                        };
                        if (!isEmptySubFiltersList())
                            saveImage(runnable);
                        else
                            runnable.run(resource.getAbsUrl(), resource.getName());
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnSetImageAs:
                //Set image as
                try {
                    if(loadingImage)
                        showSnackBar(R.string.loading_image);
                    else
                    {
                        final FileResource aux = resource;
                        Interfaces.IPostOnSave runnable = new Interfaces.IPostOnSave() {
                            @Override
                            public void run(final String path, String name) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                                        intent.putExtra(aux.getExtension(), aux.getMimeType());
                                        intent.setDataAndType(Uri.parse(path), aux.getMimeType());
                                        startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)));
                                    }
                                });
                            }
                        };
                        if (!isEmptySubFiltersList())
                            saveImage(runnable);
                        else
                            runnable.run(aux.getAbsUrl(), aux.getName());
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnRateApp:
                try {
                    rateApp();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                break;
            case R.id.mnAbout:
                showAboutDialog();
                break;
            case R.id.mnDetails:
                //Details
                try {
                    DetailsFragment details = new DetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(RESOURCE_FOR_DETAILS_KEY, resource);
                    bundle.putBoolean(OPEN_RESOURCE_ON_CLICKED, false);
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
        outState.putString(DIRECTORY_KEY, directory.getName());
        outState.putInt(INDEX_CURRENT_KEY, currentIndex);
        outState.putString(RESOURCE_PATH_KEY, resource.getAbsUrl());
        outState.putParcelableArrayList(STACK_VARS_KEY, stackVars);
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
                currentIndex = index;
                hideActionsLayout(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mutex.acquire();
                            if (currentIndex != index)
                                mutex.release();
                            else {
                                setCurrentView((Interfaces.IImageContent) view.findViewById(R.id .ivImageContent));
                                loadingImage = true;
                                if (stackVars != null && !stackVars.isEmpty())
                                    stackVars.get(stackVars.size() - 1).BeginPosition = index;
                                resource = (FileResource) directory.getResource(index);
                                clearSubFilters();
                                if (loadBitmapForResource(true)) {
                                    final Bitmap bm = bmCurrent;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            currentView.setImageBitmap(bm);
                                            invalidImage = loadingImage = false;
                                            msrlRefresh.setRefreshing(false);
                                            toolbar.setTitle(resource.getName());
                                            invalidateOptionsMenu();
                                            mutex.release();
                                        }
                                    });
                                } else {
                                    loadingImage = false;
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
                            invalidImage = loadingImage = false;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    msrlRefresh.setRefreshing(false);
                                }
                            });
                            mutex.release();
                        }
                    }
                }).start();
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
        if (layoutFilters.getAnimation() == null || (layoutFilters.getAnimation() != null && layoutFilters.getAnimation().hasEnded()))
            layoutFilters.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_layout));
        if (barImage.getAnimation() == null || (barImage.getAnimation() != null
                && barImage.getAnimation().hasEnded()))
            barImage.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_layout));
        showActionsLayout(false);
    }

    protected void showActionsLayout(boolean animFilters) {
        int size = getCountFilters();
        if (size > 0 && (layoutActions.getAnimation() == null || layoutActions.getAnimation().hasEnded())) {
            layoutActions.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v1));
            if (animFilters)
                layoutFilters.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v1));
            fbSave.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_save_button));
            fbUndo.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_undo_button));
            fbCancel.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_cancel_button));
        }
    }

    protected void hideActionsLayout(boolean animate) {
        if (animate && fbCancel.getAnimation() != null && !fbCancel.getAnimation().hasEnded()) {
            fbSave.setEnabled(false);
            fbUndo.setEnabled(false);
            fbCancel.setEnabled(false);
            layoutActions.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v2));
            fbSave.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_save_button));
            fbUndo.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_undo_button));
            fbCancel.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_cancel_button));
            fbCancel.postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    fbSave.setEnabled(true);
                    fbUndo.setEnabled(true);
                    fbCancel.setEnabled(true);
                }
            });
        }
        else if(!animate)
            layoutActions.clearAnimation();
    }
}