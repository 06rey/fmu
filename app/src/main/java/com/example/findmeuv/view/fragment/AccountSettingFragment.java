package com.example.findmeuv.view.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class AccountSettingFragment extends Fragment {

    private EditText fname, lname, gender, contact, email, pass1, pass2, pass3, txtChanged;
    private Button contactChange, btnChange, emailChange, btnSaveChange, btnCancelChange;
    private SharedPreferences fmuUser;
    private SharedPreferences.Editor editor;
    private ViewHelper viewHelper;
    private TextView txtCap;
    private LinearLayout pnlDailog;
    private AlertDialog.Builder builder;
    private AppViewModel viewModel;

    private String change = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.account_setting, container, false);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(getActivity());

        initViews(view);

        setViewModelObserver();
        return view;
    }

    private void initViews(View view) {
        fname = view.findViewById(R.id.fname);
        lname = view.findViewById(R.id.lname);
        gender = view.findViewById(R.id.gender);
        contact = view.findViewById(R.id.contact);
        email = view.findViewById(R.id.email);
        pass1 = view.findViewById(R.id.pass1);
        pass2 = view.findViewById(R.id.pass2);
        pass3 = view.findViewById(R.id.pass3);
        txtChanged = view.findViewById(R.id.txtChange);

        txtCap = view.findViewById(R.id.txtCap);


        builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

        btnChange = view.findViewById(R.id.btnChange);
        btnCancelChange = view.findViewById(R.id.btnCancelChange);
        contactChange = view.findViewById(R.id.contactChange);
        emailChange = view.findViewById(R.id.emailChange);
        btnSaveChange = view.findViewById(R.id.btnSaveChange);

        pnlDailog = view.findViewById(R.id.dialogPnl);

        viewHelper = new ViewHelper(getContext());
        fmuUser = getActivity().getSharedPreferences("fmuPref", 0);
        editor = fmuUser.edit();

        contactChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputDialog("Contact");
            }
        });

        emailChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputDialog("Email");
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        btnSaveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });

        btnCancelChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pnlDailog.setVisibility(View.GONE);
                txtChanged.setText("");
            }
        });

        loadData();

    }

    private void loadData() {
        fname.setText(fmuUser.getString("f_name", ""));
        lname.setText(fmuUser.getString("l_name", ""));
        gender.setText(fmuUser.getString("gender", ""));
        contact.setText(fmuUser.getString("contact", ""));
        email.setText(fmuUser.getString("email", ""));
    }

    private void inputDialog(String str) {
        if (str.equals("Email")) {
            txtChanged.setInputType(InputType.TYPE_CLASS_TEXT);
            txtChanged.removeTextChangedListener(viewHelper.getTextWatcher());
        } else {
            txtChanged.setInputType(InputType.TYPE_CLASS_NUMBER);
            viewHelper.setTextListener(txtChanged);
        }
        txtCap.setText(str);
        txtChanged.setHint("Enter new " + str.toLowerCase());
        change = str;
        pnlDailog.setVisibility(View.VISIBLE);
    }

    private void updatePassword() {
        if (!hasEmptyInputPassword()) {
            if (pass1.getText().toString().trim().length() >= 6) {
                if (pass1.getText().toString().equals(pass2.getText().toString())) {
                    viewHelper.showLoading();

                    Map<String, String> data = new HashMap<>();
                    data.put("main", "account");
                    data.put("sub", "change_password");
                    data.put("resp", "1");
                    data.put("id", fmuUser.getString("passenger_id", ""));
                    data.put("pass1", pass1.getText().toString());
                    data.put("pass3", pass3.getText().toString());

                    viewModel.okHttpRequest(data, "POST", null);
                } else {
                    viewHelper.alertDialog("Failed", "New password and confirm password does not match.");
                }
            } else {
                viewHelper.alertDialog("System Message", "Password must be at least 6 character long.");
            }
        }
    }

    private boolean hasEmptyInputPassword() {
        boolean isEmpty = false;
        if (pass1.getText().toString().trim().equals("")) {
            pass1.setError("New password is required.");
            isEmpty = true;
        }
        if (pass2.getText().toString().trim().equals("")) {
            pass2.setError("Please confirm new password.");
            isEmpty = true;
        }
        if (pass3.getText().toString().trim().equals("")) {
            pass3.setError("Current password is required.");
            isEmpty = true;
        }
        return isEmpty;
    }

    private boolean validateInput() {
        boolean isEmpty = false;
        if (txtChanged.getText().toString().trim().equals("")) {
            txtChanged.setError("Please enter your new " + change.toLowerCase());
            isEmpty = true;
        } else {
            if (change.equals("Contact")) {
                if (txtChanged.getText().toString().trim().length() != 11) {
                    viewHelper.alertDialog("Change " + change, "Mobile number must be 11 digit.");
                    isEmpty = true;
                }
            } else {
                if (!isValidEmail(txtChanged.getText().toString())) {
                    txtChanged.setError("Email is not valid.");
                    isEmpty = true;
                }
            }
        }
        return isEmpty;
    }

    private boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isInputNew() {
        boolean hasChanged = false;

        if (change.equals("Email")) {
            if (!txtChanged.getText().toString().equals(fmuUser.getString("email", ""))) {
                hasChanged = true;
            }
        } else {
            if (!txtChanged.getText().toString().equals(fmuUser.getString("contact", ""))) {
                hasChanged = true;
            }
        }
        if (!hasChanged) {
            viewHelper.alertDialog("Change " + change, "The " + change.toLowerCase() + " you enter is your current " + change.toLowerCase() + ". No changes will be made.");
        }
        return hasChanged;
    }

    private void updateData() {
        if (!validateInput()) {
            if (isInputNew()) {
                viewHelper.showLoading();
                Map<String, String> data = new HashMap<>();
                data.put("main", "account");
                data.put("sub", "update");
                data.put("resp", "1");
                data.put("id", fmuUser.getString("passenger_id", ""));
                data.put("value", txtChanged.getText().toString());
                data.put("mode", change);

                viewModel.okHttpRequest(data, "POST", null);
            }
        }
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                String type = list.get(0).get("type");

                viewHelper.dismissLoading();

                if (type.equals("change_password")) {
                    viewHelper.alertDialog("Password Change","Your password has been successfully change.");
                    pass1.setText("");
                    pass2.setText("");
                    pass3.setText("");
                } else if (type.equals("update_info")) {
                    viewHelper.alertDialog("Update Account", "Data saved successfully.");
                    if (change.equals("Email")) {
                        editor.putString("email", txtChanged.getText().toString());
                        email.setText(txtChanged.getText().toString());
                        editor.commit();
                    } else {
                        editor.putString("contact", txtChanged.getText().toString());
                        contact.setText(txtChanged.getText().toString());
                        editor.commit();
                    }
                    txtChanged.setText("");
                    pnlDailog.setVisibility(View.GONE);
                }

            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.alertDialog("System Message", "Something went wrong there.");
                viewHelper.dismissLoading();
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.alertDialog("System Message", "This service is not available at the moment.");
                viewHelper.dismissLoading();
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.alertDialog("System Message", "Wrong old password. Try again.");
                viewHelper.dismissLoading();
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("Password Change","The current password you enter is incorrect. Please try again..");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.alertDialog("System Message", "Something went wrong there.");
                viewHelper.dismissLoading();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getViewModelStore().clear();
    }


}
