package com.karakostas.disasterreport;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Earthquake.class}, version = 4, exportSchema = false)
public abstract class DisasterRoomDatabase extends RoomDatabase {
    private static DisasterRoomDatabase INSTANCE;
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    public static DisasterRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DisasterRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DisasterRoomDatabase.class, "disaster_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract EarthquakeDao earthquakeDao();

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final EarthquakeDao mDao;

        PopulateDbAsync(DisasterRoomDatabase db) {
            mDao = db.earthquakeDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate the database
            // when it is first created
            // mDao.deleteAll();
            return null;
        }
    }
}
