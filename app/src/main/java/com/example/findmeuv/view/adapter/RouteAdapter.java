package com.example.findmeuv.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.activity.FindUvActivity;
import com.example.findmeuv.model.pojo.Route;
import com.example.findmeuv.view.ViewHelper;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

  private List<Route> tripRouteLists;

  private Context context;
  private ViewHelper dialog;

  private SharedPreferences fmuUserPref;
  private SharedPreferences.Editor editor;


  public RouteAdapter(List<Route> tripRouteLists, Context context) {

    dialog = new ViewHelper(context);

    this.tripRouteLists = tripRouteLists;
    this.context = context;

    fmuUserPref = context.getSharedPreferences("fmuPref", 0);
    editor = fmuUserPref.edit();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.trip_route_list, parent, false);

    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    final Route list = tripRouteLists.get(position);
    holder.txtTripRouteOrigin.setText(list.getOrigin());
    holder.txtTripRouteDestination.setText(list.getDestination());
    holder.routeVia.setText(list.getVia());

    holder.routeLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        editor.putString("origin", list.getOrigin());
        editor.putString("destination", list.getDestination());
        editor.putString("via", list.getVia());
        editor.putString("origin", list.getOrigin());
        editor.commit();

        Intent intent = new Intent(context, FindUvActivity.class);
        intent.putExtra("r_name", list.getRouteName());
        intent.putExtra("mode", "Pending");
        context.startActivity(intent);
      }
    });
  }

  @Override
  public int getItemCount() {
    return tripRouteLists.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    public TextView txtTripRouteOrigin;
    public TextView txtTripRouteDestination;
    public TextView routeVia;
    public LinearLayout routeLayout;
    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      txtTripRouteOrigin = (TextView)itemView.findViewById(R.id.routeOrigin);
      txtTripRouteDestination = (TextView)itemView.findViewById(R.id.routeDestination);
      routeVia = (TextView) itemView.findViewById(R.id.routeVia);
      routeLayout = (LinearLayout) itemView.findViewById(R.id.routeLayout);
    }
  }
}
