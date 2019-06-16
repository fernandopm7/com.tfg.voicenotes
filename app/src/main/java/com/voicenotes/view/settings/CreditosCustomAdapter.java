package com.voicenotes.view.settings;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.voicenotes.R;
import com.voicenotes.view.settings.ui.CreditoUI;

import java.util.List;

import static com.voicenotes.view.settings.SettingCustomAdapter.getResId;

public class CreditosCustomAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null; //nos sirve para instanciar el diseÃ±o xml..
    SettingsActivity contexto;
    List<CreditoUI> creditos;
    public CreditosCustomAdapter(SettingsActivity contexto, List<CreditoUI> creditos){
        this.contexto=contexto;
        this.creditos=creditos;
        inflater = (LayoutInflater) contexto.getSystemService(contexto.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return creditos.size();
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

            CreditoUI credito = creditos.get(i);

            final View vista = inflater.inflate(R.layout.elemento_creditos, null);
            final TextView autor = vista.findViewById(R.id.autor);
            autor.setText(credito.getAutor());
            final TextView url = vista.findViewById(R.id.url);
            url.setText(credito.getUrl());
            ImageView creditoIcon = vista.findViewById(R.id.creditoImage);
            final int resID = getResId(credito.getIconName(), R.drawable.class);
            creditoIcon.setImageDrawable(contexto.getDrawable(resID));

            return vista;

    }
}
