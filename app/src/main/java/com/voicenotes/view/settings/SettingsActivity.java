package com.voicenotes.view.settings;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.voicenotes.R;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;
import com.voicenotes.view.utils.recordingqueue.Transcriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    Button appInfoButton;
    Button calibrationButton;
    Button saveSettingsButton;
    Button defaultSettingsButton;
    ImageButton back;
    List<String> categories ;
    List<String> categories2;
    String currentModel;

    ListView settingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_3);

        back = findViewById(R.id.backSettings);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        settingList = findViewById(R.id.settingList);
        settingList.setDivider(null);
        settingList.setAdapter(new SettingCustomAdapter(this));
    }
}
