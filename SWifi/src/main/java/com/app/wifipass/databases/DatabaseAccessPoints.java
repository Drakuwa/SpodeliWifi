package com.app.wifipass.databases;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.app.wifipass.pojos.AccessPoint;

import java.util.ArrayList;

/**
 * Created by Milena on 2/25/14.
 */
public class DatabaseAccessPoints extends SQLiteOpenHelper {

    private static int DATABASE_VERSION = 2;
    private static String DATABASE_NAME = "accesPoints.db";
    private static String DATABASE_TABLE_APS = "accesspoints";
    private static String KEY_ID = "_id";
    private static String KEY_AP_NAME = "apname";
    private static String KEY_BSSID = "bssid";
    private static String KEY_PASS = "pass";

    private static final String D_TABLE_CREATE = "CREATE TABLE " + DATABASE_TABLE_APS
            + "(" + KEY_ID + " INTEGER," + KEY_AP_NAME + " TEXT," + KEY_BSSID
            + " TEXT," + KEY_PASS + " TEXT); ";

    public DatabaseAccessPoints(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(D_TABLE_CREATE);
    }

    public void dropDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE_APS, null, null);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_APS);
        onCreate(db);
    }

    public String getLastAccessPointId() {
        String lastID = "0";
        SQLiteDatabase db = this.getWritableDatabase();
        int lastId = 0;
        String[] columns = {KEY_ID};
        Cursor cursor = db.query(DATABASE_TABLE_APS, columns, null, null, null, null, " _id DESC", "1");
        if (cursor.moveToFirst()) {
            lastId = cursor.getInt(0);
            lastID = String.valueOf(lastId);
        }
        cursor.close();
        db.close();
        return lastID;
    }

    public void addAllAccessPoints(ArrayList<AccessPoint> tmp) {
        SQLiteDatabase db = this.getWritableDatabase();
        DatabaseUtils.InsertHelper insertHelper = new DatabaseUtils.InsertHelper(db, DATABASE_TABLE_APS);
        // Get the numeric indexes for each of the columns that we're updating
        final int id = insertHelper.getColumnIndex(KEY_ID);
        final int apname = insertHelper.getColumnIndex(KEY_AP_NAME);
        final int bssid = insertHelper.getColumnIndex(KEY_BSSID);
        final int pass = insertHelper.getColumnIndex(KEY_PASS);

        try {
            db.beginTransaction();
            for (AccessPoint accessPoint : tmp) {
                insertHelper.prepareForInsert();

                insertHelper.bind(id, accessPoint.getId());
                insertHelper.bind(apname, accessPoint.getName());
                insertHelper.bind(bssid, accessPoint.getBssid());
                insertHelper.bind(pass, accessPoint.getPassword());

                insertHelper.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            insertHelper.close();
            db.close();
        }
    }

    public ArrayList<AccessPoint> getAllAps() {
        ArrayList<AccessPoint> apsList = new ArrayList<AccessPoint>();
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + DATABASE_TABLE_APS + " ORDER BY _id DESC;";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                AccessPoint access = new AccessPoint();
                access.setId(cursor.getInt(0));
                access.setName(cursor.getString(1));
                access.setBssid(cursor.getString(2));
                access.setPassword(cursor.getString(3));
                apsList.add(access);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return apsList;
    }

    public int getLastAccessPointId(String bss) {
        SQLiteDatabase db = this.getWritableDatabase();
        int lastId = 1;
        String[] bssid = new String[]{bss};
        String[] columns = {KEY_ID};
        Cursor cursor = db.query(DATABASE_TABLE_APS, columns, " bssid = ?", bssid, null, null, " _id DESC", "1");
        if (cursor.moveToFirst())
            lastId = cursor.getInt(0);
        cursor.close();
        db.close();
        return lastId;
    }
}
