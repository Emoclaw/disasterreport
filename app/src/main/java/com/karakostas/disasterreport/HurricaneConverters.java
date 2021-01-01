package com.karakostas.disasterreport;

import androidx.room.TypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

public class HurricaneConverters {
    @TypeConverter
    public static ArrayList<DataPoints> fromString(String value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return new ArrayList<>(Arrays.asList(mapper.readValue(value, DataPoints[].class)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String fromDataPointsArrayList(ArrayList<DataPoints> list) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}