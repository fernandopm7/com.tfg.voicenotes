package com.voicenotes.view.library.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.voicenotes.R;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.library.ui.AudioPlayer;
import com.voicenotes.view.library.BibliotecaActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




public class CustomAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null; //nos sirve para instanciar el diseÃ±o xml..
    Context contexto;
    //List<String> audioIds;
    private Integer checkCount;
    List<Boolean> audioChecked ;
    BibliotecaActivity bib;
boolean checksVisibles=false;
private int checkedItemWhileLongClick =0;

    public void changeChechBoxesVisibility(){

    this.checksVisibles=(!checksVisibles);
        bib.setVisible();
        notifyDataSetChanged();
    }


    public CustomAdapter(Context contexto,BibliotecaActivity bib){
        this.contexto=contexto;
        //this.audioIds=audioIds;
        this.bib = bib;
        checkCount=1;
        System.out.println("entro en construct custom adapter");
        audioChecked = new ArrayList<Boolean>();

        //bib.listaToDelete.clear();

        inflater = (LayoutInflater) contexto.getSystemService(contexto.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return this.bib.elementosBiblioteca.size();
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

    private String parseToDuracion(Date date){
        SimpleDateFormat formato = new SimpleDateFormat("mm:ss");
        String duration =formato.format(date);
        return  duration;
    }
    private String parseToFecha(Date date){
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        final View vista = inflater.inflate(R.layout.elemento_lista, null);
        final TextView audioName = vista.findViewById(R.id.audioName);
        final TextView fecha = vista.findViewById(R.id.fecha);
        final TextView duracion = vista.findViewById(R.id.duracion);
        audioName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                checkedItemWhileLongClick = i;
                changeChechBoxesVisibility();
                bib.elementosBiblioteca.get(i).setChecked(true);
                return false;
            }
        });

        final CheckBox boxElminar = vista.findViewById(R.id.checkBoxEliminar);
        if (i== checkedItemWhileLongClick){
            boxElminar.setChecked(true);
            //bib.contadorSelecciones.setText(checkCount);



        }
        if (checksVisibles) {
            boxElminar.setVisibility(View.VISIBLE);



            boxElminar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    // LayoutInflater layoutInflater2 =  (LayoutInflater)  contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //  View vista2 = layoutInflater2.inflate(R.layout.activity_custom_list, null);
                    if (isChecked) {
                        checkCount++;
                        bib.elementosBiblioteca.get(i).setChecked(true);
                        bib.contadorSelecciones.setText(checkCount.toString());
                        System.out.println("custom is checked, selecciones: " + checkCount);
                        bib.setVisible();

                        bib.elementosBiblioteca.get(i).setChecked(true);
                        //  audioChecked.set(i,true);
                        //   vista2.findViewById(R.id.shareFromList).setVisibility(View.VISIBLE);
                    } else {
                        checkCount--;
                        bib.contadorSelecciones.setText(checkCount.toString());
                        bib.elementosBiblioteca.get(i).setChecked(false);
                        System.out.println("custom is not  checked, selecciones: "+checkCount);
                        //audioChecked.set(i,false);
                        if (checkCount == 0) {
                            checksVisibles=false;
                            checkCount=1;

                            bib.contadorSelecciones.setText("1");
                            System.out.println("custom is not  checked, check==0, selecciones: 1");
                            bib.setInvisible();
                            notifyDataSetChanged();
                            //     vista2.findViewById(R.id.deleteFromlist).setVisibility(View.INVISIBLE);
                            //     vista2.findViewById(R.id.shareFromList).setVisibility(View.INVISIBLE);
                        }
                    }
                }

            });
        }else{
            System.out.println("custom, else, selecciones :"+checkCount);
bib.setInvisible();
            boxElminar.setVisibility(View.INVISIBLE);
        }
        // System.out.println("Customadapter: mapkeys elem0: "+ AudioMap.getkeys()[0].toString() );
        if (bib.elementosBiblioteca.size()>0) {
            System.out.println("CustomAdapter: elems lista"+bib.elementosBiblioteca.size());

            final AudioInfo audioInfo = bib.mapa.get(bib.elementosBiblioteca.get(i).getName());
          //  CustomAdapterElement cae = bib.elementosBiblioteca.get(i);
            System.out.println("CustomAdapter: nameElemBib: "+bib.elementosBiblioteca.get(i).getName() + " . audioInfo: "+audioInfo );
            if (audioInfo == null) {
                //no mostramos ese elemento, porque no existe en el mapa..
                bib.elementosBiblioteca.remove(i);
                this.notifyDataSetChanged();
            }else {
                final String name = audioInfo.getName().replaceFirst(".wav","");
               fecha.setText(contexto.getString(R.string.fecha) +": "+parseToFecha(audioInfo.getFechaCreacion()));
               duracion.setText(contexto.getString(R.string.duracion) +": "+parseToDuracion(audioInfo.getDuration()));

                audioName.setText(name);
                audioName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        System.out.println("OnPlaySelectedActivityClick con audioName: " + audioInfo.getName());
                        //AudioPlayer dialog here.
                        final Dialog dialog = new Dialog(bib);
                        File audioFile = bib.voiceNotesService.getAudioFile(contexto,audioInfo.getName());
                        final AudioPlayer player = new AudioPlayer(bib, audioInfo.getName(),audioFile,dialog);

                        bib.runOnUiThread(new Runnable() {


                                          @Override
                                          public void run() { //este dialog si lo mostramos porque va sin asistente por voz..es decir si hay que msotrar ui ..

                                              dialog.show();
                                          }
                                      });
// Intent intent = new Intent(contexto, PlaySelectedAudioActivity.class);
                        //intent.putExtra("audioName", audioInfo.getName());
                        //contexto.startActivity(intent);

                    }
                });
            }
        }
        /*
        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Intent visorImagen = new Intent(contexto,VisorImagen.class);
               // visorImagen.putExtra("IMG",locImg[(int) view.getTag()]);
              //  contexto.startActivity(visorImagen);
            }
        });
        */

        return vista;
    }




    public String getTextFromPath(String path){
        File file = new File(path);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }
/*
    private View.OnClickListener onPlayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isPaused=false;
            isPlaying = true;
            if (playThread == null) {
                playThread = new PlayThread();
                playThread.start();
            }
        }
    };

    private View.OnClickListener onPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isPaused=true;
        }
    };

    private View.OnClickListener onStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            isPaused=false;
            isPlaying = false;
            playThread=null;
        }
    };
*/


}











































