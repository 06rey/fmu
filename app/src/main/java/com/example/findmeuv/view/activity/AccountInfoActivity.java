package com.example.findmeuv.view.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.findmeuv.R;
import com.example.findmeuv.utility.BugReport;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class AccountInfoActivity extends AppCompatActivity {

    private EditText fname, lname, contact, email, pass1, pass2;
    private RadioButton male, female;
    private RadioGroup userGender;
    private String gender;
    private Button register;

    private ViewHelper viewHelper;
    private AlertDialog alert;
    private AlertDialog.Builder builder;
    private BugReport debug;

    private AppViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        initView();
        initVar();

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        setViewModelObserver();
        viewModel.initialize(this);
    }

    private void initVar() {
        gender = "Male";
        //debug = new BugReport(this);
    }

    private void initView() {

        viewHelper = new ViewHelper(this);

        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        contact = findViewById(R.id.contact);
        email = findViewById(R.id.email);
        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);

        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        userGender = findViewById(R.id.userGender);

        register = findViewById(R.id.register);

        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);

        viewHelper.setTextListener(contact);

        setRadioButtonListener();
        setRegisterBtnListener();

    }

    private void setRegisterBtnListener() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm() == 0) {
                    if (isContactIsValid()) {
                        if (isValidEmail(email.getText().toString())) {
                            if (isPasswordMatch()) {
                                if (pass1.getText().toString().trim().length() >= 6) {
                                    registerUser();
                                } else {
                                    viewHelper.alertDialog("System Message", "Password must be at least 6 character long.");
                                }
                            } else {
                                viewHelper.alertDialog("System Message", "Password does not match. Please try again.");
                            }
                        } else {
                            email.setError("Email is not valid.");
                        }
                    } else {
                        contact.setError("Mobile number must be 11 digit number.");
                        viewHelper.alertDialog("System Message", "Mobile number must be 11 digit number.");
                    }
                } else {
                    viewHelper.alertDialog("System Message","All fields cannot be empty.");
                }
            }
        });
    }

    private boolean isContactIsValid() {
        return (contact.getText().toString().length() == 11);
    }


    private void setRadioButtonListener() {
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "Male";
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "Female";
            }
        });
    }

    private boolean isPasswordMatch() {
        boolean match = false;
        if (pass1.getText().toString().equals(pass2.getText().toString())) {
            match = true;
        }
        return match;
    }

    public final static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private int validateForm() {
        int count = 0;
        if (fname.getText().toString().trim().equals("")) {
            fname.setError("First Name is required.");
            count++;
        }
        if (lname.getText().toString().trim().equals("")) {
            lname.setError("Last Name is required.");
            count++;
        }
        if (contact.getText().toString().trim().equals("")) {
            contact.setError("Contact Number is required.");
            count++;
        }
        if (email.getText().toString().trim().equals("")) {
            email.setError("Email is required.");
            count++;
        }
        if (pass1.getText().toString().trim().equals("")) {
            pass1.setError("Password is required.");
            count++;
        }
        if (pass2.getText().toString().trim().equals("")) {
            pass2.setError("Confirm is password.");
            count++;
        }
        return count;
    }

    private void registerUser() {
        viewHelper.showLoading();

        Map<String, String> data = new HashMap<>();
        data.put("main", "account");
        data.put("sub", "register");
        data.put("resp", "1");
        data.put("fname", fname.getText().toString());
        data.put("lname", lname.getText().toString());
        data.put("contact", contact.getText().toString());
        data.put("email", email.getText().toString());
        data.put("pass1", pass1.getText().toString());
        data.put("gender", gender);

        viewModel.okHttpRequest(data, "POST", "");

    }

    private void setViewModelObserver() {

        // Login success
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> map) {

                builder.setTitle("Registration Succes")
                        .setMessage("Thank you for registering for Find me UV. Use your account's email and password to login.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(AccountInfoActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    }
                                }, 1000);
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });
        // Connection error
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("Registration Failed", "Internet connection problem. Please check your network setting.");
            }
        });

        // Service error eg. cannot connect to db in web api server
        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Sorry, this service is not available as of the moment. Try gain later.");
            }
        });

        // Provide wrong data
        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {

            }
        });

        // No result found
        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("Registration Failed", "Email is already exists. Please provide different email and try again.");
                email.setError("Email is not available.");
            }
        });

        // Json response from web api is in wrong pattern
        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                viewHelper.dismissLoading();
                viewHelper.alertDialog("System Message", "Sorry. Something went wrong there.");
            }
        });
    }

    @Override
    protected void onPause() {
        this.getViewModelStore().clear();
        super.onPause();
    }

}
