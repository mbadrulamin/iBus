package com.shadowcoder.ibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class StudentProfileActivity extends AppCompatActivity {

    private DatabaseReference mStudentDatabase;

    private String userId;
    private String mName;
    private String mPhone;
    private String mMatricNo;
    private String mProfileImageUrl;
    private ImageView mProfileImage;

    private Button editProfile;
    private TextView name, matricNo, phoneNo, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        editProfile = findViewById(R.id.buttonEditProfileStudent);
        name = findViewById(R.id.nameProfileStudent);
        matricNo = findViewById(R.id.matricNoProfileStudent);
        phoneNo = findViewById(R.id.phoneNoProfileStudent);
        email = findViewById(R.id.emailProfileStudent);

        mProfileImage = (ImageView) findViewById(R.id.image_profileStudent);

        mStudentDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId);

        getUserInfo();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentProfileActivity.this, StudentProfileEditActivity.class);
                startActivity(intent);
                finish();
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
                        name.setText(mName);
                    }
                    if (map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        phoneNo.setText(mPhone);
                    }
                    if (map.get("matricNo") != null){
                        mMatricNo = map.get("matricNo").toString();
                        matricNo.setText(mMatricNo);
                    }
                    if (map.get("profileImageUrl") != null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
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

            email.setText(email_get);

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(StudentProfileActivity.this, StudentMainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}