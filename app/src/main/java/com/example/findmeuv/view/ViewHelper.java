package com.example.findmeuv.view;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.findmeuv.R;
import com.example.findmeuv.handler.ConfirmHandler;
import com.example.findmeuv.handler.EventHandler;
import com.example.findmeuv.view.activity.FindUvActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ViewHelper {
    private Context context;
    private Dialog dialog;
    private AlertDialog.Builder alertBuilder;
    private AlertDialog loading;
    private SharedPreferences fmuUserPref;
    private SharedPreferences.Editor editor;
    private AlertDialog progressBar;
    private TextWatcher textWatcher;
    public ConfirmHandler confirmHandler = new ConfirmHandler();

    public TextWatcher getTextWatcher() {
        return textWatcher;
    }

    public ViewHelper(Context context) {
        this.context = context;

        fmuUserPref = context.getSharedPreferences("fmuPref", 0);

        dialog = new Dialog(context, R.style.AppThemeNoActionBar);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCanceledOnTouchOutside(false);
        createLoadingBar();

    }

    public void showProgressBar(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        TextView txtDescription = view.findViewById(R.id.txtLoading);
        txtDescription.setText(description + " ...");
        builder.setCancelable(false)
                .setTitle("Please Wait..")
                .setView(view);
        progressBar = builder.create();
        progressBar.show();
    }

    public void dismissProgressBar() {
        progressBar.dismiss();
    }

    private void createLoadingBar() {
        alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        TextView txtLoading = view.findViewById(R.id.txtLoading);
        alertBuilder.setCancelable(true)
                .setView(view);
        loading = alertBuilder.create();
    }

    private void getDate(final TextView textView, final Boolean expandTextSize) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.get_date_dialog, null);
        final DatePicker date = view.findViewById(R.id.datePicker);

        builder.setTitle("Pick Date")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String datePicked = String.valueOf(date.getDayOfMonth()) + "-" + String.valueOf(date.getMonth() + 1) + "-" + String.valueOf(date.getYear());

                        if (expandTextSize) {
                            textView.setTextSize(18);
                        } else {
                            datePicked = String.valueOf(date.getYear()) + "-" + String.valueOf(date.getMonth()) + "-" + String.valueOf(date.getDayOfMonth());
                        }
                        textView.setText(datePicked);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getTime(final TextView textView, final Boolean expandTextSize, final Time_Holder hold) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.get_time_dialog, null);
        final TimePicker time = view.findViewById(R.id.timePicker);

        builder.setTitle("Pick Time")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        String hour, minute, m = "AM";
                        hour = String.valueOf(time.getCurrentHour());
                        minute = String.valueOf(time.getCurrentMinute());

                        hold.time_hold = hour +":"+ minute +":00";

                        if (time.getCurrentHour() > 12) {
                            hour = String.valueOf(time.getCurrentHour() - 12);
                            m = "PM";
                        }

                        if (time.getCurrentMinute() < 10) {
                            minute = "0" + minute;
                        }

                        textView.setText(hour + ":" + minute + " " + m);

                        if (expandTextSize == true) {
                            textView.setTextSize(18);
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void tripFilterDialog(String route, final FindUvActivity.FragmentLoader loader) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);

        View view = LayoutInflater.from(context).inflate(R.layout.get_filter_value_dialog, null);

        final TextView txtDate = view.findViewById(R.id.txtdate);
        final TextView txtTime = view.findViewById(R.id.txtTime);
        final Time_Holder holder = new Time_Holder("");

        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate(txtDate, true);
            }
        });

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTime(txtTime, true, holder);
            }
        });

        builder.setTitle("Enter date and time")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loader.url_suffix = "&date="+ txtDate.getText().toString() +"&time="+ holder.time_hold;
                        loader.load_fragment();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showLoading() {
        loading.show();
    }

    public void dismissLoading() {
        loading.dismiss();
    }

    public void alertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context, R.style.MyAlertDialogStyle);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do things
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Permission request message dialog

    public boolean checkGps(final Activity activity) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle("Device location is off")
                    .setCancelable(false)
                    .setMessage("You cannot use this service if device location is off. Please go to settings and turn on device location.") // Want to enable?
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No Thanks", null)
                    .show();
            return false;
        } else {
            return true;
        }
    }

    public void serviceNotAvailable() {
        String header = "System Message";
        String msg = "This service is not available if location access is not allowed.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context, R.style.MyAlertDialogStyle);
        builder.setTitle(header)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean checkPermission(Activity activity) {
        boolean permission = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
        if (permission) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionDialog();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
            return false;
        } else {
            return true;
        }
    }

    private void showPermissionDialog() {
        String header = "Allow 'Find Me UV' to access your location while you are using th app?";
        String msg = "This allows us to use your location to provide you certain feature like setting your boarding location and used for nearby UV Express.";

        new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                .setTitle(header)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton("ALLOW", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("DENY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Location access denied!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    public void enabledGpsDialog(final Activity activity, final Intent finalIntent) {
        new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                .setTitle("Device location is off")
                .setCancelable(false)
                .setMessage("To continue this service, device location must be tun on. Please go to settings and turn on device location.") // Want to enable?
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivity(finalIntent);
                    }
                })
                .show();
    }

    // Inner class
    private class Time_Holder {
        private String time_hold;
        private Time_Holder(String time_hold) {
            this.time_hold = time_hold;
        }
    }

    public void setTextListener(final EditText editText) {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String txt = s.toString();
                if (txt.length() > 11) {
                    editText.setText(txt.substring(0, txt.length()-1));
                    editText.setSelection(editText.getText().length());
                    Log.d("DebugLog", "JHSFYDJHSGFJHDSFJSGFGSHFD TXT " + txt.substring(0, txt.length() - 1));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        editText.addTextChangedListener(textWatcher);
    }
    public void exitActivity(final Activity activity, String title, String msg) {
        if (title.trim().equals("") || title == null) {
            title = "";
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        alertBuilder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.onBackPressed();
                    }
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    public void confirmDialog(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context, R.style.MyAlertDialogStyle);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmHandler.yes();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmHandler.no();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
