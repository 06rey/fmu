package com.example.findmeuv.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.ConfirmHandler;
import com.example.findmeuv.model.pojo.RouteItem;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view.activity.FmuHomeActivity;
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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class BookingTravelingFragment extends Fragment implements OnMapReadyCallback {

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //---------------------------------------- MAP OBJECTS AND VARIABLES ----------------------------------------
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

    //---------------------------------------- MISC VARIABLES ----------------------------------------
    private String tripId, bookId, tripUpdateTime, tripDistance, tripVacantSeat, tripOnlineStatus, booking_id, boardingStatus;

    //---------------------------------------- VIEW VARIABLES ----------------------------------------
    private View view;
    private  LinearLayout layoutLoad, noResultLayout;
    private View viewTripInfo;
    private TextView txtLocationUpdate, txtLocationDistance, txtDriverName, txtPlateNo, txtVacantSeat, txtDriverOnlineStatus;
    private Button btnAdd, btnClose, btnCancelBooking, btnNoResult;
    private AlertDialog tripInfoDialog;
    private RelativeLayout layoutMain;
    private LinearLayout btnLayout;

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

    //------------------------------------------------------------------------------------------------
    //---------------------------------------- INITIALIZATION ----------------------------------------
    //------------------------------------------------------------------------------------------------

    private void initialize(View view) {

        // PROPERTY INITIALIZATION
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        viewHelper = new ViewHelper(getContext());

        sharedPreferences = getContext().getSharedPreferences("fmuPref", 0);

        activity = getActivity();
        this.view = view;

        this.setViewModelObserver();

        layoutMain = view.findViewById(R.id.layoutMain);
        noResultLayout = view.findViewById(R.id.noResultLayout);
        btnLayout = view.findViewById(R.id.btnLayout);

        btnCancelBooking = view.findViewById(R.id.btnCancelBooking);
        btnNoResult = view.findViewById(R.id.btnNoResult);

                // Trip info dialog view
        viewTripInfo = LayoutInflater.from(getContext()).inflate(R.layout.traveling_uv_info, null);
        txtLocationUpdate = viewTripInfo.findViewById(R.id.txtLocationUpdate);
        txtLocationDistance = viewTripInfo.findViewById(R.id.txtLocationDistance);
        txtDriverName = viewTripInfo.findViewById(R.id.txtDriverName);
        txtPlateNo = viewTripInfo.findViewById(R.id.txtPlateNo);
        txtVacantSeat = viewTripInfo.findViewById(R.id.txtVacantSeat);
        txtDriverOnlineStatus = viewTripInfo.findViewById(R.id.txtDriverOnlineStatus);

        btnAdd = viewTripInfo.findViewById(R.id.btnAdd);
        btnClose = viewTripInfo.findViewById(R.id.btnClose);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setView(viewTripInfo)
                .setCancelable(false);
        tripInfoDialog = builder.create();

        setViewClickListener();
    }

    //------------------------------------------------------------------------------------------------
    //---------------------------------------- CLICK LISTENERS ---------------------------------------
    //------------------------------------------------------------------------------------------------

    private void setViewClickListener() {

        btnCancelBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHelper.confirmDialog("Cancel Reservation?", "Are you sure you want to cancel your seat reservation?");
            }
        });

        // Trip info dialog close button
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripInfoDialog.dismiss();
            }
        });

        // Confirm dialog click callback
        viewHelper.confirmHandler.setConfirmListener(new ConfirmHandler.ConfirmListener() {
            @Override
            public void onYesClick() {
                viewModel.closeServerEventConnection();
                cancelSeatReservation();
                redirectToHome();
            }

            @Override
            public void onNoClick() {
                // Do nothing
            }
        });

        // No result button
        btnNoResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToHome();
            }
        });
    }

    //------------------------------------------------------------------------------------------------
    //---------------------------------------- MISC FUNCTIONS ----------------------------------------
    //------------------------------------------------------------------------------------------------


    private void errorResult() {
        viewHelper.exitActivity(activity, "Loading Failed", "Something went wrong.");
    }

    private void setDriverOnlineStatus(String status) {
        tripOnlineStatus = status;
        if (status.equals("ONLINE")) {
            txtDriverOnlineStatus.setTextColor(getContext().getResources().getColor(R.color.green));
        } else {
            txtDriverOnlineStatus.setTextColor(getContext().getResources().getColor(R.color.red));
        }
        txtDriverOnlineStatus.setText(status);
    }

    private void redirectToHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), FmuHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                activity.finish();
            }
        }, 500);
    }

    //------------------------------------------------------------------------------------------------
    //---------------------------------------- NETWORK REQUEST ---------------------------------------
    //------------------------------------------------------------------------------------------------

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

    private void cancelSeatReservation() {
        // Cancel seat reservation
        viewHelper.showLoading();
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "delete_booking");
        data.put("event", "normal");
        data.put("book_id", booking_id);
        viewModel.okHttpRequest(data, "GET", "");
    }


    //------------------------------------------------------------------------------------------------
    //---------------------------------------- SERVER RESPONSE ---------------------------------------
    //------------------------------------------------------------------------------------------------


    private void setViewModelObserver() {

        viewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                setVanLocation(list);
            }
        });

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {

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
                        if (!boardingStatus.equals("on_board")) {
                            setPickUpPointLocation();
                        }
                        break;
                    case "delete_booking":
                        viewHelper.dismissLoading();
                        viewHelper.alertDialog("Success", "Your seat reservation has been cancelled.");
                        break;
                }
                noResultLayout.setVisibility(View.GONE);
            }
        });

        viewModel.getRouteItemList().observe(this, new Observer<List<RouteItem>>() {
            @Override
            public void onChanged(List<RouteItem> routePolyLines) {
                routeItemList = routePolyLines;
            }
        });

        // ERROR OBSERVER

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                layoutMain.setVisibility(View.GONE);
                noResultLayout.setVisibility(View.VISIBLE);
            }
        });

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

    //------------------------------------------------------------------------------------------------
    //---------------------------------------- MAP FUNCTIONS -----------------------------------------
    //------------------------------------------------------------------------------------------------


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("DebugLog", "MAP READY");
        this.googleMap = googleMap;
        this.getTrip();
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

    private void drawRoute(List<Map<String, String>> list) {

        TextView txtDestination = view.findViewById(R.id.txtDestination);
        txtDestination.setText(list.get(0).get("destination"));
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

        // Init trip info

        boardingStatus = list.get(0).get("boarding_status");

        setDriverOnlineStatus(list.get(0).get("is_online"));
        booking_id = list.get(0).get("booking_id");
        tripUpdateTime = list.get(0).get("last_online");
        tripDistance = String.format("%.2f", Float.parseFloat(list.get(0).get("uv_distance"))) + " KM";
        tripVacantSeat = list.get(0).get("vacant_seat");

        txtVacantSeat.setText(tripVacantSeat);
        txtLocationUpdate.setText(tripUpdateTime);
        txtLocationDistance.setText(tripDistance);
        txtDriverName.setText(list.get(0).get("f_name").toUpperCase() + " " + list.get(0).get("l_name").toUpperCase());
        txtPlateNo.setText(list.get(0).get("plate_no").toUpperCase());

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().contains("UV Express Location")) {
                    tripInfoDialog.show();
                }
                return false;
            }
        });
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
    private void setVanLocation(List<Map<String, String>> list) {
        if (vanM != null) {
            vanM.remove();
        }
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.van_marker);
        MarkerOptions vanMark = new MarkerOptions()
                .title("UV Express Location")
                .position(routeItemList.get(0).getCurrentLocationLatLng())
                .icon(icon);
        vanM = googleMap.addMarker(vanMark);

        boardingStatus = list.get(0).get("boarding_status");

        setDriverOnlineStatus(list.get(0).get("is_online"));
        tripUpdateTime = list.get(0).get("last_online");
        tripDistance = String.format("%.2f", Float.parseFloat(list.get(0).get("uv_distance"))) + " KM";
        tripVacantSeat = list.get(0).get("vacant_seat");

        txtVacantSeat.setText(tripVacantSeat);
        txtLocationUpdate.setText(tripUpdateTime);
        txtLocationDistance.setText(tripDistance);

        if (list.get(0).get("boarding_status").equals("on_board")) {
            btnLayout.setVisibility(View.GONE);
        }
    }


    //------------------------------------------------------------------------------------------------
    //-------------------------------------- THIS OVERRIDE FUNCTION ----------------------------------
    //------------------------------------------------------------------------------------------------

}
