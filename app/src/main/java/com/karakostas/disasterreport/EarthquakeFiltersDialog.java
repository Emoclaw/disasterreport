package com.karakostas.disasterreport;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EarthquakeFiltersDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener, View.OnClickListener, OnMapReadyCallback, OnSuccessListener<Location> {
    MapView mapView;
    GoogleMap gMap;
    private double km;
    private double selectedMinMag;
    private double selectedMaxMag;
    private Circle circle;
    private EarthquakeFiltersDialogCompletedListener mListener;
    private Context mContext;
    private int selectedDateRadio;
    private int dateSwitch;
    private ImageView infoImageView;
    private TextView startDateTextView;
    private TextView endDateTextView;
    private long startDate;
    private long endDate;
    private double mMaxRadius = 180;
    private double mLatitude;
    private double mLongitude;
    private FusedLocationProviderClient fusedLocationClient;

    public static EarthquakeFiltersDialog newInstance(String title) {
        EarthquakeFiltersDialog frag = new EarthquakeFiltersDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        infoImageView = view.findViewById(R.id.info_imageView);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.setClickable(false);
        mapView.getMapAsync(this);
        startDateTextView = view.findViewById(R.id.start_date_textView);
        endDateTextView = view.findViewById(R.id.end_date_textView);
        mListener = (EarthquakeFiltersDialogCompletedListener) getActivity();
        Log.d(mContext.toString(), Double.toString(selectedMinMag));

        startDate = System.currentTimeMillis() - 86400000L;
        endDate = System.currentTimeMillis() + 86400000L;
        TextView startDateTextView = view.findViewById(R.id.start_date_textView);
        TextView endDateTextView = view.findViewById(R.id.end_date_textView);
        final TextView minMagTextView = view.findViewById(R.id.min_mag_textView);
        final TextView maxMagTextView = view.findViewById(R.id.max_mag_textView);
        RadioButton Last24HoursRadio = view.findViewById(R.id.last_24_earthquake);
        RadioButton thisWeekRadio = view.findViewById(R.id.this_week_earthquake);
        RadioButton CustomRadio = view.findViewById(R.id.custom_earthquake);
        RadioGroup radioGroup = view.findViewById(R.id.date_radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.last_24_earthquake:
                        selectedDateRadio = 0;
                        startDate = System.currentTimeMillis() - 86400000L;
                        endDate = System.currentTimeMillis() + 86400000L;
                        startDateTextView.setVisibility(View.INVISIBLE);
                        endDateTextView.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.this_week_earthquake:
                        selectedDateRadio = 1;
                        startDate = System.currentTimeMillis() - 7 * 86400000L;
                        endDate = System.currentTimeMillis() + 86400000L;
                        startDateTextView.setVisibility(View.INVISIBLE);
                        endDateTextView.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.custom_earthquake:
                        selectedDateRadio = 2;
                        startDateTextView.setVisibility(View.VISIBLE);
                        endDateTextView.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        Button button = view.findViewById(R.id.earthquake_filters_ok_button);
        final SeekBar minMagSeekBar = view.findViewById(R.id.min_mag_seekbar);
        final SeekBar maxMagSeekBar = view.findViewById(R.id.max_mag_seekbar);
        final SeekBar distanceSeekBar = view.findViewById(R.id.distance_seekbar);
        final TextView distanceTextView = view.findViewById(R.id.distance_textView);
        final DecimalFormat dec = new DecimalFormat("#0.00");
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                km = i * 111.12 / 4;

                distanceTextView.setText("Distance: " + dec.format(km) + " km");
                mMaxRadius = i / 4.0;
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
        if (args != null) {
            selectedMinMag = args.getDouble("minMagPos");
            selectedMaxMag = args.getDouble("maxMagPos");
            selectedDateRadio = args.getInt("dateRadio");
            startDate = args.getLong("startDate");
            endDate = args.getLong("endDate");
            mLongitude = args.getDouble("longitude");
            mLatitude = args.getDouble("latitude");
            mMaxRadius = args.getDouble("maxradius");
            km = mMaxRadius * 1000;
            distanceSeekBar.setProgress((int) mMaxRadius * 4);
            j = (int) selectedMinMag * 10;
            minMagSeekBar.setProgress(j);
            minMagTextView.setText(Double.toString(selectedMinMag));
            j = (int) selectedMaxMag * 10;
            maxMagSeekBar.setProgress(j);
            maxMagTextView.setText(Double.toString(selectedMaxMag));
            switch (selectedDateRadio) {
                case 0:
                    Last24HoursRadio.setChecked(true);
                    break;
                case 1:
                    thisWeekRadio.setChecked(true);
                    break;
                case 2:
                    DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
                    CustomRadio.setChecked(true);
                    String startDateString = format.format(new Date(startDate));
                    String endDateString = format.format(new Date(endDate));
                    startDateTextView.setVisibility(View.VISIBLE);
                    endDateTextView.setVisibility(View.VISIBLE);
                    startDateTextView.setText(startDateString);
                    endDateTextView.setText(endDateString);

                    break;

            }
            //Adjust DatePicker visibilities based on whether 'Custom' date is selected
            if (!CustomRadio.isChecked()) {
                startDateTextView.setVisibility(View.INVISIBLE);
                endDateTextView.setVisibility(View.INVISIBLE);
            } else {
                startDateTextView.setVisibility(View.VISIBLE);
                endDateTextView.setVisibility(View.VISIBLE);
            }
        }
        //Minimum Magnitude SeekBar Listener
        minMagSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set the max magnitude based on the progressbar
                selectedMinMag = (double) i / 10;
                //Prevent minMag SeekBar from going over maxMag SeekBar
                if (i >= maxMagSeekBar.getProgress()) {
                    minMagSeekBar.setProgress(maxMagSeekBar.getProgress());
                }
                //Set textView minMag value
                minMagTextView.setText(Double.toString(selectedMinMag));
                Log.d("minmag", Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //OK Button Listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Ensure that there is a date or a time-frame specified
                if (selectedDateRadio == 2 && (startDate == 0 || endDate == 0 || endDate < startDate)) {
                    Toast.makeText(mContext, "Date not set correctly!", Toast.LENGTH_LONG).show();
                } else {
                    //Pass the values to the MainActivity
                    mListener.onEarthquakeDialogComplete(selectedMinMag, selectedMaxMag, selectedDateRadio, startDate, endDate, mLatitude, mLongitude, mMaxRadius);
                    dismiss();
                }
            }
        });
        maxMagSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Set the max magnitude based on the progressbar
                selectedMaxMag = (double) i / 10;
                if (i <= minMagSeekBar.getProgress()) {
                    //prevent maxMagSeekBar from going below minMagSeekBar
                    maxMagSeekBar.setProgress(minMagSeekBar.getProgress());
                }
                //Set maxMag TextView
                maxMagTextView.setText(Double.toString(selectedMaxMag));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        startDateTextView.setOnClickListener(this);
        endDateTextView.setOnClickListener(this);

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

        return inflater.inflate(R.layout.earthquake_filters_dialog, container);

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar pickedDate = Calendar.getInstance();
        pickedDate.set(year, month, day);
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        String formattedDate = format.format(pickedDate.getTime());
        switch (dateSwitch) {
            case 0:
                startDateTextView.setText(formattedDate);
                startDate = pickedDate.getTimeInMillis();
                break;
            case 1:
                endDate = pickedDate.getTimeInMillis();
                endDateTextView.setText(formattedDate);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentManager fm = getChildFragmentManager();

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

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_date_textView:
                dateSwitch = 0;
                break;
            case R.id.end_date_textView:
                dateSwitch = 1;
                break;
        }
        showDatePickerDialog();
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
        infoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setTitle("Weird Map Circle");
                alertDialog.setMessage("Because the Earth is an imperfect sphere and the map is a flat rectangle, as the circle becomes " +
                        "bigger and approaches the poles, it becomes distorted. \n" +
                        "The Mercator projection is the standard projection for navigation maps because it represents any course as a straight segment. " +
                        "However its linear scale becomes infinitely large at the poles, and therefore it cannot show the polar areas.\n\n" +
                        "An example of its inaccuracy: Greenland appears to be just as large as Africa on the map, but in reality it's 14 times smaller.\n" +
                        "The distance filter works as expected numerically and is accurate.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onSuccess(Location location) {
        Location mLocation = location;
        if (mLocation != null) {
            mLatitude = mLocation.getLatitude();
            mLongitude = mLocation.getLongitude();
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
        LatLng locCoords = new LatLng(mLatitude, mLongitude);
        //Log.d("LOCATION",""+ mLatitude + mLongitude);
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

    //Create an interface to send data back to the MainActivity
    public interface EarthquakeFiltersDialogCompletedListener {
        void onEarthquakeDialogComplete(double selectedMinMag, double selectedMaxMag, int selectedDateRadio, long startDate, long endDate, double latitude, double longitude, double maxradius);
    }
}
