package edu.cmu.lti.f14.hw3.hw3_qiqis.annotators;

import edu.cmu.lti.f14.hw3.hw3_qiqis.utils.*;
import edu.cmu.lti.f14.hw3.hw3_qiqis.typesystems.*;
import java.util.*;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_qiqis.typesystems.Document;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}

	/**
   * A basic white-space tokenizer, it deliberately does not split on punctuation!
   *
	 * @param doc input text
	 * @return    a list of tokens.
	 */

	List<String> tokenize0(String doc) {
	  List<String> res = new ArrayList<String>();
	  
	  for (String s: doc.split("\\s+"))
	    res.add(s);
	  return res;
	}

	
	 /**
   * A Stanford Lemmatizer tokenizer, it also deliberately does not split on punctuation!
   *
   * @param doc input text
   * @return    a list of tokens.
   */

  List<String> tokenize1(String doc) {
    StanfordLemmatizer stanfordLemmatizer=new StanfordLemmatizer();
    doc=stanfordLemmatizer.stemText(doc);
    
    List<String> res = new ArrayList<String>();
    
    for (String s: doc.split("\\s+"))
      res.add(s);
    return res;
  }
	
	
	
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		
		//TO DO: construct a vector of tokens and update the tokenList in CAS
    //TO DO: use tokenize0 from abovxe 
		
		//get the tokens from docoments and calculate its frequency by using hashmap
		List<String> stringList= tokenize0(docText);
		
		//Using new tokenize way to get the result
    //List<String> stringList= tokenize1(docText);
		HashMap<String, Integer> hs= new HashMap<String, Integer>();
		for(int i=0;i<stringList.size();i++)
		{
		  if(hs.containsKey(stringList.get(i)))
		  { 
		    hs.put(stringList.get(i), hs.get(stringList.get(i))+1);
		  }
		  else {
		    hs.put(stringList.get(i), 1);
      }
		}
		
	   //traverse hashmap and put this into tokens arraylist
	    List<Token> arrayList=new ArrayList<Token>();
	    for(Map.Entry<String, Integer> entry:hs.entrySet())
	    {
	     Token tokens=new Token(jcas);
	     tokens.setText(entry.getKey());
	     tokens.setFrequency(entry.getValue());
	     arrayList.add(tokens);
	    }
	    
	    //change tokenlist to flist
	    Utils ut=new Utils();   
	   doc.setTokenList(ut.fromCollectionToFSList(jcas,arrayList )) ;
     doc.addToIndexes();
	}

}
