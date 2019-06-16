package com.voicenotes.view.utils.indexes;

import android.content.Context;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.in;

public class AudioSearcher {


    public static final int MAX_SEARCH = 20;

    IndexSearcher indexSearcher;

    Directory indexDir;
    IndexReader reader;

    MultiFieldQueryParser queryParser;

    Context ctx;
    Query query;

    public AudioSearcher (  Context context ) throws IOException, IOException {
ctx = context;
        AudioIndexer.commit();
        System.out.println("AudioSearcher: entro constructor");
        File f = context.getDir("index",0);
        indexDir = FSDirectory.open( f);
        System.out.println("AudioIndexer:construc: indexDir : "+indexDir);
        if (indexDir!=null) {
            //for (String name: indexDir.listAll()) {
            //     indexDir.deleteFile(name);
            // }
            try {
                reader = DirectoryReader.open(indexDir);
            } catch (Exception e) { //ondexNotFoundException
                reader = null;
                System.out.println("AudioSearcher: Entro catch");
                e.printStackTrace();
                //audioIndexer.indexAllTexts();
                // reader = DirectoryReader.open( indexDir );
            }


            //System.out.println("AudioSearcher:reader:"+reader);
            //System.out.println("AudioSearcher:reader.numDOcs:"+reader.numDocs());
            if (reader != null) {

                indexSearcher = new IndexSearcher(reader);

                String[] matchFields = {LuceneConstants.FILE_NAME, LuceneConstants.CONTENTS, LuceneConstants.FILE_PATH};

                queryParser = new MultiFieldQueryParser(
                        Version.LUCENE_41,
                        matchFields,
                        new StandardAnalyzer(Version.LUCENE_41)
                );

            }
        }
    }

    private void reinitializeifnullpointeronquery() {
        File f = ctx.getDir("index", 0);
        try {
            indexDir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            indexDir = FSDirectory.open(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("AudioIndexer:construc: indexDir : " + indexDir);
        if (indexDir != null) {
            //for (String name: indexDir.listAll()) {
            //     indexDir.deleteFile(name);
            // }
            try {
                reader = DirectoryReader.open(indexDir);
            } catch (Exception e) { //ondexNotFoundException
                e.printStackTrace();
                reader = null;
                System.out.println("AudioSearcher: Entro catch");
                e.printStackTrace();
                // audioIndexer.indexAllTexts();
                // reader = DirectoryReader.open( indexDir );
            }


            //System.out.println("AudioSearcher:reader:"+reader);
            //System.out.println("AudioSearcher:reader.numDOcs:"+reader.numDocs());
            if (reader != null) {

                indexSearcher = new IndexSearcher(reader);

                String[] matchFields = {LuceneConstants.FILE_NAME, LuceneConstants.CONTENTS, LuceneConstants.FILE_PATH};

                queryParser = new MultiFieldQueryParser(
                        Version.LUCENE_41,
                        matchFields,
                        new StandardAnalyzer(Version.LUCENE_41)
                );

            }
        }
    }

//old search..change name for search if u want to use this method
    public TopDocs oldSearch (String searchQuery ) throws IOException, ParseException {

        if (reader ==null){
            return null;
        }else {

            query = queryParser.parse(searchQuery);
           // Set<Term> terms = new HashSet<Term>();
            //query.rewrite(reader).extractTerms(terms)
            //FuzzyQuery fz = new FuzzyQuery(terms.toArray());
            return indexSearcher.search(query, MAX_SEARCH);
        }
    }
//fuzzySearch version
    public TopDocs search(String searchString) throws IOException, ParseException {
        // Setup the fields to search through
     //   string[] searchfields = new string[] { "FirstName", "LastName" };

        //if (queryParser==null){
         //   reinitializeifnullpointeronquery();
        //}


        // Build our booleanquery that will be a combination of all the queries for each individual search term
        BooleanQuery finalQuery = new BooleanQuery();
       // var parser = new MultiFieldQueryParser(Lucene.Net.Util.Version.LUCENE_29, searchfields, CreateAnalyzer());

        // Split the search string into separate search terms by word
        String[] terms = searchString.split(" ");
        for  (String term : terms) {
            try {
                finalQuery.add(queryParser.parse(term.replace("~", "") + "~"), BooleanClause.Occur.MUST);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return indexSearcher.search(finalQuery, MAX_SEARCH);
        // Perform the search
      //  var directory = FSDirectory.Open(new DirectoryInfo(LuceneIndexBaseDirectory));
       // var searcher = new IndexSearcher(directory, true);
        //var hits = searcher.Search(finalQuery, MAX_RESULTS);
    }

    public Document getDocument (ScoreDoc scoreDoc ) throws IOException {

        return indexSearcher.doc( scoreDoc.doc );

    }

    public void close() throws IOException {

        reader.close();

    }
}
