package com.example.findmeuv.view.adapter;

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

import com.example.findmeuv.R;
import com.example.findmeuv.handler.AdapterHandler;
import com.example.findmeuv.model.pojo.PendingTrip;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view.activity.PassengerListActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookingPendingAdapter extends RecyclerView.Adapter<BookingPendingAdapter.ViewHolder> {

    private List<PendingTrip> pendingList;
    private Context context;
    private boolean pending;
    private ViewHelper dialog;

    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;
    private AdapterHandler adapterHandler;

    public BookingPendingAdapter(List<PendingTrip> list, Context context, AdapterHandler adapterHandler, boolean pending) {
        this.pendingList = list;
        this.context = context;
        this.dialog = new ViewHelper(context);
        this.adapterHandler = adapterHandler;
        this.pending = pending;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pending_book, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final PendingTrip list = pendingList.get(position);
        holder.status.setText(list.getStatus());
        holder.date.setText(list.getDate());
        holder.from.setText(list.getFrom());
        holder.to.setText(list.getTo());
        holder.company.setText(list.getCompany());
        holder.depart.setText(list.getDepart());
        holder.arrive.setText(list.getArrive());
        holder.fare.setText("₱" + String.format("%.2f", Float.parseFloat(list.getFare())));
        holder.amount.setText("₱" + String.format("%.2f", Float.parseFloat(list.getAmount())));
        holder.btnPass.setText("Passenger Details");

        holder.btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PassengerListActivity.class);
                intent.putExtra("book_id", list.getBookId());
                intent.putExtra("trip_id", list.getTripId());
                intent.putExtra("pending", pending);
                context.startActivity(intent);
            }
        });

        if (!pending) {
            holder.btnCancel.setVisibility(View.GONE);
            holder.status.setVisibility(View.GONE);
        }

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                builder.setTitle("Cancel Reservation")
                        .setMessage("Are you sure you want to cancel this reservation?")
                        .setCancelable(false)
                        .setNegativeButton("No", null)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, String> data = new HashMap<>();
                                data.put("book_id", list.getBookId());
                                data.put("position", String.valueOf(position));
                                adapterHandler.doAction(data);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return pendingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView status, date, from, to, company, depart, arrive, fare, amount;
        Button btnPass, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
             status = itemView.findViewById(R.id.status);
             date = itemView.findViewById(R.id.date);
             from = itemView.findViewById(R.id.from);
             to = itemView.findViewById(R.id.to);
             company = itemView.findViewById(R.id.company);
             depart = itemView.findViewById(R.id.depart);
             arrive = itemView.findViewById(R.id.arrive);
             fare = itemView.findViewById(R.id.fare);
             amount = itemView.findViewById(R.id.amount);

             btnPass = itemView.findViewById(R.id.btnPass);
             btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
