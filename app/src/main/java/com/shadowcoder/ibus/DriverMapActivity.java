package com.shadowcoder.ibus;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    private Button mBackButton;
    private String studentId = "";
    private Boolean isLoggingOut = false;
    private LinearLayout mStudentInfo;
    private ImageView mStudentProfileImage;
    private TextView mStudentName, mStudentPhone;
    private Switch mWorkingSwitch;
    private Boolean isWorking = false;

    Location mLastLocation;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    if (isWorking){
                        try {
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                            GeoFire geoFireAvailable = new GeoFire(refAvailable);

                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                        System.err.println("There was an error saving the location to GeoFire: " + error);
                                    } else {
                                        System.out.println("Location saved on server successfully!");
                                    }
                                }
                            });
                        } catch (Exception e) {
                            System.out.println("User has been logout");
                            e.printStackTrace();
                        }
                    }



                }

            }
        }
    };


    public DriverMapActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mStudentInfo = findViewById(R.id.customerInfo);

        mStudentProfileImage = findViewById(R.id.customerProfileImage);

        mStudentName = findViewById(R.id.customerName);
        mStudentPhone = findViewById(R.id.customerPhone);
        mBackButton = findViewById(R.id.backButtonDriver);
        mWorkingSwitch = findViewById(R.id.workingSwitch);


        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, DriverMainMenuActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectDriver();
                    isWorking = true;
                }
                else{
                    disconnectDriver();
                    isWorking = false;
                }
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());


        //ADD MARKED LOCATION IN MAPS

        //For position of mark location
        //Hostel
        LatLng kk1 = new LatLng(2.84732271980368, 101.78060374347886);
        LatLng kk_acacia = new LatLng(2.8363490364466175, 101.7872608109686);
        LatLng kksi = new LatLng(2.81837153770839, 101.79290008726143);
        LatLng kknc = new LatLng(2.8422071468754457, 101.80911766894494);
        LatLng kk_anggerik = new LatLng(2.8378422522917424, 101.78657726676377);

        //Campus
        LatLng fst = new LatLng(2.8516626545518253, 101.76767518008944);
        LatLng fsu = new LatLng(2.8387871768119597, 101.78093797658298);
        LatLng fpqs_fpbu = new LatLng(2.841148053798292, 101.78448986482843);
        LatLng tamhidi_fkp = new LatLng(2.840466080045795, 101.78456109523134);
        LatLng fem = new LatLng(2.8421940718531453, 101.77897872439287);


        //Hostel
        mMap.addMarker(new MarkerOptions().position(kk1).title("Kolej Kediaman 1")
                //This below line is for custom marker (Code connect with above line)
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_mark_hostel)));
        mMap.addMarker(new MarkerOptions().position(kk_acacia).title("Kolej Kediaman Acacia Avenue")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_mark_hostel)));
        mMap.addMarker(new MarkerOptions().position(kksi).title("Kolej Kediaman Sutera Indah")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_mark_hostel)));
        mMap.addMarker(new MarkerOptions().position(kknc).title("Kolej Kediaman Nilam Court")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_mark_hostel)));
        mMap.addMarker(new MarkerOptions().position(kk_anggerik).title("Kolej Kediaman Anggerik Court")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_mark_hostel)));

        //Campus
        mMap.addMarker(new MarkerOptions().position(fst).title("Faculty of Science and Technology (FST)")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_campus)));
        mMap.addMarker(new MarkerOptions().position(fsu).title("Faculty of Syariah and Law (FSU)")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_campus)));
        mMap.addMarker(new MarkerOptions().position(fpqs_fpbu).title("Faculty of Quranic and Sunnah (FPQS)")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_campus)));
        mMap.addMarker(new MarkerOptions().position(tamhidi_fkp).title("Pusat Tamhidi")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_campus)));
        mMap.addMarker(new MarkerOptions().position(fem).title("Faculty of Economics and Muamalat (FEM)")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_campus)));

    }

    // SET CUSTOM MARKER ON MAP
    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private void getAssignedStudentInfo() {
        mStudentInfo.setVisibility(View.VISIBLE);
        DatabaseReference mStudentDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(studentId);
        mStudentDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mStudentName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mStudentPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl") != null) {
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mStudentProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void connectDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectDriver() {

        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);

        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoggingOut){
            disconnectDriver();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent_back = new Intent(DriverMapActivity.this, DriverMainMenuActivity.class);
        startActivity(intent_back);
        finish();
    }
}