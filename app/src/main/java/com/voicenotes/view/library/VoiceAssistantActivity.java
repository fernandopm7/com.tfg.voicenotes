package com.voicenotes.view.library;

import android.app.Dialog;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.voicenotes.R;
import com.voicenotes.services.VoiceNotesService;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.library.adapter.CustomAdapter;
import com.voicenotes.view.library.adapter.CustomAdapterElement;
import com.voicenotes.view.library.ui.AudioPlayer;
import com.voicenotes.view.record.RecordActivity;
import com.voicenotes.view.utils.AudioTagsHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

public class VoiceAssistantActivity extends AppCompatActivity implements RecognitionListener {
    /* Named searches allow to quickly reconfigure the decoder */
    public final static String KWS_SEARCH = "wakeup";
    private final static String ACTION_RECORD ="grabar";
    public final static String ACTION_SEARCH ="buscar";
    private final static String ACTION_EXIT = "salir";
    private final static String MENU_SEARCH =  "menu";
    private final static String ACTION_REPRODUCIR = "reproducir";
    private final static String ACTION_FILTRAR =  "filtrar";
    /* Keyword we are looking for to activate menu */
    private  final static String KEYPHRASE =  "agenda personal";

    public SpeechRecognizer recognizer;

    private boolean searching = false;
    private boolean reproducir = false;
    public boolean asistenteGrabacionActivado = false;
    public boolean askingName = false;
    public boolean askingTag = false;
    private boolean filtrando = false;
    boolean flag = false;

    public String recordingName ="";
    private static String activeTag = "Home";

    public static RecordActivity currentRec = null;

    public TextToSpeech tts;

    public ArrayList<CustomAdapterElement> elementosBiblioteca;

    public VoiceNotesService voiceNotesService = null;

    private HashMap<String, Integer> captions;
    public Map<String,AudioInfo> mapa = new HashMap<>();


    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals(ACTION_SEARCH)) {
            searching = true;
            if (currentRec!=null){
                currentRec.backActivity();
                currentRec=null;
            }
            recognizer.stop();

            ((TextView) findViewById(R.id.result_textBib)).setText(getString(R.string.now_you_are_in_search_screen));
            searchAction();
            flag = true;
            //ConvertTextToSpeech("say something now");
            ConvertTextToSpeech(getString(R.string.say_something_now));
            while (tts.isSpeaking()){

            }
            recognizer.startListening(ACTION_SEARCH);
            //switchSearch(ACTION_SEARCH);

        }
        else if (text.equals(ACTION_EXIT)){
            //@TODO adaptacion
    //        ((TextView) findViewById(R.id.result_textBib)).setText(getString(R.string.exit));
    //        if (currentRec != null){ //quiere decir que esta actualmente en record..
            //            currentRec.backActivity();
            //          currentRec=null;
            //      }else{
            //      if(searchButton.getVisibility() == View.INVISIBLE){
                    //            //es que esta en buscar..damos a backfrombus..
            //      backFromBusqueda.performClick();
            //  }
           // }
        }
