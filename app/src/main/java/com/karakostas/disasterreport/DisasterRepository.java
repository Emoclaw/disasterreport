package com.karakostas.disasterreport;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DisasterRepository {
    private DisasterDao disasterDao;
    DisasterRepository(Application application) {
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

    public void insertEarthquake(Earthquake earthquake) {
        new insertEarthquakeAsyncTask(disasterDao).execute(earthquake);
    }
    public void insertHurricane(Hurricane hurricane) {
        new insertHurricaneAsyncTask(disasterDao).execute(hurricane);
    }
    LiveData<List<Hurricane>> getHurricanes(){
        return disasterDao.getHurricanes();
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

    private static class insertEarthquakeAsyncTask extends AsyncTask<Earthquake, Void, Void> {

        private DisasterDao mAsyncTaskDao;

        insertEarthquakeAsyncTask(DisasterDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Earthquake... params) {
            //Only insert Earthquake if contents are different. Requires OnConflictStrategy.REPLACE
            if (mAsyncTaskDao.getEarthquakeById(params[0].getId()) == null ||
                    mAsyncTaskDao.getEarthquakeById(params[0].getId()).getUpdatedDate() != params[0].getUpdatedDate())
                        mAsyncTaskDao.insertEarthquake(params[0]);
            return null;
        }
    }

    private static class insertHurricaneAsyncTask extends AsyncTask<Hurricane, Void, Void> {

        private DisasterDao mAsyncTaskDao;

        insertHurricaneAsyncTask(DisasterDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Hurricane... params) {
            //Only insert Hurricane if contents are different. Requires OnConflictStrategy.REPLACE
            if (mAsyncTaskDao.getHurricaneById(params[0].getSID()) == null ||
                    mAsyncTaskDao.getHurricaneById(params[0].getSID()).getLatitudeList().size() != params[0].getLatitudeList().size())
                        mAsyncTaskDao.insertHurricane(params[0]);
            return null;
        }
    }
}
