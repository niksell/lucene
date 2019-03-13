package lucene;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButtonMenuItem;
import java.awt.Color;
import javax.swing.JList;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.JTable;
import java.awt.ScrollPane;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Window.Type;

public class gui {

	private JFrame frmGoogre;
	private JTextField textField;
	private  		JEditorPane editorPane = new JEditorPane();
	public static  String index = "Index";
	public static String queries = null;
	public static Map <Date,Document> treeMap=new TreeMap<Date,Document>(Collections.reverseOrder());
	public static Map <Integer,Document> favoriteMap=new TreeMap<Integer,Document>(Collections.reverseOrder());
	public static Map <Integer,String> suggestions=new TreeMap<Integer,String>(Collections.reverseOrder());
	public static ArrayList<String> History = new ArrayList<>();

	public static ArrayList<String> myList = new ArrayList<>();
	public static String hash="";
	public static String Text="";
	public static String authr="";
	public static String loc="";
	public String change="";
    ArrayList<Word> words = new ArrayList<Word>();
    private JTextField textField_1;

	
	
	    // Get suggestions given a prefix and a region.
	    private static String lookup(AnalyzingInfixSuggester suggester, String word,String region) {
	        try {
	            List<Lookup.LookupResult> results;
	            HashSet<BytesRef> contexts = new HashSet<BytesRef>();
	            contexts.add(new BytesRef(region.getBytes("UTF8")));
	            // Do the actual lookup.  We ask for the top 2 results.
	            results = suggester.lookup(word, contexts, 5, true, false);
	            System.out.println("-- \"" + word + "\" (" + region + "):");
	            for (Lookup.LookupResult result : results) {
	                System.out.println(result.key);
	                Word p = getWord(result);
	                if (p != null) {
	                   return p.word;
	                }
	            }
	        } catch (IOException e) {
	            System.err.println("Error");
	        }
			return word;
	    }

