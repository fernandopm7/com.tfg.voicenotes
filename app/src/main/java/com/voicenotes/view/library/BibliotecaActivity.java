package com.voicenotes.view.library;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.voicenotes.R;
import com.voicenotes.services.VoiceNotesService;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.library.ui.AudioPlayer;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;
import com.voicenotes.view.utils.AudioTagsHelper;
import com.voicenotes.view.record.RecordActivity;
import com.voicenotes.view.utils.indexes.AudioSearcher;
import com.voicenotes.view.utils.indexes.LuceneConstants;
import com.voicenotes.view.settings.SettingsActivity;
import com.voicenotes.view.library.adapter.CustomAdapter;
import com.voicenotes.view.library.adapter.CustomAdapterElement;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.view.View.GONE;
import static android.widget.Toast.makeText;
import static java.lang.Thread.sleep;


public class BibliotecaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecognitionListener {

    public VoiceNotesService voiceNotesService = null;
    public Map<String,AudioInfo> mapa = new HashMap<>();
    SetupTask currentSetup = null;
    private static String activeTag = "Home";
    private List<String> showingTags = new ArrayList<String>();
    SubMenu submenu = null;
    //ArrayList<String> listaToDelete = new ArrayList<String>();
    public ArrayList<CustomAdapterElement> elementosBiblioteca;
    FloatingActionButton actionButton;
    ListView audioView;
    NavigationView navigationView;
    boolean isSpeaking = false; //pausa el speechRecognizer
    ImageButton quitarSelecciones;
    ImageButton share;
    boolean flag = false;
    ImageButton deleteFromlist;
    ImageButton searchButton;
    ImageButton menuFromTopToolbar;
    ImageButton backFromBusqueda;
    ImageButton filterButton;
    public TextView contadorSelecciones;
    TextView appName;
    SearchView searchView;
    TopDocs search;
    AudioSearcher audioSearcher;
    final static int RQS_OPEN_AUDIO_MP3 = 1;
    Cursor audioCursor;
    private Toolbar toolbarBot;
    AppBarLayout abl;
    private Toolbar toolbarTop;
    private NavigationView mNavigationView;
    private BibliotecaActivity bib = this;
    //permissions code:
    private static final int REQUEST_WRITE_PERMISSION = 786;
    private static final int REQUEST_RECORD_PERMISSION = 787;
    private boolean writePermissionGranted = true;
    private boolean recordPermissionGranted = true;
    private String currentFilter = "name"; //name by default

    public static RecordActivity currentRec = null;

    //blind helper here

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
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    private boolean searching = false;
    private boolean reproducir = false;
    public boolean asistenteGrabacionActivado = false;
    public boolean askingName = false;
    public boolean askingTag = false;
    private boolean filtrando = false;
    public String recordingName ="";
    public String tagName="";
    public boolean isRecording=false;
    //end of blind helper

    public String buscarEnAudioList(String valor){
        for (String elem: mapa.keySet()){ //AudioMap.getkeys()
            if (elem.contains(valor)){
                return mapa.get(elem).getWavPath(); //AudioMap.getAudioInfo(elem)

            }
        }
        return null;

    }

