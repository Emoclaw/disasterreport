package com.karakostas.disasterreport;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private EarthquakeRepository mRepository;

    @NonNull
    @Override
    public Result doWork() {
        long startDate = System.currentTimeMillis() - 86400000L;
        long endDate = System.currentTimeMillis() + 86400000L;

        TimeZone timeZone = TimeZone.getDefault();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        format.setTimeZone(timeZone);
        String startDateString = format.format(startDate);
        String endDateString = format.format(endDate);
        double mLatitude = (getInputData().getDouble("latitude",0));
        String latitudeString = Double.toString(mLatitude);
        double mLongitude = (getInputData().getDouble("longitude",0));
        String longitudeString = Double.toString(mLongitude);
        String data = NetworkUtilities.getEarthquakeData(startDateString,endDateString,"2.0","11.0",latitudeString,longitudeString,"180");

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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"1")
                .setSmallIcon(R.drawable.mag_icon)
                .setContentTitle("sdfasdf")
                .setContentText("asdgffdgsfg")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(1,builder.build());
        return Result.success();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", "name", importance);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
