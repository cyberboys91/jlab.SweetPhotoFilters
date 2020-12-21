package jlab.SweetPhotoFilters.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.SubFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import jlab.SweetPhotoFilters.Activity.Fragment.DetailsFragment;
import jlab.SweetPhotoFilters.Filter.FilterType;
import jlab.SweetPhotoFilters.R;
import jlab.SweetPhotoFilters.Utils;
import jlab.SweetPhotoFilters.View.ImageSwipeRefreshLayout;
import jlab.SweetPhotoFilters.db.FavoriteDetails;

import static jlab.SweetPhotoFilters.Activity.DirectoryActivity.STACK_VARS_KEY;
import static jlab.SweetPhotoFilters.Utils.ApplyFilter;
import static jlab.SweetPhotoFilters.Utils.DIRECTORY_KEY;
import static jlab.SweetPhotoFilters.Utils.RESOURCE_FOR_DETAILS_KEY;
import static jlab.SweetPhotoFilters.Utils.deleteFavoriteData;
import static jlab.SweetPhotoFilters.Utils.isFavorite;
import static jlab.SweetPhotoFilters.Utils.rateApp;
import static jlab.SweetPhotoFilters.Utils.saveBitmapToAppFolder;
import static jlab.SweetPhotoFilters.Utils.saveFavoriteData;
import static jlab.SweetPhotoFilters.Utils.showAboutDialog;
import static jlab.SweetPhotoFilters.Utils.showSnackBar;
import static jlab.SweetPhotoFilters.Utils.stackVars;

/*
 * Created by Javier on 25/08/2018.
 */

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final short MAX_WIDTH_SUPPORT = 1080, MAX_HEIGHT_SUPPORT = 1080;
    private SurfaceView svCameraContent;
    private LinearLayout layoutActions;
    private Filter filter;
    private boolean loadingImage, savingFilter;
    private Bitmap bmCurrent;
    public Semaphore mutex = new Semaphore(1);
    public Semaphore mutexSave = new Semaphore(1);
    private FloatingActionButton fbSave, fbCancel;
    private Camera camera;
    private Toolbar toolbar;
    private int cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        int numCamera = Camera.getNumberOfCameras();
        cameraId = numCamera > 1 ? 1 : 0;
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar.setTitleTextAppearance(this, R.style.ToolBarApparence);
        setSupportActionBar(toolbar);
        this.layoutActions = (LinearLayout) findViewById(R.id.llActionButtons);
        fbSave = (FloatingActionButton) findViewById(R.id.fbSaveFilter);
        fbCancel = (FloatingActionButton) findViewById(R.id.fbCancelFilter);
        fbSave.setOnClickListener(saveFilterOnClick);
        fbCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
            }
        });
        this.svCameraContent = (SurfaceView) findViewById(R.id.ivCameraContent);
        this.svCameraContent.setOnClickListener(onClickLayoutListener);
        this.svCameraContent.getHolder().addCallback(this);
        this.filter = new Filter();
        Utils.currentActivity = this;
        loadFromBundle(savedInstanceState != null && savedInstanceState.containsKey(DIRECTORY_KEY)
                ? savedInstanceState : getIntent().getExtras());
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

    private boolean loadBitmapForResource(boolean showError) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(resource.getRelUrl(), options);
            Point dim = getImageDimension(options.outWidth, options.outHeight);
            options.inSampleSize = calculateInSampleSize(options, (int) dim.x, (int) dim.y);
            options.outWidth = (int) dim.x;
            options.outHeight = (int) dim.y;
            options.inJustDecodeBounds = false;
