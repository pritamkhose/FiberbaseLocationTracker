package com.pritam.fiberbaselocationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

public class Utilies {



    @SuppressLint("MissingPermission")
    public static String getDeviceIMEI(Context context) {
        String deviceUniqueIdentifier = null;
        //        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
//        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            deviceUniqueIdentifier = tm.getDeviceId();
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

//        String deviceInfo = "";
//        String serviceName = Context.TELEPHONY_SERVICE;
//        String IMEI = "", IMSI = "", mPhoneNumber = "";
//        IMEI = tm.getDeviceId();
//        IMSI = tm.getSubscriberId();
//        mPhoneNumber = tm.getLine1Number();
//
//
//        String s = "";
//        s += "^OS Version:" + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")";
//        s += "^OS API Level:" + Build.VERSION.SDK;
//        s += "^Device:" + Build.DEVICE;
//        s += "^Model (and Product):" + Build.MODEL + " (" + Build.PRODUCT + ")";
//
//        deviceInfo = "PhNo:" + mPhoneNumber + "^IMEI:" + IMEI + "^IMSI:" + IMSI + s + "^" + System.getProperty("os.version") + "^" + System.getProperty("android.os.Build.VERSION.SDK");
//        System.out.println("deviceInfo==="+deviceInfo);

        return deviceUniqueIdentifier;
    }

}
