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

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

public class ANGGERIK_ScheduleActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anggerik_schedule);

        drawerLayout = findViewById(R.id.drawer_layout_schedule);
        navigationView = findViewById(R.id.nav_viewHome);
        toolbar = findViewById(R.id.toolbar_schedule);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null); //Make the title gone

        navigationView.bringToFront(); //Make selectable selection in navigationMenu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.anggerik);



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.kk1:
                Intent intent = new Intent(ANGGERIK_ScheduleActivity.this, KK1_ScheduleActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.kknc:
                Intent intent2 = new Intent(ANGGERIK_ScheduleActivity.this, KKNC_ScheduleActivity.class);
                startActivity(intent2);
                finish();
                break;
            case R.id.kksi:
                Intent intent3 = new Intent(ANGGERIK_ScheduleActivity.this, KKSI_ScheduleActivity.class);
                startActivity(intent3);
                finish();
                break;
            case R.id.anggerik:
                break;
            case R.id.acacia:
                Intent intent4 = new Intent(ANGGERIK_ScheduleActivity.this, ACACIA_ScheduleActivity.class);
                startActivity(intent4);
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            Intent intent_back = new Intent(ANGGERIK_ScheduleActivity.this, StudentMainMenuActivity.class);
            startActivity(intent_back);
            finish();
        }
    }
}