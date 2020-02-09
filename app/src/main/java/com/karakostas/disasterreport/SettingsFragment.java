package com.karakostas.disasterreport;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    SharedPreferences pref;
    Context mContext;
    public SettingsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context mContext = getActivity();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = getActivity();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference themePreference = findPreference("night_mode_switch");
        Preference notificationPreference = findPreference("notification_switch");
        Preference notificationFilterPreference = findPreference("notification_filters");
        assert notificationFilterPreference != null;
        assert notificationPreference != null;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);

        assert themePreference != null;
        themePreference.setOnPreferenceChangeListener((preference, newValue) ->{
            boolean a = Boolean.parseBoolean(newValue.toString());
            if (a){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            return true;
        });

        if (pref.getBoolean("notification_switch",false)){
            notificationFilterPreference.setEnabled(true);
        } else {
            notificationFilterPreference.setEnabled(false);
        }
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean a = Boolean.parseBoolean(newValue.toString());
            if (a){
                notificationFilterPreference.setEnabled(true);
                PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                        .addTag("notificationWorkTag")
                        .setInitialDelay(1,TimeUnit.MINUTES)
                        .build();
                WorkManager.getInstance(mContext).enqueueUniquePeriodicWork("notificationWork", ExistingPeriodicWorkPolicy.KEEP, work);

            } else {
                notificationFilterPreference.setEnabled(false);
                WorkManager.getInstance(mContext).cancelUniqueWork("notificationWork");
            }
            return true;
        });


        notificationFilterPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showDialog();
                return true;
            }
        });
    }
    private void showDialog() {

        NotificationFilterPreference earthquakeFiltersDialog = NotificationFilterPreference.newInstance("Title");
        FragmentManager fm = getActivity().getSupportFragmentManager();
        earthquakeFiltersDialog.show(fm, "earthquakeFilters");
    }

}
