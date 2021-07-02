package com.shadowcoder.ibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button mButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView mName, mEmail, mFeedback;
    private DatabaseReference mFeedBackDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_viewHome);
        toolbar = findViewById(R.id.toolbar_home);
        mButton = findViewById(R.id.submitButton);

        mName = findViewById(R.id.nameFeedback);
        mEmail = findViewById(R.id.emailFeedback);
        mFeedback = findViewById(R.id.opinionFeedback);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null); //Make the title gone

        navigationView.bringToFront(); //Make selectable selection in navigationMenu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.contact_us);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserFeedBack();
                Intent intent = new Intent(FeedbackActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Thank you for your feedback", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void saveUserFeedBack(){

        String email = mEmail.getText().toString();
        String name = mName.getText().toString();
        String feedback = mFeedback.getText().toString();
        mFeedBackDatabase = FirebaseDatabase.getInstance().getReference().child("Feedback").child(name);

        Map userFeedback = new HashMap();
        userFeedback.put("email", email);
        userFeedback.put("feedback", feedback);
        mFeedBackDatabase.updateChildren(userFeedback);
        finish();

    }


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            Intent intent = new Intent(FeedbackActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.home_nav:
                Intent intent = new Intent(FeedbackActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.about_us:
                Intent intent2 = new Intent(FeedbackActivity.this, AboutUsActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.feedback:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}