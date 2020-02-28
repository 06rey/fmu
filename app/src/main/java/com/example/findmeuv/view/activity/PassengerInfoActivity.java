package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.model.pojo.TripItinerary;
import com.example.findmeuv.view.fragment.FmuDashboardFragment;
import com.example.findmeuv.view_model.AppViewModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PassengerInfoActivity extends AppCompatActivity {

    private LinearLayout detailsLayout, parentLayout;
    private LayoutInflater inflater;

    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;

    private List<Map<String, EditText>> passenger;
    private int no_of_pass;

    private TripItinerary tripItinerary;

    private AlertDialog alert;
    private ViewHelper dialog;
    private AlertDialog.Builder builder;

    private TextView txtImAlso;
    private CheckBox imAlso;
    private Boolean check = false;
    private EditText txtBookingNotes;
    private Button btnNext, btnCancel;

    private AppViewModel viewModel;
    private boolean hasRecord = false;
    private ViewHelper viewHelper;
    private AlertDialog confirmationDialog;

    private int second = 0;
    private static final int MAX_WAITING = 10;

    private LinearLayout imAlsoLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_info);
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        initView();
        initTripItinerary();

        inflateChildLayout(no_of_pass);

        setCheckBoxClickListener();
        setBtnNextClickListener();
        setBtnCancelClickListener();

        setViewModelObserver();

        if (!tripItinerary.getBookMode().equals("pending")) {
            if (!tripItinerary.getBookMode().equals("update")) {
                passenger.get(0).get("fname").setText(fmuUserPref.getString("f_name", ""));
                passenger.get(0).get("lname").setText(fmuUserPref.getString("l_name", ""));
                passenger.get(0).get("contact").setText(fmuUserPref.getString("contact", ""));
                passenger.get(0).get("fname").setEnabled(false);
                passenger.get(0).get("lname").setEnabled(false);
                passenger.get(0).get("contact").setEnabled(false);
                tripItinerary.setDeviceId(Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID));
                imAlso.setChecked(true);
                imAlso.setEnabled(false);
                check = true;
            }
        } else {
            dialog.showLoading();
            Map<String, String> checkRecord = new HashMap<>();
            checkRecord.put("resp", "1");
            checkRecord.put("main", "booking");
            checkRecord.put("sub", "check_booking_record");
            checkRecord.put("type", "saving_info");
            checkRecord.put("trip_id", tripItinerary.getTripID());
            checkRecord.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
            viewModel.okHttpRequest(checkRecord, "GET", "");
        }
    }

    private void initView() {
        detailsLayout = findViewById(R.id.detailsLayout);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        txtImAlso = findViewById(R.id.txtImAlso);
        imAlso = findViewById(R.id.checkBoxAlso);
        btnCancel = findViewById(R.id.btnCancel);
        btnNext = findViewById(R.id.btnNext);
        txtBookingNotes = findViewById(R.id.txtBookingNotes);
        imAlsoLayout = findViewById(R.id.imAlsoLayout);

        passenger = new ArrayList<>();
        dialog = new ViewHelper(this);
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        viewModel.initialize(this);
        viewHelper = new ViewHelper(this);
    }

    private void initTripItinerary() {
        fmuUserPref = getSharedPreferences("fmuPref", 0);
        editor = fmuUserPref.edit();

        Intent intent = getIntent();
        tripItinerary = (TripItinerary) intent.getSerializableExtra("tripItinerary");
        no_of_pass = Integer.parseInt(tripItinerary.getNoOfPass());
        if (no_of_pass < 2) {
            txtImAlso.setText("Are you the passenger?");
            imAlso.setText("I'm the passenger.");
        }

        if (tripItinerary.getBookMode().equals("update")) {
            imAlsoLayout.setVisibility(View.GONE);
        }
    }

    private List<LinearLayout> parentLinearMap = new ArrayList<>();

    private void inflateChildLayout(int no_of_pass) {
        for (int x=0; x<no_of_pass; x++) {

            View view = getLayoutInflater().inflate(R.layout.passenger_details_layout, detailsLayout, false);
            parentLayout = view.findViewById(R.id.parentLayout);
            parentLinearMap.add(parentLayout);
            detailsLayout.addView(view);
            view.setId(x);
            // init views child
            TextView txtHeader = view.findViewById(R.id.txtHeader);
            EditText txtFname = view.findViewById(R.id.txtFname);
            EditText txtLname = view.findViewById(R.id.txtLname);
            EditText txtContact = view.findViewById(R.id.txtContactNumber);

            viewHelper.setTextListener(txtContact);

            // set text to header
            txtHeader.setText("Passenger " + String.valueOf(x+1) + " Details");

            // store object in list
            Map<String, EditText> map = new HashMap<>();
            map.put("fname", txtFname);
            map.put("lname", txtLname);
            map.put("contact", txtContact);
            passenger.add(map);
        }
    }

    private void setCheckBoxClickListener() {
        imAlso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check) {
                    passenger.get(0).get("fname").setText("");
                    passenger.get(0).get("lname").setText("");
                    passenger.get(0).get("contact").setText("");
                    passenger.get(0).get("fname").setEnabled(true);
                    passenger.get(0).get("lname").setEnabled(true);
                    passenger.get(0).get("contact").setEnabled(true);
                    check = false;
                } else {
                    if (!hasRecord) {
                        if (!tripItinerary.getBookMode().equals("update")) {
                            passenger.get(0).get("fname").setText(fmuUserPref.getString("f_name", ""));
                            passenger.get(0).get("lname").setText(fmuUserPref.getString("l_name", ""));
                            passenger.get(0).get("contact").setText(fmuUserPref.getString("contact", ""));
                            passenger.get(0).get("fname").setEnabled(false);
                            passenger.get(0).get("lname").setEnabled(false);
                            passenger.get(0).get("contact").setEnabled(false);
                            check = true;
                        }
                    } else {
                        imAlso.setChecked(false);
                        String message = "You still have a pending trip reserved. You cannot reserve different trip or a seat for yourself multiple times at a time. If you wish to change your trip itinerary, you may go to booking option to update your reservation.";
                        builder.setTitle("System Message")
                                .setMessage(message)
                                .setCancelable(false)
                                .setNegativeButton("NO", null)
                                .setPositiveButton("GO TO BOOKING", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(PassengerInfoActivity.this, FmuHomeActivity.class);
                                        intent.putExtra("booking", true);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        startActivity(intent);
                                        deleteBookingQueue();
                                    }
                                });
                        alert = builder.create();
                        alert.show();
                    }
                }
            }
        });
    }

    private void redirectToHome() {
        Intent intent = new Intent(PassengerInfoActivity.this, FmuHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void setBtnCancelClickListener() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Are you sure you want to cancel your booking?";
                builder.setTitle("System Message")
                        .setMessage(message)
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                PassengerInfoActivity.this.dialog.showLoading();
                                deleteBookingQueue();
                                redirectToHome();
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });
    }

    private Boolean validateInput() {
        boolean hasEmpty = false;
        for (int x=0; x<parentLinearMap.size(); x++) {
            LinearLayout inputParent = parentLinearMap.get(x);
            for( int i = 0; i < inputParent.getChildCount(); i++ ) {
                boolean editTextIsDisabled = false;
                if( inputParent.getChildAt( i ) instanceof EditText ) {
                    EditText editText = ((EditText) inputParent.getChildAt(i));
                    if (!editText.isEnabled()) {
                        editText.setEnabled(true);
                        editTextIsDisabled = true;
                    }
                    if (editText.getText().toString().trim().length() == 0) {
                        editText.setError(editText.getHint() + " is required.");
                        editText.getId();
                        hasEmpty = true;
                    }
                    if (R.id.txtContactNumber == editText.getId()) {
                        if (editText.getText().toString().length() != 11) {
                            editText.setError("Contact number must be 11 digit.");
                            hasEmpty = true;
                        }
                    }
                    if (editTextIsDisabled) {
                        editText.setEnabled(false);
                    }
                }
            }
        }
        return hasEmpty;
    }

    private void setBtnNextClickListener() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateInput()) {
                    List<Map<String, String>> list = new ArrayList<>();
                    for (int x=0; x<passenger.size(); x++) {

                        Map<String, String> map = new HashMap<>();
                        map.put("fname", passenger.get(x).get("fname").getText().toString());
                        map.put("lname", passenger.get(x).get("lname").getText().toString());
                        map.put("contact", passenger.get(x).get("contact").getText().toString());
                        list.add(map);
                    }

                    String passengerJson = new Gson().toJson(list);
                    tripItinerary.setPassengerJson(passengerJson);
                    tripItinerary.setImAlso(check);
                    tripItinerary.setNote(txtBookingNotes.getText().toString());

                    if (tripItinerary.getBookMode().equals("pending")) {
                        Intent intent = new Intent(PassengerInfoActivity.this, PassengerLocationActivity.class);
                        intent.putExtra("tripItinerary", tripItinerary);
                        startActivity(intent);
                    } else if (tripItinerary.getBookMode().equals("update")) {
                        dialog.showProgressBar("Saving reservation...");
                        updateBooking();
                    } else {
                        dialog.showProgressBar("Saving reservation...");
                        postData();
                    }
                } else {
                    dialog.alertDialog("System Message", "All fields cannot be empty.");
                }
            }
        });
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

    private void updateBookingQueue() {
        Map<String, String> updateQueue = new HashMap<>();
        updateQueue.put("resp", "1");
        updateQueue.put("main", "booking");
        updateQueue.put("sub", "update_booking_queue");
        updateQueue.put("trip_id", tripItinerary.getTripID());
        updateQueue.put("queue_id", tripItinerary.getQueueId());
        updateQueue.put("status", "selecting");

        viewModel.okHttpRequest(updateQueue, "GET", "");
    }

    private void setViewModelObserver() {
        viewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(final List<Map<String, String>> list) {
                String status = list.get(0).get("status");
                String type = list.get(0).get("type");
            }
        });

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String type = list.get(0).get("type");
                String message = "Your seat reservation has been successfully saved. Thank you.";
                if (type.equals("save_booking")) {
                    dialog.dismissProgressBar();
                    builder.setTitle("System Message")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(PassengerInfoActivity.this, FmuHomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        }
                                    }, 200);
                                }
                            });
                    alert = builder.create();
                    alert.show();
                } else if (type.equals("check_record")) {
                    dialog.dismissLoading();
                        if (list.get(0).get("status").equals("true")) {
                            hasRecord = true;
                        } else {
                            hasRecord = false;
                        }
                    dialog.alertDialog("System Message", "We use passenger's contact number to notify for further update of the trip. Contact number provided must be active.");
                } else if (type.equals("update_booking")) {
                    dialog.dismissProgressBar();
                    builder.setTitle("System Message")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(PassengerInfoActivity.this, FmuHomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        }
                                    }, 200);
                                }
                            });
                    alert = builder.create();
                    alert.show();
                }

            }
        });

        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                String message = "Internet conection problem. Please check your device network setting.";
                builder.setTitle("System Message")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                String message = "Sorry. This service in not available at the moment.";
                builder.setTitle("System Message")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                String message = "Sorry. Something went wrong there.";
                builder.setTitle("System Message")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });
    }

    private void updateBooking() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "update_booking");
        data.put("booking_id", tripItinerary.getBookId());
        data.put("no_of_passenger", tripItinerary.getNoOfPass());
        data.put("trip_id", tripItinerary.getTripID());
        data.put("queue_id", tripItinerary.getQueueId());

        int size = Integer.parseInt(tripItinerary.getNoOfPass());
        for (int i=1; i<=size; i++) {
            data.put("seat"+String.valueOf(i), tripItinerary.getSeat("seat"+String.valueOf(i)));
        }

        try {
            JSONArray jArray = new JSONArray(tripItinerary.getPassengerJson());
            for (int i=0; i<jArray.length(); i++) {
                JSONObject obj = jArray.getJSONObject(i);
                for (int x=0; x<obj.length(); x++) {
                    data.put(obj.names().getString(x) + String.valueOf(i+1), obj.getString(obj.names().getString(x)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DebugLog", data.toString());

        viewModel.okHttpRequest(data, "GET", "");
    }

    private void postData() {
        Map<String, String> postData = new HashMap<>();
        int size = Integer.parseInt(tripItinerary.getNoOfPass());

        postData.put("resp", "1");
        postData.put("main", "booking");
        postData.put("sub", "save_booking");
        postData.put("no_of_passenger", tripItinerary.getNoOfPass());
        postData.put("amount", "0");
        postData.put("pass_type", "Pick-up");
        postData.put("trip_id", tripItinerary.getTripID());
        postData.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
        postData.put("notes", tripItinerary.getNote());
        postData.put("boarding_point", tripItinerary.getBoardingPoint());
        postData.put("locLat", tripItinerary.getLocLat());
        postData.put("locLng", tripItinerary.getLocLng());
        postData.put("queue_id", tripItinerary.getQueueId());
        postData.put("device_id", tripItinerary.getDeviceId());
        postData.put("im_a_passenger", "true");

        for (int i=1; i<=size; i++) {
            postData.put("seat"+String.valueOf(i), tripItinerary.getSeat("seat"+String.valueOf(i)));
        }
        try {
            JSONArray jArray = new JSONArray(tripItinerary.getPassengerJson());
            for (int i=0; i<jArray.length(); i++) {
                JSONObject obj = jArray.getJSONObject(i);
                for (int x=0; x<obj.length(); x++) {
                    postData.put(obj.names().getString(x) + String.valueOf(i+1), obj.getString(obj.names().getString(x)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        viewModel.okHttpRequest(postData, "POST", "");
    }

    @Override
    public void onBackPressed() {
        updateBookingQueue();
        super.onBackPressed();
    }

    public void passInfoBack(View view) {
        onBackPressed();
    }
}
