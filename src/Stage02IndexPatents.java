package experiments.patents.journal.strmng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

import preprocessingIO.FileOperationsRadical;

public class Stage02IndexPatents {

	public Stage02IndexPatents(){}

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
			if((nDocs%100000)==0){
				System.out.println("Processed = "+nDocs+" documents");
			}
		}
		brContent.close();
		pwIndexed.close();
	}
	
	public static void main(String[] args) throws IOException{
		//Step 5: Index patents
		FileOperationsRadical fo = new FileOperationsRadical();
		Stage02IndexPatents ip = new Stage02IndexPatents();
		File fMainDir = new File("C:/Users/JC/Documents/KULeuvenII/CodeandData/textdatasets/2016 strategic management data");
		File fClean = new File(fMainDir,"patents_terms.txt");
		File fPatentsIdxs = new File(fMainDir, "patents_idxs.txt");
		File fVocabulary = new File(fMainDir, "vocabulary.txt");
		
		File fIndexed = new File(fMainDir, "patents_indexed.txt");
		
		LinkedHashMap<String, String> vocabulary = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> patentsIdxs = new LinkedHashMap<String, String>();
		//fo.readSequentialData(fVocabulary, vocabulary);
		fo.readIndexes(fVocabulary, vocabulary);
		fo.readIndexes(fPatentsIdxs, patentsIdxs);
		ip.indexPatents(fClean, fIndexed, vocabulary, patentsIdxs);
	}
}
