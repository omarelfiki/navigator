package map;

import util.NetworkUtil;

public class MapProvider {
    private static MapIntegration instance;

    private MapProvider() {}

    public static synchronized MapIntegration getInstance() {
        if (instance == null) {
            boolean isOnline = NetworkUtil.isNetworkAvailable();
            instance = new MapIntegration(isOnline);
        }
        return instance;
    }
}