//            switchSearch(ACTION_EXIT);
        else if (text.equals(ACTION_RECORD)) {


            asistenteGrabacionActivado=true;
            if (currentRec == null) {
                ((TextView) findViewById(R.id.result_textBib)).setText(getString(R.string.now_you_are_in_record_screen));

                recognizer.stop();

               // actionButton.performClick();
                grabarAction();
            } else {
                ((TextView) findViewById(R.id.result_textBib)).setText(getString(R.string.you_are_already_in_record_screen));
            }
        }else if (text.equals(ACTION_FILTRAR)){
            if (currentRec!=null){ //si estamos en la pantalla de grabar, volvemos a bib.
                currentRec.backActivity();
                currentRec=null;
            }
            filtrando=true;
            recognizer.stop();
            recognizer.startListening(ACTION_SEARCH);


        }
        else if (text.equals(ACTION_REPRODUCIR)){
            if (currentRec!=null){ //si estamos en la pantalla de grabar, volvemos a bib.
                currentRec.backActivity();
                currentRec=null;
            }
            String speech = "";
            reproducir = true;
            switchSearch(ACTION_REPRODUCIR);
        }
        //          switchSearch(ACTION_RECORD);
        else {


            ((TextView) findViewById(R.id.result_textBib)).setText(text);


            System.out.println("flag activated: "+  text);
            if (reproducir){//reproducir
                Integer pos =  text.equals(getString(R.string.one)) ? 0 : (text.equals(getString(R.string.two)) ? 1 :2);
                recognizer.stop();
                String speech = "";
                if (elementosBiblioteca.size() > pos){
                    //   System.out.println("OnPlaySelectedActivityClick con audioName: " + audioInfo.getName());
                    //AudioPlayer dialog here.
                    final Dialog dialog = new Dialog(this);
                    File audioFile = voiceNotesService.getAudioFile(getApplicationContext(),elementosBiblioteca.get(pos).getName());
                    final AudioPlayer player = new AudioPlayer(this, elementosBiblioteca.get(pos).getName(),audioFile,dialog);
                    player.play();

                }else{
                    //speech = "No records were found";
                    speech = getString(R.string.voice_note_not_found);
                    ConvertTextToSpeech(speech);
                }

                reproducir = false;

                recognizer.startListening(KWS_SEARCH);
                while (tts.isSpeaking()){

                }
            }else if (searching){//buscar
                System.out.println("entro en searching");
                requestSearchImput(text);
                //searchView.setQuery(text, true);
            }

            //else if (asistenteGrabacionActivado){
            //  if (askingName){
            //    recordingName = text;
            //  recognizer.stop();
            // System.out.println("HE LLEGAOOOOOOOOOOOOO");
            //      File currentRecording = new File(currentRec.outputFile);
            //    Date horaMinSeg = currentRec.currentDate;
            //  saveAudioUsingAccesibleMode(currentRecording, recordingName,"BlindsTag",horaMinSeg);
            //askingName=false;
            // asistenteGrabacionActivado=false;
            //}
            //else if (askingTag){
            //  System.out.println("******************** BIB: askingTag **********************");
            // tagName= text;
            //recognizer.stop();
            //askingTag=false;
            //}
            // }
            flag = false;

        }
    }
 //______________________________________________

    @Override
    public void onResult(Hypothesis hypothesis) {
        ((TextView) findViewById(R.id.result_textBib)).setText("");
        if (hypothesis != null) {
            if (asistenteGrabacionActivado) {
                if (askingName) {
                    recordingName = hypothesis.getHypstr();
                    recognizer.stop();
                    System.out.println("HE LLEGAOOOOOOOOOOOOO");
                    System.out.println(  "RAWDATA while askingName:  "+recognizer.getDecoder().getRawdata().toString());
                    System.out.println("RAWDATA size:  " + recognizer.getDecoder().getRawdata().length);
                    //File currentRecording = new File(currentRec.outputFile);
                    //Date horaMinSeg = currentRec.currentDate;
                    //saveAudioUsingAccesibleMode(currentRecording, recordingName, "BlindsTag", horaMinSeg);
                    //asistenteGrabacionActivado = false;
                    askingName = false;
                    askTag();
                }else if (askingTag){
                    final String recordingTag = hypothesis.getHypstr();
                    recognizer.stop();
                    File currentRecording = new File(currentRec.outputFile);
                    Date horaMinSeg = currentRec.currentDate;
                    saveAudioUsingAccesibleMode(currentRecording, recordingName, recordingTag, horaMinSeg);
                    askingTag = false;
                    asistenteGrabacionActivado = false;
                }
            }else{
                if(filtrando && (!hypothesis.getHypstr().equals(ACTION_FILTRAR))){
                    filtrando=false;
                    String tagName=hypothesis.getHypstr();
                    recognizer.stop();
                    List<String> alltags = AudioTagsHelper.getPersonalTags(getApplicationContext());
                    if (alltags.contains(tagName)) {

                        activeTag = tagName;
                        requestTitleChange(activeTag);
                       // appName.setText(activeTag);
                        updateListView();
                        //  ConvertTextToSpeech("filtered finished ");
                        ConvertTextToSpeech(getString(R.string.filtered_finished));
                        while (tts.isSpeaking()) {

                        }
                        //ConvertTextToSpeech(elementosBiblioteca.size() + " results found");
                        ConvertTextToSpeech(elementosBiblioteca.size() + getString(R.string.results_found));
                        while (tts.isSpeaking()) {

                        }
                    }else{
                        // ConvertTextToSpeech("filtered finished ");
                        ConvertTextToSpeech(getString(R.string.filtered_finished));
                        while (tts.isSpeaking()) {

                        }
                        //ConvertTextToSpeech(0 + " results found");
                        ConvertTextToSpeech(0 + getString(R.string.results_found));
                    }
                    recognizer.startListening(KWS_SEARCH);

                }
                //else if (grabando){
                //      recognizer.getDecoder().getRawdata();
                //}
            }
            String text = hypothesis.getHypstr();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

        }
    }
    //______________________________________________

    @Override
    public void onBeginningOfSpeech() {
    }
    //______________________________________________

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        if (searching){
            recognizer.stop();
            //si el asistente de voz ha acabado de buscar->el aistente mediante tts reproducirá el nombre de las 3 primeras notas de voz.
            String speech = "";
            if (!elementosBiblioteca.isEmpty()){
                if (elementosBiblioteca.get(0) != null){
                    speech = getString(R.string.the_first_result_is) + withoutDotWav(elementosBiblioteca.get(0).getName());
                    if (elementosBiblioteca.size() >1) {
                        speech = speech + getString(R.string.the_second_result_is)+ withoutDotWav(elementosBiblioteca.get(1).getName());
                        if (elementosBiblioteca.size() >2){
                            speech = speech + getString(R.string.the_third_result_is) + withoutDotWav(elementosBiblioteca.get(2).getName());
                        }
                    }
                }

            }else{
                // speech = "No results were found";
                speech = getString(R.string.no_reults_were_found);
            }
            ConvertTextToSpeech(speech);
            searching = false; //searching finalizado.
            while(tts.isSpeaking()){
                //esperamos a que acabe de hablar el asistente de voz
            }
            recognizer.startListening(KWS_SEARCH);
        }else {
            if ((recognizer != null) && (recognizer.getSearchName() != null)) {
                if (!recognizer.getSearchName().equals(KWS_SEARCH))
                    switchSearch(KWS_SEARCH);
            }
        }
    }
    //______________________________________________

    @Override
    public void onError(Exception error) {
        //((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
        // this.actionButton.setColorFilter(Color.argb(255, 255, 255, 255));
    }
    //______________________________________________

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }
    //______________________________________________
    //______________________________________________
    //______________________________________________

    private void switchSearch(String searchName) {
        recognizer.stop();
        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH)) {
            System.out.println("switchSearch-KWS_SEARCH: "+ searchName);
            recognizer.startListening(searchName);
        }else {
            recognizer.startListening(searchName, 10000);
            System.out.println("switchSearch-NO_KWS_SEARCH:  " + searchName);
        }
        String caption = getResources().getString(captions.get(searchName));
        // this.actionButton.setColorFilter(Color.argb(255, 255, 0, 0));
        // ((TextView) findViewById(R.id.result_textBib)).setText("captionSwitchsear: " +caption);
    }

    public void ConvertTextToSpeech(String text) {
        // TODO Auto-generated method stub
        if(text==null||"".equals(text))
        {
            //text = "Content not available";
            //  text = "contenido no disponible";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void askTag (){
        ConvertTextToSpeech(getString(R.string.give_a_tag_to_the_voice_note));
        while(tts.isSpeaking()){
            //mientras el asistente hable, no hacer nada.
        }
        askingTag = true;
        recognizer.startListening(ACTION_SEARCH);
    }

    private void saveAudioUsingAccesibleMode(File currentRecord,String name,String tag,Date horaMinSeg){
        System.out.println("EL NOMBRE EEE:  "+name);
        String path = name + ".wav";
        final File aux = buscarEnAudioList(path) != null ? new File(buscarEnAudioList(path)):null;
        String res="";

        res = path;
        if (aux!=null){
            aux.delete();
        }
        final String destName = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC) + "/" + res;
        System.out.println("1111111111");

        //todo mecanica de tags here..
        File filex = new File(destName);
        // AudioInfo audioInfo = new AudioInfo(filex.getName(), null, null,true,tag,horaMinSeg,new Date());
        File currentRecording = currentRecord;
        //File wavFile = ObjectsManager.writeFile(getApplicationContext(), "recordings", filex.getName(), currentRecording);
        //AudioMap.setAudioInfo(getApplicationContext(), filex.getName(), audioInfo);
        //ThreadQueue.addElement(filex.getName());
        voiceNotesService.processAudio(getApplicationContext(),currentRecording,filex.getName(),tag,new Date(),horaMinSeg,currentRec.currenteTranscriptionLanguage != null ? currentRec.currenteTranscriptionLanguage : "es-es");
        recognizer.startListening(KWS_SEARCH);
    }

    public void updateListView(){
        elementosBiblioteca.clear();
        Map<String,AudioInfo> mapaUpdated = voiceNotesService.getVoiceNotesMap(getApplicationContext());
        mapa = mapaUpdated;
        addAllAsCAEList(getKeys()); //Audiomap.getKeys
        loadListElementsFromMap(null);
    }

    private String withoutDotWav(String in){
        return in.replaceAll(".wav","");
    }

    public String buscarEnAudioList(String valor){
        for (String elem: mapa.keySet()){ //AudioMap.getkeys()
            if (elem.contains(valor)){
                return mapa.get(elem).getWavPath(); //AudioMap.getAudioInfo(elem)
            }
        }
        return null;
    }

    public  void addAllAsCAEList(String[] elems){

        if (activeTag.contentEquals("Home")){
            //todos..

            for (String elem: elems) {
                if (mapa.get(elem) != null) {//AudioMap.getAudioInfo(elem
                    elementosBiblioteca.add(new CustomAdapterElement(elem, mapa.get(elem).getFechaCreacion(), mapa.get(elem).getDuration()));
                }
            }
        }else if (activeTag.contentEquals("Reminders")){
            //solo con recordatorio
            //todo..provicionalmente mostramos todo..
            for (String elem: elems) {
                if (mapa.get(elem) != null) {
                    elementosBiblioteca.add(new CustomAdapterElement(elem, mapa.get(elem).getFechaCreacion(),mapa.get(elem).getDuration()));
                }
            }
        }else{
            for (String elem: elems){
                if (mapa.get(elem) != null) {
                    if (mapa.get(elem).getTag().contentEquals(activeTag)) { //solo añadimos elemenstos del mapa que son del mismo tag que el activo
                        elementosBiblioteca.add(new CustomAdapterElement(elem, mapa.get(elem).getFechaCreacion(), mapa.get(elem).getDuration()));
                    }
                }
            }
        }
    }

    private String[] getKeys(){
        return mapa.keySet().toArray(new String[mapa.keySet().size()]);
    }

    private void loadListElementsFromMap(String[] elems){
        //if (elems==null) {
        //arrlis = new ArrayList<String>();
        //     for (String elem : AudioMap.getkeys()) {
        //   if (!AudioMap.getAudioInfo(elem).isInQueue())
        //     arrlis.add(elem);
        //  }
        // System.out.println("arrlist elements:  " + arrlis.toString());
        audioView.setAdapter(new CustomAdapter(this, this));
        //}else{
        //    List<String> lista =  new ArrayList<String>();
        //    for (String item: elems){
        //      lista.add(item);
        //}
        //udioView.setAdapter(new CustomAdapter(this, this,lista));
        //}
    }

}
