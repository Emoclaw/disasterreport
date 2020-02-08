package com.karakostas.disasterreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class EarthquakeAdapter extends ListAdapter<Earthquake, EarthquakeAdapter.ViewHolder> implements Filterable {
    private LayoutInflater mInflater;
    private Context mContext;

    protected EarthquakeAdapter() {
        super(Earthquake.DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public EarthquakeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        mContext = parent.getContext();
        View view = layoutInflater.inflate(R.layout.earthquake, parent, false);
        return new EarthquakeAdapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull EarthquakeAdapter.ViewHolder holder, int position) {
        Earthquake earthquake = getItem(position);
        String loc = earthquake.getLocation();
        if (loc.contains(" of ")) {
            String[] mLocation = loc.split(" of ");
            String relativeLocation = "(" + mLocation[0] + ")";
            holder.relativeLocationText.setText(relativeLocation);
            holder.locationText.setText(mLocation[1]);
        } else {
            holder.locationText.setText(loc);
            holder.relativeLocationText.setText("");
        }

        String mDate = DisasterUtils.timeToString(earthquake.getDate());
        holder.dateText.setText(mDate);
        String mMag = Double.toString(earthquake.getMag());
        holder.magText.setText(mMag);
        double mag = earthquake.getMag();
        int color;
        if (mag < 0) {
            color = R.color.mag0;
        } else if (mag < 0.5) {
            color = R.color.mag0_5;
        } else if (mag < 1) {
            color = R.color.mag1;
        } else if (mag < 1.5) {
            color = R.color.mag1_5;
        } else if (mag < 2) {
            color = R.color.mag2;
        } else if (mag < 2.5) {
            color = R.color.mag2_5;
        } else if (mag < 3) {
            color = R.color.mag3;
        } else if (mag < 3.5) {
            color = R.color.mag3_5;
        } else if (mag < 4.5) {
            color = R.color.mag4;
        } else if (mag < 5) {
            color = R.color.mag4_5;
        } else {
            color = R.color.mag5;
        }
        holder.magText.setTextColor(ContextCompat.getColor(mContext, color));
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView locationText;
        final TextView relativeLocationText;
        final TextView dateText;
        final TextView magText;
        final EarthquakeAdapter mEarthquakeAdapter;

        ViewHolder(@NonNull View itemView, EarthquakeAdapter adapter) {
            super(itemView);
            locationText = itemView.findViewById(R.id.location_textView_Earthquake);
            dateText = itemView.findViewById(R.id.date_textView_earthquake);
            relativeLocationText = itemView.findViewById(R.id.relative_earthquake_location);
            magText = itemView.findViewById(R.id.mag_Earthquake);
            this.mEarthquakeAdapter = adapter;
        }
    }
}
