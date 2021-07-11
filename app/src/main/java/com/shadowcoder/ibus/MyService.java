package com.shadowcoder.ibus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyService extends Service {
    
    DriverStudent ds = new DriverStudent();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (ds.getIsDriver())
            Driver();

        else if (ds.getIsStudent())
            Student();

        return START_STICKY;
    }

    private void Driver(){
        createNotificationChannel();

        Intent intent1 = new Intent(this, DriverMapActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification notification = new NotificationCompat.Builder(this, "ChannelId1")
                .setContentTitle("iBus")
                .setContentText("iBus is running")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);


    }

    private void Student(){
        createNotificationChannel();

        Intent intent1 = new Intent(this, StudentMapActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification notification = new NotificationCompat.Builder(this, "ChannelId1")
                .setContentTitle("iBus")
                .setContentText("iBus is running")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

    }

    private void createNotificationChannel() {

        //check if OS is Oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = new NotificationChannel(
                    "ChannelId1", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        DriverStudent ds = new DriverStudent();
        ds.setWorking(false);
        //save switch state in shared preference
        SharedPreferences sharedPreferences = getSharedPreferences("save", MODE_PRIVATE);
        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
        // Storing the key and its value as the data fetched
        editor.putBoolean("value", false);
        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        editor.apply();

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

        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {

        //save switch state in shared preference
        SharedPreferences sharedPreferences = getSharedPreferences("save", MODE_PRIVATE);
        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
        // Storing the key and its value as the data fetched
        editor.putBoolean("value", false);
        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        editor.apply();

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

        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }
}
