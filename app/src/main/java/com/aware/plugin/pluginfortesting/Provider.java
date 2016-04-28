package com.aware.plugin.pluginfortesting;


import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;


/**
 * Created by Comet on 21/01/16.
 */
public class Provider extends ContentProvider {
    public static final int DATABASE_VERSION = 10;
    /**
     * Provider authority: com.aware.plugin.pluginfortesting.provider.pluginfortesting
     */
    public static String AUTHORITY = "com.aware.plugin.pluginfortesting.provider.pluginfortesting";
    //store activity data
    private static final int UNLOCK_MONITOR = 1;
    private static final int UNLOCK_MONITOR_ID = 2;

    public static final String DATABASE_NAME = "pluginfortesting.db";

    public static final String[] DATABASE_TABLES = {
            "plugin_pluginfortesting"
    };

    public static final class Unlock_Monitor_Data implements BaseColumns {
        private Unlock_Monitor_Data(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_pluginfortesting");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.pluginfortesting";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.pluginfortesting";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String ACTIVITY  = "activity";    //
        public static final String CONFIDENCE  = "confidence";    //
    }

    public static final String[] TABLES_FIELDS = {
        Unlock_Monitor_Data._ID + " integer primary key autoincrement," +
        Unlock_Monitor_Data.TIMESTAMP + " real default 0," +
        Unlock_Monitor_Data.DEVICE_ID + " text default ''," +
        Unlock_Monitor_Data.ACTIVITY + " text default ''," +
        Unlock_Monitor_Data.CONFIDENCE + " integer default 0," +
        "UNIQUE("+ Unlock_Monitor_Data.TIMESTAMP+","+ Unlock_Monitor_Data.DEVICE_ID+")"

    };

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;

    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;


    @Override
    public boolean onCreate() {
        //AUTHORITY = getContext().getPackageName() + ".provider.template";

        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], UNLOCK_MONITOR);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", UNLOCK_MONITOR_ID);

        databaseMap = new HashMap<>();
        databaseMap.put(Unlock_Monitor_Data._ID, Unlock_Monitor_Data._ID);
        databaseMap.put(Unlock_Monitor_Data.TIMESTAMP, Unlock_Monitor_Data.TIMESTAMP);
        databaseMap.put(Unlock_Monitor_Data.DEVICE_ID, Unlock_Monitor_Data.DEVICE_ID);
        databaseMap.put(Unlock_Monitor_Data.ACTIVITY, Unlock_Monitor_Data.ACTIVITY);
        databaseMap.put(Unlock_Monitor_Data.CONFIDENCE, Unlock_Monitor_Data.CONFIDENCE);

        return true;
    }


    private boolean initializeDB() {

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                return Unlock_Monitor_Data.CONTENT_TYPE;
            case UNLOCK_MONITOR_ID:
                return Unlock_Monitor_Data.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                long weather_id = database.insert(DATABASE_TABLES[0], Unlock_Monitor_Data.DEVICE_ID, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Unlock_Monitor_Data.CONTENT_URI,
                            weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}