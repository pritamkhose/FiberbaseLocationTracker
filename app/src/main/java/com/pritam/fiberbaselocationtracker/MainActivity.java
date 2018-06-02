package com.pritam.fiberbaselocationtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pritam.fiberbaselocationtracker.services.LocationMonitoringService;

import com.google.gson.JsonArray;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// http://devdeeds.com/android-location-tracking-in-background-service/


public class MainActivity extends AppCompatActivity {

    Button http_get, http_post, http_delete;
    TextView text_response;
    ProgressDialog progress;
    Switch gpsswitch;
    //    private RequestPermissionHandler mRequestPermissionHandler;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private boolean mAlreadyStartedService = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        text_response = (TextView) findViewById(R.id.text_response);
        gpsswitch = (Switch) findViewById(R.id.gpsswitch);
        gpsswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);
                startStopService(isChecked);
            }

        });

        http_get = (Button) findViewById(R.id.http_get);
        http_get.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvaiable()) {
                    http_get_request();
                } else {
                    alertDialog("Offline", "No Internet Connection");
                }

            }
        });

        http_post = (Button) findViewById(R.id.http_post);
        http_post.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvaiable()) {
                    http_post_request();
                } else {
                    alertDialog("Offline", "No Internet Connection");
                }
            }
        });

        http_delete = (Button) findViewById(R.id.http_delete);
        http_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvaiable()) {
                    http_delete_request();
                } else {
                    alertDialog("Offline", "No Internet Connection");
                }
            }
        });

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);


        //mRequestPermissionHandler = new RequestPermissionHandler();
        //handlePermission();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String latitude = intent.getStringExtra(LocationMonitoringService.EXTRA_LATITUDE);
                        String longitude = intent.getStringExtra(LocationMonitoringService.EXTRA_LONGITUDE);

                        if (latitude != null && longitude != null) {
                            text_response.setText(getString(R.string.msg_location_service_started) + "\n Latitude : " + latitude + "\n Longitude: " + longitude);
                        }
                    }
                }, new IntentFilter(LocationMonitoringService.ACTION_LOCATION_BROADCAST)
        );
    }


    private boolean isNetworkAvaiable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }


    String myResponse;
    ArrayList<HashMap<String, Object>> aList = new ArrayList<>();

    private void http_get_request() {
        try {
            progress.show();
            myResponse = "";
            // String url = "https://angular-db-fa163.firebaseio.com/freejson.json";
            String url = "https://angular-db-fa163.firebaseio.com/locationtrack/" + MyApplication.deviceID + ".json";

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d("-->>", request.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    progress.dismiss();
                    Log.d("-->>", getStackTrace(e));
                    alertDialog("Request Failure", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    progress.dismiss();

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                myResponse = response.body().string();//response.body().toString(); response.toString() + "\n" +
                                aList = new ArrayList<>();

                                JsonObject json1 = (JsonObject) (new JsonParser()).parse(myResponse);
                                myResponse = response.toString() + "\n" + String.valueOf(json1);

                                //Log.d("-->>", myResponse);
                                Set<Map.Entry<String, JsonElement>> entrySet = json1.entrySet();
                                for(Map.Entry<String,JsonElement> entry : entrySet){
                                    //Log.d("-->>", entry.getKey()+ json1.get(entry.getKey()));
                                    JsonObject json2 = (JsonObject)json1.get(entry.getKey());
                                    Set<Map.Entry<String, JsonElement>> entrySet2 = json2.entrySet();
                                    for(Map.Entry<String,JsonElement> entry2 : entrySet2){
                                        //Log.d("-->>", entry2.getKey()+ json2.get(entry2.getKey()));
                                        JsonObject json3 = (JsonObject)json2.get(entry2.getKey());
                                        HashMap<String, Object> hm = new HashMap<>();
                                        hm.put("Accuracy", json3.get("Accuracy"));
                                        hm.put("Altitude", json3.get("Altitude"));
                                        hm.put("Latitude", json3.get("Latitude"));
                                        hm.put("Longitude", json3.get("Longitude"));
                                        //hm.put("Provider", json3.get("Provider"));
                                        hm.put("Speed", json3.get("Speed"));
                                        hm.put("TimeStamp", json3.get("TimeStamp"));
                                        hm.put("Date", entry.getKey());
                                        aList.add(hm);
                                    }
                                }
                                Log.d("-->>", aList.toString());
                            } catch (Exception e) {
                                Log.d("-->>", getStackTrace(e));
                                myResponse += response.toString();
                            }

                            text_response.setText(myResponse);
                        }
                    });

                }
            });
        } catch (Exception e) {
            progress.dismiss();
            Log.d("-->>", getStackTrace(e));
            alertDialog("Exception", getStackTrace(e));
        }
    }

    private void http_post_request() {
        try {
            progress.show();
            String url = "https://angular-db-fa163.firebaseio.com/trypost/post2.json";

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            String postBody = getBody();
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
                    progress.dismiss();
                    Log.d("-->>", getStackTrace(e));
                    alertDialog("Request Failure", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    progress.dismiss();
                    myResponse = response.toString() + "\n";

                    try {
                        JsonObject newObj = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                        myResponse += String.valueOf(newObj);
                    } catch (Exception e) {
                        Log.d("-->>", getStackTrace(e));
                        myResponse += response.body().toString();
                    }

                    Log.d("-->>", myResponse);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_response.setText(myResponse);
                        }
                    });

                }
            });
        } catch (Exception e) {
            progress.dismiss();
            Log.d("-->>", getStackTrace(e));
            alertDialog("Exception", getStackTrace(e));
        }
    }


    private void http_delete_request() {
        try {
            progress.show();
            String url = "https://angular-db-fa163.firebaseio.com/locationtrack/" + MyApplication.deviceID + ".json";

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .build();
            Log.d("-->>", request.toString());


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    progress.dismiss();
                    Log.d("-->>", getStackTrace(e));
                    alertDialog("Request Failure", getStackTrace(e));
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    progress.dismiss();
                    myResponse = response.toString() + "\n" + "Delete Location History";

                    Log.d("-->>", myResponse);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_response.setText(myResponse);
                            if (response.code() == 200) {
                                alertDialog("Delete", "Location History");
                            }
                        }
                    });

                }
            });
        } catch (Exception e) {
            progress.dismiss();
            Log.d("-->>", getStackTrace(e));
            alertDialog("Exception", getStackTrace(e));
        }
    }

    public String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public void alertDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.ic_launcher_foreground);
        alertDialog.setCancelable(false);

        // Setting Cancel Button
        alertDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                alertDialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public String getBody() {
        Gson gson = new Gson();
        ArrayList<HashMap<String, Object>> body = new ArrayList<>();
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("name", "Pritam");
        hm.put("phone", 987654321);
        body.add(hm);
        return gson.toJson(body);
    }


    /**
     * Step 1: Check Google Play services
     */
    private void checkGooglePlayServices() {

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {

            //Passing null to indicate that it is executing for the first time.
            checkInternetConnection(null);

        } else {
            Toast.makeText(getApplicationContext(), R.string.no_google_playservice_available, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Step 2: Check & Prompt Internet connection
     */
    private Boolean checkInternetConnection(DialogInterface dialog) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }


        if (dialog != null) {
            dialog.dismiss();
        }

        //Yes there is active internet connection. Next check Location is granted by user or not.

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            checkAppPermissions();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = getString(R.string.btn_label_refresh);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        //Block the Application Execution until user grants the permissions
                        if (checkInternetConnection(dialog)) {

                            //Now make sure about location permission.
                            if (checkPermissions()) {

                                //Step 2: Start the Location Monitor Service
                                //Everything is there to start the service.
                                checkAppPermissions();
                            } else if (!checkPermissions()) {
                                requestPermissions();
                            }

                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Step 3: Start the Location Monitor Service
     */
    private void checkAppPermissions() {

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        if (!mAlreadyStartedService && text_response != null) {

            text_response.setText(R.string.msg_location_service_started);

            //Start location sharing service to app server.........
//            Intent intent = new Intent(this, LocationMonitoringService.class);
//            startService(intent);
//
//            mAlreadyStartedService = true;
            //Ends................................................
        }
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                checkAppPermissions();

            } else {
                // Permission denied.

                // Notify the img_user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the img_user for permission (device policy or "Never ask
                // again" prompts). Therefore, a img_user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }


    @Override
    public void onDestroy() {


        //Stop location sharing service to app server.........

//        stopService(new Intent(this, LocationMonitoringService.class));
//        mAlreadyStartedService = false;
        //Ends................................................


        super.onDestroy();
    }


    private void startStopService(boolean isChecked) {
        if (isChecked) {

            //And it will be keep running until you close the entire application from task manager.
            //This method will executed only once.

            if (!mAlreadyStartedService && text_response != null) {

                text_response.setText(R.string.msg_location_service_started);

                //Start location sharing service to app server.........
                Intent intent = new Intent(this, LocationMonitoringService.class);
                startService(intent);

                mAlreadyStartedService = true;
                //Ends................................................
            }

        } else {
            //Stop location sharing service to app server.........

            stopService(new Intent(this, LocationMonitoringService.class));

            mAlreadyStartedService = false;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        // put your code here....

        checkGooglePlayServices();


        settingsrequest();
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            showSettingsAlert("Enable Location Access");
//            createLocationRequest();
//        }
    }

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    GoogleApiClient googleApiClient;

    public void settingsrequest() {


        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            builder.setAlwaysShow(true); //this is the key ingredient

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        settingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    public void showSettingsAlert(String s) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);


        // Setting Dialog Title
        alertDialog.setTitle(s);//+ String.valueOf(b)

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}
