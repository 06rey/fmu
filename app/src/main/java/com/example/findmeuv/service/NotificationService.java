package com.example.findmeuv.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.example.findmeuv.R;
import com.example.findmeuv.view.notification.MessageNotificationActivity;
import com.google.android.gms.maps.model.LatLng;
import com.star_zero.sse.EventHandler;
import com.star_zero.sse.EventSource;
import com.star_zero.sse.MessageEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificationService extends Service implements EventHandler {

    private SharedPreferences sharedPreferences;

    private LatLng userPickUpLatLng;
    private boolean isUserNotifiedUvExpressIsClose = false;

    private EventSource eventSource;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DebugLog", "NOTIFICATION SERVICE STARTED");
        sharedPreferences = this.getSharedPreferences("fmuPref", 0);
        getUvDistance();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("DebugLog", "NOTIFICATION SERVICE DESTROYED");
        super.onDestroy();
    }

    //-------------------------------------------------------------------------------------------------
    //------------------------------------------  NETWORK REQUEST -------------------------------------
    //-------------------------------------------------------------------------------------------------
    public void getUvDistance() {
        Map<String, String> data = new HashMap<>();
        data.put("main", "notification");
        data.put("sub", "get_uv_distance");
        data.put("passenger_id", sharedPreferences.getString("passenger_id", ""));
        initEventSource(getUrl(data));
    }


    //-------------------------------------------------------------------------------------------------
    //------------------------------------------  EVENT SOURCE ----------------------------------------
    //-------------------------------------------------------------------------------------------------

    private void initEventSource(String url) {
        eventSource = new EventSource(url, this);
        eventSource.connect();
    }

    @Override
    public void onMessage(@NonNull MessageEvent event) {
        String response = event.getData();
        Log.d("DebugLog", "NOTIFICATION SERVICE RESPONSE: " + response);
        List<Map<String, String>> mapDataList = parseJson(response);
        if (mapDataList.size() > 0) {
            float distance  = Float.parseFloat(mapDataList.get(0).get("uv_distance"));
            if (distance < 1000) {
                if (!isUserNotifiedUvExpressIsClose) {
                    String msg = "The UV express is close to a kilometer to your location. Get ready and make sure you are at the right pick up location.";
                    showMessage(msg);
                    isUserNotifiedUvExpressIsClose = true;
                }
            }
        }
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onError(@Nullable Exception e) {

    }

    //------------------------------------------------------------------------------------------------
    //-------------------------------------  DISPLAY NOTIFICATION ---------------------------------
    //------------------------------------------------------------------------------------------------

    private void showMessage(String msg) {
        Intent dialogIntent = new Intent(this, MessageNotificationActivity.class);
        dialogIntent.putExtra("msg", msg);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }



    //------------------------------------------------------------------------------------------------
    //-------------------------------------  URL MAKER & JSON PARSER ---------------------------------
    //------------------------------------------------------------------------------------------------

    public String getUrl(Map<String, String> data) {
        String url = this.getResources().getString(R.string.BASE_URL);
        url += "?tk=" + this.getResources().getString(R.string.tk); // Set app id
        url += "&event=server_event&ref=1&resp=1"; // Set other default GET param needed
        for (String key: data.keySet()) {
            url += "&"+key+"="+data.get(key);
        }
        Log.d("DebugLog", "URL: " + url);
        return url;
    }

    public List<Map<String, String>> parseJson(String response) {
        List<Map<String, String>> list = new ArrayList<>();
        if ((response.contains("DATA") && response.contains("200"))) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject objDATA = jsonObject.getJSONObject("DATA");
                JSONArray jArrBody = objDATA.getJSONArray("body");
                JSONObject responseData = jArrBody.getJSONObject(0);

                int arrLen = jArrBody.length();
                for(int a=0; a<arrLen; a++) {
                    int len = responseData.names().length();
                    Map<String, String> value = new HashMap<>();
                    for (int x=0; x<len; x++) {
                        String key = responseData.names().getString(x);
                        value.put(key, responseData.getString(key));
                    }
                    list.add(value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("DebugLog", "Notification service JSONException Message: " + e.getMessage());
            }
        }
        return list;
    }
}
