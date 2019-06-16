package com.voicenotes.services;

import android.content.Context;

import com.voicenotes.dataaccess.ObjectsManager;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.utils.indexes.AudioIndexer;
import com.voicenotes.view.utils.recordingqueue.ThreadQueue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceNotesService {


    public Map<String, AudioInfo> getVoiceNotesMap (Context context){
        Map<String, AudioInfo> mapa = null;
        try {
            mapa = (HashMap<String, AudioInfo>) ObjectsManager.readObject(context,"mapa","audioMap");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (mapa == null){
            return new HashMap<String, AudioInfo>();
        }
        return mapa;
    }

    public File saveTranscription (Context context, String voiceNoteName, String transcription){
        try {
            return ObjectsManager.writeObject(context, "texts", voiceNoteName, transcription);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public void processAudio (Context context,File recordFile, String nombre, String tag, Date fechaCreacion, Date duracion, String currenteTranscriptionLanguage){
        AudioInfo audioInfo = new AudioInfo(nombre,recordFile.getPath(),null,true, tag,duracion,fechaCreacion,currenteTranscriptionLanguage);
        ObjectsManager.writeFile(context, "recordings", nombre,recordFile); //guardamos el File por defecto de las grabaciones en uno con nombre indicado por el usuario, tener en cuenta que cada vez que se crea una nueva grabación esta se guarda sobre ese file por defecto..
        //añadimos ese AudioFile al mapa..
        ObjectsManager.updateAudioMap (context, audioInfo);
        //procesamos la transcripcion y su indexacion
        ThreadQueue.addElement(nombre,currenteTranscriptionLanguage);
    }

    public void saveMap (Context context, Map<String, AudioInfo> mapa){
        try {
            ObjectsManager.writeObject(context,"mapa","audioMap",mapa);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void updateVoiceNote (Context context, AudioInfo audioInfo){ //para un futuro si se quiere cambiar información del tag o del nombre

        Map<String, AudioInfo> mapa = getVoiceNotesMap(context);
        mapa.put(audioInfo.getName(),audioInfo);
        saveMap(context, mapa);

    }

    public void updateVoiceNoteTextPath (Context context, String name, String textPath){
        Map<String, AudioInfo> mapa = getVoiceNotesMap(context);
        mapa.get(name).setTextPath(textPath);
        mapa.get(name).setInQueue(false);
        saveMap(context, mapa);
    }

    public File getAudioFile (Context context, String audioName){
       return  (File) ObjectsManager.readFile(context,"recordings",audioName);
    }

    public void deleteVoiceNote(Context context, String name){
        ObjectsManager.deleteFile(context,name); //esto puede que si..nueva imple de eliminar..probar..
        //eliminamos del mapa
        Map<String, AudioInfo> mapa = getVoiceNotesMap(context);
        mapa.remove(name);
        try {
            ObjectsManager.writeObject(context,"mapa","audioMap",mapa);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //eliminamos del indice //todo comprobar si va bien
        AudioIndexer.deleteFromIndex(name);
    }

    public void deleteVoiceNotes(Context context, List<String> names){ //para un futuro

    }
}
