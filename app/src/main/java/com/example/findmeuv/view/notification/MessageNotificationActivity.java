package com.example.findmeuv.view.notification;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.findmeuv.R;

public class MessageNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uv_distance_notification);
        setTitle("Find Me UV Message");
        this.setFinishOnTouchOutside(false);

        TextView txtMsg = findViewById(R.id.txtMsg);
        String msg = getIntent().getStringExtra("msg");
        txtMsg.setText(msg);
    }

    public void onClickBtnOk(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
