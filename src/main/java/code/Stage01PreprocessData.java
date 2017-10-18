/**
 * Created on October, 2017 
 * @author Juan Carlos Gomez
 * @email jc.gomez@ugto.mx
 * @cite Arts, S., Cassiman, B., & Gomez, J. C. (2017). Text matching to measure patent similarity. Strategic Management Journal.
 * 
 * Preprocesses patent data in a CSV format
  */
package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import code.EnglishStopWords;

public class Stage01PreprocessData {

	
	/**
	 * Constructor
	 */
	public Stage01PreprocessData(){}

	/**
	 * Gets a token pattern to extract words from a text using a defined regular expression.
	 * The default regular expression matches alphanumeric sequences of characters and - and
	 * does not consider the _.
	 * 
	 * @return A token pattern.
	 */
	private Pattern getTokenPattern() {
		Pattern $token_pattern = null;
		if ($token_pattern == null) {
			try {
				$token_pattern = Pattern.compile("\\w[[\\-]\\w&&[^_]]+\\w"); //Regular expression
			} catch (PatternSyntaxException e) {
				System.err.println(e);
				System.exit(1);
			}
		}
		return $token_pattern;
	}

	/**
	 * Splits a text into a list of tokens (words) using a defined regular expression.
	 * Transforms each token (word) to lower case.
	 * 
	 * @param text The text.
	 * @return A list of tokens.
	 */
	public ArrayList<String> tokenize(String text) {
		ArrayList<String> tokens = new ArrayList<String>();
		Matcher matcher = getTokenPattern().matcher(text); //Load the matcher with the regular expression
		while (matcher.find())
			tokens.add(matcher.group().toLowerCase());
		return tokens;
	}

	/**
	 * Reads a file containing patent raw content and transform it into a bag-of-words file.
	 * The file with the raw content should have a patent per line in a CSV format.
	 * Takes the title and the abstract sections.
	 * Transform the patent content to lower case.
	 * By default this process removes English stopwords, words formed only by numbers
	 * and words of only one character.
	 * [Hint: Check the format of the raw CSV file to match the number and order of columns].
	 * 
	 * @param fInput The file containing the patents raw content.
	 * @param fOutput The file containing the patents content as a bag-of-words.
	 */
	public void createBagofWords(File fInput, File fOutput) throws IOException{
		EnglishStopWords sw = new EnglishStopWords(); //Stopword list
		String line = "";
		String[] lineSplit = null;
		String text = "";
		String year = "";
		int nDocs = 0;
		String patentNum = "";
		BufferedReader brContent = new BufferedReader(new FileReader(fInput));
		line = brContent.readLine(); //Remove header in case the file contains one
		PrintWriter pwOutput = new PrintWriter(fOutput);
		while((line=brContent.readLine())!=null){
			lineSplit = line.split("\"");
			if(lineSplit.length==1){
				lineSplit = lineSplit[0].split(",");
				patentNum = lineSplit[0];
				year = lineSplit[1];
				text = lineSplit[2]+" "+lineSplit[3];
			}
			else if(lineSplit.length==2){
				text = lineSplit[1];
				lineSplit = lineSplit[0].split(",");
				patentNum = lineSplit[0];
				year = lineSplit[1];
				text = lineSplit[2]+" "+text;
			}
			else if (lineSplit.length==3){
				text = lineSplit[1]+" "+lineSplit[2];
				lineSplit = lineSplit[0].split(",");
				patentNum = lineSplit[0];
				year = lineSplit[1];
			}
			else{ 
				text = lineSplit[1] + lineSplit[3];
				lineSplit = lineSplit[0].split(",");
				patentNum = lineSplit[0];
				year = lineSplit[1];
			}
			text = text.toLowerCase();
			ArrayList<String> tokens = this.tokenize(text); //Split the patent content in tokens (words)
			TreeMap<String, Integer> vector = new TreeMap<String, Integer>();
			for(String token:tokens){
				if(!sw.isStopWord(token) && token.length()>1 && !token.matches("[[0-9]+[-][0-9]+]+")){ //Remove stopwords, words formed only by numbers and words of only one character
					vector.put(token, 1);
				}
			}
			if(!vector.isEmpty()){
				pwOutput.print(patentNum+" "+year);
				for(String token:vector.keySet()){
					pwOutput.print(" "+token);
				}
				pwOutput.println();
			}
			else{
				System.out.println(nDocs);
			}
			nDocs++;
			if((nDocs%100000)==0){ //Outputs the progress of this process
				System.out.println("\tProcessed = "+nDocs+" documents");
			}
		}
		brContent.close();
		pwOutput.close();
	}

	/**
	 * Removes words from a vocabulary, given a threshold of the minimum number of documents
	 * where a word occurs.
	 * 
	 * @param vocabulary The dictionary from where to remove words.
	 * @param threshold The threshold of minimum number of documents where a word should occur.
	 */
	public void pruneVocabulary(LinkedHashMap<String, Integer> vocabulary, int threshold){
		List<String> words = new Vector<String>(vocabulary.keySet());
		for(String word:words){
			int totalFrequency = vocabulary.get(word);
			if(totalFrequency<threshold)
				vocabulary.remove(word);
		}
	}

