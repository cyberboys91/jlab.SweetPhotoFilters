package jlab.SweetPhotoFilters;

import android.view.View;
import android.graphics.Bitmap;
import android.widget.ImageView;
import java.util.concurrent.Semaphore;
import jlab.SweetPhotoFilters.db.FavoriteDetails;
import jlab.SweetPhotoFilters.Resource.FileResource;

/*
 * Created by Javier on 7/9/2016.
 */
public class LoaderImageTask {
    private ImageView mResIcon, mivFavorite;
    private static Semaphore semaphore = new Semaphore(2);
    public static OnSetImageIconUIThread monSetImageIcon = new OnSetImageIconUIThread() {
        @Override
        public void setImage(ImageView imageView, FileResource file) {

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

    public LoaderImageTask(ImageView rico, ImageView ivFavorite) {
        this.mResIcon = rico;
        this.mivFavorite = ivFavorite;
    }

    public void load(final FileResource file, final boolean isAlbum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                    monSetImageIcon.setImage(mResIcon, file);
                    //TODO: ineficiente la variante comentada
                    //monSetImageIcon.setImage(mResIcon, Utils.getThumbnailForUriFile(file.getRelUrl(), file));
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            }
        }).start();
    }

    public interface OnSetImageIconUIThread
    {
        void setImage(ImageView imageView, FileResource file);
        void setImage(ImageView imageView, Bitmap image);
        void setImage(ImageView imageView, int idRes);
        void runOnUserInterfaceThread(Runnable runnable);
    }
}