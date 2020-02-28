package com.example.findmeuv.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.AdapterHandler;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view.activity.FmuHomeActivity;
import com.example.findmeuv.view.adapter.BookingPendingAdapter;
import com.example.findmeuv.model.pojo.PendingTrip;
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

public class BookingPendingFragment extends Fragment {

    private RecyclerView bookingRecyclerView;
    private RecyclerView.Adapter pendingAdapter;

    private ProgressBar progressBar;

    private LinearLayout retry, noResLayout;
    private SharedPreferences fmuUser;

    private List<PendingTrip> pendingBookItem = new ArrayList<>();
    private AppViewModel viewModel;
    private Activity activity;
    private AdapterHandler adapterHandler = new AdapterHandler();
    private String mode = "none";
    private ViewHelper viewHelper;
    private int position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_pending, container, false);
        activity = getParentFragment().getActivity();
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(activity);
        initialize(view);
        setViewModelObserver();
        getPendingTrip();
        return view;
    }

    private void initialize(final View view) {
        noResLayout = view.findViewById(R.id.noResultLayout);
        progressBar = view.findViewById(R.id.pbBar);
        fmuUser = getContext().getSharedPreferences("fmuPref", 0);

        progressBar.setVisibility(View.VISIBLE);

        viewHelper = new ViewHelper(getContext());

        bookingRecyclerView = view.findViewById(R.id.recyclerViewPending);
        bookingRecyclerView.setHasFixedSize(true);
        bookingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        retry = view.findViewById(R.id.retryLayout);
        Button retryBtn = view.findViewById(R.id.btnRetry);

        Button btnNoResult = view.findViewById(R.id.btnNoResult);
        btnNoResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToHome();
            }
        });

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookingPendingFragment.this.getFragmentManager().beginTransaction().detach(BookingPendingFragment.this).attach(BookingPendingFragment.this).commit();
            }
        });

        adapterHandler.setAdapterListener(new AdapterHandler.AdapterListener() {
            @Override
            public void onAdapterAction(Map<String, String> data) {
                viewHelper.showProgressBar("Please wait ...");
                data.put("resp", "1");
                data.put("main", "booking");
                data.put("sub", "delete_booking");
                viewModel.okHttpRequest(data, "GET", "");
                mode = "delete";
                position = Integer.parseInt(data.get("position"));
            }
        });
    }

    private void getPendingTrip() {
        String passenger_id = fmuUser.getString("passenger_id", "0");
        Map<String, String> param = new HashMap<>();
        param.put("resp", "1");
        param.put("main", "booking");
        param.put("sub", "get_pending_booking");
        param.put("passenger_id", passenger_id);
        viewModel.okHttpRequest(param, "GET", "");
    }

    private void redirectToHome() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), FmuHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                progressBar.setVisibility(View.GONE);
                startActivity(intent);
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                activity.finish();
            }
        }, 500);
    }

    private void showErrorDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("System Message")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        redirectToHome();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void dismissLoading() {
        if (mode.equals("none")) {
            progressBar.setVisibility(View.GONE);
        } else {
            viewHelper.dismissProgressBar();
        }
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String type = list.get(0).get("type");

                switch (type) {
                    case "get_pending_book":
                        for (Map<String, String> map: list) {
                            PendingTrip pending = new PendingTrip();
                            pending.setStatus(map.get("status"));
                            pending.setDate(map.get("date"));
                            pending.setFrom(map.get("origin"));
                            pending.setTo(map.get("destination"));
                            pending.setCompany(map.get("company_name"));
                            pending.setDepart(map.get("depart_time"));
                            pending.setArrive(map.get("arrival_time"));
                            pending.setFare(map.get("fare"));
                            pending.setAmount(map.get("amount"));
                            pending.setPass(map.get("no_of_pass"));
                            pending.setBookId(map.get("booking_id"));
                            pending.setTripId(map.get("trip_id"));
                            pendingBookItem.add(pending);
                        }
                        pendingAdapter = new BookingPendingAdapter(pendingBookItem, getContext(), adapterHandler);
                        bookingRecyclerView.setAdapter(pendingAdapter);
                        progressBar.setVisibility(View.GONE);
                        break;
                    case "delete_booking":
                        pendingBookItem.remove(position);
                        pendingAdapter.notifyItemRemoved(position);
                        viewHelper.alertDialog("System Message", "Reservation successfully canceled");
                        if (pendingBookItem.size() == 0) {
                            noResLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                dismissLoading();
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
                retry.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
                showErrorDialog("This service is not available at the moment.");
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
                noResLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dismissLoading();
                showErrorDialog("Something went wrong there.");
            }
        });
    }
}
