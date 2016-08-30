package nz.co.govhack.tumbleweed.mapdrawer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback,
         ClusterManager.OnClusterItemInfoWindowClickListener<PlaygroundMarker>,
         ClusterManager.OnClusterClickListener<PlaygroundMarker>{

    private GoogleMap mMap;
    private JSONArray parksJson;
    private UiSettings mUiSettings;

    private double defaultLat = -41;
    private double defaultLon = 174;
    private int defaultZoom = 9;

    private HashMap<Marker, Integer> mMarkers;
    private HashMap<PlaygroundMarker, Integer> mPlaygroundMarkers;
    private ClusterManager<PlaygroundMarker> mClusterManager;

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

        /* ask for location permission if need be cf https://developer.android.com/training/permissions/requesting.html */
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_PERMISSION_REQUEST_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

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
        mPlaygroundMarkers = new HashMap<>();
        setUpClusterer();

        // Add markers for each record in the database
        for(int i = 0; i < parksJson.length(); i++) {
            try {
                double lon = ((JSONObject)parksJson.get(i)).getDouble("long");
                double lat = ((JSONObject)parksJson.get(i)).getDouble("lat");
                int id = ((JSONObject)parksJson.get(i)).getInt("id");
                int items = ((JSONObject)parksJson.get(i)).getInt("nb_items");
                String name = ((JSONObject)parksJson.get(i)).getString("name");
                String address = ((JSONObject)parksJson.get(i)).getString("address");
                // String geocode_address = ((JSONObject)parksJson.get(i)).getString("geocode_address");

                LatLng location = new LatLng(lat, lon);
                MarkerOptions marker = new MarkerOptions()
                        .position(location)
                        .title(name)
                        .snippet(address).visible(false);

                Marker m = mMap.addMarker(marker);
                PlaygroundMarker pm = new PlaygroundMarker(lat, lon, name, items);
                mClusterManager.addItem(pm);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMarkers.put(m, id);
                mPlaygroundMarkers.put(pm, id);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //http://stackoverflow.com/questions/30958224/android-maps-utils-clustering-show-infowindow
            mMap.setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);
            mClusterManager.setOnClusterClickListener(this);


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

     /*
        // capture click events
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //int id = mMarkers.get(marker);
                int id = mMarkers.get(marker);

                Bundle b = new Bundle();
                b.putString("record_id", "" + id);

                Intent intent = new Intent(getActivity(), ViewRecordActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });*/
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

    private void setUpClusterer() {

        mClusterManager = new ClusterManager<PlaygroundMarker>(this.getContext(), getMyMap());
        mClusterManager.setRenderer(new PlaygroundIconRender(this.getContext(), mMap, mClusterManager));
        getMyMap().setOnCameraChangeListener(mClusterManager);


    }

    @Override
    public void onClusterItemInfoWindowClick(PlaygroundMarker pm) {
        int id = mPlaygroundMarkers.get(pm);

        Bundle b = new Bundle();
        b.putString("record_id", "" + id);

        Intent intent = new Intent(getActivity(), ViewRecordActivity.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public boolean onClusterClick(Cluster<PlaygroundMarker> cl) {
        Log.i("##################", "########################");
        Toast.makeText(getActivity(), "More playgrounds here!", Toast.LENGTH_LONG).show();
        return false;
    }

}

