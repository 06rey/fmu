package com.example.findmeuv.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.EventHandler;
import com.example.findmeuv.view.activity.FindUvActivity;
import com.example.findmeuv.view.adapter.PendingTripAdapter;
import com.example.findmeuv.model.pojo.Trip;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class PendingTripFragment extends Fragment {

    private RecyclerView pendingRecyclerView;
    private RecyclerView.Adapter pendingAdapter;
    private List<Trip> uvItem;

    private ViewHelper dialog;

    private ProgressBar progress;

    private LinearLayout retry, noResLayout;
    private Button retryBtn, btnOk;

    private TextView txtNoResult;
    int state = 1;
    private String route;
    private String search_key = "";
    private FindUvActivity findUvActivity;
    private boolean isRecyclerViewLoaded = false;

    private SwipeRefreshLayout swipeContainer;

    private AppViewModel appViewModel;
    public EventHandler eventHandler;
    private boolean serverSentEventIsRunning = false;

    private SharedPreferences fmuUserPref;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pending_traveling_trip_view, container, false);

        initialize(view);
        appViewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);
        appViewModel.initialize(getActivity());
        setViewModelObserver();

        fmuUserPref = getActivity().getSharedPreferences("fmuPref", 0);

        return view;
    }

    private void initialize(View view) {
        progress = view.findViewById(R.id.tripProgressBar);

        findUvActivity = (FindUvActivity) getActivity();

        eventHandler = new EventHandler();

        route = findUvActivity.getRoute_name();
        search_key = findUvActivity.loader.url_suffix;

        txtNoResult = view.findViewById(R.id.txtUvRouteNoResult);
        btnOk = view.findViewById(R.id.btnUvRouteOk);
        noResLayout = view.findViewById(R.id.uvRouteNoResult) ;

        pendingRecyclerView = view.findViewById(R.id.recyclerViewPending);
        pendingRecyclerView.setHasFixedSize(true);
        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dialog = new ViewHelper(getContext());

        retry = view.findViewById(R.id.retryLayout);
        retryBtn = view.findViewById(R.id.retryBtn);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingTripFragment.this.getFragmentManager().beginTransaction().detach(PendingTripFragment.this).attach(PendingTripFragment.this).commit();
            }
        });

        // Refresh recyclerview by swiping down
        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (serverSentEventIsRunning) {
                    appViewModel.closeServerEventConnection();
                }
                findUvActivity.getSupportFragmentManager().beginTransaction().remove(PendingTripFragment.this).commit();
                findUvActivity.loader.load_fragment();
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

       eventHandler.button.setClickListener(new EventHandler.ClickListener() {
           @Override
           public void onButtonClick() {
               dialog.showLoading();
           }
       });
    }

    private void startServerEvent() {
        progress.setVisibility(View.VISIBLE);

        Map<String, String> attr = new HashMap<>();
        uvItem = new ArrayList<>();

        attr.put("resp", "1");
        attr.put("main", "route");
        attr.put("sub", "route_trip");
        attr.put("route", route);
        attr.put("status", "Pending");
        attr.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));

        appViewModel.serverSentInitRequest(attr, "GET", search_key);
    }

    private Trip prepareItem(Map<String, String> data) {
        Trip item = new Trip(
                data.get("trip_id"),
                data.get("date"),
                data.get("route_name"),
                data.get("company_name"),
                data.get("plate_no"),
                data.get("model"),
                data.get("status"),
                data.get("depart_time"),
                data.get("arrival_time"),
                data.get("max_pass"),
                data.get("fare")
        );
        return item;
    }

    private void setViewModelObserver() {

        /*-----------------------------------------------------------------------------------
         * SERVER SENT EVENT
         *-----------------------------------------------------------------------------------
         */

        // SUCCESS OBSERVER
        appViewModel.getServerSentInitData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> maps) {
                uvItem = new ArrayList<>();
                int len = maps.size();
                for(int a=0; a<len; a++) {
                    Trip item = prepareItem(maps.get(a));
                    uvItem.add(item);
                }
                pendingAdapter = new PendingTripAdapter(uvItem, getContext(), getActivity(), appViewModel, eventHandler);
                pendingRecyclerView.setAdapter(pendingAdapter);
                progress.setVisibility(View.GONE);

                Map<String, String> attr = new HashMap<>();

                attr.put("resp", "1");
                attr.put("main", "route");
                attr.put("sub", "route_trip");
                attr.put("route", route);
                attr.put("status", "Pending");
                attr.put("event", "server_event");
                attr.put("passenger_id", fmuUserPref.getString("passenger_id", "0"));
                serverSentEventIsRunning = true;
                appViewModel.serverSentEvent(attr, search_key);
            }
        });

        appViewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                int listSize = list.size();
                for (int a=0; a<uvItem.size(); a++) {
                    for (int x=0; x<listSize; x++) {
                        if (uvItem.get(a).getTripID().equals(list.get(x).get("trip_id"))) {
                            break;
                        }
                        if (x == listSize-1) {
                            uvItem.remove(a);
                            pendingAdapter.notifyItemRemoved(a);
                        }
                    }
                }
                for (int x=0; x<listSize; x++) {
                    int uvItemSize = uvItem.size();
                    for (int a=0; a<uvItemSize; a++) {
                        if (list.get(x).get("trip_id").equals(uvItem.get(a).getTripID())) {

                            if (!list.get(x).get("max_pass").equals(uvItem.get(a).getVacantSeat())) {
                                uvItem.get(a).setVacantSeat(list.get(x).get("max_pass"));
                                pendingAdapter.notifyItemChanged(a);
                            }

                            if (!list.get(x).get("date").equals(uvItem.get(a).getTripDate())) {
                                uvItem.get(a).setTripDate(list.get(x).get("date"));
                                pendingAdapter.notifyItemChanged(a);
                            }

                            if (!list.get(x).get("depart_time").equals(uvItem.get(a).getDepartTime())) {
                                uvItem.get(a).setDepartTime(list.get(x).get("depart_time"));
                                pendingAdapter.notifyItemChanged(a);
                            }

                            if (!list.get(x).get("arrival_time").equals(uvItem.get(a).getArriveTime())) {
                                uvItem.get(a).setArriveTime(list.get(x).get("arrival_time"));
                                pendingAdapter.notifyItemChanged(a);
                            }

                            if (!list.get(x).get("plate_no").equals(uvItem.get(a).getPlateNo())) {
                                uvItem.get(a).setPlateNo(list.get(x).get("plate_no"));
                                pendingAdapter.notifyItemChanged(a);
                            }

                            if (!list.get(x).get("model").equals(uvItem.get(a).getVehicleModel())) {
                                uvItem.get(a).setVehicleModel(list.get(x).get("model"));
                                pendingAdapter.notifyItemChanged(a);
                            }

                            break;
                        }
                        if (a == uvItemSize-1) {
                            uvItem.add(Integer.parseInt(list.get(x).get("rank")), prepareItem(list.get(x)));
                            pendingAdapter.notifyItemInserted(Integer.parseInt(list.get(x).get("rank")));
                        }
                    }
                }
            }
        });

        // ERROR OBSERVER
        appViewModel.getSseConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progress.setVisibility(View.GONE);
                retry.setVisibility(View.VISIBLE);
            }
        });
        appViewModel.getSseServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progress.setVisibility(View.GONE);
                noResLayout.setVisibility(View.VISIBLE);
                txtNoResult.setText("This service is not available as of the moment. Please try again later.");
            }
        });
        appViewModel.getSseInitDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progress.setVisibility(View.GONE);
                noResLayout.setVisibility(View.VISIBLE);
            }
        });
        appViewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });
        appViewModel.getSseInitJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progress.setVisibility(View.GONE);
                noResLayout.setVisibility(View.VISIBLE);
                txtNoResult.setText("Sorry. Something went wrong there.");
            }
        });

        // Okhttp observer
        appViewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> maps) {
                eventHandler.response(maps);
                dialog.dismissLoading();
            }
        });
        // ERROR OBSERVER
        appViewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Internet connection problem. Please check your internet setting.");
            }
        });
        appViewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "This service is not available as og the moment. Please try again later.");
            }
        });
        appViewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "You cannot book this trip, departure schedule is out of date.");
            }
        });

        appViewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Sorry. Something went wrong there.");
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (state == 0) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        } else {
            super.setUserVisibleHint(false);
        }
    }

    @Override
    public void onPause() {
        if (serverSentEventIsRunning) {
            appViewModel.closeServerEventConnection();
        }
        getActivity().getViewModelStore().clear();
        isRecyclerViewLoaded = false;
        serverSentEventIsRunning = false;
        int size = uvItem.size();
        if (size > 0) {
            uvItem.clear();
            pendingAdapter.notifyItemRangeRemoved(0, size);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        startServerEvent();
        super.onResume();
    }

}
