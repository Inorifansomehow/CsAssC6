package com.example.csass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    EditText mEmail,mPassword;
    Button mLoginBtn,mLoginPhoneBtn;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    TextView mCreateBtn, forgotLink;
    int attempt=1;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail          =   findViewById(R.id.email);
        mPassword       =   findViewById(R.id.password);
        progressBar     =   findViewById(R.id.progressbar2);
        fAuth           =   FirebaseAuth.getInstance();
        mLoginBtn       =   findViewById(R.id.loginbutton);
        //mLoginPhoneBtn  =   findViewById(R.id.loginphone);
        mCreateBtn      =   findViewById(R.id.createaccount);
        forgotLink      =   findViewById(R.id.forgotpassword);

        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email    =   mEmail.getText().toString().trim();
                String password =   mPassword.getText().toString().trim();


                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPassword.setError(("Password is Required"));
                    return;
                }
                if (password.length() < 6){
                    mPassword.setError("Password must be greater than 6 characters");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                //Limit Attempt
                if (attempt < 4) {
                    //Authenticate the user
                    fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(Login.this, "Login is Succeessfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            }else{
                                Toast.makeText(Login.this, "Email or Password are incorrect", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }else if (attempt==4){
                    Toast.makeText(Login.this, "Login Limit is Exceed", Toast.LENGTH_SHORT).show();
                }else if (attempt>4){
                    mLoginBtn.setEnabled(false);
                    // System.exit(0);
                }
                attempt++;

            }



        });

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });
        //mLoginPhoneBtn.setOnClickListener(new View.OnClickListener() {
          //  @Override
          //  public void onClick(View v) {
         //       startActivity(new Intent(getApplicationContext(),Verify.class));
         //   }
       // });



        forgotLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetmail=new EditText(v.getContext());
                final AlertDialog.Builder passwordresetdialog= new AlertDialog.Builder(v.getContext());
                passwordresetdialog.setTitle("Reset Password");
                passwordresetdialog.setMessage("Enter Your Email to Receive the Link.");
                passwordresetdialog.setView(resetmail);

                passwordresetdialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract the email and send the reset link

                        String mail=resetmail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login.this, "Reset Link Had Sent to Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(Login.this, "Yabai! Link is Not Sent"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                passwordresetdialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Back to Login View, Close dialog
                    }
                });



                passwordresetdialog.create().show();


            }
        });
    }
}