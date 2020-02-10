package com.karakostas.disasterreport;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements EarthquakeFiltersDialog.EarthquakeFiltersDialogCompletedListener {
    static final Boolean DEBUG_MODE = true;
    private SharedPreferences earthquakePrefs;
    private SharedPreferences.Editor earthquakePrefEditor;
    SharedPreferences pref;
    SharedPreferences.Editor defaultPrefEditor;
    Toolbar toolbar;

    private SearchView searchView;
    private MenuItem searchMenuItem;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private FragmentManager fm;
    private FusedLocationProviderClient fusedLocationClient;
    private sendDataToFragment s;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            float minMag = earthquakePrefs.getFloat("min_mag",2);
            float maxMag = earthquakePrefs.getFloat("max_mag",11);
            int selectedDateRadio = earthquakePrefs.getInt("selected_date_radio",0);
            long startDate = pref.getLong("start_date",System.currentTimeMillis() - 86400000L);
            long endDate = pref.getLong("end_date",System.currentTimeMillis() + 86400000L);
            float maxRadius = earthquakePrefs.getFloat("max_radius",180);
            showDialog(minMag,maxMag,selectedDateRadio,startDate,
                    endDate,maxRadius,
                    Double.longBitsToDouble(pref.getLong("location_latitude",0)),
                    Double.longBitsToDouble(pref.getLong("location_longitude",0)));
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(float minMag, float maxMag, int mSelectedDateRadio, long mStartDate, long mEndDate, float mMaxRadius, double mLatitude, double mLongitude) {
        EarthquakeFiltersDialog earthquakeFiltersDialog = EarthquakeFiltersDialog.newInstance("Title");
        Bundle args = new Bundle();
        args.putFloat("minMagPos", minMag);
        args.putFloat("maxMagPos", maxMag);
        args.putInt("dateRadio", mSelectedDateRadio);
        args.putLong("startDate", mStartDate);
        args.putLong("endDate", mEndDate);
        args.putFloat("maxradius", mMaxRadius);
        args.putDouble("latitude", mLatitude);
        args.putDouble("longitude", mLongitude);
        earthquakeFiltersDialog.setArguments(args);
        earthquakeFiltersDialog.show(fm, "earthquakeFilters");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_items, menu);

        searchMenuItem = menu.findItem(R.id.action_earthquake_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Search");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                MainActivity.this.s.sendEarthquakeSearchQuery(s);
                return false;
            }
        });
        if (searchView != null) {
            searchView.setIconified(true);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);
        drawerLayout.closeDrawer(Gravity.LEFT, false);
        drawerToggle.syncState();

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (pref.getBoolean("night_mode_switch",false)){
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder permissionDialog = new AlertDialog.Builder(MainActivity.this);
            permissionDialog.setMessage("Disaster Report needs to access your location in order to " +
                    "provide relative distance information of events, location-based notifications and " +
                    "precise location search filters. \n\nYour location is not transmitted or collected and is only " +
                    "used to create local map markers.");
            permissionDialog.setTitle("Location Permission");
            permissionDialog.setCancelable(false);
            permissionDialog.setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    0));
            permissionDialog.setNegativeButton("EXIT", (dialogInterface, i) -> finish());
            AlertDialog permissionExplanationDialog = permissionDialog.create();
            permissionExplanationDialog.show();

        } else {
            getLocationToPrefs(false);
        }
        fm = getSupportFragmentManager();


        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);

        earthquakePrefs = getApplicationContext().getSharedPreferences("EarthquakeFilterPrefs",0);
        earthquakePrefEditor = earthquakePrefs.edit();

        defaultPrefEditor = pref.edit();
        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        NavigationView navigationView = findViewById(R.id.navigation);
        setupDrawerContent(navigationView);
        selectDrawerItem(navigationView.getMenu().getItem(0));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableNotifications = sharedPref.getBoolean("notification_switch",false);
        if (enableNotifications) {
            PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                    .addTag("notificationWorkTag")
                    .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("notificationWork", ExistingPeriodicWorkPolicy.KEEP, work);
        } else {
            WorkManager.getInstance(this).cancelUniqueWork("notificationWork");
        }

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    MainActivity.this.selectDrawerItem(menuItem);
                    return true;
                });
    }

    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;

        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.earthquake_fragment:
                fragmentClass = EarthquakeFragment.class;
                break;
            case R.id.hurricane_fragment:
                fragmentClass = HurricaneFragment.class;
                break;
            case R.id.fire_fragment:
                fragmentClass = FireFragment.class;
                break;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                //Since a fragment isn't selected, do not specify a fragment.
                fragmentClass = null;
                break;
            default:
                fragmentClass = EarthquakeFragment.class;
        }
        try {
            if (fragmentClass != null) fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment, when a fragment is selected.
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragment != null) {
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            // Highlight the selected item
            menuItem.setChecked(true);
            drawerLayout.closeDrawer(Gravity.LEFT, false);
        }
        setTitle(menuItem.getTitle());
    }

    //Implement EarthquakeFiltersDialogCompletedListener's onEarthquakeDialogComplete method to receive the data
    @Override
    public void onEarthquakeDialogComplete(float selectedMinMag, float selectedMaxMag, int selectedDateRadio, long startDate, long endDate, double latitude, double longitude, float maxradius) {
        searchMenuItem.collapseActionView();
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }
        //Save the data using SharedPreferences. We can cast double to float here because precision isn't important.
        earthquakePrefEditor.putFloat("min_mag", selectedMinMag);
        earthquakePrefEditor.putFloat("max_mag", selectedMaxMag);
        earthquakePrefEditor.putInt("selected_date_radio",selectedDateRadio);
        earthquakePrefEditor.putLong("start_date",startDate);
        earthquakePrefEditor.putLong("end_date",endDate);
        earthquakePrefEditor.putFloat("max_radius",maxradius);
        earthquakePrefEditor.commit();
        //Send the data to the EarthquakeFragment
        s.sendData(selectedMinMag, selectedMaxMag, startDate, endDate, latitude, longitude, maxradius);

    }

    //Since we can't obtain fragment from viewPager, call this method from the fragment.
    void setSendDataToFragmentListener(sendDataToFragment listener) {
        this.s = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationToPrefs(true);
                } else {
                    Toast.makeText(MainActivity.this, "You need to enable Location permission. Exiting...", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void getLocationToPrefs(boolean fresh){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        //Since Editor doesn't have putDouble, convert Double to it's raw long bits
                        //We don't use putFloat as we can lose precision, and putString is inefficient
                        defaultPrefEditor.putLong("location_latitude",Double.doubleToRawLongBits(location.getLatitude()));
                        defaultPrefEditor.putLong("location_longitude",Double.doubleToRawLongBits(location.getLongitude()));
                        defaultPrefEditor.commit();
                        if (fresh)
                            s.sendData(2, 11, System.currentTimeMillis() - 86400000L, System.currentTimeMillis() + 86400000L, location.getLatitude(), location.getLongitude(), 180);

                    }
                });
    }
    //Create interface to send data from EarthquakeFiltersDialog, called from this activity, to a EarthquakeFragment
    public interface sendDataToFragment {
        void sendData(double z, double maxMag, long startDate, long endDate, double latitude, double longitude, float maxradius);
        void sendEarthquakeSearchQuery(String searchQuery);
    }
}
