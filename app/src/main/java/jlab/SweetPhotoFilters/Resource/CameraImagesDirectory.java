package jlab.SweetPhotoFilters.Resource;

import jlab.SweetPhotoFilters.R;
import android.os.Handler;
import java.util.ArrayList;
import java.util.Comparator;
import android.os.Environment;
import jlab.SweetPhotoFilters.Utils;
import static java.util.Collections.sort;

/*
 * Created by Javier on 07/08/2018.
 */

public class CameraImagesDirectory extends FilesLocalDirectory {

    public CameraImagesDirectory(String name) {
        super(name, R.color.orange);
    }

    @Override
    public void openSynchronic(Handler handler) {
        synchronized (monitor) {
            try {
                if (!loaded) {
                    clear();
                    ocupedSpace = 0;
                    ArrayList<String> stgPath = Utils.getStoragesPath();
                    for (int i = 0; i < stgPath.size(); i++) {
                        String current = stgPath.get(i);
                        if (Utils.existAndMountDir(current))
                            loadContentForDir(String.format("%s/%s/Camera/", current, Environment.DIRECTORY_DCIM));
                    }
                    sort(getContent(), new Comparator<Resource>() {
                        @Override
                        public int compare(Resource res1, Resource res2) {
                            if (res1.modification > res2.modification)
                                return -1;
                            if (res1.modification < res2.modification)
                                return 1;
                            return 0;
                        }
                    });
                }
                Utils.lostConnection = false;
            } catch (Exception ignored) {
                Utils.lostConnection = true;
                ignored.printStackTrace();
            } finally {
                loaded = true;
                if (handler != null)
                    handler.sendEmptyMessage(Utils.LOADING_INVISIBLE);
            }
        }
    }

    private void loadContentForDir(String path) {
        LocalDirectory directory = new LocalDirectory("", path, "", false, 0);
        directory.openSynchronic(null);
        for (int i = 0; i < directory.getCountElements(); i++) {
            Resource current = directory.getResource(i);
            if ((Utils.showHiddenFiles || !current.isHidden())
                    && !current.isDir() && ((FileResource) current).isImage()
                    && (path + current.getName()).equals(current.getRelUrl())) {
                LocalFile localFile = (LocalFile) directory.getResource(i);
                addResource(localFile);
            }
        }
    }

    @Override
    public boolean isMultiColumn() {
        return true;
    }

    @Override
    public void loadData() {
        openSynchronic(null);
    }
}
