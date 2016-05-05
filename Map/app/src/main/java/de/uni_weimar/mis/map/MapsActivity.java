package de.uni_weimar.mis.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.ArraySet;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener,GoogleMap.OnCameraChangeListener {
    ///I search about this task from stack overFlow in this address: http://stackoverflow.com/questions/16097143/google-maps-android-api-v2-detect-long-click-on-map-and-add-marker-not-working
    //
    private GoogleMap myMap;
    private EditText teMarker;
    private Set<String> locationMarkers;
    ArrayList<Marker> markers=new ArrayList<Marker>();
    private ArrayList<Circle> circles=new ArrayList<Circle>();
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
        setOnCameraChangeListener();
    }
    private void LoadSavedLocations(){
        SharedPreferences sharedPref = getSharedPreferences("myLocation", MODE_PRIVATE);
        locationMarkers=new HashSet<String>();
        locationMarkers =sharedPref.getStringSet("locationMarkers", new HashSet<String>());
        for (String locationMark :locationMarkers
                ) {
            //Load saved marker locations and titles
            String[] locs=locationMark.split(" ");
            String title = locs[0].toString();
            double lat = Double.parseDouble(locs[1].toString());
            double lon = Double.parseDouble(locs[2].toString());
            LatLng point = new LatLng(lat,lon) ;
            //Add marker on map
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
                LatLngBounds bounds = myMap.getProjection().getVisibleRegion().latLngBounds;
                //Find center of screen
                Location boundCenter = new Location(String.valueOf(bounds.getCenter()));
               //remove all of old circles
                for(Circle circle:circles){
                    circle.remove();
                }
                //Check all markers if it is out of the bounds
                for (Marker marker : markers) {
                    // change marker position from  LatLng to Location
                    LatLng markerPos=marker.getPosition();
                    if (!bounds.contains(markerPos)) {
                        Location markerLoc = new Location(markerPos.toString());
                        markerLoc.setLatitude(markerPos.latitude);
                        markerLoc.setLongitude(markerPos.longitude);
                        circles.add(myMap.addCircle(new CircleOptions().center(markerPos).radius(
                                markerLoc.distanceTo(boundCenter) /4)));
                    }
                }



            }
        });
    }

    @Override
    public void onMapLongClick(LatLng points) {

        String title=teMarker.getText().toString();
        if(!title.equals("")){
            Marker marker=myMap.addMarker(new MarkerOptions().position(points).title(title));
            SharedPreferences.Editor editor = getSharedPreferences("myLocation", MODE_PRIVATE).edit();
            //Add information with a space character
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

}
