package edu.cmu.lti.f14.hw3.hw3_qiqis.casconsumers;

import java.awt.BufferCapabilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_qiqis.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_qiqis.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_qiqis.utils.Utils;

/** the class for each document **/
class DocInfo
{
  int qid;
  int rel;
  Map<String, Integer> vector;
  String text;
  
  public DocInfo(int qid,int rel,Map<String,Integer> vector,String text) {
    // TODO Auto-generated constructor stub
    this.qid=qid;
    this.rel=rel;
    this.vector=vector;
    this.text=text;
  }
  
  public Map<String, Integer> GetVector()
  {
    return vector;
  }
  
  public int GetQid()
  {
    return qid;
  }
  
  public int GetRel()
  {
    return rel;
  }
  
  public String GetText()
  {
    return text;
  }
}

public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	/** ArrayList to store documents for each query **/
	public ArrayList<DocInfo> docList=new ArrayList<DocInfo>();
	
	/** Arraylist of arraylist to store documents **/
	public ArrayList<DocInfo>  relDocInfos=new ArrayList<DocInfo>();
	
	/** word dictionary **/ 
  public ArrayList<String> wordDictionary;

  
  /** similarity for all **/
  public ArrayList<ArrayList<Double>> similarityAll=new ArrayList<ArrayList<Double>>();
  
  /** record the query **/
  DocInfo query = null;  
  
  /** record the rank **/
  ArrayList<Integer> rankList=new ArrayList<Integer>();
  
  

	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		
		double similarity = 0;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
		 Map<String, Integer> Vector=new HashMap<String, Integer>();
	  
	  
		if (it.hasNext()) {
			Document doc = (Document) it.next();
      
			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList=Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			//Do something useful here
      // create  doc class for each document
			for(int i=0;i<tokenList.size();i++)
      {
        Vector.put(tokenList.get(i).getText(),tokenList.get(i).getFrequency());
      } 
			
			DocInfo dInfo =new DocInfo(doc.getQueryID(), doc.getRelevanceValue(), Vector,doc.getText());
			//add document to docList
			docList.add(dInfo); 
			
			if(doc.getRelevanceValue()==99)
			{
			  query=dInfo;
        similarityAll.add(new ArrayList<Double>());
			}
			else
			{
			  similarity=computeCosineSimilarity(query.GetVector(), Vector);
			  similarityAll.get(similarityAll.size()-1).add(similarity);

			  //add queryDocument to relDocInfos List
			  if(doc.getRelevanceValue()==1)
			    relDocInfos.add(dInfo);
			  
			}     
			
		}
		

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
	  
		super.collectionProcessComplete(arg0);
		
	
		// TODO :: compute the rank of retrieved sentences
		File f=new File("report.txt");
	  BufferedWriter fBufferedWriter=new BufferedWriter(new FileWriter(f));
	  ArrayList<Double> similarityCluster=null;
	  DecimalFormat format=new DecimalFormat("0.0000");
		for(int i=0;i<similarityAll.size();i++)
		{
		  similarityCluster=similarityAll.get(i);
		  
		  double firstOne=similarityCluster.get(0);
		  Collections.sort(similarityCluster);  
		  Collections.reverse(similarityCluster);
		 
		  
		  rankList.add(similarityCluster.indexOf(firstOne)+1);
		 
		  
		  String newFirst=format.format(firstOne);
		  fBufferedWriter.write("cosine="+newFirst+" rank="+(similarityCluster.indexOf(firstOne)+1)+" qid="+relDocInfos.get(i).GetQid()+" rel="+1+" "+relDocInfos.get(i).GetText());
		  fBufferedWriter.write("\n");
		}
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		 String newMetric_mrr=format.format(metric_mrr);
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
		
		fBufferedWriter.write("MRR="+newMetric_mrr);
		fBufferedWriter.close();
		
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;
		double total1 = 0,total2 = 0;
		
		for(Map.Entry<String, Integer> entry:queryVector.entrySet())
		{
		  total1+=Math.pow(entry.getValue(), 2);
		}
		
		for(Map.Entry<String, Integer> entry:docVector.entrySet())
    {
      total2+=Math.pow(entry.getValue(), 2);
    }

		// TODO :: compute cosine similarity between two sentences
		for(Map.Entry<String, Integer> entry: queryVector.entrySet())
		{
		  //for the same string, we need to calculate it.
		  if (docVector.containsKey(entry.getKey()))
		  {
		    cosine_similarity+=(entry.getValue()/Math.sqrt(total1))*(docVector.get(entry.getKey())/Math.sqrt(total2));
		  }
		  //for the not same string, do nothing
		  else {
        continue;
      }
		}
	
		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		for(int i=0;i<rankList.size();i++)
		{
		  metric_mrr+=1.0/rankList.get(i);	  
		}
		metric_mrr=(1.0/similarityAll.size())*metric_mrr;
		return metric_mrr;
	}

}
