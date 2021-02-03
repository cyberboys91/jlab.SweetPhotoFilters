package jlab.SweetPhotoFilters.Activity;

import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import java.io.IOException;

import jlab.SweetPhotoFilters.Filter.FilterType;
import jlab.SweetPhotoFilters.Filter.gpu.GPUImageFilterGroup;
import jlab.SweetPhotoFilters.Filter.gpu.GPUImageView;
import jlab.SweetPhotoFilters.Interfaces;
import jlab.SweetPhotoFilters.R;
import static jlab.SweetPhotoFilters.Utils.ApplyGPUFilter;
import static jlab.SweetPhotoFilters.Utils.saveFolderPath;
import static jlab.SweetPhotoFilters.Utils.saveGPUImageToAppFolder;
import static jlab.SweetPhotoFilters.Utils.showSnackBar;

/*
 * Created by Javier on 02/02/2021.
 */

//TODO: Incomplete
public class GPUImageViewActivity extends ImageViewActivity {

    protected GPUImageFilterGroup filter;

    @Override
    protected void initFilter() {
        filter = new GPUImageFilterGroup();
    }

    @Override
    protected void loadFiltersImages() {
        final ImageView ivBrightnessFilter = findViewById(R.id.ivDefaultBrightnessFilter),
                ivHightBrightnessFilter = findViewById(R.id.ivHightBrightnessFilter),
                ivBlackAndWhiteFilter = findViewById(R.id.ivBlackAndWhiteFilter),
                ivLowBrightnessFilter = findViewById(R.id.ivLowBrightnessFilter),
                ivLowSaturationFilter = findViewById(R.id.ivLowSaturationFilter),
                ivDefaultSaturationFilter = findViewById(R.id.ivDefaultSaturationFilter),
                ivHightSaturationFilter = findViewById(R.id.ivHightSaturationFilter),
                ivColorOverlayFilter = findViewById(R.id.ivColorOverlayFilter),
                ivContrastFilter = findViewById(R.id.ivContrastFilter),
                ivVignetteFilter = findViewById(R.id.ivVignetteFilter),
                ivAverageSmoothFilter = findViewById(R.id.ivAverageSmoothFilter),
                ivGammaCorrectionAFilter = findViewById(R.id.ivGammaCorrectionAFilter),
                ivGammaCorrectionBFilter = findViewById(R.id.ivGammaCorrectionBFilter),
                ivGammaCorrectionCFilter = findViewById(R.id.ivGammaCorrectionCFilter),
                ivGammaCorrectionDFilter = findViewById(R.id.ivGammaCorrectionDFilter),
                ivXORFilter = findViewById(R.id.ivXORFilter),
                ivORFilter = findViewById(R.id.ivORFilter),
                ivLightFilter = findViewById(R.id.ivLightFilter),
                ivPixelateFilter = findViewById(R.id.ivPixelateFilter),
                ivSketchFilter = findViewById(R.id.ivSketchFilter),
//                ivTvFilter = findViewById(R.id.ivTvFilter),
//                ivWeaveFilter = findViewById(R.id.ivWeaveFilter),
                ivUnsharpFilter = findViewById(R.id.ivUnsharpFilter),
                ivThresholdFilter = findViewById(R.id.ivThresholdFilter),
                ivStampFilter = findViewById(R.id.ivStampFilter),
                ivSolarizeFilter = findViewById(R.id.ivSolarizeFilter),
                ivSmearFilter = findViewById(R.id.ivSmearFilter),
                ivSharpenFilter = findViewById(R.id.ivSharpenFilter),
                ivRippleFilter = findViewById(R.id.ivRippleFilter),
                ivRescaleFilter = findViewById(R.id.ivRescaleFilter),
                ivQuantizeFilter = findViewById(R.id.ivQuantizeFilter),
//                ivOtherFilter = findViewById(R.id.ivOtherFilter),
                ivPosterizeFilter = findViewById(R.id.ivPosterizeFilter),
//                ivOilFilter = findViewById(R.id.ivOilFilter),
//                ivPointillizeFilter = findViewById(R.id.ivPointillizeFilter),
//                ivOffsetFilter = findViewById(R.id.ivOffsetFilter),
                ivNoiseFilter = findViewById(R.id.ivNoiseFilter),
//                ivMinimumFilter = findViewById(R.id.ivMinimumFilter),
                ivMaskFilter = findViewById(R.id.ivMaskFilter),
                ivMarbleFilter = findViewById(R.id.ivMarbleFilter),
                ivInvertFilter = findViewById(R.id.ivInvertFilter),
                ivGaussianFilter = findViewById(R.id.ivGaussianFilter),
                ivExposeFilter = findViewById(R.id.ivExposeFilter),
//                ivFlipFilter = findViewById(R.id.ivFlipFilter),
                ivEmbossFilter = findViewById(R.id.ivEmbossFilter),
                ivEdgeFilter = findViewById(R.id.ivEdgeFilter),
                ivDiffuseFilter = findViewById(R.id.ivDiffuseFilter),
//                ivCrystallizeFilter = findViewById(R.id.ivCrystallizeFilter),
                ivDisplaceFilter = findViewById(R.id.ivDisplaceFilter),
                ivContourFilter = findViewById(R.id.ivContourFilter),
//                ivColorHalftoneFilter = findViewById(R.id.ivColorHalftoneFilter),
                ivBumpFilter = findViewById(R.id.ivBumpFilter),
                ivHgayanOneFilter = findViewById(R.id.ivHgayanOneFilter),
//                ivHgayanTwoFilter = findViewById(R.id.ivHgayanTwoFilter),
                ivHgayanThreeFilter = findViewById(R.id.ivHgayanThreeFilter),
                ivHgayanFourFilter = findViewById(R.id.ivHgayanFourFilter),
                ivHgayanFiveFilter = findViewById(R.id.ivHgayanFiveFilter),
                ivHgayanSixFilter = findViewById(R.id.ivHgayanSixFilter),
                ivHgayanSevenFilter = findViewById(R.id.ivHgayanSevenFilter),
                ivHgayanEightFilter = findViewById(R.id.ivHgayanEightFilter),
//                ivHgayanNineFilter = findViewById(R.id.ivHgayanNineFilter),
                ivHgayanTenFilter = findViewById(R.id.ivHgayanTenFilter),
                ivHgayanElevenFilter = findViewById(R.id.ivHgayanElevenFilter),
                ivHgayanTwelveFilter = findViewById(R.id.ivHgayanTwelveFilter),
                ivHgayanFourteenFilter = findViewById(R.id.ivHgayanFourteenFilter),
                ivHgayanFifteenFilter = findViewById(R.id.ivHgayanFifteenFilter),
                ivHgayanSixteenFilter = findViewById(R.id.ivHgayanSixteenFilter),
                ivVintageFilter = findViewById(R.id.ivVintageFilter),
                ivSepiaFilter = findViewById(R.id.ivSepiaFilter),
                ivBrownishFilter = findViewById(R.id.ivBrownishFilter),
                ivTintLowFilter = findViewById(R.id.ivTintLowFilter),
                ivTintMediumFilter = findViewById(R.id.ivTintMediumFilter),
                ivTintHighFilter = findViewById(R.id.ivTintHighFilter);

        ivBrightnessFilter.setOnClickListener(applyFilterAux(FilterType.GPUSwirlFilter));
        ivHightBrightnessFilter.setOnClickListener(applyFilterAux(FilterType.GPUAddBlend));
        ivLowBrightnessFilter.setOnClickListener(applyFilterAux(FilterType.GPUALphaBlend));
        ivBlackAndWhiteFilter.setOnClickListener(applyFilterAux(FilterType.GPUBilateral));
        ivLowSaturationFilter.setOnClickListener(applyFilterAux(FilterType.GPUBoxBlur));
        ivDefaultSaturationFilter.setOnClickListener(applyFilterAux(FilterType.GPUBrightness));
        ivHightSaturationFilter.setOnClickListener(applyFilterAux(FilterType.GPUBulgeDistortion));
        ivColorOverlayFilter.setOnClickListener(applyFilterAux(FilterType.GPUCGAColorSpace));
        ivContrastFilter.setOnClickListener(applyFilterAux(FilterType.GPUChromaKeyBlend));
        ivVignetteFilter.setOnClickListener(applyFilterAux(FilterType.GPUColorBalance));
        ivAverageSmoothFilter.setOnClickListener(applyFilterAux(FilterType.GPUColorBlend));
        ivGammaCorrectionAFilter.setOnClickListener(applyFilterAux(FilterType.GPUColorBurnBlend));
        ivGammaCorrectionBFilter.setOnClickListener(applyFilterAux(FilterType.GPUColorDodgeBlend));
        ivGammaCorrectionCFilter.setOnClickListener(applyFilterAux(FilterType.GPUColorInvert));
        ivGammaCorrectionDFilter.setOnClickListener(applyFilterAux(FilterType.GPUColorMatrix));
        ivXORFilter.setOnClickListener(applyFilterAux(FilterType.GPUContrast));
        ivORFilter.setOnClickListener(applyFilterAux(FilterType.GPUCrosshatch));
        ivLightFilter.setOnClickListener(applyFilterAux(FilterType.GPUDarkenBlend));
        ivPixelateFilter.setOnClickListener(applyFilterAux(FilterType.GPUDifferenceBlend));
        ivSketchFilter.setOnClickListener(applyFilterAux(FilterType.GPUDilation));
//        ivTvFilter.setOnClickListener(applyFilterAux(FilterType.Tv));
//        ivWeaveFilter.setOnClickListener(applyFilterAux(FilterType.Weave));
        ivUnsharpFilter.setOnClickListener(applyFilterAux(FilterType.GPUDirectionalSobelEdgeDetection));
        ivThresholdFilter.setOnClickListener(applyFilterAux(FilterType.GPUDissolveBlend));
        ivStampFilter.setOnClickListener(applyFilterAux(FilterType.GPUDivideBlend));
        ivSolarizeFilter.setOnClickListener(applyFilterAux(FilterType.GPUEmboss));
        ivSmearFilter.setOnClickListener(applyFilterAux(FilterType.GPUExclusionBlend));
        ivSharpenFilter.setOnClickListener(applyFilterAux(FilterType.GPUFalseColor));
        ivRippleFilter.setOnClickListener(applyFilterAux(FilterType.GPUGaussianBlur));
        ivRescaleFilter.setOnClickListener(applyFilterAux(FilterType.GPUGlassSphere));
        ivQuantizeFilter.setOnClickListener(applyFilterAux(FilterType.GPUGrayscale));
        ivPosterizeFilter.setOnClickListener(applyFilterAux(FilterType.GPUHalftone));
//        ivPointillizeFilter.setOnClickListener(applyFilterAux(FilterType.Pointillize));
//        ivOilFilter.setOnClickListener(applyFilterAux(FilterType.Oil));
//        ivOffsetFilter.setOnClickListener(applyFilterAux(FilterType.Offset));
        ivNoiseFilter.setOnClickListener(applyFilterAux(FilterType.GPUHardLightBlend));
//        ivMinimumFilter.setOnClickListener(applyFilterAux(FilterType.Minimum));
        ivMaskFilter.setOnClickListener(applyFilterAux(FilterType.GPUHaze));
        ivInvertFilter.setOnClickListener(applyFilterAux(FilterType.GPUHighlightShadow));
        ivMarbleFilter.setOnClickListener(applyFilterAux(FilterType.GPUHueBlend));
        ivGaussianFilter.setOnClickListener(applyFilterAux(FilterType.GPUHue));
//        ivFlipFilter.setOnClickListener(applyFilterAux(FilterType.Flip));
        ivExposeFilter.setOnClickListener(applyFilterAux(FilterType.GPUKuwahara));
        ivEmbossFilter.setOnClickListener(applyFilterAux(FilterType.GPULaplacian));
        ivEdgeFilter.setOnClickListener(applyFilterAux(FilterType.GPULevels));
        ivDisplaceFilter.setOnClickListener(applyFilterAux(FilterType.GPULightenBlend));
        ivDiffuseFilter.setOnClickListener(applyFilterAux(FilterType.GPULinearBurnBlend));
//        ivCrystallizeFilter.setOnClickListener(applyFilterAux(FilterType.Crystallize));
        ivContourFilter.setOnClickListener(applyFilterAux(FilterType.GPULookup));
//        ivColorHalftoneFilter.setOnClickListener(applyFilterAux(FilterType.ColorHalftone));
        ivBumpFilter.setOnClickListener(applyFilterAux(FilterType.GPULuminosityBlend));
        ivHgayanOneFilter.setOnClickListener(applyFilterAux(FilterType.GPUMonochrome));
        ivHgayanThreeFilter.setOnClickListener(applyFilterAux(FilterType.GPUMultiplyBlend));
        ivHgayanFourFilter.setOnClickListener(applyFilterAux(FilterType.GPUNonMaximumSuppression));
        ivHgayanFiveFilter.setOnClickListener(applyFilterAux(FilterType.GPUNormalBlend));
        ivHgayanSixFilter.setOnClickListener(applyFilterAux(FilterType.GPUOpacity));
        ivHgayanSevenFilter.setOnClickListener(applyFilterAux(FilterType.GPUOverlayBlend));
        ivHgayanEightFilter.setOnClickListener(applyFilterAux(FilterType.GPUPixelation));
        ivHgayanTenFilter.setOnClickListener(applyFilterAux(FilterType.GPUPosterize));
        ivHgayanElevenFilter.setOnClickListener(applyFilterAux(FilterType.GPUDilation));
        ivHgayanTwelveFilter.setOnClickListener(applyFilterAux(FilterType.GPURGB));
        ivHgayanFourteenFilter.setOnClickListener(applyFilterAux(FilterType.Saturation));
        ivHgayanFifteenFilter.setOnClickListener(applyFilterAux(FilterType.GPUScreenBlend));
        ivHgayanSixteenFilter.setOnClickListener(applyFilterAux(FilterType.GPUSepia));
        ivVintageFilter.setOnClickListener(applyFilterAux(FilterType.GPUSharpen));
        ivSepiaFilter.setOnClickListener(applyFilterAux(FilterType.GPUSketch));
        ivBrownishFilter.setOnClickListener(applyFilterAux(FilterType.GPUSmoothToon));
        ivTintLowFilter.setOnClickListener(applyFilterAux(FilterType.GPUSobelEdgeDetection));
        ivTintMediumFilter.setOnClickListener(applyFilterAux(FilterType.GPUSobelThreshold));
        ivTintHighFilter.setOnClickListener(applyFilterAux(FilterType.GPUSoftLightBlend));
//        ivOtherFilter.setOnClickListener(applyFilterAux(FilterType.Other));
    }

