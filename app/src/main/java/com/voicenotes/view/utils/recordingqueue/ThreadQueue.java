package com.voicenotes.view.utils.recordingqueue;

import android.content.Context;
import android.util.Pair;

import com.voicenotes.services.VoiceNotesService;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;
import com.voicenotes.view.utils.indexes.AudioIndexer;

import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThreadQueue {

    private static List<Pair<String,String>> queue = new ArrayList<Pair<String,String>>();
    private static boolean endLoop = false;
    private static VoiceNotesService vns = new VoiceNotesService();
    private static Context context;
    public static void initialize (Context ctx){
        context= ctx;
        Map<String, AudioInfo> mapa = vns.getVoiceNotesMap(ctx);
        for (String key: mapa.keySet()){
            if (mapa.get(key).isInQueue()){
                queue.add(new Pair<String,String>(key,mapa.get(key).getIdioma()));
            }
        }
        startQueueLoop();
    }

    public static void addElement (String elem, String currenteTranscriptionLanguage){
        Pair<String,String> elementoAEncolar = new Pair<String,String>(elem, currenteTranscriptionLanguage);
        queue.add(elementoAEncolar);
    }

    public static void stopQueueLoop(){
        endLoop=true;
    }

    public static void startQueueLoop(){
        endLoop=false;

        new Thread(new Runnable() {
            @Override
            public void run() {

                processLoop();
            }
        }).start();
    }

    private static void processLoop(){
        while (!endLoop){
            if (!queue.isEmpty()){
                Pair<String,String> pairHead = queue.get(0);
                String head = pairHead.first;
                String idioma = pairHead.second;
                String transcription = Transcriber.nuevoTranscribe(head,idioma);
                //String transcription = Transcriber.transcribe(head);

         //       if (!DefaultSettingsXmlParser.getCurrentModel().contentEquals(idioma)){ //cambiamos idioma para esa transcripción en concreto
           //         DefaultSettingsXmlParser.setCurrentModel(idioma);
             //       Transcriber.initialize(context);
               // }
                try {
                    //guardamos la transcripcion en un File..
                    File transcriptionFile = vns.saveTranscription(context,head,transcription);
                    //indexamos..
                    Document doc = AudioIndexer.getDocument(transcriptionFile);
                    AudioIndexer.indexDoc(doc);
                    //actualizamos el mapa, informando el path de la transcripcion y cambiando el flag inqueue a false.
                    vns.updateVoiceNoteTextPath(context,head,transcriptionFile.getPath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                queue.remove(0);
                /*
                Intent intent2 =
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent2.setData(Uri.fromFile(filex));
                sendBroadcast(intent2);
                */
            }
        }
    }

}
