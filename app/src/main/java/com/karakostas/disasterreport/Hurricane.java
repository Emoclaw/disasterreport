package com.karakostas.disasterreport;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "hurricane_table")
public class Hurricane {
    @PrimaryKey
    @ColumnInfo(name = "SID")
    @NonNull
    String SID;
    String name;
    List<Float> latitudeList = new ArrayList<>();
    List<Float> longitudeList = new ArrayList<>();
    List<String> timeList = new ArrayList<>();


    public Hurricane(@NonNull String SID, String name, List<Float> latitudeList, List<Float> longitudeList, List<String> timeList){
        this.SID = SID;
        this.latitudeList.addAll(latitudeList);
        this.name = name;
        this.longitudeList.addAll(longitudeList);
        this.timeList.addAll(timeList);
    }

    @NonNull
    public String getSID() {
        return SID;
    }

    public String getName() {
        return name;
    }

    public List<Float> getLatitudeList() {
        return latitudeList;
    }

    public List<Float> getLongitudeList() {
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
