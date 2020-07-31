package com.karakostas.disasterreport;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DisasterRepository {
    private final DisasterDao disasterDao;
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
            if (disasterDao.getEarthquakeById(earthquake.getId()) == null ||
                    disasterDao.getEarthquakeById(earthquake.getId()).getUpdatedDate() != earthquake.getUpdatedDate())
                disasterDao.insertEarthquake(earthquake);
        }

    public void insertAllEarthquakes(List<Earthquake> earthquakes) {
        disasterDao.insertAllEarthquakes(earthquakes);
    }

    public void insertHurricane(Hurricane hurricane) {
            if (disasterDao.getHurricaneById(hurricane.getSID()) == null ||
                    disasterDao.getHurricaneById(hurricane.getSID()).getLatitudeList().size() != hurricane.getLatitudeList().size())
                disasterDao.insertHurricane(hurricane);
    }
    public void insertAllHurricanes(List<Hurricane> hurricanes) {
        disasterDao.insertAllHurricanes(hurricanes);
    }

    LiveData<List<Hurricane>> getHurricanes(){
        return disasterDao.getHurricanes();
    }
}
