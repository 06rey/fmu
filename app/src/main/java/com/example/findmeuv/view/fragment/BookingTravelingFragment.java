package com.example.findmeuv.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.model.pojo.RouteItem;
import com.example.findmeuv.utility.UserLocation;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class BookingTravelingFragment extends Fragment implements OnMapReadyCallback {

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

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
    private Marker pick_up_marker;

    //MISC VARIABLES
    private String tripId, bookId;
    private  LinearLayout layoutLoad, layoutMap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_traveling, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Fragment parentFragment = getParentFragment();
        activity = parentFragment != null ? parentFragment.getActivity() : null;

        initialize(view);
        return view;
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

    private void initialize(View view) {

        // PROPERTY INITIALIZATION
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        viewHelper = new ViewHelper(getContext());

        sharedPreferences = getContext().getSharedPreferences("fmuPref", 0);

        layoutLoad = view.findViewById(R.id.layoutLoadMap);
        layoutMap = view.findViewById(R.id.mapLay);

        activity = getActivity();

        this.setViewModelObserver();

        // METHOD CALL
        this.getTrip();
    }
    // MISC FUNCTIONS
    private void hideLoadingMap() {
        layoutLoad.setVisibility(View.GONE);
        layoutMap.setVisibility(View.VISIBLE);
    }

    private void errorResult() {
        hideLoadingMap();
        viewHelper.exitActivity(activity, "Loading Failed", "Something went wrong.");
    }

    // NETWORK REQUEST

    private void getTrip() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "get_traveling_trip");
        data.put("event", "normal");
        data.put("passenger_id", sharedPreferences.getString("passenger_id", ""));
        viewModel.okHttpRequest(data, "GET", "");
    }

    private void getPickPoint() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "pick_up_point");
        data.put("event", "normal");
        data.put("book_id", bookId);
        viewModel.okHttpRequest(data, "GET", "");
    }

    private void getVanLocation() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "van_location");
        data.put("trip_id", tripId);
        viewModel.serverSentEvent(data, "");
    }

    // SERVER RESPONSE

    private void setViewModelObserver() {

        viewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                setVanLocation();
            }
        });

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                hideLoadingMap();

                String type = list.get(0).get("type");
                Log.d("DebugLog", list.toString());
                switch (type) {
                    case "get_trip":
                        bookId = list.get(0).get("booking_id");
                        tripId = list.get(0).get("trip_id");
                        drawRoute(list);
                        getPickPoint();
                        getVanLocation();
                        break;
                    case "pick_up":
                        setPickUpPointLocation();
                        break;
                }

            }
        });

        viewModel.getRouteItemList().observe(this, new Observer<List<RouteItem>>() {
            @Override
            public void onChanged(List<RouteItem> routePolyLines) {
                routeItemList = routePolyLines;
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

        googleMap.addMarker(new MarkerOptions().position(originLatLng).title(transportServiceName + " " + originName + " Terminal"));
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

    private void setPickUpPointLocation() {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_pin);
        MarkerOptions userMark = new MarkerOptions()
                .title("Pick up point")
                .position(routeItemList.get(0).getPickUpLoc())
                .icon(icon);
        googleMap.addMarker(userMark);
    }

    private Marker vanM;
    private void setVanLocation() {
        if (vanM != null) {
            vanM.remove();
        }
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.van_marker);
        MarkerOptions vanMark = new MarkerOptions()
                .title("UV Express Location")
                .position(routeItemList.get(0).getCurrentLocationLatLng())
                .icon(icon);
        vanM = googleMap.addMarker(vanMark);
    }
}
