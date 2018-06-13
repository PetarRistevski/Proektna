package ristevski.petar.pc.proektna;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ViewRequestsActivity extends AppCompatActivity {


    public static final String REQUEST_LATTITUDE = "requestLattitude";
    public static final String REQUEST_LONGITUDE = "requestLongitude";
    public static final String HELPER_LATTITUDE = "helperLattitude";
    public static final String HELPER_LONGITUDE = "helperLongitude";
    public static final String OPIS_NA_USLUGA = "OPIS NA USLUGA";
    public static final String CENA_NA_USLUGA = "CENA NA USLUGA";
    public static final String REQUESTER_USER_NAME = "userName";
    public static final String HELPER_USER_NAME = "helperUserName";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String currentUser;
    DatabaseReference myRef = database.getReference();

    LocationManager mLocationManager;
    LocationListener mLocationListener;
    ListView requestListView;
    ArrayList<String> requests = new ArrayList<String>();

    ArrayAdapter mArrayAdapter;

    ArrayList<Double> requestLattitudes = new ArrayList<Double>();
    ArrayList<Double> requestLongitudes = new ArrayList<Double>();
    ArrayList<String> opisiNaUsluga = new ArrayList<String>();
    ArrayList<String> ceniNaUsluga = new ArrayList<String>();


    ArrayList<String> userIDs = new ArrayList<String>();
    boolean isHelperActive = false;



    Button logOut;

    public void logOutHelper(View view) {
        logOut = findViewById(R.id.bttnOdjava);
        deleteUser();



    }

    private void deleteUser() {

        getCurrentUser();

        myRef.child("Users").child("Helpers").child(currentUser).removeValue();
        isHelperActive = false;
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void getCurrentUser() {
        Intent intent = getIntent();

        currentUser = intent.getStringExtra("momentalenUser");

    }

    public void    updateListView(final Location location) {
        if (location != null) {

            requests.clear();
            requestLattitudes.clear();
            requestLongitudes.clear();
            getCurrentUser();
            isHelperActive=true;

            Query requestsQuery = myRef.child("Users").child("Requests");
            requestsQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Double lattiude;
                    Double longitude;
                    String opisUsluga;
                    Long tempCena;
                    String cenaUsluga;
                    for (DataSnapshot req : dataSnapshot.getChildren()) {
                        String user = (String) req.getKey();

                        opisUsluga = (String) req.child("Usluga").getValue();
                        try {

                            tempCena = (Long) req.child("Cena").getValue();
                            cenaUsluga = Objects.toString(tempCena, "0");
                        } catch (Exception ex) {
                            cenaUsluga = (String) req.child("Cena").getValue();
                        }



                        lattiude = (Double) req.child("Location").child("Lattitude").getValue();
                        longitude = (Double) req.child("Location").child("Longitude").getValue();


                        // Toast.makeText(ViewRequestsActivity.this, lattiude + " " + longitude, Toast.LENGTH_SHORT).show();


                        if (lattiude != null && longitude != null) {
                            Location location1 = new Location(LocationManager.GPS_PROVIDER);

                            location1.setLatitude(lattiude);
                            location1.setLongitude(longitude);

                            requestLattitudes.add(lattiude);
                            requestLongitudes.add(longitude);
                            opisiNaUsluga.add(opisUsluga);
                            ceniNaUsluga.add(cenaUsluga);




                            double distance = location1.distanceTo(location);
                            distance /= 1000;
                            distance *= 1.6;
                            Double roundDistance = (double) Math.round(distance * 100) / 100;

                            requests.add(roundDistance.toString() + " km" +" - "+ opisUsluga);
                            mArrayAdapter.notifyDataSetChanged();
                        }
                        userIDs.add(user);
                        if (requests.size() == 0) {
                            requests.add("Во моментов нема активни побарувања../");
                            mArrayAdapter.notifyDataSetChanged();
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
    protected void onCreate(Bundle savedInstanceState) {
        getCurrentUser();

        isHelperActive = !isHelperActive;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        setTitle("Моментално активни побарувања...");



        requestListView = findViewById(R.id.requestListView);
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);

        requests.clear();
        requests.add("Моментално активни побараувања за услуга...");
        requestListView.setAdapter(mArrayAdapter);


        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(ViewRequestsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (requestLattitudes.size() > i && requestLongitudes.size() > i && userIDs.size() > i && lastKnowLocation != null) {
                            Intent intent = new Intent(getApplicationContext(), HelperActivity.class);




                            intent.putExtra(REQUEST_LATTITUDE, requestLattitudes.get(i));
                            intent.putExtra(REQUEST_LONGITUDE,requestLongitudes.get(i));
                            intent.putExtra(HELPER_LATTITUDE, lastKnowLocation.getLatitude());
                            intent.putExtra(HELPER_LONGITUDE, lastKnowLocation.getLongitude());
                            intent.putExtra(OPIS_NA_USLUGA, opisiNaUsluga.get(i));
                            intent.putExtra(CENA_NA_USLUGA, ceniNaUsluga.get(i));
                            intent.putExtra(REQUESTER_USER_NAME,userIDs.get(i));
                            intent.putExtra(HELPER_USER_NAME, currentUser);



                        //Toast.makeText(ViewRequestsActivity.this, requestLattitudes.size() + " " + requestLongitudes.size() + " " + userIDs.size() , Toast.LENGTH_SHORT).show();
                        //  Toast.makeText(ViewRequestsActivity.this, userIDs.get(i) + " " + requestLattitudes.get(i) + " " + requestLongitudes.get(i), Toast.LENGTH_LONG).show();

                            startActivity(intent);
                    }
                }
            }
        });

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(isHelperActive) {
                    updateListView(location);


                    Double lattitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    try {

                        myRef.child("Users").child("Helpers").child(currentUser).child("Location").child("Lattitude").setValue(lattitude);
                        myRef.child("Users").child("Helpers").child(currentUser).child("Location").child("Longitude").setValue(longitude);
                        myRef.child("Users").child("Helpers").child(currentUser).child("NudamIliBaram").setValue("SakamDaPomognam");

                        }catch (Exception ex) {
                        return;
                    }
 }

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


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnowLocation != null && isHelperActive== true) {

                    updateListView(lastKnowLocation);
                }

            }
        }
    }



    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    public void onBackPressed() {

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]grantResults) {
            if (requestCode == 1) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                        Location lastKnowLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(isHelperActive) {
                            updateListView(lastKnowLocation);
                        }
                    }
                }
            }
        }

}
