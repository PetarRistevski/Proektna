package ristevski.petar.pc.proektna;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static ristevski.petar.pc.proektna.ViewRequestsActivity.CENA_NA_USLUGA;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.HELPER_LATTITUDE;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.HELPER_LONGITUDE;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.HELPER_USER_NAME;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.OPIS_NA_USLUGA;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.REQUESTER_USER_NAME;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.REQUEST_LATTITUDE;
import static ristevski.petar.pc.proektna.ViewRequestsActivity.REQUEST_LONGITUDE;

public class HelperActivity extends FragmentActivity implements OnMapReadyCallback {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String currentUser;
    DatabaseReference myRef = database.getReference();
    Intent intent;
    private GoogleMap mMap;

    @Override
    public void onBackPressed() {

    }

    public void getBack(View view){

        Intent intent = new Intent(this, ViewRequestsActivity.class);
        intent.putExtra("momentalenUser",currentUser);
        startActivity(intent);
    }



    public void acceptRequest(View view) {
        String helperUsername = intent.getStringExtra("helperUserName");
        String user = intent.getStringExtra("userName");

        myRef.child("Users").child("Requests").child(user).child("Helper").setValue(helperUsername);

        Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + intent.getDoubleExtra("helperLattitude", 0) + "," + intent.getDoubleExtra("helperLongitude", 0) + "&daddr=" + intent.getDoubleExtra("requestLattitude", 0) + "," + intent.getDoubleExtra("requestLongitude", 0)));
        startActivity(directionsIntent);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        intent = getIntent();

        final Double helperLattitude = intent.getDoubleExtra(HELPER_LATTITUDE,0);
        final Double helperLongitude = intent.getDoubleExtra(HELPER_LONGITUDE,0);


        final Double requestLattitude = intent.getDoubleExtra(REQUEST_LATTITUDE,0);
        final Double requestLongitude = intent.getDoubleExtra(REQUEST_LONGITUDE,0);

        String opisNaUsluga = intent.getStringExtra(OPIS_NA_USLUGA);
        String cenaNaUsluga = intent.getStringExtra(CENA_NA_USLUGA);

        currentUser = intent.getStringExtra(HELPER_USER_NAME);


        Usluga usluga = new Usluga(opisNaUsluga, cenaNaUsluga);

        DataToShowFragment dialog = DataToShowFragment.newInstance(usluga);
        dialog.show(getFragmentManager(),"DIALOG_FRAGMENT");
        dialog.setCancelable(false);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RelativeLayout mapLayout = findViewById(R.id.mapRelativeLayout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {






                LatLng helperLocation =new LatLng(helperLattitude, helperLongitude);
                LatLng requestLocation = new LatLng(requestLattitude, requestLongitude);


                ArrayList<Marker> markers = new ArrayList<>();
                markers.clear();
                try {
                if(helperLocation != null && requestLocation != null ) {
                    markers.add(mMap.addMarker(new MarkerOptions().position(helperLocation).title("Вашата локација")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Локација на барање").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                }}catch (Exception ex) {
                    return;
                }
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker: markers ) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                int pading = 60;

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, pading);

                mMap.animateCamera(cameraUpdate);

            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
