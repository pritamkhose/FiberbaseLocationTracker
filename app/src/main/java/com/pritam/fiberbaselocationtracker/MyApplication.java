package com.pritam.fiberbaselocationtracker;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyApplication extends Application {

    public static String deviceID;

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();

        try {
            // try for imei perssion
            deviceID = Utilies.getDeviceIMEI(context);
        } catch (Exception e){
            e.printStackTrace();
            deviceID = "temp_" +(new SimpleDateFormat("yyyy-MM-dd=HH-mm-ss-SSS")).format(new Date());
        }
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}
