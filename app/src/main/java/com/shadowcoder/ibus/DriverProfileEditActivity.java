package com.shadowcoder.ibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverProfileEditActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mStaffIdField, mEmailField, mPasswordField, mBusRegistrationNoField;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;

    private DatabaseReference mDriverDatabase;

    private String userId, mName, mPhone, mStaffId, mBusRegistrationNo, mProfileImageUrl;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile_edit);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mNameField = findViewById(R.id.driverNameProfile);
        mPhoneField = findViewById(R.id.driverPhoneProfile);
        mStaffIdField = findViewById(R.id.driverStaffIdProfile);
        mEmailField = findViewById(R.id.driverEmailProfile);
        mPasswordField = findViewById(R.id.driverPasswordProfile);
        mBusRegistrationNoField = findViewById(R.id.driverBusRegistrationProfile);

        mBack = findViewById(R.id.driverBackButtonProfile);
        mConfirm = findViewById(R.id.driverConfirmButtonProfile);

        mProfileImage = (ImageView) findViewById(R.id.driverImageProfile);


        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");


                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateName() | !validatePhone() | !validateStaffId()) {
                    return;
                } else {
                    saveUserInformation();
                    Intent intent = new Intent(DriverProfileEditActivity.this, DriverProfileActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(), "Your information have been saved", Toast.LENGTH_SHORT).show();
                }


            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverProfileEditActivity.this, DriverProfileActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }

    private void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map.get("name") != null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if (map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if (map.get("staffId") != null){
                        mStaffId = map.get("staffId").toString();
                        mStaffIdField.setText(mStaffId);
                    }
                    if (map.get("busRegistrationNo") != null){
                        mBusRegistrationNo = map.get("busRegistrationNo").toString();
                        mBusRegistrationNoField.setText(mBusRegistrationNo);
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
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();

            mEmailField.setText(email);

        }
    }


    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mStaffId = mStaffIdField.getText().toString();
        mBusRegistrationNo = mBusRegistrationNoField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("staffId", mStaffId);
        userInfo.put("busRegistrationNo", mBusRegistrationNo);

        mDriverDatabase.updateChildren(userInfo);

        if (resultUri != null){

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull  Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });

        }else{
            finish();
        }

        finish();

    }

    private Boolean validateName() {
        String val = mNameField.getText().toString();
        if (val.isEmpty()) {
            mNameField.setError("Field cannot be empty");
            return false;
        } else {
            mNameField.setError(null);
            return true;
        }
    }

    private Boolean validateStaffId() {
        String val = mStaffIdField.getText().toString();
        if (val.isEmpty()) {
            mStaffIdField.setError("Field cannot be empty");
            return false;
        } else {
            mStaffIdField.setError(null);
            return true;
        }
    }

    private Boolean validatePhone() {
        String val = mPhoneField.getText().toString();
        if (val.isEmpty()) {
            mPhoneField.setError("Field cannot be empty");
            return false;
        } else {
            mPhoneField.setError(null);
            return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DriverProfileEditActivity.this, DriverProfileActivity.class);
        startActivity(intent);
        finish();
    }
}