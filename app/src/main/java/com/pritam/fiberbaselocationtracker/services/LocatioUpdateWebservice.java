package com.pritam.fiberbaselocationtracker.services;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pritam.fiberbaselocationtracker.MyApplication;
import com.pritam.fiberbaselocationtracker.Utilies;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android


class LocatioUpdateWebservice {
    //@RequiresApi(api = Build.VERSION_CODES.O)
    public LocatioUpdateWebservice(Location location) {

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss.SSS").format(new Date());
       // ArrayList<HashMap<String, Object>> body = new ArrayList<>();
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("Longitude", location.getLongitude());
        hm.put("Latitude", location.getLatitude());
        hm.put("Accuracy", location.getAccuracy());
        hm.put("Altitude", location.getAltitude());
        hm.put("Provider", location.getProvider());
        hm.put("Speed", location.getSpeed());
//        hm.put("SpeedAccuracyMetersPerSecond", location.getSpeedAccuracyMetersPerSecond());
        hm.put("TimeStamp", timeStamp);
       // body.add(hm);
        timeStamp = new SimpleDateFormat("yyyy_MM_dd").format(new Date());

        Gson gson = new Gson();
        http_post_request(gson.toJson(hm), timeStamp);

    }

    private void http_post_request(String postBody, String date) {
        try {
            String url = "https://angular-db-fa163.firebaseio.com/locationtrack/"+ MyApplication.deviceID+"/"+date+".json";

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, postBody);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.d("-->>", request.toString());


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d("-->>", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("-->>", response.toString());
                }
            });
        } catch (Exception e) {
            Log.d("-->>", getStackTrace(e));
        }
    }

    public String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

}