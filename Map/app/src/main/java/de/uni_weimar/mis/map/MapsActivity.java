package de.uni_weimar.mis.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.ArraySet;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener,GoogleMap.OnCameraChangeListener {
    ///I search about this task from stack overFlow in this address: http://stackoverflow.com/questions/16097143/google-maps-android-api-v2-detect-long-click-on-map-and-add-marker-not-working
    private GoogleMap myMap;
    private EditText teMarker;
    private Set<String> locationMarkers;
    ArrayList<Marker> markers=new ArrayList<Marker>();
    private ArrayList<Polyline> polygons=new ArrayList<Polyline>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
                .findFragmentById(R.id.map);
        myMap = mySupportMapFragment.getMap();
        myMap.setOnMapLongClickListener(this);
        teMarker=(EditText )findViewById( R.id.teMarker);
        LoadSavedLocations();
        setOnCameraChangeListener(); // to add circles after camera change
    }
    private void LoadSavedLocations(){
        SharedPreferences sharedPref = getSharedPreferences("myLocation", MODE_PRIVATE);
        locationMarkers=new HashSet<String>();
        locationMarkers =sharedPref.getStringSet("locationMarkers", new HashSet<String>());
        for (String locationMark :locationMarkers
                ) {
            String[] locs=locationMark.split(" ");
            String title = locs[0].toString();
            double lat = Double.parseDouble(locs[1].toString());
            double lon = Double.parseDouble(locs[2].toString());
            LatLng point = new LatLng(lat,lon) ;
            // Drawing marker on the map
            Marker marker=myMap.addMarker(new MarkerOptions().position(point).title(title));
            markers.add(marker);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    private void setOnCameraChangeListener() {
       myMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            public void onCameraChange(CameraPosition position) {
               double distance=0.0;
                LatLngBounds bounds = myMap.getProjection().getVisibleRegion().latLngBounds;
                VisibleRegion vr = myMap.getProjection().getVisibleRegion();
                LatLng fd=bounds.southwest;

                for(Polyline pol:polygons){
                    pol.remove();
                }
                for (Marker marker : markers) {
                    if (!bounds.contains(marker.getPosition())) {
                        LatLng centerLoc=myMap.getCameraPosition().target;
                        polygons.add(  myMap.addPolyline(new PolylineOptions().add(marker.getPosition(),
                                centerLoc)));
                       // distance=getDistance(bounds,marker.getPosition()); // this will return the radius for the circle we want to draw
//                        polygons.add(myMap.addPolygon(
//                                new PolygonOptions()
//                                        .add(marker.getPosition(),distance, distance)
//                                        .strokeColor(Color.RED)
//                        ); // add a circle to map, marker used as center
                    }
                }



            }
        });
    }
    private double getDistance(LatLngBounds bound,LatLng pos)
    {
        double distance=0.0;
        ArrayList<LatLng> points=new ArrayList<LatLng>();
        //find 8 points on bounds
        //Bounds= currennt view bounds,  position= marker position
        points=getPoints(bound);
        distance=getDistance(points,pos);
        return distance;
    }
    private LatLng getNearestPosition(ArrayList<LatLng> points, LatLng position)
    {
        double distance=0.0;
        float[] dis=new float[8];
        LatLng pos=position;
        int counter=0;
        for(LatLng point:points){
           // LatLng centerPos=myMap.getMapCenter();
            Location.distanceBetween(point.latitude,point.longitude,position.latitude,position.longitude,dis);
            if(distance==0 || distance>dis[0]){
                distance=dis[0];
                pos=point;
            }
            counter++;
        }
        double lng=pos.longitude;
        double lat=pos.latitude;
//        if(pos.longitude<0)
//            lng -=50;
//        else
//            lng +=50;
//        if(pos.latitude<0)
//            lat -=50;
//        else
//            lat +=50;
        return new LatLng(lat,lng);
    }
    private double getDistance(ArrayList<LatLng> points, LatLng position) {
        double distance=0.0;
        float[] dis=new float[8];
        int pos=0;
        int counter=0;
        for(LatLng point:points){
          Location.distanceBetween(point.latitude,point.longitude,position.latitude,position.longitude,dis);
           if(distance==0 || distance>dis[0]){
                distance=dis[0];
                pos=counter;
            }
            counter++;
        }
        String title="";
        switch (pos) {
            case 0:
                title="northeast";
                break;
            case 1:
                title="southeast";
                break;
            case 2:
                title="southwest";
                break;
            case 3:
                title="northwest";
                break;
            case 4:
                title="north";
                break;
            case 5:
                title="east";
                break;
            case 6:
                title="south";
                break;
            case 7:
                title="west";
                break;
        }
        Toast.makeText(this,
                "Pos:"+title+" ;distance: "+distance, Toast.LENGTH_SHORT).show();
        return distance*1.03;
    }
    private ArrayList<LatLng> getPoints(LatLngBounds bounds) {
        ArrayList<LatLng> points=new ArrayList<LatLng>();
       points.add(bounds.southwest);
        points.add(bounds.northeast);
        return points;
    }

    @Override
    public void onMapLongClick(LatLng points) {

        String title=teMarker.getText().toString();
        if(!title.equals("")){
            // locationCount++;
            Marker marker=myMap.addMarker(new MarkerOptions().position(points).title(title));
            SharedPreferences.Editor editor = getSharedPreferences("myLocation", MODE_PRIVATE).edit();
            String locationMarkerStr=title+" "+points.latitude+" "+points.longitude;
            locationMarkers.add(locationMarkerStr);
            editor.putStringSet("locationMarkers", locationMarkers);
            editor.commit();
            teMarker.setText("");

            markers.add(marker);
        }
        else{
            Toast.makeText(this,
                    "Please enter marker name!", Toast.LENGTH_LONG).show();
        }

    }
    private class LocationMarker
    {
        public String Title;
        public double Latitude;
        public double Longitude;
    }

}
