package com.voicenotes.dataaccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.voicenotes.dataaccess.schemas.VoiceNotesDBSchema;

public class VoiceNotesSQLHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    public static final String DATABASE_NAME = "voiceNotes.db";
    public VoiceNotesSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + VoiceNotesDBSchema.AudioInfoTable.name + "(" +
                "_id integer primary key autoincrement, " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.ID + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.AUDIO_NAME + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.DURACION + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.FECHA_CREACION + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.IDIOMA + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.IN_QUEUE + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.TAG + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.TRANSCRIPTION_PATH + ", " +
                VoiceNotesDBSchema.AudioInfoTable.Cols.WAV_PATH + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
