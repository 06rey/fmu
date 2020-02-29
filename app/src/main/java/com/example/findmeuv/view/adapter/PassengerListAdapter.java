package com.example.findmeuv.view.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.EventHandler;
import com.example.findmeuv.handler.EventListener;
import com.example.findmeuv.model.pojo.Passenger;
import com.example.findmeuv.view.ViewHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PassengerListAdapter extends RecyclerView.Adapter<PassengerListAdapter.ViewHolder> {

    private List<Passenger> passengerList;
    private ViewHelper viewHelper;
    private Context context;

    private EventHandler eventHandler;
    private String name;
    private int size;
    private boolean pendingStat;

    public PassengerListAdapter(ViewHelper viewHelper, List<Passenger> passengerList, Context context, EventHandler eventHandler, boolean pending) {
        this.viewHelper = viewHelper;
        this.passengerList = passengerList;
        this.context = context;
        this.eventHandler = eventHandler;
        this.setHttpResponseObserver();
        this.size = passengerList.size();
        this.pendingStat = pending;
    }

    private void setHttpResponseObserver() {
        eventHandler.setEventListener(new EventListener() {
            @Override
            public void onResponseReady(List<Map<String, String>> list) {
                String type = list.get(0).get("type");
                switch (type) {
                    case "remove_passenger":
                        if (list.get(0).get("status").equals("success")) {
                            viewHelper.alertDialog("Success", "Seat reservation for " + name + " is successfully canceled.");
                        } else {
                            viewHelper.alertDialog("System Message", "Seat reservation for " + name + " cannot be cancel. Passenger is already on board.");
                        }
                        break;
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.passenger_list_textview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Passenger passenger = passengerList.get(position);
        holder.txtName.setText(passenger.getName());
        holder.txtNoSeat.setText("Seat No.: " + passenger.getSeatNo());
        holder.txtBoardPass.setText("Boarding Pass: " + passenger.getBoardPass());

        if (size < 2 && !passenger.getStatus().equals("on_board")) {
            holder.removeBtn.setVisibility(View.GONE);
        }

        if (!pendingStat) {
            holder.removeBtn.setVisibility(View.GONE);
        }

        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                builder.setTitle("Update Reservation")
                        .setMessage("Are you sure want to cancel seat reservation for " + passenger.getName() + "?")
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Map<String, String> data = new HashMap<>();
                                data.put("resp", "1");
                                data.put("main", "booking");
                                data.put("sub", "remove_passenger");
                                data.put("seat_id", passenger.getSeatId());
                                data.put("position", String.valueOf(position));
                                eventHandler.button.doClickWithParam(data);
                                name = passenger.getName();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        if (passenger.getStatus().equals("on_board")) {
            holder.removeBtn.setText("On board");
            holder.removeBtn.setEnabled(false);
            holder.removeBtn.setBackgroundColor(context.getResources().getColor(R.color.header));
            holder.removeBtn.setTextColor(context.getResources().getColor(R.color.green));
        }

    }

    @Override
    public int getItemCount() {
        return passengerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button removeBtn;
        private TextView txtNoSeat, txtName, txtBoardPass;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            removeBtn = itemView.findViewById(R.id.btnRemove);
            txtNoSeat = itemView.findViewById(R.id.txtSeatNo);
            txtBoardPass = itemView.findViewById(R.id.txtBoardPass);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
