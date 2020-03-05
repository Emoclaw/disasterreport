package com.karakostas.disasterreport;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "earthquake_table")
public class Earthquake {
    public static DiffUtil.ItemCallback<Earthquake> DIFF_CALLBACK = new DiffUtil.ItemCallback<Earthquake>() {
        @Override
        public boolean areItemsTheSame(@NonNull Earthquake oldItem, @NonNull Earthquake newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Earthquake oldItem, @NonNull Earthquake newItem) {
            return oldItem.getUpdatedDate() == newItem.getUpdatedDate();
        }
    };
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String id;
    private long date;
    private double mag;
    private String location;
    private String URL;
    private Double latitude;
    private Double longitude;
    private Double distanceFromUser;

    public long getUpdatedDate() {
        return updatedDate;
    }

    private long updatedDate;

    public Earthquake(String location, long date, double mag, String URL, @NonNull String id, double latitude, double longitude, double distanceFromUser, long updatedDate) {
        this.date = date;
        this.mag = mag;
        this.URL = URL;
        this.location = location;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceFromUser = distanceFromUser;
        this.updatedDate = updatedDate;
    }

    long getDate() {
        return date;
    }

    double getMag() {
        return mag;
    }

    public String getLocation() {
        return location;
    }

    String getURL() {
        return URL;
    }


    @NonNull
    String getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDistanceFromUser() {
        return distanceFromUser;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this)
//            return true;
//
//        Earthquake Earthquake = (Earthquake) obj;
//        return Earthquake.getURL().equals(this.Earthquake.getURL()) && Earthquake.firstName == this.firstName;
//    }
}
