package com.aware.plugin.pluginfortesting;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.net.Uri;
import android.view.LayoutInflater;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Applications;
import com.aware.Locations;
import com.aware.WiFi;
import com.aware.providers.WiFi_Provider.WiFi_Data;
import com.aware.providers.Applications_Provider.Applications_Foreground;
import com.aware.utils.Aware_Plugin;
import com.aware.plugin.pluginfortesting.Provider.Unlock_Monitor_Data;

import com.aware.providers.WiFi_Provider;
import com.aware.plugin.pluginfortesting.historicalProviders.Battery_Provider.Battery_Data;
import com.aware.providers.Locations_Provider.Locations_Data;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RatingBar;
import android.widget.TextView;
import android.net.Uri;

//Chu: add my two here

//real-time: GPS, WIFI
//historical: acceleration
//simulated: plugin crash

/*
                            ContentValues crashData = new ContentValues();
                            crashData.put(Applications_Crashes.TIMESTAMP, System.currentTimeMillis());
                            crashData.put(Applications_Crashes.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                            crashData.put(Applications_Crashes.PACKAGE_NAME, error.processName);
                            crashData.put(Applications_Crashes.APPLICATION_NAME, appName);
                            crashData.put(Applications_Crashes.APPLICATION_VERSION, (pkgInfo != null) ? pkgInfo.versionCode : -1); //some prepackages don't have version codes...
                            crashData.put(Applications_Crashes.ERROR_SHORT, error.shortMsg);

                            String error_long = "";
                            error_long += error.longMsg + "\nStack:\n";
                            error_long += (error.stackTrace != null) ? error.stackTrace : "";

                            crashData.put(Applications_Crashes.ERROR_LONG, error_long);
                            crashData.put(Applications_Crashes.ERROR_CONDITION, error.condition);
                            crashData.put(Applications_Crashes.IS_SYSTEM_APP, pkgInfo != null && isSystemPackage(pkgInfo));

                            getContentResolver().insert(Applications_Crashes.CONTENT_URI, crashData);

                            if (Aware.DEBUG) Log.d(Aware.TAG, "Crashed: " + crashData.toString());

                            Intent crashed = new Intent(ACTION_AWARE_APPLICATIONS_CRASHES);
                            crashed.putExtra(EXTRA_DATA, crashData);
                            sendBroadcast(crashed);
                        } catch (NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        //string   ACTION_AWARE_APPLICATIONS_CRASHES

 */


