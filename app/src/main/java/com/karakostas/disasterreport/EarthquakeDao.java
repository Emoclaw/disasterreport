package com.karakostas.disasterreport;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EarthquakeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Earthquake earthquake);

    @Query("DELETE FROM earthquake_table")
    void deleteAll();

    @Query("SELECT * from earthquake_table ORDER BY date DESC ")
    LiveData<List<Earthquake>> getAllEarthquakes();

    @Query("SELECT * from earthquake_table WHERE mag >= :minMag AND mag <= :maxMag AND date >= :startDate AND date <= :endDate " +
            "AND :circleFilterRadius * 111.12 >= distanceFromUser ORDER BY date DESC ")
    LiveData<List<Earthquake>> getFilteredEarthquakes(double minMag, double maxMag, long startDate, long endDate, double circleFilterRadius);

    @Query("SELECT * from earthquake_table WHERE mag >= :minMag AND mag <= :maxMag AND date >= :startDate AND date <= :endDate " +
            "AND :circleFilterRadius * 111.12 >= distanceFromUser AND location LIKE '%'||:query||'%' ORDER BY date DESC ")
    LiveData<List<Earthquake>> getFilteredEarthquakesWithSearch(double minMag, double maxMag, long startDate, long endDate, double circleFilterRadius, String query);


}