//            bmCurrent = BitmapFactory.decodeFile(resource.getRelUrl(), options);
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

    private void loadFiltersImages() {
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
                ivOilFilter = (ImageView) findViewById(R.id.ivOilFilter),
//                ivPointillizeFilter = (ImageView) findViewById(R.id.ivPointillizeFilter),
//                ivOffsetFilter = (ImageView) findViewById(R.id.ivOffsetFilter),
                ivNoiseFilter = (ImageView) findViewById(R.id.ivNoiseFilter),
                ivMinimumFilter = (ImageView) findViewById(R.id.ivMinimumFilter),
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
        ivOilFilter.setOnClickListener(applyFilterAux(FilterType.Oil));
//        ivOffsetFilter.setOnClickListener(applyFilterAux(FilterType.Offset));
        ivNoiseFilter.setOnClickListener(applyFilterAux(FilterType.Noise));
        ivMinimumFilter.setOnClickListener(applyFilterAux(FilterType.Minimum));
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

    private View.OnClickListener applyFilterAux(final FilterType filterType, final float... params) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (loadingImage)
                            showSnackBar(R.string.loading_image);
                        else {
                            try {
                                mutex.acquire();
                                loadingImage = true;
                                filter = new Filter();
                                ApplyFilter(filter, filterType, params);
                                final boolean error = !ApplyFilter(bmCurrent, filter, true);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (error) {
                                            showSnackBar(R.string.loading_image_error);
                                            filter.getSubFilters().remove(filter.getSubFilters().size() - 1);
                                            showActionsLayout();
                                        } else {
                                            //TODO: svCameraContent.setImageBitmap(bmCurrent);
                                            showActionsLayout();
                                        }
                                        mutex.release();
                                        loadingImage = false;
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                mutex.release();
                                loadingImage = false;
                            }
                        }
                    }
                }).start();
            }
        };
    }

    private void resetFilter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (loadingImage)
                    showSnackBar(R.string.loading_image);
                else {
                    try {
                        mutex.acquire();
                        loadingImage = true;
                        final List<SubFilter> subFilters = new ArrayList<>();
                        for (SubFilter subFilter : filter.getSubFilters()) {
                            subFilters.add(subFilter);
                        }
                        filter.clearSubFilters();
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
                                    showActionsLayout();
                                } else
                                    //TODO: svCameraContent.setImageBitmap(bmCurrent);
                                mutex.release();
                                loadingImage = false;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        mutex.release();
                        loadingImage = false;
                    }
                }
            }
        }).start();
    }

    private View.OnClickListener saveFilterOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (savingFilter)
                showSnackBar(R.string.saving_image_wait);
            else {
                try {
                    mutexSave.acquire();
                    savingFilter = true;
                    showSnackBar(R.string.saving_image);
                    Date date = new Date();
                    final String name = "IMG_" + date.toString();
                    final Bitmap aux = Bitmap.createBitmap(bmCurrent);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String saveName = saveBitmapToAppFolder(aux, name);
                            if (saveName != null) {
                                showSnackBar(String.format("%s \"%s\"", getString(R.string.save_image_complete),
                                        saveName));
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
    };

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

    private void loadFromBundle(Bundle bundle) {
        String name = bundle != null ? bundle.getString(DIRECTORY_KEY) : "";
        if (bundle != null && bundle.containsKey(STACK_VARS_KEY))
            stackVars = bundle.getParcelableArrayList(STACK_VARS_KEY);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STACK_VARS_KEY, stackVars);
    }

    public View.OnClickListener onClickLayoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showActionsLayout();
        }
    };

    private void showActionsLayout() {
        int size = filter.getSubFilters().size();
        if (size > 0 && (layoutActions.getAnimation() == null || layoutActions.getAnimation().hasEnded())) {
            layoutActions.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v1));
            fbSave.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_save_button));
            fbCancel.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_cancel_button));
        }
    }

    private void hideActionsLayout(boolean animate) {
        if (animate && fbCancel.getAnimation() != null && !fbCancel.getAnimation().hasEnded()) {
            fbSave.setEnabled(false);
            fbCancel.setEnabled(false);
            layoutActions.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.out_layout_v2));
            fbSave.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_save_button));
            fbCancel.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.in_out_cancel_button));
            fbCancel.postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    fbSave.setEnabled(true);
                    fbCancel.setEnabled(true);
                }
            });
        }
        else if(!animate)
            layoutActions.clearAnimation();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(cameraId);
        try {
            setCameraDisplayOrientation(cameraId);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            setCameraDisplayOrientation(cameraId);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
    }

    public void setCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnRotateCamera:
                cameraId = (cameraId + 1) % Camera.getNumberOfCameras();
                camera.release();
                surfaceCreated(svCameraContent.getHolder());
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
            case R.id.mnClose:
                finish();
                break;
        }
        return true;
    }

}