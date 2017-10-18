/**
 * Created on October, 2017 
 * @author Juan Carlos Gomez
 * @email jc.gomez@ugto.mx
 * @cite Arts, S., Cassiman, B., & Gomez, J. C. (2017). Text matching to measure patent similarity. Strategic Management Journal.
 * 
 * Compute pair-wise Jaccard similarity between patents in the same year.
  */

package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Stage05ComputeSimilarity {

	/**
	 * Constructor
	  */
	public Stage05ComputeSimilarity(){}

	/**
	 * Reads the codified patent numbers and the original ones and stores them in a map.
	 * 
	 * @param lhm The map to store the patent numbers.
	 */
	public void readIndexes(File fInput, LinkedHashMap<String, String> lhm) throws IOException{
		String line = "";
		String[] lineBreak;
		BufferedReader brInput = new BufferedReader(new FileReader(fInput));
		while((line=brInput.readLine())!=null){
			lineBreak = line.split(" ");
			lhm.put(lineBreak[0], lineBreak[1]);
		}
		brInput.close();	
	}

	/**
	 * Loads the patent data from a file, one at a time, and stores them in a map. The key for
	 * the patent in the map is the codified patent number.
	 * 
	 * Additionally it stores an inverted index in a map, for each keyword it stores the list of
	 * codified patent numbers that are associated with it.
	 * 
	 * @param fContent The patent data file.
	 * @param patents The map to store the patent data.
	 * @param invertedIndex The inverted index of keywords and their associated patent numbers.
	 * 
	 */
	public void readPatentsSequencially(File fContent, LinkedHashMap<String, LinkedHashMap<String, Integer>> patents,
			LinkedHashMap<String, ArrayList<String>> invertedIndex) throws IOException{
		String line = "";
		String[] lineSplit = null;
		String[] elements = null;
		String token = "";
		int tfToken = 0; 
		String idPatent = "";
		int nPatent = 0;
		BufferedReader brContent = new BufferedReader(new FileReader(fContent));
		while((line=brContent.readLine())!=null){
			lineSplit = line.split(" ");
			idPatent = lineSplit[0];
			LinkedHashMap<String, Integer> patent = new LinkedHashMap<String, Integer>();
			for(int i=2;i<lineSplit.length;i++){
				elements = lineSplit[i].split(":");
				token = elements[0];
				tfToken = Integer.valueOf(elements[1]);
				patent.put(token, tfToken);
				if(invertedIndex.get(token)==null)
					invertedIndex.put(token, new ArrayList<String>());
				invertedIndex.get(token).add(idPatent);
			}
			patents.put(idPatent, patent);
			nPatent++;
			if (nPatent%10000==0){ //Outputs the progress of this process
				System.out.println("\t"+nPatent+" patents read...");
			}
		}
		brContent.close();
	}
	
	/**
	 * Computes the number of keywords shared by two patents (intersection).
	 * It uses the inverted index to compute the intersection for all the patents related with
	 * a focus patent (patent A). It stores the intersections in a map.
	 * Since the similarities are computed pair-wise and the similarity between A and B
	 * is the same as between B and A, it stores a map of previous patents
	 * for which the similarity was already computed.
	 * 
	 * @param patentA The focus patent.
	 * @param values The map to store all the intersections.
	 * @param patents The patent data.
	 * @param invertedIndex The inverted index of keywords and their associated patent numbers.
	 * @param previousPatents The map of previous patents.
	 */
	public void processPatents(LinkedHashMap<String, Integer> patentA, LinkedHashMap<String, Integer> values, 
			LinkedHashMap<String, LinkedHashMap<String, Integer>> patents, LinkedHashMap<String, ArrayList<String>> invertedIndex,
			LinkedHashMap<String, Integer> previousPatents){
		for(String idx:patentA.keySet()){
			for(String idxPatent:invertedIndex.get(idx)){
				if(previousPatents.get(idxPatent)==null){
					if(values.get(idxPatent)==null)
						values.put(idxPatent, 0);
					values.put(idxPatent, values.get(idxPatent)+1);
				}
			}
		}
	}

	/**
	 * Computes the pair-wise Jaccard similarity between two patents.
	 * 
	 * @param patents The patent data.
	 * @param invertedIndex The inverted index of keywords and their associated patent numbers.
	 * @param lhmPatentsIdx The map containing the codified patent numbers.
	 */
	public void jaccardSimilarity(LinkedHashMap<String, LinkedHashMap<String, Integer>> patents,
			LinkedHashMap<String, ArrayList<String>> invertedIndex, File fSimilarity, LinkedHashMap<String, String> lhmPatentsIdx) throws IOException{
		LinkedHashMap<String, Integer> values = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> previousPatents = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Integer> patentA = null;
		LinkedHashMap<String, Integer> patentB = null;
		for(String idxPatent:patents.keySet()){
			values.put(idxPatent, 0);
		}
		int numKwPatentA = 0;
		int numKwPatentB = 0;
		int union = 0;
		int n = 0;
		PrintWriter pwSimilarity = new PrintWriter(fSimilarity);
		for(String idxPatentA:patents.keySet()){
			previousPatents.put(idxPatentA, 0);
			patentA = patents.get(idxPatentA);
			numKwPatentA = patentA.size();
			values.clear();
			this.processPatents(patentA, values, patents, invertedIndex, previousPatents);
			for(String idxPatentB:values.keySet()){
				if(previousPatents.get(idxPatentB)==null){
					patentB = patents.get(idxPatentB);
					numKwPatentB = patentB.size();
					int intersection = values.get(idxPatentB);
					union = (numKwPatentA+numKwPatentB)-intersection;
					double jaccardSimilarity = (double)intersection/(double)union;
					int roundSim = (int)(jaccardSimilarity*100000);
					jaccardSimilarity = roundSim/100000.0; //Round to 6 digits
					if(jaccardSimilarity>0){ //Outputs only values greather than 0
						pwSimilarity.println(lhmPatentsIdx.get(idxPatentA)+" "+lhmPatentsIdx.get(idxPatentB)+" "+jaccardSimilarity);
					}
				}
			}
			n++;
			if (n%10000==0){ //Outputs the progress of this process
				System.out.println("\t\t"+n+" patents processed...");
			}
		}
		pwSimilarity.close();
	}

	public static void main(String[] args) throws IOException{
		Stage05ComputeSimilarity cs = new Stage05ComputeSimilarity();
		
		int initYear = 1940; //Initial year to compute similarities
		int endYear = 1945; //Final year to compute similarities (check the maximum year in the data)

		LinkedHashMap<String, LinkedHashMap<String, Integer>> patents = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
		LinkedHashMap<String, ArrayList<String>> invertedIndex = new LinkedHashMap<String, ArrayList<String>>();
		LinkedHashMap<String, String> lhmPatentsIdx = new LinkedHashMap<String, String>();
		
		File fMainDir = new File("C:/Users/JC/Documents/CodeandData/datasets/2016_smj_data"); //Working directory
		File fPatentsIdxs = new File(fMainDir,"patents_idxs.txt");
		File fYearData = null;
		File fSimilarity = null;
		File fJaccard = new File(fMainDir+"/jaccard/");
		if (!fJaccard.exists())
			fJaccard.mkdirs();
		
		System.out.println("Reading the codified patent numbers...");
		cs.readIndexes(fPatentsIdxs, lhmPatentsIdx);
		
		for(int year=initYear;year<=endYear;year++){
			patents.clear();
			invertedIndex.clear();
			System.out.println("Computing similarities for year = "+year);
			fYearData = new File(fMainDir, "years/patents_indexed_"+year+".txt");
			System.out.println("\tReading data for year = "+year);
			cs.readPatentsSequencially(fYearData, patents, invertedIndex);
			fSimilarity = new File(fJaccard,"jaccard_"+year+".txt");
			System.out.println("\tDoing the calculations...");
			cs.jaccardSimilarity(patents, invertedIndex, fSimilarity, lhmPatentsIdx);
		}
	}
}