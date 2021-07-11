package com.shadowcoder.ibus;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class StudentMainMenuActivity extends AppCompatActivity {

    private Button schedule, busRealTime, profileStudent ,logout;

    private DatabaseReference mStudentDatabase;

    private String mName, userId;
    private TextView name_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main_menu);

        DriverStudent ds = new DriverStudent();
        ds.setIsStudent(true); ds.setIsDriver(false);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        schedule = findViewById(R.id.bus_scheduleStudent);
        busRealTime = findViewById(R.id.real_time_location);
        profileStudent = findViewById(R.id.profileDriver);
        logout = findViewById(R.id.logout);
        name_user = findViewById(R.id.name_userDriver);

        mStudentDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId);

        getUserInfo();

        //start foreground service
        Intent intent = new Intent(this, MyService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intent);
        }
        else {
            startService(intent);
        }

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentMainMenuActivity.this, KK1_ScheduleActivity.class);
                startActivity(intent);
                finish();
            }
        });

        busRealTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentMainMenuActivity.this, StudentMapActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profileStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentMainMenuActivity.this,StudentProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(StudentMainMenuActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "You've been logout", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }

    private void getUserInfo(){
        mStudentDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null){
                        mName = map.get("name").toString();
                        name_user.setText(mName);
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

    @Override
    public void onBackPressed() {
        Intent intent_back = new Intent(StudentMainMenuActivity.this,HomeActivity.class);
        startActivity(intent_back);
        finish();
    }
}