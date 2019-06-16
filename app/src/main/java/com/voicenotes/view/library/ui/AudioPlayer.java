package com.voicenotes.view.library.ui;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.voicenotes.R;
import com.voicenotes.view.library.helpers.AudioShareHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


public class AudioPlayer {

    String audioPath;
    String audioName;
    ImageButton play;
    ImageButton pause;
    ImageButton stop;
    ImageButton shareAudio;

    //Button audio2text;
    Boolean isPaused = false;
    public Boolean isPlaying = false;
    File audioFile;
    SeekBar seekBar;
    int progress=0;

    private int sampleRate;
    private int minBufferSize;
    AudioTrack audioTrack;
    private PlayThread playThread;

    public void play(){
        play.performClick();
        while (isPlaying){
            //esperamos que acabe la resproduccion
        }
        stopPlayer();

    }

    public AudioPlayer(Dialog dialog){
        play = dialog.findViewById(R.id.playButDialog);
    }
    public AudioPlayer(final Context context, String audioName,File audioFile, Dialog dialog
    ){
        //
        this.audioFile = audioFile;
        dialog.setContentView(R.layout.audio_player_dialog);
        dialog.setTitle("AudioPlayer");
        TextView nome = dialog.findViewById(R.id.namePlayerDialog);
        nome.setText(audioName);
        ImageButton playBut = dialog.findViewById(R.id.playButDialog);
        ImageButton pauseBut = dialog.findViewById(R.id.pauseButDialog);
        ImageButton stop = dialog.findViewById(R.id.backStopButDialog);
        ImageButton shareBut = dialog.findViewById(R.id.shareButDialog);
        SeekBar seekBarBut = dialog.findViewById(R.id.playerSeekBar);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               stopPlayer();
                dialog.dismiss();
            }
        });

        AudioShareHelper.init(context);

        //pillamos los parametros de la clase que nos llama:
        this.audioName = audioName;
      //  audioPath = AudioMap.getAudioInfo(audioName).getWavPath();
       // audioFile = (File) ObjectsManager.readFile(context,"recordings",audioName);

        //File recordingsFile =  new File(getApplicationContext().getFilesDir(), "recordings");
        // System.out.println("PlaySelectedAudioActivity: recordingsFile: " + recordingsFile);
        // System.out.println("PlaySelectedAudioActivity: recordingsFile.list: " + recordingsFile.list()[0] + ", " + recordingsFile.list()[1] + ", " + recordingsFile.list()[2]);


        shareAudio = shareBut;
        shareAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioShareHelper.sendAudioFile(audioFile,context);
            }
        });
        play = playBut;
        play.setOnClickListener(onPlayButtonClicked);

        System.out.println("PlaySELECTEDAUDIO: audiPath" + audioPath);
        //  File proba = new File(audioPath);



        pause = pauseBut;
        pause.setOnClickListener(onPauseButtonClicked);

        //configuramos el AudioTrack
        sampleRate = 16000;
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM);

        seekBar = seekBarBut;
        seekBar.setMax(100);
        progress=0;
        seekBar.setProgress(progress);


    }

    View.OnClickListener onPlayButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.VISIBLE);
            isPaused=false;
            isPlaying = true;
            if (isPaused==false) {
                if (playThread != null) {
                    try {
                        playThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    playThread = null;
                }
            }
            System.out.println("play:: thread is: "+playThread);
            if (playThread == null) {
                playThread = new PlayThread();
                playThread.start();
            }
        }
    };

    View.OnClickListener onPauseButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            isPaused=true;
            pause.setVisibility(View.INVISIBLE);
            play.setVisibility(View.VISIBLE);
          }
    };

    public void stopPlayer(){
            System.out.println("entro stop");
            pause.setVisibility(View.INVISIBLE);
            play.setVisibility(View.VISIBLE);
            isPaused=false;
            isPlaying = false;
            playThread=null;
            progress=0;
            seekBar.setProgress(progress);
        }



    class PlayThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[minBufferSize];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(audioFile), minBufferSize);

                audioTrack.play();

                int readSize = -1;
                while (isPlaying && (readSize = bis.read(buffer)) != -1) {
                    while (isPaused){

                    }
                    System.out.println("audioFileLength: "+ audioFile.length());
                    float delta = (float)readSize/audioFile.length();
                    delta = (delta*100);
                    System.out.println("readsize: "+ readSize);
                    System.out.println("la div es:"+ (readSize/audioFile.length()));
                    System.out.println("delta: "+delta);

                    System.out.println("PlaySelected..: seekUpdateHere: progress: "+progress);
                    seekBar.setProgress((int) (seekBar.getProgress()+delta));
                    audioTrack.write(buffer, 0, readSize);
                }
                System.out.println("stopppppppppppppppp");
                seekBar.setProgress(seekBar.getMax());
                audioTrack.stop();
                progress=0;
                seekBar.setProgress(progress);
                pause.setVisibility(View.INVISIBLE);
                play.setVisibility(View.VISIBLE);
                isPaused=false;
                isPlaying = false;


                bis.close();
            } catch (Throwable t) {

            }
        }

    };
}
