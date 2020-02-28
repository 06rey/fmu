package com.example.findmeuv.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class User {

    SharedPreferences fmuUserPref, fmuError;
    SharedPreferences.Editor editor, fmuErrorEdit;
    Context context;

    public User(Context context) {
        this.fmuUserPref = context.getSharedPreferences("fmuPref", 0); // 0 - for private mode = accessible la within the app, dire ha bug os na device
        this.editor = fmuUserPref.edit();
        fmuError = context.getSharedPreferences("error", 0);
        fmuErrorEdit = fmuError.edit();
        this.context = context;
    }

    public void login(Map<String, String> map) {

        for (String key: map.keySet()) {
            this.editor.putString(key, map.get(key));
        }
        this.editor.commit();

        fmuErrorEdit.putString("user_id", fmuUserPref.getString("passenger_id", ""));
        fmuErrorEdit.commit();
    }

    public String fullname() {
        return fmuUserPref.getString("f_name", "") + " " + fmuUserPref.getString("l_name", "");
    }

}
