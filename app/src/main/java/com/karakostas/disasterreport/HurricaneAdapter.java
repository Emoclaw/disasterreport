package com.karakostas.disasterreport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class HurricaneAdapter extends ListAdapter<Hurricane, HurricaneAdapter.ViewHolder> {
    boolean nightMode = false;
    protected HurricaneAdapter() {
        super(Hurricane.DIFF_CALLBACK);
    }
    @NonNull
    @Override
    public HurricaneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        nightMode = PreferenceManager.getDefaultSharedPreferences(parent.getContext()).getBoolean("night_mode_switch",false);
        View view = layoutInflater.inflate(R.layout.hurricane, parent, false);
        return new HurricaneAdapter.ViewHolder(view, this);
    }

    //Free map resources when recycled
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.gMap.clear();
        holder.gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hurricane hurricane = getItem(position);
        holder.nameTextView.setText(hurricane.getName());
        holder.dateTextView.setText(hurricane.getTimeList().get(0));
        holder.mapView.setTag(getItem(position));
        holder.setMapSettings();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        final TextView nameTextView;
        final TextView dateTextView;
        private Context mContext;
        HurricaneAdapter mAdapter;
        MapView mapView;
        GoogleMap gMap;
        ViewHolder(@NonNull View itemView, HurricaneAdapter adapter) {
            super(itemView);
            mContext = itemView.getContext();
            mapView = itemView.findViewById(R.id.hurricane_mapview);
            mapView.setClickable(false);
            nameTextView = itemView.findViewById(R.id.name_hurricane);
            dateTextView = itemView.findViewById(R.id.date_textView_hurricane);
            mAdapter = adapter;
            if (mapView != null){
                mapView.onCreate(null);
                mapView.getMapAsync(this);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            gMap = googleMap;
            setMapSettings();
        }

        private void setMapSettings(){
            if (gMap != null) {
                //Map UI
                gMap.getUiSettings().setAllGesturesEnabled(false);
                gMap.getUiSettings().setScrollGesturesEnabled(false);
                gMap.getUiSettings().setMapToolbarEnabled(false);
                if (mAdapter.nightMode)
                    gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mContext, R.raw.map_night));

                //Map info
                Hurricane hurricane = (Hurricane) mapView.getTag();
                gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(hurricane.getLatitudeList().get(0), hurricane.getLongitudeList().get(0))));
                Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_hurricane);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                BitmapDescriptor marker = BitmapDescriptorFactory.fromBitmap(smallMarker);
                gMap.addMarker(new MarkerOptions()
                        .position(new LatLng(hurricane.getLatitudeList().get(0), hurricane.getLongitudeList().get(0)))
                        .icon(marker)
                        .anchor(0.5f,0.5f));

                //Set map back to normal, since it's set to none when recycled
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }


}
