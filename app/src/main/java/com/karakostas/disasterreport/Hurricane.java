package com.karakostas.disasterreport;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.List;
@Entity(tableName = "hurricane_table")
public class Hurricane {
    @PrimaryKey
    @ColumnInfo(name = "SID")
    @NonNull
    String SID;
    String name;
    String time;
    @TypeConverters(HurricaneConverters.class)
    ArrayList<String> timeList = new ArrayList<>();
    @TypeConverters(HurricaneConverters.class)
    ArrayList<Float> latitudeList = new ArrayList<>();
    @TypeConverters(HurricaneConverters.class)
    ArrayList<Float> longitudeList = new ArrayList<>();


    public Hurricane(@NonNull String SID, String name, ArrayList<Float> latitudeList, ArrayList<Float> longitudeList, ArrayList<String> timeList){
        this.SID = SID;
        this.latitudeList.addAll(latitudeList);
        this.name = name;
        this.longitudeList.addAll(longitudeList);
        this.timeList.addAll(timeList);
        this.time = timeList.get(0);
    }

    @NonNull
    public String getSID() {
        return SID;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Float> getLatitudeList() {
        return latitudeList;
    }

    public ArrayList<Float> getLongitudeList() {
        return longitudeList;
    }

    public List<String> getTimeList() {
        return timeList;
    }

    public static DiffUtil.ItemCallback<Hurricane> DIFF_CALLBACK = new DiffUtil.ItemCallback<Hurricane>() {
        @Override
        public boolean areItemsTheSame(@NonNull Hurricane oldItem, @NonNull Hurricane newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Hurricane oldItem, @NonNull Hurricane newItem) {
            return false;
        }
    };



}
