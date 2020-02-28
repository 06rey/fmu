package com.example.findmeuv.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.model.pojo.Route;
import com.example.findmeuv.view.adapter.RouteAdapter;
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

import static android.content.Context.INPUT_METHOD_SERVICE;

public class RouteFragment extends Fragment {

    RecyclerView tripRouteRecycler;
    RecyclerView.Adapter tripRouteAdapter;
    List<Route> tripRouteItem;

    ViewHelper dialog;

    ProgressBar progress;

    LinearLayout retry, noResLayout;
    Button retryBtn;

    LinearLayout btnRouteSearch;
    Button btnOk;
    TextView txtRouteSearch;
    TextView txtNoResult;

    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private String ref = "all", origin, dest;
    private int state = 1;

    private AppViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.trip_route_view, container, false);

        initialize(view);

        viewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);
        setViewModelObserver();
        viewModel.initialize(getActivity());

        tripRouteItem = new ArrayList<>();

        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "route");
        data.put("sub", "route_detail");
        if (ref.equals("search")) {
            data.put("search", "true");
            data.put("origin", origin);
            data.put("dest", dest);
        } else {
            data.put("search", "false");
        }

        viewModel.okHttpRequest(data, "GET", "");

        return view;
    }

    private void initialize(View view) {
        txtRouteSearch = (TextView)view.findViewById(R.id.txtRouteSearch);
        btnRouteSearch = (LinearLayout)view.findViewById(R.id.btnRouteSearch);
        txtNoResult = (TextView) view.findViewById(R.id.txtNoResult);
        btnOk = (Button)view.findViewById(R.id.btnTripRouteOk);
        noResLayout = (LinearLayout)view.findViewById(R.id.tripRouteNoResult) ;

        dialog = new ViewHelper(getContext());
        builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref = "all";
                RouteFragment.this
                        .getFragmentManager()
                        .beginTransaction()
                        .detach(RouteFragment.this)
                        .attach(RouteFragment.this)
                        .commit();
            }
        });


        btnRouteSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = LayoutInflater.from(getContext()).inflate(R.layout.get_route_dialog, null);

                final TextView txtOrigin = (EditText)view.findViewById(R.id.txtOrigin);
                final TextView txtDest = (EditText)view.findViewById(R.id.txtDest);

                builder.setTitle("Search Route")
                        .setView(view)
                        .setCancelable(false)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do something nothing for something nothing
                            }
                        })
                        .setPositiveButton("Search", null);
                alert = builder.create();
                alert.show();

                Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ref = "search";
                                //ref = txtRouteSearch.getText().toString();
                                origin = txtOrigin.getText().toString();
                                dest = txtDest.getText().toString();
                                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                                if (txtOrigin.getText().toString().trim().isEmpty() || txtDest.getText().toString().trim().isEmpty()) {
                                    if (txtOrigin.getText().toString().trim().isEmpty()) {
                                        txtOrigin.setError("Please enter origin");
                                    }
                                    if (txtDest.getText().toString().trim().isEmpty()) {
                                        txtDest.setError("Please enter destination");
                                    }
                                } else {
                                    alert.dismiss();
                                    RouteFragment.this
                                            .getFragmentManager()
                                            .beginTransaction()
                                            .detach(RouteFragment.this)
                                            .attach(RouteFragment.this)
                                            .commit();
                                }
                            }
                        });
                    }
                });
            }
        });

        progress = (ProgressBar)view.findViewById(R.id.tripRouteProgBar);
        dialog = new ViewHelper(getContext());

        progress.setVisibility(View.VISIBLE);

        tripRouteRecycler = (RecyclerView)view.findViewById(R.id.tripRouteRecycler);
        tripRouteRecycler.setHasFixedSize(true);

        tripRouteRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        retry = (LinearLayout) view.findViewById(R.id.tripRouteRetry);
        retryBtn = (Button) view.findViewById(R.id.tripRouteRetryBtn);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteFragment.this
                        .getFragmentManager()
                        .beginTransaction()
                        .detach(RouteFragment.this)
                        .attach(RouteFragment.this)
                        .commit();
            }
        });
    }

    private void setViewModelObserver() {

        // Login success
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> map) {
                progress.setVisibility(View.GONE);
                int len = map.size();
                for (int a=0; a<len; a++) {
                    Route item = new Route(
                            map.get(a).get("origin"),
                            map.get(a).get("destination"),
                            map.get(a).get("route_name"),
                            map.get(a).get("via")
                    );
                    tripRouteItem.add(item);
                }
                tripRouteAdapter = new RouteAdapter(tripRouteItem, getContext());
                tripRouteRecycler.setAdapter(tripRouteAdapter);
                state = 1;
            }
        });
        // Connection error
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progress.setVisibility(View.GONE);
                retry.setVisibility(View.VISIBLE);
                state = 0;
            }
        });

        // Service error eg. cannot connect to db in web api server
        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                progress.setVisibility(View.GONE);
                txtNoResult.setText("Sorry, This service is not available as of the moment. try again later.");
                noResLayout.setVisibility(View.VISIBLE);
            }
        });

        // No result found
        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                progress.setVisibility(View.GONE);
                noResLayout.setVisibility(View.VISIBLE);
            }
        });

        // Json response from web api is in wrong pattern
        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                progress.setVisibility(View.GONE);
                txtNoResult.setText("Sorry, something went wrong there. Try again.");
                noResLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        getActivity().getViewModelStore().clear();
        super.onPause();
    }
}
