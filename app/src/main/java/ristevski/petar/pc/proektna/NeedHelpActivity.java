package ristevski.petar.pc.proektna;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class NeedHelpActivity extends FragmentActivity implements OnMapReadyCallback, DialogFragmentClass.DataEntryListener {

    private GoogleMap mMap;
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String currentUser;
    DatabaseReference myRef = database.getReference();

    Button pobarajUsluga;
    Boolean aktivnoBaranje = false;
    Button logOut;
    Boolean helperActive = false;

    TextView infoTextVieew;




    Handler handler = new Handler();
    public void checkForUpdates() {

        try {
            Query isRequestActiveQuery = myRef.child("Users").child("Requests").child(currentUser);
            isRequestActiveQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final String helper = (String) dataSnapshot.child("Helper").getValue();

                    if (helper != null && !helper.equals("none")) {
                        helperActive = true;

                        Query findDriversLocation = myRef.child("Users").child("Helpers");


                        findDriversLocation.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Double lattiude;
                                Double longitude;
                                for (DataSnapshot req : dataSnapshot.getChildren()) {
                                    String user = (String) req.getKey();

                                    if (user != null && user.equals(helper)) {

                                        lattiude = (Double) req.child("Location").child("Lattitude").getValue();
                                        longitude = (Double) req.child("Location").child("Longitude").getValue();
                                        Location helpersLocation = new Location(LocationManager.GPS_PROVIDER);
                                        if (lattiude != null && longitude != null) {
                                            helpersLocation.setLatitude(lattiude);
                                            helpersLocation.setLongitude(longitude);
                                            if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(NeedHelpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                if (lastKnownLocation != null) {

                                                    double distance = lastKnownLocation.distanceTo(helpersLocation);
                                                    distance /= 1000;
                                                    distance *= 1.6;

                                                    Double roundDistance = (double) Math.round(distance * 100) / 100;
                                                    if (roundDistance < 0.1) {
                                                        infoTextVieew.setText("Вашата помош пристигна");
                                                        makeNotification();

                                                        deleteUser();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                infoTextVieew.setText("");
                                                                pobarajUsluga.setVisibility(View.VISIBLE);
                                                                logOut.setVisibility(View.VISIBLE);
                                                                pobarajUsluga.setText("ПОБАРАЈ УСЛУГА");
                                                                aktivnoBaranje = false;
                                                                helperActive = false;

                                                            }
                                                        }, 3000);

                                                    }


                                                    else {
                                                        infoTextVieew.setText("Вашата помош е оддалечена " + roundDistance + " километри од Вас");

                                                        LatLng helperLocationLatLng = new LatLng(helpersLocation.getLatitude(), helpersLocation.getLongitude());
                                                        LatLng requestLocationlatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());


                                                        ArrayList<Marker> markers = new ArrayList<>();
                                                        mMap.clear();
                                                        markers.clear();
                                                        try {
                                                            if (helperLocationLatLng != null && requestLocationlatLng != null) {
                                                                markers.add(mMap.addMarker(new MarkerOptions().position(helperLocationLatLng).title("Локација на помошта која пристига")));
                                                                markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationlatLng).title("Ваша Локација").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                                                            }
                                                        } catch (Exception ex) {
                                                            return;
                                                        }
                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                        for (Marker marker : markers) {
                                                            builder.include(marker.getPosition());
                                                        }
                                                        LatLngBounds bounds = builder.build();

                                                        int pading = 90;

                                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, pading);

                                                        mMap.animateCamera(cameraUpdate);

                                                        pobarajUsluga.setVisibility(View.INVISIBLE);
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                checkForUpdates();
                                                            }
                                                        }, 4000);


                                                    }

                                                }
                                            }
                                        }


                                    }
                                }
                                }





                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        catch (Exception ex){
            return;
        }





}

    private void makeNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);


            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setAutoCancel(true)
                .setSmallIcon(R.drawable.usluzise)
                .setContentTitle("Бараната помош штотуку пристигна на Вашата адреса")
                .setContentText("Ви благодариме што го користевте нашиот сервис")
                .setContentInfo("Info");
        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }


    public void getCurrentUser() {
    Intent intent = getIntent();

    currentUser = intent.getStringExtra("momentalenUser");

}
public void odjavise(View view){

    if(currentUser!= null) {
        deleteUser();
    }
     Intent i = new Intent(getApplicationContext(), MainActivity.class);
     startActivity(i);
}

    public void pobarajUsluga(View view){

        getCurrentUser();

        if(aktivnoBaranje){

            deleteUser();
            aktivnoBaranje= false;
            logOut.setVisibility(View.VISIBLE);
            pobarajUsluga.setText("Побарај услуга");


            }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(lastKnowLocation != null) {

                    Double lattitude = lastKnowLocation.getLatitude();
                    Double longitude = lastKnowLocation.getLongitude();

                    myRef.child("Users").child("Requests").child(currentUser).child("NudamIliBaram").setValue("PotrebnaMiEUsluga");
                    myRef.child("Users").child("Requests").child(currentUser).child("Helper").setValue("none");
                    myRef.child("Users").child("Requests").child(currentUser).child("Location").child("Lattitude").setValue(lattitude);
                    myRef.child("Users").child("Requests").child(currentUser).child("Location").child("Longitude").setValue(longitude);
                    pobarajUsluga.setText("Откажи барање на услуга");

                    aktivnoBaranje = true;

                    checkForUpdates();

                    DialogFragmentClass dialog = new DialogFragmentClass();
                    dialog.show(getFragmentManager(), "DIALOG_FRAGMENT");
                    dialog.setCancelable(false);

                }
            }
        }

    }

    private void deleteUser() {
        logOut = findViewById(R.id.bttnOdjava);
        logOut.setEnabled(true);
        aktivnoBaranje = false;
        infoTextVieew.setText("");
        pobarajUsluga.setText("Побарај услуга");
        myRef.child("Users").child("Requests").child(currentUser).removeValue();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                    Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateMap(lastKnowLocation);
                    checkForUpdates();
                }
            }
        }
    }

    public void updateMap(Location location) {
        if(helperActive == false) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Вашата локација"));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_needhelp);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pobarajUsluga = findViewById(R.id.bttnPobarajUsluga);
        logOut = findViewById(R.id.bttnOdjava);

        checkIfAlreadyHaveRequestFromThisUser();

        infoTextVieew = findViewById(R.id.infoTextView);




    }

    private void checkIfAlreadyHaveRequestFromThisUser() {
        getCurrentUser();

            Query isRequestActiveQuery = myRef.child("Users").child("Requests");
            isRequestActiveQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data: dataSnapshot.getChildren()) {
                        String key = data.getKey();
                        if(key!= null && key.equals(currentUser)) {
                            aktivnoBaranje = true;
                            pobarajUsluga.setText("Откажи барање на услуга");
                            logOut.setVisibility(View.INVISIBLE);
                            checkForUpdates();

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForUpdates();


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);

            }


            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }
        }
        else {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnowLocation != null) {
                    updateMap(lastKnowLocation);
                }
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnowLocation != null) {
                    updateMap(lastKnowLocation);
                }

            }
        }


    }
    @Override
    public void onBackPressed() {

    }
    @Override
    public void onDataEntryCompleted(String usluga, String cena) {
        if(!usluga.equals("")) {
            if(!cena.equals("")) {
                myRef.child("Users").child("Requests").child(currentUser).child("Usluga").setValue(usluga);
                myRef.child("Users").child("Requests").child(currentUser).child("Cena").setValue(cena);
            }
            else {
                myRef.child("Users").child("Requests").child(currentUser).child("Usluga").setValue(usluga);
                myRef.child("Users").child("Requests").child(currentUser).child("Cena").setValue("0");
            }
        }
        else {
            deleteUser();
            logOut.setVisibility(View.VISIBLE);

        }
    }
}
