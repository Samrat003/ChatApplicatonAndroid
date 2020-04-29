package com.example.flappy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String reciverUserId,current_state,senderUserId;
    private CircleImageView userProfileImage;
    private TextView userProfileName,UserProfileStatus;
    private Button sendMessageRequest, DeclineMessageRequestButton;
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");
        senderUserId=mAuth.getCurrentUser().getUid();

        reciverUserId=getIntent().getExtras().get("visit_user_id").toString();
        userProfileImage=findViewById(R.id.visit_profile_image);
        userProfileName=findViewById(R.id.visit_profile_name);
        UserProfileStatus=findViewById(R.id.visit_profile_status);
        sendMessageRequest=findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=findViewById(R.id.decline_message_request_button);
        current_state="new";


        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        UserRef.child(reciverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage=dataSnapshot.child("image").getValue().toString();
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    UserProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
                else{
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    UserProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(reciverUserId)){
                            String request_type=dataSnapshot.child(reciverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                current_state="request_sent";
                                sendMessageRequest.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received")){
                                current_state="request_received";
                                sendMessageRequest.setText("Accept Chat Request");
                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else{
                            ContactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(reciverUserId)){
                                                current_state="friends";
                                                sendMessageRequest.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserId.equals(reciverUserId)){
            sendMessageRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequest.setEnabled(false);
                    if (current_state.equals("new")){
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")){
                            CancelChatRequest();
                    }
                    if (current_state.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (current_state.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else{
            sendMessageRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserId).child(reciverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(reciverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendMessageRequest.setEnabled(true);
                                            current_state="new";
                                            sendMessageRequest.setText("Send Message");

                                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });

    }




    private void AcceptChatRequest() {
        ContactsRef.child(senderUserId).child(reciverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(reciverUserId).child(senderUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                ChatRequestRef.child(senderUserId).child(reciverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    ChatRequestRef.child(reciverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendMessageRequest.setEnabled(true);
                                                                                    current_state="friends";
                                                                                    sendMessageRequest.setText("Remove this Contact");

                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserId).child(reciverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(reciverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendMessageRequest.setEnabled(true);
                                            current_state="new";
                                            sendMessageRequest.setText("Send Message");

                                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        ChatRequestRef.child(senderUserId).child(reciverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){


                            ChatRequestRef.child(reciverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                HashMap<String,String> chatNotificatioMap=new HashMap<>();
                                                chatNotificatioMap.put("from",senderUserId);
                                                chatNotificatioMap.put("type","request");

                                                NotificationRef.child(reciverUserId).push()
                                                        .setValue(chatNotificatioMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    sendMessageRequest.setEnabled(true);
                                                                    current_state="request_sent";
                                                                    sendMessageRequest.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });


                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
