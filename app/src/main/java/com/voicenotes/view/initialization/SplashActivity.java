package com.voicenotes.view.initialization;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.voicenotes.R;
import com.voicenotes.view.utils.indexes.AudioIndexer;

public class SplashActivity extends AppCompatActivity {

ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressBar = findViewById(R.id.progressBar);

        System.out.println("SplashActivity: onCreate: inicio");
        //inicializarApp();
       // AudioMap.initialize(getApplicationContext());
        AudioIndexer.initialize(getApplicationContext());

        //hacer que espere medio segundo..
        Intent intent = new Intent(this, InitProgressBarActivity.class);
        startActivity(intent);
        finish();

        System.out.println("SplashActivity: onCreate: fin");
    }





}
