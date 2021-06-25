package com.example.csass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText editprofilename,editprofileemail,editprofilephone;
    ImageView editprofileimage;
    Button savebutton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data =   getIntent();
        String FullName =   data.getStringExtra("FullName");
        String Email    =   data.getStringExtra("Email");
        String Phone    =   data.getStringExtra("Phone");

        editprofilename     =   findViewById(R.id.editprofilename);
        editprofileemail    =   findViewById(R.id.editprofileemail);
        editprofilephone    =   findViewById(R.id.editprofilephone);
        editprofileimage    =   findViewById(R.id.editprofileimageview);
        savebutton          =   findViewById(R.id.saveeprofile);

        fAuth               =   FirebaseAuth.getInstance();
        fStore              =   FirebaseFirestore.getInstance();
        user                =   fAuth.getCurrentUser();
        storageReference    =   FirebaseStorage.getInstance().getReference();;

        StorageReference profileRef =   storageReference.child("user/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(editprofileimage);
            }
        });

        editprofileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editprofilename.getText().toString().isEmpty() || editprofileemail.getText().toString().isEmpty() || editprofilephone.getText().toString().isEmpty()){
                    Toast.makeText(EditProfile.this, "Got Field/Fields is/are Empty", Toast.LENGTH_SHORT).show();
                }

                String email =  editprofileemail.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DocumentReference documentReference =   fStore.collection("users").document(user.getUid());
                        Map<String,Object>  edited  =   new HashMap<>();
                        edited.put("Email",email);
                        edited.put("FullName",editprofilename.getText().toString());
                        edited.put("Phone",editprofilephone.getText().toString());
                        documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(EditProfile.this, "Profile is Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();
                            }
                        });
                        Toast.makeText(EditProfile.this, "Field is Updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        editprofileemail.setText(Email);
        editprofilename.setText(FullName);
        editprofilephone.setText(Phone);


        Log.d(TAG,"onCreate: "+ FullName + " " + Email + " " + Phone );



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000){
            if (resultCode== Activity.RESULT_OK){
                Uri imageUri=data.getData();
                //profileImage.setImageURI(imageUri);

                uploadImagetoFirebase(imageUri);


            }
        }
    }

    private void uploadImagetoFirebase(Uri imageUri) {
        //upload image to firebase storage
        StorageReference fileRef = storageReference.child("user/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(editprofileimage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to Upload", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
