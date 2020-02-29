package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.EventHandler;
import com.example.findmeuv.model.pojo.Passenger;
import com.example.findmeuv.model.pojo.TripItinerary;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view.adapter.PassengerListAdapter;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class PassengerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private ProgressBar progressBar;

    private String bookId, tripId;
    private List<Passenger> passengerList = new ArrayList<>();
    private Context context;

    private EventHandler eventHandler = new EventHandler();
    private TripItinerary tripItinerary = new TripItinerary();
    private String mode = "none", noOfPass;
    private boolean pendingStat;
    int vacantSeat, position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_list);

        this.viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        this.viewModel.initialize(this);

        this.initialize();
        this.setViewModelObserver();
        this.getPassenger();
    }

    private void initialize() {
        recyclerView = findViewById(R.id.passList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewHelper = new ViewHelper(this);
        progressBar = findViewById(R.id.progressBar);

        bookId =  getIntent().getStringExtra("book_id");
        tripId =  getIntent().getStringExtra("trip_id");
        pendingStat = getIntent().getBooleanExtra("pending", false);
        context = this;

        eventHandler.button.setClickListenerWithParam(new EventHandler.ClickListenerWithParam() {
            @Override
            public void onButtonClick(Map<String, String> data) {
                viewHelper.showProgressBar("Please wait ...");
                mode = "remove";
                position = Integer.parseInt( data.get("position"));
                viewModel.okHttpRequest(data, "GET", "");
            }
        });

        if (!pendingStat) {
            LinearLayout linearLayout = findViewById(R.id.addPassBtn);
            LinearLayout mapLayout = findViewById(R.id.mapLayout);
            linearLayout.setVisibility(GONE);
            mapLayout.setVisibility(GONE);
        }
    }

    public void onClickBtnAdd(View view) {
        confirmAddingPassenger();
    }

    private void confirmAddingPassenger() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        builder.setTitle("Update Reservation")
                .setMessage("Are you sure want to reserve more seat?")
                .setCancelable(false)
                .setNegativeButton("No", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        viewHelper.showProgressBar("Please wait ...");
                        mode = "add_passenger";
                        Map<String, String> data = new HashMap<>();

                        data.put("resp", "1");
                        data.put("main", "booking");
                        data.put("sub", "get_trip_info");
                        data.put("trip_id", tripId);
                        viewModel.okHttpRequest(data, "GET", "");

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setNoOfPassenger(final int vacant_seat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.get_no_of_passenger, null);
        final TextView txtNoPass = view.findViewById(R.id.txtNoOfPass);
        final TextView txtVacantSeat = view.findViewById(R.id.txtVacantSeatDialog);
        txtVacantSeat.setText(String.valueOf(vacant_seat));
        builder.setTitle("Enter number of passenger")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int no_of_pass = 0;
                        if (!txtNoPass.getText().toString().equals("")) {
                            no_of_pass = Integer.parseInt(txtNoPass.getText().toString());
                        }
                        if (no_of_pass <= vacant_seat && no_of_pass != 0) {
                            noOfPass = String.valueOf(no_of_pass);
                            enqueueBooking(no_of_pass);
                        } else {
                            viewHelper.alertDialog("System Message" , "Please enter a number not less than zero or not greater than number of available seat.");
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadBookingActivity(int vacant_seat, int no_of_pass) {
        tripItinerary.setAvailableSeat(String.valueOf(vacant_seat));
        tripItinerary.setNoOfPass(String.valueOf(no_of_pass));
        tripItinerary.setBookMode("update");
        tripItinerary.setTripID(tripId);
        tripItinerary.setBookId(bookId);

        Intent intent = new Intent(PassengerListActivity.this, BookingActivity.class);
        intent.putExtra("selected_trip", tripItinerary);
        startActivity(intent);
    }
    // Method for web service request
    private void enqueueBooking(int noOfPass) {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "enqueue_booking");
        data.put("event", "normal");
        data.put("trip_id", tripId);
        data.put("no_of_pass", String.valueOf(noOfPass));
        viewModel.okHttpRequest(data, "GET", "");
    }

    private void getPassenger() {
        Map<String, String> data = new HashMap<>();

        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "get_passenger_list");
        data.put("book_id", bookId);

        viewModel.okHttpRequest(data, "GET", "");
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {

                String type = list.get(0).get("type");

                switch (type) {
                    case "passenger_list":
                        for (Map<String, String> data: list) {
                            Passenger passenger = new Passenger();
                            passenger.setBookId(data.get("booking_id"));
                            passenger.setName(data.get("full_name"));
                            passenger.setBoardPass(data.get("boarding_pass"));
                            passenger.setSeatNo(data.get("seat_no"));
                            passenger.setSeatId(data.get("seat_id"));
                            passenger.setStatus(data.get("boarding_status"));
                            passengerList.add(passenger);
                        }
                        adapter = new PassengerListAdapter(viewHelper, passengerList, context, eventHandler, pendingStat);
                        recyclerView.setAdapter(adapter);
                        break;
                    case "remove_passenger":
                        viewHelper.dismissProgressBar();

                        String status = list.get(0).get("status");

                        if (status.equals("success")) {
                            passengerList.remove(position);
                            adapter.notifyItemRemoved(position);
                            if (passengerList.size() == 1) {
                                recyclerView.findViewHolderForAdapterPosition(0).itemView.findViewById(R.id.btnRemove).setVisibility(GONE);
                            }
                        }
                        eventHandler.response(list);
                        break;
                    case "trip_info":
                        int vacant_seat = Integer.parseInt(list.get(0).get("vacant_seat"));

                        if (vacant_seat < 14 && vacant_seat > 0) {
                            tripItinerary.setAvailableSeat(list.get(0).get("vacant_seat"));
                            tripItinerary.setBookMode("update");
                            tripItinerary.setNoOfPass("1");
                            tripItinerary.setTripID(tripId);
                            vacantSeat = vacant_seat;
                            setNoOfPassenger(vacant_seat);
                        } else {
                            viewHelper.alertDialog("Cannot Add Passenger","Sorry! This trip is currently fully booked.");
                        }
                        break;
                    case "enqueue":
                        String status2 = list.get(0).get("status");
                        if (status2.equals("success")) {
                            tripItinerary.setQueueId(list.get(0).get("id"));
                            loadBookingActivity(vacantSeat, Integer.parseInt(noOfPass));
                        } else {
                            viewHelper.alertDialog("System Message", "Sorry. Some available seat of this trip has been reserved just now and cannot accommodate " + noOfPass + " passenger(s).");
                        }
                        break;
                }
                // Dismiss loading bar
                dismissLoading();
            }
        });
        // ERROR OBSERVER
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();            }
        });
        viewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
            }
        });
        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
            }
        });
    }

    private void dismissLoading() {
        if (mode.equals("none")) {
            progressBar.setVisibility(GONE);
        } else {
            viewHelper.dismissProgressBar();
        }
        mode = "none";
    }

    public void onClickBtnMap(View view) {
        Intent intent = new Intent(PassengerListActivity.this, UvExpressMapActivity.class);
        intent.putExtra("tripId", tripId);
        intent.putExtra("bookId", bookId);
        startActivity(intent);
    }
}
