package com.shadowcoder.ibus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DriverSignUpActivity extends AppCompatActivity {



    private EditText mEmailField, mPasswordField, mNameField, mStaffIdField, mPhoneField, mBusRegistrationNumberField;
    private Button mSignup, mBack;
    private String user_id;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private DatabaseReference mDriverDatabase;

    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sign_up);

        mEmailField = findViewById(R.id.driverEmailSignup);
        mPasswordField = findViewById(R.id.driverPasswordSignup);
        mNameField = findViewById(R.id.driverNameSignup);
        mStaffIdField = findViewById(R.id.driverStaffIdSignup);
        mPhoneField = findViewById(R.id.driverPhoneSignup);
        mSignup = findViewById(R.id.driverSignupButton);
        //mBack = findViewById(R.id.driverBackButtonSignup);
        mBusRegistrationNumberField = findViewById(R.id.driverBusRegistrationSignup);

        spinner=(ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();


        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Validate signup info
                if (!validateEmail() | !validatePassword() | !validateStaffId() | !validatePhone() | !validateName() | !validateBusRegistrationNumber()) {

                    return;
                }

                    final String email = mEmailField.getText().toString();
                    final String password = mPasswordField.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverSignUpActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                            } else {
                                spinner.setVisibility(View.VISIBLE);
                                user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                current_user_db.setValue(true);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                        saveUserInformation();

                                        Intent intent = new Intent(DriverSignUpActivity.this, RouteRegisterActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                },2000);
                            }
                        }
                    });



            }
        });

//        mBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(DriverSignUpActivity.this, DriverLoginActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
    }



    private void saveUserInformation() {
        String mName = mNameField.getText().toString();
        String mPhone = mPhoneField.getText().toString();
        String mStaffId = mStaffIdField.getText().toString();
        String mBusPlateNo = mBusRegistrationNumberField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("staffId", mStaffId);
        userInfo.put("busRegistrationNo", mBusPlateNo);

        mDriverDatabase.updateChildren(userInfo);
        finish();

    }


    private Boolean validateEmail() {
        String emailInput = mEmailField.getText().toString();
        if (emailInput.isEmpty()) {
            mEmailField.setError("Field cannot be empty");
            return false;
        }

        // Matching the input email to a predefined email pattern
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            mEmailField.setError("Please enter a valid email address");
            return false;
        } else {
            mEmailField.setError(null);
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
        String passwordInput = mPasswordField.getText().toString();
        if (passwordInput.isEmpty()) {
            mPasswordField.setError("Field cannot be empty");
            return false;
        }

        // if password does not matches to the pattern
        // it will display an error message "Password is too weak"
        else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            mPasswordField.setError("Password is too weak");
            return false;
        } else {
            mPasswordField.setError(null);
            return true;
        }
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

    private Boolean validateBusRegistrationNumber() {
        String val = mPhoneField.getText().toString();
        if (val.isEmpty()) {
            mBusRegistrationNumberField.setError("Field cannot be empty");
            return false;
        } else {
            mBusRegistrationNumberField.setError(null);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent_back = new Intent(DriverSignUpActivity.this, DriverLoginActivity.class);
        startActivity(intent_back);
        finish();
    }
}