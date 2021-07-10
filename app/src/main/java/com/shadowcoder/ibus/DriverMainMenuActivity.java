package com.shadowcoder.ibus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class DriverMainMenuActivity extends AppCompatActivity {

    private DatabaseReference mDriverDatabase;
    private String userId, mName;
    private TextView nameDriver;

    private Button busRealTime, profileDriver, logout, route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main_menu);

        DriverWorking dw = new DriverWorking();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        busRealTime = findViewById(R.id.real_time_location);
        profileDriver = findViewById(R.id.profileDriver);
        logout = findViewById(R.id.logout);
        nameDriver = findViewById(R.id.name_userDriver);
        route = findViewById(R.id.route_Driver);

        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
        getUserInfo();

        busRealTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMainMenuActivity.this, DriverMapActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profileDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMainMenuActivity.this, DriverProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dw.getWorking()){
                    Toast.makeText(DriverMainMenuActivity.this, "Please switch off on duty toggle", Toast.LENGTH_SHORT).show();
                    return;
                }
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMainMenuActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "You've been logout", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMainMenuActivity.this, RouteEditActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getUserInfo() {
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        nameDriver.setText(mName);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email_get = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

        }
    }

    private void disconnectDriver() {

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
    public void onBackPressed() {
        Intent intent_back = new Intent(DriverMainMenuActivity.this, HomeActivity.class);
        startActivity(intent_back);
        finish();
    }
}