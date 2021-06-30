package com.shadowcoder.ibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RouteEditActivity extends AppCompatActivity {

    private EditText mRouteField;
    private TextView currentRoute_text;

    private Button save;


    private DatabaseReference mDriverDatabase;

    private String userId, current_route;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_edit);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRouteField = findViewById(R.id.route_updateField);
        currentRoute_text = findViewById(R.id.currentRoute_text);

        save = findViewById(R.id.routeEdit_save);

        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);

        getRouteInfo();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateRouteEdit()) {
                    return;
                } else {
                    saveRouteInformation();
                    Intent intent = new Intent(RouteEditActivity.this,DriverMainMenuActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(), "Your new  bus route have been saved", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void getRouteInfo() {
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("route") != null) {
                        current_route = map.get("route").toString();
                        currentRoute_text.setText(current_route);
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
            String route = user.getDisplayName();


            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();


        }
    }


    private void saveRouteInformation() {
        current_route = mRouteField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("route", current_route);

        mDriverDatabase.updateChildren(userInfo);

        finish();

    }


    private Boolean validateRouteEdit() {
        String val = mRouteField.getText().toString();
        if (val.isEmpty()) {
            mRouteField.setError("Field cannot be empty");
            return false;
        } else {
            mRouteField.setError(null);
            return true;
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RouteEditActivity.this, DriverMainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
