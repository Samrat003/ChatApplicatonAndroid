package com.example.flappy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {
    private Button sendVerificationCodeButton,VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth=FirebaseAuth.getInstance();

        sendVerificationCodeButton=findViewById(R.id.send_verification_code_button);
        VerifyButton=findViewById(R.id.send_verify_button);
        InputPhoneNumber=findViewById(R.id.phone_number_input);
        InputVerificationCode=findViewById(R.id.verification_code_input);
        loadingBar=new ProgressDialog(this);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String PhoneNumber=InputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(PhoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while we are authenticating your phone... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            PhoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                }
            }
        });


        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode=InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please write code first...", Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, while we are verifying your code... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });




        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please Enter Correct Number with Your Country Code", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check...", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }





    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulation you are logged in...", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }else{
                            String message=task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this,"Error :"+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}
