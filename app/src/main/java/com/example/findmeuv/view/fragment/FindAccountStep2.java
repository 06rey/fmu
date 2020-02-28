package com.example.findmeuv.view.fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class FindAccountStep2 extends Fragment {

    private AppViewModel viewModel;
    private ViewHelper viewHelper;
    private TextView txtFullname, txtContact;
    private Button btnContinue;
    private String passenger_id, contactNumber;
    private FragmentHandler fragmentHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.find_account_step2, container, false);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        initView(view);

        Bundle bundle = this.getArguments();

        StringBuilder str = new StringBuilder(bundle.getString("contact"))
                .replace(0, 8, "********");

        txtFullname.setText(bundle.getString("name"));
        txtContact.setText(str);
        passenger_id = bundle.getString("passenger_id");
        contactNumber = bundle.getString("contact");

        setViewModelObserver();
        return view;
    }

    private void initView(View view) {
        viewHelper = new ViewHelper(getContext());
        txtFullname = view.findViewById(R.id.txtFullName);
        txtContact = view.findViewById(R.id.txtContact);

        btnContinue = view.findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewHelper.showLoading();

                Map<String, String> data = new HashMap<>();
                data.put("main", "account");
                data.put("sub", "send_code");
                data.put("resp", "1");
                data.put("contact", contactNumber);
                data.put("passenger_id", passenger_id);
                viewModel.okHttpRequest(data, "POST", null);

            }
        });
    }


    private void setViewModelObserver() {

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                viewHelper.dismissLoading();

                Bundle bundle = new Bundle();
                bundle.putString("contact", contactNumber);
                bundle.putString("passenger_id", passenger_id);

                fragmentHandler.changeFragment(3, bundle);
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Sorry. Something went wrong there.");
            }
        });

        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Internet connection problem. Please check your device network setting.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Sorry. This service is not available at the moment.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Sorry. Something went wrong there.");
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
