package com.example.findmeuv.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findmeuv.R;
import com.example.findmeuv.view.ViewHelper;
import com.example.findmeuv.handler.FragmentHandler;
import com.example.findmeuv.view.fragment.FindAccountStep2;
import com.example.findmeuv.view.fragment.FindAccountStep3;
import com.example.findmeuv.view.fragment.FindAccountStep4;
import com.example.findmeuv.view_model.AppViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ForgotPasswordActivity extends AppCompatActivity implements FragmentHandler.ChangeFragmentListener {

    private Button btnBack;
    private EditText email;
    private Button search;
    private LinearLayout step1;
    private TextView txtTitle;

    private ViewHelper dialog;
    private AppViewModel viewModel;

    private FragmentHandler fragmentHandler = new FragmentHandler();
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);

        initViews();
        setViewModelObserver();

        fragmentHandler.setChangeFragmentListener(this);
        fragmentManager = getSupportFragmentManager();
    }

    private void initViews() {
        dialog = new ViewHelper(this);
        email = findViewById(R.id.txtEmail);
        search = findViewById(R.id.btnSearch);
        step1 = findViewById(R.id.step1);
        btnBack = findViewById(R.id.btnback);
        txtTitle = findViewById(R.id.txtTitle);

        setClickListener();
    }

    private void setClickListener() {
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noError = true;
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                    email.setError("Invalid email.");
                    noError = false;
                }
                if (email.getText().toString().trim().equals("")) {
                    email.setError("Email is required.");
                    noError = false;
                }
                if (noError) {
                    checkEmail();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void checkEmail() {
        dialog.showLoading();

        Map<String, String> data = new HashMap<>();
        data.put("main", "account");
        data.put("sub", "find_account");
        data.put("resp", "1");
        data.put("email", email.getText().toString());
        viewModel.okHttpRequest(data, "POST", null);
    }

    private void setViewModelObserver() {


        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                dialog.dismissLoading();
                Bundle bundle = new Bundle();
                bundle.putString("name", list.get(0).get("f_name") + " " + list.get(0).get("l_name"));
                bundle.putString("contact", list.get(0).get("contact"));
                bundle.putString("passenger_id", list.get(0).get("passenger_id"));

                fragmentHandler.changeFragment(2, bundle);

                if(getCurrentFocus()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                step1.setVisibility(View.GONE);
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dialog.dismissLoading();
                dialog.alertDialog("No accounts match that information", "Make sure you've entered the correct email address.");
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

    @Override
    protected void onPause() {
        getViewModelStore().clear();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
       if (fragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            fragmentManager.executePendingTransactions();
            fragmentManager.popBackStack();
            if (fragmentManager.getBackStackEntryCount() == 1) {
                step1.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onChangeFragment(int id, Bundle bundle) {

        Fragment fragment = null;

        if (id == 2) {
            fragment = new FindAccountStep2();
            ((FindAccountStep2) fragment).setFragmentHandler(fragmentHandler);
            txtTitle.setText("Confirm Your Account");
        } else if (id == 3) {
            fragment = new FindAccountStep3();
            ((FindAccountStep3) fragment).setFragmentHandler(fragmentHandler);
            txtTitle.setText("Reset Your Password");
        } else if (id == 4) {
            fragment = new FindAccountStep4();
            ((FindAccountStep4) fragment).setFragmentHandler(fragmentHandler);
            txtTitle.setText("Create New Password");
        }

        fragment.setArguments(bundle);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.findAccountFrame, fragment)
                .addToBackStack(null);
        fragmentTransaction.commit();

    }
}