    public void continueRecog(){
        //ConvertTextToSpeech("now give a name to the voice note");
        ConvertTextToSpeech(getString(R.string.give_a_name_to_the_voice_note));
        while(tts.isSpeaking()){
            //mientras el asistente hable, no hacer nada.
        }
        askingName = true;
        recognizer.startListening(ACTION_SEARCH);
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

    public static void saveCurrentRec (RecordActivity rec){
        currentRec=rec;
    }

    private void getFilteredList(){

        Collections.sort(elementosBiblioteca, new Comparator<CustomAdapterElement>() {
            @Override
            public int compare(CustomAdapterElement e1, CustomAdapterElement e2) {
                if (currentFilter.contentEquals("nombre")) {
                    if (e1.getName() == null || e2.getName() == null)
                        return 0;
                    return e1.getName().compareTo(e2.getName());
                }else if (currentFilter.contentEquals("fecha")){
                    if (e1.getDate() == null || e2.getDate() == null)
                        return 0;
                    return e1.getDate().compareTo(e2.getDate());
                }else{ //duración
                    if (e1.getDuration() == null || e2.getDuration() == null)
                        return 0;
                    return e1.getDuration().compareTo(e2.getDuration());
                }
            }


        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer( Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_home) {
            // Handle the camera action
            activeTag="Home";
            appName.setText( activeTag);
        } else if (id == R.id.nav_reminders) {
            //activeTag="Reminders";
            //appName.setText( activeTag);
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);

            startActivity(intent);


        } else { //personal tags.. todo quitar de submenu y poner en menu..
            activeTag= AudioTagsHelper.getPersonalTags(getApplicationContext()).get(id-10); //todo verificar

            appName.setText( activeTag);

        }
        updateListView();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //////////////
    /////////////




    /** Called when the activity is first created. */
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

    private void addItemsRunTime(NavigationView navigationView) {
        //adding items run time

        if (submenu != null){

            submenu.removeItem(9);//usaremos "9" siempre para identificar el submenu
            submenu.clear();

            submenu=null;
        }

        final Menu menu = navigationView.getMenu();

        // adding a section and items into it
        // final SubMenu subMenu = menu.addSubMenu("Personal Tags");
        //subMenu.add("Work");
        // subMenu.add("University");
        // subMenu.add("Science club");
        // subMenu.add("Others");

        submenu = menu.addSubMenu(0, 9, Menu.NONE, getString(R.string.personal_tags));

        //comenzamos en 10..
        if (AudioTagsHelper.getPersonalTags(getApplicationContext()) != null){
            int i=10;
            for (String tag: AudioTagsHelper.getPersonalTags(getApplicationContext())){

                submenu.add(0, i, Menu.NONE, tag);
                i = i + 1;

            }
        }




        // refreshing navigation drawer adapter
        /* for (int i = 0, count = mNavigationView.getChildCount(); i < count; i++) {
            final View child = mNavigationView.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }*/
    }

    public void updateListView(){
        elementosBiblioteca.clear();
        Map<String,AudioInfo> mapaUpdated = voiceNotesService.getVoiceNotesMap(getApplicationContext());
        mapa = mapaUpdated;
        addAllAsCAEList(getKeys()); //Audiomap.getKeys
        loadListElementsFromMap(null);
    }

    @Override
    public void onResume() {
        updateLanguage();

        super.onResume();
        if (currentRec!=null) {
            currentRec.finish();
            currentRec = null;
        }
        updateListView();

        addItemsRunTime(navigationView);

        loadListElementsFromMap(null);

    }

    //request permissions on runtime code..
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordPermissionGranted = true;
                //blind helper here
                if (currentSetup != null) {
                    currentSetup.cancel(true);
                    currentSetup = null;
                    currentSetup = new SetupTask(this);
                    currentSetup.execute();
                } else {
                    currentSetup = new SetupTask(this);
                    currentSetup.execute();
                }
                //end blind helper

            } else {
                recordPermissionGranted = false;
            }
        } else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                writePermissionGranted = true;
                requestRecordPermission();
            } else {
                writePermissionGranted = false;
            }

        }
    }

    //blind helper here

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {

        WeakReference<BibliotecaActivity> activityReference;

        SetupTask(BibliotecaActivity activity) {

            this.activityReference = new WeakReference<>(activity);

        }

        @Override

        protected Exception doInBackground(Void... params) {

            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);

            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override

        protected void onPostExecute(Exception result) {

            if (result != null) {

                //  ((TextView) activityReference.get().findViewById(R.id.caption_text))

                //        .setText("Failed to init recognizer " + result);


            } else {

                activityReference.get().switchSearch(KWS_SEARCH);

            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            if (!recognizer.getSearchName().equals(KWS_SEARCH))
                switchSearch(KWS_SEARCH);
            recognizer.cancel();
            recognizer.shutdown();

        }
    }

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
            searchButton.performClick();
            flag = true;
            //ConvertTextToSpeech("say something now");
            ConvertTextToSpeech(getString(R.string.say_something_now));
            while (tts.isSpeaking()){

            }
            recognizer.startListening(ACTION_SEARCH);
            //switchSearch(ACTION_SEARCH);

        }
        else if (text.equals(ACTION_EXIT)){
            ((TextView) findViewById(R.id.result_textBib)).setText(getString(R.string.exit));
        if (currentRec != null){ //quiere decir que esta actualmente en record..
            currentRec.backActivity();
            currentRec=null;
        }else{
            if(searchButton.getVisibility() == View.INVISIBLE){
                //es que esta en buscar..damos a backfrombus..
                backFromBusqueda.performClick();
            }
        }
        }
