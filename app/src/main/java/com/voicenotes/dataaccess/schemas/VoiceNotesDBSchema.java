package com.voicenotes.dataaccess.schemas;

import java.util.Date;

public class VoiceNotesDBSchema {

    public static final class AudioInfoTable{
        public static final String name = "audioInfo";
        public static final class Cols{
            public static final String ID = "id";
            public static final String AUDIO_NAME = "name";
            public static final String WAV_PATH = "wavPath";
            public static final String TRANSCRIPTION_PATH ="transcriptionPath";
            public static final String DURACION = "duracion";
            public static final String FECHA_CREACION = "fechaCreacion";
            public static final String IN_QUEUE = "inQueue";
            public static final String TAG = "tag";
            public static final String IDIOMA = "idioma";

        }
    }
}