public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_PLUGINFORTESTING = "ACTION_AWARE_PLUGIN_PLUGINFORTESTING";

    public static final String EXTRA_DATA = "data";

    //historical acceleration
    //public static final String ACTION_AWARE_ACCELERATION = "ACTION_AWARE_ACCELERATION";

    //context
    private static ContextProducer sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View layout = inflater.inflate(R.layout.question, null);
        builder.setView(layout);

        alert = builder.create();

        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        //Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, true);

        //IntentFilter application_filter = new IntentFilter();
        //application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);

        //registerReceiver(applicationListener, application_filter);

        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.

        //Real acc
        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER,20000);


        //WIFI
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_WIFI, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_WIFI, 60);

        //Locations
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_NETWORK, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LOCATION_GPS, 180);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 300);

        //WIFI data
        IntentFilter wifi_filter = new IntentFilter();
        wifi_filter.addAction(WiFi.ACTION_AWARE_WIFI_NEW_DEVICE);

        //Location data
        IntentFilter location_filter = new IntentFilter();
        location_filter.addAction(Locations.ACTION_AWARE_LOCATIONS);

        //for historical acceleration data
        IntentFilter acceleration_filter = new IntentFilter();
        acceleration_filter.addAction("ACTION_AWARE_ACCELEROMETER");

        Log.d("UNLOCK", "143");

        registerReceiver(wifiListener, wifi_filter);
        registerReceiver(locationListener, location_filter);
        registerReceiver(accelerationListener, acceleration_filter);

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

        //startService(new Intent(MainActivity.this, ThreeFloatProcedure.class));

        /*
        if (Aware.getSetting(this, "study_id").length() == 0) {
            Intent joinStudy = new Intent(this, Aware_Preferences.StudyConfig.class);
            joinStudy.putExtra(Aware_Preferences.StudyConfig.EXTRA_JOIN_STUDY, "https://api.awareframework.com/index.php/webservice/index/634/0FOT21HRz8IZ");
            startService(joinStudy);
        }*/
    }


    //wifiListener
    /**
     * BroadcastReceiver that will receive wifi events from AWARE
     */

    private static WifiListener wifiListener = new WifiListener();

    public static class WifiListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            //variableReset();
            if (intent.getAction().equals(WiFi.ACTION_AWARE_WIFI_NEW_DEVICE)){
                Log.d("UNLOCK","ACTION_AWARE_WIFI_NEW_DEVICE ");

                if (intent.getAction().equals(WiFi.ACTION_AWARE_WIFI_NEW_DEVICE)){
                    Cursor cursor = context.getContentResolver().query(WiFi_Provider.WiFi_Data.CONTENT_URI, null, null, null, WiFi_Provider.WiFi_Data.TIMESTAMP + " DESC LIMIT 1");
                    if (cursor != null && cursor.moveToFirst()) {
                        String wifi = cursor.getString(cursor.getColumnIndex(WiFi_Provider.WiFi_Data.SSID));
                        Log.d("UNLOCK","wifi="+ wifi);
                    }
                    if (cursor != null && !cursor.isClosed())
                    {
                        cursor.close();
                    }
                }

                //Write the test result DB of that

                /*
                for(ScanResult ap : aps) {
                    ContentValues rowData = new ContentValues();
                    rowData.put(WiFi_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(),Aware_Preferences.DEVICE_ID));
                    rowData.put(WiFi_Data.TIMESTAMP, currentScan);
                    rowData.put(WiFi_Data.BSSID, ap.BSSID);
                    rowData.put(WiFi_Data.SSID, ap.SSID);
                    rowData.put(WiFi_Data.SECURITY, ap.capabilities);
                    rowData.put(WiFi_Data.FREQUENCY, ap.frequency);
                    rowData.put(WiFi_Data.RSSI, ap.level);

                    try {
                        getContentResolver().insert(WiFi_Data.CONTENT_URI, rowData);
                    } catch (SQLiteException e) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    } catch (SQLException e) {
                        if(Aware.DEBUG) Log.d(TAG,e.getMessage());
                    }

                    if( Aware.DEBUG ) Log.d(TAG, ACTION_AWARE_WIFI_NEW_DEVICE + ": " + rowData.toString());
                    Intent detectedAP = new Intent(ACTION_AWARE_WIFI_NEW_DEVICE);
                    detectedAP.putExtra(EXTRA_DATA, rowData);
                    sendBroadcast(detectedAP);
                }

                 */
            }


            //sync data!
            //BroadContext(context);
        }
    }

    //locationListener
    /**
     * BroadcastReceiver that will receive location events from AWARE
     */
    private static LocationListener locationListener = new LocationListener();

    public static class LocationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            //variableReset();
            if (intent.getAction().equals(Locations.ACTION_AWARE_LOCATIONS)){
                Log.d("UNLOCK","Locations");

                Cursor cursor = context.getContentResolver().query(Locations_Data.CONTENT_URI, null, null, null, Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    double latitude = cursor.getDouble(cursor.getColumnIndex(Locations_Data.LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(Locations_Data.LONGITUDE));
                    String locationSource = cursor.getString(cursor.getColumnIndex(Locations_Data.PROVIDER));
                    Log.d("UNLOCK","latitude="+ latitude);
                    Log.d("UNLOCK","longitude="+ longitude);
                    Log.d("UNLOCK","locationSource="+ locationSource);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }
                /*

        ContentValues rowData = new ContentValues();
        rowData.put(Locations_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Locations_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        rowData.put(Locations_Data.LATITUDE, bestLocation.getLatitude());
        rowData.put(Locations_Data.LONGITUDE, bestLocation.getLongitude());
        rowData.put(Locations_Data.BEARING, bestLocation.getBearing());
        rowData.put(Locations_Data.SPEED, bestLocation.getSpeed());
        rowData.put(Locations_Data.ALTITUDE, bestLocation.getAltitude());
        rowData.put(Locations_Data.PROVIDER, bestLocation.getProvider());
        rowData.put(Locations_Data.ACCURACY, bestLocation.getAccuracy());

        try {
            getContentResolver().insert(Locations_Data.CONTENT_URI, rowData);
        } catch (SQLiteException e) {
            if (Aware.DEBUG) Log.d(TAG, e.getMessage());
        } catch (SQLException e) {
            if (Aware.DEBUG) Log.d(TAG, e.getMessage());
        }

        Intent locationEvent = new Intent(ACTION_AWARE_LOCATIONS);
        sendBroadcast(locationEvent);

                 */

            }

            //sync data!
            //BroadContext(context);
        }
    }

    //historical acceleration
    private static AccelerationListener accelerationListener = new AccelerationListener();

    public static class AccelerationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            //variableReset();
            if (intent.getAction().equals("ACTION_AWARE_ACCELEROMETER")) {
                Log.d("UNLOCK","ACTION_AWARE_ACCELERATION received");
                ContentValues acc_data = intent.getParcelableExtra(EXTRA_DATA);
                if (acc_data != null) {
                    Log.d("UNLOCK", "ACC DATA AVAILABLE");
                    double acc_0 = acc_data.getAsDouble("double_values_0");
                    double acc_1 = acc_data.getAsDouble("double_values_1");
                    double acc_2 = acc_data.getAsDouble("double_values_2");
                    Log.d("UNLOCK","acc_0 = "+ acc_0);
                    Log.d("UNLOCK","acc_1 = "+ acc_1);
                    Log.d("UNLOCK","acc_2 = "+ acc_2);
                    //judge fall
                    //calculate G
                    if(1==0)
                    {
                        //push alert
                        alert.show();
                    }
                } else {
                    Log.d("UNLOCK", "ACC DATA UNAVAILABLE");
                }

            }
        }

    }

    public static AlertDialog alert;

    /*
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
*/
    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_WIFI, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_NETWORK, false);

        if(wifiListener != null) {
            unregisterReceiver(wifiListener);
        }

        if(locationListener != null) {
            unregisterReceiver(locationListener);
        }

        if(accelerationListener != null) {
            unregisterReceiver(accelerationListener);
        }

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.pluginfortesting");
    }
}
