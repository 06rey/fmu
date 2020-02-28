package com.example.findmeuv.utility;

import android.app.Activity;
import android.util.Log;

import com.example.findmeuv.R;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GetPostUrlMaker {

    public String url;
    public OkHttpClient client;
    public FormBody.Builder formBuilder;
    public RequestBody form;
    public okhttp3.Request request;

    public GetPostUrlMaker(Activity activity, String method, Map<String, String> data, String param) {

        url = activity.getResources().getString(R.string.BASE_URL);
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        if (method.equals("POST")) {
            formBuilder = new FormBody.Builder();
            formBuilder.add("ref", "1");
            formBuilder.add("tk", activity.getResources().getString(R.string.tk));
            formBuilder.add("event", "normal");

            for(String key: data.keySet()) {
                formBuilder.add(key, data.get(key));
            }
            form = formBuilder.build();
            request = new Request.Builder()
                    .url(url)
                    .post(form)
                    .build();

        } else {
            url = url + "?ref=1&tk=" + activity.getResources().getString(R.string.tk);
            url = url + "&event=normal";
            for(String key: data.keySet()) {
                url = url + "&" + key + "=" + data.get(key);
            }
            url = url + param;
            Log.d("DebugLog", "GetPostUrlMaker->GET: " + url);
            request = new Request.Builder()
                    .url(url)
                    .build();
        }
    }
}
