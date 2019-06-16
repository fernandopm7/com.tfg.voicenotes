package com.voicenotes.view.initialization;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.voicenotes.R;
import com.voicenotes.view.library.BibliotecaActivity;
import com.voicenotes.view.utils.recordingqueue.ThreadQueue;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;
import com.voicenotes.view.utils.recordingqueue.Transcriber;


public class InitProgressBarActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    Integer count =1;
    InitProgressBarActivity thisClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_progress_bar);
        progressBar = findViewById(R.id.progressBar);
        thisClass=this;
        progressBar.setProgress(0);
        new MyTask().execute();

    }



    class MyTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {


            DefaultSettingsXmlParser.initialize(thisClass);

            //inicializamos el decoder de CMUSPHINX con los modelos..etc
            Transcriber.initialize(getApplicationContext());
            ThreadQueue.initialize(getApplicationContext());
            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(thisClass, BibliotecaActivity.class);
            startActivity(intent);
            finish();

        }
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected void onProgressUpdate(Integer... values) {


          //  progressBar.setProgress(values[0]);
        }
    }
}
