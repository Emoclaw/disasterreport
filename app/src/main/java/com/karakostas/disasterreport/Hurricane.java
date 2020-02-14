package com.karakostas.disasterreport;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hurricane_table")
public class Hurricane {

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
    public Hurricane(@NonNull String SID){
        this.SID = SID;
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
