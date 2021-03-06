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
import android.support.annotation.VisibleForTesting;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
    private DatabaseReference refUbicaciones;

    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RC_SIGN_IN = 200;

    final HashMap<String, Pesca> markers = new HashMap<>();

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

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

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, FormAdd.class);

                intent.putExtra("lat",mMap.getCameraPosition().target.latitude);
                intent.putExtra("lng",mMap.getCameraPosition().target.longitude);
                startActivity(intent);
            }
        });


        FloatingActionButton fabsearch = (FloatingActionButton) findViewById(R.id.fabsearch);
        fabsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(MapsActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }

        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MapsActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Called when a drawer's position changes.
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                TextView tv = (TextView) findViewById(R.id.usuariologueado);

                NavigationView navigationView= (NavigationView) findViewById(R.id.nav_view);
                Menu menuNav=navigationView.getMenu();
                MenuItem nav_login = menuNav.findItem(R.id.nav_login);
                MenuItem nav_logout = menuNav.findItem(R.id.nav_logout);
                MenuItem nav_config = menuNav.findItem(R.id.nav_config);
                MenuItem nav_mis_pescas = menuNav.findItem(R.id.nav_mis_pescas);

                if (auth.getCurrentUser() != null) {
                    tv.setText(auth.getCurrentUser().getEmail());
                    nav_login.setVisible(false);
                    nav_logout.setVisible(true);
                    nav_mis_pescas.setVisible(true);
                    nav_config.setVisible(true);
                } else {
                    tv.setText("Invitado");
                    nav_login.setVisible(true);
                    nav_logout.setVisible(false);
                    nav_mis_pescas.setVisible(false);
                    nav_config.setVisible(false);
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

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
                            )).build(),
                    RC_SIGN_IN);

        } else if (id == R.id.nav_logout) {

            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            // startActivity(new Intent(MyActivity.this, SignInActivity.class));
                            // finish();
                        }
                    });

        } else if (id == R.id.nav_config) {

            refUbicaciones = FirebaseDatabase.getInstance().getReference().child("ubicaciones");
            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

            Ubicacion ubicacion = new Ubicacion(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude );
            refUbicaciones.child(currentFirebaseUser.getUid()).setValue(ubicacion);

            this.displayMessage("Ubicación Guardada");

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

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        //LatLng chascomus = new LatLng(-35.582637,-58.062126);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window, null);

                ImageView catchPic = (ImageView) v.findViewById(R.id.catch_pic);
                TextView fish = (TextView) v.findViewById(R.id.fish);
                TextView bait = (TextView) v.findViewById(R.id.bait);
                TextView line = (TextView) v.findViewById(R.id.line);
                TextView description = (TextView) v.findViewById(R.id.description);

                Pesca pesca = markers.get(marker.getSnippet());

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://apppescar-e204f.appspot.com/");
                StorageReference imgRef = storageRef.child("pescas/"+pesca.getImgname()+".png");




                Glide.with(getApplicationContext())
                        .using(new FirebaseImageLoader())
                        .load(imgRef)
                        .listener(new RequestListener<StorageReference, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                if(!isFromMemoryCache) marker.showInfoWindow();
                                return false;
                            }
                        })
                        .into(catchPic);


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
                intent.putExtra("imgname",pesca.getImgname());
                intent.putExtra("description",pesca.getDescription());
                startActivity(intent);
            }
        });

        this.GeneraMarcadores();
        this.GeneraUbicacionFavorita();

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(chascomus, 15.0f));
    }


    private void GeneraUbicacionFavorita() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        ValueEventListener ubiListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Ubicacion ubicacion = dataSnapshot.getValue(Ubicacion.class);

                if (ubicacion!=null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(ubicacion.lat, ubicacion.lng), 15.0f));
                } else {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-35.582637,-58.062126), 15.0f));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("D", "Ubicacion Favorita Cancelo", databaseError.toException());
            }
        };

        if (auth.getCurrentUser()!=null) {
            refUbicaciones = FirebaseDatabase.getInstance().getReference().child("ubicaciones");
            refUbicaciones.child(auth.getCurrentUser().getUid()).addValueEventListener(ubiListener);
        }

    }

    private void GeneraMarcadores() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                CameraUpdate location = CameraUpdateFactory.newLatLng(place.getLatLng());

                mMap.animateCamera(location);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                //              Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                //            Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }



        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                refDatabase = FirebaseDatabase.getInstance().getReference().child("pescas");

                this.GeneraMarcadores();
                this.GeneraUbicacionFavorita();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    this.displayMessage("Fallo Login");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    this.displayMessage("No hay Red para loguearse");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    this.displayMessage("Error desconocido al loguearse");
                    return;
                }
            }


        }


    }


}
