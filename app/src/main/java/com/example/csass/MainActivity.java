package com.example.csass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

public class MainActivity<implementation> extends AppCompatActivity {
    public static final String TAG = "tag";
    TextView fullname,email,phone,verifyMSG;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String userID;
    Button resendCode,resetPasswordLocal, changeProfile;
    FirebaseUser user;
    ImageView profileImage;
    StorageReference storageReference;

    private FirebaseAnalytics mFirebaseAnalytics;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        phone                =   findViewById(R.id.profilephone);
        fullname             =   findViewById(R.id.profilename);
        email                =   findViewById(R.id.profileemail);
        resetPasswordLocal   =   findViewById(R.id.resetpassword);

        profileImage    =   findViewById(R.id.profileimage);
        changeProfile   =   findViewById(R.id.changeprofile);

        fAuth               =   FirebaseAuth.getInstance();
        fstore              =   FirebaseFirestore.getInstance();
        storageReference    =   FirebaseStorage.getInstance().getReference();

        StorageReference profileRef =   storageReference.child("user/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        resendCode  =   findViewById(R.id.button2);
        verifyMSG   =   findViewById(R.id.verifymsg);

        userID  =   fAuth.getCurrentUser().getUid();
        user=fAuth.getCurrentUser();

        if (!user.isEmailVerified()){
            verifyMSG.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(v.getContext(), "Verification Link Had Been Sent", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d(TAG,"onFailure: Email Does Not Exist"+e.getMessage());
                        }
                    });
                }
            });
        }
        DocumentReference documentReference =   fstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                phone.setText(value.getString("Phone"));
                fullname.setText(value.getString("FullName"));
                email.setText(value.getString("Email"));
            }
        });
        resetPasswordLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetPassword =new EditText(v.getContext());
                final AlertDialog.Builder passwordresetdialog= new AlertDialog.Builder(v.getContext());
                passwordresetdialog.setTitle("Reset Password");
                passwordresetdialog.setMessage("Enter New Password.");
                passwordresetdialog.setView(resetPassword);

                passwordresetdialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract the email and send the reset link
                        String newPassword  =   resetPassword.getText().toString();

                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MainActivity.this, "Password is Reset Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(MainActivity.this, "Password is Failed to Reset", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordresetdialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Close
                    }
                });
                passwordresetdialog.create().show();
            }
        });

        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent i    =   new Intent(v.getContext(),EditProfile.class);
                i.putExtra("FullName",fullname.getText().toString());
                i.putExtra("Email",email.getText().toString());
                i.putExtra("Phone",phone.getText().toString());
                startActivity(i);
               // Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
               // startActivityForResult(openGalleryIntent,1000);
            }
        });

    ;}



    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}