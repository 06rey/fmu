package com.example.findmeuv.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.handler.FragmentHandler;
import com.example.findmeuv.view.activity.LoginActivity;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class FindAccountStep4 extends Fragment {

    private AppViewModel viewModel;
    private ViewHelper dialog;
    private Button btnCont;
    private FragmentHandler fragmentHandler;
    private EditText txtNew, txtConfirm;
    private String passengerId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.find_account_step4, container, false);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        initView(view);

        Bundle bundle = this.getArguments();
        passengerId = bundle.getString("passenger_id");

        setViewModelObserver();
        return view;
    }

    private void initView(View view) {
        dialog = new ViewHelper(getContext());
        txtNew = view.findViewById(R.id.txtNew);
        txtConfirm = view.findViewById(R.id.txtConfrim);

        btnCont = view.findViewById(R.id.btnCont);

        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtNew.getText().toString().trim().length() >= 6) {
                    if (txtNew.getText().toString().trim().equals(txtConfirm.getText().toString().trim())) {
                        dialog.showLoading();

                        Map<String, String> data = new HashMap<>();
                        data.put("main", "account");
                        data.put("sub", "change_password");
                        data.put("resp", "1");
                        data.put("forgot_pass", "true");
                        data.put("pass1", txtNew.getText().toString().trim());
                        data.put("id", passengerId);
                        viewModel.okHttpRequest(data, "POST", null);
                    } else {
                        dialog.alertDialog("System Message", "Password does not match. Try again.");
                    }
                } else {
                    dialog.alertDialog("System Message", "Password must be at least 6 character long.");
                }
            }
        });
    }


    private void setViewModelObserver() {

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                dialog.dismissLoading();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                builder.setTitle("System Message")
                        .setMessage("You have successfully change your account password. Use your new password to login to your account.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Sorry. Something went wrong there.");
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
