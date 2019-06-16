package com.voicenotes.view.utils.indexes;

import android.content.Context;
import android.util.Log;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class AudioIndexer {

    public static final String LOG_TAG = "AudioIndexer";

    private static IndexWriter writer;

    private static  Analyzer analyzer;
    private static Directory indexDir;
    private static  IndexWriterConfig iwc;
    static File f;
    private String textsPath=    "/storage/sdcard1/Music/texts/"  ;
static Context context;
    public static void initialize( Context context2 )  {
        System.out.println("AudioIndexer: entro constructor");
        Log.v( LOG_TAG, "Createing index at " );
        context=context2;
        f  = context.getDir( "index", 0);

        Directory indexDir = null; //file replaced by path (nio package)
        try {
            indexDir = FSDirectory.open( f );
           for (String name: indexDir.listAll()) {
               //indexDir.deleteFile(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        analyzer = new StandardAnalyzer( Version.LUCENE_41 );
        iwc = new IndexWriterConfig( Version.LUCENE_41,  analyzer );
        //default configuration for index. This cannot be changed after index is created using this object. For any changes we will need to getConfig from the index writer.
        iwc.setOpenMode( IndexWriterConfig.OpenMode.CREATE_OR_APPEND ); // only create removes previous index. create or append adds to
        iwc.setRAMBufferSizeMB( 16.0 ); //
        try {
            writer = new IndexWriter( indexDir, iwc );
        } catch ( Exception e ) {
            Log.e ( LOG_TAG, e.getMessage(),  e );
        }
    }

    public static void deleteFromIndex(String name){

        try {
            f  = context.getDir( "index", 0);

            Directory indexDir = null; //file replaced by path (nio package)
            try {
                indexDir = FSDirectory.open( f );
                //   for (String name: indexDir.listAll()) {
                //       indexDir.deleteFile(name);
                //  }
            } catch (IOException e) {
                e.printStackTrace();
            }/*
            for (String item: indexDir.listAll()) {
                System.out.println("AudioIndexer: delete: item: "+item);
                if (item == name) {
                    indexDir.deleteFile(name);
                }
            }*/
            Term term = new Term(LuceneConstants.FILE_NAME, name);
            writer.deleteDocuments(term);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() throws IOException {

        writer.close();

    }
    public static Document getDocument(File file) throws IOException {
        System.out.println("AudioIndexer: getDocument: "+file.getPath());
        Document document = new Document();

        //index file contents
        Field contentField = new Field(LuceneConstants.CONTENTS,
                new FileReader(file));

        //index file name
        Field fileNameField = new Field(LuceneConstants.FILE_NAME,
                file.getName(),
                Field.Store.YES,Field.Index.NOT_ANALYZED);

        //index file path
        Field filePathField = new Field(LuceneConstants.FILE_PATH,
                file.getCanonicalPath(),
                Field.Store.YES,Field.Index.NOT_ANALYZED);

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);

        return document;
    }
    /*
    public void indexAllTexts(){
        System.out.println("AudioIndexer: indexAllTexts, mapKeys: "+ AudioMap.getkeys());
        for (String key: AudioMap.getkeys()){
            this.indexDoc(AudioMap.getAudioInfo(key).getDoc());
        }
    }*/


    public void createIndex(Context context) {

        // if diease is disabled, don't index

      //  Map<String, Disease> diseases = DiseaseManager.getInstance().getDiseases();

        //Set<String> diseaseKeys =  diseases.keySet();

       // for ( String key: diseaseKeys ) {

        //    Disease disease = diseases.get( key );

        //    if ( disease.isDisabled() )
        //        continue;

         //   indexDisease( context, disease );

       // }

    }



    public static  void indexDoc(  Document doc ) {
//doc.getFields().get(0).stringValue()
        Log.v(LOG_TAG, "Indexing audio: "  );

        try {

            System.out.println("lucene: entro indexDoc");

          //  if ( writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE ) {
                System.out.println("lucene: entro create");

                writer.addDocument(doc);
                writer.commit();

          //  } else { //proximamente

              //  Term idTerm = new Term( DiseaseLuceneFieldNames.ID.getStr(), disease.getFolder() );

               // writer.updateDocument(  idTerm , doc );

          //  }

        } catch ( IOException e ) {

            Log.e( LOG_TAG, "Section index failed: ", e );

            return;
        }


    }


    public static void commit(){
        if (writer!=null) {
            try {
                writer.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
