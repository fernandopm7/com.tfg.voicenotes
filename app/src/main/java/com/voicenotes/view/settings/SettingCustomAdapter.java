package com.voicenotes.view.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.VideoView;

import com.voicenotes.R;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.library.BibliotecaActivity;
import com.voicenotes.view.library.ui.AudioPlayer;
import com.voicenotes.view.settings.ui.CreditoUI;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingCustomAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null; //nos sirve para instanciar el diseÃ±o xml..
    SettingsActivity contexto;
    //List<String> audioIds;
    private Integer checkCount;
    int position =0;

    public SettingCustomAdapter(SettingsActivity contexto){
        this.contexto=contexto;
        inflater = (LayoutInflater) contexto.getSystemService(contexto.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int i) {
        // return items.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

      @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
      @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        final View vista = inflater.inflate(R.layout.elemento_lista_settings, null);
        final TextView settingName = vista.findViewById(R.id.settingName);
        final TextView settingDescription = vista.findViewById(R.id.settingDescription);
        ImageView settingIcon = vista.findViewById(R.id.settingImage);

        switch (i){
            case 0:
                settingName.setText(contexto.getText(R.string.language));
                String idioma = contexto.getString(R.string.ingles).contentEquals(DefaultSettingsXmlParser.getCurrentLenguage()) ? contexto.getString(R.string.ingles_local) : contexto.getString(R.string.espanhol_local) ;

                settingDescription.setText(idioma);
                settingIcon.setImageDrawable(contexto.getDrawable(R.drawable.iconidioma));

                settingName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(contexto);
                        builderSingle.setIcon(R.drawable.iconidioma);
                        builderSingle.setTitle(R.string.language);

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(contexto, android.R.layout.select_dialog_singlechoice);
                        arrayAdapter.add(contexto.getString(R.string.espanhol_local));
                        arrayAdapter.add(contexto.getString(R.string.ingles_local));

                        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String idiomaSelected = which == 0 ? "es" : "en";

                                System.out.println("SettingsActivity: onItemSelected: Spinner lenguage");
                                DefaultSettingsXmlParser.setCurrentLenguage(idiomaSelected);
                                Locale locale;
                                //todo internacionalización
                                if (idiomaSelected.equals(contexto.getString(R.string.ingles))) {
                                    locale = new Locale("en");
                                } else {
                                    locale = new Locale("es", "ES");
                                }
                                Configuration config = contexto.getResources().getConfiguration();
                                config.locale = locale;
                                contexto.getResources().updateConfiguration(config, contexto.getResources().getDisplayMetrics());


                                AlertDialog.Builder builderInner = new AlertDialog.Builder(contexto);
                                builderInner.setMessage(arrayAdapter.getItem(which));
                                //builderInner.setTitle("Your Selected Item is");
                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        String idioma = contexto.getString(R.string.ingles).contentEquals(DefaultSettingsXmlParser.getCurrentLenguage()) ? contexto.getString(R.string.ingles_local) : contexto.getString(R.string.espanhol_local) ;
                                        settingDescription.setText(idioma);

                                       contexto.recreate();
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.show();
                            }
                        });
                        builderSingle.show();


                    }
                });
                break;
            case 1:
                settingName.setText(contexto.getText(R.string.model));
                String modelo = DefaultSettingsXmlParser.getCurrentModel();
                settingDescription.setText(modelo);
                settingIcon.setImageDrawable(contexto.getDrawable(R.drawable.iconmodelo));

                settingName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(contexto);
                        builderSingle.setIcon(R.drawable.iconmodelo);
                        builderSingle.setTitle(R.string.model);

                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(contexto, android.R.layout.select_dialog_singlechoice);
                        arrayAdapter.add("es-es");
                        arrayAdapter.add("en-us");

                        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String modeloSelected = arrayAdapter.getItem(which);

                                System.out.println("SettingsActivity: onItemSelected: Spinner models");
                                DefaultSettingsXmlParser.setCurrentModel(modeloSelected);


                                AlertDialog.Builder builderInner = new AlertDialog.Builder(contexto);
                                builderInner.setMessage(modeloSelected);
                                //builderInner.setTitle("Your Selected Item is");
                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String modelo = DefaultSettingsXmlParser.getCurrentModel();
                                        settingDescription.setText(modelo);
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.show();
                            }
                        });
                        builderSingle.show();


                    }
                });
                break;
            case 2:
                settingName.setText(contexto.getText(R.string.info));
                settingDescription.setText(contexto.getText(R.string.usage));
                settingIcon.setImageDrawable(contexto.getDrawable(R.drawable.iconinfo));

                settingName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(contexto);
                        popupBuilder.setTitle(contexto.getString(R.string.info));
                        popupBuilder.setIcon(R.drawable.iconinfo);
                        TextView myMsg = new TextView(contexto);
                        myMsg.setVerticalScrollBarEnabled(true);
                        myMsg.setMovementMethod(new ScrollingMovementMethod());
                        //@todo el buen hardcode
                        myMsg.setText("\tVoiceNotes es un proyecto desarrollado como\n" +
                                      "\ttrabajo de fin de grado de la Universidad\n" +
                                      "\tde la Coruña por el alumno Fernando Pena\n" +
                                      "\tMartínez.\n\n" +
                                      "\tVoiceNotes es una aplicación destinada a la\n" +
                                      "\tcreación y gestión de notas de voz. La versión\n" +
                                      "\tactual (v1.0.0) incluye las siguientes\n" +
                                      "\tfuncionalidades y tecnologías:\n" +
                                      "\t-Grabar, reproducir y compartir archivos WAV.\n" +
                                      "\t-Sistema de etiquetas para organizar y filtrar\n" +
                                      "\tnotas de voz según conveniencia del usuario.\n" +
                                      "\t-Sistema de transcripción de audio a texto\n" +
                                      "\t(Tecnología: Pocketsphinx).\n" +
                                      "\t-Sistema de reconocimiento de voz continuo\n" +
                                      "\t(Tecnología: Pocketsphinx y Android TTS).\n" +
                                      "\t-Indexación y búsqueda mediante índices\n" +
                                      "\tinvertidos y fuzzyQueries (Tecnología: Lucene).\n" +
                                      "\t-Funcionalidades disponibles tanto en español\n" +
                                      "\tcomo en inglés.\n\n" +

                                      "\t\t\t\tPara cualquier pregunta o solicitud:\n" +
                                        "\t\t\t\t\t\t\t\t\t\tf.pena1@udc.es" +

                                "\n"
                                );
                        //myMsg.setGravity(Gravity.HORIZONTAL);
                        popupBuilder.setView(myMsg);
                        popupBuilder.show();
                    }
                });

                break;
            case 3:
                settingName.setText(contexto.getText(R.string.creditos));
                settingDescription.setText(contexto.getText(R.string.copyright));
                settingIcon.setImageDrawable(contexto.getDrawable(R.drawable.iconcreditos));

                settingName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builderSingle = new AlertDialog.Builder(contexto);
                        builderSingle.setIcon(R.drawable.iconcreditos);
                        builderSingle.setTitle(R.string.creditos);

                       // final ArrayAdapter<View> arrayCreditoAdapter = new ArrayAdapter<View>(contexto, android.R.layout.select_dialog_singlechoice);
                        List<CreditoUI> creditos = DefaultSettingsXmlParser.getCreditos();

                        CreditosCustomAdapter adapter = new CreditosCustomAdapter(contexto, creditos);

                        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing for now
                            }
                    });
                        builderSingle.show();
                    }
                });
                break;

            case 4:
                settingName.setText(contexto.getText(R.string.demo));
                settingDescription.setText(contexto.getText(R.string.demoDescription));
                settingIcon.setImageDrawable(contexto.getDrawable(android.R.drawable.ic_media_play));
                settingName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(contexto);//,android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.demo_layout);//add your own xml with defied with and height of videoview
                        dialog.show();
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        dialog.getWindow().setAttributes(lp);
                        Uri uri= DefaultSettingsXmlParser.getDemoUri(contexto);

                        contexto.getWindow().setFormat(PixelFormat.TRANSLUCENT);

                        VideoView mVideoView =(VideoView) dialog.findViewById(R.id.video);

                        mVideoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                               if( mVideoView.isPlaying()) {
                                   mVideoView.pause();
                                   position = mVideoView.getCurrentPosition();
                               }else{
                                   if (mVideoView != null) {
                                       mVideoView.start();
                                       mVideoView.seekTo(position);
                                       position=0;
                                   }
                               }
                            }
                        });
                        mVideoView.setVideoURI(uri);
                        mVideoView.start();
                    }
                });
                break;
        }

        return vista;
    }

    public static int getResId(String resName, Class<?> c) {

              try {
                  Field idField = c.getDeclaredField(resName);
                  return idField.getInt(idField);
              } catch (Exception e) {
                  e.printStackTrace();
                  return -1;
              }
          }
}
