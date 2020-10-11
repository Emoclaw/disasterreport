package com.karakostas.disasterreport;

import android.Manifest;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    private static final String GROUP_EARTHQUAKE_NOTIFICATION_KEY = "com.karakostas.disasterreport.EARTHQUAKES";
    SharedPreferences pref;
    @NonNull
    @Override
    public Result doWork() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            WorkManager.getInstance(getApplicationContext()).cancelUniqueWork("notificationWork");
        }
        createNotificationChannel();
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        double mLongitude = Double.longBitsToDouble(pref.getLong("location_longitude",0));
        double mLatitude = Double.longBitsToDouble(pref.getLong("location_latitude",0));
        float mMaxRadius = pref.getFloat("max_radius_notification_filter",180);
        float minMag = pref.getFloat("min_mag_notification_filter",0);
        float maxMag = pref.getFloat("max_mag_notification_filter",11);
        DisasterDao dao = DisasterRoomDatabase.getDatabase(getApplicationContext()).earthquakeDao();
        long startDate = System.currentTimeMillis() - 4*3600000L;
        long endDate = System.currentTimeMillis() + 3600000L;
        TimeZone timeZone = TimeZone.getDefault();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        format.setTimeZone(timeZone);
        String startDateString = format.format(startDate);
        String endDateString = format.format(endDate);
        String latitudeString = Double.toString(mLatitude);
        String longitudeString = Double.toString(mLongitude);
        String data = DisasterUtils.getEarthquakeData(startDateString,endDateString,Float.toString(minMag),Float.toString(maxMag),latitudeString,longitudeString,Float.toString(mMaxRadius));
        List<Earthquake> mList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            final JSONArray earthquakesArray = jsonObject.getJSONArray("features");
            String location;
            for (int i = 0; i < earthquakesArray.length(); i++) {
                JSONObject earthquake = earthquakesArray.getJSONObject(i);
                JSONObject properties = earthquake.getJSONObject("properties");
                long timeInMs = properties.getLong("time");
                long updatedTimeInMs = properties.getLong("updated");
                location = properties.getString("place");
                double mag = properties.getDouble("mag");
                mag = Math.round(mag * 10) / 10d;
                String detailsURL = properties.getString("detail");
                String id = earthquake.getString("id");
                JSONObject geometry = earthquake.getJSONObject("geometry");
                JSONArray JSONCoordinates = geometry.getJSONArray("coordinates");
                double latitude = JSONCoordinates.getDouble(1);
                double longitude = JSONCoordinates.getDouble(0);
                double distanceFromUser = DisasterUtils.HaversineInKM(latitude, longitude, mLatitude, mLongitude);
                if (MainActivity.DEBUG_MODE)
                    Log.d("Coords", "Latitude: " + latitude + " Longitude: " + longitude + "\n UserLatitude: " + mLatitude + " UserLongitude: " + mLongitude);
                mList.add(new Earthquake(location,timeInMs,mag,detailsURL,id,latitude,longitude,distanceFromUser,updatedTimeInMs));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int count = 0;
        for (int i = mList.size() - 1; i >= 0; i--) {
            if (dao.getEarthquakeById(mList.get(i).getId()) == null) {
                count++;
                createNotification(mList.get(i).getLocation(), "A " + mList.get(i).getMag() + " earthquake has occurred", count,mList.get(i).getDate(),mList.get(i).getURL());
                dao.insertEarthquake(mList.get(i));
            }
        }
        return Result.success();
    }
    private void createNotification(String location, String title, int id,long time, String URL){
        Intent intent = new Intent(getApplicationContext(), EarthquakeInformationActivity.class);
        //Inflate the backstack so that we can return to MainActivity
        intent.putExtra("detailsURL", URL);
        intent.putExtra("Location", location);
        intent.putExtra("dateTime", time);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification earthquakeNotification =  new NotificationCompat.Builder(getApplicationContext(),"1")
                .setSmallIcon(R.drawable.mag_icon)
                .setContentTitle(title)
                .setContentText(location)
                .setWhen(time)
                .setSubText("Disaster Report")
                .setContentIntent(resultPendingIntent)
                .setGroup(GROUP_EARTHQUAKE_NOTIFICATION_KEY)
                .setPriority(NotificationCompat.PRIORITY_HIGH).build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

        Notification earthquakeSummary =
                new NotificationCompat.Builder(getApplicationContext(),"1")
                .setContentTitle("New earthquakes have occurred")
                .setSmallIcon(R.drawable.mag_icon)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setBigContentTitle(id +" new earthquakes")
                        .setSummaryText("Expand/Click for more details"))
                .setGroup(GROUP_EARTHQUAKE_NOTIFICATION_KEY)
                .setGroupSummary(true).build();

        manager.notify(id,earthquakeNotification);
        manager.notify(-1,earthquakeSummary);
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
