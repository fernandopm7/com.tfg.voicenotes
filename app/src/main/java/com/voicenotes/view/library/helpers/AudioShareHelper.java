package com.voicenotes.view.library.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class AudioShareHelper {

    private static  FFmpeg ffmpeg;

    public static void init(Context context) {
        ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    System.out.println("*****FAAAAAAAIIIIIIILLLLLLLLLLLLLLLLLLLLLLLL*****");
                }

                @Override
                public void onSuccess() {
                    System.out.println("*****SUCCEEEEEEEEEEEEEEEEEEEEEESSSSSSSSSSSS*****");
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            System.out.println("LLLOOOOOOOOOOOOOOOOOOOOOOOOOOOOLLL:  ");
            e.printStackTrace();
            // Handle if FFmpeg is not supported by device
        }
    }



        public static void sendAudioFile (final File f, final Context context){
        System.out.println("AudioShareHelper: file: "+f);
        //Uri uri = Uri.parse("file://"+f.getAbsolutePath());

       /*
        String baseName = f.getName().replaceAll(".wav","");

        final String destName = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC) +"/" +baseName + ".3gp" ;
      //  destName.replaceAll(".wav","");
        System.out.println("AudioShareHelper: orgFilePath: "+f.getPath());
        System.out.println("AudioShareHelper: destFilePath: "+destName);

        final String[] cmd = new String[]{"-i",f.getPath(),"-ar","16000",destName};
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    System.out.println("******Start*****");
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                    System.out.println("*****FAAAAAAAIIIIIIILLLLLLLLLLLLLLLLLLLLLLLL***** "+message);
                }

                @Override
                public void onSuccess(String message) {
                    System.out.println("succes:  " + message);
                }

                @Override
                public void onFinish() {
                    File filex = new File (destName);
                    System.out.println("autorithyPackName: "+ context.getApplicationContext().getPackageName());

                    Uri uri =  FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName()+".audio" , filex);


                    System.out.println("AudioShareHelper: my uri: "+ uri);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.setType("audio/*");
                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(share, "Share audio File"));
                }


            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
        */
            System.out.println("autorithyPackName: "+ context.getApplicationContext().getPackageName());

            Uri uri =  FileProvider.getUriForFile(context, "com.voicenotes.utils.fileprovider" , f);


            System.out.println("AudioShareHelper: my uri: "+ uri);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setType("audio/*");
          //  share.setPackage("org.telegram.messenger");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "Share audio File"));


    }
}
