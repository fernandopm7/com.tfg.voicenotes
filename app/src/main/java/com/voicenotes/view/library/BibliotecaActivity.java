package com.voicenotes.view.library;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.voicenotes.R;
import com.voicenotes.services.VoiceNotesService;
import com.voicenotes.utils.centalmap.AudioInfo;
import com.voicenotes.view.library.adapter.CustomAdapterElement;
import com.voicenotes.view.library.ui.AudioPlayer;
import com.voicenotes.view.record.RecordActivity;
import com.voicenotes.view.settings.SettingsActivity;
import com.voicenotes.view.settings.utils.DefaultSettingsXmlParser;
import com.voicenotes.view.utils.AudioTagsHelper;
import com.voicenotes.view.utils.indexes.AudioSearcher;
import com.voicenotes.view.utils.indexes.LuceneConstants;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


public class BibliotecaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RecognitionListener {

    public VoiceNotesService voiceNotesService = null;
    public Map<String,AudioInfo> mapa = new HashMap<>();
    SetupTask currentSetup = null;
    private static String activeTag = "Home";
    private List<String> showingTags = new ArrayList<String>();
    SubMenu submenu = null;
    //ArrayList<String> listaToDelete = new ArrayList<String>();
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
    public TextToSpeech tts;

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

    private CustomAdapter mAdapter;

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
        String path = name + ".wav";
        final File aux = buscarEnAudioList(path) != null ? new File(buscarEnAudioList(path)):null;
        String res="";
        res = path;
        if (aux!=null){
         aux.delete();
        }
        final String destName = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC) + "/" + res;
        File filex = new File(destName);
        File currentRecording = currentRecord;
        voiceNotesService.processAudio(getApplicationContext(),currentRecording,filex.getName(),tag,new Date(),horaMinSeg,currentRec.currenteTranscriptionLanguage != null ? currentRec.currenteTranscriptionLanguage : "es-es");
        recognizer.startListening(KWS_SEARCH);
    }

    public static void saveCurrentRec (RecordActivity rec){
        currentRec=rec;
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
            activeTag="Home";
            appName.setText( activeTag);
        } else if (id == R.id.nav_reminders) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else { //personal tags..
            activeTag= AudioTagsHelper.getPersonalTags(getApplicationContext()).get(id-10);
            appName.setText( activeTag);
        }
        mAdapter.getFilter().filter(activeTag); //    sustituimos  updateListView();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public  ArrayList<CustomAdapterElement> getProductionElementsFilteredByActiveTag(String[] elems){ //@TODO optimizar esto..no acceder varias veces al mapa..
        ArrayList<CustomAdapterElement> productionElements = new ArrayList<CustomAdapterElement>();
        if (activeTag.contentEquals("Home")){
            //todos..
            for (String elem: elems) {
                if (mapa.get(elem) != null) {//AudioMap.getAudioInfo(elem
                    productionElements.add(new CustomAdapterElement(elem, mapa.get(elem).getFechaCreacion(), mapa.get(elem).getDuration()));
                }
            }
        }else if (activeTag.contentEquals("Reminders")){
            //solo con recordatorio
            //@todo..provicionalmente mostramos todo..cambiar cuando se añada funcionalidad de recordatorios
            for (String elem: elems) {
                if (mapa.get(elem) != null) {
                    productionElements.add(new CustomAdapterElement(elem, mapa.get(elem).getFechaCreacion(),mapa.get(elem).getDuration()));
                }
            }
        }else{
            for (String elem: elems){
                if (mapa.get(elem) != null) {
                    if (mapa.get(elem).getTag().contentEquals(activeTag)) { //solo añadimos elemenstos del mapa que son del mismo tag que el activo
                        productionElements.add(new CustomAdapterElement(elem, mapa.get(elem).getFechaCreacion(), mapa.get(elem).getDuration()));
                    }
                }
            }
        }
        return productionElements;
    }

    private void addItemsRunTime(NavigationView navigationView) {
        //adding items run time
        if (submenu != null){
            submenu.removeItem(9);//usaremos "9" siempre para identificar el submenu
            submenu.clear();
            submenu=null;
        }
        final Menu menu = navigationView.getMenu();
        submenu = menu.addSubMenu(0, 9, Menu.NONE, getString(R.string.personal_tags));
        if (AudioTagsHelper.getPersonalTags(getApplicationContext()) != null){
            int i=10;
            for (String tag: AudioTagsHelper.getPersonalTags(getApplicationContext())){
                submenu.add(0, i, Menu.NONE, tag);
                i = i + 1;
            }
        }
    }

    /**Esta función recupera de BD todos las notas de voz y luego filtra o muestra según el tag activo en ese momento*/
    public void updateListView(){
        //elementosBiblioteca.clear();
        Map<String,AudioInfo> mapaUpdated = voiceNotesService.getVoiceNotesMap(getApplicationContext());
        mapa = mapaUpdated;
        loadListElementsFromMap(getProductionElementsFilteredByActiveTag(getKeys()));
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
       // loadListElementsFromMap(null);
    }

    //request permissions on runtime code..
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordPermissionGranted = true;
                if (currentSetup != null) {
                    currentSetup.cancel(true);
                    currentSetup = null;
                    currentSetup = new SetupTask(this);
                    currentSetup.execute();
                } else {
                    currentSetup = new SetupTask(this);
                    currentSetup.execute();
                }
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
                //TODO do/show something
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
                //wait...
            }
            recognizer.startListening(ACTION_SEARCH);

        }else if (text.equals(ACTION_EXIT)){
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
        }else if (text.equals(ACTION_RECORD)) {
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
            reproducir = true;
            switchSearch(ACTION_REPRODUCIR);
        }else {
            ((TextView) findViewById(R.id.result_textBib)).setText(text);
            if (reproducir){//reproducir
                Integer pos =  text.equals(getString(R.string.one)) ? 0 : (text.equals(getString(R.string.two)) ? 1 :2);
                recognizer.stop();
                String speech = "";
                if (mAdapter.getCount() > pos){
                    final Dialog dialog = new Dialog(bib);
                    File  audioFile = voiceNotesService.getAudioFile(getApplicationContext(),((CustomAdapterElement)mAdapter.getItem(pos)).getName());
                    final AudioPlayer player = new AudioPlayer(bib, ((CustomAdapterElement)mAdapter.getItem(pos)).getName(),audioFile,dialog);
                    player.play();
                }else{
                    speech = getString(R.string.voice_note_not_found);
                        ConvertTextToSpeech(speech);
                }
                reproducir = false;
                recognizer.startListening(KWS_SEARCH);
                while (tts.isSpeaking()){
                    //wait..
                }
            }else if (searching){//buscar
                searchView.setQuery(text, true);
            }
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
                        appName.setText(activeTag);
                        mAdapter.getFilter().filter(activeTag);//updateListView();
                        ConvertTextToSpeech(getString(R.string.filtered_finished));
                        while (tts.isSpeaking()) {

                        }
                        ConvertTextToSpeech(mAdapter.getCount() + getString(R.string.results_found));
                        while (tts.isSpeaking()) {

                        }
                        ConvertTextToSpeech(getString(R.string.filtered_finished));
                        while (tts.isSpeaking()) {

                        }
                        ConvertTextToSpeech(0 + getString(R.string.results_found));
                    }
                    recognizer.startListening(KWS_SEARCH);
                }
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
            if (!mAdapter.displayedElements.isEmpty()){
                if (mAdapter.getItem(0) != null){
                    speech = getString(R.string.the_first_result_is) + withoutDotWav(((CustomAdapterElement)mAdapter.getItem(0)).getName());
                    if (mAdapter.getCount() >1) {
                        speech = speech + getString(R.string.the_second_result_is)+ withoutDotWav(((CustomAdapterElement)mAdapter.getItem(1)).getName());
                        if (mAdapter.getCount() >2){
                            speech = speech + getString(R.string.the_third_result_is) + withoutDotWav(((CustomAdapterElement)mAdapter.getItem(2)).getName());
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
              recognizer.startListening(searchName);
        }else {
            recognizer.startListening(searchName, 10000);
        }
        String caption = getResources().getString(captions.get(searchName));
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
        //empty
    }
    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    //************************************************
    //End of PocketSphinx RecongniterListener methods..
    //************************************************

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

    public void ConvertTextToSpeech(String text) {
        if(text==null||"".equals(text))
        {
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

        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(MENU_SEARCH, R.string.menu_caption);
        captions.put(ACTION_SEARCH, R.string.digits_caption);
        captions.put(ACTION_EXIT, R.string.phone_caption);
        captions.put(ACTION_RECORD, R.string.forecast_caption);
        captions.put(ACTION_REPRODUCIR,R.string.reproducir_caption);

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

        //////////////////////////////nav drawer here
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarTop, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        addItemsRunTime(navigationView);
        navigationView.setNavigationItemSelectedListener(this);

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
                            mAdapter.sortDisplayedElements();
                            //getFilteredList();//ordena elementosBiblioteca..
                            //loadListElementsFromMap(elementosBiblioteca);

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
                updateListView();
                //elementosBiblioteca.clear();
                //addAllAsCAEList(getKeys()); //cargamos "desde 0" todos los elementos @TODO ver si se puede conservar los elementos "originales" que hay en el adapter en lugar de volver a instanciar..
                //loadListElementsFromMap(null);
            }
        });
        appName = toolbarTop.findViewById(R.id.toolbarText);
        searchButton= toolbarTop.findViewById(R.id.searchButton);
        searchView = toolbarTop.findViewById(R.id.searchImput);

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
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                updateListView();
                //elementosBiblioteca.clear();
                //addAllAsCAEList(getKeys()); //cargamos "desde 0" todos los elementos @TODO ver si se puede conservar los elementos "originales" que hay en el adapter en lugar de volver a instanciar..
               // loadListElementsFromMap(null);
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
                mAdapter.getFilter().filter(query); //@TODO comprobar funcionamiento de esot..viene a sustituir el load.. que instancia el adapter otra vez..
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                String query = searchView.getQuery().toString();
                mAdapter.getFilter().filter(query);
                return false;
            }
        });

        actionButton = findViewById(R.id.fabNew);
        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
                RecordActivity.setBib(BibliotecaActivity.this);
                startActivity(intent);
            }
        });

        audioView = findViewById(R.id.customList);
        audioView.setDivider(null);

        //mAdapter = new CustomAdapter(this, elementosBiblioteca); //primera inicialización del adapter..

        //inicializamos los elementos de la lista elementosBib..
        // elementosBiblioteca = new ArrayList<CustomAdapterElement>();
        //addAllAsCAEList(getKeys());
        updateListView();

        //loadListElementsFromMap(null);
    }

    private void loadListElementsFromMap(ArrayList<CustomAdapterElement> elems){
        if (elems == null) {
            mAdapter = new CustomAdapter(this, new ArrayList<CustomAdapterElement>());
            audioView.setAdapter(mAdapter);
        }else {
            mAdapter = new CustomAdapter(this, elems);
            audioView.setAdapter(mAdapter);
        }
    }

    OnClickListener buttonQuitarSeleccionesListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            for (int i =0;i <mAdapter.getCount();i++){
                ((CustomAdapterElement)mAdapter.getItem(i)).setChecked(false);
            }
            contadorSelecciones.setText("1");
            setInvisible();
            //loadListElementsFromMap(null);
            updateListView();
        }
    };

    OnClickListener ButtonDeleteFromListListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            for (CustomAdapterElement elem : ((ArrayList<CustomAdapterElement>)mAdapter.displayedElements.clone()) ) {
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
                    voiceNotesService.deleteVoiceNote(getApplicationContext(),elem.getName());
                    mapa.remove(elem.getName());
                    //eliminamos de arrList y audioChecked (este ultimo simplemente al instanciar adapr otra vez es suficiente..)
                    //elementosBiblioteca.remove(elem);
                }
            }
            setInvisible();
            // actualizamos la vista incializando el adapter otra vez.
            //loadListElementsFromMap(null);
            updateListView();
        }
    };
