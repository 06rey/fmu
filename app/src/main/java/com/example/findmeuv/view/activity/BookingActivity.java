package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.model.pojo.TripItinerary;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BookingActivity extends AppCompatActivity {

    private int no_of_pass;
    private int no_of_selected_seat = 0;

    ViewHelper dialog;
    private TextView selectSeatErr;
    // Hold textView object
    private Map<String, TextView> seatArr;
    // Hold textView object click state
    private SparseBooleanArray isSeatSelectedArr;
    private LinearLayout progressBar;
    private LinearLayout noInternet;
    private Button btnSelectErr, btnProceed, btnCancel;
    private TextView txtAvailableSeat;
    private TextView txtSelectedSeat;

    private TextView txtSelectingSeat;

    private int occupied_seat = 0;
    AlertDialog alert;

    private ViewHelper viewHelper;
    private AlertDialog.Builder builder;

    private AppViewModel viewModel;
    private TripItinerary tripItinerary;

    Timer choosingSeatTimer = new Timer();
    private int noOfPeopleChoosingSeat = 0;
    private String dot = ".";
    private int dotCount = 0;

    private boolean serverEventIsRunning = false;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        initialize();
        setClickListener();
        this.setViewModelObserver();

    }

    private void initialize() {
        // Init
        Intent prevIntent = getIntent();

        tripItinerary = new TripItinerary();
        tripItinerary = (TripItinerary) prevIntent.getSerializableExtra("selected_trip");

        dialog = new ViewHelper(this);
        viewHelper = new ViewHelper(this);
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

        progressBar = findViewById(R.id.selectSeatDialog);
        progressBar.setVisibility(View.VISIBLE);
        noInternet = findViewById(R.id.selectSeatNoInternet);
        selectSeatErr = findViewById(R.id.selectSeatErr);
        btnSelectErr = findViewById(R.id.btnSelectSeatErr);
        btnProceed = findViewById(R.id.btnProceed);
        btnCancel = findViewById(R.id.btnCancel);

        txtSelectingSeat = findViewById(R.id.txtSelectingSeat);

        txtAvailableSeat = findViewById(R.id.txtAvailableSeat);
        TextView txtNoOfPassenger = findViewById(R.id.txtNoOfPass);
        txtSelectedSeat = findViewById(R.id.txtSelectedSeat);

        no_of_pass = Integer.parseInt(tripItinerary.getNoOfPass());

        if (tripItinerary.getBookMode().equals("pending")) {
            TextView txtFare = findViewById(R.id.txtFare);
            TextView txtTotalFare = findViewById(R.id.txtTotalFare);
            float totalFare = Float.parseFloat(tripItinerary.getFare()) * no_of_pass;
            txtFare.setText("₱" + String.format("%.2f", Float.parseFloat(tripItinerary.getFare())));
            txtTotalFare.setText("₱" + String.format("%.2f", totalFare));
        } else {
            LinearLayout fareLayout = findViewById(R.id.fareLayout);
            fareLayout.setVisibility(View.GONE);
        }

        txtAvailableSeat.setText(tripItinerary.getAvailableSeat());
        txtNoOfPassenger.setText(tripItinerary.getNoOfPass());
        txtSelectedSeat.setText(String.valueOf(no_of_selected_seat));

        seatArr = new HashMap<>();
        isSeatSelectedArr = new SparseBooleanArray();

        seatArr.put("1", (TextView)findViewById(R.id.txtSeat1));
        seatArr.put("2", (TextView)findViewById(R.id.txtSeat2));
        seatArr.put("3", (TextView)findViewById(R.id.txtSeat3));
        seatArr.put("4", (TextView)findViewById(R.id.txtSeat4));
        seatArr.put("5", (TextView)findViewById(R.id.txtSeat5));
        seatArr.put("6", (TextView)findViewById(R.id.txtSeat6));
        seatArr.put("7", (TextView)findViewById(R.id.txtSeat7));
        seatArr.put("8", (TextView)findViewById(R.id.txtSeat8));
        seatArr.put("9", (TextView)findViewById(R.id.txtSeat9));
        seatArr.put("10", (TextView)findViewById(R.id.txtSeat10));
        seatArr.put("11", (TextView)findViewById(R.id.txtSeat11));
        seatArr.put("12", (TextView)findViewById(R.id.txtSeat12));
        seatArr.put("13", (TextView)findViewById(R.id.txtSeat13));
        seatArr.put("14", (TextView)findViewById(R.id.txtSeat14));



        for (int i=1; i<=14; i++) {
            isSeatSelectedArr.put(i, false);
        }
    }
    int tempSeat = 0;
    private void setClickListener() {
        for (int a = 1; a <= seatArr.size(); a++) {

            final int key = a;
            final String strKey = String.valueOf(a);
            seatArr.get(strKey).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    tempSeat = key;
                    if (!isSeatSelectedArr.get(key)) {
                        if (no_of_selected_seat < no_of_pass) {
                            seatArr.get(strKey).setBackgroundColor(getResources().getColor(R.color.light_green));
                            no_of_selected_seat++;
                            isSeatSelectedArr.put(key, true);
                            // Add selected seat
                            addSelectedSeat(Integer.parseInt(seatArr.get(strKey).getText().toString()));
                        } else {
                            dialog.alertDialog("System Message", "You already selected " + String.valueOf(no_of_selected_seat) + " seat for " + String.valueOf(no_of_pass) + " passenger.");
                        }
                    } else {
                        seatArr.get(strKey).setBackgroundColor(getResources().getColor(R.color.lineColor));
                        no_of_selected_seat--;
                        isSeatSelectedArr.put(key, false);
                        //delete unselected seat
                        deleteSelectedSeat(Integer.parseInt(seatArr.get(strKey).getText().toString()));
                    }
                    txtSelectedSeat.setText(String.valueOf(no_of_selected_seat));
                }
            });

        }

        // btn click listener
        // No internet button
        btnSelectErr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (no_of_selected_seat == no_of_pass) {
                    for (int x = 1; x<= isSeatSelectedArr.size(); x++) {
                        if (isSeatSelectedArr.get(x)) {
                            tripItinerary.addSeat(String.valueOf(x));
                        }
                    }

                    intent = new Intent(BookingActivity.this, PassengerInfoActivity.class);
                    intent.putExtra("tripItinerary", tripItinerary);

                    updateBookingQueue();

                } else {
                    String seat = "seat", pass = "passenger.";
                    if (no_of_pass > 1) {
                        seat = "seats";
                        pass = "passengers.";
                    }
                    dialog.alertDialog("System Message", "Please select "+ String.valueOf(no_of_pass) + " "+ seat +" for "+ String.valueOf(no_of_pass) +" " + pass);
                }
            }
        });

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
                                viewHelper.showLoading();
                                deleteBookingQueue();
                                if (tripItinerary.getBookMode().equals("update")) {
                                    onBackPressed();
                                } else {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(BookingActivity.this, FmuHomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            finish();
                                        }
                                    }, 500);
                                }
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });
    }

    private Map<String, String> setRequestAttr() {
        Map<String, String> attr = new HashMap<>();

        attr.put("resp", "1");
        attr.put("main", "booking");
        attr.put("sub", "get_seat");
        attr.put("trip_id", tripItinerary.getTripID());
        attr.put("queue_id", tripItinerary.getQueueId());

        return attr;
    }

    private void updateSeatingStatus(Map<String, String> seat, int choosingSeat) {
        occupied_seat = 0;
        int size = seatArr.size();
        for (int a=1; a<size; a++) {
            String str = String.valueOf(a);
            if (seat.containsValue(str)) {
                seatArr.get(str).setBackgroundColor(getResources().getColor(R.color.red));
                seatArr.get(str).setEnabled(false);
                occupied_seat++;
            } else {
                if (!isSeatSelectedArr.get(a)) {
                    seatArr.get(str).setBackgroundColor(getResources().getColor(R.color.lineColor));
                    seatArr.get(str).setEnabled(true);
                }
            }
        }

        updateAvailableSeatText();

        if (choosingSeat > 0) {
            if (noOfPeopleChoosingSeat < 1) {
                showChoosingSeat();
            }
            noOfPeopleChoosingSeat = choosingSeat;
        } else {
            if (noOfPeopleChoosingSeat > 0) {
                choosingSeatTimer.cancel();
                dot = " .";
                dotCount = 0;
                txtSelectingSeat.setVisibility(View.INVISIBLE);
            }
            noOfPeopleChoosingSeat = choosingSeat;
        }
    }

    private void updateAvailableSeatText() {
        final int MAX_SEAT = 14 - no_of_selected_seat;
        txtAvailableSeat.setText(String.valueOf(MAX_SEAT - occupied_seat));
        txtSelectedSeat.setText(String.valueOf(no_of_selected_seat));
    }

    private void showChoosingSeat() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (noOfPeopleChoosingSeat > 0) {
                            String str;
                            if (noOfPeopleChoosingSeat > 1) {
                                str = String.valueOf(noOfPeopleChoosingSeat) + " other persons are selecting seat" + dot;
                            } else {
                                str = String.valueOf(noOfPeopleChoosingSeat) + " other person is selecting seat" + dot;
                            }
                            dot = dot + ".";
                            dotCount++;
                            if (dotCount == 7) {
                                dotCount = 0;
                                dot = ".";
                            }
                            txtSelectingSeat.setText(str);
                            txtSelectingSeat.setVisibility(View.VISIBLE);
                            showChoosingSeat();
                        }
                    }
                });
            }
        };
        choosingSeatTimer.schedule(task, 200);
    }

    private void deleteBookingQueue() {
        Map<String, String> deleteQueue = setRequestAttr();
        deleteQueue.put("resp", "1");
        deleteQueue.put("main", "booking");
        deleteQueue.put("sub", "delete_queue");
        deleteQueue.put("trip_id", tripItinerary.getTripID());
        deleteQueue.put("queue_id", tripItinerary.getQueueId());

        viewModel.okHttpRequest(deleteQueue, "GET", "");
    }

    private void updateBookingQueue() {
        Map<String, String> updateQueue = setRequestAttr();
        updateQueue.put("resp", "1");
        updateQueue.put("main", "booking");
        updateQueue.put("sub", "update_booking_queue");
        updateQueue.put("trip_id", tripItinerary.getTripID());
        updateQueue.put("queue_id", tripItinerary.getQueueId());
        updateQueue.put("status", "passenger_info");

        viewModel.okHttpRequest(updateQueue, "GET", "");
    }

    private void deleteSelectedSeat(int seatNo) {
        Map<String, String> delete = setRequestAttr();
        delete.put("resp", "1");
        delete.put("main", "booking");
        delete.put("sub", "delete_seat");
        delete.put("trip_id", tripItinerary.getTripID());
        delete.put("queue_id", tripItinerary.getQueueId());
        delete.put("seat_no", String.valueOf(seatNo));

        viewModel.okHttpRequest(delete, "GET", "");
    }

    private void addSelectedSeat(int seatNo) {
        Map<String, String> add = setRequestAttr();
        add.put("resp", "1");
        add.put("main", "booking");
        add.put("sub", "add_seat");
        add.put("trip_id", tripItinerary.getTripID());
        add.put("queue_id", tripItinerary.getQueueId());
        add.put("seat_no", String.valueOf(seatNo));

        viewModel.okHttpRequest(add, "GET", "");
    }

    private void setViewModelObserver() {
        // SSE success observer
        viewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                updateSeatingStatus(list.get(0), Integer.parseInt(list.get(1).get("no")));
                serverEventIsRunning = true;
            }
        });

        viewModel.getServerSentInitData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                updateSeatingStatus(list.get(0), Integer.parseInt(list.get(1).get("no")));
                progressBar.setVisibility(View.GONE);
                viewModel.serverSentEvent(setRequestAttr(), "");
            }
        });
        // SSE error
        viewModel.getSseConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                serverEventIsRunning = false;
            }
        });

        viewModel.getSseServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                selectSeatErr.setText("This service in not available as of the moment. Please try again later.");
                serverEventIsRunning = false;
            }
        });

        viewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                selectSeatErr.setText("Sorry. Something went wrong there.");
                serverEventIsRunning = false;
            }
        });

        viewModel.getSseInitDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                selectSeatErr.setText("Sorry. Something went wrong there.");
                serverEventIsRunning = false;
            }
        });

        viewModel.getSseInitJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                noInternet.setVisibility(View.VISIBLE);
                selectSeatErr.setText("Sorry. Something went wrong there.");
                serverEventIsRunning = false;
            }
        });
        // OKHTTP observer
        // Success observer
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                if (list.get(0).get("status").equals("failed") && list.get(0).get("type").equals("add_seat")) {
                    dialog.alertDialog("System Message", "Sorry. This seat has been reserved to other passenger just now. Please select another seat");
                    seatArr.get(String.valueOf(tempSeat)).setBackgroundColor(getResources().getColor(R.color.red));
                    seatArr.get(String.valueOf(tempSeat)).setEnabled(false);
                    isSeatSelectedArr.put(tempSeat, false);
                    no_of_selected_seat--;
                    updateAvailableSeatText();
                } else if (list.get(0).get("status").equals("success") && list.get(0).get("type").equals("update_queue")) {
                    startActivity(intent);
                }
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.alertDialog("System Message", "Internet connection problem. Please check your device network setting.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.alertDialog("System Message", "Sorry, This service is not available at the moment. Try again later.");
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.alertDialog("System Message", "Sorry, Something went wrong there.");
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
                dialog.alertDialog("System Message", "Sorry, Something went wrong there.");
            }
        });
    }

    @Override
    public void onBackPressed() {
        deleteBookingQueue();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (serverEventIsRunning) {
            viewModel.closeServerEventConnection();
        }
        getViewModelStore().clear();
        serverEventIsRunning = false;
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        viewModel.serverSentInitRequest(setRequestAttr(), "GET", "");
        super.onPostResume();
    }
    public void backClick(View view) {
        onBackPressed();
    }
}


