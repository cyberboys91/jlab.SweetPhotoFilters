package jlab.SweetPhotoFilters.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Created by Javier on 24/04/2017.
 */
public class FavoriteDbManager extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sweetPhotoFilters.db";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FavoriteContract.TABLE_NAME;

    public FavoriteDbManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + FavoriteContract.TABLE_NAME + " ("
                    + FavoriteContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + FavoriteContract.PATH + " TEXT NOT NULL,"
                    + FavoriteContract.SIZE + " BIGINT NOT NULL,"
                    + FavoriteContract.MODIFICATION_DATE + " BIGINT NOT NULL,"
                    + FavoriteContract.PARENT_NAME + " TEXT,"
                    + FavoriteContract.COMMENT + " TEXT NOT NULL)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long saveFavoriteData(FavoriteDetails favorite) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.insert(FavoriteContract.TABLE_NAME, null, favorite.toContentValues());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    public ArrayList<FavoriteDetails> getFavoriteData() {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ArrayList<FavoriteDetails> result = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(FavoriteContract.TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(FavoriteContract.PATH)),
                    comment = cursor.getString(cursor.getColumnIndex(FavoriteContract.COMMENT)),
                    parent = cursor.getString(cursor.getColumnIndex(FavoriteContract.PARENT_NAME));
            int id = cursor.getInt(cursor.getColumnIndex(FavoriteContract._ID));
            long size = cursor.getLong(cursor.getColumnIndex(FavoriteContract.SIZE)),
                    modification = cursor.getLong(cursor.getColumnIndex(FavoriteContract.MODIFICATION_DATE));
            result.add(new FavoriteDetails(id, path, comment, parent, size, modification));
        }
        cursor.close();
        return result;
    }

    public int updateFavoriteData(long id, FavoriteDetails newFavoriteDetails) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.update(FavoriteContract.TABLE_NAME,
                newFavoriteDetails.toContentValues(),
                FavoriteContract._ID + " LIKE ?",
                new String[]{Long.toString(id)});
    }

    public int deleteFavoriteData(long id) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.delete(FavoriteContract.TABLE_NAME,
                FavoriteContract._ID + " LIKE ?",
                new String[]{Long.toString(id)});
    }
}