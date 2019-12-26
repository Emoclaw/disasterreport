package com.karakostas.disasterreport;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import icepick.State;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class EarthquakeFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>, MainActivity.sendDataToFragment {
    List<Earthquake> mList = new ArrayList<>();
    @State
    double minMag = 2;
    @State
    double maxMag = 11;
    @State
    long mStartDate = 0;
    @State
    long mEndDate = 0;
    @State
    double mMaxRadius = 180;
    @State
    double mLatitude;
    @State
    double mLongitude;
    private Context mContext;
    private DateFormat format;
    private EarthquakeViewModel earthquakeViewModel;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EarthquakeAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String mSearchQuery = "";

    public EarthquakeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//        mSwipeRefreshLayout.setRefreshing(false);
        return networkInfo != null && networkInfo.isConnected();
    }

    public void fetchData() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (isConnected(mContext)) {
            Bundle queryBundle = new Bundle();
            String startDate = "";
            String endDate = "";
            TimeZone timeZone = TimeZone.getDefault();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            format.setTimeZone(timeZone);
            startDate = format.format(new Date(mStartDate));
            endDate = format.format(new Date(mEndDate));
            String minimumMagnitude = Double.toString(minMag);
            String maximumMagnitude = Double.toString(maxMag);
            String latitude = Double.toString(mLatitude);
            String longitude = Double.toString(mLongitude);
            String maxradius = Double.toString(mMaxRadius);
            Log.d("COORDINATESRadius", maxradius);
            queryBundle.putString("startDate", startDate);
            queryBundle.putString("endDate", endDate);
            queryBundle.putString("minMag", minimumMagnitude);
            queryBundle.putString("maxMag", maximumMagnitude);
            queryBundle.putString("latitude", latitude);
            queryBundle.putString("longitude", longitude);
            queryBundle.putString("maxradius", maxradius);
            mRecyclerView.setVisibility(View.VISIBLE);
            LoaderManager.getInstance(this).restartLoader(0, queryBundle, this);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext, "Failed to get data. Check your Internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Bridge.restoreInstanceState(this, savedInstanceState);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView = view.findViewById(R.id.recyclerView_earthquake);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                //Toast.makeText(mContext,"Latitude: " + currentEarthquake.getLatitude() + " Longitude" + currentEarthquake.getLongitude(),Toast.LENGTH_LONG).show();
                if (isConnected(mContext)) {
                    Earthquake currentEarthquake = mList.get(position);
                    String detailsURL = currentEarthquake.getURL();
                    Intent intent = new Intent(mContext, EarthquakeInformationActivity.class);
                    intent.putExtra("detailsURL", detailsURL);
                    intent.putExtra("Location", currentEarthquake.getLocation());
                    intent.putExtra("dateTime", currentEarthquake.getDate());
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "You are offline!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mSwipeRefreshLayout = view.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary), ContextCompat.getColor(mContext, R.color.colorSecondary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
            }
        });
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(mRecyclerView.getContext()) {
//                    @Override
//                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//                        return 12f / displayMetrics.densityDpi;
//                    }
//                };
//                linearSmoothScroller.setTargetPosition(0);
//                mLayoutManager.startSmoothScroll(linearSmoothScroller);
                mLayoutManager.scrollToPosition(0);
            }

        });
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new EarthquakeAdapter();
        mStartDate = System.currentTimeMillis() - 86400000L;
        mEndDate = System.currentTimeMillis() + 86400000L;
        earthquakeViewModel = new ViewModelProvider(this).get(EarthquakeViewModel.class);
        earthquakeViewModel.setFilters(minMag, maxMag, mStartDate, mEndDate, mMaxRadius, "");
        earthquakeViewModel.getFilteredEarthquakes().observe(getViewLifecycleOwner(), new Observer<List<Earthquake>>() {
            @Override
            public void onChanged(List<Earthquake> earthquakes) {
                mList = earthquakes;
                mAdapter.submitList(mList);
                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                this.mLatitude = location.getLatitude();
                this.mLongitude = location.getLongitude();
            } else {
                LocationManager mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fetchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getContext();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setSendDataToFragmentListener(this);
        }
        return inflater.inflate(R.layout.fragment_earthquake, container, false);

    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String startDate = null;
        String endDate = null;
        String maxMag = null;
        String minMag = null;
        String latitude = null;
        String longitude = null;
        String maxradius = null;
        if (args != null) {
            startDate = args.getString("startDate");
            endDate = args.getString("endDate");
            minMag = args.getString("minMag");
            maxMag = args.getString("maxMag");
            latitude = args.getString("latitude");
            longitude = args.getString("longitude");
            maxradius = args.getString("maxradius");
        }
        return new EarthquakeLoader(mContext, startDate, endDate, minMag, maxMag, latitude, longitude, maxradius);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        if (data.contains("Error 400")) {
            Toast.makeText(mContext, "Current filter criteria has results that exceed the limit of USGS (20000). Try narrowing your filter range.", Toast.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            int i = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        final JSONArray earthquakesArray = jsonObject.getJSONArray("features");
                        String location;
                        for (int i = 0; i < earthquakesArray.length(); i++) {
                            JSONObject earthquake = earthquakesArray.getJSONObject(i);
                            JSONObject properties = earthquake.getJSONObject("properties");
                            long timeInMs = properties.getLong("time");
                            location = properties.getString("place");
                            double mag = properties.getDouble("mag");
                            mag = Math.round(mag * 10) / 10d;
                            String detailsURL = properties.getString("detail");
                            String id = earthquake.getString("id");
                            JSONObject geometry = earthquake.getJSONObject("geometry");
                            JSONArray JSONCoordinates = geometry.getJSONArray("coordinates");
                            double latitude = JSONCoordinates.getDouble(1);
                            double longitude = JSONCoordinates.getDouble(0);
                            double distanceFromUser = NetworkUtilities.HaversineInKM(latitude, longitude, mLatitude, mLongitude);
                            if (MainActivity.DEBUG_MODE)
                                Log.d("Coords", "Latitude: " + latitude + " Longitude: " + longitude + "\n UserLatitude: " + mLatitude + " UserLongitude: " + mLongitude);
                            earthquakeViewModel.insert(new Earthquake(location, timeInMs, mag, detailsURL, id, latitude, longitude, distanceFromUser));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            if (MainActivity.DEBUG_MODE) Log.v("tag", "Items: " + i);
        }

        // LoaderManager.getInstance(this).destroyLoader(0);
    }

    private void filterRecyclerView() {
//        mListCopy.clear();
//        mListCopy.addAll(mList);
//        filteredList.clear();
//        if (!mSearchQuery.equals("")) {
//            for (Earthquake earthquake : mList) {
//                if (earthquake.getLocation().toLowerCase().contains(mSearchQuery)) {
//                    filteredList.add(earthquake);
//                }
//            }
//           // mAdapter.updateEarthquakes(filteredList);
//            mListCopy.clear();
//            mListCopy.addAll(filteredList);
//            mAdapter.submitList(mListCopy);
//        } else {
////            filteredList.addAll(mListCopy);
////           // mAdapter.updateEarthquakes(mListCopy);
////            mList.clear();
////            mList.addAll(mListCopy);
//            mListCopy.clear();
//            mListCopy.addAll(mList);
//            mAdapter.submitList(mList);
//            mAdapter.notifyDataSetChanged();
//        }
        earthquakeViewModel.setFilters(minMag, maxMag, mStartDate, mEndDate, mMaxRadius, mSearchQuery);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override
    public void sendData(double minMag, double maxMag, int selectedDateRadio, long startDate, long endDate, double latitude, double longitude, double maxradius, boolean clearDatabase) {
        this.minMag = minMag;
        this.maxMag = maxMag;
        this.mStartDate = startDate;
        this.mEndDate = endDate;
        this.mMaxRadius = maxradius;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        if (clearDatabase) {
            earthquakeViewModel.deleteAll();
            fetchData();
        }
        earthquakeViewModel.setFilters(minMag, maxMag, startDate, endDate, maxradius, "");
        mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
    }

    @Override
    public void sendEarthquakeSearchQuery(String searchQuery) {
        mSearchQuery = searchQuery.toLowerCase();
        filterRecyclerView();
    }

    @Override
    public void startFetching() {
        fetchData();
    }

}
