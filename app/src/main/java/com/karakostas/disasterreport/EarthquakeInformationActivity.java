package com.karakostas.disasterreport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.Math.exp;

public class EarthquakeInformationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, OnMapReadyCallback {
    String completeLocationString;
    double latitude;
    double longitude;
    private GoogleMap gMap;
    private Bundle queryBundle;
    private Button zoomOutButton;
    private Button zoomInButton;
    private String dateTimeString;
    private long dateTime;
    boolean nightMode = false;
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_information);
        Intent intent = getIntent();
        String URL = intent.getStringExtra("detailsURL");
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        nightMode = pref.getBoolean("night_mode_switch",false);
        dateTime = intent.getLongExtra("dateTime", 0);
        dateTimeString = DisasterUtils.timeToString(dateTime);
        MapView mapView = findViewById(R.id.mapView);
        mapView.setClickable(false);
        zoomInButton = findViewById(R.id.zoom_in_button);
        zoomOutButton = findViewById(R.id.zoom_out_button);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        String locationString = intent.getStringExtra("Location");
        completeLocationString = locationString;
        if (locationString != null) {
            locationString = locationString.replaceAll("\\d", "");
            locationString = locationString.replaceAll("\\([^()]*\\)", "");
            locationString = locationString.replaceAll("[()]", "");
            if (locationString.contains(" of ")) {
                String[] locationFin = locationString.split(" of ");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(locationFin[1]);
                }
            } else {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(locationString);
                }
            }
        }
        queryBundle = new Bundle();
        queryBundle.putString("URL", URL);

    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String URL = null;
        if (args != null) {
            URL = args.getString("URL");
        }
        return new EarthquakeLoader(this, URL);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if (MainActivity.DEBUG_MODE) Log.d("JSON", data);
        try {

            String jsonData = data.replace("depth-type", "depthtype");
            JSONObject earthquake = new JSONObject((jsonData));
            JSONObject properties = earthquake.getJSONObject("properties");
            JSONObject geometry = earthquake.getJSONObject("geometry");
            JSONArray JSONCoordinates = geometry.getJSONArray("coordinates");
            JSONObject products = properties.getJSONObject("products");
//            Log.e(this.toString(),products.toString());
//            JSONArray phaseDataArray = products.getJSONArray("phase-data");
            JSONArray originArray = products.getJSONArray("origin");
            JSONObject origin = originArray.getJSONObject(0);
            JSONObject originProperties = origin.getJSONObject("properties");
            String depth = originProperties.getString("depth");
            String magType = properties.getString("magType");
            String id = earthquake.getString("id");
            long updateTime = properties.getLong("updated");

            double mag = properties.getDouble("mag");


            mag = Math.round(mag * 10) / 10d;
            double radius = exp((mag / 1.01) - 0.13);
            if (mag > 9) {
                radius = radius * 0.4;
            } else if (mag > 8) {
                radius = radius * 0.5;
            } else if (mag > 7.5) {
                radius = radius * 0.6;
            }
            int height = 100;
            int width = 100;
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.earthquakemarker_darkred);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            BitmapDescriptor marker = BitmapDescriptorFactory.fromBitmap(smallMarker);
            LatLng coords = new LatLng(JSONCoordinates.getDouble(1), JSONCoordinates.getDouble(0));
            gMap.addMarker(new MarkerOptions().position(coords).title("Your Location").icon(marker));
            gMap.moveCamera(CameraUpdateFactory.newLatLng(coords));
            CircleOptions circle = new CircleOptions()
                    .center(coords)
                    .fillColor(ContextCompat.getColor(this, R.color.colorSecondary30))
                    .strokeColor(ContextCompat.getColor(this, R.color.colorSecondary))
                    .strokeWidth(3)
                    .radius(radius * 1000);
            gMap.addCircle(circle);
            CircleOptions smallerCircle = new CircleOptions()
                    .center(coords)
                    .fillColor(ContextCompat.getColor(this, R.color.veryRed30))
                    .strokeColor(ContextCompat.getColor(this, R.color.veryRed))
                    .strokeWidth(3)
                    .radius(radius * 400);
            gMap.addCircle(smallerCircle);
            gMap.moveCamera(CameraUpdateFactory.zoomBy(3));
            gMap.setMinZoomPreference(2);

            zoomInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gMap.moveCamera(CameraUpdateFactory.zoomIn());
                }
            });
            zoomOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gMap.getCameraPosition().zoom > 2) {
                        gMap.moveCamera(CameraUpdateFactory.zoomOut());
                    }
                }
            });
            ArrayList<String> list = new ArrayList<>();
            list.add(dateTimeString);
            list.add(completeLocationString + "\nLat: " + coords.latitude + ", Long: " + coords.longitude);
            if (MainActivity.DEBUG_MODE) {
                Log.d("COORDINATES ", "" + coords.latitude + coords.longitude);
                Log.d("COORDINATESDistance", "" + DisasterUtils.HaversineInKM(coords.latitude, coords.longitude, 40.6375225D, 22.9522647D));
            }
            list.add("Magnitude: " + mag + " " + magType);
            list.add("Depth: " + depth + " km");

            list.add("Tsunami Alert: No");
            list.add("USGS ID: " + id);
            list.add("Updated: " + calculateEpoch(updateTime));
            ArrayList<Integer> iconList = new ArrayList<>();
            iconList.add(R.drawable.ic_clock);
            iconList.add(R.drawable.ic_location);
            iconList.add(R.drawable.mag_icon);
            iconList.add(R.drawable.ic_depth);
            iconList.add(R.drawable.ic_tsunami);
            iconList.add(R.drawable.ic_hash);
            iconList.add(R.drawable.ic_update);
            EarthquakeDetailsAdapter adapter = new EarthquakeDetailsAdapter(getApplicationContext(), list, iconList, nightMode);
            ListView lv = findViewById(R.id.earthquake_listView);
            lv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LoaderManager.getInstance(this).destroyLoader(1);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.getUiSettings().setAllGesturesEnabled(false);
        gMap.getUiSettings().setScrollGesturesEnabled(false);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        if (nightMode){
            gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(),R.raw.map_night));
        }
        LoaderManager.getInstance(EarthquakeInformationActivity.this).restartLoader(1, queryBundle, this);
    }

    public String calculateEpoch(long time) {
        final long currentTime = System.currentTimeMillis();
        TimeZone.setDefault(null);
        TimeZone tz = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int hourInMillis = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        int minuteInMillis = calendar.get(Calendar.MINUTE) * 60 * 1000;
        int secondInMillis = calendar.get(Calendar.SECOND) * 1000;
        int millis = calendar.get(Calendar.MILLISECOND);

        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        dateFormat.setTimeZone(tz);
        timeFormat.setTimeZone(tz);
        Date date = new Date(time);
        String exactTime = timeFormat.format(date);
        if (currentTime - time <= hourInMillis + minuteInMillis + secondInMillis + millis) {
            return "Today " + exactTime;
        } else {
            return dateFormat.format(date) + " - " + exactTime;
        }
    }
}
