package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.fragment.PendingTripFragment;
import com.example.findmeuv.view.ViewHelper;

public class FindUvActivity extends AppCompatActivity {

    FragmentTransaction fragTrans;
    ViewHelper dialog;

    private String route_name, mode;
    LinearLayout btnTripFilter;

    public FragmentLoader loader;
    LinearLayout routeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_uv);

        route_name = getIntent().getStringExtra("r_name");
        mode = getIntent().getStringExtra("mode");

        routeTitle = findViewById(R.id.titleRoute);
        TextView pendingTitle = findViewById(R.id.pendingTripRoute);
        TextView txtRouteTitle = findViewById(R.id.txtRouteTitle);
        pendingTitle.setText(route_name + " Pending Trip");
        txtRouteTitle.setText("Search");

        loader = new FragmentLoader("");
        dialog = new ViewHelper(this);
        btnTripFilter = findViewById(R.id.btntripFilter);

        btnTripFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.tripFilterDialog(route_name, loader);
            }
        });

        if (mode.equals("Pending")) {
            loader.load_fragment();
        }
    }

    public String getRoute_name() {
        return route_name;
    }

    public void arrowOnclick(View view) {
        super.onBackPressed();
    }

    public void backClick(View view) {
        super.onBackPressed();
    }

    public class FragmentLoader {

        public String url_suffix;

        private FragmentLoader(String url_suffix) {
            this.url_suffix = url_suffix;
        }

        public void load_fragment() {
            //dialog.alertDialog(, url_suffix);
            if (mode.equals("Pending")) {
                btnTripFilter.setVisibility(View.VISIBLE);
                fragTrans = getSupportFragmentManager().beginTransaction();
                fragTrans.replace(R.id.findUvFrame, new PendingTripFragment());
                fragTrans.commit();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