/* //TODO file open para importar..
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
*/

    public void setVisible(){
        abl.setVisibility(View.VISIBLE);
        actionButton.setVisibility(View.INVISIBLE);
    }

    public void setInvisible(){
        abl.setVisibility(GONE);
        actionButton.setVisibility(View.VISIBLE);
    }

    private String[] getKeys(){ //@TODO crear una clase helper que contenga todas estas operaciones genéricas
        return mapa.keySet().toArray(new String[mapa.keySet().size()]);
    }

    private void updateLanguage(){
        if (DefaultSettingsXmlParser.getCurrentLenguage().equals("english")){
            tts.setLanguage(new Locale("en"));
        }else{
            tts.setLanguage(new Locale("spa", "ESP"));
        }
    }

    public class CustomAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater inflater = null;
        Context contexto;
        private Integer checkCount;
        List<Boolean> audioChecked ;
        boolean checksVisibles=false;
        private int checkedItemWhileLongClick =0;

        private ArrayList<CustomAdapterElement> originalElements;
        private ArrayList<CustomAdapterElement> displayedElements;

        public CustomAdapter(Context contexto, ArrayList<CustomAdapterElement> productionElements){
            this.contexto=contexto;
            originalElements = productionElements;
            displayedElements = productionElements;
            checkCount=1;
            audioChecked = new ArrayList<Boolean>();
            inflater = (LayoutInflater) contexto.getSystemService(contexto.LAYOUT_INFLATER_SERVICE);
        }

        public void changeChechBoxesVisibility(){
            this.checksVisibles=(!checksVisibles);
            setVisible();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return displayedElements.size();
        }

        @Override
        public Object getItem(int i) {
            return displayedElements.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
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
                    displayedElements.get(i).setChecked(true);
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
                            displayedElements.get(i).setChecked(true);
                            contadorSelecciones.setText(checkCount.toString());
                            setVisible();
                        } else {
                            checkCount--;
                            contadorSelecciones.setText(checkCount.toString());
                            displayedElements.get(i).setChecked(false);
                            if (checkCount == 0) {
                                checksVisibles=false;
                                checkCount=1;
                                contadorSelecciones.setText("1");
                                setInvisible();
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
            }else{
                bib.setInvisible();
                boxElminar.setVisibility(View.INVISIBLE);
            }
            if (displayedElements.size()>0) {
                final AudioInfo audioInfo = mapa.get(displayedElements.get(i).getName()); //bib.elementosBiblioteca.get(i).getName()
                if (audioInfo == null) {
                    //no mostramos ese elemento, porque no existe en el mapa..
                    displayedElements.remove(i);
                    notifyDataSetChanged();
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

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint,FilterResults results) {

                    displayedElements = (ArrayList<CustomAdapterElement>) results.values; // has the filtered values
                    notifyDataSetChanged();  // notifies the data with new filtered values
                }

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    ArrayList<CustomAdapterElement> FilteredArrList = new ArrayList<CustomAdapterElement>();

                    if (originalElements == null) {
                        originalElements = new ArrayList<CustomAdapterElement>(displayedElements); // saves the original data in mOriginalValues
                    }

                    /********
                     *
                     *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                     *  else does the Filtering and returns FilteredArrList(Filtered)
                     *
                     ********/
                    if (constraint == null || constraint.length() == 0) {

                        // set the Original result to return
                        results.count = originalElements.size();
                        results.values = originalElements;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        String query = constraint.toString();
                        try {
                            try {
                                audioSearcher = new AudioSearcher (getApplicationContext());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //todo if searcher ==null es que no hay elementos en el indice
                            if ((audioSearcher!=null)&&((search = audioSearcher.search(query)) != null)) {
                                String[] bestDocs = new String[search.scoreDocs.length];
                                int i = 0;
                                for (ScoreDoc doc : search.scoreDocs) {
                                    bestDocs[i] = audioSearcher.getDocument(doc).get(LuceneConstants.FILE_NAME);
                                    i++;
                                }
                                displayedElements = getProductionElementsFilteredByActiveTag(bestDocs);

                            }else{ //no hay nada en el indice
                                displayedElements.clear();
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // set the Filtered result to return
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }
                    return results;
                }
            };
            return filter;
        }

        public void sortDisplayedElements(){
            Collections.sort(displayedElements, new Comparator<CustomAdapterElement>() {
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
            notifyDataSetChanged();
        }
    }
}