package nz.co.govhack.tumbleweed.mapdrawer;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collection;

class PlaygroundIconRender extends DefaultClusterRenderer<PlaygroundMarker> {

    public PlaygroundIconRender(Context context, GoogleMap map, ClusterManager<PlaygroundMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(PlaygroundMarker item, MarkerOptions markerOptions) {
        // Draw a single playground
        markerOptions.icon(item.getIcon());
        //markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

/*

    @Override
    protected void onBeforeClusterRendered(Cluster<PlaygroundMarker> cluster, MarkerOptions markerOptions) {
        Collection<PlaygroundMarker> clust = cluster.getItems();
        PlaygroundMarker item = clust.iterator().next();
        markerOptions.snippet(cluster.getSize() + " playgrounds here !");
        super.onBeforeClusterItemRendered(item, markerOptions);
    }


    @Override
    protected void onBeforeClusterRendered(Cluster<PlaygroundMarker> cluster, MarkerOptions markerOptions) {
        // Draw multiple playground
        Collection<PlaygroundMarker> clust = cluster.getItems();
        PlaygroundMarker item = clust.iterator().next();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_playground));
        markerOptions.snippet(cluster.getSize() + " parcels here");
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }

*/

}

