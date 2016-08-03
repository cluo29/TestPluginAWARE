package com.aware.plugin.pluginfortesting;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.net.Uri;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Applications;
import com.aware.providers.Applications_Provider.Applications_Foreground;
import com.aware.utils.Aware_Plugin;
import com.aware.plugin.pluginfortesting.Provider.Unlock_Monitor_Data;

import com.aware.plugin.pluginfortesting.historicalProviders.Battery_Provider.Battery_Data;

//Chu: add my two here


public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_PLUGINFORTESTING = "ACTION_AWARE_PLUGIN_PLUGINFORTESTING";

    public static final String EXTRA_DATA = "data";

    //context
    private static ContextProducer sContext;


    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);


        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, true);


        IntentFilter application_filter = new IntentFilter();
        application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);

        registerReceiver(applicationListener, application_filter);

        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.

        //Any active plugin/sensor shares its overall context using broadcasts
        sContext = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
                ContentValues data = new ContentValues();
                data.put(Unlock_Monitor_Data.TIMESTAMP, System.currentTimeMillis());


                //send to AWARE
                Intent context_unlock = new Intent();
                context_unlock.setAction(ACTION_AWARE_PLUGIN_PLUGINFORTESTING);
                context_unlock.putExtra(EXTRA_DATA,data);
                sendBroadcast(context_unlock);

                getContentResolver().insert(Unlock_Monitor_Data.CONTENT_URI, data);
            }
        };
        CONTEXT_PRODUCER = sContext;

        //Add permissions you need (Support for Android M) e.g.,
        //REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        //table 1, 2, 3
        CONTEXT_URIS = new Uri[]{ Provider.Unlock_Monitor_Data.CONTENT_URI};

        Aware.startPlugin(this, "com.aware.plugin.pluginfortesting");

        /*
        if (Aware.getSetting(this, "study_id").length() == 0) {
            Intent joinStudy = new Intent(this, Aware_Preferences.StudyConfig.class);
            joinStudy.putExtra(Aware_Preferences.StudyConfig.EXTRA_JOIN_STUDY, "https://api.awareframework.com/index.php/webservice/index/634/0FOT21HRz8IZ");
            startService(joinStudy);
        }*/
    }

    private static ApplicationListener applicationListener = new ApplicationListener();

    public static class ApplicationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables

            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND)) {
                Log.d("UNLOCK", "data comes");
                //get data!!!
                Cursor AppCursor = context.getContentResolver().query(Applications_Foreground.CONTENT_URI, null, null, null, Applications_Foreground.TIMESTAMP + " DESC LIMIT 1");

                if (AppCursor != null && AppCursor.moveToFirst()) {

                    String application = AppCursor.getString(AppCursor.getColumnIndex(Applications_Foreground.PACKAGE_NAME));

                    Log.d("UNLOCK","application = "+ application);
                }
                if (AppCursor != null && !AppCursor.isClosed())
                {
                    AppCursor.close();
                }


                Cursor cursor = context.getContentResolver().query(Uri.parse(Battery_Data.CONTENT_URI_STRING),
                        null, null, null, Battery_Data.TIMESTAMP + " DESC LIMIT 1");

                if (cursor != null && cursor.moveToFirst()) {

                    int batteryLevel = cursor.getInt(cursor.getColumnIndex(Battery_Data.LEVEL));

                    Log.d("UNLOCK","batteryLevel = "+ batteryLevel);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }

            }



        }
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, false);

        if(applicationListener != null) {
            unregisterReceiver(applicationListener);
        }

        //Deactivate any sensors/plugins you activated here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.pluginfortesting");
    }
}
