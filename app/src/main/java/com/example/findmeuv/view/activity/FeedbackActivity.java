package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);

        sharedPreferences = getSharedPreferences("fmuPref", 0);

        viewHelper = new ViewHelper(this);

        final EditText txtMsg = findViewById(R.id.txtMsg);
        final Button btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtMsg.getText().toString().trim().equals("")) {
                    progressBar.setVisibility(View.VISIBLE);
                    btnSend.setEnabled(false);

                    Map<String, String> data = new HashMap<>();
                    data.put("resp", "1");
                    data.put("main", "booking");
                    data.put("sub", "save_feedback");
                    data.put("message", txtMsg.getText().toString());
                    data.put("passenger_id", sharedPreferences.getString("passenger_id", ""));

                    viewModel.okHttpRequest(data, "GET", "");

                } else {
                    txtMsg.setError("Message cannot be empty.");
                }
            }
        });

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                txtMsg.setText("");
                Toast.makeText(getApplicationContext(), "Feedback sent!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                btnSend.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Failed to send feedback. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
