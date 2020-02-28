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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.model.pojo.TripItinerary;
import com.example.findmeuv.view_model.AppViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripItineraryActivity extends AppCompatActivity {

    private TextView txtDate, txtDepart, txtArrive, txtOrigin, txtDest, txtVia, txtFare, txtNoPass, txtTotal, txtOperator;
    private Button btnCancel, btnSave;
    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;

    private AlertDialog alert;
    private ViewHelper viewHelper;
    private AlertDialog.Builder builder;
    private TripItinerary tripItinerary;
    private AppViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_itenerary);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);

        initViews();
        setValues();
        setBtnSaveCLick();
        setBtnCancelCLick();

        setViewModelObserver();
    }

    private void initViews() {
        txtDate = findViewById(R.id.txtDate);
        txtDepart = findViewById(R.id.txtDepart);
        txtArrive = findViewById(R.id.txtArrive);
        txtOrigin = findViewById(R.id.txtOrigin);
        txtDest = findViewById(R.id.txtDest);
        txtVia = findViewById(R.id.txtVia);
        txtFare = findViewById(R.id.txtFare);
        txtNoPass = findViewById(R.id.txtNoPass);
        txtTotal = findViewById(R.id.txtTotalFare);
        txtOperator = findViewById(R.id.txtOperator);

        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        viewHelper = new ViewHelper(this);
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

        fmuUserPref = getSharedPreferences("fmuPref", 0);
        editor = fmuUserPref.edit();

        Intent intent = getIntent();
        tripItinerary = (TripItinerary) intent.getSerializableExtra("tripItinerary");
    }

    private void postData() {

        Map<String, String> postData = new HashMap<>();
        int size = tripItinerary.getSelectedSeatSize();

        postData.put("resp", "1");
        postData.put("main", "booking");
        postData.put("sub", "save_booking");
        postData.put("no_of_passenger", String.valueOf(size));
        postData.put("amount", tripItinerary.getTotalFare());
        postData.put("pass_type", "terminal");
        postData.put("trip_id", tripItinerary.getTripID());
        postData.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
        postData.put("notes", tripItinerary.getNote());
        postData.put("boarding_point", tripItinerary.getBoardingPoint());
        postData.put("device_id", tripItinerary.getDeviceId());
        if (tripItinerary.getIsImAlso()) {
            postData.put("im_a_passenger", "true");
        } else {
            postData.put("im_a_passenger", "false");
        }

        if (tripItinerary.getBoardingPoint().equals("Pick_up")){
            postData.put("locLat", tripItinerary.getLocLat());
            postData.put("locLng", tripItinerary.getLocLng());
        }
        postData.put("queue_id", tripItinerary.getQueueId());

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
            Log.d("DebugLog", "TripItenerary->postData: " + e.getMessage());
        }
        Log.d("DebugLog", "TripItenerary->postData VAL:: " + postData.toString());
        viewModel.okHttpRequest(postData, "POST", "");
    }

    private void setBtnSaveCLick() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHelper.showLoading();
                postData();
            }
        });
    }

    private void setBtnCancelCLick() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                deleteBookingQueue();
                                viewHelper.showLoading();
                                Intent intent = new Intent(TripItineraryActivity.this, FmuHomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });
    }

    private void setValues() {

        txtDate.setText(tripItinerary.getTripDate());
        txtDepart.setText(tripItinerary.getDepartTime());
        txtArrive.setText(tripItinerary.getArriveTime());
        txtOperator.setText(tripItinerary.getTransportService());
        txtOrigin.setText(tripItinerary.getOrigin());
        txtDest.setText(tripItinerary.getDestination());
        txtVia.setText(tripItinerary.getVia());
        String s = "₱" + String.format("%.2f", Float.parseFloat(tripItinerary.getFare()));
        txtFare.setText(s);
        txtNoPass.setText(tripItinerary.getNoOfPass());
        float amount = Float.parseFloat(tripItinerary.getFare()) * Integer.parseInt(tripItinerary.getNoOfPass());
        s = "₱" + String.format("%.2f", amount);
        tripItinerary.setTotalFare(String.valueOf(amount));
        txtTotal.setText(s);
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

    public void backClick(View view) {
        onBackPressed();
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                viewHelper.dismissLoading();
                String message = "Your seat reservation has been successfully saved. Thank you.";
                builder.setTitle("System Message")
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(TripItineraryActivity.this, FmuHomeActivity.class);
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
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Internet connection problem. Please check your device network setting.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "This service is not available at the moment. Try again later.");
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Sorry. Something went wrong there.");
            }
        });
    }
}
