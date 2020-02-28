package com.example.findmeuv.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BugReport {

    SharedPreferences fmuUserPref, fmuError;
    SharedPreferences.Editor fmuErrorEdit;
    Context context;
    Activity act;

    public BugReport(Activity act) {
        this.context = act;
        this.act = act;
        fmuUserPref = context.getSharedPreferences("fmuPref",
                Context.MODE_PRIVATE);
        fmuError = context.getSharedPreferences("error", 0);
        fmuErrorEdit = fmuError.edit();
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void sendBug() {
        if (fmuError.contains("stacktrace")) {
            checkErrorLog();
        }
    }

    private boolean isUIThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    public void handleUncaughtException(Thread thread, Throwable e) {
        Map<String, String> data = new HashMap<>();

        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        //  Log.d("DebugLog: ", "handleUncaughtException is called\n STACKTRACE: "+ exceptionAsString);

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;

        fmuErrorEdit.putString("main", "log");
        fmuErrorEdit.putString("sub", "log_error");
        fmuErrorEdit.putString("ref", "1");
        fmuErrorEdit.putString("resp", "1");
        fmuErrorEdit.putString("app_name","passenger");
        fmuErrorEdit.putString("stacktrace", exceptionAsString.replace(" ", "-"));
        fmuErrorEdit.putString("msg", e.getMessage());
        fmuErrorEdit.putString("manufacturer", manufacturer);
        fmuErrorEdit.putString("model", model);
        fmuErrorEdit.putString("version", String.valueOf(version));
        fmuErrorEdit.putString("version_release", versionRelease);
        fmuErrorEdit.commit();


        final String s = e.getMessage();
        if (isUIThread()) {
            // exception occurred from UI thread

        } else {  //handle non UI thread throw uncaught exception

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    }

    ViewHelper dialog;

    public void checkErrorLog() {
        dialog = new ViewHelper(context);
        final Context con = context;
        final Activity actv = act;
        actv.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.showLoading();
            }
        });
        final SharedPreferences fmuErr = this.context.getSharedPreferences("error", 0);
        final SharedPreferences.Editor fmuErrEdit = fmuErr.edit();
        Map<String, ?> data = new HashMap<>();
        data = fmuErr.getAll();
        final Map<String, ?> errPref = fmuErr.getAll();
        String url = "http://"+ context.getResources().getString(R.string.base_url) +"/fmuv/";

        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        for(String key: data.keySet()) {
            formBuilder.add(key, data.get(key).toString());
        }

        RequestBody form = formBuilder.build();
        final Request request = new Request.Builder()
                .url(url)
                .post(form)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("DebugLog: ", "REQ FAILED: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Log.d("DebugLog: ", "RESPONSE: " + response.body().string());
                for(String key: errPref.keySet()) {
                    if (!key.equals("passenger_id")) {
                        fmuErrEdit.remove(key);
                    }
                }
                fmuErrEdit.commit();
                actv.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismissLoading();
                    }
                });
            }
        });
    }
}
