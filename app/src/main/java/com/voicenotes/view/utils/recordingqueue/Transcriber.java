package com.voicenotes.view.utils.recordingqueue;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.voicenotes.dataaccess.ObjectsManager;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

public class Transcriber {

   // private static Decoder decoder = null;
   // private static Config c;
    private static FileOutputStream audioOutputStream = null;
    private static int sampleRate;
    private static int minBufferSize;
    //private static AudioRecord recorder;
    private static final String audioStorageFilePath  =Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_MUSIC) + "/_lastRecording.3gp"; //todo..solucion provicional esto..
    private static Context context ;

    static {
        System.loadLibrary("pocketsphinx_jni");
    }

    public  static void initialize (Context contexto){
    context = contexto;

        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    public static String nuevoTranscribe (String name, String idioma){



            DefaultSettingsXmlParser.setCurrentModel(idioma);

    Config c;
    c = Decoder.defaultConfig();

    c.setString("-hmm", DefaultSettingsXmlParser.getHmmPath() ); // Environment.getExternalStorageDirectory()+"/Android/data/edu.cmu.pocketsphinx/hmm/en-us"
    c.setString("-dict",DefaultSettingsXmlParser.getDictPath() ); // Environment.getExternalStorageDirectory()+"/Android/data/edu.cmu.pocketsphinx/lm/cmudict-en-us.dict"
    c.setString("-lm", DefaultSettingsXmlParser.getLmPath() ); // Environment.getExternalStorageDirectory()+"/Android/data/edu.cmu.pocketsphinx/lm/en-us.lm.dmp"
    c.setString("-rawlogdir", DefaultSettingsXmlParser.getAssetDir().getPath()); // Environment.getExternalStorageDirectory()+"/Android/data/edu.cmu.pocketsphinx"

    Decoder decoder = new Decoder(c);decoder.getConfig().getFloat("-samprate");

    sampleRate = (int) c.getFloat("-samprate");
    minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord recorder =
            new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

        FileInputStream stream = null;
        try {

            // final File file =  new File(assetDir, "aligner.wav");
            // final File file =  new File(audioPath);
            //Uri uri = Uri.fromFile(file);
            //File auxFile = new File(uri.getPath());
            //  stream = new FileInputStream(auxFile);



            // final File file =  new File(assetDir, "aligner.wav");
            File file = (File) ObjectsManager.readFile(context,"recordings",name);
            stream = new FileInputStream(file);
            //  System.out.println("TRANSCRIBER:  AUXFILE  "+auxFile + "    .Y AUXFILEPATH:"+auxFile.getPath());
            System.out.println("TRANSCRIBER: stream:  " + stream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder.startUtt();
        //System.out.println("Samplerate from decoder: "+ decoder.getConfig().getFloat("-samprate"));
        byte[] b = new byte[4096];
        try {
            int nbytes;
            while ((nbytes = stream.read(b)) >= 0) {

                ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                short[] s = new short[nbytes/2];
                bb.asShortBuffer().get(s);
                decoder.processRaw(s, nbytes/2, false, false);
                byte bufferData[] = short2byte(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //  Log.i(TAG, "Error when reading .wav: " + e.getMessage());
        }
        decoder.endUtt();
        System.out.println("*********HYPSTR AQUI:  "+  decoder.hyp().getHypstr());
        //  for (Segment seg : decoder.seg()) {
        //      System.out.println("****GETWORD AQUI:   "+seg.getWord());

        return decoder.hyp().getHypstr();
}


    private static byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }
   /*  public static String transcribe (String name,boolean spanish){

        FileInputStream stream = null;
        try {

            // final File file =  new File(assetDir, "aligner.wav");
           // final File file =  new File(audioPath);
            //Uri uri = Uri.fromFile(file);
            //File auxFile = new File(uri.getPath());
          //  stream = new FileInputStream(auxFile);



           // final File file =  new File(assetDir, "aligner.wav");
            File file = (File) ObjectsManager.readFile(context,"recordings",name);
            stream = new FileInputStream(file);
          //  System.out.println("TRANSCRIBER:  AUXFILE  "+auxFile + "    .Y AUXFILEPATH:"+auxFile.getPath());
            System.out.println("TRANSCRIBER: stream:  " + stream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        decoder.startUtt();
        //System.out.println("Samplerate from decoder: "+ decoder.getConfig().getFloat("-samprate"));
        byte[] b = new byte[4096];
        try {
            int nbytes;
            while ((nbytes = stream.read(b)) >= 0) {

                ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                short[] s = new short[nbytes/2];
                bb.asShortBuffer().get(s);
                decoder.processRaw(s, nbytes/2, false, false);
                byte bufferData[] = short2byte(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
          //  Log.i(TAG, "Error when reading .wav: " + e.getMessage());
        }
        decoder.endUtt();
        System.out.println("*********HYPSTR AQUI:  "+  decoder.hyp().getHypstr());
      //  for (Segment seg : decoder.seg()) {
      //      System.out.println("****GETWORD AQUI:   "+seg.getWord());


        return decoder.hyp().getHypstr();

    }*/



}
