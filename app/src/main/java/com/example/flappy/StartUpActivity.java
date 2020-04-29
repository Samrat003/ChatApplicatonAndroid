package com.example.flappy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class StartUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        try
        {
            this.getSupportActionBar().hide();
        }

        catch(NullPointerException e){}
        android.os.Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i=new Intent(StartUpActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        },3000);
    }
}
