package com.karakostas.disasterreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HurricaneAdapter extends ListAdapter<Hurricane, HurricaneAdapter.ViewHolder> {
    private Context mContext;
    private List<Hurricane> mList;
    protected HurricaneAdapter(List<Hurricane> list) {
        super(Hurricane.DIFF_CALLBACK);
        mList = list;
    }
    @Override
    public int getItemCount() {
        return mList.size();
    }
    @NonNull
    @Override
    public HurricaneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        mContext = parent.getContext();
        View view = layoutInflater.inflate(R.layout.hurricane, parent, false);
        return new HurricaneAdapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hurricane hurricane = mList.get(position);
        holder.textView.setText(hurricane.getSID());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final HurricaneAdapter mAdapter;
        ViewHolder(@NonNull View itemView, HurricaneAdapter adapter) {
            super(itemView);
            textView = itemView.findViewById(R.id.sid_hurricane);
            mAdapter = adapter;
        }
    }
}
