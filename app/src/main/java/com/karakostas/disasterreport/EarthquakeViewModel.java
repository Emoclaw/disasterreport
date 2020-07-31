package com.karakostas.disasterreport;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;

import java.util.List;

public class EarthquakeViewModel extends AndroidViewModel {
    double minMag = 2, maxMag = 11;
    long startDate, endDate;
    double circleRadius;
    String searchQuery = "";
    private DisasterRepository mRepository;
    private LiveData<List<Earthquake>> mFilteredEarthquakes;
    private MutableLiveData<earthquakeFilter> earthquakeFilter;

    public EarthquakeViewModel(@NonNull Application application) {
        super(application);
        mRepository = new DisasterRepository(application);
        earthquakeFilter = new MediatorLiveData<>();
        mFilteredEarthquakes = Transformations.switchMap(earthquakeFilter, input -> mRepository.getFilteredEarthquakes(minMag, maxMag, startDate, endDate, circleRadius, searchQuery));

    }


    LiveData<List<Earthquake>> getFilteredEarthquakes() {
        return mFilteredEarthquakes;
    }

    void setFilters(double minMag, double maxMag, long startDate, long endDate, double circleRadius, String searchQuery) {
        this.circleRadius = circleRadius;
        this.minMag = minMag;
        this.maxMag = maxMag;
        this.startDate = startDate;
        this.endDate = endDate;
        this.searchQuery = searchQuery;
        earthquakeFilter filter = new earthquakeFilter(minMag, maxMag, startDate, endDate, searchQuery);
        earthquakeFilter.setValue(filter);
    }
    void search(String s){
        setFilters(minMag,maxMag,startDate,endDate, circleRadius,s);
    }
    void insert(Earthquake earthquake) {
        mRepository.insertEarthquake(earthquake);
    }
    void insertAll(List<Earthquake> earthquakes){
        mRepository.insertAllEarthquakes(earthquakes);
    }
    static class earthquakeFilter {
        final double minMag, maxMag;
        final long startDate, endDate;
        String searchQuery = "";

        earthquakeFilter(double minMag, double maxMag, long startDate, long endDate, String searchQuery) {
            this.minMag = minMag;
            this.maxMag = maxMag;
            this.startDate = startDate;
            this.endDate = endDate;
            this.searchQuery = searchQuery;
        }
    }
}
