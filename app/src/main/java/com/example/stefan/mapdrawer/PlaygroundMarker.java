package com.example.stefan.mapdrawer;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class PlaygroundMarker implements ClusterItem {
    private final LatLng mPosition;

    public PlaygroundMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }


    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_playground);
    }

    public String getSnippet() {
        return "Playground properties";
    }

    public String getTitle() {
        return "Playground";
    }

}


