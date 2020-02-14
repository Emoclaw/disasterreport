package com.karakostas.disasterreport;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;



public class HurricaneFragment extends Fragment {


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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView_hurricane);
        List<Hurricane> mList = new ArrayList<>();
        mList.add(new Hurricane("123213123"));
        mList.add(new Hurricane("123213123"));
        mList.add(new Hurricane("123213123"));
        mList.add(new Hurricane("123213123"));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        HurricaneAdapter adapter = new HurricaneAdapter(mList);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter.notifyDataSetChanged();

    }
}
