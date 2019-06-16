package com.voicenotes.view.record;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.voicenotes.R;
import com.voicenotes.services.VoiceNotesService;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.library.BibliotecaActivity;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;
import com.voicenotes.view.utils.AudioTagsHelper;
import com.voicenotes.view.utils.recordingqueue.Transcriber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

public class RecordActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_STORAGE = 1;
    private static final int REQUEST_CODE_RECORD = 2;
    private ImageButton stop, record, back;
    private MediaRecorder myAudioRecorder;
String durationToSave ="";
    private Config c;
    private Decoder decoder;
    private AudioRecord recorder;
    private int sampleRate;
    private int minBufferSize;
    public Date currentDate;
    public boolean isRecording = true;

    private VoiceNotesService voiceNotesService = null;
    SpeechRecognizerThread srt;
    private FileOutputStream audioOutputStream = null;

    private TextView timerValue;


    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    public String currenteTranscriptionLanguage = "es-es";

public static BibliotecaActivity currentBib = null;

    private Date getDiaMesAño(){
        Date date = new Date();
        return date;

       // return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

public static  void setBib (BibliotecaActivity bib){
        currentBib = bib;
}

    //  int sampleRate = (int)this.decoder.getConfig().getFloat("-samprate");
    //   float bufferSize = Math.round((float)this.sampleRate * 0.4F);
    //   AudioRecord ar = new AudioRecord(6,sampleRate,16,2,bufferSize*2);

    // private String outputFile = outputFile =Environment.getExternalStoragePublicDirectory(
    //      Environment.DIRECTORY_MUSIC) + "/_lastRecording.3gp" ;
    public String outputFile = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MUSIC) + "/_lastRecording.wav";
    private String currentTranscription;
    private final String finalPath = getNewPath(outputFile);
    private String resultText ;
    private String tagSelected;
    FFmpeg ffmpeg;
    String res="";
    ArrayList<String> audioList   = new ArrayList<>();
    ArrayList<String> pathsList   = new ArrayList<>();
    public String getNewPath (String oldPath){
        File f = new File (oldPath);
        String result =f.getName().replaceAll("(\\.)([a-zA-Z0-9_]*)",".wav");
        result = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC) + "/" +result ;
        return result;
        //return  oldPath.replaceAll("(\\.)([a-zA-Z0-9_]*)",".wav");

    }

    public String buscarEnAudioList(String valor){
        int count=0;

        for (String elem: audioList){
            if (elem.contains(valor)){
                return pathsList.get(count);

            }
            count++;
        }
        return null;

    }

   // public void saveAsModoAccesibilidad(final Date horaMinSeg){
        //currentBib.ConvertTextToSpeech("now give a name to the voice note");
       // while(currentBib.tts.isSpeaking()){
          //  //mientras el asistente hable, no hacer nada.
        //}
        //currentBib.askingName = true;
        //currentBib.recognizer.startListening(currentBib.ACTION_SEARCH,10); //action_search nos vale..por el tipo de reconocimiento que sa va hacer
        //while (!currentBib.askingName){
          // System.out.println("****************** ASKING NAME **********************");
         //   //esperamos que el usuario termine de indicar un nombre para la grbación
       // }
      //  String name = currentBib.recordingName;

       ////ahora preguntamos por el tag
       // currentBib.ConvertTextToSpeech("ok now specify a tag please");
       // while(currentBib.tts.isSpeaking()){
         //   //mientras el asistente hable, no hacer nada.
       // }
       // currentBib.askingTag = true;
       // currentBib.recognizer.startListening(currentBib.ACTION_SEARCH,10000);
       // while (currentBib.askingTag){
         //   //esperamos que el usuario termine de indicar un tag para la grbación
        //    System.out.println("****************** ASKING TAG **********************");
       // }
        //String tag = currentBib.tagName;

       // //ahora guardamos, por ahora no incluimos el sobreescribir, si ya esxiste se sobreescribe.
        //String path = name + ".wav";
        //final File aux = new File(buscarEnAudioList(path));
        //res = path;
       // aux.delete();
        //final String destName = Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_MUSIC) + "/" + res;
       // System.out.println("1111111111");

        // //todo mecanica de tags here..
        //File filex = new File(destName);
       // AudioInfo audioInfo = new AudioInfo(filex.getName(), null, null,true,tagSelected,horaMinSeg,getDiaMesAño());
      //  File currentRecording = new File(outputFile);

      // // File wavFile = ObjectsManager.writeFile(getApplicationContext(), "recordings", filex.getName(), currentRecording);

       // //AudioMap.setAudioInfo(getApplicationContext(), filex.getName(), audioInfo);
      //  //ThreadQueue.addElement(filex.getName());


       // currentBib.ConvertTextToSpeech("the voice note successfully created");
      //  while (currentBib.tts.isSpeaking()){

      //  }
    //    currentBib.asistenteGrabacionActivado=false;
  //      currentBib.recognizer.startListening(currentBib.KWS_SEARCH);
  //  }






    public void isThatFileAlreadyExist(final String path, final String tagSelected, final Date horaMinSeg) {
        //devuelve el destName final luego de comprobar si existe..preguntar sobreescribir,etc

        System.out.println("***************************************");
        System.out.println("***************************************");
        System.out.println("path constains:  " + path);
        System.out.println("***************************************");
        System.out.println("*****************************************");
        if (audioList.contains(path)) {
//abbb
            System.out.println("ENTROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");

            LayoutInflater layoutInflater = LayoutInflater.from(RecordActivity.this);
            View promptView = layoutInflater.inflate(R.layout.overwrite_file, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(promptView);
            final EditText editText = (EditText) promptView.findViewById(
                    R.id.edittext2);

            //builder.setTitle("overwrite file?");
            // builder.setMessage("overwrite file?");

            //hola<z

            builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final File aux = new File(buscarEnAudioList(path));
                    res = path;
                    aux.delete();
                    final String destName = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MUSIC) + "/" + res;
                    System.out.println("1111111111");

                    //todo mecanica de tags here..
                    File filex = new File(destName);
                    //AudioInfo audioInfo = new AudioInfo(filex.getName(), null, null,true,tagSelected,horaMinSeg,getDiaMesAño());
                    File currentRecording = new File(outputFile);

                    //File wavFile = ObjectsManager.writeFile(getApplicationContext(), "recordings", filex.getName(), currentRecording);

                    //AudioMap.setAudioInfo(getApplicationContext(), filex.getName(), audioInfo);
                    //ThreadQueue.addElement(filex.getName());
                    voiceNotesService.processAudio(getApplicationContext(),currentRecording,filex.getName(),tagSelected,getDiaMesAño(),horaMinSeg,currenteTranscriptionLanguage);

                    dialog.dismiss();


                }

            });


            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {


                    res = editText.getText().toString();
                    final String destName = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_MUSIC) + "/" + res + ".wav";
                    System.out.println("222222222");
                    File filex = new File(destName);
  //                  AudioInfo audioInfo = new AudioInfo(filex.getName(), null, null,true,tagSelected,horaMinSeg,getDiaMesAño());
                    File currentRecording = new File(outputFile);


