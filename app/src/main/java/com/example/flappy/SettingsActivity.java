package com.example.flappy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int galaryPick=1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        InitializeFields();

        userName.setVisibility(View.INVISIBLE);//for not changing the user name once it created

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galaryIntent=new Intent();
                galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galaryIntent.setType("image/*");
                startActivityForResult(galaryIntent,galaryPick);
            }
        });
    }


    private void InitializeFields() {
        UpdateAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);
        SettingsToolBar=findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galaryPick && resultCode == RESULT_OK && data!= null){
            Uri imageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please Wait, while we are updating your profile image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri=result.getUri();
                final StorageReference filePath=UserProfileImagesRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(SettingsActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();


                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = String.valueOf(uri);
                                    Log.d("SettingsActivity", downloadUrl);
                                    RootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(SettingsActivity.this, "Image save successfully", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                    else
                                                    {
                                                        String message=task.getException().toString();
                                                        Toast.makeText(SettingsActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        String message=e.getMessage();
                        Toast.makeText(SettingsActivity.this, "Error :"+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                });

                //                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                ////                    @Override
                ////                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                ////                        if (!task.isSuccessful()) {
                ////                            Log.e("SettingsActivity", "Error!");
                ////                            task.getException().printStackTrace();
                ////                        }
                ////                        Log.d("SettingsActivity", "Cool!");
                ////                        return UserProfileImagesRef.getDownloadUrl();
                ////                    }
                ////                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                ////                    @Override
                ////                    public void onComplete(@NonNull Task<Uri> task) {
                ////                        if (task.isSuccessful()) {
                ////                            Uri downloadUri = task.getResult();
                ////                            Log.d("SettingsActivity", String.valueOf(downloadUri)) ;
                ////                            Toast.makeText(SettingsActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                ////                            loadingBar.dismiss();
                ////                        }
                ////                    }
                ////                });

            }
        }
    }

    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please Set Name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please Set Status", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                String massage=task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : "+massage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                        {
                            String retriveUserName=dataSnapshot.child("name").getValue().toString();
                            String retriveStatus=dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage=dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);

                            Picasso.get().load(retriveProfileImage).into(userProfileImage);

                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retriveUserName=dataSnapshot.child("name").getValue().toString();
                            String retriveStatus=dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);
                        }
                        else {
                            userName.setVisibility(View.VISIBLE); //for not change the user name
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information....", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent MainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}
