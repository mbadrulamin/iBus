package com.shadowcoder.ibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StudentProfileEditActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mMatricNoField, enterPassword, confirmPassword, oldPassword;

    private Button mBack, mConfirm;

    private TextView mChangePass, enterPassError, confirmPassError, mEmailField;

    private ImageView mProfileImage, mGetImage;

    private DatabaseReference mStudentDatabase;

    private String userId, mName, mPhone, mMatricNo, mProfileImageUrl;

    private Uri resultUri;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile_edit);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        user = FirebaseAuth.getInstance().getCurrentUser();

        mNameField = findViewById(R.id.studentNameProfile);
        mPhoneField = findViewById(R.id.studentPhoneProfile);
        mMatricNoField = findViewById(R.id.studentMatricNoProfile);
        mEmailField = findViewById(R.id.studentEmailProfile);

        //mBack = findViewById(R.id.studentBackButtonProfile);
        mConfirm = findViewById(R.id.studentConfirmButtonProfile);
        mChangePass = findViewById(R.id.studentChangePassword);
        mGetImage = findViewById(R.id.getImageStudent);

        mProfileImage = (ImageView) findViewById(R.id.studentImageProfile);


        mStudentDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId);

        getUserInfo();

        mGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //More Detail: https://github.com/ArthurHub/Android-Image-Cropper
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");


                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, 1);

            }
        });


       /* mProfileImage.setOnClickListener(new View.OnClickListener() {
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
        });*/

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateName() | !validatePhone() | !validateMatricNumber()) {
                    return;
                } else {
                    saveUserInformation();
                    Intent intent = new Intent(StudentProfileEditActivity.this, StudentProfileActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(), "Your information have been saved", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /*mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentProfileEditActivity.this, StudentProfileActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });*/


        mChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdatePasswordDialog();
            }
        });


    }

    private void showUpdatePasswordDialog() {
        final Dialog dialog = new Dialog(StudentProfileEditActivity.this);
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true);
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.dialog_update_password);

        //Initializing the views of the dialog.
        enterPassword = dialog.findViewById(R.id.enterPasswordET);
        confirmPassword = dialog.findViewById(R.id.confirmPasswordET);
        oldPassword = dialog.findViewById(R.id.oldPasswordET);
        enterPassError = dialog.findViewById(R.id.enterPassError);
        confirmPassError = dialog.findViewById(R.id.confirmPassError);


        Button updatePass = dialog.findViewById(R.id.updatePasswordButton);

        updatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pass = enterPassword.getText().toString();
                String confirmPass = confirmPassword.getText().toString();
                String oldPass = oldPassword.getText().toString();
                String currentUserEmail = user.getEmail();

                if (!validatePassword()){
                    return;
                }
                //Reauthenticate current user before changing password
                AuthCredential credential = EmailAuthProvider.getCredential(currentUserEmail, oldPass);
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(getApplication(), "Authentication Successful", Toast.LENGTH_SHORT).show();
                        user.updatePassword(pass).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplication(), "Password Change Successfully", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(getApplication(), "Password Change Failed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getApplication(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
                //Boolean hasAccepted = termsCb.isChecked();
                //populateInfoTv(name,age,hasAccepted);

                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void getUserInfo(){
        mStudentDatabase.addValueEventListener(new ValueEventListener() {
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
                    if (map.get("matricNo") != null){
                        mMatricNo = map.get("matricNo").toString();
                        mMatricNoField.setText(mMatricNo);
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
        mMatricNo = mMatricNoField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("matricNo", mMatricNo);
        mStudentDatabase.updateChildren(userInfo);

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
                    Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Got the uri
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", uri.toString());
                            mStudentDatabase.updateChildren(newImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(getApplication(), "Failed to upload", Toast.LENGTH_LONG).show();
                        }
                    });

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

    private Boolean validateMatricNumber() {
        String val = mMatricNoField.getText().toString();
        if (val.isEmpty()) {
            mMatricNoField.setError("Field cannot be empty");
            return false;
        } else {
            mMatricNoField.setError(null);
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

    // defining our own password pattern
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=])" +     // at least 1 special character
                    "(?=\\S+$)" +            // no white spaces
                    ".{4,}" +                // at least 4 characters
                    "$");

    private Boolean validatePassword() {

        // if password field is empty
        // it will display error message "Field can not be empty"
        String pass = enterPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();
        if (pass.isEmpty() | confirmPass.isEmpty()) {
            enterPassError.setText("Field can not be empty");
            confirmPassError.setText("Field can not be empty");
            return false;
        }

        // if password does not matches to the pattern
        // it will display an error message "Password is too weak"
        else if (!PASSWORD_PATTERN.matcher(confirmPass).matches()) {
            enterPassError.setText("Password is too weak");
            confirmPassError.setText("Password is too weak");
            return false;
        }
        else if (pass.equals(confirmPass)){
            enterPassword.setError(null);
            confirmPassword.setError(null);
            return true;

        }
        else {
            Toast.makeText(getApplication(), "Password not match", Toast.LENGTH_LONG).show();
            return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data!= null){
            final Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

            //resultUri = imageUri;
            //mProfileImage.setImageURI(resultUri);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                mProfileImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(StudentProfileEditActivity.this, StudentProfileActivity.class);
        startActivity(intent);
        finish();
    }
}