//                    File wavFile = ObjectsManager.writeFile(getApplicationContext(), "recordings", filex.getName(), currentRecording);

                    //AudioMap.setAudioInfo(getApplicationContext(), filex.getName(), audioInfo);
                    //ThreadQueue.addElement(filex.getName());
                    voiceNotesService.processAudio(getApplicationContext(),currentRecording,filex.getName(),tagSelected,getDiaMesAño(),horaMinSeg,currenteTranscriptionLanguage);

                    //dialog.dismis.. ??
                }

            });


            AlertDialog alert = builder.create();
            alert.show();
        } else {
            res = path;
            //caso audio no existe antes en lista..
            System.out.println("******************************************");
            System.out.println("******** resultIsdir..: " + res + "*******");
            System.out.println("*************************************");
            final String destName = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC) + "/" + res;
            System.out.println("33333333");


            File filex = new File(destName);
          //  AudioInfo audioInfo = new AudioInfo(filex.getName(), null, null,true,tagSelected,horaMinSeg,getDiaMesAño());
            File currentRecording = new File(outputFile);

           // File wavFile = ObjectsManager.writeFile(getApplicationContext(), "recordings", filex.getName(), currentRecording);

            //AudioMap.setAudioInfo(getApplicationContext(), filex.getName(), audioInfo);
            //ThreadQueue.addElement(filex.getName());
            voiceNotesService.processAudio(getApplicationContext(),currentRecording,filex.getName(),tagSelected,getDiaMesAño(),horaMinSeg,currenteTranscriptionLanguage);


        }
    }

    AdapterView.OnItemSelectedListener tagsListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

   // private List<String> getTagsFromMap(){
     //   List<String> tags = new ArrayList<String>();
      //  tags.add("Select a tag");
      //  for (String key: AudioMap.getkeys()){
       //     tags.add(AudioMap.getAudioInfo(key).getTag());
       // }
       // tags.add("create new tag");
        //return tags;
    //}
    public void saveAudioAs (final Date horaMinSeg){

        //cuadro de dialogo de guardar
        // get prompts.xml view
        // LayoutInflater layoutInflater = LayoutInflater.from( RecordActivity.this);
        View promptView = getLayoutInflater().inflate(R.layout.saveasdialog, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( RecordActivity.this);
        alertDialogBuilder.setView(promptView);
        final Switch switchIdiomas = promptView.findViewById(R.id.switchIdiomas);
        switchIdiomas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) { //on position
                    currenteTranscriptionLanguage = "en-us";
                }else{
                    currenteTranscriptionLanguage= "es-es";
                }
            }
        });
        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        //tags

        final Spinner mSpinner = (Spinner) promptView.findViewById(R.id.spinnerX4);
        List<String> elements = new ArrayList<String>();
        elements.addAll(AudioTagsHelper.getSpinnerTags(getApplicationContext()));
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(RecordActivity.this,android.R.layout.simple_spinner_item,elements);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tagSelected = adapterView.getItemAtPosition(i).toString();
                boolean nuevoTag = tagSelected.contentEquals(getString(R.string.create_new_tag));

                if (nuevoTag) {
                    ///new way
                    LayoutInflater layoutInflater2 = LayoutInflater.from(RecordActivity.this);
                    View promptView2 = layoutInflater2.inflate(R.layout.new_tag_dialog, null);
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(RecordActivity.this);
                    builder2.setView(promptView2);
                    final EditText textEditTagSelected = (EditText) promptView2.findViewById(R.id.taginput);
                    System.out.println("Testing tagSpinner: onItemSelected:nuevoTag");

                    // builder2.setTitle("overwrite file?");
                    //builder2.setMessage("overwrite file?");

                    builder2.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do something here
                            tagSelected = textEditTagSelected.getText().toString();
                            dialog.dismiss();
                        }
                    });

                    builder2.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    AlertDialog alert2 = builder2.create();
                    alert2.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Spinner Drop down elements
        //todo obtener los tags del mapa
        /*
        final List<String> tags = getTagsFromMap();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tags);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        System.out.println("Testing tagSpinner: luego de declararlo");
        tagSelected = "otros"; //inicializamos a "otros".
// Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("Testing tagSpinner: on itemSelected");
                //todo comprobar que se seleeciono create new tag
                //todo desplegamos el nuevo dialogo para crear un nuevo tag
                tagSelected = adapterView.getItemAtPosition(i).toString();
               boolean nuevoTag = tagSelected.contentEquals("create new tag");
                if (nuevoTag) {
                    ///new way
                    LayoutInflater layoutInflater2 = LayoutInflater.from(RecordActivity.this);
                    View promptView2 = layoutInflater2.inflate(R.layout.new_tag_dialog, null);
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(RecordActivity.this);
                    builder2.setView(promptView2);
                    final EditText textEditTagSelected = (EditText) promptView2.findViewById(R.id.taginput);
                    System.out.println("Testing tagSpinner: onItemSelected:nuevoTag");

                   // builder2.setTitle("overwrite file?");
                     //builder2.setMessage("overwrite file?");


                    builder2.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                          //do something here
                            tagSelected = textEditTagSelected.getText().toString();
                            dialog.dismiss();


                        }

                    });


                    builder2.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                        }

                    });


                    AlertDialog alert2 = builder2.create();
                    alert2.show();



                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

*/
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("*****AQUIIIIIIIIIII*****");
                        System.out.println("outputFile: "+outputFile + "  finalPath= "+finalPath);
                        //cambiamos el nombre
                        resultText = editText.getText().toString(); //obtenemos el nombre introducido por el usuario
                        System.out.println("**************DEBUGING  m_text:  " +resultText);
                        //tag seleccionado y logica implicada

                        System.out.println("RecordActivity:onSaveAs:acepted:tagSelected: "+  tagSelected);

                        isThatFileAlreadyExist(
                                resultText + ".wav",tagSelected,horaMinSeg);

                    }
                })
                .setNegativeButton("cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        alertDialogBuilder.setView(promptView);

        // create an alert dialog
        runOnUiThread(new Runnable() {
            @ Override
            public void run() {
                //Create the alert dialog
                AlertDialog mDialog = alertDialogBuilder.create();
                mDialog.show();
            }
        });


    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //  Log.v(TAG,"Permission is granted");

            } else {

                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
            }
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_RECORD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso aceptado
                    System.out.println("RecordActibity: OnRequestPermissionResults, granted");
                    srt = new SpeechRecognizerThread();
                    srt.start();

                    stop.setEnabled(true);
                    record.setEnabled(false);
                }
                else{
                    System.out.println("RecordActibity: OnRequestPermissionResults.record, deny");
                    // Permiso denegado
                }
                return;
            // Gestionar el resto de permisos

            case REQUEST_CODE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("RecordActibity: OnRequestPermissionResults.storage, granted");
                    // Permiso aceptado

                }
                else{
                    System.out.println("RecordActibity: OnRequestPermissionResults.storage, deny");
                    // Permiso denegado
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void grabar(){
        System.out.println("RecordActibity: grabar");
        stop.setVisibility(View.VISIBLE);
        record.setVisibility(View.INVISIBLE);

        startTime = SystemClock.uptimeMillis();

        customHandler.postDelayed(updateTimerThread, 0);

        //iniciar contador aqui..

        try {
            audioOutputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // try {
        //     myAudioRecorder.prepare();
        //      myAudioRecorder.start();
        //  } catch (IllegalStateException ise) {
        //      // make something ...
        //  } catch (IOException ioe) {
        //      // make something
        //  }
        // record.setEnabled(false);



        System.out.println("RecordActibity: record.setOnClickListener");
        // Comprobar permiso
        // int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);

        // if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
        //  System.out.println("RecordActibity: record.setOnClickListener: permission granted");
        srt = new SpeechRecognizerThread();
        srt.start();

        stop.setEnabled(true);
        record.setEnabled(false);
        //} else {
        //   ActivityCompat.requestPermissions(RecordActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD);


        //}




        //  Transcriber.startRecording();
        //  Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
    }

    public void backActivity(){
        onBackPressed();
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record2);
        BibliotecaActivity.saveCurrentRec(this);
        voiceNotesService = new VoiceNotesService();
        //isStoragePermissionGranted();
        //inicializamos los modelos de pocketshinx..

        sampleRate = 16000;
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        recorder =
                new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufferSize);

        System.out.println("recordActivity: onCreate: recorder: " + recorder);
        Map<String,AudioInfo> mapa =  voiceNotesService.getVoiceNotesMap(getApplicationContext());
        for (String name:mapa.keySet()) {
            audioList.add(name);
            pathsList.add(mapa.get(name).getWavPath());
        }

        back = findViewById(R.id.backRecord);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        stop = (ImageButton) findViewById(R.id.voiceStop);
        record = (ImageButton) findViewById(R.id.voiceRecord);
        stop.setEnabled(false);
        timerValue = findViewById(R.id.timerValue);



        // myAudioRecorder = new MediaRecorder();
        //myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //  myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //  myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        //   myAudioRecorder.setOutputFile(outputFile);

        //  // myAudioRecorder.setAudioSamplingRate(8000);
        System.out.println("****** RECORD OUTPUT HEREEEEE: "+ outputFile );





        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("entre grabar onclick");
                isRecording=true;
                grabar();
            }});


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop.setVisibility(View.INVISIBLE);
                record.setVisibility(View.VISIBLE);

                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);

                //detemos timer aqui.
            Date date = new Date(updatedTime);
                SimpleDateFormat formato = new SimpleDateFormat("HH/mm/ss");
                String durationToSave =formato.format(date);



                //Transcriber.stopRecording();
                // myAudioRecorder.stop();
                // myAudioRecorder.release();
                //  myAudioRecorder = null;
                try {
                    srt.interrupt();
                    srt.join();
                } catch (InterruptedException e) {
                    // Restore the interrupted status.
                    Thread.currentThread().interrupt();
                }
                srt = null;
                record.setEnabled(true);
                stop.setEnabled(false);
                //   play.setEnabled(true);
                //   Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();

                //implementamos guardado ..
                //  saveAudioAs();


            }
        });

        if (currentBib.asistenteGrabacionActivado) {
            //currentBib.ConvertTextToSpeech("Recording will begin now");
            currentBib.ConvertTextToSpeech(getString(R.string.recording_will_begin_now));
            while(currentBib.tts.isSpeaking()){

            }
            record.performClick();
        }
    }



    private final class SpeechRecognizerThread extends Thread {

        private byte[] short2byte(short[] sData) {
            int shortArrsize = sData.length;
            byte[] bytes = new byte[shortArrsize * 2];
            for (int i = 0; i < shortArrsize; i++) {
                bytes[i * 2] = (byte) (sData[i] & 0x00FF);
                bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
                sData[i] = 0;
            }
            return bytes;
        }

        @Override public void run() { //graba el audio..

            // decoder.startUtt();
            recorder.startRecording();
            short[] buffer = new short[minBufferSize/2];

            while (!interrupted()) {
                int nread = recorder.read(buffer, 0, minBufferSize/2);

                if (-1 == nread) {
                    throw new RuntimeException("error reading audio buffer");
                } else if (nread > 0) {

                    byte bufferData[] = short2byte(buffer);
                    try {
                        // // writes the data to file from buffer
                        // // stores the voice buffer
                        audioOutputStream.write(bufferData, 0, bufferData.length);
                        //Log.d("Record", "Audio data actually saved");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            //onStop..
            recorder.stop();
            try {
                audioOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //  decoder.endUtt();
            //guardar como...

            Date date = new Date(updatedTime);
            //SimpleDateFormat formato = new SimpleDateFormat("mm:ss");
            //String durationToSave =formato.format(date);
            updatedTime=0L;
            timeInMilliseconds=0L;
            startTime=0L;
            timeSwapBuff=0L;
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI

                    timerValue.setText("" + "00" + ":"
                            + String.format("%02d", 0) + ":"
                            + String.format("%03d", 0));
                }
            });
            if (currentBib.asistenteGrabacionActivado){

                currentDate = date;
                currentBib.continueRecog();
                //saveAsModoAccesibilidad(date);
            }else {
                saveAudioAs(date);
            }
        }
    }


    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);
        }

    };


}