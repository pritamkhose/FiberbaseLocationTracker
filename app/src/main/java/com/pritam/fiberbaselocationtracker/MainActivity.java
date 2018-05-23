package com.pritam.fiberbaselocationtracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button http_get, http_post;
    TextView text_response;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        text_response = (TextView) findViewById(R.id.text_response);

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

        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false);

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

    private void http_get_request() {
        try {
            progress.show();
            myResponse = "";
            String url = "https://angular-db-fa163.firebaseio.com/freejson.json";

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
                public void onResponse(Call call, Response response) throws IOException {
                    progress.dismiss();
                    myResponse = response.toString() + "\n";

                    try {
                        //JsonObject newObj = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                        JsonArray newArr = new JsonParser().parse(response.body().toString()).getAsJsonArray();
                        myResponse += String.valueOf(newArr);
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
}
