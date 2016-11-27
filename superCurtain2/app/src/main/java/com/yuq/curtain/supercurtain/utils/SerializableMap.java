package com.yuq.curtain.supercurtain.utils;

import java.io.Serializable;
import java.util.Map;

public class SerializableMap implements Serializable {

    private Map<String,String> map;

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}