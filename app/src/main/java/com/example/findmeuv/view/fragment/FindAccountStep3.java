package com.example.findmeuv.view.fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.handler.FragmentHandler;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


public class FindAccountStep3 extends Fragment {

    private AppViewModel viewModel;
    private ViewHelper dialog;

    private EditText txtCode;
    private Button btnCont, btnSendAgain;
    private TextView txtContact;

    private FragmentHandler fragmentHandler;
    private String passengerId, contactNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.find_account_step3, container, false);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        initView(view);

        Bundle bundle = this.getArguments();
        passengerId = bundle.getString("passenger_id");
        contactNumber = bundle.getString("contact");
        txtContact.setText(contactNumber);


        setViewModelObserver();

        return view;
    }

    private void initView(View view) {
        dialog = new ViewHelper(getContext());
        txtCode = view.findViewById(R.id.txtCode);
        btnCont = view.findViewById(R.id.btnCont);
        txtContact = view.findViewById(R.id.txtContact);
        btnSendAgain = view.findViewById(R.id.btnSendAgain);

        setClickListener();
    }

    private void setClickListener() {

        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtCode.getText().toString().trim().length() != 6) {
                    txtCode.setError("Code must be 6 digit number");
                } else {
                    dialog.showLoading();

                    Map<String, String> data = new HashMap<>();
                    data.put("main", "account");
                    data.put("sub", "confirm_code");
                    data.put("resp", "1");
                    data.put("code", txtCode.getText().toString().trim());
                    data.put("passenger_id", passengerId);
                    viewModel.okHttpRequest(data, "POST", null);
                }
            }

        });

        btnSendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.showLoading();

                Map<String, String> data = new HashMap<>();
                data.put("main", "account");
                data.put("sub", "send_code");
                data.put("resp", "1");
                data.put("contact", contactNumber);
                data.put("passenger_id", passengerId);
                viewModel.okHttpRequest(data, "POST", null);
            }
        });

    }

    private void setViewModelObserver() {

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                dialog.dismissLoading();
                if (list.get(0).get("type").equals("confirm_code")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("passenger_id", passengerId);
                    fragmentHandler.changeFragment(4, bundle);
                } else if (list.get(0).get("type").equals("send_code")) {
                    Toast.makeText(getContext(), "Code has been sent!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Oops. That one didn't work. Try again.");
            }
        });

        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Internet connection problem. Please check your device network setting.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Sorry. This service is not available at the moment.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Sorry. Something went wrong there.");
            }
        });


    }

    public void setFragmentHandler(FragmentHandler fragmentHandler) {
        this.fragmentHandler = fragmentHandler;
    }

    @Override
    public void onPause() {
        super.onPause();
        getViewModelStore().clear();
    }

}
