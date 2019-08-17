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
import com.voicenotes.view.library.BibliotecaActivity;
import com.voicenotes.view.library.ui.AudioPlayer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




public class CustomAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    Context contexto;
    private Integer checkCount;
    List<Boolean> audioChecked ;
    BibliotecaActivity bib;
    boolean checksVisibles=false;
    private int checkedItemWhileLongClick =0;

    public CustomAdapter(Context contexto,BibliotecaActivity bib){
        this.contexto=contexto;
        //this.audioIds=audioIds;
        this.bib = bib;
        checkCount=1;
        audioChecked = new ArrayList<Boolean>();
        inflater = (LayoutInflater) contexto.getSystemService(contexto.LAYOUT_INFLATER_SERVICE);
    }

    public void changeChechBoxesVisibility(){
        this.checksVisibles=(!checksVisibles);
        bib.setVisible();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.bib.elementosBiblioteca.size();
    }

    @Override
    public Object getItem(int i) {
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
        }
        if (checksVisibles) {
            boxElminar.setVisibility(View.VISIBLE);
            boxElminar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        checkCount++;
                        bib.elementosBiblioteca.get(i).setChecked(true);
                        bib.contadorSelecciones.setText(checkCount.toString());
                        bib.setVisible();
                        bib.elementosBiblioteca.get(i).setChecked(true);
                    } else {
                        checkCount--;
                        bib.contadorSelecciones.setText(checkCount.toString());
                        bib.elementosBiblioteca.get(i).setChecked(false);
                        if (checkCount == 0) {
                            checksVisibles=false;
                            checkCount=1;
                            bib.contadorSelecciones.setText("1");
                            bib.setInvisible();
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }else{
            bib.setInvisible();
            boxElminar.setVisibility(View.INVISIBLE);
        }
        if (bib.elementosBiblioteca.size()>0) {
            final AudioInfo audioInfo = bib.mapa.get(bib.elementosBiblioteca.get(i).getName());
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
                        //AudioPlayer dialog here.
                        final Dialog dialog = new Dialog(bib);
                        File audioFile = bib.voiceNotesService.getAudioFile(contexto,audioInfo.getName());
                        final AudioPlayer player = new AudioPlayer(bib, audioInfo.getName(),audioFile,dialog);
                        bib.runOnUiThread(new Runnable() {
                            @Override
                            public void run() { //este dialog si lo mostramos porque va sin asistente por voz..es decir, si hay que msotrar ui ..
                                dialog.show();
                            }
                        });
                    }
                });
            }
        }
        return vista;
    }
}











































