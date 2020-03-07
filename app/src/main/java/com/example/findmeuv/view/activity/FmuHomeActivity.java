package com.example.findmeuv.view.activity;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.FragmentBackPressedHandler;
import com.example.findmeuv.service.NotificationService;
import com.example.findmeuv.view.fragment.AccountSettingFragment;
import com.example.findmeuv.view.fragment.BookingFragment;
import com.example.findmeuv.view.fragment.FmuDashboardFragment;
import com.example.findmeuv.view.fragment.RouteFragment;
import com.example.findmeuv.view.fragment.UserGuideFragment;
import com.example.findmeuv.view.fragment.TravelingTripFragment;
import com.example.findmeuv.helper.User;
import com.example.findmeuv.utility.BugReport;
import com.example.findmeuv.view.ViewHelper;

import android.os.Handler;
import android.util.Log;
import android.view.View;


import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class FmuHomeActivity extends AppCompatActivity implements
        FmuDashboardFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    public TextView title;
    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;
    private ViewHelper viewHelper;
    private TextView email, name;
    private View userView;
    private User user;
    private FragmentTransaction fragment_transaction;
    private NavigationView navigationView;
    private BugReport bugReporter;
    private LocationManager locationManager;
    private final static String TAG = "DebugLog";
    private FragmentBackPressedHandler fragmentBackPressedHandler = new FragmentBackPressedHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fmu_home);

        initialize();

        fragment_transaction = getSupportFragmentManager().beginTransaction();
        fragment_transaction.replace(R.id.mainframe, new FmuDashboardFragment());
        fragment_transaction.commit();
    }


    private void initialize() {
        fmuUserPref = getSharedPreferences("fmuPref",
                Context.MODE_PRIVATE);
        editor = fmuUserPref.edit();

        viewHelper = new ViewHelper(this);
        user = new User(this);

        title = findViewById(R.id.titleText);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        userView = navigationView.getHeaderView(0);

        name = userView.findViewById(R.id.userFullName);
        email = userView.findViewById(R.id.userEmail);

        if (fmuUserPref.getString("email", "").equals("")) {
            email.setText(fmuUserPref.getString("contact", ""));
        } else {
            email.setText(fmuUserPref.getString("email", ""));
        }
        name.setText(user.fullname());

        navigationView.setNavigationItemSelectedListener(this);

        //NOTE:  Checks first item in the navigation drawer initially
        navigationView.setCheckedItem(R.id.dashboard);

        fragmentBackPressedHandler.setFragmentBackStateListener(new FragmentBackPressedHandler.FragmentBackStateListener() {
            @Override
            public void onFragmentBackStateResponse(boolean backState) {
                if (backState) {
                    onBackPressed();
                }
            }
        });
        startNotificationService();
    }

    private void startNotificationService() {
        Log.d("DebugLog", "NOTIFICATION SERVICE TRIGGERED");
        startService(new Intent(this, NotificationService.class));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getSupportFragmentManager().getBackStackEntryCount() > 2) {
                getSupportFragmentManager().popBackStack();
            } else {
                if (fragmentBackPressedHandler.getFragmentBackPressedListener() != null) {
                    fragmentBackPressedHandler.doBackPressed();
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fmu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        fragmentBackPressedHandler.doBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        fragmentBackPressedHandler.removeFragmentBackPressedListener();
        Fragment fragment = null;

        if (id == R.id.dashboard) {
            this.setTitle("");
            title.setText("Home");
            fragment = new FmuDashboardFragment();
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (id == R.id.findUV) {
            this.setTitle("");
            title.setText("Pending Trip");
            fragment = new RouteFragment();
        } else if (id == R.id.myUV) {
            this.setTitle("");
            onClickBtnTraveling(null);
        } else if (id == R.id.booking) {
            this.setTitle("");
            title.setText("My Booking");
            fragment = new BookingFragment();
        } else if (id == R.id.account) {
            this.setTitle("");
            title.setText("Account Setting");
            fragment = new AccountSettingFragment();
        } else if (id == R.id.userguide) {
            this.setTitle("");
            title.setText("User Guide");
            fragment = new UserGuideFragment();
        }

        //NOTE: Fragment changing code
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainframe, fragment).addToBackStack(null);

            ft.commit();
        }

        this.getViewModelStore().clear();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logoutClick(MenuItem item) {
        // Logout code, clearing all user preferences such as user name email etc
        viewHelper.showLoading();
        editor.clear();
        editor.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHelper.dismissLoading();
                Intent intent = new Intent(FmuHomeActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, 500);
    }

    private void switchFragment(Fragment fragment, String str, int itemID) {
        TextView title = findViewById(R.id.titleText);
        this.setTitle("");
        title.setText(str);

        navigationView.setCheckedItem(itemID);

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainframe, fragment).addToBackStack(null);
            ft.commit();
        }
        this.getViewModelStore().clear();
    }

    public void onClickBtnFuv(View view) {
        switchFragment(new RouteFragment(), "Pending Trip", R.id.findUV);
    }

    public void onClickBtnTraveling(View view) {
        if (viewHelper.checkPermission(getParent())) {
            if (viewHelper.checkGps(getParent())) {
                Fragment fragment = new TravelingTripFragment();
                ((TravelingTripFragment) fragment).setFragmentHandlerListener(fragmentBackPressedHandler);
                switchFragment(fragment, "Traveling Trip", R.id.myUV);
            }
        } else {
            viewHelper.serviceNotAvailable();
        }
    }

    public void onClickbtnBh(View view) {
        switchFragment(new BookingFragment(), "My Booking", R.id.booking);
    }

    public void onCLickBtnAs(View view) {
        switchFragment(new AccountSettingFragment(), "Account Setting", R.id.account);
    }

    public void onCLickbtnUg(View view) {
        switchFragment(new UserGuideFragment(), "User Guide", R.id.userguide);
    }

    public void logout(View view) {
        logoutClick(null);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Intent intent = getIntent();
        if (intent.getBooleanExtra("booking", false)) {
            onClickbtnBh(null);
        }
    }

}
