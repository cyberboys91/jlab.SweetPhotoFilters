package jlab.SweetPhotoFilters.db;

import android.provider.BaseColumns;

/*
 * Created by Javier on 24/04/2017.
 */
public class FavoriteContract implements BaseColumns {
    public static final String TABLE_NAME = "favorite";
    public static final String COMMENT = "comment";
    public static final String PATH = "path";
    public static final String SIZE = "size";
    public static final String MODIFICATION_DATE = "modification";
    public static final String PARENT_NAME = "parent";
}