package lucene;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
//import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IndexFiles {

	  private IndexFiles() {}

	  /** Index all text files under a directory. */
	  public static void main(String[] args) {
	
	    //EdW paei to source tou index
	    
	    String indexPath = "Index";
	    // Edw fortwnoume ta data 
	    
	    String docsPath = "Documents";
	    boolean create = true;
	
	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }

	    Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + indexPath + "'...");
	      
	      Directory dir = FSDirectory.open(Paths.get(indexPath));
	      Analyzer analyzer = new StandardAnalyzer();
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
	      //Tokenizer
	      if (create) {
	        // Create a new index in the directory, removing any
	        // previously indexed documents:
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        // Add new documents to an existing index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }

	      // Optional: for better indexing performance, if you
	      // are indexing many documents, increase the RAM
	      // buffer.  But if you do this, increase the max heap
	      // size to the JVM (eg add -Xmx512m or -Xmx1g):
	      //
	      // iwc.setRAMBufferSizeMB(256.0);
	      /*****WHERE WE WRITE THE INDWX*****/
	      IndexWriter writer = new IndexWriter(dir, iwc);
	      indexDocs(writer, docDir);

	      // NOTE: if you want to maximize search performance,
	      // you can optionally call forceMerge here.  This can be
	      // a terribly costly operation, so generally it's only
	      // worth it when your index is relatively static (ie
	      // you're done adding documents to it):
	      //
	      // writer.forceMerge(1);

	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	  }

	 
	  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
	    if (Files.isDirectory(path)) {
	      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	          try {
	            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
	          } catch (IOException ignore) {
	            // don't index files that can't be read.
	          }
	          return FileVisitResult.CONTINUE;
	        }
	      });
	    } else {
	      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
	    }
	  }

	  /** Indexes a single document 
	   *  EDW PREPEI NA KANOYME TO TOKENIZER
	   * Diavazei ena ena ta arxeia
	   * 
	   * */
	  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
	    try (InputStream stream = Files.newInputStream(file)) {
	      // make a new, empty document
	        //System.out.println("at " + file);
	    	  FileReader Efile=new FileReader(file.toString());
				BufferedReader br = new BufferedReader(Efile);
				String line;
				while((line=br.readLine())!=null){
	      Document doc = new Document();

	      // Add the path of the file as a field named "path".  Use a
	      // field that is indexed (i.e. searchable), but don't tokenize
	      // the field into separate words and don't index term frequency
	      // or positional information:
	      Field pathField = new StringField("path", file.toString(), Field.Store.YES);
	      doc.add(pathField);
	      
	      // Use a LongPoint that is indexed (i.e. efficiently filterable with
	      // PointRangeQuery).  This indexes to milli-second resolution, which
	      // is often too fine.  You could instead create a number based on
	      // year/month/day/hour/minutes/seconds, down the resolution you require.
	      // For example the long value 2011021714 would mean
	      // February 17, 2011, 2-3 PM.
	      doc.add(new LongPoint("modified", lastModified));

	      // Add the contents of the file to a field named "contents".  Specify a Reader,
	      // so that the text of the file is tokenized and indexed, but not stored.
	      // Note that FileReader expects the file to be in UTF-8 encoding.
	      // If that's not the case searching for special characters will fail.
	    
				//System.out.println(line);
			
			JSONObject jsonObject = new JSONObject(line);
			String name = (String) jsonObject.get("text");
			//System.out.println(name);
			//System.exit(0);  // is it necessary? And when it must be called? 
			 doc.add(new TextField("content",name,  Field.Store.YES));
			 JSONObject Entities=jsonObject.getJSONObject("entities");
			 //JSONObject hash=(JSONObject) Entities.get("hashtags");
			 //String hashtags = (String) hash.get("text");
			 
			// System.out.println(Entities.get("hashtags"));
			 //JSONObject t= new JSONObject(Entities.getJSONArray("hashtags"));
			 //System.out.println(t);
			 JSONArray test =new JSONArray(Entities.get("hashtags").toString());
			 for(int i=0;i < test.length();i++){
				 JSONObject jsonobj = test.getJSONObject(i);
				 //System.out.println(jsonobj.get("text"));
				 String hash = (String) jsonobj.get("text");
				 doc.add(new TextField("hashtags",hash,  Field.Store.YES));
			 }
			 JSONObject User=(JSONObject) jsonObject.get("user");
			 String username = (String) User.get("name");
			 String created= (String) jsonObject.get("created_at");
			 String favorite= (String) jsonObject.get("favorite_count").toString();
			 String ret= (String) jsonObject.get("retweet_count").toString();
			 
			 String location = (String) User.get("location");
			 doc.add(new TextField("author",username,  Field.Store.YES));
			 doc.add(new TextField("location",location,  Field.Store.YES));
			
			 doc.add(new TextField("created_at",created,  Field.Store.YES));
			 doc.add(new TextField("favorite_count",favorite,  Field.Store.YES));
			 doc.add(new TextField("retweet_count",ret,  Field.Store.YES));	   

	      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	        // New index, so we just add the document (no old document can be there):
	       // System.out.println("adding " + file);
	        writer.addDocument(doc);
	      } else {
	        // Existing index (an old copy of this document may have been indexed) so
	        // we use updateDocument instead to replace the old one matching the exact
	        // path, if present:
	        System.out.println("updating " + file);
	        writer.updateDocument(new Term("path", file.toString()), doc);
	      }
			}
	    }catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("adding " + file);
	  }
	}
