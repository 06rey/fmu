package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.findmeuv.helper.User;
import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    EditText email;
    EditText password;
    Button login;

    private AppViewModel viewModel;
    ViewHelper dialog;

    private AlertDialog alert;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);

        initViews();
        setListener();
        setViewModelObserver();

        viewModel.initialize(this);
    }

    private void initViews() {
        email = (EditText)findViewById(R.id.etRegFname);
        password = (EditText)findViewById(R.id.etUserPass);
        login = (Button)findViewById(R.id.btnLogin);

        dialog = new ViewHelper(this);
        builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
    }

    private void setListener() {
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (email.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
                    login.setEnabled(true);
                    login.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    login.setEnabled(false);
                    login.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (email.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
                    login.setEnabled(true);
                    login.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    login.setEnabled(false);
                    login.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public final static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void clickRegister(View view) {
        Intent register = new Intent(LoginActivity.this, AccountInfoActivity.class);
        startActivity(register);
    }

    public void clickBtnLogin(View view) {
       if (isValidEmail(email.getText().toString())) {
            login();
        } else {
            dialog.alertDialog("Invalid Email", "Email is not valid. Please try again.");
            email.setError("Invalid email");
        }
    }

    private void login() {
        dialog.showLoading();
        Map<String, String> data = new HashMap<>();

        data.put("resp", "1");
        data.put("main", "account");
        data.put("sub", "login");
        data.put("email", email.getText().toString());
        data.put("pass", password.getText().toString());

        viewModel.okHttpRequest(data, "POST", "");
    }

    public void forgotPasswordClick(View view) {
        Intent forgotPassword = new Intent(this, ForgotPasswordActivity.class);
        startActivity(forgotPassword);
    }

    private void setViewModelObserver() {

        // Login success
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> map) {
                new User(getApplicationContext()).login(map.get(0));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, FmuHomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        dialog.dismissLoading();
                        finish();
                    }
                }, 500);
            }
        });
        // Connection error
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("Login Failed", "Internet connection problem. Please check your network setting.");
            }
        });

        // Service error eg. cannot connect to db in web api server
        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Sorry, this service is not available as of the moment. Try gain later.");
            }
        });

        // Provide wrong data
        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                dialog.dismissLoading();
                builder.setTitle("Can't Find Account")
                        .setMessage("It looks like " + email.getText().toString() + " doesn't match an existing account. If you don't have a Find me UV account, you can create one now.")
                        .setCancelable(false)
                        .setPositiveButton("TRY AGAIN", null)
                        .setNegativeButton("CREATE ACCOUNT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(LoginActivity.this, AccountInfoActivity.class);
                                startActivity(intent);
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });

        // No result found
        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                dialog.dismissLoading();
                dialog.alertDialog("Login Failed", "Wrong email or password. Please try again.");
            }
        });

        // Json response from web api is in wrong pattern
        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean response) {
                dialog.dismissLoading();
                dialog.alertDialog("System Message", "Sorry. Something went wrong there.");
            }
        });
    }

    @Override
    protected void onPause() {
        this.getViewModelStore().clear();
        super.onPause();
    }
}
