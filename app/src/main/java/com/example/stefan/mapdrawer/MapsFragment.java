package com.example.stefan.mapdrawer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray parksJson;
    private UiSettings mUiSettings;

    private double defaultLat = -41;
    private double defaultLon = 174;
    private int defaultZoom = 9;

    private HashMap<Marker, Integer> mMarkers;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mLocationPermissionDenied = false;

    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_LAYER_PERMISSION_REQUEST_CODE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String json = Utils.loadJSONFromAsset(getActivity().getApplicationContext().getAssets());
        try {
            parksJson = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mUiSettings = mMap.getUiSettings();

        LatLng ll = new LatLng(defaultLat, defaultLon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, defaultZoom));

        // show current location
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMarkers = new HashMap<>();

        // Add markers for each record in the database
        for(int i = 0; i < parksJson.length(); i++) {
            try {
                double lon = ((JSONObject)parksJson.get(i)).getDouble("long");
                double lat = ((JSONObject)parksJson.get(i)).getDouble("lat");
                int id = ((JSONObject)parksJson.get(i)).getInt("id");
                String name = ((JSONObject)parksJson.get(i)).getString("name");
                String address = ((JSONObject)parksJson.get(i)).getString("address");
                // String geocode_address = ((JSONObject)parksJson.get(i)).getString("geocode_address");

                LatLng location = new LatLng(lat, lon);
                MarkerOptions marker = new MarkerOptions()
                        .position(location)
                        .title(name)
                        .snippet(address);

                Marker m = mMap.addMarker(marker);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

                mMarkers.put(m, id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        // set UI interface of the map
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);

        // capture click events
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int id = mMarkers.get(marker);

                Bundle b = new Bundle();
                b.putString("record_id", "" + id);

                Intent intent = new Intent(getActivity(), ViewRecordActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE) {
            // Enable the My Location button if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                mUiSettings.setMyLocationButtonEnabled(true);
            }

        } else if (requestCode == LOCATION_LAYER_PERMISSION_REQUEST_CODE) {
            // Enable the My Location layer if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                mMap.setMyLocationEnabled(true);
            } else {
                mLocationPermissionDenied = true;
            }
        }
    }

    public GoogleMap getMyMap() {
        return mMap;
    }
}
