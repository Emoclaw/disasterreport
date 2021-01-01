package com.karakostas.disasterreport;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Earthquake.class, Hurricane.class}, version = 9, exportSchema = false)
public abstract class DisasterRoomDatabase extends RoomDatabase {
    private static DisasterRoomDatabase INSTANCE;
    public static DisasterRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DisasterRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DisasterRoomDatabase.class, "disaster_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract DisasterDao earthquakeDao();
}
