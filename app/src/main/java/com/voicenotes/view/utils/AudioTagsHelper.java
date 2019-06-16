package com.voicenotes.view.utils;

import android.content.Context;

import com.voicenotes.R;
import com.voicenotes.services.VoiceNotesService;
import com.voicenotes.utils.centalmap.AudioInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AudioTagsHelper {

    private static final List<String> defaultTags = Arrays.asList(new String[]{"Home", "Reminders"});
    private static VoiceNotesService vns = new VoiceNotesService();

    public static List<String> getPersonalTags(Context ctx){
        Map<String, AudioInfo> mapa = vns.getVoiceNotesMap(ctx);
        List<String> tags = new ArrayList<String>();
        for (String key: mapa.keySet()){
            if (!tags.contains(mapa.get(key).getTag()))
            tags.add(mapa.get(key).getTag());
        }
        return tags;
    }

    public static List<String> getSpinnerTags(Context ctx){
        List<String> spinnerTags = new ArrayList<String>();
        spinnerTags.add(ctx.getString(R.string.select_a_tag));
        spinnerTags.addAll(getPersonalTags(ctx));
        spinnerTags.add(ctx.getString(R.string.create_new_tag));
        return spinnerTags;
    }

    public static List<String> getDefaultTags(){
        return defaultTags;
    }

    public static List<String> getAllTags(Context ctx){
        List<String> allTags = new ArrayList<String>();
         allTags.addAll(getDefaultTags());
         allTags.addAll(getPersonalTags(ctx));
         return allTags;
    }


}
