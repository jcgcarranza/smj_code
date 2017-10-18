package experiments.patents.journal.strmng;

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

import varia.EnglishStopWords;

public class Stage01PreprocessData {

	public Stage01PreprocessData(){}

	/**
	 * Gets a token pattern to extract words from a text using a defined regular expression.
	 * The default regular expression matches alphanumeric sequences of characters and - and
	 * does not consider the _.
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
	 * Reads a file containing patent raw content and transform it into a bag of words file.
	 * The file with the raw content should have a patent per line.
	 * By default this process removes English stopwords, words formed only by numbers
	 * and words of only one character.
	 * [Hint: Check the format of the raw file].
	 * @param fInput The file containing the patents raw content.
	 * @param fOutput The file containing the patents content as a bag of words.
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
			if((nDocs%100000)==0){
				System.out.println("Processed = "+nDocs+" documents");
			}
		}
		brContent.close();
		pwOutput.close();
	}

	/**
	 * Removes words from a dictionary given a threshold of the minimum number of documents
	 * where a word appears.
	 * Transform each token (word) to lower case.
	 * @param text The text.
	 * @return A list of tokens.
	 */
	public void pruneDictionary(LinkedHashMap<String, Integer> dictionary, int threshold){
		List<String> words = new Vector<String>(dictionary.keySet());
		for(String word:words){
			int totalFrequency = dictionary.get(word);
			if(totalFrequency<threshold)
				dictionary.remove(word);
		}
	}

	public void extractVocabulary(File fInput, File fVocabulary) throws IOException{
		String line = "";
		String[] lineSplit = null;
		int nDocs = 0;
		BufferedReader brContent = new BufferedReader(new FileReader(fInput));
		LinkedHashMap<String, Integer> vocabulary = new LinkedHashMap<String, Integer>();
		while((line=brContent.readLine())!=null){
			lineSplit = line.split(" ");
			for(int i=2;i<lineSplit.length;i++){
				String token = lineSplit[i];
				if(vocabulary.get(token)==null)
					vocabulary.put(token, 0);
				vocabulary.put(token, vocabulary.get(token)+1);
			}
			nDocs++;
			if((nDocs%100000)==0){
				System.out.println("Processed = "+nDocs+" documents");
			}
		}
		brContent.close();
		this.pruneDictionary(vocabulary, 2);
		PrintWriter pwVocabulary = new PrintWriter(fVocabulary);
		for(String token:vocabulary.keySet()){
			pwVocabulary.println(token);
		}
		pwVocabulary.close();
	}

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
			if((nDocs%100000)==0){
				System.out.println("Processed = "+nDocs+" documents");
			}
		}
		brContent.close();
		pwYear.close();
		pwIdx.close();
		pwOutput.close();
	}
	
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
		File fMainDir = new File("C:/Users/JC/Documents/KULeuvenII/CodeandData/textdatasets/2016 strategic management data"); //Working directory
		File fData = new File(fMainDir, "patent_data_raw.csv");
		File fTerms = new File(fMainDir, "patents_terms_raw.txt");
		File fYears = new File(fMainDir, "patents_years.txt");
		File fPatentsIdxs = new File(fMainDir, "patents_numbers.txt");
		File fClean = new File(fMainDir,"patents_terms.txt");
		File fVocabulary = new File(fMainDir, "vocabulary_raw.txt");
		
		//Step 01: create bag of words file
		ppd.createBagofWords(fData, fTerms);
		//Step 02: extract vocabulary (use a threshold of document frequency > 1)
		ppd.extractVocabulary(fTerms, fVocabulary);
		//Step 03: clean the patents using the vocabulary (remove specifc words)
		LinkedHashMap<String, Integer> vocabulary = new LinkedHashMap<String, Integer>();
		ppd.readVocabulary(fVocabulary, vocabulary);
		ppd.cleanPatents(fTerms, fClean, fYears, fPatentsIdxs, vocabulary);
	}
}