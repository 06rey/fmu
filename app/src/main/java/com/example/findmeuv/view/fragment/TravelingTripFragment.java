package com.example.findmeuv.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.findmeuv.handler.FragmentBackPressedHandler;
import com.example.findmeuv.model.pojo.RouteItem;
import com.example.findmeuv.model.pojo.TravelingUvExpress;
import com.example.findmeuv.model.pojo.TripItinerary;
import com.example.findmeuv.utility.UserLocation;
import com.example.findmeuv.view.activity.BookingActivity;
import com.example.findmeuv.view.activity.FmuHomeActivity;
import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.location.Location;
import android.location.LocationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class TravelingTripFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private GoogleMap googleMap;
    private Marker pickUpPointMarker;
    private LinearLayout noResult;
    private boolean pickUpPointMarkerIsClickable = true;
    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;

    private UserLocation userLocation = new UserLocation();
    private LatLng userCurrentLatLng;
    private AlertDialog waitingUserLocationDialog;
    private Marker userMarker;
    private boolean userLocationIsReady = false, waitingForLocation = false, pickUpPointIsReady = false, routeItemIsReady = false, serverSentEventIsRunning = false, tripDetailsDialogIsOpen = false;
    private ImageView sniperMarker;
    private LatLng pickUpPointLatLng;
    private Boolean sniperLatLngIsValid = false;

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private List<RouteItem> routeList, routeItemList;
    private List<LatLng> pickUpPointLine;
    private List<Marker> uvMarkerList = new ArrayList<>();

    private AlertDialog destinationOptionDialog, tripDetailsDialog;
    private final static String TAG = "DebugLog";

    private String type;
    private boolean cloneLocation = false;
    private LatLng cloneVal = new LatLng(11.204227, 125.010693);//new LatLng(11.219460706249155, 124.99335919734938); //new LatLng(11.270362068860159, 124.9637014886307)

    private TextView txtHeadingTo, tripId, destination, arrival, plate, vehicle, distance, seat;
    private int noOfPass = 0;
    private TravelingUvExpress selectedUvExpress;
    private FragmentBackPressedHandler fragmentBackPressedHandler;
    private TripItinerary tripItinerary = new TripItinerary();
    private final static int UV_MAX_DISTANCE = 0;
    private boolean isStatusChecked = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.travelling_trip, container, false);
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initViews(view);
        setViewModelObserver();
        checkTravelingStatus();
        return view;
    }

    private void setMapListeners() {
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                pickUpPointLatLng = googleMap.getCameraPosition().target;
                if (PolyUtil.isLocationOnPath(pickUpPointLatLng, pickUpPointLine, true)) {
                    sniperMarker.setBackground(getResources().getDrawable(R.drawable.target_green));
                    sniperLatLngIsValid = true;
                } else {
                    sniperMarker.setBackground(getResources().getDrawable(R.drawable.target_red));
                    sniperLatLngIsValid = false;
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() == null) {
                    if (!marker.getTitle().contains("Terminal")) {
                        if (pickUpPointMarkerIsClickable) {
                            pickUpPointMarker.remove();
                            sniperMarker.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    selectedUvExpress = (TravelingUvExpress) marker.getTag();
                    showTripDetails(selectedUvExpress);
                }
                return false;
            }
        });
    }

    private void setImgViewMarkerClick() {
        sniperMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sniperLatLngIsValid) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_pin);
                    pickUpPointMarker = googleMap.addMarker(new MarkerOptions()
                            .title("My Pick Up Point")
                            .position(pickUpPointLatLng)
                            .icon(icon)
                    );
                    sniperMarker.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(getContext(), "Pick up point location is not allowed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isMapReady = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        isMapReady = true;
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(11.224951574789777,125.01982289054729))
                .zoom(8)
                .bearing(0)
                .tilt(0)
                .build();
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);

        if (isStatusChecked) {
            if (cloneLocation) {
                userCurrentLatLng = cloneVal;
                if (!userLocationIsReady) {
                    userLocationIsReady = true;
                    setUserMarker(userCurrentLatLng);
                    getPossibleDestination();
                }
            }
        }
    }

    private void initViews(View view) {

        fmuUserPref = getContext().getSharedPreferences("fmuPref", 0);

        viewHelper = new ViewHelper(getContext());
        txtHeadingTo = view.findViewById(R.id.txtHeadTo);
        sniperMarker = view.findViewById(R.id.imgViewMarkerTravel);
        noResult = view.findViewById(R.id.tripLayout);

        setImgViewMarkerClick();

        Button btnCancel = view.findViewById(R.id.btnCancel);
        final Button btnFindUv = view.findViewById(R.id.btnNext);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle)
                        .setTitle("System Message")
                        .setMessage("Are sure you want to cancel your booking?")
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectToHome();
                            }
                        }).show();
            }
        });

        btnFindUv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (sniperMarker.getVisibility() == View.INVISIBLE) {
                   pickUpPointMarkerIsClickable = false;
                   CameraPosition cameraPosition = CameraPosition.builder()
                           .target(new LatLng(userCurrentLatLng.latitude,userCurrentLatLng.longitude))
                           .zoom(9)
                           .bearing(0)
                           .tilt(0)
                           .build();
                   googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
                   viewHelper.showLoading();
                   viewModel.serverSentInitRequest(prepareParam(), "GET", "");
               } else {
                  viewHelper.alertDialog("System Message", "Please pin your boarding point.");
               }
            }
        });
    }

    private void redirectToHome() {
        fragmentBackPressedHandler.removeFragmentBackPressedListener();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), FmuHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                getActivity().finish();
            }
        }, 500);
    }

    private void showTripDetails(TravelingUvExpress uvExpress) {
        View tripDetailsView = getLayoutInflater().inflate(R.layout.traveling_trip_details, null);

        tripId = tripDetailsView.findViewById(R.id.tripId);
        destination = tripDetailsView.findViewById(R.id.destination);
        arrival = tripDetailsView.findViewById(R.id.arrival);
        plate = tripDetailsView.findViewById(R.id.plate);
        vehicle = tripDetailsView.findViewById(R.id.vehicle);
        distance = tripDetailsView.findViewById(R.id.distanceInKm);
        seat = tripDetailsView.findViewById(R.id.seat);

        destination.setText(uvExpress.getDestination());
        arrival.setText(uvExpress.getArrival());
        plate.setText(uvExpress.getPlateNo());
        vehicle.setText(uvExpress.getModel());
        distance.setText(uvExpress.getDistanceInKm());
        seat.setText(uvExpress.getVacantSeat());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle(uvExpress.getCompanyName())
                .setView(tripDetailsView)
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tripDetailsDialogIsOpen = false;
                        destination.setText("");
                        arrival.setText("");
                        plate.setText("");
                        vehicle.setText("");
                        distance.setText("");
                        seat.setText("");
                        tripDetailsDialog.dismiss();
                    }
                })
                .setPositiveButton("Book Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getNumberOfPass();
                    }
                });
        tripDetailsDialog = builder.create();
        tripDetailsDialog.show();
        tripDetailsDialogIsOpen = true;

        if (uvExpress.getDistanceInMeter() < UV_MAX_DISTANCE) {
            tripDetailsDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    private void updateTripDetails(List<Map<String, String>> tripMapList, List<RouteItem> routeItems) {
        int listSize = tripMapList.size();
        boolean selectedUvIsExists = false;
        for (Marker marker: uvMarkerList) {
            marker.remove();
        }
        uvMarkerList = new ArrayList<>();
        for (int i=1; i<listSize; i++) {

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.van_marker);
            LatLng latLng = routeItems.get(i).getCurrentLocationLatLng();
            TravelingUvExpress travelingUvExpress = new TravelingUvExpress();

            Float temp = Float.parseFloat(tripMapList.get(i).get("distance_to_user"))/1000;
            String distance = String.format("%.2f", temp) + " km";
            String tripId = tripMapList.get(i).get("trip_id");

            if (selectedUvExpress != null) {
                if (selectedUvExpress.getTripId().equals(tripId)) {
                    selectedUvIsExists = true;
                }
            }

            travelingUvExpress.setTripId(tripId);
            travelingUvExpress.setCompanyName(tripMapList.get(i).get("company_name"));
            travelingUvExpress.setDestination(tripMapList.get(i).get("destination"));
            travelingUvExpress.setArrival(tripMapList.get(i).get("arrival_time"));
            travelingUvExpress.setPlateNo(tripMapList.get(i).get("plate_no"));
            travelingUvExpress.setModel(tripMapList.get(i).get("model"));
            travelingUvExpress.setDistanceInKm(distance);
            travelingUvExpress.setVacantSeat(tripMapList.get(i).get("max_pass"));
            travelingUvExpress.setDistanceInMeter(Float.parseFloat(tripMapList.get(i).get("distance_to_user")));

            if (tripDetailsDialogIsOpen) {
                if (selectedUvExpress.getTripId().equals(tripId)) {
                    arrival.setText(tripMapList.get(i).get("arrival_time"));
                    this.distance.setText(distance);
                    seat.setText(tripMapList.get(i).get("max_pass"));
                }
            }

            Marker mark = googleMap.addMarker(new MarkerOptions()
                    .icon(icon)
                    .position(latLng));
            mark.setTag(travelingUvExpress);
            uvMarkerList.add(mark);
        }

        if (tripDetailsDialogIsOpen && !selectedUvIsExists) {
            tripDetailsDialog.dismiss();
        }
    }

    private void setViewModelObserver() {
        // SSE success observer
        viewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String status = list.get(0).get("status");
                switch (status){
                    case "Success":
                        if (noResult.getVisibility() == View.VISIBLE) {
                            noResult.setVisibility(View.GONE);
                        }
                        break;
                    case "No result":
                        noResult.setVisibility(View.VISIBLE);
                        break;
                }
                updateTripDetails(list, routeItemList);
            }
        });

        viewModel.getServerSentInitData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                viewHelper.dismissLoading();
                String status = list.get(0).get("status");
                switch (status) {
                    case "Success":
                        if (noResult.getVisibility() == View.VISIBLE) {
                            noResult.setVisibility(View.GONE);
                        }
                        updateTripDetails(list, routeItemList);
                        break;
                    case "No result":
                        noResult.setVisibility(View.VISIBLE);
                        break;
                }
                viewModel.serverSentEvent(prepareParam(), "");
                serverSentEventIsRunning = true;
            }
        });
        // SSE error
        viewModel.getSseConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                builder.setTitle("System Message")
                        .setMessage("Fail to connect to server. Try again.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                viewHelper.showLoading();
                                viewModel.serverSentInitRequest(prepareParam(), "GET", "");
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        viewModel.getSseServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "This service is not available at the moment.");
            }
        });

        viewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Something went wrong there.");
            }
        });

        viewModel.getSseInitDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Something went wrong there.");
            }
        });

        viewModel.getSseInitJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Something went wrong there.");
            }
        });

        viewModel.getRouteItemList().observe(this, new Observer<List<RouteItem>>() {
            @Override
            public void onChanged(List<RouteItem> routeItem) {
                routeItemList = routeItem;
                routeItemIsReady = true;
            }
        });

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                type = list.get(0).get("type");
                switch (type) {
                    case "possible_destination":
                        waitingUserLocationDialog.dismiss();
                        setPossibleDestinationOption(list);
                        break;
                    case "way_point":
                        prepareRouteList(list);
                        viewHelper.showLoading();
                        Map<String, String> param = prepareParam();
                        param.put("type", "conflict");
                        viewModel.okHttpRequest(param, "GET", "");
                        break;
                    case "conflict":
                        viewHelper.dismissLoading();
                        String msg = "You have a trip from " + list.get(1).get("origin") + " to " +
                                list.get(1).get("destination") + " later at " + list.get(1).get("depart_time") + ". This will result to conflict trip schedules.";

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                        builder.setTitle("System Message")
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        redirectToHome();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                    case "no_conflict":
                        viewHelper.dismissLoading();
                        new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle)
                                .setTitle("System Message")
                                .setMessage("Select and pin your pick up location on the green line on the map.")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CameraPosition cameraPosition = CameraPosition.builder()
                                                .target(routeList.get(1).getNearestLatLng())
                                                .zoom(15)
                                                .bearing(0)
                                                .tilt(0)
                                                .build();
                                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null);
                                    }
                                }).show();
                        break;
                    case "enqueue":
                        if (list.get(0).get("status").equals("success")) {
                            tripItinerary.setArriveTime(selectedUvExpress.getArrival());
                            tripItinerary.setTripID(selectedUvExpress.getTripId());
                            tripItinerary.setNoOfPass(String.valueOf(noOfPass));
                            tripItinerary.setBookMode("traveling");
                            tripItinerary.setQueueId(String.valueOf(list.get(0).get("id")));
                            tripItinerary.setAvailableSeat(selectedUvExpress.getVacantSeat());
                            tripItinerary.setTransportService(selectedUvExpress.getCompanyName());
                            tripItinerary.setLocLat(String.valueOf(pickUpPointMarker.getPosition().latitude));
                            tripItinerary.setLocLng(String.valueOf(pickUpPointMarker.getPosition().longitude));
                            tripItinerary.setBoardingPoint("Pick_up");
                            try {
                                tripItinerary.setDeviceId(Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID));
                            } catch (Exception e) {
                                tripItinerary.setDeviceId("Not Available");
                            }
                            Intent booking = new Intent(getContext(), BookingActivity.class);
                            booking.putExtra("selected_trip", tripItinerary);
                            getContext().startActivity(booking);
                            viewHelper.dismissLoading();
                        } else if (list.get(0).get("status").equals("failed")) {
                            viewHelper.alertDialog("System Message", "Sorry. Some available seat of this trip has been reserved just now and cannot accommodate " + String.valueOf(noOfPass) + " passenger(s).");
                        }
                        break;
                    case "check_traveling":
                        if (list.get(0).get("status").equals("success")) {
                            isStatusChecked = true;
                            if (isMapReady) {
                                if (cloneLocation) {
                                    userCurrentLatLng = cloneVal;
                                    if (!userLocationIsReady) {
                                        userLocationIsReady = true;
                                        setUserMarker(userCurrentLatLng);
                                        getPossibleDestination();
                                    }
                                }
                            }
                        } else {
                            waitingUserLocationDialog.dismiss();
                            AlertDialog.Builder travel = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                            travel.setTitle("Not Allowed")
                                    .setMessage("You have already book traveling trip.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            redirectToHome();
                                        }
                                    });
                            AlertDialog alst = travel.create();
                            alst.show();
                        }
                        break;
                }
            }
        });

        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                waitingUserLocationDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                builder.setTitle("System Message")
                        .setMessage("Fail to connect to server.")
                        .setCancelable(false)
                        .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                waitForUserLocationDialog();
                                getPossibleDestination();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                waitingUserLocationDialog.dismiss();
                viewHelper.alertDialog("System Message", "Service is not available at the moment.");
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                waitingUserLocationDialog.dismiss();
                viewHelper.alertDialog("System Message", "Something went wrong there.");
            }
        });
        final LocationListener locationListener = this;
        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                waitingUserLocationDialog.dismiss();
                userLocation.locationManager.removeUpdates(locationListener);
                viewHelper.alertDialog("System Message", "This service is not available in this area.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                waitingUserLocationDialog.dismiss();
                viewHelper.alertDialog("System Message", "Something went wrong there.");
            }
        });
    }

    private Map<String, String> prepareParam() {
        Map<String, String> param = new HashMap<>();
        int size = routeList.size();
        for (int i=1; i<size; i++) {
            String index = String.valueOf(i);
            RouteItem routeItem = routeList.get(i);
            param.put("route_id" + index, routeItem.getRouteId());
        }
        param.put("resp", "1");
        param.put("main", "route");
        param.put("sub", "traveling_trip");
        param.put("status", "Traveling");
        param.put("lat", String.valueOf(routeList.get(1).getNearestLatLng().latitude));
        param.put("lng", String.valueOf(routeList.get(1).getNearestLatLng().longitude));
        param.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
        param.put("size", String.valueOf(routeList.size()));
        param.put("head", txtHeadingTo.getText().toString());
        return param;
    }

    private void setRoutePolyLineOnMap() {
        int size = routeList.size();
        for (int i=1; i<size; i++) {
            RouteItem routeItem = routeList.get(i);
            googleMap.addMarker(new MarkerOptions()
                    .position(routeItem.getDestLatLng())
                    .title(routeItem.getCompanyName() + " " + routeItem.getDestination() + " Terminal"));
            googleMap.addMarker(new MarkerOptions()
                    .position(routeItem.getOriginLatLng())
                    .title(routeItem.getCompanyName() + " " + routeItem.getOrigin() + " Terminal"));

            routeItem.getFromOrigin().color(getActivity().getResources().getColor(R.color.lineColor))
                    .visible(true);
            routeItem.getToDestination().color(getActivity().getResources().getColor(R.color.to_destination_line_color))
                    .visible(true);
            routeItem.getValidPickUpPointLine().color(getActivity().getResources().getColor(R.color.green))
                    .width(20)
                    .visible(true);
            googleMap.addPolyline(routeItem.getFromOrigin());
            googleMap.addPolyline(routeItem.getToDestination());
            googleMap.addPolyline(routeItem.getValidPickUpPointLine());
        }
    }

    private void prepareRouteList(List<Map<String, String>> list) {
        if (routeItemIsReady) {
            routeList = routeItemList;
            int size = list.size();
            for (int i=1; i<size; i++) {
                Map<String, String> item = list.get(i);
                routeList.get(i).setCompanyName(item.get("company_name"));
                routeList.get(i).setDestination(item.get("destination"));
                routeList.get(i).setOrigin(item.get("origin"));
                routeList.get(i).setNearest(item.get("nearest_distance"));
                routeList.get(i).setCompanyName(item.get("company_name"));
                routeList.get(i).setNearest(item.get("nearest_distance"));
                routeList.get(i).setRouteId(item.get("route_id"));
            }
            pickUpPointLine = routeList.get(1).getValidPickUpPointLine().getPoints();
            setRoutePolyLineOnMap();
            setMapListeners();
        } else {
            prepareRouteList(list);
        }
    }

    private void setPossibleDestinationOption(List<Map<String, String>> list) {

        int dataSize = list.get(1).size();
        View view = getLayoutInflater().inflate(R.layout.destination_layout, null);
        LinearLayout parentLayout = view.findViewById(R.id.parentLayout);
        for (int i=0; i<dataSize; i++) {
            View txtView = getLayoutInflater().inflate(R.layout.txt_layout, null);
            final TextView txtDest = txtView.findViewById(R.id.txtDest);
            parentLayout.setPadding(0, 20, 0, 0);
            txtDest.setText(list.get(1).get("dest"+String.valueOf(i)));
            parentLayout.addView(txtView);
            final String dest = list.get(1).get("dest"+String.valueOf(i));
            txtDest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtHeadingTo.setText(dest);
                    viewHelper.showLoading();
                    getRouteWayPoint(dest);
                    destinationOptionDialog.dismiss();
                }
            });
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("Where are you heading to?")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        redirectToHome();
                    }
                });
        destinationOptionDialog = builder.create();
        destinationOptionDialog.show();
    }

    private void getRouteWayPoint(String head) {
        Map<String, String> param = new HashMap<>();
        param.put("resp", "1");
        param.put("main", "route");
        param.put("sub", "get_nearest_route_way_point");
        param.put("lat", String.valueOf(userCurrentLatLng.latitude));
        param.put("lng", String.valueOf(userCurrentLatLng.longitude));
        param.put("head", head);
        viewModel.okHttpRequest(param, "GET", "");
    }

    private void checkTravelingStatus() {
        waitForUserLocationDialog();
        Map<String, String> param = new HashMap<>();
        param.put("resp", "1");
        param.put("main", "booking");
        param.put("sub", "check_book");
        param.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
        viewModel.okHttpRequest(param, "GET", "");
    }

    // Get user possible destination
    private void getPossibleDestination() {
        Map<String, String> param = new HashMap<>();
        param.put("resp", "1");
        param.put("main", "route");
        param.put("sub", "get_nearest_route");
        param.put("lat", String.valueOf(userCurrentLatLng.latitude));
        param.put("lng", String.valueOf(userCurrentLatLng.longitude));
        viewModel.okHttpRequest(param, "POST", "");
    }

    // Waiting user current location update
    private void waitForUserLocationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);
        builder.setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContext().startActivity(new Intent(getContext(), FmuHomeActivity.class));
                    }
                });
        waitingUserLocationDialog = builder.create();
        waitingUserLocationDialog.show();
        waitingForLocation = true;

    }

    private void enqueueBooking(int noOfPass) {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "enqueue_booking");
        data.put("event", "normal");
        data.put("mode", "Traveling");
        data.put("trip_id", selectedUvExpress.getTripId());
        data.put("no_of_pass", String.valueOf(noOfPass));
        viewModel.okHttpRequest(data, "GET", "");
    }

    private AlertDialog numberOfPassDialog;
    private void getNumberOfPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.get_no_of_passenger, null);
        LinearLayout pnl = view.findViewById(R.id.pnlNoPass);
        pnl.setVisibility(View.GONE);
        final TextView txtNoPass = view.findViewById(R.id.txtNoOfPass);
        builder.setTitle("Enter number of passenger")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        numberOfPassDialog.dismiss();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!(txtNoPass.getText().toString().trim().equals("")) && !(Integer.parseInt(txtNoPass.getText().toString()) < 1) && !(Integer.parseInt(txtNoPass.getText().toString()) > 14)) {
                            noOfPass = Integer.parseInt(txtNoPass.getText().toString());
                            viewHelper.showLoading();
                            enqueueBooking(noOfPass);
                        } else {
                            new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle)
                                    .setCancelable(false)
                                    .setTitle("System Message")
                                    .setMessage("Please enter a number not less than zero or not greater than fourteen.")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getNumberOfPass();
                                        }
                                    }).show();
                        }
                    }
                });
        numberOfPassDialog = builder.create();
        numberOfPassDialog.show();
    }

    // User map marker
    private void setUserMarker(LatLng latLng) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.current_loc);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Im here!")
                .icon(icon);
        if (userMarker != null) {
            userMarker.remove();
        }
        userMarker = googleMap.addMarker(options);
        final double EARTH_RADIUS = 1.56786;
        double cirRadius = 1000 * EARTH_RADIUS;
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(cirRadius)
                .fillColor(0x110000FF)
                .strokeColor(0x40ff0000)
                .strokeWidth(5);
        googleMap.addCircle(circleOptions);
    }

    // Location listener
    @Override
    public void onLocationChanged(Location location) {
        LatLng  latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (cloneLocation) {
            userCurrentLatLng = cloneVal;
            if (!userLocationIsReady) {
                userLocationIsReady = true;
                setUserMarker(cloneVal);
                getPossibleDestination();
            }
        } else {
            userCurrentLatLng = latLng;
            if (!userLocationIsReady) {
                userLocationIsReady = true;
                setUserMarker(latLng);
                getPossibleDestination();
            }
        }


    }

    @Deprecated
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getContext(), "GPS is enabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getContext(), "GPS is disabled.", Toast.LENGTH_SHORT).show();
        if (!pickUpPointIsReady) {
            viewHelper.enabledGpsDialog(getActivity(), new Intent(getActivity(), FmuHomeActivity.class));
        }
    }

    @Override
    public void onPause() {
        userLocation.locationManager.removeUpdates(this);
        if (serverSentEventIsRunning) {
            viewModel.closeServerEventConnection();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (serverSentEventIsRunning) {
            viewHelper.showLoading();
            viewModel.serverSentInitRequest(prepareParam(), "GET", "");
        }
        userLocation.initLocationManager(getActivity(), this);
        super.onResume();
    }

    public void setFragmentHandlerListener(final FragmentBackPressedHandler backPressedHandler) {
        fragmentBackPressedHandler = backPressedHandler;
        fragmentBackPressedHandler.setFragmentBackPressedListener(new FragmentBackPressedHandler.FragmentBackPressedListener() {
            @Override
            public void onFragmentBackPressed() {
                new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle)
                        .setTitle("System Message")
                        .setMessage("Are sure you want to cancel your booking?")
                        .setCancelable(false)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fragmentBackPressedHandler.finishFragment(false);
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fragmentBackPressedHandler.finishFragment(true);
                            }
                        }).show();
            }
        });
    }
}