package com.karakostas.disasterreport;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    SwitchPreferenceCompat notificationPreference;
    Preference notificationFilterPreference;
    SharedPreferences pref;
    Context mContext;
    public SettingsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = getActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    notificationFilterPreference.setEnabled(true);
                    PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                            .addTag("notificationWorkTag")
                            .setInitialDelay(5, TimeUnit.MINUTES)
                            .build();
                    WorkManager.getInstance(mContext).enqueueUniquePeriodicWork("notificationWork", ExistingPeriodicWorkPolicy.KEEP, work);
                } else {

                    Toast.makeText(mContext, "Could not get access to background location. Notifications will be disabled.", Toast.LENGTH_LONG).show();
                    notificationPreference.setChecked(false);
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference themePreference = findPreference("night_mode_switch");
        notificationPreference = findPreference("notification_switch");
        notificationFilterPreference = findPreference("notification_filters");
        assert notificationFilterPreference != null;
        assert notificationPreference != null;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);


        themePreference.setOnPreferenceChangeListener((preference, newValue) ->{
            boolean a = Boolean.parseBoolean(newValue.toString());
            if (a){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            return true;
        });
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            notificationPreference.setChecked(false);
        }

        notificationFilterPreference.setEnabled(pref.getBoolean("notification_switch", false));
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean a = Boolean.parseBoolean(newValue.toString());
            if (a) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    notificationPreference.setChecked(false);
                    AlertDialog.Builder permissionDialog = new AlertDialog.Builder(mContext);
                    permissionDialog.setMessage("Disaster Report needs to access your background location " +
                            "to provide notifications. \n\nPlease enable \"Allow all the time\" in the next screen.");
                    permissionDialog.setTitle("Location Permission");
                    permissionDialog.setCancelable(false);
                    permissionDialog.setPositiveButton("OK", (dialogInterface, i) ->
                            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    0));
                    permissionDialog.setNegativeButton("CANCEL", (dialogInterface, i) -> notificationPreference.setChecked(false));
                    AlertDialog permissionExplanationDialog = permissionDialog.create();
                    permissionExplanationDialog.show();
                    } else {
                    notificationFilterPreference.setEnabled(true);
                    PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                            .addTag("notificationWorkTag")
                            .setInitialDelay(5, TimeUnit.MINUTES)
                            .build();
                    WorkManager.getInstance(mContext).enqueueUniquePeriodicWork("notificationWork", ExistingPeriodicWorkPolicy.KEEP, work);
                    }
                } else {
                    notificationFilterPreference.setEnabled(false);
                    WorkManager.getInstance(mContext).cancelUniqueWork("notificationWork");
                }
                return true;
            }
        );

        notificationFilterPreference.setOnPreferenceClickListener(preference -> {
            showDialog();
            return true;
        });
    }
    private void showDialog() {

        EarthquakeNotificationFilterPreference earthquakeFiltersDialog = EarthquakeNotificationFilterPreference.newInstance("Title");
        FragmentManager fm = getActivity().getSupportFragmentManager();
        earthquakeFiltersDialog.show(fm, "earthquakeFilters");
    }

}
