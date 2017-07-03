package com.appescar.appescar;

/**
 * Created by nicol on 14/04/2017.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    private GoogleMap mMap;

    private DatabaseReference refDatabase;

    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RC_SIGN_IN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            this.displayMessage(auth.getCurrentUser().getEmail());
        } else {

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
                                    )).build(),
                    RC_SIGN_IN);

          /*  startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance().createSignInIntentBuilder().build(),
                    RC_SIGN_IN); */
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, FormAdd.class);
                startActivity(intent);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MapsActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
/*
        TextView tv = (TextView) findViewById(R.id.usuariologueado);
        if (auth.getCurrentUser() != null) {
            tv.setText(auth.getCurrentUser().getEmail());
        } else {
            tv.setText("Invitado");
        }
*/

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Called when a drawer's position changes.
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                TextView tv = (TextView) findViewById(R.id.usuariologueado);
                if (auth.getCurrentUser() != null) {
                    tv.setText(auth.getCurrentUser().getEmail());
                } else {
                    tv.setText("Invitado");
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
            }
        });


        refDatabase = FirebaseDatabase.getInstance().getReference().child("pescas");
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            this.displayMessage("Deslogueando usuario");
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                           // startActivity(new Intent(MyActivity.this, SignInActivity.class));
                           // finish();
                        }
                    });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mis_pescas) {

            Intent intent = new Intent(this, MisPescasActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_login) {


        } else if (id == R.id.nav_logout) {
        } else if (id == R.id.nav_config) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
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
        final HashMap<String, Pesca> markers = new HashMap<>();

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        LatLng chascomus = new LatLng(-35.582637,-58.062126);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window, null);

                ImageView catchPic = (ImageView) v.findViewById(R.id.catch_pic);
                TextView fish = (TextView) v.findViewById(R.id.fish);
                TextView bait = (TextView) v.findViewById(R.id.bait);
                TextView line = (TextView) v.findViewById(R.id.line);
                TextView description = (TextView) v.findViewById(R.id.description);

                Pesca pesca = markers.get(marker.getSnippet());

                byte[] decoded = Base64.decode(pesca.getImg(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                catchPic.setImageBitmap(decodedByte);
                fish.setText(marker.getTitle());
                bait.setText(getString(R.string.carnada, pesca.getBait()));
                line.setText(getString(R.string.linea, pesca.getLine()));
                description.setText(getString(R.string.detalle, pesca.getDescription()));

                return v;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, DetallesPesca.class);
                Pesca pesca = markers.get(marker.getSnippet());
                intent.putExtra("fish",pesca.getFish());
                intent.putExtra("line",pesca.getLine());
                intent.putExtra("bait",pesca.getBait());
                intent.putExtra("tst",pesca.getTst());

                byte[] decoded = Base64.decode(pesca.getImg(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                String fileName = "myImage";//no .png or .jpg needed
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    decodedByte.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
                    fo.write(bytes.toByteArray());
                    // remember close file output
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                intent.putExtra("img",fileName);
                intent.putExtra("description",pesca.getDescription());
                startActivity(intent);
            }
        });

        refDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String prevChildKey) {
                Pesca pesca = dataSnapshot.getValue(Pesca.class);
                LatLng newLocation = new LatLng(pesca.getLat(), pesca.getLng());

                MarkerOptions marker = new MarkerOptions()
                        .position(newLocation)
                        .title(pesca.getFish())
                        .snippet(dataSnapshot.getKey());

                mMap.addMarker(marker);
                markers.put(dataSnapshot.getKey(), pesca);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chascomus, 15.0f));

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Detectando ubicacion...", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void displayMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
