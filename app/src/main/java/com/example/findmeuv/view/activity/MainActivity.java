package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.findmeuv.R;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static int WELCOME_TIMEOUT = 1000;

    SharedPreferences fmuUserPref, fmuError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this , LoginActivity.class);
                fmuUserPref = getSharedPreferences("fmuPref",
                        Context.MODE_PRIVATE);
                fmuError = getSharedPreferences("error",
                        Context.MODE_PRIVATE);

                Map<String, ?> map;
                map = fmuError.getAll();

                for (String key: map.keySet()) {
                    Log.d("DebugLog", "MAP->" + key + ": " + map.get(key).toString());
                }

                if (fmuUserPref.contains("passenger_id")) {
                    intent = new Intent(MainActivity.this, FmuHomeActivity.class);
                }

                startActivity(intent);

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, WELCOME_TIMEOUT);

    }
}
