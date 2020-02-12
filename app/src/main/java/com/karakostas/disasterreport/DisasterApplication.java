package com.karakostas.disasterreport;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class DisasterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (pref.getBoolean("night_mode_switch",false)){
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
