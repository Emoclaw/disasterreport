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
    List<String[]> multipleDetailList = new ArrayList<>();

    public Hurricane(@NonNull String SID, List<String[]> multipleDetailList){
        this.SID = SID;
        this.multipleDetailList.addAll(multipleDetailList);
    }
    public List<String[]> getMultipleDetailList() {
        return multipleDetailList;
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

    List<String> LAT;

    public List<String> getLAT() {
        return LAT;
    }


    @PrimaryKey
    @ColumnInfo(name = "SID")
    @NonNull
    String SID;

    @NonNull
    public String getSID() {
        return SID;
    }

}
