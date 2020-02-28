package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BookTravelingActivity extends AppCompatActivity {
    private String trip_id, available_seat, fare;
    private int no_of_pass, no_of_selected_seat = 0;
    private float totalFare;
    ViewHelper dialog;
    private TextView selectSeatErr, txt_seat1, txt_seat2, txt_seat3, txt_seat4, txt_seat5, txt_seat6, txt_seat7, txt_seat8, txt_seat9, txt_seat10, txt_seat11, txt_seat12, txt_seat13, txt_seat14;
    // Hold textview object
    private Map<String, TextView> seat_map;
    // Hold textview object click state
    private Map<String, Boolean> seat_map_selected;
    private ProgressBar selectSeatProgress;
    private LinearLayout noInternet;
    private ScrollView selectSeatScroll;
    private Button btnSelectErr, btnProceed, btnCancel;
    private TextView txtavailableSeat, txtNoOfPassenger, txtSelectedSeat, txtFare, txtTotalFare;
    RequestQueue MyRequestQueue;

    //Http Request

    private String url;
    private OkHttpClient client;
    private okhttp3.Request request;
    final private long delay = 500;
    public Boolean bool = false;
    private Boolean stop = false, timeout = true;
    int i = 0;

    int queue_id = 0;

    private TextView txtSelectingSeat;

    private int occupied_seat = 0;
    AlertDialog alert;

    SharedPreferences fmuUserPref;
    SharedPreferences.Editor editor;

    private int selectState = 0;
    private  Boolean isPause = false;

    private ViewHelper viewHelper;
    private AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_traveling);

        // Init
        dialog = new ViewHelper(this);
        viewHelper = new ViewHelper(this);
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        Intent bookingIntent = getIntent();
        MyRequestQueue = Volley.newRequestQueue(this);
        selectSeatProgress = (ProgressBar)findViewById(R.id.selectSeatDialog);
        noInternet = (LinearLayout) findViewById(R.id.selectSeatNoInternet);
        selectSeatScroll = (ScrollView)findViewById(R.id.selectSeatScroll);
        selectSeatErr = (TextView)findViewById(R.id.selectSeatErr);
        btnSelectErr = (Button)findViewById(R.id.btnSelectSeatErr);
        btnProceed = (Button)findViewById(R.id.btnProceed);
        btnCancel = (Button)findViewById(R.id.btnCancel);

        txtSelectingSeat = (TextView)findViewById(R.id.txtSelectingSeat);

        txtavailableSeat = (TextView) findViewById(R.id.txtAvailableSeat);
        txtNoOfPassenger = (TextView) findViewById(R.id.txtNoOfPass);
        txtSelectedSeat = (TextView) findViewById(R.id.txtSelectedSeat);

        trip_id = bookingIntent.getStringExtra("trip_id");
        no_of_pass = Integer.parseInt(bookingIntent.getStringExtra("no_of_pass"));
        available_seat = bookingIntent.getStringExtra("available_seat");

        txtavailableSeat.setText(available_seat);
        txtNoOfPassenger.setText(String.valueOf(no_of_pass));
        txtSelectedSeat.setText(String.valueOf(no_of_selected_seat));


        selectSeatScroll.setVisibility(View.GONE);

        seat_map = new HashMap<>();
        seat_map_selected = new HashMap<>();

        seat_map.put("1", (TextView)findViewById(R.id.txtSeat1));
        seat_map.put("2", (TextView)findViewById(R.id.txtSeat2));
        seat_map.put("3", (TextView)findViewById(R.id.txtSeat3));
        seat_map.put("4", (TextView)findViewById(R.id.txtSeat4));
        seat_map.put("5", (TextView)findViewById(R.id.txtSeat5));
        seat_map.put("6", (TextView)findViewById(R.id.txtSeat6));
        seat_map.put("7", (TextView)findViewById(R.id.txtSeat7));
        seat_map.put("8", (TextView)findViewById(R.id.txtSeat8));
        seat_map.put("9", (TextView)findViewById(R.id.txtSeat9));
        seat_map.put("10", (TextView)findViewById(R.id.txtSeat10));
        seat_map.put("11", (TextView)findViewById(R.id.txtSeat11));
        seat_map.put("12", (TextView)findViewById(R.id.txtSeat12));
        seat_map.put("13", (TextView)findViewById(R.id.txtSeat13));
        seat_map.put("14", (TextView)findViewById(R.id.txtSeat14));



        for (int i=1; i<=14; i++) {
            seat_map_selected.put(String.valueOf(i), false);
        }

        // Seat button onclick listener

        for (int a=1; a <= seat_map.size(); a++) {

            final TextView txt = seat_map.get(String.valueOf(a));
            final int id =a;
            seat_map.get(String.valueOf(a)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (seat_map_selected.get(String.valueOf(id)) == false) {

                        if (no_of_selected_seat < no_of_pass) {
                            seat_map.get(String.valueOf(id)).setBackgroundColor(getResources().getColor(R.color.light_green));
                            no_of_selected_seat++;
                            seat_map_selected.put(String.valueOf(id), true);
                            // Add selected seat
                            addDeleteSelectedSeat("select", seat_map.get(String.valueOf(id)).getText().toString());
                        } else {
                            dialog.alertDialog("System Message", "You already selected " + String.valueOf(no_of_selected_seat) + " seat for " + String.valueOf(no_of_pass) + " passenger.");
                        }
                    } else {
                        seat_map.get(String.valueOf(id)).setBackgroundColor(getResources().getColor(R.color.lineColor));
                        no_of_selected_seat--;
                        seat_map_selected.put(String.valueOf(id), false);
                        //delete unselected seat
                        addDeleteSelectedSeat("del", seat_map.get(String.valueOf(id)).getText().toString());
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
                    fmuUserPref = getSharedPreferences("fmuPref", 0); // 0 - for private mode = accessible la within the app, dire ha bug os na device
                    editor = fmuUserPref.edit();
                    editor.putString("total_fare", String.valueOf(totalFare));
                    editor.putString("queue_id", String.valueOf(queue_id));
                    int a = 1;
                    for (int x=1; x<=seat_map_selected.size(); x++) {
                        if (seat_map_selected.get(String.valueOf(x))) {
                            editor.putInt("seat" + String.valueOf(a), x);
                            a++;
                        } else {
                            editor.remove("seat"+x);
                        }
                    }
                    editor.putString("no_of_passenger", String.valueOf(no_of_pass));
                    editor.putString("boarding_point", "pick-up");
                    editor.commit();

                    url = "http://"+ getResources().getString(R.string.base_url) +"/fmu/user/passenger/delete_booking_queue.php?key=fmu_user&status=on_progress&id="+ queue_id;
                    OkHttpClient client3 = new OkHttpClient();
                    okhttp3.Request  request2 = new okhttp3.Request.Builder()
                            .url(url)
                            .header("Connection", "close")
                            .build();

                    client3.newCall(request2).enqueue(new Callback() {
                        @Override public void onFailure(Call call, IOException e) {
                            dialog.alertDialog("System Message", "Sorry, unable to proceed. Something went wrong.");
                            call.cancel();
                        }
                        @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), PassengerInfoActivity.class));
                                }
                            });
                            call.cancel();
                        }
                    });
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
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(BookTravelingActivity.this, FmuHomeActivity.class);
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
        });
        // End btn listener

        selectSeatScroll.setVisibility(View.GONE);

        bool = false;

        String bookUrl = "fmu/user/passenger/booking_queue.php?key=fmu_user&trip_id="+ trip_id +"&no_of_pass=" + no_of_pass;
        buildRequest(bookUrl, "close");
        sendRequest();

        showSelectingSeat();
    }

    private String dot = ".";
    private int dotCount = 0;

    public void showSelectingSeat() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (selectState > 0) {
                            String str;
                            if (selectState > 1) {
                                str = String.valueOf(selectState) + " other persons are selecting seat" + dot;
                            } else {
                                str = String.valueOf(selectState) + " other person is selecting seat" + dot;
                            }
                            dot = dot + ".";
                            dotCount++;
                            if (dotCount == 7) {
                                dotCount = 0;
                                dot = ".";
                            }
                            txtSelectingSeat.setText(str);
                            txtSelectingSeat.setVisibility(View.VISIBLE);
                        } else {
                            dot = " .";
                            dotCount = 0;
                            txtSelectingSeat.setVisibility(View.INVISIBLE);
                        }
                        showSelectingSeat();
                    }
                });
            }
        };
        timer.schedule(task, 200);
    }

    public void deleteBookingQueue() {
        url = "http://"+ getResources().getString(R.string.base_url) +"/fmu/user/passenger/delete_booking_queue.php?key=fmu_user&id="+ queue_id;
        OkHttpClient client2 = new OkHttpClient();
        okhttp3.Request  request2 = new okhttp3.Request.Builder()
                .url(url)
                .header("Connection", "close")
                .build();

        client2.newCall(request2).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                call.cancel();
            }
            @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                call.cancel();
            }
        });
    }

    public void addDeleteSelectedSeat(String ref, String seatNo) {
        String bookUrl = "fmu/user/passenger/select_seat.php?key=fmu_user&ref="+ ref +"&seat_no="+ seatNo +"&id="+ queue_id+ "&trip_id="+ trip_id;
        url = "http://"+ getResources().getString(R.string.base_url) +"/"+bookUrl;
        OkHttpClient client2 = new OkHttpClient();
        okhttp3.Request  request2 = new okhttp3.Request.Builder()
                .url(url)
                .header("Connection", "close")
                .build();

        client2.newCall(request2).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                call.cancel();
            }
            @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                call.cancel();
            }
        });
    }

    private void getTripSeat(String response) {
        selectSeatScroll.setVisibility(View.VISIBLE);
        selectSeatProgress.setVisibility(View.GONE);

        if (!response.contains("message") || response.contains("No result")) {

            if (!response.contains("No result")) {
                try {
                    occupied_seat = 0;

                    JSONArray jArray = new JSONArray(response);
                    JSONObject obj = jArray.getJSONObject(0);

                    selectState = obj.getInt("selecting_seat");

                    for (int a=1; a <= seat_map.size(); a++) {

                        for (int b = 1; b <= obj.length() - 1; b++) {
                            // Mark seat as reserved seat
                            // user selected seat will not be included in the result, kay ginfilter na daan ha query nga dire api it selected seat hine na user
                            if (seat_map.get(String.valueOf(a)).getText().toString().equals(obj.getString("seat" + String.valueOf(b))) ) {
                                seat_map.get(String.valueOf(a)).setBackgroundColor(getResources().getColor(R.color.red));
                                seat_map.get(String.valueOf(a)).setEnabled(false);
                                if (seat_map_selected.get(String.valueOf(a))){
                                    seat_map_selected.put(String.valueOf(a), false);
                                    no_of_selected_seat--;
                                }
                                occupied_seat++;
                                break;
                            } else {
                                // Mark seat as available
                                if (!seat_map_selected.get(String.valueOf(a))) {
                                    seat_map.get(String.valueOf(a)).setBackgroundColor(getResources().getColor(R.color.lineColor));
                                    seat_map.get(String.valueOf(a)).setEnabled(true);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    JSONexception(e);
                }
            } else {
                for (int a=1; a <= seat_map.size(); a++) {
                    // All seats are available
                    if (!seat_map_selected.get(String.valueOf(a))) {
                        seat_map.get(String.valueOf(a)).setBackgroundColor(getResources().getColor(R.color.lineColor));
                        seat_map.get(String.valueOf(a)).setEnabled(true);
                    }
                }
            }
            selectSeatScroll.setVisibility(View.VISIBLE);
        } else {
            serverError();
        }
        // Maximum seat capacity of van type UV
        final int MAX_SEAT = 14 - no_of_selected_seat;
        txtavailableSeat.setText(String.valueOf(MAX_SEAT - occupied_seat));
    }

    private void buildRequest(String str, String type) {
        url = "http://"+ getResources().getString(R.string.base_url) +"/"+str;
        client = new OkHttpClient();
        request = new okhttp3.Request.Builder()
                .url(url)
                .header("Connection", type)
                .build();

    }

    public void startSeatSync() {
        bool = true;
        String str = "fmu/user/passenger/get_occupied_seat.php?key=fmu_user&ref=occupied_seat&trip_id=" + trip_id + "&queue_id="+queue_id;
        buildRequest(str, "close");
        sendRequest();
    }

    public void sendRequest() {
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                if (!bool) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            volleyError();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), " No internet.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                if (bool) {
                    runTask(call);
                }
            }
            @Override public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String resp = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bool) {
                            getTripSeat(resp);
                        } else {
                            try {
                                queue_id = new JSONObject(resp).optInt("id");
                                startSeatSync();
                            } catch (JSONException e) {
                                e.printStackTrace();

                            }
                        }
                       // Toast.makeText(getApplicationContext(), String.valueOf(i), Toast.LENGTH_SHORT).show();
                    }
                });
                i++;
                if (bool) {
                    runTask(call);
                }
            }
        });
    }

    private void runTask(Call call) {
        call.cancel();
        if (!stop) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    sendRequest();
                }
            };
            Timer t = new Timer();
            t.schedule(task, delay);
        }
    }

    @Override
    public void onBackPressed() {
        deleteBookingQueue();
        super.onBackPressed();
    }

    public void JSONexception(Exception e) {
        dialog.alertDialog("System Message", "ERROR Exception: " + e.getMessage());
        selectSeatErr.setText("ERROR: " + e.getMessage());
        selectSeatProgress.setVisibility(View.GONE);
        selectSeatScroll.setVisibility(View.GONE);
        noInternet.setVisibility(View.VISIBLE);
    }

    public void serverError() {
        selectSeatErr.setText("Cannot connect to the server. Try again later.");
        selectSeatProgress.setVisibility(View.GONE);
        selectSeatScroll.setVisibility(View.GONE);
        noInternet.setVisibility(View.VISIBLE);
    }

    public void volleyError() {
        selectSeatProgress.setVisibility(View.GONE);
        selectSeatScroll.setVisibility(View.GONE);
        noInternet.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        stop = true;
        super.onStop();
    }

    @Override
    protected void onPause() {
        timeout = false;
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        if (isPause) {
            startSeatSync();
            showSelectingSeat();
            stop = false;
        }

        super.onPostResume();
    }

    public void backClick(View view) {
        onBackPressed();
    }
}
