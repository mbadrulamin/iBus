package com.shadowcoder.ibus;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StudentMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Switch mShowStudentSwitch, visibleLocation_student;

    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private SupportMapFragment mapFragment;

    private Marker mDriverMarker, mStudentMarker;

    private BottomSheetDialog bottomSheetDialog;
    private TextView NameDriver,PlateDriver,RouteDriver,PhoneDriver, mDriverDistance;

    private TextView NameStudent,MatricNo_student,PhoneStudent, StudentDistance;
    private ImageView mStudentProfileImage, mDriverProfileImage;

    //an object used to store the student visibility state
    DriverStudent ds = new DriverStudent();

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));      //Default : 11

                    if (ds.getIsStudentVisible()){
                        try {
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("studentsAvailable");
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


                    if(!getDriversAroundStarted){
                        getDriversAround();
                    }

//                    if (!getStudentsAroundStarted){
//                        getStudentsAround();
//                    }

                }
            }
        }
    };

    public StudentMapActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_map);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mShowStudentSwitch = findViewById(R.id.getStudentInfo_switch);
        visibleLocation_student = findViewById(R.id.showLoc_student);

        //save switch state in shared preference
        SharedPreferences sharedPreferences = getSharedPreferences("save", MODE_PRIVATE);
        visibleLocation_student.setChecked(sharedPreferences.getBoolean("value", false));


        visibleLocation_student.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                    ds.setIsStudentVisible(true);
                    connectStudent();
                    // Creating an Editor object to edit(write to the file)
                    SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
                    // Storing the key and its value as the data fetched
                    editor.putBoolean("value", true);
                    // Once the changes have been made,
                    // we need to commit to apply those changes made,
                    // otherwise, it will throw an error
                    editor.apply();
                }
                else{

                    ds.setIsStudentVisible(false);
                    disconnectStudent();
                    SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
                    editor.putBoolean("value", false);
                    editor.apply();
                }
            }
        });

        mShowStudentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked){
                        if (mLastLocation == null){
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mShowStudentSwitch.setChecked(false);
                                }
                            }, 2000);

                        }
                        getStudentsAround();
                        Toast.makeText(StudentMapActivity.this, "Please turn of view student to view the driver info!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        recreate();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


        /*-------------------------------------------- Map specific functions -----
    |  Function(s) getDriversAround
    |
    |  Purpose:  Get's most updated drivers location and it's always checking for movements.
    |
    |  Note:
    |	   Even tho we used geofire to push the location of the driver we can use a normal
    |      Listener to get it's location with no problem.
    |
    |      0 -> Latitude
    |      1 -> Longitudde
    |
    *-------------------------------------------------------------------*/

    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();

    private void getDriversAround(){
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 2000);


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_foreground)));
                mDriverMarker.setTag(key);
                markers.add(mDriverMarker);

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        try {
                            getDriverInfo((String) marker.getTag());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return false;
                    }
                });


            }

            @Override
            public void onKeyExited(String key) {
//                for(Marker markerIt : markers){
//                    if(markerIt.getTag().equals(key)){
//                        markerIt.remove();
//                        markers.remove(markerIt);
//                    }
//                }

                try {
                    Iterator<Marker> itr = markers.iterator();
                    while (itr.hasNext()){
                        Marker markerIt = itr.next();
                        if (markerIt.getTag().equals(key)){
                            markerIt.remove();
                            markers.remove(markerIt);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }



        });

//        mMap.setOnMarkerClickListener(this);

    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//
//        //Retrieve the data from the marker.
//        try {
//            if (marker.equals(mDriverMarker)){
//                getDriverInfo((String) marker.getTag());
//            }
//            else if(marker.equals(mStudentMarker)){
//                getStudentInfo((String) marker.getTag()); // buat if statement kejap lagi  //DONE!!!
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Not click on bus icon");
//        }
//
//
//        return false;
//    }


    /*-------------------------------------------- getDriverInfo -----
|  Function(s) getDriverInfo
|
|  Purpose:  Get all the user information that we can get from the user's database.
|
|  Note: --
|
*-------------------------------------------------------------------*/
    private void getDriverInfo(String driverFoundID){

        DatabaseReference mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);


        bottomSheetDialog = new BottomSheetDialog(StudentMapActivity.this,R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.fragment_bottom_sheet, (ViewGroup) findViewById(R.id.bottomSheetContainerStudent));

        NameDriver = sheetView.findViewById(R.id.name_bus_driver);
        PlateDriver = sheetView.findViewById(R.id.plate_bus_driver);
        RouteDriver = sheetView.findViewById(R.id.route_bus_driver);
        PhoneDriver = sheetView.findViewById(R.id.phone_bus_driver);
        mDriverDistance = sheetView.findViewById(R.id.driverDistance);
        mDriverProfileImage = sheetView.findViewById(R.id.image_bus_driver);


        getDriverLocation(driverFoundID);

        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        NameDriver.setText(dataSnapshot.child("name").getValue().toString());

                    }
                    if(dataSnapshot.child("phone")!=null){
                        PhoneDriver.setText(dataSnapshot.child("phone").getValue().toString());

                    }
                    if(dataSnapshot.child("busRegistrationNo")!=null){
                        PlateDriver.setText(dataSnapshot.child("busRegistrationNo").getValue().toString());

                    }
                    if(dataSnapshot.child("route")!=null){
                        RouteDriver.setText(dataSnapshot.child("route").getValue().toString());
                    }

                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void getDriverLocation(String driverFoundID){
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable").child(driverFoundID).child("l");
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLocation = new LatLng(locationLat, locationLng);
                    LatLng studentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());


                    Location loc1 = new Location("");
                    loc1.setLatitude(studentLocation.latitude);
                    loc1.setLongitude(studentLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLocation.latitude);
                    loc2.setLongitude(driverLocation.longitude);

                    float distance = loc1.distanceTo(loc2)/1000;

                    mDriverDistance.setText(String.format("%.4g%n",distance));

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mMap.setMyLocationEnabled(true);
            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        //Get last known user location
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        }
                    }
                });

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
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(StudentMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(StudentMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void connectStudent(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectStudent() {

        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("studentsAvailable");

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


//    @Override
//    protected void onStop() {
//        disconnectStudent();
//        super.onStop();
//
//    }


           /*-------------------------------------------- Map specific functions -----
    |  Function(s) getStudentsAround
    |
    |  Purpose:  Get's most updated drivers location and it's always checking for movements.
    |
    |  Note:
    |	   Even tho we used geofire to push the location of the driver we can use a normal
    |      Listener to get it's location with no problem.
    |
    |      0 -> Latitude
    |      1 -> Longitudde
    |
    *-------------------------------------------------------------------*/

    boolean getStudentsAroundStarted = false;
    List<Marker> markers2 = new ArrayList<Marker>();

    private void getStudentsAround(){
        getStudentsAroundStarted = true;
        DatabaseReference studentLocation = FirebaseDatabase.getInstance().getReference().child("studentsAvailable");
        GeoFire geoFire = new GeoFire(studentLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 2000);


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt2 : markers2){
                    if(markerIt2.getTag().equals(key))
                        return;
                }

                LatLng studentLocation = new LatLng(location.latitude, location.longitude);

                mStudentMarker = mMap.addMarker(new MarkerOptions().position(studentLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_student_foreground)));
                mStudentMarker.setTag(key);
                markers2.add(mStudentMarker);

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        try {
                            getStudentInfo((String) marker.getTag());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return false;
                    }
                });

            }

            @Override
            public void onKeyExited(String key) {

//                for(Marker markerIt2 : markers2){
//                    if(markerIt2.getTag().equals(key)){
//                        markerIt2.remove();
//                        markers2.remove(markerIt2);
//                    }
//                }
                try {
                    Iterator<Marker> itr = markers2.iterator();
                    while (itr.hasNext()){
                        Marker markerIt2 = itr.next();
                        if (markerIt2.getTag().equals(key)){
                            markerIt2.remove();
                            markers2.remove(markerIt2);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt2 : markers2){
                    if(markerIt2.getTag().equals(key)){
                        markerIt2.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }



        });

//        mMap.setOnMarkerClickListener(this);

    }


    /*-------------------------------------------- getStudentInfo -----
|  Function(s) getStudentInfo
|
|  Purpose:  Get all the user information that we can get from the user's database.
|
|  Note: --
|
*-------------------------------------------------------------------*/
    private void getStudentInfo(String studentFoundID){

        DatabaseReference mStudentDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(studentFoundID);


        bottomSheetDialog = new BottomSheetDialog(StudentMapActivity.this,R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_get_student_info, (ViewGroup) findViewById(R.id.bottomSheetgetStudentInfo));

        NameStudent = sheetView.findViewById(R.id.getName_student);
        MatricNo_student = sheetView.findViewById(R.id.getMatricNo_student);
        PhoneStudent = sheetView.findViewById(R.id.getPhone_student);
        mStudentProfileImage = sheetView.findViewById(R.id.getImage_student);
        StudentDistance = sheetView.findViewById(R.id.getDistance_student);

        getStudentLocation(studentFoundID);

        mStudentDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        NameStudent.setText(dataSnapshot.child("name").getValue().toString());

                    }
                    if(dataSnapshot.child("phone")!=null){
                        PhoneStudent.setText(dataSnapshot.child("phone").getValue().toString());

                    }
                    if(dataSnapshot.child("matricNo")!=null){
                        MatricNo_student.setText(dataSnapshot.child("matricNo").getValue().toString());

                    }

                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mStudentProfileImage);
                    }


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void getStudentLocation(String studentFoundID){
        DatabaseReference studentLocationRef = FirebaseDatabase.getInstance().getReference().child("studentsAvailable").child(studentFoundID).child("l");
        studentLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng studentLocation = new LatLng(locationLat, locationLng);
                    LatLng currUserLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());


                    Location loc1 = new Location("");
                    loc1.setLatitude(studentLocation.latitude);
                    loc1.setLongitude(studentLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(currUserLocation.latitude);
                    loc2.setLongitude(currUserLocation.longitude);

                    float distance = loc1.distanceTo(loc2)/1000;

                    StudentDistance.setText(String.format("%.4g%n",distance ));

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent_back = new Intent(StudentMapActivity.this, StudentMainMenuActivity.class);
        startActivity(intent_back);
        finish();
    }
}