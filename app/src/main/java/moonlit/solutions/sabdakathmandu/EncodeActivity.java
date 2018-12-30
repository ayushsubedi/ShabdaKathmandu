package moonlit.solutions.sabdakathmandu;


import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_BEVEL;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


public class EncodeActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, MapboxMap.OnCameraIdleListener, MapboxMap.OnCameraMoveStartedListener, View.OnClickListener{
    private MapView mapView;
    private TextView textViewDebug, textViewResult;
    private MapboxMap mapboxMap;
    private FeatureCollection featureCollection;
    private List<Point> current_pointList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_key));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        textViewDebug = findViewById(R.id.textview_debug);
        textViewResult = findViewById(R.id.textview_result);
        mapView.onCreate(savedInstanceState);
        FloatingActionButton loc_fab = findViewById(R.id.loc_fab);
        FloatingActionButton toggle_fab = findViewById(R.id.toggle_fab);
        mapView.getMapAsync(this);
        loc_fab.setOnClickListener(this);
        toggle_fab.setOnClickListener(this);
    }

    private void toggleStyle(){
        String url = mapboxMap.getStyleUrl();
        String sat_map = getResources().getString(R.string.mapbox_style_satellite);
        String nor_map = getResources().getString(R.string.mapbox_style_mapbox_streets);
        assert url != null;
        if (url.equals(sat_map)){
            mapboxMap.setStyle(nor_map);
        }
        else if (url.equals(nor_map)){
            mapboxMap.setStyle(sat_map);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // todo work on this
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "Please enable location.", Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate
            locationComponent.activateLocationComponent(this);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_GPS_NORTH);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            PermissionsManager permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            Toast.makeText(this, R.string.location_warning, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        EncodeActivity.this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setCompassMargins(0, (int) getResources().getDimension(R.dimen._100sdp),(int) getResources().getDimension(R.dimen.fab_margin),0);
        BuildingPlugin buildingPlugin = new BuildingPlugin(mapView, mapboxMap);
        buildingPlugin.setVisibility(true);
        enableLocationComponent();
        mapboxMap.addOnCameraMoveStartedListener(this);
        mapboxMap.addOnCameraIdleListener(this);
        setUpLineLayer();
    }

    /**
     * Sets up the source and layer for drawing the building outline
     */
    private void setUpLineLayer() {
        // Create an empty FeatureCollection
        featureCollection = FeatureCollection.fromFeatures(new Feature[] {});

        // Create a GeoJSONSource from the empty FeatureCollection
        GeoJsonSource geoJsonSource = new GeoJsonSource("source", featureCollection);
        mapboxMap.addSource(geoJsonSource);

        // Use runtime styling to adjust the look of the building outline LineLayer
        LineLayer lineLayer = new LineLayer("lineLayer", "source");
        lineLayer.withProperties(
                lineColor(ContextCompat.getColor(EncodeActivity.this, R.color.colorAccent)),
                lineWidth(3f),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_BEVEL)
        );
        mapboxMap.addLayer(lineLayer);
    }



    private void moveCamera(LatLng latLng){
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
    }

    private List<Point> getBuildingFeatureOutline() {
        // Retrieve the middle of the map
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(new LatLng(
                mapboxMap.getCameraPosition().target.getLatitude(),
                mapboxMap.getCameraPosition().target.getLongitude()
        ));

        List<Point> pointList = new ArrayList<>();

        // Check whether the map style has a building layer
        if (mapboxMap.getLayer("building") != null) {

            // Retrieve the building Feature that is displayed in the middle of the map
            List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "building");

            if (features.size() > 0) {
                Feature buildingFeature = features.get(0);

                // Build a list of Point objects from the building Feature's coordinates
                for (int i = 0; i < Objects.requireNonNull(((Polygon) Objects.requireNonNull(buildingFeature.geometry())).coordinates()).size(); i++) {
                    for (int j = 0;
                         j < Objects.requireNonNull(((Polygon) Objects.requireNonNull(buildingFeature.geometry())).coordinates()).get(i).size(); j++) {
                        pointList.add(Point.fromLngLat(
                                Objects.requireNonNull(((Polygon) Objects.requireNonNull(buildingFeature.geometry())).coordinates()).get(i).get(j).longitude(),
                                Objects.requireNonNull(((Polygon) Objects.requireNonNull(buildingFeature.geometry())).coordinates()).get(i).get(j).latitude()

                        ));
                    }
                }
                // Create a LineString from the list of Point objects
            }
        }
        return pointList;
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onCameraIdle() {
        // Update the data source used by the building outline LineLayer and refresh the map
        List<Point> pointList = getBuildingFeatureOutline();

        // Process 1: move the camera to center of the house
        if (current_pointList == null || pointList.size()==0) {
            current_pointList = pointList;
        } else if (!current_pointList.equals(pointList)) {
            LatLng houseCenter = Helper.houseCenter(pointList);
            moveCamera(houseCenter);
            current_pointList = pointList;
        }


        // Process 2: update the house outline
        featureCollection = FeatureCollection.fromFeatures(new Feature[]
                {Feature.fromGeometry(LineString.fromLngLats(pointList))});
        GeoJsonSource source = mapboxMap.getSourceAs("source");
        if (source != null) {
            source.setGeoJson(featureCollection);
        }

        // Process 3: display the name
        textViewResult.setText(Helper.encodeLocationToWords(this, mapboxMap.getCameraPosition().target));
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        textViewDebug.setText("");
        textViewResult.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loc_fab:
                enableLocationComponent();
                break;

            case R.id.toggle_fab:
                toggleStyle();
                break;
        }
    }
}