    @Override
    protected View.OnClickListener applyFilterAux(final FilterType filterType, float ...params) {
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
                                loadingImage = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        msrlRefresh.setRefreshing(true);
                                    }
                                });
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ApplyGPUFilter(filter, filterType);
                                        currentView.setFilter(filter);
                                        showActionsLayout(true);
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

    @Override
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
                        loadingImage = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msrlRefresh.setRefreshing(true);
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideActionsLayout(true);
                            }
                        });
                        filter.clearFilters();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentView.setFilter(filter);
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

    @Override
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
                            int countFilters = filter.getFilters().size();
                            if (countFilters > 0) {
                                if (countFilters == 1)
                                    resetFilter();
                                else {
                                    try {
                                        mutex.acquire();
                                        loadingImage = true;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                msrlRefresh.setRefreshing(true);
                                            }
                                        });
                                        filter.removeFilter(getCountFilters() - 1);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                currentView.setFilter(filter);
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
                        }
                    }
                }).start();
            }
        };
    }

    @Override
    protected void saveImage (final Interfaces.IPostOnSave postOnSave) {
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
                //TODO: Revisar
                saveGPUImageToAppFolder((GPUImageView) currentView, name, new Interfaces.IPostOnSave() {
                    @Override
                    public void run(String path, String name) {
                        if (name != null) {
                            postOnSave.run(String.format("file://%s/%s", saveFolderPath, name), name);
                        } else
                            showSnackBar(R.string.error_saving_image);
                        savingFilter = false;
                        mutexSave.release();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                showSnackBar(R.string.error_saving_image);
                mutexSave.release();
            }
        }
    }

    @Override
    public View getView(ViewGroup parent, int position, boolean isDir) {
        return mlInflater.inflate(R.layout.gpu_image_view, parent, false);
    }

    @Override
    protected int getCountFilters() {
        return filter.getFilters().size();
    }

    @Override
    protected void clearSubFilters() {
        this.filter.getFilters().clear();
    }

    @Override
    protected void removeSubFilter(int index) {
        this.filter.getFilters().remove(index);
    }
}