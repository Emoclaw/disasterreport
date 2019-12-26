package com.karakostas.disasterreport;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;

public class EarthquakeRepository {
    private EarthquakeDao earthquakeDao;
    EarthquakeRepository(Application application) {
        DisasterRoomDatabase db = DisasterRoomDatabase.getDatabase(application);
        earthquakeDao = db.earthquakeDao();
    }


    LiveData<List<Earthquake>> getFilteredEarthquakes(double minMag, double maxMag, long startDate, long endDate, double circleRadius, String searchQuery) {
        LiveData<List<Earthquake>> mFilteredEarthquakes;
        if (searchQuery.equals("")) {
            mFilteredEarthquakes = earthquakeDao.getFilteredEarthquakes(minMag, maxMag, startDate, endDate, circleRadius);
        } else {
            mFilteredEarthquakes = earthquakeDao.getFilteredEarthquakesWithSearch(minMag, maxMag, startDate, endDate, circleRadius, searchQuery);
        }
        return mFilteredEarthquakes;
    }

    public void insert(Earthquake earthquake) {
        new insertAsyncTask(earthquakeDao).execute(earthquake);
    }

    private static class deleteAllEarthquakesAsyncTask extends AsyncTask<Void, Void, Void> {
        private EarthquakeDao mAsyncTaskDao;

        deleteAllEarthquakesAsyncTask(EarthquakeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public void deleteAll() {
        new deleteAllEarthquakesAsyncTask(earthquakeDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<Earthquake, Void, Void> {

        private EarthquakeDao mAsyncTaskDao;

        insertAsyncTask(EarthquakeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Earthquake... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
