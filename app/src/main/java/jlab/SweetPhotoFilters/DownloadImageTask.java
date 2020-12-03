package jlab.SweetPhotoFilters;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import jlab.SweetPhotoFilters.Resource.FileResource;

/*
 * Created by Javier on 7/9/2016.
 */
public class DownloadImageTask {
    private ImageView mResIcon, mivFavorite;
    public static OnSetImageIconUIThread monSetImageIcon = new OnSetImageIconUIThread() {
        @Override
        public void setImage(ImageView imageView, String path) {

        }

        @Override
        public void setImage(ImageView imageView, Bitmap image) {

        }

        @Override
        public void setImage(ImageView imageView, int idRes) {

        }

        @Override
        public void runOnUserInterfaceThread(Runnable runnable) {

        }
    };

    public DownloadImageTask(ImageView rico, ImageView ivFavorite, int idResource, boolean setBackground, boolean isMultiColumn) {
        this.mResIcon = rico;
        this.mivFavorite = ivFavorite;
        this.mResIcon.setImageResource(idResource);
    }

    public void load(final FileResource file, final boolean isAlbum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                monSetImageIcon.setImage(mResIcon, file.getRelUrl());
                if (!isAlbum && !file.getFavoriteStateLoad() && mivFavorite != null) {
                    final boolean isFavorite = Utils.isFavorite(file);
                    monSetImageIcon.setImage(mivFavorite, isFavorite
                            ? R.drawable.img_favorite_checked
                            : R.drawable.img_favorite_not_checked);
                    monSetImageIcon.runOnUserInterfaceThread(new Runnable() {
                        @Override
                        public void run() {
                            mivFavorite.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (file.isFavorite())
                                        file.setIsFavorite(false, Utils.deleteFavoriteData(file.getIdFavorite()));
                                    else
                                        file.setIsFavorite(true, Utils.saveFavoriteData(new FavoriteDetails(file.getRelUrl(),
                                                file.getComment(), file.getParentName(), file.mSize, file.getModificationDate())));
                                    monSetImageIcon.setImage(mivFavorite, file.isFavorite()
                                            ? R.drawable.img_favorite_checked
                                            : R.drawable.img_favorite_not_checked);
                                }
                            });
                            mivFavorite.setOnLongClickListener(new View.OnLongClickListener() {
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

    public interface OnSetImageIconUIThread
    {
        void setImage(ImageView imageView, String path);
        void setImage(ImageView imageView, Bitmap image);
        void setImage(ImageView imageView, int idRes);
        void runOnUserInterfaceThread(Runnable runnable);
    }
}