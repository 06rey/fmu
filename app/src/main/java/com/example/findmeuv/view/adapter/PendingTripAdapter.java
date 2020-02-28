package com.example.findmeuv.view.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.findmeuv.handler.EventHandler;
import com.example.findmeuv.handler.EventListener;
import com.example.findmeuv.model.pojo.TripItinerary;
import com.example.findmeuv.view.activity.BookingActivity;
import com.example.findmeuv.R;
import com.example.findmeuv.model.pojo.Trip;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class PendingTripAdapter extends RecyclerView.Adapter<PendingTripAdapter.ViewHolder> {

    private List<Trip> uvList;
    private Context context;
    private ViewHelper dialog;
    private Activity activity;
    private EventHandler eventHandler;
    private Trip selectedTrip;

    SharedPreferences fmuUserPref;
    SharedPreferences.Editor editor;
    private AppViewModel appViewModel;


    public PendingTripAdapter(List<Trip> list, Context context, Activity activity, AppViewModel serverSentViewModel, EventHandler eventHandler) {
        this.uvList = list;
        this.context = context;
        this.dialog = new ViewHelper(context);
        this.activity = activity;
        this.appViewModel = serverSentViewModel;
        this.eventHandler = eventHandler;
        fmuUserPref = activity.getSharedPreferences("fmuPref", 0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_trip_lay, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Trip list = uvList.get(position);

        holder.tripId.setText(list.getTripID());
        holder.tripDate.setText(list.getTripDate());
        holder.transService.setText(list.getTransService());
        holder.departTime.setText(list.getDepartTime());
        holder.arriveTime.setText(list.getArriveTime());
        holder.vacantSeat.setText(list.getVacantSeat());
        holder.fare.setText("₱" + String.format("%.2f", Float.parseFloat(list.getFare())));
        holder.tripPanel.setTag("pnl" + String.valueOf(position));
        holder.bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventHandler.button.doClick();
                selectedTrip = list;
                Map<String, String> data = new HashMap<>();
                data.put("resp", "1");
                data.put("main", "booking");
                data.put("sub", "check_booking_record");
                data.put("event", "normal");
                data.put("type", "booking");
                data.put("status", "Pending");
                data.put("trip_id", list.getTripID());
                data.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
                appViewModel.okHttpRequest(data, "GET", "");

                eventHandler.setEventListener(new EventListener() {
                    @Override
                    public void onResponseReady(List<Map<String, String>> list) {
                        if (list.get(0).get("status").equals("true")) {
                            dialog.alertDialog("System Message", "You already have booking for this trip. If you wish to add more passenger to this trip, go to booking and update passenger list.");
                        } else {
                            getNoOfPassenger(selectedTrip);
                        }
                    }
                });
            }
        });
    }

    private void checkAvailableSeat(final Trip list, final int no_of_pass) {
        eventHandler.button.doClick();
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "booking");
        data.put("sub", "enqueue_booking");
        data.put("event", "normal");
        data.put("trip_id", list.getTripID());
        data.put("no_of_pass", String.valueOf(no_of_pass));
        appViewModel.okHttpRequest(data, "GET", "");

        eventHandler.setEventListener(new EventListener() {
            @Override
            public void onResponseReady(List<Map<String, String>> response) {
                if (response.get(0).get("type").equals("enqueue")) {
                    if (response.get(0).get("status").equals("success")) {

                        TripItinerary tripItinerary = new TripItinerary();
                        tripItinerary.setTripDate(list.getTripDate());
                        tripItinerary.setDepartTime(list.getDepartTime());
                        tripItinerary.setArriveTime(list.getArriveTime());
                        tripItinerary.setTripID(list.getTripID());
                        tripItinerary.setNoOfPass(String.valueOf(no_of_pass));
                        tripItinerary.setAvailableSeat(list.getVacantSeat());
                        tripItinerary.setFare(list.getFare());
                        tripItinerary.setBookMode("pending");
                        tripItinerary.setQueueId(String.valueOf(response.get(0).get("id")));
                        tripItinerary.setTransportService(list.getTransService());

                        Intent booking = new Intent(context, BookingActivity.class);
                        booking.putExtra("selected_trip", tripItinerary);
                        context.startActivity(booking);
                    } else {
                        dialog.alertDialog("System Message", "Sorry. Some available seat of this trip has been reserved just now and cannot accommodate " + String.valueOf(no_of_pass) + " passenger(s).");
                    }
                }
            }
        });
    }

    private void getNoOfPassenger(final Trip list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.get_no_of_passenger, null);
        final TextView txtNoPass = view.findViewById(R.id.txtNoOfPass);
        final TextView txtVacantSeat = view.findViewById(R.id.txtVacantSeatDialog);
        txtVacantSeat.setText(list.getVacantSeat());
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
                        if (no_of_pass <= Integer.parseInt(list.getVacantSeat()) && no_of_pass != 0) {

                            checkAvailableSeat(list, no_of_pass);

                        } else {
                            ViewHelper viewHelper = new ViewHelper(context);
                            viewHelper.alertDialog("System Message" , "Please enter a number not less than zero or not greater than number of available seat.");
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public int getItemCount() {
        return uvList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tripId;
        private TextView tripDate;
        private TextView transService;
        public TextView status;
        private TextView departTime;
        private TextView arriveTime;
        private TextView vacantSeat;
        public TextView fare;
        private Button bookNow;
        private CardView tripPanel;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            tripId = itemView.findViewById(R.id.tripId);
            tripDate = itemView.findViewById(R.id.date);
            transService = itemView.findViewById(R.id.company);
            departTime = itemView.findViewById(R.id.depart);
            arriveTime = itemView.findViewById(R.id.arrive);
            vacantSeat = itemView.findViewById(R.id.seat);
            fare = itemView.findViewById(R.id.fare);
            bookNow = itemView.findViewById(R.id.btnBookPending);
            tripPanel = itemView.findViewById(R.id.pendingPanel);
        }

    }

}
