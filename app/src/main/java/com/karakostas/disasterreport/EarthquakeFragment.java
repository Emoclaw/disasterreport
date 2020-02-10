package com.karakostas.disasterreport;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class EarthquakeFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>, MainActivity.sendDataToFragment {
    private List<Earthquake> mList = new ArrayList<>();
    private Context mContext;
    private EarthquakeViewModel earthquakeViewModel;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EarthquakeAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    long startDate;
    long endDate;
    SharedPreferences earthquakeFilterPrefs;
    SharedPreferences pref;
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

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//        mSwipeRefreshLayout.setRefreshing(false);
        return networkInfo != null && networkInfo.isConnected();
    }

    private void fetchData(double minMag, double maxMag, long startDate, long endDate, double latitude, double longitude, float maxradius) {
        mSwipeRefreshLayout.setRefreshing(true);
        if (isConnected()) {
            Bundle queryBundle = new Bundle();
            String stringStartDate = "";
            String stringEndDate = "";
            TimeZone timeZone = TimeZone.getDefault();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            format.setTimeZone(timeZone);
            stringStartDate = format.format(new Date(startDate));
            stringEndDate = format.format(new Date(endDate));
            String minimumMagnitude = Double.toString(minMag);
            String maximumMagnitude = Double.toString(maxMag);
            String stringLatitude = Double.toString(latitude);
            String stringLongitude = Double.toString(longitude);
            String stringMaxRadius = Float.toString(maxradius);
            Log.d("COORDINATESRadius", stringMaxRadius);
            queryBundle.putString("startDate", stringStartDate);
            queryBundle.putString("endDate", stringEndDate);
            queryBundle.putString("minMag", minimumMagnitude);
            queryBundle.putString("maxMag", maximumMagnitude);
            queryBundle.putString("latitude", stringLatitude);
            queryBundle.putString("longitude", stringLongitude);
            queryBundle.putString("maxradius", stringMaxRadius);
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
                if (isConnected()) {
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

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> mLayoutManager.scrollToPosition(0));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new EarthquakeAdapter();
        long defaultStartDate = System.currentTimeMillis() - 86400000L;
        long defaultEndDate = System.currentTimeMillis() + 86400000L;
        earthquakeViewModel = new ViewModelProvider(this).get(EarthquakeViewModel.class);
        earthquakeFilterPrefs = mContext.getSharedPreferences("EarthquakeFilterPrefs",0);
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        double latitude = Double.longBitsToDouble(pref.getLong("location_latitude",0));
        double longitude = Double.longBitsToDouble(pref.getLong("location_longitude",0));
        float minMag = earthquakeFilterPrefs.getFloat("min_mag", 2);
        float maxMag = earthquakeFilterPrefs.getFloat("max_mag", 11);
        int dateRadio = earthquakeFilterPrefs.getInt("selected_date_radio",0);
        startDate = defaultStartDate;
        endDate = defaultEndDate;
        switch (dateRadio) {
            case 1:
                startDate = System.currentTimeMillis() - 7 * 86400000L;
                endDate = defaultEndDate;
                break;
            case 2:
                startDate = earthquakeFilterPrefs.getLong("start_date", defaultStartDate);
                endDate = earthquakeFilterPrefs.getLong("end_date", defaultEndDate);
                break;
        }
        float maxRadius = earthquakeFilterPrefs.getFloat("max_radius", 180);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            fetchData(minMag, maxMag, startDate, startDate, latitude, longitude, maxRadius);
            earthquakeViewModel.setFilters(minMag, maxMag, startDate, startDate, maxRadius, mSearchQuery);
        });
        if (latitude != 0 && longitude != 0){
            fetchData(minMag, maxMag, startDate, endDate, latitude, longitude, maxRadius);
        }
        earthquakeViewModel.setFilters(minMag, maxMag, startDate, endDate, maxRadius, mSearchQuery);
        earthquakeViewModel.getFilteredEarthquakes().observe(getViewLifecycleOwner(), earthquakes -> {
            mList = earthquakes;
            mAdapter.submitList(mList);
        });

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);


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
        String stringStartDate = null;
        String stringEndDate = null;
        String maxMag = null;
        String minMag = null;
        String latitude = null;
        String longitude = null;
        String maxradius = null;
        if (args != null) {
            stringStartDate = args.getString("startDate");
            stringEndDate = args.getString("endDate");
            minMag = args.getString("minMag");
            maxMag = args.getString("maxMag");
            latitude = args.getString("latitude");
            longitude = args.getString("longitude");
            maxradius = args.getString("maxradius");
        }
        return new EarthquakeLoader(mContext, stringStartDate, stringEndDate, minMag, maxMag, latitude, longitude, maxradius);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        if (data.contains("Error 400")) {
            Toast.makeText(mContext, "Current filter criteria has results that exceed the limit of USGS (20000). Try narrowing your filter range.", Toast.LENGTH_LONG).show();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            int i = 0;
            new Thread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    final JSONArray earthquakesArray = jsonObject.getJSONArray("features");
                    String location;
                    double userLatitude = Double.longBitsToDouble(pref.getLong("location_latitude",0));
                    double userLongitude = Double.longBitsToDouble(pref.getLong("location_longitude",0));
                    for (int i1 = 0; i1 < earthquakesArray.length(); i1++) {
                        JSONObject earthquake = earthquakesArray.getJSONObject(i1);
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
                        double distanceFromUser = DisasterUtils.HaversineInKM(latitude, longitude, userLatitude, userLongitude);
                        if (MainActivity.DEBUG_MODE)
                            Log.d("Coords", "Latitude: " + latitude + " Longitude: " + longitude + "\n UserLatitude: " + userLatitude + " UserLongitude: " + userLongitude);
                        earthquakeViewModel.insert(new Earthquake(location, timeInMs, mag, detailsURL, id, latitude, longitude, distanceFromUser));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).start();
            if (MainActivity.DEBUG_MODE) Log.v("tag", "Items: " + i);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
    String mSearchQuery="";
    @Override
    public void sendData(double minMag, double maxMag, long startDate, long endDate, double latitude, double longitude, float maxradius) {
        fetchData(minMag, maxMag, startDate, endDate, latitude, longitude, maxradius);
        earthquakeViewModel.setFilters(minMag, maxMag, startDate, endDate, maxradius, mSearchQuery);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            fetchData(minMag, maxMag, startDate, endDate, latitude, longitude, maxradius);
            earthquakeViewModel.setFilters(minMag, maxMag, startDate, endDate, maxradius, mSearchQuery);
        });
        mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
    }

    @Override
    public void sendEarthquakeSearchQuery(String searchQuery) {
        mSearchQuery = searchQuery;
        earthquakeViewModel.search(mSearchQuery.toLowerCase());
    }

}
