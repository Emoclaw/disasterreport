package com.karakostas.disasterreport;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;

public class EarthquakeRepository {
    private DisasterDao disasterDao;
    EarthquakeRepository(Application application) {
        DisasterRoomDatabase db = DisasterRoomDatabase.getDatabase(application);
        disasterDao = db.earthquakeDao();
    }


    LiveData<List<Earthquake>> getFilteredEarthquakes(double minMag, double maxMag, long startDate, long endDate, double circleRadius, String searchQuery) {
        LiveData<List<Earthquake>> mFilteredEarthquakes;
        if (searchQuery.equals("")) {
            mFilteredEarthquakes = disasterDao.getFilteredEarthquakes(minMag, maxMag, startDate, endDate, circleRadius);
        } else {
            mFilteredEarthquakes = disasterDao.getFilteredEarthquakesWithSearch(minMag, maxMag, startDate, endDate, circleRadius, searchQuery);
        }
        return mFilteredEarthquakes;
    }

    public void insert(Earthquake earthquake) {
        new insertAsyncTask(disasterDao).execute(earthquake);
    }

    private static class deleteAllEarthquakesAsyncTask extends AsyncTask<Void, Void, Void> {
        private DisasterDao mAsyncTaskDao;

        deleteAllEarthquakesAsyncTask(DisasterDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    public void deleteAll() {
        new deleteAllEarthquakesAsyncTask(disasterDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<Earthquake, Void, Void> {

        private DisasterDao mAsyncTaskDao;

        insertAsyncTask(DisasterDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Earthquake... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
