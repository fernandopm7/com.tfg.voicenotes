package com.voicenotes.dataaccess.daos;

import android.content.Context;

import java.io.File;
import java.io.IOException;

public interface VoiceNoteDao {

    void removeObject (Context context, String dirName, String key);

    void  deleteFile (Context context, String fileName);

    void  removeObjectDirectory(Context context, String dirName);

    File writeFile (Context context,String dirName, String key, File fileOrg);

    File readFile(Context context, String dirName, String key);

    File writeObject(Context context,String dirName, String key, Object object) throws IOException; //devuelve el file donde guarda el objeto..

    Object readObject(Context context, String dirName, String key) throws IOException, ClassNotFoundException;



}
