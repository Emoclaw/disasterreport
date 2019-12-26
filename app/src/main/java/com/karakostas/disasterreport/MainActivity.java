package com.karakostas.disasterreport;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import icepick.State;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements EarthquakeFiltersDialog.EarthquakeFiltersDialogCompletedListener {
    public static final Boolean DEBUG_MODE = false;
    @State
    public double mLatitude;
    @State
    public double mLongitude;
    @State
    double minMag = 2;
    @State
    double maxMag = 11;
    @State
    int mSelectedDateRadio;
    @State
    long mStartDate;
    @State
    long mEndDate;
    @State
    double mMaxRadius = 180;
    @State
    String mSearchQuery;
    SearchView searchView;
    MenuItem searchMenuItem;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FragmentManager fm;
    private FusedLocationProviderClient fusedLocationClient;
    private sendDataToFragment s;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            showDialog();
        }
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        EarthquakeFiltersDialog earthquakeFiltersDialog = EarthquakeFiltersDialog.newInstance("Title");
        Bundle args = new Bundle();
        args.putDouble("minMagPos", minMag);
        args.putDouble("maxMagPos", maxMag);
        args.putInt("dateRadio", mSelectedDateRadio);
        args.putLong("startDate", mStartDate);
        args.putLong("endDate", mEndDate);
        args.putDouble("maxradius", mMaxRadius);
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
        searchView.setQueryHint("Type & Press search icon on your keyboard");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Toast like print

//                if(!searchView.isIconified()) {
//                    searchView.setIconified(true);
//                }
//                searchMenuItem.collapseActionView();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (s.equals("")) {
                    MainActivity.this.s.sendEarthquakeSearchQuery(s);
                } else {
                    MainActivity.this.s.sendEarthquakeSearchQuery(s);
                }
                return false;
            }
        });
        if (mSearchQuery != null && !mSearchQuery.equals("")) {
            searchView.setIconified(false);
            String tempQuery = mSearchQuery;
            searchMenuItem.expandActionView();

            searchView.clearFocus();
            searchView.setQuery(tempQuery, false);
        } else if (searchView != null) {
            searchView.setIconified(true);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawerLayout.closeDrawer(Gravity.LEFT, false);
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mStartDate = System.currentTimeMillis() - 86400000L;
        mEndDate = System.currentTimeMillis() + 86400000L;
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder permissionDialog = new AlertDialog.Builder(MainActivity.this);
            permissionDialog.setMessage("Disaster Report needs to access your location in order to " +
                    "provide relative distance information of events, location-based notifications and " +
                    "precise location search filters. \n\nYour location is not transmitted or collected and is only " +
                    "used to create local map markers.");
            permissionDialog.setTitle("Location Permission");
            permissionDialog.setCancelable(false);
            permissionDialog.setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0));
            permissionDialog.setNegativeButton("EXIT", (dialogInterface, i) -> finish());
            AlertDialog permissionExplanationDialog = permissionDialog.create();
            permissionExplanationDialog.show();

        }
        fm = getSupportFragmentManager();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);


        drawerToggle.syncState();
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView = findViewById(R.id.navigation);
        setupDrawerContent(navigationView);
        selectDrawerItem(navigationView.getMenu().getItem(0));
        getSupportActionBar().setHomeButtonEnabled(true);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableNotifications = sharedPref.getBoolean("notification_switch",false);
        if (enableNotifications) {
            Data locationData = new Data.Builder()
                    .putDouble("latitude",mLatitude)
                    .putDouble("longitude",mLongitude)
                    .build();
            PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                    .addTag("notificationWorkTag")
                    .setInputData(locationData)
                    .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork("notificationWork", ExistingPeriodicWorkPolicy.KEEP, work);
        } else {
            WorkManager.getInstance(this).cancelUniqueWork("notificationWork");
        }
    }

    public void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        MainActivity.this.selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
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
            // Highlight the selected item has been done by NavigationView
            menuItem.setChecked(true);
            drawerLayout.closeDrawer(Gravity.LEFT, false);
        }


        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer

    }

    //Implement EarthquakeFiltersDialogCompletedListener's onEarthquakeDialogComplete method to receive the data
    @Override
    public void onEarthquakeDialogComplete(double selectedMinMag, double selectedMaxMag, int selectedDateRadio, long startDate, long endDate, double latitude, double longitude, double maxradius) {
        minMag = selectedMinMag;
        maxMag = selectedMaxMag;
        mSelectedDateRadio = selectedDateRadio;
        mStartDate = startDate;
        mMaxRadius = maxradius;
        mLatitude = latitude;
        mLongitude = longitude;
        mEndDate = endDate;
        searchMenuItem.collapseActionView();
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }

        //Send the data to the EarthquakeFragment

        s.sendData(minMag, maxMag, mSelectedDateRadio, mStartDate, mEndDate, mLatitude, mLongitude, mMaxRadius, false);
        s.startFetching();

    }

    //Since we can't obtain fragment from viewPager, call this method from the fragment.
    public void setSendDataToFragmentListener(sendDataToFragment listener) {
        this.s = listener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, location -> {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    s.sendData(minMag, maxMag, mSelectedDateRadio, mStartDate, mEndDate, location.getLatitude(), location.getLongitude(), mMaxRadius, true);
                                } else {
                                    LocationManager mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                                            @Override
                                            public void onLocationChanged(Location location) {
                                                mLatitude = location.getLatitude();
                                                mLongitude = location.getLongitude();
                                            }

                                            @Override
                                            public void onStatusChanged(String s, int i, Bundle bundle) {

                                            }

                                            @Override
                                            public void onProviderEnabled(String s) {

                                            }

                                            @Override
                                            public void onProviderDisabled(String s) {

                                            }
                                        });
                                    }
                                }
                            });
                } else {
                    Toast.makeText(MainActivity.this, "You need to enable Location permission. Exiting...", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //Create interface to send data from DialogFragment, called from this activity, to a fragment
    public interface sendDataToFragment {
        void sendData(double z, double maxMag, int selectedDateRadio, long startDate, long endDate, double latitude, double longitude, double maxradius, boolean clearDatabase);

        void sendEarthquakeSearchQuery(String searchQuery);

        void startFetching();
    }
}
