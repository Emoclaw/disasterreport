package com.karakostas.disasterreport;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DisasterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEarthquake(Earthquake earthquake);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllEarthquakes(List<Earthquake> earthquakes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHurricane(Hurricane hurricane);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllHurricanes(List<Hurricane> hurricanes);

    @Query("SELECT * from earthquake_table WHERE mag >= :minMag AND mag <= :maxMag AND date >= :startDate AND date <= :endDate " +
            "AND :circleFilterRadius * 111.12 >= distanceFromUser ORDER BY date DESC ")
    LiveData<List<Earthquake>> getFilteredEarthquakes(double minMag, double maxMag, long startDate, long endDate, double circleFilterRadius);

    @Query("SELECT * from earthquake_table WHERE mag >= :minMag AND mag <= :maxMag AND date >= :startDate AND date <= :endDate " +
            "AND :circleFilterRadius * 111.12 >= distanceFromUser AND location LIKE '%'||:query||'%' ORDER BY date DESC ")
    LiveData<List<Earthquake>> getFilteredEarthquakesWithSearch(double minMag, double maxMag, long startDate, long endDate, double circleFilterRadius, String query);

    @Query("SELECT * from earthquake_table WHERE id == :id")
    Earthquake getEarthquakeById(String id);

    @Query("SELECT * from hurricane_table ORDER BY firstActive DESC")
    LiveData<List<Hurricane>> getHurricanes();

    @Query("SELECT * from hurricane_table WHERE SID == :id")
    Hurricane getHurricaneById(String id);


}
