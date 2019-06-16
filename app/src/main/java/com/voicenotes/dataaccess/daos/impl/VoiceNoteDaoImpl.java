package com.voicenotes.dataaccess.daos.impl;

import android.content.Context;

import com.voicenotes.dataaccess.daos.VoiceNoteDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;

public class VoiceNoteDaoImpl implements VoiceNoteDao {

    public void removeObject (Context context, String dirName, String key){
        File file = new File(context.getFilesDir(), dirName);
        File gpxfile = new File(file, key);
        gpxfile.delete();
    }
    public void  deleteFile (Context context, String fileName){
        context.deleteFile(fileName);
    }
    public void  removeObjectDirectory(Context context, String dirName){
        File file = new File(context.getFilesDir(), dirName);
        file.delete();
    }

    public File writeFile (Context context,String dirName, String key, File fileOrg){
        System.out.println("ORW:  wirte: context: "+context);
        System.out.println("ORW:  wirte: dirName: "+dirName);
        File file = new File(context.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdir();
        }
        File fileDest = new File(file, key);

        try {
            FileChannel src = new FileInputStream(fileOrg).getChannel();
            FileChannel dest = new FileOutputStream(fileDest).getChannel();
            dest.transferFrom(src, 0, src.size());
            return fileDest;
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            return null;
        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }
    }

    public File readFile(Context context, String dirName, String key){
        File file = new File(context.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("ObjectsManager: readFile: file not exists");
        }
        File gpxfile = new File(file, key);

        return gpxfile;

    }


    public File writeObject(Context context,String dirName, String key, Object object) throws IOException { //devuelve el file donde guarda el objeto..
        System.out.println("ObjectsReadWriter: writeObject: "+key);
        //FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        //ObjectOutputStream oos = new ObjectOutputStream(fos);
        //oos.writeObject(object);
        //oos.close();
        //fos.close();

        System.out.println("ORW:  wirte: context: "+context);
        System.out.println("ORW:  wirte: dirName: "+dirName);
        File file = new File(context.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdir();
        }
        System.out.println("ObjectsManager: write: fileContent:"+file.list());

        try {
            File gpxfile = new File(file, key);
            System.out.println("ObjectsManager: write: gpxFileContent:"+gpxfile.list());
            FileWriter writer = new FileWriter(gpxfile);

            writer.close();
            FileOutputStream f = new FileOutputStream(gpxfile);
            ObjectOutputStream s = new ObjectOutputStream(f);

            s.writeObject(object);
            s.close();
            return gpxfile;
        } catch (Exception e) {
            System.out.println("ObjectsReadWriter: error here");
            e.printStackTrace();
            return null;
        }


    }

    public  Object readObject(Context context, String dirName, String key) throws IOException,
            ClassNotFoundException {
        System.out.println("ObjectsReadWriter: readObject: "+key);
        //FileInputStream fis = context.openFileInput(key);
        //ObjectInputStream ois = new ObjectInputStream(fis);
        //Object object = ois.readObject();
        File file = new File(context.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdir();
            System.out.println("ObjectsManager: read: file not exists");
        }
//        System.out.println("ObjectsManager: read: fileContent:"+file.list()[0]);
        Object fileObj2 = null;
        try {
            File gpxfile = new File(file, key);

           // System.out.println("ObjectsManager: read: fileContentAgain:"+file.list());
         //   System.out.println("ObjectsManager: read: gpxFileContent:"+gpxfile.list());
            //System.out.println("ObjectsManager: read: gpxFile"+gpxfile);
            //System.out.println("ObjectsReadWriter: Read: file: "+ file);
            //System.out.println("ObjectsReadWriter: Read: filePath: "+ file.getPath());
            if (gpxfile == null){return null;}
            FileInputStream  f = new FileInputStream(gpxfile);
            ObjectInputStream s = new ObjectInputStream(f);

            fileObj2 =  s.readObject();
            s.close();

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }

        return fileObj2;
    }
}
