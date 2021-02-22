package com.karakostas.disasterreport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HurricaneDeserializer extends JsonDeserializer<Hurricane> {
    @Override
    public Hurricane deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = p.getCodec().readTree(p);
        String name = node.get("name").asText();
        String id = node.get("id").asText();
        int maxSpeed = node.get("maxSpeed").asInt();
        long firstActive = node.get("firstActive").asLong();
        long lastActive = node.get("lastActive").asLong();
        ArrayNode coordinates = (ArrayNode) node.get("datapoints");
        ArrayList<DataPoints> geopoints = new ArrayList<>(Arrays.asList(mapper.readValue(mapper.treeAsTokens(coordinates), DataPoints[].class)));
        boolean isActive = node.get("active").asBoolean();
        return new Hurricane(id,name, geopoints, isActive, maxSpeed, firstActive, lastActive);
    }
}
