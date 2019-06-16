package com.voicenotes.dataaccess;

import android.content.Context;

import com.voicenotes.dataaccess.daos.VoiceNoteDao;
import com.voicenotes.dataaccess.daos.impl.VoiceNoteDaoImpl;
import com.voicenotes.utils.centalmap.AudioInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ObjectsManager {

    private static  VoiceNoteDao voiceNoteDao = new VoiceNoteDaoImpl();
    private ObjectsManager() {}

    public static void removeObject (Context context, String dirName, String key){
        voiceNoteDao.removeObject(context, dirName, key);
    }
    public static void  deleteFile (Context context, String fileName){
        voiceNoteDao.deleteFile(context,fileName);
    }
    public static void  removeObjectDirectory(Context context, String dirName){
        voiceNoteDao.removeObjectDirectory(context,dirName);
    }
    public static File writeFile (Context context,String dirName, String key, File fileOrg){
        return voiceNoteDao.writeFile(context,dirName,key,fileOrg);
    }
    public static File readFile(Context context, String dirName, String key){
        return voiceNoteDao.readFile(context,dirName,key);
    }
    public static File writeObject(Context context,String dirName, String key, Object object) throws IOException { //devuelve el file donde guarda el objeto..
       return voiceNoteDao.writeObject(context,dirName,key,object);
    }

    public static Object readObject(Context context, String dirName, String key) throws IOException,
            ClassNotFoundException {
       return voiceNoteDao.readObject(context,dirName,key);
    }



    public  static  void updateAudioMap (Context context, AudioInfo audioInfo){
        try {
            Map<String, AudioInfo> mapa = (HashMap<String, AudioInfo>) readObject(context,"mapa","audioMap");
            if (mapa == null ) {
                mapa = new HashMap<String, AudioInfo>();
            }
                mapa.put(audioInfo.getName(), audioInfo);

            writeObject(context,"mapa","audioMap",mapa);
        } catch (IOException e) {
            //@todo gestionar caso de mapa vacio (caso primer inicio en la app)
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
