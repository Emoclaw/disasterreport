package com.karakostas.disasterreport;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class HurricaneViewModel extends AndroidViewModel {
    private DisasterRepository mRepository;
    private LiveData<List<Hurricane>> mHurricanes;
    private MutableLiveData<EarthquakeViewModel.earthquakeFilter> hurricaneFilter;
    public HurricaneViewModel(@NonNull Application application) {
        super(application);
        mRepository = new DisasterRepository(application);
        mHurricanes = getHurricanes();
    }
    LiveData<List<Hurricane>> getHurricanes() {
        return mRepository.getHurricanes();
    }

    void insert(Hurricane hurricane){
        mRepository.insertHurricane(hurricane);
    }
}
