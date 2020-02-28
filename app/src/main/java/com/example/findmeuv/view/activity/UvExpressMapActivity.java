package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.findmeuv.R;
import com.example.findmeuv.model.pojo.RouteItem;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UvExpressMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private Activity activity;

    //MAP OBJECTS AND VARIABLES
    private GoogleMap googleMap;
    private String pickUpLocLat = null, pickUpLocLng = null, transportServiceName;
    private ImageView imgMarker;
    private LatLng imgMarkerLatLng;
    private Marker mark;
    private PolylineOptions routePolyLine = new PolylineOptions();
    private LatLng originLatLng;
    private LatLng destinationLatLng;
    private List<RouteItem> routeItemList = new ArrayList<>();
    private boolean isPolyLineSet = false;
    private MarkerOptions passengerMarkerOption;
    private Marker pick_up_marker;

    //MISC VARIABLES
    private String tripId, bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uv_express_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initialize();
    }

    // MAP FUNCTIONS

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void zoomMap() {
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(11.224951574789777,125.01982289054729))
                .zoom(8)
                .bearing(0)
                .tilt(0)
                .build();
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
    }

    private void initialize() {

        // PROPERTY INITIALIZATION
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);

        viewHelper = new ViewHelper(this);

        tripId = getIntent().getStringExtra("tripId");
        bookId = getIntent().getStringExtra("bookId");

        activity = this;

        this.setViewModelObserver();

        // METHOD CALL
        this.getRoute();
    }
    // MISC FUNCTIONS
    private void hideLoadingMap() {
        LinearLayout linearLoadingMapText = findViewById(R.id.layoutLoadMap);
        linearLoadingMapText.setVisibility(View.GONE);
        LinearLayout mapLayout = findViewById(R.id.mapLayout);
        mapLayout.setVisibility(View.VISIBLE);
    }

    private void errorResult() {
        hideLoadingMap();
        viewHelper.exitActivity(activity, "Loading Failed", "Something went wrong.");
    }

    // NETWORK REQUEST

    private void getRoute() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "route");
        data.put("sub", "get_route_way_point");
        data.put("event", "normal");
        data.put("trip_id", tripId);
        data.put("mode", "book_info");
        data.put("booId", String.valueOf(bookId));
        viewModel.okHttpRequest(data, "GET", "");
    }

    // SERVER RESPONSE

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                hideLoadingMap();

                String type = list.get(0).get("type");
                Log.d("DebugLog", list.toString());
                switch (type) {
                    case "route":
                        drawRoute(list);
                        break;
                }

            }
        });

        viewModel.getRouteItemList().observe(this, new Observer<List<RouteItem>>() {
            @Override
            public void onChanged(List<RouteItem> routePolyLines) {
                if (!isPolyLineSet) {
                    routeItemList = routePolyLines;
                    isPolyLineSet = true;
                }
            }
        });

        // ERROR OBSERVER

        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                errorResult();
            }
        });
        viewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                errorResult();
            }
        });
        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                errorResult();
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                errorResult();
            }
        });


    }

    // MAP FUNCTIONS
    private void drawRoute(List<Map<String, String>> list) {
        transportServiceName = list.get(0).get("company_name");
        originLatLng = routeItemList.get(0).getOriginLatLng();
        destinationLatLng = routeItemList.get(0).getDestLatLng();

        String originName = list.get(0).get("origin");
        String destinationName = list.get(0).get("destination");
        if (list.get(0).get("pick_up_loc").equals("Terminal")) {
            imgMarkerLatLng = originLatLng;
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_pin);
            passengerMarkerOption = new MarkerOptions()
                    .title("Boarding location: "+transportServiceName + " " + originName + " Terminal")
                    .position(imgMarkerLatLng)
                    .icon(icon);
            pick_up_marker = googleMap.addMarker(passengerMarkerOption);

        } else {
            googleMap.addMarker(new MarkerOptions().position(originLatLng).title(transportServiceName + " " + originName + " Terminal"));
        }
        googleMap.addMarker(new MarkerOptions().position(destinationLatLng).title(transportServiceName + " " + destinationName + " Terminal"));

        routePolyLine = routeItemList.get(0).getPolyLineOption();
        routePolyLine.color(getResources().getColor(R.color.lineColor));
        routePolyLine.visible( true );
        routePolyLine.clickable(true);
        googleMap.addPolyline(routePolyLine);
        CameraPosition googlePlex = CameraPosition.builder()
                .target(originLatLng)
                .zoom(15)
                .bearing(0)
                .tilt(0)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);

        zoomMap();
    }

    private void getPickUpPointLocation() {

    }
}
