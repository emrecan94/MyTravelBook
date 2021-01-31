package com.emrecanisik.mytravelbook.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.emrecanisik.mytravelbook.R;
import com.emrecanisik.mytravelbook.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    SQLiteDatabase database;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intentToMain=new Intent(this,MainActivity.class);
        startActivity(intentToMain);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        Intent intent=getIntent();
        mMap.setOnMapLongClickListener(this);

        String info=intent.getStringExtra("info");

        if(info.matches("new")) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {


                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.emrecanisik.mytravelbook", MODE_PRIVATE);
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean", false);            //ilk çalıştırdığımda false gelecek
                    if (!trackBoolean) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply();
                    }

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastlocation != null) {
                    LatLng lastuserLocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastuserLocation, 15));
                }
            }
        }

                else {
                    mMap.clear();
                    Place place = (Place) intent.getSerializableExtra("Place");
                    LatLng latLng = new LatLng(place.latitude, place.longitude);
                    String placeName = place.name;

                    mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                }

            }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if(grantResults.length>0){
        if(requestCode==1){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Intent intent=getIntent();
                String info=intent.getStringExtra("info");

                if(info.matches("new")) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastLocation != null) {
                        LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));

                    }
                }
                    else{
                        //sqlite data
                         mMap.clear();
                         Place place=(Place) intent.getSerializableExtra("Place");
                         LatLng latLng=new LatLng(place.latitude,place.longitude);
                         String placeName=place.name;

                         mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
                         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));



                    }



        }

    }
    }
}

        @Override
        public void onMapLongClick(LatLng latLng) {

            Geocoder geocoder =new Geocoder(getApplicationContext(), Locale.getDefault());
            String adress="";

            try {
                List<Address>addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                if(addressList!=null&&addressList.size()>0){
                    if(addressList.get(0).getThoroughfare()!=null){

                        adress+=addressList.get(0).getThoroughfare();
                    }
                    if(addressList.get(0).getSubThoroughfare()!=null){

                        adress+="";
                        adress+=addressList.get(0).getSubThoroughfare();
                    }
                }
                else{
                    adress="new place";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            mMap.clear();
            mMap.addMarker(new MarkerOptions().title(adress).position(latLng));

            Double latitude=latLng.latitude;
            Double longitude=latLng.longitude;

            final Place place=new Place(adress,latitude,longitude);

            AlertDialog.Builder alertDialog=new AlertDialog.Builder(MapsActivity.this);
            alertDialog.setCancelable(false);    //kullanıcı bizim dışımızda herhangi bir yere tıklayamasın.
            alertDialog.setTitle("Are You Sure?");
            alertDialog.setMessage(place.name);
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        database=MapsActivity.this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
                        database.execSQL("CREATE TABLE IF NOT EXISTS Places(id INTEGER PRIMARY KEY ,name VARCHAR,latitude VARCHAR,longitude VARCHAR)");

                        String toCompile="INSERT INTO Places(name,latitude,longitude) VALUES(?,?,?)";
                        SQLiteStatement sqLiteStatement=database.compileStatement(toCompile);
                        sqLiteStatement.bindString(1,place.name);
                        sqLiteStatement.bindString(2,String.valueOf(place.latitude));
                        sqLiteStatement.bindString(3,String.valueOf(place.longitude));
                        sqLiteStatement.execute();

                        Toast.makeText(getApplicationContext(),"Saved!,Toast",Toast.LENGTH_LONG).show();


                    }
                    catch (Exception exception){

                        exception.printStackTrace();
                    }

                }


            });
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(),"Canceled",Toast.LENGTH_LONG).show();
                }
            });
           alertDialog.show();

        }

}
