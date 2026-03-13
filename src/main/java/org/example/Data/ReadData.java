package org.example.Data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ReadData {
    public static InstancesClass read(File file){
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InstancesClass instance;
        try {
            instance = om.readValue(file, InstancesClass.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }
}
