package com.karakostas.disasterreport;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.maps.model.LatLng;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class HurricaneFragment extends Fragment{
    List<Hurricane> mList = new ArrayList<>();
    private HurricaneViewModel hurricaneViewModel;
    Context mContext;
    HurricaneAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public HurricaneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hurricane, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView_hurricane);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorAccent));
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(mContext,R.color.colorBackground));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {getHurricanes();});
        mRecyclerView.setHasFixedSize(true);
        adapter = new HurricaneAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mRecyclerView.scrollToPosition(0);
            }
        });
        hurricaneViewModel = new ViewModelProvider(this).get(HurricaneViewModel.class);
        hurricaneViewModel.getHurricanes().observe(getViewLifecycleOwner(), hurricanes -> {
            mList = hurricanes;
            adapter.submitList(mList);
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getHurricanes();
    }



    private void getHurricanes(){
        Single.fromCallable(() -> {
            DisasterUtils.getHurricaneData(mContext);
            return new File(mContext.getFilesDir() + "/active.csv");
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).subscribe(new SingleObserver<File>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull File file) {

                //Read & parse the csv file
                //TODO: Determine needed columns
                CsvParserSettings settings = new CsvParserSettings();
                //settings.selectFields(0 = "SID",5 = "NAME",6 = "ISO_TIME",8 = "LAT",9 = "LON",13 = "DIST2LAND",14 = "TRACK_TYPE",22 = "USA_STATUS",161 = "STORM_SPEED");
                settings.selectIndexes(0, 5,6, 8, 9, 13, 14, 22, 161);
                settings.setColumnReorderingEnabled(false);
                settings.setSkipEmptyLines(true);
                settings.setReadInputOnSeparateThread(true);
                settings.setNumberOfRowsToSkip(2);
                CsvParser parser = new CsvParser(settings);
                long time = System.nanoTime();
                List<String[]> mList = null;
                try {
                    mList = parser.parseAll(new FileReader(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ArrayList<Float> latitudeList = new ArrayList<>(100);
                ArrayList<Float> longitudeList = new ArrayList<>(100);
                ArrayList<String> timeList = new ArrayList<>(100);
                ArrayList<String> sidList = new ArrayList<>(100);
                ArrayList<String> nameList = new ArrayList<>(100);
                ArrayList<LatLng> locationList = new ArrayList<>(100);
                ArrayList<Float> speedList = new ArrayList<>(100);
                ArrayList<Hurricane> hurricanes = new ArrayList<>(100);
                assert mList != null;
                int i = 0;
                float sumSpeed = 0;
                for (i = 0; i < mList.size()-1; i++){
                    latitudeList.add(Float.parseFloat(mList.get(i)[8]));
                    longitudeList.add(Float.parseFloat(mList.get(i)[9]));
                    timeList.add(mList.get(i)[6]);
                    sidList.add(mList.get(i)[0]);
                    nameList.add(mList.get(i)[5]);
                    speedList.add((float) (Float.parseFloat(mList.get(i)[161]) * 1.852));
                    sumSpeed += (float) (Float.parseFloat(mList.get(i)[161]) * 1.852);
                    locationList.add(new LatLng(Float.parseFloat(mList.get(i)[8]),Float.parseFloat(mList.get(i)[9])));
                    if (!mList.get(i)[0].equals(mList.get(i+1)[0])){
                        float averageSpeed = sumSpeed/speedList.size();
                        sumSpeed = 0;
                        hurricanes.add(new Hurricane(mList.get(i)[0],mList.get(i)[5],latitudeList,longitudeList,timeList,speedList,false,averageSpeed));
                        latitudeList.clear();
                        longitudeList.clear();
                        timeList.clear();
                        sidList.clear();
                        nameList.clear();
                        speedList.clear();
                        locationList.clear();
                    }
                }
                //Insert the last Hurricane in the list
                float averageSpeed = sumSpeed/speedList.size();
                hurricanes.add(new Hurricane(mList.get(i)[0],mList.get(i)[5],latitudeList,longitudeList,timeList,speedList,false,averageSpeed));
                hurricaneViewModel.insertAll(hurricanes);
                time = System.nanoTime() - time;
                Log.d("Benchmark",time + " ns");
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }

        });
    }
}
