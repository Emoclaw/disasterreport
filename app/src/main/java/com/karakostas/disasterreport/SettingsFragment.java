package com.karakostas.disasterreport;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

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
        Preference notificationPreference = findPreference("notification_switch");
        assert notificationPreference != null;
        notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean a = Boolean.parseBoolean(newValue.toString());
            if (a){
                PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                        .addTag("notificationWorkTag")
                        .build();
                WorkManager.getInstance(mContext).enqueueUniquePeriodicWork("notificationWork", ExistingPeriodicWorkPolicy.KEEP, work);

            } else {
                WorkManager.getInstance(mContext).cancelUniqueWork("notificationWork");
            }
            return true;
        });
    }


}
