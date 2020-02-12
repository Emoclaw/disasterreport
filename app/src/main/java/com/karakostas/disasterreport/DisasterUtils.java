package com.karakostas.disasterreport;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

class DisasterUtils {


    static final double EQUATORIAL_EARTH_RADIUS = 6378.1370D;
    static final double DEGREES_TO_RADIANS = (Math.PI / 180D);
    private static final String EARTHQUAKE_BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
    private static final String PARAM_STARTTIME = "starttime";
    private static final String PARAM_ENDTIME = "endtime";
    private static final String PARAM_MINMAG = "minmag";
    private static final String PARAM_MAXMAG = "maxmag";
    private static final String PARAM_LATITUDE = "latitude";
    private static final String PARAM_LONGITUDE = "longitude";
    private static final String PARAM_MAXRADIUS = "maxradius";

    private static final String HURRICANE_URL = "https://www.ncei.noaa.gov/data/international-best-track-archive-for-climate-stewardship-ibtracs/v04r00/access/csv/ibtracs.ACTIVE.list.v04r00.csv";
    static String getEarthquakeData(String startTime, String endTime, String minMag, String maxMag, String latitude, String longitude, String maxradius) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String earthquakeJSONData;
        try {
            Uri builtURI = Uri.parse(EARTHQUAKE_BASE_URL).buildUpon()
                    .appendQueryParameter(PARAM_STARTTIME, startTime)
                    .appendQueryParameter(PARAM_ENDTIME, endTime)
                    .appendQueryParameter(PARAM_MINMAG, minMag)
                    .appendQueryParameter(PARAM_MAXMAG, maxMag)
                    .appendQueryParameter(PARAM_LATITUDE, latitude)
                    .appendQueryParameter(PARAM_LONGITUDE, longitude)
                    .appendQueryParameter(PARAM_MAXRADIUS, maxradius)
                    .build();
            URL requestURL = new URL(builtURI.toString());

            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            InputStream inputStream;
            if (code == 400 || code == 404 || code == 204) {
                inputStream = urlConnection.getErrorStream();
            } else {
                inputStream = urlConnection.getInputStream();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            if (builder.length() == 0) {
                return null;
            }
            earthquakeJSONData = builder.toString();
            if (MainActivity.DEBUG_MODE) Log.v("tag", earthquakeJSONData);
            return earthquakeJSONData;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static String getEarthquakeDetails(String URL) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String earthquakeDetailsJSONData;
        try {
            URL requestURL = new URL(URL);
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream;
            int code = urlConnection.getResponseCode();
            if (code == 400 || code == 404 || code == 204) {
                inputStream = urlConnection.getErrorStream();
            } else {
                inputStream = urlConnection.getInputStream();
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            if (builder.length() == 0) {
                return null;
            }
            earthquakeDetailsJSONData = builder.toString();
            Log.d("tag", earthquakeDetailsJSONData);
            return earthquakeDetailsJSONData;


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    static void getHurricaneData(Context context) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            URL requestURL = new URL(HURRICANE_URL);
            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            outputStream = new FileOutputStream(context.getFilesDir() + "/active.csv");

            byte data[] = new byte[4096];
            int count;
            while ((count = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    static String timeToString(long time) {
        String dateFinal;
        long currentTime = System.currentTimeMillis();
        TimeZone.setDefault(null);
        TimeZone tz = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        int hourInMillis = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        int minuteInMillis = calendar.get(Calendar.MINUTE) * 60 * 1000;
        int secondInMillis = calendar.get(Calendar.SECOND) * 1000;
        int millis = calendar.get(Calendar.MILLISECOND);
        DateFormat.getTimeInstance(DateFormat.SHORT);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        dateFormat.setTimeZone(tz);
        timeFormat.setTimeZone(tz);
        Date date = new Date(time);
        String exactTime = timeFormat.format(date);
        if (currentTime - time <= hourInMillis + minuteInMillis + secondInMillis + millis) {
            dateFinal = "Today " + exactTime;
        } else {
            dateFinal = dateFormat.format(date) + " - " + exactTime;
        }
        return dateFinal;
    }

    public static double HaversineInKM(double lat1, double long1, double lat2, double long2) {
        double dLong = (long2 - long1) * DEGREES_TO_RADIANS;
        double dLat = (lat2 - lat1) * DEGREES_TO_RADIANS;
        double a = Math.pow(Math.sin(dLat / 2D), 2D) + Math.cos(lat1 * DEGREES_TO_RADIANS) * Math.cos(lat2 * DEGREES_TO_RADIANS)
                * Math.pow(Math.sin(dLong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));

        return EQUATORIAL_EARTH_RADIUS * c;
    }
}
