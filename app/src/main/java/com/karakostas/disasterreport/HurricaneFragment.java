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
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

        Toast.makeText(mContext,"Load Complete",Toast.LENGTH_LONG).show();
        //Read the file, only needed columns
        //TODO: Determine needed columns
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectFields("SID","NAME","LAT","LON","ISO_TIME","DIST2LAND");
        settings.setSkipEmptyLines(true);
        settings.setNullValue("");
        CsvParser parser = new CsvParser(settings);
        try {
            list = parser.parseAll(new FileReader(data));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //Parse the file.
        //Because some cells are repeating in some columns (like SID, NAME), and some columns correspond to a certain
        // Hurricane - SID (column 0), we need to split them accordingly.
        //There might be a better way than using multiple lists.
        long time = System.nanoTime();
        ArrayList<String> hurricaneUniqueIds = new ArrayList<>();
        ArrayList<String> hurricaneUniqueNames = new ArrayList<>();
        ArrayList<Float> latitudeList = new ArrayList<>();
        ArrayList<Float> longitudeList = new ArrayList<>();
        ArrayList<String> timeList = new ArrayList<>();
        for (int i = 2; i < list.size() - 1; i++){
            //Store all non-repeating rows & columns which correspond to one Hurricane
            latitudeList.add(Float.parseFloat(list.get(i)[2]));
            longitudeList.add(Float.parseFloat(list.get(i)[3]));
            timeList.add(list.get(i)[4]);
            //If SID is different, store the Hurricane & its data in the POJO list and clear the data list.
            if (!list.get(i)[0].equals(list.get(i+1)[0])){
                hurricaneUniqueIds.add(list.get(i)[0]);
                hurricaneUniqueNames.add(list.get(i)[1]);
                hurricaneViewModel.insert(new Hurricane(list.get(i)[0],list.get(i)[1],latitudeList,longitudeList,timeList));
                latitudeList.clear();
                longitudeList.clear();
                timeList.clear();
            }
        }
        long elapsed = System.nanoTime() - time;
        Log.d("Benchmark"," Time " + elapsed +" ns");
    }

    @Override
    public void onLoaderReset(@NonNull Loader<File> loader) {

    }
}
