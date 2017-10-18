package code;

import java.util.LinkedHashMap;

public class StopWords {

	private LinkedHashMap<String, Integer> stopWords = null;

	public void add(String word){
		stopWords.put(word, 1);
	}

	public void setStopWords(){
		this.stopWords = new LinkedHashMap<String, Integer>();
	}

	public boolean isStopWord(String word){
		if(this.stopWords.get(word)!=null)
			return true;
		else
			return false;
	}
}
