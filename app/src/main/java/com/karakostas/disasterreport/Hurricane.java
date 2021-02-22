package com.karakostas.disasterreport;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;

@JsonDeserialize(using = HurricaneDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "hurricane_table")
public class Hurricane {
    @PrimaryKey
    @ColumnInfo(name = "SID")
    @NonNull
    String SID;
    String name;
    boolean isActive;
    int maxSpeed;
    long firstActive;
    long lastActive;

    @TypeConverters(HurricaneConverters.class)
    ArrayList<DataPoints> dataPointsList;

    public Hurricane(@NonNull String SID, String name, ArrayList<DataPoints> dataPointsList, boolean isActive, int maxSpeed, long firstActive, long lastActive) {
        this.SID = SID;
        this.dataPointsList = dataPointsList;
        this.name = name;
        this.isActive = isActive;
        this.maxSpeed = maxSpeed;
        this.firstActive = firstActive;
        this.lastActive = lastActive;
    }

    public static DiffUtil.ItemCallback<Hurricane> DIFF_CALLBACK = new DiffUtil.ItemCallback<Hurricane>() {
        @Override
        public boolean areItemsTheSame(@NonNull Hurricane oldItem, @NonNull Hurricane newItem) {
            return oldItem.getSID().equals(newItem.getSID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Hurricane oldItem, @NonNull Hurricane newItem) {
            return oldItem.getDataPointsList().size() == newItem.getDataPointsList().size();
        }
    };

    @NonNull
    public String getSID() {
        return SID;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public long getFirstActive() {
        return firstActive;
    }

    public long getLastActive() {
        return lastActive;
    }

    public ArrayList<DataPoints> getDataPointsList() {
        return dataPointsList;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class DataPoints {

    float lat;
    float lon;
    long time;
    int stormSpeed;
    int windSpeed;
    int cat;
    int dist2land;

    private DataPoints() {
    }

    public DataPoints(float lat, float lon, long time, int stormSpeed, int windSpeed, int cat, int dist2land) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
        this.stormSpeed = stormSpeed;
        this.windSpeed = windSpeed;
        this.cat = cat;
        this.dist2land = dist2land;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public long getTime() {
        return time;
    }

    public int getStormSpeed() {
        return stormSpeed;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public int getCat() {
        return cat;
    }

    public int getDist2land() {
        return dist2land;
    }
}