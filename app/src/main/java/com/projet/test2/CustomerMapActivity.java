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
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

import java.util.HashMap;
import java.util.List;
import  java.lang.Object;

import static java.util.Objects.requireNonNull;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener  {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private String TAG;
    private Button Logout;
    private Button CallDriverBtn;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference ReqCustomer;
    private  LatLng CustomerPickUpLocation;
    private  DatabaseReference DriverWaitRef ,  CurrentWorkDriver;
    private  DatabaseReference DriverFounded;
    private int radius = 1;

    private Boolean driverFound = false, requestType = false;//////driver found or not found ///

    private String driverFoundID;/////id of driver who found//////
    private String customerID;
    Marker DriverMarker, CustomerMarker;
    GeoQuery geoQuery;
    private ValueEventListener DriverWorkRefListner;///////////////Classes implementing this interface can be used to receive events about data changes at a location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_customer_map );
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID =  FirebaseAuth.getInstance ( ).getCurrentUser ( ) .getUid();
        ReqCustomer = FirebaseDatabase.getInstance().getReference().child("Customer Trip Requests");
        DriverWaitRef= FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        CurrentWorkDriver = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
        Logout = findViewById(R.id.logout_customer_btn);
        CallDriverBtn = findViewById(R.id.call_a_car_button);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ( )
                .findFragmentById ( R.id.map );
        mapFragment.getMapAsync ( this );
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mAuth.signOut();
                LogOutCustomer();
            }

    });

        CallDriverBtn.setOnClickListener ( new View.OnClickListener (){
            @Override
            public void onClick(View v) {
                if (requestType=false)
                { geoQuery.removeAllListeners();
                    CurrentWorkDriver.removeEventListener( DriverWorkRefListner);
                    if (driverFound!=null)////boolean value
                    { DriverFounded =FirebaseDatabase.getInstance ().getReference ().child ( "Users" ).child ( "Drivers" ).child ( driverFoundID )
                            .child ("CustomerTripID");
                        DriverFounded.removeValue ();driverFoundID=null; }driverFound=false;radius=1;
                    String customerId=FirebaseAuth.getInstance ().getCurrentUser().getUid();
                    GeoFire geoFire=new GeoFire ( ReqCustomer );
                    geoFire.removeLocation ( customerId );
                    if (CustomerMarker !=null){
                        CustomerMarker.remove ();}/////////////removing markers if the user logout or cancel
                    if (DriverMarker!=null){DriverMarker.remove ();}
                    CallDriverBtn.setText ( "Request Another DRIVER"); }
                else{
                    requestType=true;
                    String customerId=FirebaseAuth.getInstance ().getCurrentUser().getUid();
                    GeoFire geoFireCustDb=new GeoFire ( ReqCustomer );
                    geoFireCustDb.setLocation ( customerId , new GeoLocation ( lastLocation.getLatitude () , lastLocation.getLongitude () ));
                    CustomerPickUpLocation=new LatLng ( lastLocation.getLatitude (),lastLocation.getLongitude () ) ;
                    CustomerMarker =mMap.addMarker ( new MarkerOptions().position ( CustomerPickUpLocation).title ( "Your Location" )
                            .icon ( BitmapDescriptorFactory.fromResource ( R.drawable.customer) )  );///location of the customer
                         CallDriverBtn.setText ( "the driver will arrive soon" );
                         getClosetDriver();
                }
            }
        });


    }

    private void getClosetDriver() {
        GeoFire geoFire=new GeoFire ( DriverWaitRef );
        geoQuery=geoFire.queryAtLocation ( new GeoLocation ( CustomerPickUpLocation.latitude,CustomerPickUpLocation.longitude ) , radius);
        geoQuery.removeAllListeners ();
        geoQuery.addGeoQueryEventListener ( new GeoQueryEventListener ( ) {
            @Override
            public void onKeyEntered(String key , GeoLocation location) {///////the function the search for driver
                    if (!driverFound&&requestType){//////if they are true
                    driverFound=true;driverFoundID=key;
                    DriverFounded =FirebaseDatabase.getInstance ().getReference ().child ( "users" ).child ( "Drivers" ).child ( driverFoundID );
                    HashMap <String, Object> DriverMap=new HashMap <String, Object> ();
                    DriverMap.put ( "CustomerTripID",customerID );///////////(KEY,value)
                    DriverFounded.updateChildren ( DriverMap); ////////to delete multiple children in a single API call
                    GettingDriverCoordinates();
                    CallDriverBtn.setText ( "search for driver loaction" ); }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key , GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {radius++;
                getClosetDriver ();}

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        } );
    }
////////////////////////////to show the customer the loaction of his driver
    private void GettingDriverCoordinates() {
        DatabaseReference driverWorkRef = CurrentWorkDriver;
        DriverWorkRefListner = driverWorkRef.child(driverFoundID).child("l").addValueEventListener ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) //////////This method will be called with a snapshot of the data at this location.
            { if (dataSnapshot.exists ( ) && requestType) {
                    List <Object> driverLocation = (List <Object>)  ( dataSnapshot ).getValue ( );
                    CallDriverBtn.setText ( "driver is founded" );
                    double LocationLatitude = 0;
                    if (( driverLocation ).get ( 0 ) != null) {///////// get :Returns the element at the specified position in this list.
                        LocationLatitude = Double.parseDouble ( driverLocation.get ( 0 ).toString ( ) ); }
                    double LocationLongitude = 0;
                    if (driverLocation.get ( 1 ) != null) {
                        LocationLongitude = Double.parseDouble ( driverLocation.get ( 1 ).toString ( ) ); }
                    LatLng DriverLL = new LatLng ( LocationLatitude , LocationLongitude ); /////location of the driver
                    if (DriverMarker != null) {
                        DriverMarker.remove ( );
                    } /////removing CustomerMarker
                    Location PositionCustomer = new Location ( "" );
                    PositionCustomer.setLatitude ( CustomerPickUpLocation.latitude );
                    PositionCustomer.setLongitude ( CustomerPickUpLocation.longitude );

                    Location PositionDriver = new Location ( "" );
                    PositionDriver.setLatitude ( DriverLL.latitude );
                    PositionDriver.setLongitude ( DriverLL.latitude );

                    float Distance = PositionCustomer.distanceTo ( PositionDriver ); ////to calculate the distance between the driver
                    if (Distance < 90) {
                        CallDriverBtn.setText ( "driver is near you" );
                    } else {
                        CallDriverBtn.setText ( "distance" + Distance ); }
                    DriverMarker = mMap.addMarker ( new MarkerOptions ( ).position ( DriverLL )
                            .title ( "Your driver is there" ).icon ( BitmapDescriptorFactory.fromResource ( R.drawable.car ) ) );
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
        if
        (ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager
                        .PERMISSION_GRANTED) {
            return; }
        buildGoogleApiClient ();
        mMap.setMyLocationEnabled ( true );

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
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest ( );
        locationRequest.setInterval ( 1000 );
        locationRequest.setFastestInterval ( 1000 );
        locationRequest.setPriority ( LocationRequest.PRIORITY_HIGH_ACCURACY );
        if (ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission ( this , Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
        return; }
        LocationServices.FusedLocationApi.requestLocationUpdates ( googleApiClient , this.locationRequest , this );
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;
        LatLng latLng=new LatLng ( location.getLatitude (),location.getLongitude () );
        mMap.moveCamera ( CameraUpdateFactory.newLatLng ( latLng ) );
        mMap.animateCamera ( CameraUpdateFactory.zoomTo ( 13) );
    }

    @Override
    protected void onStop() {
        super.onStop ( );
    }

    private void LogOutCustomer() {
        Intent CustomerLogIntent = new Intent(CustomerMapActivity.this, WelcomeActivity.class);
        CustomerLogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(CustomerLogIntent);
        finish();
    }
}
