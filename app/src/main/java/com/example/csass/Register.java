package com.example.csass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    public static final String TAG1 = "TAG";
    EditText mFullName,mEmail,mPassword,mPhone,mRetypePassword,mCountryCode;
    Button mRegisterButton;
    TextView mLoginButton;
    FirebaseAuth fAuth;
    ProgressBar ProgressBar;
    FirebaseFirestore fstore;
    String userID;
    private static final Pattern Password_Pattern   =   Pattern.compile(
            "^"+
                    "(?=.*[0-9])"+              //at least 1 digit
                    //"(?=.*[a-z])"+              //at lesat 1 lower case letter
                    "(?=.*[A-Z])"+              //at least 1 upper case letter
                    "(?=.*[@#$%^&+=_-])"+       //at least 1 special character
                    "(?=\\S+$)"+                //no space bar
                    ".{3,}"+                    //at least 3 characters
                    "$"
    );


    Boolean isdatavalid =   false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName           =    findViewById(R.id.fullname);
        mEmail              =    findViewById(R.id.email);
        mPassword           =    findViewById(R.id.password);
        mPhone              =    findViewById(R.id.phone);
        mRegisterButton     =    findViewById(R.id.registor);
        mLoginButton        =    findViewById(R.id.createtext);
        mRetypePassword     =    findViewById(R.id.retypepassword);
        mCountryCode        =    findViewById(R.id.countrycode);


        fAuth       =   FirebaseAuth.getInstance();
        fstore      =   FirebaseFirestore.getInstance();
        ProgressBar =   findViewById(R.id.progressbar);

        if (fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),Register.class));
            finish();
        }



        mRegisterButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View a){
                String email    =   mEmail.getText().toString().trim();
                String password =   mPassword.getText().toString().trim();
                String fullname =   mFullName.getText().toString();
                String phone    =   mPhone.getText().toString();
                String retype   =   mRetypePassword.getText().toString().trim();
                String countrycode  =   mCountryCode.getText().toString();


                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //Identify Pattern of Email
                    mEmail.setError("Please Enter a Valid Address");
                    return;
                }
                if (TextUtils.isEmpty(fullname)){
                    mFullName.setError("Your Name is Required");
                }
                if (TextUtils.isEmpty(phone)){
                    mPhone.setError("Phone Number is Missing");
                }
                if (TextUtils.isEmpty(countrycode)){
                    mCountryCode.setError("Country Code is Required");
                }
                if (TextUtils.isEmpty(password)){
                    mPassword.setError(("Password is Required"));
                    return;
                }else if (!Password_Pattern.matcher(password).matches()){
                    mPassword.setError("Password is Too Weak, 1 Upper Case, Special Case, Digit is Required");
                    return;
                }else if (password.length() <6){
                    mPassword.setError("Password is less than 6 Character");
                }
                if (TextUtils.isEmpty(retype)){
                    mRetypePassword.setError("Retype Password is Required");
                    return;
                }
                if (!password.equals(retype)){
                    isdatavalid =   false;
                    mRetypePassword.setError("Passowrd Does Not Match");
                }else{
                    isdatavalid =   true;
                }
                ProgressBar.setVisibility(View.VISIBLE);

                if (isdatavalid){
                    // proceed the registration

                    //register use in firebase
                    fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                //Send Verification Link to Email
                                FirebaseUser user1  =   fAuth.getCurrentUser();
                                user1.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(Register.this, "Verification Link Had Been Sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Log.d(TAG,"onFailure: Email Does Not Exist"+e.getMessage());
                                    }
                                });


                                Toast.makeText(Register.this, "User is Created.", Toast.LENGTH_SHORT).show();
                                userID = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference =  fstore.collection("users").document(userID);
                                Map<String,Object> user = new HashMap<>();
                                user.put("FullName",fullname);
                                user.put("Email",email);
                                user.put("Phone",countrycode+phone);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG,"User Profile is Created for "+userID);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Log.d(TAG,"onFailure: "+e.toString());
                                    }
                                });
                                startActivity(new Intent(getApplicationContext(),Login.class));
                            }else{
                                Toast.makeText(Register.this, "Error !!"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                ProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }


            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

    }

}