	    // Deserialize a Product from a LookupResult payload.
	    private static Word getWord(Lookup.LookupResult result)
	    {
	        try {
	            BytesRef payload = result.payload;
	            if (payload != null) {
	                ByteArrayInputStream bis = new ByteArrayInputStream(payload.bytes);
	                ObjectInputStream in = new ObjectInputStream(bis);
	                Word p = (Word) in.readObject();
	                return p;
	            } else {
	                return null;
	            }
	        } catch (IOException|ClassNotFoundException e) {
	            throw new Error("Could not decode payload :(");
	        }
	    }
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui window = new gui();
					window.frmGoogre.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @return 
	 * @throws IOException 
	 */
	public String backDoor(String word) throws IOException{
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();

	    String docsPath = "resources/pop.json";
	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    try (InputStream stream = Files.newInputStream(docDir)) {
		
		    	  FileReader Efile=new FileReader(docDir.toString());
					BufferedReader br = new BufferedReader(Efile);
					String line;
					while((line=br.readLine())!=null){
		  
				
						JSONObject jsonObject = new JSONObject(line);
						Iterator<?> keys = jsonObject.keys();
						while( keys.hasNext() ) {
						    String key = (String)keys.next();
						    key=key.replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ").trim();
							if(key.contains("http") || key.contains("@") || stopWords.contains(key) || key.contains("RT")){
								continue;
							}else{
								 if ( jsonObject.has(key)) {
									if((Integer)jsonObject.get(key)!=0){
										suggestions.put((Integer)jsonObject.get(key),key);
									}
								 }
							}
						   

						   
						}
						
				//		System.out.println(suggestions);
				
				
		      
				}
					 RAMDirectory index_dir1 = new RAMDirectory();
			          StandardAnalyzer analyzer1 = new StandardAnalyzer();
			          AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester( index_dir1, analyzer1);	
						for(Map.Entry<Integer,String> entry : suggestions.entrySet()){
							words.add(new Word(entry.getValue(),"US",entry.getKey()));
						}
			            suggester.build(new WordIterator(words.iterator()));
			            
			            
			            return lookup(suggester, word, "US");
					
		    }catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return word;
	}
	public gui() {
		initialize();
		try {
			backDoor("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGoogre = new JFrame();
		frmGoogre.setTitle("GOORGE");
		frmGoogre.setForeground(new Color(255, 240, 245));
		frmGoogre.setBackground(Color.BLACK);
		frmGoogre.getContentPane().setBackground(new Color(255, 240, 245));
		frmGoogre.setBounds(100, 100, 1200, 772);
		frmGoogre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGoogre.getContentPane().setLayout(null);
		
		textField = new JTextField();
		DeferredDocumentListener listener = new DeferredDocumentListener(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Execute your required functionality here...
            	try {
            		textField_1.setText(backDoor(textField.getText()).toLowerCase());
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            }
        }, true);
		textField.getDocument().addDocumentListener(listener);
		textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                listener.start();
            }

            @Override
            public void focusLost(FocusEvent e) {
                listener.stop();
            }
        });

			
		
		
		textField.setBackground(new Color(255, 255, 255));
		textField.setForeground(new Color(255, 69, 0));
		textField.setBounds(10, 11, 731, 29);
		frmGoogre.getContentPane().add(textField);
		textField.setColumns(10);
	    DefaultListModel<String> model = new DefaultListModel<>();

	    JList list = new JList(model);
		JRadioButton rdbtnText = new JRadioButton("text");
		rdbtnText.setBackground(new Color(255, 240, 245));
		rdbtnText.setForeground(new Color(0, 0, 0));
		rdbtnText.setBounds(531, 58, 73, 23);
		frmGoogre.getContentPane().add(rdbtnText);
		
		JRadioButton rdbtnLocation = new JRadioButton("location");
		rdbtnLocation.setBackground(new Color(255, 240, 245));
		rdbtnLocation.setForeground(new Color(0, 0, 0));
		rdbtnLocation.setBounds(401, 58, 89, 23);
		frmGoogre.getContentPane().add(rdbtnLocation);
		
		JRadioButton rdbtnAuthor = new JRadioButton("author");
		rdbtnAuthor.setBackground(new Color(255, 240, 245));
		rdbtnAuthor.setForeground(new Color(0, 0, 0));
		rdbtnAuthor.setBounds(644, 58, 97, 23);
		frmGoogre.getContentPane().add(rdbtnAuthor);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 88, 672, 2);
		frmGoogre.getContentPane().add(separator);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(204, 88, 672, 650);
		frmGoogre.getContentPane().add(scrollPane);
		scrollPane.setViewportView(editorPane);
		editorPane.setContentType("\r\ntext/html\r\n");
		
		JRadioButton rdbtnHashtags = new JRadioButton("Hashtags");
		rdbtnHashtags.setForeground(new Color(0, 0, 0));
		rdbtnHashtags.setBackground(new Color(255, 240, 245));
		rdbtnHashtags.setBounds(281, 58, 84, 23);
		frmGoogre.getContentPane().add(rdbtnHashtags);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnText);
		group.add(rdbtnLocation);
		group.add(rdbtnAuthor);
		group.add(rdbtnHashtags);
		
		ButtonGroup groupCheck = new ButtonGroup();
		
	    
		JButton btnSearch = new JButton("Search");
		btnSearch.setBackground(new Color(255, 240, 245));
		btnSearch.setForeground(new Color(0, 0, 0));
		btnSearch.setBounds(751, 14, 89, 23);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				try {
					History.add(textField.getText());
					model.clear();
					for(int i=0;i<History.size();i++){
						model.addElement(History.get(i));
					}
					sentChoises(textField.getText(),rdbtnLocation.isSelected(),rdbtnAuthor.isSelected(),rdbtnHashtags.isSelected(),rdbtnText.isSelected());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		frmGoogre.getContentPane().add(btnSearch);
	    
	    JButton btnAdvance = new JButton("advanced");
	    btnAdvance.setBackground(new Color(255, 240, 245));
	    btnAdvance.setForeground(Color.RED);
	    btnAdvance.setBounds(751, 48, 89, 17);
	    frmGoogre.getContentPane().add(btnAdvance);
	    
	    JCheckBox chckbxDate = new JCheckBox("Date");
	    chckbxDate.setBackground(new Color(255, 240, 245));
	    
	    chckbxDate.setBounds(54, 135, 97, 23);
	    frmGoogre.getContentPane().add(chckbxDate);
	    
	    JCheckBox chckbxFavorites = new JCheckBox("Favorites");
	    chckbxFavorites.setBackground(new Color(255, 240, 245));
	    chckbxFavorites.setBounds(54, 207, 97, 23);
	    frmGoogre.getContentPane().add(chckbxFavorites);
	    
	    JCheckBox chckbxNewCheckBox = new JCheckBox("Score");
	    chckbxNewCheckBox.setBackground(new Color(255, 240, 245));
	    chckbxNewCheckBox.setBounds(54, 167, 97, 23);
	    frmGoogre.getContentPane().add(chckbxNewCheckBox);
	    
	    groupCheck.add(chckbxDate);
	    groupCheck.add(chckbxFavorites);
	    groupCheck.add(chckbxNewCheckBox);
	    chckbxDate.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		try {
					changeOrder(chckbxDate.isSelected(),chckbxFavorites.isSelected(),chckbxNewCheckBox.isSelected());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    });
	    chckbxFavorites.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		try {
					changeOrder(chckbxDate.isSelected(),chckbxFavorites.isSelected(),chckbxNewCheckBox.isSelected());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    });
	    chckbxNewCheckBox.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		try {
					changeOrder(chckbxDate.isSelected(),chckbxFavorites.isSelected(),chckbxNewCheckBox.isSelected());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    });
	    JLabel lblArrangeBy = new JLabel("Arrange by ");
	    lblArrangeBy.setBounds(54, 101, 97, 14);
	    frmGoogre.getContentPane().add(lblArrangeBy);
	    
	    textField_1 = new JTextField();
	    textField_1.setBounds(110, 59, 169, 20);
	    frmGoogre.getContentPane().add(textField_1);
	    textField_1.setColumns(10);
	    
	    JLabel lblDidYouMean = new JLabel("Did You Mean:");
	    lblDidYouMean.setBounds(10, 62, 90, 14);
	    frmGoogre.getContentPane().add(lblDidYouMean);
	
	    list.setBounds(22, 280, 169, 325);
	    frmGoogre.getContentPane().add(list);
	    
	    JLabel lblRecentHistoy = new JLabel("Recent History");
	    lblRecentHistoy.setBounds(54, 254, 97, 14);
	    frmGoogre.getContentPane().add(lblRecentHistoy);
	    btnAdvance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					History.add(textField.getText());
					model.clear();
					for(int i=0;i<History.size();i++){
						model.addElement(History.get(i));
					}
					advanceSearch(textField.getText());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	 


	}
	public void advanceSearch(String input) throws Exception{
		String names[]={"jimmy fallon",
		"donald J. trump",
		"barack obama",
		"leonardo dicaprio",
		"ellen degeneres"};
		String locations[]={"washington, dc",
				"los angeles, CA ",
				"new york",
				"california"};
		String test[]=input.trim().toLowerCase().split("\\s+");
	
		int repeat = 0;
	    boolean raw = false;
	    int hitsPerPage = 10;
	    change=input;
	    //SearchFiles t=new SearchFiles();
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser author = new QueryParser("author", analyzer);
	    QueryParser location = new QueryParser("location", analyzer);

	    QueryParser content = new QueryParser("content", analyzer);
	    QueryParser hashtag = new QueryParser("hashtags", analyzer);

	    String line = input;
	     hash="";
	     Text="";
	     authr="";
	     loc="";
	    line = line.trim();


	      Query queryA = null ;
	      Query queryL = null ;
	      Query queryH = null ;
	      Query queryC = null ;
	      Query q2=null;
	      boolean flagA=false;
	      boolean flagL=false;
	      boolean flagH=false;
	      boolean flagC=false;
		for(int i=0;i<test.length;i++){
			if(test[i].charAt(0)=='#'){
				System.out.println("first"+test[i]);
				hash=test[i].trim();
				
			}else if(names[0].contains(test[i]) || names[1].contains(test[i]) || names[2].contains(test[i]) || names[3].contains(test[i])){
				System.out.println("second"+test[i]);
				authr=test[i];
			}else if(locations[0].contains(test[i]) || locations[1].contains(test[i]) || locations[2].contains(test[i]) || locations[3].contains(test[i])){
				System.out.println("third"+test[i]);
				loc=test[i];
			}else{
				System.out.println("four"+test[i]);
				Text=Text+" "+test[i];
			}
			
		}
		
		Text=Text.trim();
		if(!hash.equals("")){
			flagH=true;
			hash=hash.replace("#", "");
			queryH=hashtag.parse(hash);
		}
		if(!authr.equals("")){
			flagA=true;
			queryA=author.parse(authr);
		}
		if(!loc.equals("")){
			flagL=true;

			queryL=location.parse(loc);	
		}
		if(!Text.equals("")){
			flagC=true;
			queryC=content.parse(Text);
		}

		if(flagA&&flagC&&flagH&&flagL){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    .add(queryH, BooleanClause.Occur.SHOULD)
			    	    .add(queryC, BooleanClause.Occur.MUST)
			    	    .add(queryA, BooleanClause.Occur.MUST)
			    	    .add(queryL, BooleanClause.Occur.MUST)
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagA&&flagC&&flagH){
			BooleanQuery booleanQuery = new BooleanQuery.Builder()
		    	    .add(queryH, BooleanClause.Occur.MUST)
		    	    .add(queryC, BooleanClause.Occur.MUST)
		    	    .add(queryA, BooleanClause.Occur.MUST)
		    	    .build();
			 q2 = new ConstantScoreQuery(booleanQuery);
		}else if(flagA&&flagH&&flagL){
			BooleanQuery booleanQuery = new BooleanQuery.Builder()
		    	    .add(queryH, BooleanClause.Occur.SHOULD)
		    	    .add(queryA, BooleanClause.Occur.MUST)
		    	    .add(queryL, BooleanClause.Occur.MUST)
		    	    .build();
			 q2 = new ConstantScoreQuery(booleanQuery);
		}else if(flagC&&flagH&&flagL){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    .add(queryH, BooleanClause.Occur.SHOULD)
			    	    .add(queryC, BooleanClause.Occur.MUST)
			    	    .add(queryL, BooleanClause.Occur.MUST)
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagA&&flagC&&flagL){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    .add(queryC, BooleanClause.Occur.MUST)
			    	    .add(queryA, BooleanClause.Occur.MUST)
			    	    .add(queryL, BooleanClause.Occur.MUST)
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagA&&flagC){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    
			    	    .add(queryC, BooleanClause.Occur.MUST)
			    	    .add(queryA, BooleanClause.Occur.MUST)
			    	    
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagA&&flagH){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    .add(queryH, BooleanClause.Occur.MUST)
			    	    
			    	    .add(queryA, BooleanClause.Occur.SHOULD)
			    	    
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagA&&flagL){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    
			    	    .add(queryA, BooleanClause.Occur.MUST)
			    	    .add(queryL, BooleanClause.Occur.MUST)
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagC&&flagH){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    .add(queryH, BooleanClause.Occur.MUST)
			    	    .add(queryC, BooleanClause.Occur.MUST)
			    	    
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagC&&flagL){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    
			    	    .add(queryC, BooleanClause.Occur.MUST)
			    	    
			    	    .add(queryL, BooleanClause.Occur.MUST)
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}else if(flagH&&flagL){
			 BooleanQuery booleanQuery = new BooleanQuery.Builder()
			    	    .add(queryH, BooleanClause.Occur.SHOULD)
			    	    
			    	    .add(queryL, BooleanClause.Occur.MUST)
			    	    .build();
				 q2 = new ConstantScoreQuery(booleanQuery);

		}
		
			
		
	   
		//Query q2 = new ConstantScoreQuery(booleanQuery);
	    if (repeat > 0) {                           // repeat & time as benchmark
	        Date start = new Date();
	        for (int i = 0; i < repeat; i++) {
	          searcher.search(q2, 100);
	        }
	        Date end = new Date();
	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
	      }
	      
	   // Collect enough docs to show 5 pages
	      TopDocs results = searcher.search(q2, 5 * hitsPerPage);
	      ScoreDoc[] hits = results.scoreDocs;
	      
	      int numTotalHits = results.totalHits;
	      System.out.println(numTotalHits + " total matching documents");

	      int start = 0;
	      int end = Math.min(numTotalHits, hitsPerPage);
	     myList.clear();

	
		    int page=0;
		    if(numTotalHits>5 * 10){
		    	page=5 * 10;
		    }else{
		    	page=numTotalHits;
		    }
	        treeMap.clear();
	        favoriteMap.clear();


		    for (int i = start; i < page; i++) {
		        

		        Document doc = searcher.doc(hits[i].doc);
		        Date time=getDate(doc.get("created_at"));
		        treeMap.put(time, doc);
		        int count=Integer.parseInt(doc.get("favorite_count"));
		        favoriteMap.put(count, doc);
		        String path = doc.get("path");
		        if (path != null) {
		          String text1 = doc.get("content");
		     
		
		          if (text1 != null) {
		           
		        	   	  myList.add("<p>"+(i+1) + ".   Author: " + doc.get("author").toLowerCase()+"<br>"
			            		+"   location: " + doc.get("location").toLowerCase()+"<br>"
			            				+"<br>"+"<font style=\" color:#83a7ff \">"
			            		+"   created_at: " + doc.get("created_at").toLowerCase()
			            		+		"</font>"+"<br"+"<font style=\" color:#8300ff \">"
			            		+"   Text: " + doc.get("content").toLowerCase()+		"</font>"+"<br"
		        	  			+"<br>"+"<font style=\" color:#ff66cc \">"+"   retweets: " + doc.get("retweet_count")+ "</font>"+"  "+"<font style=\" color:#ff66cc \">"+"   favorites: " + doc.get("favorite_count")+ "</font>"+"\n"+"</p>"+"<hr >");

		        	  
		        	        
		          }
		        } else {
		          System.out.println((i+1) + ". " + "No path for this document");
		        }
		        
		              
		      }
		    String temp[]=input.trim().split("\\s+");
		    for(int j=0;j<temp.length;j++){
		    	for(int i=0;i<end;i++){
		    	
		    	myList.set(i, myList.get(i).replaceAll(temp[j], "<font style=\" color:#ff0000 \">" + temp[j] + "</font>"));
		    	}
		    }
		    String Text1=numTotalHits + " total matching documents\n\r";
		    for(int i=0;i<end;i++){
	        	Text1=Text1+myList.get(i);
	        }

		    editorPane.setEditable(false);
		    editorPane.setContentType("text/html");
		    editorPane.setText(Text1);
		    editorPane.setCaretPosition(0);

		    reader.close();
		
	}
	public void sentChoises(String input,boolean location,boolean author,boolean hashtags, boolean text) throws Exception{
		String choise="";
		change=input;

	    //SearchFiles t=new SearchFiles();
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();
		QueryParser author_1 = new QueryParser("author", analyzer);
	    QueryParser location_1 = new QueryParser("location", analyzer);

	    QueryParser content = new QueryParser("content", analyzer);
	    QueryParser hashtag = new QueryParser("hashtags", analyzer);
	    Query queryA = null ;
	    Query queryL = null ;
	    Query queryH = null ;
	    Query queryC = null ;
	    Query q2=null;
	    int temp1=0;
		if(text){
			choise="content";
			
		}
		else if(hashtags){
			choise="hashtags";
			
		}
		else if(author){
			choise="author";
			
		}
		else if(location){
			choise="location";
		}else{
			
			queryA=author_1.parse(input.trim());
			queryL=location_1.parse(input.trim());
			queryH=hashtag.parse(input.trim());
			queryC=content.parse(input.trim());
			BooleanQuery booleanQuery = new BooleanQuery.Builder()
		    	    .add(queryH, BooleanClause.Occur.SHOULD)
		    	    .add(queryC, BooleanClause.Occur.SHOULD)
		    	    .add(queryA, BooleanClause.Occur.SHOULD)
		    	    .add(queryL, BooleanClause.Occur.SHOULD)
		    	    .build();
			 q2 = new ConstantScoreQuery(booleanQuery);
			 System.out.println("mphka");
			 temp1=1;
		}

	    int repeat = 0;
	    boolean raw = false;
	    int hitsPerPage = 10;

	    
	    
	    QueryParser parser = new QueryParser(choise, analyzer);
	    String line = input;
	    
	    line = line.trim();

	
	      Query query = parser.parse(line);
	      System.out.println("Searching for: " + query.toString(choise));
	            
	      if (repeat > 0) {                           // repeat & time as benchmark
	        Date start = new Date();
	        	for (int i = 0; i < repeat; i++) {
		        		searcher.search(query, 100);
		        
		        }
	        
	        
	        Date end = new Date();
	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
	      }
	      
	   // Collect enough docs to show 5 pages
	      ScoreDoc[] hits=null;
	      int numTotalHits=0;
	      if(temp1==0){
		      TopDocs results = searcher.search(query, 5 * hitsPerPage);
		      hits = results.scoreDocs;
		      
		       numTotalHits = results.totalHits;

	      }else{
		      TopDocs results = searcher.search(q2, 5 * hitsPerPage);
		      hits = results.scoreDocs;
		      
		       numTotalHits = results.totalHits;

	      }
	      
	
	      System.out.println(numTotalHits + " total matching documents");

	      int start = 0;
	      int end = Math.min(numTotalHits, hitsPerPage);

		myList.clear();

	    System.out.println("end is "+end);
	
	    int page=0;
	    if(numTotalHits>5 * 10){
	    	page=5 * 10;
	    }else{
	    	page=numTotalHits;
	    }
        treeMap.clear();
        favoriteMap.clear();

	    for (int i = start; i < page; i++) {
	        
	    	
	        Document doc = searcher.doc(hits[i].doc);
	        Date time=getDate(doc.get("created_at"));
	        treeMap.put(time, doc);
	        int count=Integer.parseInt(doc.get("favorite_count"));
	        favoriteMap.put(count, doc);
	        String path = doc.get("path");
	        if (path != null) {
	          String text1 = doc.get("content");
	     

	          if (text1 != null) {
	        	  myList.add("<p>"+(i+1) +"<font style=\" color:#003300 \">"+ ".   Author: " + doc.get("author").toLowerCase()+"</font>"+"<br>"
	        			  +"<font style=\" color:#003300 \">"+"   location: " + doc.get("location").toLowerCase()+"</font>"+"<br>"
		            				+"<br>"+"<font style=\" color:#83a7ff \">"
		            		+"   created_at: " + doc.get("created_at").toLowerCase()
		            		+		"</font>"+"<br"+"<font style=\" color:#8300ff \">"
		            		+"   Text: " + doc.get("content").toLowerCase()+		"</font>"+"<br"
	        	  			+"<br>"+"<font style=\" color:#ff66cc \">"+"   retweets: " + doc.get("retweet_count")+ "</font>"+"  "+"<font style=\" color:#ff66cc \">"+"   favorites: " + doc.get("favorite_count")+ "</font>"+"\n"+"</p>"+"<hr >");

	        	
	        	        
	          }
	        } else {
	          System.out.println((i+1) + ". " + "No path for this document");
	        }
	        
	              
	      }
	    String Text=numTotalHits + " total matching documents\n\r";
	    String temp[]=input.trim().split("\\s+");
	    for(int j=0;j<temp.length;j++){
	    	for(int i=0;i<myList.size();i++){
	    	
	    	myList.set(i, myList.get(i).replaceAll(temp[j], "<font style=\" color:#ff0000 \">" + temp[j] + "</font>"));
	    	}
	    }
	    for(int i=0;i<myList.size();i++){
	    	
        	Text=Text+myList.get(i);
        }
	
	    
	    editorPane.setEditable(false);
	    editorPane.setContentType("text/html");
	    editorPane.setText(Text);
	    editorPane.setCaretPosition(0);

	    reader.close();

		
	}
	public void changeOrder(boolean date,boolean favorite,boolean score) throws Exception{
		String DateText="";
		if(date){
			
			int i=0;
			ArrayList<String> myDate = new ArrayList<>();
			for(Map.Entry<Date,Document> entry : treeMap.entrySet()){
				Document doc=entry.getValue();

			
		          myDate.add("<p>"+(i+1) + ".   Author: " + doc.get("author").toLowerCase()+"<br>"
			            		+"   location: " + doc.get("location").toLowerCase()+"<br>"
			            				+"<br>"+"<font style=\" color:#83a7ff \">"
			            		+"   created_at: " + doc.get("created_at").toLowerCase()
			            		+		"</font>"+"<br"+"<font style=\" color:#8300ff \">"
			            		+"   Text: " + doc.get("content").toLowerCase()+		"</font>"+"<br"
		        	  			+"<br>"+"<font style=\" color:#ff66cc \">"+"   retweets: " + doc.get("retweet_count")+ "</font>"+"  "+"<font style=\" color:#ff66cc \">"+"   favorites: " + doc.get("favorite_count")+ "</font>"+"\n"+"</p>"+"<hr >");
		          
		        	  i++;
			}
			String temp[]=change.trim().split("\\s+");
			for(int j=0;j<temp.length;j++){
		    	for(int c=0;c<myDate.size();c++){
		    	
		    		myDate.set(c, myDate.get(c).replaceAll(temp[j], "<font style=\" color:#ff0000 \">" + temp[j] + "</font>"));
		    	}
		    }
			for(int k=0;k<myDate.size();k++){
				DateText=DateText+myDate.get(k);
			}

		}else if(favorite){
			int i=0;
			ArrayList<String> myDate = new ArrayList<>();
			for(Map.Entry<Integer,Document> entry : favoriteMap.entrySet()){
				Document doc=entry.getValue();

				
		          myDate.add("<p>"+(i+1) + ".   Author: " + doc.get("author").toLowerCase()+"<br>"
			            		+"   location: " + doc.get("location").toLowerCase()+"<br>"
			            				+"<br>"+"<font style=\" color:#83a7ff \">"
			            		+"   created_at: " + doc.get("created_at").toLowerCase()
			            		+		"</font>"+"<br"+"<font style=\" color:#8300ff \">"
			            		+"   Text: " + doc.get("content").toLowerCase()+		"</font>"+"<br"
		        	  			+"<br>"+"<font style=\" color:#ff66cc \">"+"   retweets: " + doc.get("retweet_count")+ "</font>"+"  "+"<font style=\" color:#ff66cc \">"+"   favorites: " + doc.get("favorite_count")+ "</font>"+"\n"+"</p>"+"<hr >");
		          
		        	  i++;
			}
			String temp[]=change.trim().split("\\s+");
			for(int j=0;j<temp.length;j++){
		    	for(int k=0;k<myDate.size();k++){
		    	
		   		myDate.set(k, myDate.get(k).replaceAll(temp[j], "<font style=\" color:#ff0000 \">" + temp[j] + "</font>"));
		    	}
		    }
			for(int k=0;k<myDate.size();k++){
				DateText=DateText+myDate.get(k);
			}
		}else{
			String temp[]=change.trim().split("\\s+");
			for(int j=0;j<temp.length;j++){
		    	for(int k=0;k<myList.size();k++){
		    	
		    		myList.set(k, myList.get(k).replaceAll(temp[j], "<font style=\" color:#ff0000 \">" + temp[j] + "</font>"));
		    	}
		    }
			for(int k=0;k<myList.size();k++){
				
				DateText=DateText+myList.get(k);
			}
		}
		
		editorPane.setEditable(false);
	    editorPane.setContentType("text/html");
	    editorPane.setText(DateText);
	    editorPane.setCaretPosition(0);
	}
	public static Date getDate(String date) throws ParseException{
		final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		  SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
		  sf.setLenient(true);
		  return sf.parse(date);
	}
}
