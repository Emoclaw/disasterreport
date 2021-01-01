package com.karakostas.disasterreport;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class HurricaneAdapter extends ListAdapter<Hurricane, HurricaneAdapter.ViewHolder> {
    boolean nightMode = false;
    private Context mContext;
    protected HurricaneAdapter() {
        super(Hurricane.DIFF_CALLBACK);
    }
    @NonNull
    @Override
    public HurricaneAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        nightMode = PreferenceManager.getDefaultSharedPreferences(parent.getContext()).getBoolean("night_mode_switch",false);
        View view = layoutInflater.inflate(R.layout.hurricane, parent, false);
        mContext = parent.getContext();
        return new HurricaneAdapter.ViewHolder(view, this);
    }


    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.gMap.clear();
        holder.gMap.setMapType(GoogleMap.MAP_TYPE_NONE);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hurricane hurricane = getItem(position);
        int color;
        String category;
        float speed = hurricane.getAverageSpeed();
        if (speed < 153){
            color = R.color.category_1;
            category = "Cat. 1";
        } else if (speed < 177) {
            color = R.color.category_2;
            category = "Cat. 2";
        } else if (speed < 208) {
            color = R.color.category_3;
            category = "Cat. 3";
        } else if (speed < 251) {
            color = R.color.category_4;
            category = "Cat. 4";
        } else {
            color = R.color.category_5;
            category = "Cat. 5";
        }
        String status = hurricane.isActive()?"(Active)":"(Inactive)";
        holder.nameTextView.setText(hurricane.getName());
        holder.dateTextView.setText(DisasterUtils.timeToString(hurricane.getStartTime()));
        holder.speedTextView.setTextColor(ContextCompat.getColor(mContext,color));
        holder.categoryTextView.setText(category + " " + status);
        //holder.speedUnitTextView.setTextColor(ContextCompat.getColor(mContext,color));
        holder.speedTextView.setText(String.format("%.1f",speed));
        holder.mapView.setTag(getItem(position));
        holder.setMapSettings();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        final TextView nameTextView;
        final TextView dateTextView;
        final TextView speedTextView;
        final TextView categoryTextView;
        //final TextView speedUnitTextView;
        private Context mContext;
        HurricaneAdapter mAdapter;
        int wh;
        MapView mapView;
        GoogleMap gMap;
        ViewHolder(@NonNull View itemView, HurricaneAdapter adapter) {
            super(itemView);
            mContext = itemView.getContext();
            mapView = itemView.findViewById(R.id.hurricane_mapview);
            mapView.setClickable(false);
            nameTextView = itemView.findViewById(R.id.name_hurricane);
            dateTextView = itemView.findViewById(R.id.date_textView_hurricane);
            speedTextView = itemView.findViewById(R.id.speed_hurricane);
            categoryTextView = itemView.findViewById(R.id.hurricane_category);
            //speedUnitTextView = itemView.findViewById(R.id.speed_unit_hurricane);
            mAdapter = adapter;

            //Glide uses px sizes, convert dp to px.
            wh = (int) (30 * Resources.getSystem().getDisplayMetrics().density);

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
                gMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(hurricane.getDataPointsList().get(hurricane.getDataPointsList().size()-1).getLat(), hurricane.getDataPointsList().get(hurricane.getDataPointsList().size()-1).getLat())));
                //Change marker icon based on whether Night Mode is enabled
                int drawable = mAdapter.nightMode ? R.drawable.ic_hurricane_orange : R.drawable.ic_hurricane;
                Glide.with(mContext).asBitmap().load(drawable).into(new CustomTarget<Bitmap>(wh,wh) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        gMap.addMarker(new MarkerOptions()
                                .position(new LatLng(hurricane.getDataPointsList().get(hurricane.getDataPointsList().size()-1).getLat(), hurricane.getDataPointsList().get(hurricane.getDataPointsList().size()-1).getLat()))
                                .icon(BitmapDescriptorFactory.fromBitmap(resource))
                                .anchor(0.5f,0.5f));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(ContextCompat.getColor(mContext,R.color.colorSecondary)).width(15);
                for (int i=0; i<hurricane.getDataPointsList().size();i++){
                    polylineOptions.add(new LatLng(hurricane.getDataPointsList().get(i).getLat(),hurricane.getDataPointsList().get(i).getLon()));
                }
                Polyline polyline = gMap.addPolyline(polylineOptions);
                //Set map back to normal, since it's set to none when recycled
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }


}
