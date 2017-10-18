/**
 * Created on October, 2017 
 * @author Juan Carlos Gomez
 * @email jc.gomez@ugto.mx
 * @cite Arts, S., Cassiman, B., & Gomez, J. C. (2017). Text matching to measure patent similarity. Strategic Management Journal.
 * 
 * Indexes the patent data using the codified patent numbers and words from the vocabulary.
 * This helps to save space in disk and memory when computing the similarities.
  */

package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

public class Stage03IndexPatents {

	/**
	 * Constructor
	 */
	public Stage03IndexPatents(){}
	
	/**
	 * Indexes the patent data using the codified patent numbers and words from the general
	 * vocabulary.
	 * 
	 * @param fClean The file containing the clean patent data.
	 * @param vocabulary The codified vocabulary for the data.
	 * @param patentsIdxs The codified patent numbers.
	 */
	public void indexPatents(File fClean, File fIndexed, LinkedHashMap<String, String> vocabulary, LinkedHashMap<String, String> patentsIdxs) throws IOException{
		String line = "";
		String[] lineSplit = null;
		String numPatent = "";
		String numTerms = "";
		int nDocs = 0;
		BufferedReader brContent = new BufferedReader(new FileReader(fClean));
		PrintWriter pwIndexed = new PrintWriter(fIndexed);
		while((line=brContent.readLine())!=null){
			lineSplit = line.split(";");
			numPatent = lineSplit[0];
			numTerms = lineSplit[1];
			String idxPatent = patentsIdxs.get(numPatent);
			pwIndexed.print(idxPatent+" "+numTerms);
			lineSplit = lineSplit[2].split(" ");
			for(int i=0;i<lineSplit.length;i++){
				String token = lineSplit[i];
				String idxToken = vocabulary.get(token);
				pwIndexed.print(" "+idxToken+":"+1);
			}
			pwIndexed.println();
			nDocs++;
			if((nDocs%100000)==0){  //Outputs the progress of this process
				System.out.println("\tProcessed = "+nDocs+" documents");
			}
		}
		brContent.close();
		pwIndexed.close();
	}
	
	/**
	 * Loads a file composed of a codified index and a original number (or word).
	 * Stores the codified index and the original number (or word) in a map.
	 * 
	 * @param fContent The file containing codified indexes and original numbers (or words). 
	 * @param lhmIdx The map to store the data from the file.
	 */
	public void readIndexes(File fContent, LinkedHashMap<String, String> lhmIdx) throws IOException{
		FileInputStream fIContent = new FileInputStream(fContent);
		BufferedReader brContent = new BufferedReader(new InputStreamReader(fIContent,"UTF-8"));
		String line = null;
		String[] lineBreak = null;
		while ((line=brContent.readLine())!=null){
			lineBreak = line.split(" ");
			lhmIdx.put(lineBreak[1], lineBreak[0]);
		}
		brContent.close();
	}
	
	public static void main(String[] args) throws IOException{
		Stage03IndexPatents ip = new Stage03IndexPatents();
		
		File fMainDir = new File("C:/Users/JC/Documents/CodeandData/datasets/2016_smj_data"); //Working directory
		File fClean = new File(fMainDir,"patents_terms.txt"); //Clean patent data
		File fPatentsIdxs = new File(fMainDir, "patents_idxs.txt"); //Codified patent numbers
		File fVocabulary = new File(fMainDir, "vocabulary.txt"); //Codified vocabulary
		File fIndexed = new File(fMainDir, "patents_indexed.txt"); //Indexed patent data
		
		LinkedHashMap<String, String> vocabulary = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> patentsIdxs = new LinkedHashMap<String, String>();
		
		System.out.println("Loading codified vocabulary...");
		ip.readIndexes(fVocabulary, vocabulary);
		System.out.println("Loading codified patent numbers...");
		ip.readIndexes(fPatentsIdxs, patentsIdxs);
		System.out.println("Indexing patent data...");
		ip.indexPatents(fClean, fIndexed, vocabulary, patentsIdxs);
	}
}
