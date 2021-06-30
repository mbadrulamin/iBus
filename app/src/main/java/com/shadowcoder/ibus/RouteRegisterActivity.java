package com.shadowcoder.ibus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RouteRegisterActivity extends AppCompatActivity {

    private Button route_register;
    private EditText routeField;

    private DatabaseReference mDriverDatabase;
    private String userId, mRouteField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_register);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        route_register = (Button)findViewById(R.id.buttonSave_routeRegister);
        routeField = findViewById(R.id.routeDriverRegister);

        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);

        route_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateRoute()) {
                    return;
                } else {
                    saveRouteInformation();
                }
                Intent intent = new Intent(RouteRegisterActivity.this, DriverMainMenuActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

    private void saveRouteInformation() {
        mRouteField = routeField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("route", mRouteField);

        mDriverDatabase.updateChildren(userInfo);

        finish();
    }

    private Boolean validateRoute() {
        String val = route_register.getText().toString();
        if (val.isEmpty()) {
            route_register.setError("Field cannot be empty");
            return false;
        } else {
            route_register.setError(null);
            return true;
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RouteRegisterActivity.this, DriverMainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}