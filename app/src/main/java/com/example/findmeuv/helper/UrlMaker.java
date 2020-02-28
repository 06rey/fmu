package com.example.findmeuv.helper;

import android.app.Activity;

import com.example.findmeuv.R;

import java.util.Map;

public class UrlMaker {

    public String url;

    public String createGetUrl(Activity activity, Map<String, String> attr, String param) {
        url = activity.getResources().getString(R.string.BASE_URL);
        url = url + "?ref=1&resp=1&tk=" + activity.getResources().getString(R.string.tk);
        for(String key: attr.keySet()) {
            url = url + "&" + key + "=" + attr.get(key);
        }
        url = url + param;
        return url;
    }
}
