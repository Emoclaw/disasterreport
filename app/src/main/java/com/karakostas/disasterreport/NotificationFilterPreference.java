package com.karakostas.disasterreport;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;

public class NotificationFilterPreference extends DialogFragment implements OnMapReadyCallback, OnSuccessListener<Location> {
    MapView mapView;
    GoogleMap gMap;
    private float km;
    private float selectedMinMag;
    private float selectedMaxMag;
    private Circle circle;
    private Context mContext;
    private ImageView infoImageView;
    private float mMaxRadius = 180;
    private double mLatitude;
    private double mLongitude;
    private FusedLocationProviderClient fusedLocationClient;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static NotificationFilterPreference newInstance(String title) {
        NotificationFilterPreference frag = new NotificationFilterPreference();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();
        infoImageView = view.findViewById(R.id.info_imageView);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.setClickable(false);
        mapView.getMapAsync(this);
        if (MainActivity.DEBUG_MODE) Log.d(mContext.toString(), Float.toString(selectedMinMag));

        final TextView minMagTextView = view.findViewById(R.id.min_mag_textView);
        final TextView maxMagTextView = view.findViewById(R.id.max_mag_textView);

        Button button = view.findViewById(R.id.earthquake_filters_ok_button);
        final SeekBar minMagSeekBar = view.findViewById(R.id.min_mag_seekbar);
        final SeekBar maxMagSeekBar = view.findViewById(R.id.max_mag_seekbar);
        final SeekBar distanceSeekBar = view.findViewById(R.id.distance_seekbar);
        final TextView distanceTextView = view.findViewById(R.id.distance_textView);
        final DecimalFormat dec = new DecimalFormat("#0.00");
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                km = (float) (i * 111.12 / 4);

                distanceTextView.setText("Distance: " + dec.format(km) + " km");
                mMaxRadius = (float) (i / 4.0);
                if (i == 0) {
                    distanceTextView.setText("Distance: Max/No Filter");
                    mMaxRadius = 180;
                }
                if (circle != null) {
                    circle.setRadius(km * 1000);
                }
                if (km > 3000 && infoImageView.getVisibility() != View.VISIBLE) {
                    infoImageView.setVisibility(View.VISIBLE);
                } else if (infoImageView.getVisibility() != View.GONE && km <= 3000) {
                    infoImageView.setVisibility(View.INVISIBLE);
                }
                zoomMapAccordingly();
                //180 is the maximum radius so it's set back to no filter.
                if (i == 180 * 4) {
                    distanceSeekBar.setProgress(0);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Bundle args = getArguments();
        int j;
        //Restore filter dialog state

            selectedMinMag = pref.getFloat("min_mag_notification_filter",2);
            selectedMaxMag = pref.getFloat("max_mag_notification_filter",11);
            mLongitude = Double.longBitsToDouble(pref.getLong("location_longitude",0));
            mLatitude = Double.longBitsToDouble(pref.getLong("location_latitude",0));
            mMaxRadius = pref.getFloat("max_radius_notification_filter",180);
            km = mMaxRadius * 1000;
            distanceSeekBar.setProgress((int) mMaxRadius * 4);
            j = (int) selectedMinMag * 10;
            minMagSeekBar.setProgress(j);
            minMagTextView.setText(Float.toString(selectedMinMag));
            j = (int) selectedMaxMag * 10;
            maxMagSeekBar.setProgress(j);
            maxMagTextView.setText(Float.toString(selectedMaxMag));


        //Minimum Magnitude SeekBar Listener
        minMagSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set the max magnitude based on the progressbar
                selectedMinMag = (float) i / 10;
                //Prevent minMag SeekBar from going over maxMag SeekBar
                if (i >= maxMagSeekBar.getProgress()) {
                    minMagSeekBar.setProgress(maxMagSeekBar.getProgress());
                }
                //Set textView minMag value
                minMagTextView.setText(Float.toString(selectedMinMag));
                if (MainActivity.DEBUG_MODE) Log.d("minmag", Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //OK Button Listener
        button.setOnClickListener(view1 -> {
                editor.putFloat("max_mag_notification_filter",selectedMaxMag);
                editor.putFloat("min_mag_notification_filter",selectedMinMag);
                editor.putFloat("max_radius_notification_filter",mMaxRadius);
                editor.apply();
                dismiss();

        });
        maxMagSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set the max magnitude based on the progressbar
                selectedMaxMag = (float) i / 10;
                if (i <= minMagSeekBar.getProgress()) {
                    //prevent maxMagSeekBar from going below minMagSeekBar
                    maxMagSeekBar.setProgress(minMagSeekBar.getProgress());
                }
                //Set maxMag TextView
                maxMagTextView.setText(Float.toString(selectedMaxMag));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        return inflater.inflate(R.layout.earthquake_notification_filters_dialog, container);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void zoomMapAccordingly() {
        if (gMap != null) {
            if (km < 400) {
                gMap.moveCamera(CameraUpdateFactory.zoomTo(6));
            } else if (km < 1500) {
                gMap.moveCamera(CameraUpdateFactory.zoomTo(4));
            } else if (km < 2500) {
                gMap.moveCamera(CameraUpdateFactory.zoomTo(3));
            } else if (km < 5000){
                gMap.moveCamera(CameraUpdateFactory.zoomTo(1));
            } else {
                gMap.moveCamera(CameraUpdateFactory.zoomTo(0));
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.


        gMap.getUiSettings().setAllGesturesEnabled(false);
        gMap.getUiSettings().setScrollGesturesEnabled(false);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.setOnMapClickListener(null);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this);
        //Show a dialog that explains the weird circle on the map when it's too big, when the info icon is clicked
        infoImageView.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle("Weird Map Circle");
            alertDialog.setMessage("Because the Earth is an imperfect sphere and the map is a flat rectangle, as the circle becomes " +
                    "bigger and approaches the poles, it becomes distorted. \n" +
                    "The Mercator projection is the standard projection for navigation maps because it represents any course as a straight segment. " +
                    "However its linear scale becomes infinitely large at the poles, and therefore it cannot show the polar areas.\n\n" +
                    "An example of its inaccuracy: Greenland appears to be just as large as Africa on the map, but in reality it's 14 times smaller.\n" +
                    "The distance filter works as expected numerically and is accurate.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    (dialog, which) -> {

                dialog.dismiss();
                }
            );
            alertDialog.show();
        });
    }

    @Override
    public void onSuccess(Location location) {
        LatLng locCoords = new LatLng(mLatitude, mLongitude);
        gMap.addMarker(new MarkerOptions().position(locCoords).title("Your Location"));
        CircleOptions circleOptions = new CircleOptions()
                .center(locCoords)
                .strokeWidth(5)
                .strokeColor(ContextCompat.getColor(mContext, R.color.colorSecondary))
                .radius(000)
                .fillColor(ContextCompat.getColor(mContext, R.color.colorSecondary30));

// Get back the mutable Circle
        circle = gMap.addCircle(circleOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(locCoords));
        gMap.moveCamera(CameraUpdateFactory.zoomTo(6));
        circle.setRadius(km * 1000);
        zoomMapAccordingly();
    }
}
