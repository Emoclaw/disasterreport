package com.karakostas.disasterreport;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.AbstractRowProcessor;
import com.univocity.parsers.common.processor.ConcurrentRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;


public class HurricaneFragment extends Fragment implements LoaderManager.LoaderCallbacks<File> {
    List<String[]> list;
    List<Hurricane> mList = new ArrayList<>();
    private HurricaneViewModel hurricaneViewModel;
    Context mContext;
    HurricaneAdapter adapter;
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

        mRecyclerView.setHasFixedSize(true);
        adapter = new HurricaneAdapter();
        hurricaneViewModel = new ViewModelProvider(this).get(HurricaneViewModel.class);
        hurricaneViewModel.getHurricanes().observe(getViewLifecycleOwner(), earthquakes -> {
            mList = earthquakes;
            adapter.submitList(mList);
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        LoaderManager.getInstance(this).restartLoader(1, null, this);
    }

    @NonNull
    @Override
    public Loader<File> onCreateLoader(int id, @Nullable Bundle args) {
        return new HurricaneLoader(mContext);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<File> loader, File data) {

        //Read & parse the csv file
        //TODO: Determine needed columns
        CsvParserSettings settings = new CsvParserSettings();
        //settings.selectFields("SID","NAME","LAT","LON","ISO_TIME","DIST2LAND");
        settings.selectIndexes(0, 5, 8, 9, 6, 14);
        settings.setColumnReorderingEnabled(false);
        settings.setSkipEmptyLines(true);
        settings.setReadInputOnSeparateThread(true);
        settings.setNumberOfRowsToSkip(2);
        settings.setProcessor(new ConcurrentRowProcessor(new AbstractRowProcessor() {
            ArrayList<Float> latitudeList = new ArrayList<>(100);
            ArrayList<Float> longitudeList = new ArrayList<>(100);
            ArrayList<String> timeList = new ArrayList<>(100);
            ArrayList<String> sidList = new ArrayList<>(100);
            ArrayList<String> nameList = new ArrayList<>(100);
            ArrayList<LatLng> locationList = new ArrayList<>(100);
            int i = 0;

            @Override
            public void rowProcessed(String[] row, ParsingContext context) {
                sidList.add(row[0]);
                latitudeList.add(Float.parseFloat(row[8]));
                longitudeList.add(Float.parseFloat(row[9]));
                locationList.add(new LatLng(Float.parseFloat(row[8]), Float.parseFloat(row[9])));
                timeList.add(row[6]);
                nameList.add(row[5]);
                if (i != 0 && !row[0].equals(sidList.get(i - 1))) {
                    hurricaneViewModel.insert(new Hurricane(sidList.get(i - 1), nameList.get(i - 1), latitudeList, longitudeList, timeList));
                    latitudeList.clear();
                    longitudeList.clear();
                    timeList.clear();
                    sidList.clear();
                    nameList.clear();
                    i = 0;
                } else {
                    i++;
                }
            }
        }));
        CsvParser parser = new CsvParser(settings);
        parser.parse(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<File> loader) {

    }
}
