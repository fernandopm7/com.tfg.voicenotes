package com.voicenotes.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.voicenotes.dataaccess.schemas.VoiceNotesDBSchema;
import com.voicenotes.utils.centalmap.AudioInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioInfoDB {
    private static AudioInfoDB audioInfoDB;
    private List<AudioInfo> audioInfos;
    private SQLiteDatabase dataBase;
    private Context context;

    private AudioInfoDB(Context context) {
        this.context = context.getApplicationContext();
        dataBase = new VoiceNotesSQLHelper(this.context).getWritableDatabase();
        audioInfos = new ArrayList<>();
    }

    private static ContentValues getContentValues(AudioInfo audioInfo) {
        ContentValues cvs = new ContentValues();
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.ID, audioInfo.getName());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.AUDIO_NAME, audioInfo.getName());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.DURACION, audioInfo.getDuration().getTime());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.FECHA_CREACION, audioInfo.getFechaCreacion().getTime());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.IDIOMA, audioInfo.getIdioma());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.IN_QUEUE, audioInfo.isInQueue() ? 1 : 0);
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.TAG, audioInfo.getTag());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.TRANSCRIPTION_PATH, audioInfo.getTextPath());
        cvs.put(VoiceNotesDBSchema.AudioInfoTable.Cols.WAV_PATH, audioInfo.getWavPath());
        return cvs;
    }

    public void addAudioInfo(AudioInfo audioInfo) {
        ContentValues cvs = getContentValues(audioInfo);
        dataBase.insert(VoiceNotesDBSchema.AudioInfoTable.name, null, cvs);
    }

    public void updateAudioInfo(AudioInfo audioInfo) {
        String id = audioInfo.getName();
        ContentValues cvs = getContentValues(audioInfo);
        dataBase.update(VoiceNotesDBSchema.AudioInfoTable.name, cvs, VoiceNotesDBSchema.AudioInfoTable.Cols.ID + " = ?", new String[]{id});
    }

    public AudioInfo getAudioInfo(String id) {

        Cursor cursor = dataBase.query(VoiceNotesDBSchema.AudioInfoTable.name, null, VoiceNotesDBSchema.AudioInfoTable.Cols.ID + " = ?", new String[]{id}, null, null, null);
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            String bd_id = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.ID));
            String bd_name = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.AUDIO_NAME));
            String bd_duracion = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.DURACION));
            String bd_fechaCreacion = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.FECHA_CREACION));
            String bd_idioma = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.IDIOMA));
            String bd_inQueue = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.IN_QUEUE));
            String bd_tag = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.TAG));
            String bd_transcriptioPath = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.TRANSCRIPTION_PATH));
            String bd_wavPath = cursor.getString(cursor.getColumnIndex(VoiceNotesDBSchema.AudioInfoTable.Cols.WAV_PATH));

            Date fc = new Date(); fc.setTime(Long.valueOf(bd_fechaCreacion));
            Date dur = new Date(); dur.setTime(Long.valueOf(bd_duracion));
            return new AudioInfo(bd_id, bd_wavPath, bd_transcriptioPath, Integer.valueOf(bd_inQueue) == 1 , bd_tag, dur, fc, bd_idioma);
        }finally {
            cursor.close();
        }
    }
}
