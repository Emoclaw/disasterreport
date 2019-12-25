package com.karakostas.disasterreport;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class EarthquakeLoader extends AsyncTaskLoader<String> {
    private String mStartDate;
    private String mEndDate;
    private String mMinMag;
    private String mMaxMag;
    private String mLatitude;
    private String mLongitude;
    private String mMaxRadius;
    private String mURL;

    EarthquakeLoader(@NonNull Context context, String startDate, String endDate, String minMag, String maxMag, String latitude, String longitude, String maxradius) {
        super(context);
        mStartDate = startDate;
        mEndDate = endDate;
        mMinMag = minMag;
        mMaxMag = maxMag;
        mLatitude = latitude;
        mLongitude = longitude;
        mMaxRadius = maxradius;
    }

    EarthquakeLoader(@NonNull Context context, String URL) {
        super(context);
        mURL = URL;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        if (mURL == null) {
            return NetworkUtilities.getEarthquakeData(mStartDate, mEndDate, mMinMag, mMaxMag, mLatitude, mLongitude, mMaxRadius);
        } else {
            return NetworkUtilities.earthquakeDetails(mURL);
        }
    }
}
