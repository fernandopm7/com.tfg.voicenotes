package com.voicenotes.view.settings.utils;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.voicenotes.view.initialization.InitProgressBarActivity;
import com.voicenotes.view.settings.ui.CreditoUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import edu.cmu.pocketsphinx.Assets;

public class DefaultSettingsXmlParser {

    private static Properties properties ;
    private static Properties propertiesCreditos ;
    static Assets assets = null;
    static File assetDir = null;
    private static File demoFile;

    public static void initialize(InitProgressBarActivity sa){
        properties = new Properties();
        propertiesCreditos = new Properties();

        WeakReference<InitProgressBarActivity> activityReference;
        activityReference = new WeakReference<>(sa);

        try {
            assets = new Assets(activityReference.get());
            assetDir = assets.syncAssets();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String path = new File(assetDir, "defaultSettings.properties").getPath();
        String pathProperties = new File(assetDir, "creditos.properties").getPath();
        demoFile = new File(assetDir, "demo.mp4");



        try {
            properties.load(new FileInputStream(path));
            propertiesCreditos.load(new FileInputStream(pathProperties));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentLenguage(){
        return properties.getProperty("currentLenguage");
    }
    public static String getCurrentModel(){
        return properties.getProperty("currentModel");
    }
    public static String getDefaultLenguage(){
        return properties.getProperty("defaultLenguage");
    }
    public static String getDefaultModel(){
        return properties.getProperty("defaultModel");
    }
    public static void setCurrentLenguage(String lenguage){
        properties.setProperty("currentLenguage",lenguage);
    }
    public static void setCurrentModel(String model){
        properties.setProperty("currentModel",model);
    }
    public static String getHmmPath(){
        return (new File(assetDir, getCurrentModel()+"/"+getCurrentModel())).getPath();
    }
    public static String getDictPath (){
        return (new File(assetDir, getCurrentModel()+"/"+getCurrentModel()+".dict")).getPath();
    }
    public static String getLmPath (){
        return (new File(assetDir, getCurrentModel()+"/"+getCurrentModel()+".lm")).getPath();
    }

    public static File getAssetDir(){
        return assetDir;
    }

    public static List<CreditoUI> getCreditos(){
        List<CreditoUI> creditos = new ArrayList<CreditoUI>();

        for (Object line:  propertiesCreditos.values()){
           final String lineaStr = (String) line;
           String[] splited = lineaStr.split(",");
           final String autor = splited[0].trim();
           final String url = splited[1].trim();
           final String iconName = splited[2].trim();

           creditos.add(new CreditoUI(autor,url,iconName));
        }
        return creditos;
    }

    public static  Uri getDemoUri(Context ctx){
        Uri uri =  FileProvider.getUriForFile(ctx, "com.voicenotes.utils.fileprovider" , demoFile);
        return uri;
    }

}