//            switchSearch(ACTION_EXIT);
        else if (text.equals(ACTION_RECORD)) {


            asistenteGrabacionActivado=true;
            if (currentRec == null) {
                ((TextView) findViewById(R.id.result_textBib)).setText(getString(R.string.now_you_are_in_record_screen));

                recognizer.stop();

                actionButton.performClick();



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
                        final Dialog dialog = new Dialog(bib);
                       File  audioFile = voiceNotesService.getAudioFile(getApplicationContext(),elementosBiblioteca.get(pos).getName());
                        final AudioPlayer player = new AudioPlayer(bib, elementosBiblioteca.get(pos).getName(),audioFile,dialog);
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
                    searchView.setQuery(text, true);
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
    /**
     * This callback is called when we stop the recognizer.
     */
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
                    List<String>alltags = AudioTagsHelper.getPersonalTags(getApplicationContext());
                    if (alltags.contains(tagName)) {

                        activeTag = tagName;
                        appName.setText(activeTag);
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
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();


        }

    }
    @Override
    public void onBeginningOfSpeech() {
    }

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

    private String withoutDotWav(String in){
        return in.replaceAll(".wav","");
    }
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
    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "es-es/es-es"))
                .setDictionary(new File(assetsDir, "es-es/es-es.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();

        recognizer.addListener(this);
        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */
        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);


        File digitsGrammar = new File(assetsDir, "digitos.gram");
        recognizer.addGrammarSearch(ACTION_REPRODUCIR, digitsGrammar);

        File languageModel = new File(assetsDir, "es-es/es-es.lm");

        recognizer.addNgramSearch(ACTION_SEARCH, languageModel);


    }
    @Override
    public void onError(Exception error) {
        //((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
       // this.actionButton.setColorFilter(Color.argb(255, 255, 255, 255));
    }
    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    //end blind helper

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
            writePermissionGranted=true;
        }
    }

    private void requestRecordPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_PERMISSION);
        } else {
            recordPermissionGranted=true;
        }
    }

    String text;

    public TextToSpeech tts;

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



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer);
        voiceNotesService = new VoiceNotesService();
        mapa = voiceNotesService.getVoiceNotesMap(getApplicationContext());
        System.out.println("bib: entro en oncreate");
         tts = new TextToSpeech(this,  new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                tts.setSpeechRate(1f);
                if(status == TextToSpeech.SUCCESS){
                    Locale loc;
                    if (DefaultSettingsXmlParser.getCurrentLenguage().equals("spanish")) {
                         loc = new Locale("spa", "ESP");
                    }else{
                         loc = new Locale("en");
                    }
                    int result=tts.setLanguage(loc);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        ConvertTextToSpeech(null);
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });

        //ConvertTextToSpeech("Wellcome back");
       // ConvertTextToSpeech("Bienvenido");
        //blind helper here

        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(ACTION_SEARCH, R.string.digits_caption);
        captions.put(ACTION_EXIT, R.string.phone_caption);
        captions.put(ACTION_RECORD, R.string.forecast_caption);
        captions.put(ACTION_REPRODUCIR,R.string.reproducir_caption);

        //((TextView) findViewById(R.id.caption_text))
        //      .setText("Preparing the recognizer");
        //this.actionButton.setColorFilter(Color.argb(255, 255, 255, 255));

        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task


        //solicitamos permisos de escritura:
        requestStoragePermission();
        if (this.recordPermissionGranted) {
            if (currentSetup != null) {
                currentSetup.cancel(true);
                currentSetup = null;
                currentSetup = new SetupTask(this);
                currentSetup.execute();
            } else {
                currentSetup = new SetupTask(this);
                currentSetup.execute();
            }
        }
        //end blind helper




        //inicializamos los elementos de la lista elementosBib..
        elementosBiblioteca = new ArrayList<CustomAdapterElement>();
        addAllAsCAEList(getKeys());// addAllAsCAEList(AudioMap.getkeys());
        //   requestWindowFeature(Window.FEATURE_ACTION_BAR);

        //////////////////////////////7nav drawer here

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarTop, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        addItemsRunTime(navigationView);
        navigationView.setNavigationItemSelectedListener(this);







        /////////////////////////////7
        ///////////////////////////


        //fixme new toolbars here

        //botton bar
        abl = findViewById(R.id.botLayout);
        toolbarBot = (Toolbar) findViewById(R.id.toolbarBot);
        quitarSelecciones = toolbarBot.findViewById(R.id.backFromList);
        quitarSelecciones.setOnClickListener(buttonQuitarSeleccionesListener);
        contadorSelecciones = (TextView) toolbarBot.findViewById(R.id.seleccionadosFromList);
        deleteFromlist = (ImageButton) toolbarBot.findViewById(R.id.deleteFromList);
        deleteFromlist.setOnClickListener(ButtonDeleteFromListListener);


        toolbarTop = (Toolbar) findViewById(R.id.toolbarTop);
        menuFromTopToolbar = toolbarTop.findViewById(R.id.menuDeLaToolbarTop);
        menuFromTopToolbar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer( Gravity.LEFT);

            }
        });
        filterButton = toolbarTop.findViewById(R.id.iconFilter);
        filterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == filterButton) {
                    PopupMenu popup = new PopupMenu(BibliotecaActivity.this, view);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            List<String> listaOrdenada;
                            switch (menuItem.getItemId()){
                                case R.id.filtroNombre:
                                    currentFilter= getString(R.string.nombre);
                                    break;
                                case R.id.filtroFecha:
                                    currentFilter=getString(R.string.fecha);
                                    break;
                                case R.id.filtroDuracion:
                                    currentFilter=getString(R.string.duracion);
                                    break;

                            }
                            getFilteredList();//ordena elementosBiblioteca..
                            loadListElementsFromMap(null);

                            return false;
                        }
                    });// to implement on click event on items of menu
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.filter_menu, popup.getMenu());
                    popup.show();
                }
            }
        });

        backFromBusqueda = toolbarTop.findViewById(R.id.backToolbarTop);
        backFromBusqueda.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                searchView.setVisibility(View.INVISIBLE);
                backFromBusqueda.setVisibility(View.INVISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                //menu.setVisibility(View.VISIBLE);
                appName.setVisibility(View.VISIBLE);
                actionButton.setVisibility(View.VISIBLE);
                menuFromTopToolbar.setVisibility(View.VISIBLE);
                //estos 3 proximas lineas son para meter en la lista los elementos del mapa..antes teniamos los de la busqueda.
                elementosBiblioteca.clear();
                addAllAsCAEList(getKeys());// addAllAsCAEList(AudioMap.getkeys());
                loadListElementsFromMap(null);

            }
        });
        appName = toolbarTop.findViewById(R.id.toolbarText);
        searchButton= toolbarTop.findViewById(R.id.searchButton);
        searchView = toolbarTop.findViewById(R.id.searchImput);
        //fixme until here



        //action bar here
        // android.support.v7.app.ActionBar mActionBar = getSupportActionBar();

        //  System.out.println("BIBLIOTECA: mactionbar: "+mActionBar);
        // mActionBar.setDisplayShowHomeEnabled(false);
        // mActionBar.setDisplayShowTitleEnabled(false);
        //   LayoutInflater mInflater = LayoutInflater.from(this);
        // View mCustomView = mInflater.inflate(R.layout.activity_custom_action_bar, null);
        //appName = mCustomView.findViewById(R.id.appName);
        // quitarSelecciones = mCustomView.findViewById(R.id.quitarSelecciones);
        // quitarSelecciones.setOnClickListener(buttonQuitarSeleccionesListener);
        //deleteFromlist = mCustomView.findViewById(R.id.deleteFromList);
        // deleteFromlist.setOnClickListener(ButtonDeleteFromListListener);
        // filterButton = mCustomView.findViewById(R.id.filterBut);
        //filterButton.setOnClickListener(new OnClickListener() {
        //   @Override
        // public void onClick(View view) {
        //   //todo tags
        //}
        //});
        // contadorSelecciones = mCustomView.findViewById(R.id.contadorSeleccionados );
        //mActionBar.setCustomView(mCustomView);
        //  mActionBar.setDisplayShowCustomEnabled(true);
        //search de la action bar here
        //menu = mCustomView.findViewById(R.id.menu);
        //backFromBusqueda = mCustomView.findViewById(R.id.quitarBusquedaInput);
        // backFromBusqueda.setOnClickListener(new OnClickListener() {
        //   @Override
        // public void onClick(View view) {
        //   searchButton.setVisibility(View.VISIBLE);
        // searchView.setVisibility(View.INVISIBLE);
        //filterButton.setVisibility(View.INVISIBLE);
        //   backFromBusqueda.setVisibility(View.INVISIBLE);
        // appName.setVisibility(View.VISIBLE);
        //menu.setVisibility(View.VISIBLE);
        //}
        //});
        //  searchButton= mCustomView.findViewById(R.id.searchBut);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                searchButton.setVisibility(View.INVISIBLE);
                // filterButton.setVisibility(View.VISIBLE);
                //backFromBusqueda.setVisibility(View.VISIBLE);
                appName.setVisibility(View.INVISIBLE);
                //menu.setVisibility(View.INVISIBLE);
                searchView.setVisibility(View.VISIBLE);
                searchView.setIconified(false);
                backFromBusqueda.setVisibility(View.VISIBLE);
                actionButton.setVisibility(View.INVISIBLE);
                menuFromTopToolbar.setVisibility(View.INVISIBLE);



            }
        });
        // searchView = mCustomView.findViewById(R.id.searchInput);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                elementosBiblioteca.clear();
                addAllAsCAEList(getKeys());// addAllAsCAEList(AudioMap.getkeys());
                loadListElementsFromMap(null);
                searchView.setVisibility(View.INVISIBLE);
                searchButton.setVisibility(View.VISIBLE);
                appName.setVisibility(View.VISIBLE);

                return false;
            }

        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                String query = searchView.getQuery().toString();
                System.out.println("LucActivity:  query: " + query);
                if (query==null ||query==""){
                    loadListElementsFromMap(null);
                }else {
                    try {
                        try {
                            audioSearcher = new AudioSearcher (getApplicationContext());
                            System.out.println("Bib: audioSearch ini: " + audioSearcher);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //todo if searcher ==null es que no hay elementos en el indice
                        if ((audioSearcher!=null)&&((search = audioSearcher.search(query)) != null)) {
                            System.out.println("Bib: audioSearcher: " + audioSearcher);

                            System.out.println("LucActivity: search:" + search);

                            String[] bestDocs = new String[search.scoreDocs.length];
                            int i = 0;
                            System.out.println("LucActivity: totalHits..:" + search.totalHits);
                            for (ScoreDoc doc : search.scoreDocs) {
                                bestDocs[i] = audioSearcher.getDocument(doc).get(LuceneConstants.FILE_NAME);
                                i++;
                            }

                            //System.out.println("LucActivity: bestDocs 1:" + bestDocs[0]);
                            elementosBiblioteca.clear();
                            //  ArrayList<String> temp = new ArrayList<String>();
                            // for (String elem: AudioMap.getkeys()){
                            //      temp.add(elem);
                            //  }
                            addAllAsCAEList(bestDocs);
                            //for (String elem: bestDocs){

                            //   if (temp.contains(elem)) {//si existe en el mapa es que no ha sido borrado..sino lo ignoramos (soluciÃ³n temporal al borrado del indice)
                            //       elementosBiblioteca.add(new CustomAdapterElement(elem));
                            //  }
                            //}
                        }else{ //no hay nada en el indice
                            elementosBiblioteca.clear();
                        }
                        loadListElementsFromMap(null);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                String query = searchView.getQuery().toString();
                System.out.println("LucActivity:  query: " + query);
                if (query==null ||query==""){
                    loadListElementsFromMap(null);
                }else {
                    try {
                        try {
                            //if (audioSearcher == null) {
                                audioSearcher = new AudioSearcher(getApplicationContext());
                            //}
                            System.out.println("Bib: audioSearch ini: " + audioSearcher);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //todo if searcher ==null es que no hay elementos en el indice
                        if ((audioSearcher!=null)&&((search = audioSearcher.search(query)) != null)) {
                            System.out.println("Bib: audioSearcher: " + audioSearcher);

                            System.out.println("LucActivity: search:" + search);

                            String[] bestDocs = new String[search.scoreDocs.length];
                            int i = 0;
                            System.out.println("LucActivity: totalHits..:" + search.totalHits);
                            for (ScoreDoc doc : search.scoreDocs) {
                                bestDocs[i] = audioSearcher.getDocument(doc).get(LuceneConstants.FILE_NAME);
                                i++;
                            }

                            //System.out.println("LucActivity: bestDocs 1:" + bestDocs[0]);
                            elementosBiblioteca.clear();
                            //  ArrayList<String> temp = new ArrayList<String>();
                            // for (String elem: AudioMap.getkeys()){
                            //      temp.add(elem);
                            //  }
                            addAllAsCAEList(bestDocs);
                            //for (String elem: bestDocs){

                            //   if (temp.contains(elem)) {//si existe en el mapa es que no ha sido borrado..sino lo ignoramos (soluciÃ³n temporal al borrado del indice)
                            //       elementosBiblioteca.add(new CustomAdapterElement(elem));
                            //  }
                            //}
                        }else{ //no hay nada en el indice
                            elementosBiblioteca.clear();
                        }
                        loadListElementsFromMap(null);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });



        actionButton = findViewById(R.id.fabNew);
        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //detenemos el recognizer
               // recognizer.stop();
                Intent intent = new Intent(getApplicationContext(), RecordActivity.class);

               RecordActivity.setBib(BibliotecaActivity.this);
                startActivity(intent);


            }
        });

        audioView = findViewById(R.id.customList);
        audioView.setDivider(null);
        ////   @Override
        // public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
        //// ( (CustomAdapter)arg0.getAdapter()).changeChechBoxesVisibility();

        //return true;
        //}
        //});
        loadListElementsFromMap(null);



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

/*
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        System.out.println("entro listItemClick: pos: " +  position);
        //llamamos a la activity de reproducir...y le pasamos por parÃ¡metro los elem necesarios
        String name = arrlis.get(position);
        Intent intent = new Intent(Biblioteca.this, PlaySelectedAudio.class);
        intent.putExtra("audioName",name);
        startActivity(intent);

      //  Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
    }
    */

    OnClickListener buttonQuitarSeleccionesListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            for (int i =0;i <elementosBiblioteca.size();i++){
                elementosBiblioteca.get(i).setChecked(false);
            }

            contadorSelecciones.setText("1");
            setInvisible();
            loadListElementsFromMap(null);
        }
    };

    OnClickListener ButtonDeleteFromListListener = new OnClickListener() {
        @Override
        public void onClick(View view) {


            for (CustomAdapterElement elem : ((ArrayList<CustomAdapterElement>)elementosBiblioteca.clone()) ) {

                if (elem.getChecked()) {
                    ArrayList<String> aa = new ArrayList<String>(){
                        @Override
                        public String toString(){
                            String res = "{";
                            for (int i=0; i< this.size();i++){
                                res = res +", "+this.get(i);
                            }
                            res = res+"}";
                            return res;
                        }
                    };


                    aa.addAll(Arrays.asList(getKeys()));
                    System.out.println("audioMap antes de eliminar: "+aa.toString());
                    //eliminamos de la memoria
                   // File audioToDelete = (File) ObjectsManager.readFile(getApplicationContext(), "recordings", elem.getName());
                  //  audioToDelete.delete(); //esto no sirve


                  //  ObjectsManager.deleteFile(getApplicationContext(),elem.getName()); //esto puede que si..nueva imple de eliminar..probar..
                    //eliminamos del mapa
                  //  AudioMap.deleteAudioInfo(elem.getName(), getApplicationContext());
                    //eliminamos del indice //todo comprobar si va bien
                   // AudioIndexer.deleteFromIndex(elem.getName());
                    voiceNotesService.deleteVoiceNote(getApplicationContext(),elem.getName());
                    mapa.remove(elem.getName());

                    //eliminamos de arrList y audioChecked (este ultimo simplemente al instanciar adapr otra vez es suficiente..)
                    elementosBiblioteca.remove(elem);
                    aa.clear();
                    aa.addAll(Arrays.asList(getKeys()));
                    System.out.println("onDeleteBib: audioMapKeys: " +aa.toString() + "elementosBib: "+elementosBiblioteca.toString() );
                }

            }
            setInvisible();
            // actualizamos la vista incializando el adapter otra vez.
            loadListElementsFromMap(null);
        }
    };


    OnClickListener buttonOpenOnClickListener
            = new OnClickListener(){

        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent();
            intent.setType("audio/mp3");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(
                    intent, "Open Audio (mp3) file"), RQS_OPEN_AUDIO_MP3);
        }};


    public void setVisible(){
        abl.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.INVISIBLE);
        // buttonShare.setVisibility(View.VISIBLE);
        // buttonDelete.setVisibility(View.VISIBLE);
        // appName.setVisibility(View.INVISIBLE);
        // menu.setVisibility(View.INVISIBLE);
        //searchButton.setVisibility(View.INVISIBLE);
        //searchView.setVisibility(View.INVISIBLE);
        //  filterButton.setVisibility(View.INVISIBLE);
        //  backFromBusqueda.setVisibility(View.INVISIBLE);
        //searchView.setVisibility(View.INVISIBLE);
        //deleteFromlist.setVisibility(View.VISIBLE);
        //contadorSelecciones.setVisibility(View.VISIBLE);
        //quitarSelecciones.setVisibility(View.VISIBLE);

    }
    public void setInvisible(){
        abl.setVisibility(GONE);
        actionButton.setVisibility(View.VISIBLE);
        // buttonShare.setVisibility(View.INVISIBLE);
        //  buttonDelete.setVisibility(View.INVISIBLE);
        // appName.setVisibility(View.VISIBLE);
        // menu.setVisibility(View.VISIBLE);
        //searchButton.setVisibility(View.VISIBLE);
        // backFromBusqueda.setVisibility(View.INVISIBLE);
        // filterButton.setVisibility(View.INVISIBLE);
        // searchView.setVisibility(View.INVISIBLE);
        //deleteFromlist.setVisibility(View.INVISIBLE);
        //contadorSelecciones.setVisibility(View.INVISIBLE);
        //quitarSelecciones.setVisibility(View.INVISIBLE);
    }
private String[] getKeys(){
    return mapa.keySet().toArray(new String[mapa.keySet().size()]);
}

private void updateLanguage(){

        if (DefaultSettingsXmlParser.getCurrentLenguage().equals("english")){
            tts.setLanguage(new Locale("en"));
        }else{
            tts.setLanguage(new Locale("spa", "ESP"));
        }
}

}