package com.karakostas.disasterreport;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        adapter = new HurricaneAdapter(mList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter.notifyDataSetChanged();
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
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectFields("SID","LAT","LON","DIST2LAND");
        settings.setSkipEmptyLines(true);
        settings.setNullValue("");
        CsvParser parser = new CsvParser(settings);
        try {
            list = parser.parseAll(new FileReader(data));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<String> hurricaneUniqueIds = new ArrayList<>();
        ArrayList<String[]> multipleDetailList = new ArrayList<>();

        for (int i = 2; i < list.size() - 1; i++){
            multipleDetailList.add(new String[]{list.get(i)[1],list.get(i)[2],list.get(i)[3]});
            if (!list.get(i)[0].equals(list.get(i+1)[0])){
                hurricaneUniqueIds.add(list.get(i)[0]);
                mList.add(new Hurricane(list.get(i)[0],multipleDetailList));
                multipleDetailList.clear();
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<File> loader) {

    }
}
