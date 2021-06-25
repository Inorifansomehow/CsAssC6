package com.example.csass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class Verify extends AppCompatActivity {

    EditText countryCode,phoneNumber,enterOTP;
    Button sendButton,verifyOTP,resendOTP;
    String userphonenumber,verificationid;
    PhoneAuthProvider.ForceResendingToken token;
    FirebaseAuth fAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        countryCode     =   findViewById(R.id.countrycode);
        phoneNumber     =   findViewById(R.id.phone2);
        enterOTP        =   findViewById(R.id.enterotp);
        resendOTP       =   findViewById(R.id.resend);
        verifyOTP       =   findViewById(R.id.verify);
        sendButton      =   findViewById(R.id.sendotp);
        resendOTP.setEnabled(false);

        fAuth           =   FirebaseAuth.getInstance();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countryCode.getText().toString().isEmpty()){
                    countryCode.setError("Country Code is Required");
                }
                if (phoneNumber.getText().toString().isEmpty()){
                    phoneNumber.setError("Phone Number is Missing");
                }

                userphonenumber =   "+"+countryCode.getText().toString()+phoneNumber.getText().toString();
                verifyphonenumber(userphonenumber);

                Toast.makeText(Verify.this, userphonenumber, Toast.LENGTH_SHORT).show();
            }
        });

        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyphonenumber(userphonenumber);
                resendOTP.setEnabled(false);
            }
        });

        verifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the OTP
                if (enterOTP.getText().toString().isEmpty()){
                    enterOTP.setError("Enter OTP");
                }

                PhoneAuthCredential credential  =   PhoneAuthProvider.getCredential(verificationid,enterOTP.getText().toString());
                authentication(credential);

            }
        });


        callbacks   =   new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                authentication(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {
                Toast.makeText(Verify.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationid  =   s;
                token           =   forceResendingToken;
                countryCode.setVisibility(View.GONE);
                phoneNumber.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);

                enterOTP.setVisibility(View.VISIBLE);
                resendOTP.setVisibility(View.VISIBLE);
                verifyOTP.setVisibility(View.VISIBLE);
                resendOTP.setEnabled(false);

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull @NotNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                resendOTP.setEnabled(true);
            }
        };


    }
    public void verifyphonenumber(String phonenum){
        //send OTP
        PhoneAuthOptions options    =   PhoneAuthOptions.newBuilder(fAuth)
            .setActivity(this)
                .setPhoneNumber(phonenum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    public void authentication(PhoneAuthCredential credential){
        fAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(Verify.this, "Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(Verify.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        }
    }
