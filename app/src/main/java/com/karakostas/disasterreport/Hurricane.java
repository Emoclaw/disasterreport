package com.karakostas.disasterreport;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.*;
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
    long startTime;
    boolean isActive;
    float lastSpeed;
    float averageSpeed;


    public ArrayList<DataPoints> getDataPointsList() {
        return dataPointsList;
    }
    @TypeConverters(HurricaneConverters.class)
    ArrayList<DataPoints> dataPointsList;


    public Hurricane(@NonNull String SID, String name, ArrayList<DataPoints> dataPointsList, boolean isActive, float averageSpeed) {
        this.SID = SID;
        this.dataPointsList = dataPointsList;
        this.name = name;
        this.startTime = dataPointsList.get(0).getTime();
        this.isActive = isActive;
        lastSpeed = dataPointsList.get(dataPointsList.size() - 1).getSpeed();
        this.averageSpeed = averageSpeed;
    }

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

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public float getLastSpeed() {
        return lastSpeed;
    }

    public long getStartTime() {
        return startTime;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)

class DataPoints {
    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public long getTime() {
        return time;
    }

    public int getSpeed() {
        return speed;
    }

    float lat;
    float lon;
    long time;
    int speed;
    private DataPoints(){}

    public DataPoints(float lat, float lon, long time, int speed) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
        this.speed = speed;
    }
}