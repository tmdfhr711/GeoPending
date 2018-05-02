package com.plplim.david.geopending;

import java.util.HashMap;
import java.util.Map;

public class TrakingModel {
    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, TrakingInfo> traking = new HashMap<>();

    private class TrakingInfo {
        public String latitude;
        public String longitude;
        public String radius;

        public String currentLat;
        public String currentLong;
    }
}
