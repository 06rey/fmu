package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.model.pojo.RouteItem;
import com.example.findmeuv.model.pojo.TripItinerary;
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
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassengerLocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    private AlertDialog alert;
    private ViewHelper viewHelper;
    private AlertDialog.Builder builder;

    private RadioButton rdMap, rdTerminal;
    private RelativeLayout pnlMap, btnPnlHideShow;
    private LinearLayout pnlChoice, subPnlChoice;
    private TextView txtHideShow;
    private Boolean isMapVisible = false;
    private Button btnNext;
    private MarkerOptions passengerMarkerOption;

    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;


    private GoogleMap mMap;
    private String pickUpLocLat = null, pickUpLocLng = null, transportServiceName;
    private ImageView imgMarker;
    private LatLng imgMarkerLatLng;
    private Marker mark;
    private PolylineOptions routePolyLine = new PolylineOptions();
    private Boolean isSnipInValidPosition = false;
    private LatLng originLatLng;
    private LatLng destinationLatLng;

    Double lat, lng;
    Marker pick_up_marker;

    private LocationManager locationManager;

    private AppViewModel viewModel;
    private TripItinerary tripItinerary = new TripItinerary();
    private List<RouteItem> routeItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);

        initViews();
        initSharedPref();
        setRadioButtonClickListener();
        setBtnHideShowClickListener();
        setBtnNextCLickListener();
        // Ask user checkPermission for access fine location
        setImgViewMarkerClick();

        setViewModelObserver();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getRoutePolyline();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                pickUpLocLat = String.valueOf(latLng.latitude);
                pickUpLocLng = String.valueOf(latLng.longitude);
            }
        });
    }

    private void setMapListeners() {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                imgMarkerLatLng = mMap.getCameraPosition().target;
                if (PolyUtil.isLocationOnPath(mMap.getCameraPosition().target, routePolyLine.getPoints(), true)) {
                    imgMarker.setBackground(getResources().getDrawable(R.drawable.target_green));
                    isSnipInValidPosition = true;
                } else {
                    imgMarker.setBackground(getResources().getDrawable(R.drawable.target_red));
                    isSnipInValidPosition = false;
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().equals("My location")) {
                    pick_up_marker.remove();
                    imgMarker.setVisibility(View.VISIBLE);
                    pickUpLocLat = null;
                    pickUpLocLng = null;
                }
                return false;
            }
        });
    }

    private void setImgViewMarkerClick() {
        imgMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSnipInValidPosition) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_pin);
                    passengerMarkerOption = new MarkerOptions()
                            .title("My location")
                            .position(imgMarkerLatLng)
                            .icon(icon);
                    pick_up_marker = mMap.addMarker(passengerMarkerOption);
                    imgMarker.setVisibility(View.INVISIBLE);

                    pickUpLocLat = String.valueOf(imgMarkerLatLng.latitude);
                    pickUpLocLng = String.valueOf(imgMarkerLatLng.longitude);
                }
            }
        });
    }

    private void initSharedPref() {
        fmuUserPref = getSharedPreferences("fmuPref", 0);
        editor = fmuUserPref.edit();

        Intent intent = getIntent();
        tripItinerary = (TripItinerary) intent.getSerializableExtra("tripItinerary");
    }

    private void initViews() {
        viewHelper = new ViewHelper(this);
        rdMap = findViewById(R.id.rdMap);
        rdTerminal = findViewById(R.id.rdTerminal);
        pnlMap = findViewById(R.id.pnlMap);
        pnlChoice = findViewById(R.id.pnlChoice);
        subPnlChoice = findViewById(R.id.subPnlChoice);
        btnPnlHideShow = findViewById(R.id.btnPnlHideShow);
        txtHideShow = findViewById(R.id.txtHideShow);
        btnNext = findViewById(R.id.btnNext);
        imgMarker = findViewById(R.id.imgViewMarker);

        viewHelper = new ViewHelper(this);
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
    }

    private void setBtnNextCLickListener() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMapVisible) {
                   if (pickUpLocLng == null) {
                       viewHelper.alertDialog("System Message", "Please select your pick up location in the map.");
                   } else {
                       tripItinerary.setBoardingPoint("Pick_up");
                       tripItinerary.setLocLat(pickUpLocLat);
                       tripItinerary.setLocLng(pickUpLocLng);
                       tripItinerary.setDeviceId(Settings.Secure.getString(getContentResolver(),
                               Settings.Secure.ANDROID_ID));
                   }
                } else {
                    tripItinerary.setBoardingPoint("Terminal");
                    tripItinerary.setDeviceId("none");
                }
                Intent intent = new Intent(PassengerLocationActivity.this, TripItineraryActivity.class);
                intent.putExtra("tripItinerary", tripItinerary);
                startActivity(intent);
            }
        });
    }

    private void setBtnHideShowClickListener() {

        btnPnlHideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMapVisible) {
                    if (txtHideShow.getText().toString().equals("Hide")) {
                        subPnlChoice.setVisibility(View.GONE);
                        txtHideShow.setText("Show");
                    } else {
                        subPnlChoice.setVisibility(View.VISIBLE);
                        txtHideShow.setText("Hide");
                    }
                }
            }
        });
    }

    private void setRadioButtonClickListener() {
        rdTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMapVisible) {
                    pnlMap.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams layParams = (RelativeLayout.LayoutParams) pnlChoice.getLayoutParams();
                    layParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                    pnlChoice.setLayoutParams(layParams);
                    txtHideShow.setVisibility(View.INVISIBLE);
                    isMapVisible = false;
                }
            }
        });

        final Context cont = this;

        rdMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                if (!isMapVisible) {
                    if (!tripItinerary.getIsImAlso()) {
                        viewHelper.alertDialog("System Message", "We use this device to communicate with the passenger until they get picked up from the boarding point. If the reserver is not a passenger, the boarding point of the passenger's will be at the "+transportServiceName+" terminal.");
                        rdTerminal.setChecked(true);
                        rdMap.setChecked(false);
                        rdMap.setEnabled(false);
                        isMapVisible = false;
                    } else {
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            new AlertDialog.Builder(cont, R.style.MyAlertDialogStyle)
                                    .setTitle("System Message")  // GPS not found
                                    .setCancelable(false)
                                    .setMessage("Device location is off. Please go to settings and turn on device location.") // Want to enable?
                                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    })
                                    .setNegativeButton("No Thanks", null)
                                    .show();
                        }
                        pnlMap.setVisibility(View.VISIBLE);
                        RelativeLayout.LayoutParams layParams = (RelativeLayout.LayoutParams) pnlChoice.getLayoutParams();
                        layParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        layParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        pnlChoice.setLayoutParams(layParams);
                        txtHideShow.setVisibility(View.VISIBLE);
                        isMapVisible = true;
                    }
                }
            }
        });
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void backClick(View view) {
        onBackPressed();
    }

    public void btnCancel(View view) {

        String message = "Are you sure you want to cancel your booking?";
        builder.setTitle("System Message")
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        viewHelper.showLoading();
                        deleteBookingQueue();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(PassengerLocationActivity.this, FmuHomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        }, 500);
                    }
                });
        alert = builder.create();
        alert.show();
    }

    private void getRoutePolyline() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "route");
        data.put("sub", "get_route_way_point");
        data.put("trip_id", tripItinerary.getTripID());

        viewModel.okHttpRequest(data, "POST", "");
    }

    private void drawRoutePolylineOnMap(List<Map<String, String>> list) {
        transportServiceName = list.get(0).get("company_name");
        originLatLng = routeItemList.get(0).getOriginLatLng();
        destinationLatLng = routeItemList.get(0).getDestLatLng();

        String originName = list.get(0).get("origin");
        String destinationName = list.get(0).get("destination");
        mMap.addMarker(new MarkerOptions().position(originLatLng).title(transportServiceName + " " + originName + " Terminal"));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(transportServiceName + " " + destinationName + " Terminal"));

        routePolyLine = routeItemList.get(0).getPolyLineOption();
        routePolyLine.color(getResources().getColor(R.color.lineColor));
        routePolyLine.visible( true );
        routePolyLine.clickable(true);
        mMap.addPolyline(routePolyLine);
        CameraPosition googlePlex = CameraPosition.builder()
                .target(originLatLng)
                .zoom(15)
                .bearing(0)
                .tilt(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
        setMapListeners();
    }

    private void deleteBookingQueue() {
        Map<String, String> deleteQueue = new HashMap<>();
        deleteQueue.put("resp", "1");
        deleteQueue.put("main", "booking");
        deleteQueue.put("sub", "delete_queue");
        deleteQueue.put("trip_id", tripItinerary.getTripID());
        deleteQueue.put("queue_id", tripItinerary.getQueueId());

        viewModel.okHttpRequest(deleteQueue, "GET", "");
    }
    private boolean isPolyLineSet = false;
    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                drawRoutePolylineOnMap(list);
                tripItinerary.setOrigin(list.get(0).get("origin"));
                tripItinerary.setDestination(list.get(0).get("destination"));
                tripItinerary.setVia(list.get(0).get("via"));
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

        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });
    }


    // Location listener
    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            lat = location.getLatitude();
            lng = location.getLongitude();
            LatLng latLng = new LatLng(lat, lng);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.cur_loc_circle16);
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title("Im here!")
                    .icon(icon);
            if (mark != null) {
                mark.remove();
            }
            mark = mMap.addMarker(options);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
