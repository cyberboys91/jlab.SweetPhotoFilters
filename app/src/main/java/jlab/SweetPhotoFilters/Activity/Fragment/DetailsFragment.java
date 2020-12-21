package jlab.SweetPhotoFilters.Activity.Fragment;

import android.animation.Animator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.content.Context;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import jlab.SweetPhotoFilters.Activity.DirectoryActivity;
import jlab.SweetPhotoFilters.R;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import jlab.SweetPhotoFilters.Resource.AlbumDirectory;
import jlab.SweetPhotoFilters.Resource.Directory;
import jlab.SweetPhotoFilters.Utils;
import android.support.v7.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.MediaStoreSignature;

import java.util.ArrayList;

import jlab.SweetPhotoFilters.Resource.Resource;
import jlab.SweetPhotoFilters.Resource.FileResource;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static jlab.SweetPhotoFilters.Activity.DirectoryActivity.fromPoint;
import static jlab.SweetPhotoFilters.Activity.DirectoryActivity.iconSize;
import static jlab.SweetPhotoFilters.Utils.getDimensionScreen;

public class DetailsFragment extends DialogFragment {

    private Resource resource;
    private View detailsView;
    private ImageView image;
    private boolean stopThread;
    private String monitor = "monitor";
    private View rootView;
    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setCancelable(false);
        this.resource = (Resource) getArguments().getSerializable(Utils.RESOURCE_FOR_DETAILS_KEY);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    private ArrayList<Resource> getResourceForImage () {
        if (resource instanceof AlbumDirectory && !((AlbumDirectory) resource).isEmpty())
            return ((AlbumDirectory) resource).getContent();
        ArrayList<Resource> result = new ArrayList<>();
        result.add(resource);
        return result;
    }

    @Override
    public AlertDialog onCreateDialog(Bundle saveInstance) {
        stopThread = false;
        Context context = getActivity().getBaseContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        detailsView = inflater.inflate(R.layout.resource_details_dialog, null, false);
        TextView name = detailsView.findViewById(R.id.tvResourceNameDetails),
                path = detailsView.findViewById(R.id.tvResourcePathDetails),
                size = detailsView.findViewById(R.id.tvResourceSizeDetails),
                modification = detailsView.findViewById(R.id.tvResourceModificationDetails);
        name.setText(resource.getName());
        path.setText(resource.getRelUrl());
        modification.setText(resource.getModificationDateLong());

        if (resource.isDir())
            size.setText(String.format("%s %s", ((AlbumDirectory) resource).getCountElements()
                    , getString(R.string.elements)));
        else
            size.setText(((FileResource) resource).sizeToString());


        dialog = new AlertDialog.Builder(inflater.getContext())
                .setView(detailsView)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.open), null)
                .setNegativeButton(getString(R.string.close), null)
                .create();
        return dialog;
    }

    private void openResource (final Resource elem) {
        if (DirectoryActivity.mlcResourcesDir != null)
            DirectoryActivity.mlcResourcesDir.openResource(elem,
                    elem.getIndexPattern(), fromPoint);
    }

    private void beginImageViewAnim() {
        final DisplayMetrics dimen = Utils.getDimensionScreen();
        image.startAnimation(AnimationUtils.loadAnimation(image.getContext(), R.anim.beat));
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Resource> resourcesForImage = getResourceForImage();
                final boolean[] first = {true};
                for (int i = 0; i < resourcesForImage.size() && !stopThread; i++) {
                    final FileResource current = (FileResource) resourcesForImage.get(i);
                    synchronized (monitor) {
                        if(!stopThread)
                            try {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(image).load(current.getRelUrl())
                                                .apply(new RequestOptions().signature(new MediaStoreSignature(current.getMimeType(),
                                                        current.getModificationDate(), 0)).centerCrop())
                                                .into(image);
                                        if (!first[0] && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                            final Animator animator = ViewAnimationUtils.createCircularReveal(image,
                                                    image.getWidth() / 2, image.getHeight() / 2,
                                                    0, max(dimen.widthPixels, dimen.heightPixels));
                                            animator.setDuration(1000);
                                            animator.start();
                                        }
                                        first[0] = false;
                                        image.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                endAnimation(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        openResource(resource);
                                                        dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }catch (Exception ignored) {
                                ignored.printStackTrace();
                            }
                        else
                            break;
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i != 0 && i == resourcesForImage.size() - 1)
                        i = -1;
                }
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        startAnimation();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        synchronized (monitor) {
            stopThread = true;
            super.onDismiss(dialog);
        }
    }

    private void startAnimation() {
        Button close = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        close.setTextColor(getResources().getColor(R.color.red_dark));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endAnimation(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
            }
        });
        Button open = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endAnimation(new Runnable() {
                    @Override
                    public void run() {
                        openResource(resource);
                        dismiss();
                    }
                });
            }
        });
        DisplayMetrics dimen = Utils.getDimensionScreen();
        image = detailsView.findViewById(R.id.ivResourceIcon);
        ViewGroup.LayoutParams lp = image
                .getLayoutParams();
        lp.height = Math.min(dimen.widthPixels / 2, dimen.heightPixels / 2);
        image.setLayoutParams(lp);
        View view = detailsView;
        ViewParent parent = view.getParent();
        while (parent != null) {
            parent = parent.getParent();
            if (parent instanceof View) {
                ((View) parent).setBackgroundResource(R.color.transparent);
                view.setBackgroundResource(R.color.white);
                view = (View) parent;
                view.setPadding(0, 0, 0, 0);
            }
        }
        rootView = view;
        rootView.post(new Runnable() {
            @Override
            public void run() {
                beginImageViewAnim();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Animator animator = ViewAnimationUtils.createCircularReveal(rootView,
                            fromPoint.x + iconSize / 2,
                            fromPoint.y + iconSize / 2,
                            0, max(rootView.getWidth(), rootView.getHeight()));
                    animator.setDuration(400);
                    animator.start();
                }
            }
        });
    }

    private void endAnimation (final Runnable onEndAnimator) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Animator animator = ViewAnimationUtils.createCircularReveal(rootView,
                    fromPoint.x + iconSize / 2,
                    fromPoint.y + iconSize / 2,
                    max(rootView.getWidth(), rootView.getHeight()), 0);
            animator.setDuration(400);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    onEndAnimator.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    onEndAnimator.run();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
        else
            onEndAnimator.run();
    }
}