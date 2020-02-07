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
    private EarthquakeRepository mRepository;
    private LiveData<List<Earthquake>> mAllEarthquakes;
    private LiveData<List<Earthquake>> mFilteredEarthquakes;
    private MutableLiveData<earthquakeFilter> earthquakeFilter;

    public EarthquakeViewModel(@NonNull Application application) {
        super(application);
        mRepository = new EarthquakeRepository(application);
        earthquakeFilter = new MediatorLiveData<>();
        mFilteredEarthquakes = Transformations.switchMap(earthquakeFilter, input -> mRepository.getFilteredEarthquakes(minMag, maxMag, startDate, endDate, circleRadius, searchQuery));

    }

    public void deleteAll() {
        mRepository.deleteAll();
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
        mRepository.insert(earthquake);
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
