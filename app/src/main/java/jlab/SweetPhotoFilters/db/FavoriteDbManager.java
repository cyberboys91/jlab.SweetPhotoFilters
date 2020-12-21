package jlab.SweetPhotoFilters.db;

import java.util.ArrayList;

import android.content.ContentValues;
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
    private static final String NUM_COLUMNS_TABLE_NAME = "columns";
    private static final String SQL_DELETE_ENTRIES2 = "DROP TABLE IF EXISTS " + NUM_COLUMNS_TABLE_NAME;
    private static final String COUNT_COLUMN_NAME = "count";
    private static final int MAX_NUM_COLUMNS = 4, MIN_NUM_COLUMNS = 2;

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
            db.execSQL("CREATE TABLE " + NUM_COLUMNS_TABLE_NAME + " ("
                    + FavoriteContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COUNT_COLUMN_NAME + " INT NOT NULL)");
            saveNumColumns(2, db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long saveNumColumns(int numColumns, SQLiteDatabase sqLiteDatabase) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COUNT_COLUMN_NAME, numColumns);
        return sqLiteDatabase.insert(NUM_COLUMNS_TABLE_NAME, null, contentValues);
    }

    public int getIDNumColumns() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(NUM_COLUMNS_TABLE_NAME, null, null, null, null, null, null);
        int result = 1;
        if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex(FavoriteContract._ID));
        cursor.close();
        return result;
    }

    public long saveFavoriteData(FavoriteDetails favorite) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.insert(FavoriteContract.TABLE_NAME, null, favorite.toContentValues());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES2);
        onCreate(sqLiteDatabase);
    }

    public int getNumColumns() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(NUM_COLUMNS_TABLE_NAME, null, null, null, null, null, null);
        int result = 2;
        if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex(COUNT_COLUMN_NAME));
        cursor.close();
        return result;
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

    private int updateNumColumns(int numColumns) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COUNT_COLUMN_NAME, numColumns);
        return sqLiteDatabase.update(NUM_COLUMNS_TABLE_NAME,
                contentValues,
                FavoriteContract._ID + " LIKE ?",
                new String[]{Long.toString(getIDNumColumns())});
    }

    public int incrementNumColumns () {
        int newColumns = Math.min(getNumColumns() + 1, MAX_NUM_COLUMNS);
        updateNumColumns(newColumns);
        return newColumns;
    }

    public int decremtNumColuns () {
        int newColumns = Math.max(getNumColumns() - 1, MIN_NUM_COLUMNS);
        updateNumColumns(newColumns);
        return newColumns;
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