package com.projet.test2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoFire.CompletionListener;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button logoutDriverBtn ;
    private FirebaseAuth mAuth;
    private Boolean DriverLogStatus = false;
    private String TAG;
    private FirebaseUser currentUser;
    private String driverId;
    private String customerID =""; /////customerId is initiated with null;
    Marker CustomerMarker; ///////////this marker of the customer
    private DatabaseReference AvailableDriver, BusyDriver ;
    private ValueEventListener ReqCustomerPickUpListner;
    private DatabaseReference ReqCustomer, ReqCustomerPickUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_driver_map );
        mAuth = FirebaseAuth.getInstance();// Initialize Firebase Auth
        currentUser = mAuth.getCurrentUser();// Check if user is signed in (non-null) and update UI accordingly.
        driverId = mAuth.getCurrentUser().getUid();
        logoutDriverBtn = (Button) findViewById ( R.id.logout_driver_btn );
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ( )
                .findFragmentById ( R.id.map );
        mapFragment.getMapAsync ( this );
        logoutDriverBtn.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                DriverLogStatus = true;
                DisconnectDriver();
                mAuth.signOut();
                LogOutDriver();
            }
        } );
        RequestOfCustomer();
    }

    //////corrected////////// i changed .child("driver on trip") to .child(driverid)
    //and i change the name of customerId to customerID to match with the same databse reference inside the cutomerMapActivity .
    private void RequestOfCustomer()
    { ReqCustomer=FirebaseDatabase.getInstance ().getReference ().child ( "Users" ).child ( "Drivers" )
                .child ( driverId ).child ( "CustomerOnTripID" );
        ReqCustomer.addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            { if(dataSnapshot.exists()){
                    ///////Returns true if the snapshot contains a non-null value.
                    customerID =dataSnapshot.getValue ().toString ();
                    CustomerPickupLocation(); }
                else {
                    customerID = "";
                    if (CustomerMarker != null) { CustomerMarker.remove(); }
                    if (ReqCustomerPickUpListner != null) { ReqCustomerPickUp.removeEventListener(ReqCustomerPickUpListner); }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

    }
    private  void CustomerPickupLocation()
    { ReqCustomerPickUp=FirebaseDatabase.getInstance ().getReference ().child ( "Customer 's Request" ).child ( customerID ).child ( "l" );
        ReqCustomerPickUpListner=ReqCustomerPickUp.addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists ()){
                    List<Object>CustomerLocation=(List<Object>)dataSnapshot.getValue ();
                    double LocationLatitude =0;
                    double LocationLongitude =0;
                    if (CustomerLocation.get (0) != null ){ LocationLatitude=Double.parseDouble ( CustomerLocation.get(0).toString () );}
                    if (CustomerLocation.get ( 1 )!=null){LocationLongitude=Double.parseDouble ( CustomerLocation.get ( 1 ).toString () );}
                    LatLng DriverLL = new LatLng ( LocationLatitude , LocationLongitude ); /////location of the driver
                    CustomerMarker = mMap.addMarker ( new MarkerOptions ().position ( DriverLL )
                            .title ( "CUSTOMER position" ).icon ( BitmapDescriptorFactory.fromResource ( R.drawable.customer )));                      ///////////this marker of the customer
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient ();
        mMap.setMyLocationEnabled ( true );

    }
    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null){
            lastLocation=location;
            LatLng latLng=new LatLng ( location.getLatitude (),location.getLongitude () );
            mMap.moveCamera ( CameraUpdateFactory.newLatLng ( latLng ) );
            mMap.animateCamera ( CameraUpdateFactory.zoomTo ( 13) );
            /////////////create reference ( available driver  & busy driver  )inside the database//////////////////////
            String userID =( FirebaseAuth.getInstance().getCurrentUser()).getUid();
            BusyDriver=FirebaseDatabase.getInstance ().getReference ().child ( "busy Drivers" );
            GeoFire geoFireBusy = new GeoFire ( BusyDriver );
            AvailableDriver = FirebaseDatabase.getInstance ( ).getReference ( ).child ( "Available Drivers" );
            GeoFire geoFireAvailable = new GeoFire(  AvailableDriver );
            switch (customerID)/////CORRECTED the SWITCHE BETWEEN geofireAvailable and geofireBusy
            {
                case ""://///NO request case
                    geoFireBusy.removeLocation(userID);
                    geoFireAvailable.setLocation(userID,  new GeoLocation (location.getLatitude (),location.getLongitude ()));
                    break;
                default://///there is a request (if everyone is working we will remove the available driver location and vice versa)
                    geoFireAvailable.removeLocation(userID);
                    geoFireBusy.setLocation(userID,  new GeoLocation (location.getLatitude (),location.getLongitude () ));
                    break;
            }
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest ( );
        locationRequest.setInterval ( 1000 );
        locationRequest.setFastestInterval ( 1000 );
        locationRequest.setPriority ( LocationRequest.PRIORITY_HIGH_ACCURACY );
        if (ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates ( googleApiClient , this.locationRequest , this );

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    protected  synchronized  void buildGoogleApiClient(){

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect ();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        if(!DriverLogStatus)
        {
            DisconnectDriver();
        }
    }


////to remove the driver from the database
    private void DisconnectDriver()
    {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        AvailableDriver = FirebaseDatabase.getInstance ( ).getReference ( ).child ( "Available Drivers" );
        GeoFire geoFireAvDriv = new GeoFire(  AvailableDriver );
           geoFireAvDriv.removeLocation ( userID );
           geoFireAvDriv.removeLocation(userID, new CompletionListener ( ) {
            @Override
            public void onComplete(String key , DatabaseError error) {
                Log.e(TAG, "Driver logout");
           }
       } );
    }


///to switch activities
    public void LogOutDriver()
    {
        Intent DriverLogIntent = new Intent(DriverMapActivity.this, WelcomeActivity.class);
        DriverLogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(DriverLogIntent);
        finish();
    }





}