	/**
	 * Extracts a vocabulary from a patent file in bag-of-words format.
	 * Prunes the vocabulary using a threshold of the minimum number of documents where a word
	 * should occur.
	 * Stores the vocabulary in a file with one word per line.
	 * 
	 * @param fInput The patent file in bag-of-words format.
	 * @param fVocabulary The file where to store the extracted vocabulary.
	 * @param threshold The threshold of minimum number of documents where a word should occur.
	 */
	public void extractVocabulary(File fInput, File fVocabulary, int threshold) throws IOException{
		String line = "";
		String[] lineSplit = null;
		int nDocs = 0;
		BufferedReader brContent = new BufferedReader(new FileReader(fInput));
		LinkedHashMap<String, Integer> vocabulary = new LinkedHashMap<String, Integer>();
		while((line=brContent.readLine())!=null){
			lineSplit = line.split(" ");
			for(int i=2;i<lineSplit.length;i++){
				String word = lineSplit[i];
				if(vocabulary.get(word)==null)
					vocabulary.put(word, 0);
				vocabulary.put(word, vocabulary.get(word)+1);
			}
			nDocs++; 
			if((nDocs%100000)==0){ //Outputs the progress of this process
				System.out.println("\tProcessed = "+nDocs+" documents");
			}
		}
		brContent.close();
		this.pruneVocabulary(vocabulary, threshold);
		PrintWriter pwVocabulary = new PrintWriter(fVocabulary);
		for(String word:vocabulary.keySet()){
			pwVocabulary.println(word);
		}
		pwVocabulary.close();
	}

	/**
	 * Cleans a patent file in bag-of-words format by removing words that are not in the general
	 * vocabulary.
	 * Additionally creates two files, one containing the patent numbers and the year of each patent.
	 * The clean patent file, the patent number file and the patent year file have a correspondence one
	 * to one.
	 * 
	 * @param fInput The patent file in bag-of-words format.
	 * @param fOutput The clean patent file.
	 * @param fYear The patent years file.
	 * @param fIdx The patent numbers file.
	 * @param vocabulary The vocabulary that is used to clean the bag-of-words patent file.
	 */
	public void cleanPatents(File fInput, File fOutput, File fYear, File fIdx, LinkedHashMap<String, Integer> vocabulary) throws IOException{
		String line = "";
		String[] lineSplit = null;
		String numPatent = "";
		String year = "";
		int nDocs = 0;
		ArrayList<String> vector = new ArrayList<String>();
		BufferedReader brContent = new BufferedReader(new FileReader(fInput));
		PrintWriter pwYear = new PrintWriter(fYear);
		PrintWriter pwIdx = new PrintWriter(fIdx);
		PrintWriter pwOutput = new PrintWriter(fOutput);
		while((line=brContent.readLine())!=null){
			lineSplit = line.split(" ");
			numPatent = lineSplit[0];
			year = lineSplit[1];
			vector.clear();
			for(int i=2;i<lineSplit.length;i++){
				String token = lineSplit[i];
				if(vocabulary.get(token)!=null){
					vector.add(token);
				}
			}
			if(!vector.isEmpty()){
				pwYear.println(year);
				pwIdx.println(numPatent);
				pwOutput.print(numPatent+";"+vector.size()+";"+vector.get(0));
				for(int i=1;i<vector.size();i++){
					String token=vector.get(i);
					pwOutput.print(" "+token);
				}
				pwOutput.println();
			}
			nDocs++;
			if((nDocs%100000)==0){ //Outputs the progress of this process
				System.out.println("\tProcessed = "+nDocs+" documents");
			}
		}
		brContent.close();
		pwYear.close();
		pwIdx.close();
		pwOutput.close();
	}
	
	/**
	 * Loads a vocabulary from a vocabulary file. This file must have one word per line.
	 * The vocabulary is formed by a word and an index, corresponding to the line number in the file.
	 * 
	 * @param fVocabulary The vocabulary file.
	 * @param vocabulary The vocabulary.
	 */
	public void readVocabulary(File fVocabulary, LinkedHashMap<String, Integer> vocabulary) throws IOException{
		String line = "";
		int nLine = 0;
		BufferedReader brVocabulary = new BufferedReader(new FileReader(fVocabulary));
		while((line=brVocabulary.readLine())!=null){
			vocabulary.put(line, nLine);
			nLine++;
		}
		brVocabulary.close();
	}
	
	public static void main(String[] args) throws IOException{
		Stage01PreprocessData ppd = new Stage01PreprocessData();
		
		File fMainDir = new File("C:/Users/JC/Documents/CodeandData/datasets/2016_smj_data"); //Working directory
		File fData = new File(fMainDir, "patent_data_raw.csv"); //Raw patent data in CSV format
		File fTerms = new File(fMainDir, "patents_terms_raw.txt"); //Patent data in bag-of-words format
		File fYears = new File(fMainDir, "patents_years.txt"); //Patent years file
		File fPatentsIdxs = new File(fMainDir, "patents_numbers.txt"); //Patent numbers file
		File fClean = new File(fMainDir,"patents_terms.txt"); //Clean patent data
		File fVocabulary = new File(fMainDir, "vocabulary_raw.txt"); //Vocabulary
		
		System.out.println("Creating bag-of-words file...");
		ppd.createBagofWords(fData, fTerms);
		System.out.println("Extracting vocabulary...");
		ppd.extractVocabulary(fTerms, fVocabulary, 2);
		System.out.println("Cleaning the bag-of-words file...");
		LinkedHashMap<String, Integer> vocabulary = new LinkedHashMap<String, Integer>();
		ppd.readVocabulary(fVocabulary, vocabulary);
		ppd.cleanPatents(fTerms, fClean, fYears, fPatentsIdxs, vocabulary);
	}
}