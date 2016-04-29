package com.aware.plugin.pluginfortesting.TestAWARE;

/**
 * Created by Comet on 29/04/16.
 */
public class Battery_Provider {
    public static String AUTHORITY = "io.github.cluo29.contextdatareading.provider.battery";

    public static final class Battery_Data {

        public static final String CONTENT_URI_STRING = "content://"
                + Battery_Provider.AUTHORITY + "/battery";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.battery";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.battery";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String STATUS = "battery_status";
        public static final String LEVEL = "battery_level";
        public static final String SCALE = "battery_scale";
        public static final String VOLTAGE = "battery_voltage";
        public static final String TEMPERATURE = "battery_temperature";
        public static final String PLUG_ADAPTOR = "battery_adaptor";
        public static final String HEALTH = "battery_health";
        public static final String TECHNOLOGY = "battery_technology";
    }
}
