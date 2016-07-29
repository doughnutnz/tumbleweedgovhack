package com.example.stefan.mapdrawer;

import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static String loadJSONFromAsset(AssetManager assets) {
        String json;
        try {
            InputStream is = assets.open("parks_geocoded.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
