/**
 * Created on October, 2017 
 * @author Juan Carlos Gomez
 * @email jc.gomez@ugto.mx
 * @cite Arts, S., Cassiman, B., & Gomez, J. C. (2017). Text matching to measure patent similarity. Strategic Management Journal.
 * 
 * Codifies the patent numbers  and words in the vocabulary using a base 50 index.
 * This helps saving space when storing the similarity calculations
  */
package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Stage02CodifyIdxPatents {

	char digits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','!','@','#','$','%','&','*','(',')','-','_','=','+','?'};
	
	public Stage02CodifyIdxPatents(){}

	/**
	 * Returns the 50 possible digits.
	 * 
	 * @return The vector of digits
	 */
	public char[] getDigits() {
		return digits;
	}
	
	/**
	 * Converts an integer in a base 50 code.
	 * 
	 * @return The codified integer as String.
	 */
	public String convertToCode(int n){
		String coded = "";
		int rem = 0;
		if(n==0)
			coded = "0";
		while(n>0)
		{
			rem=n%this.getDigits().length; 
			coded=this.getDigits()[rem]+coded; 
			n=n/this.getDigits().length;
		}
		return coded;
	}
	
	public static void main(String[] args) throws IOException{
		Stage02CodifyIdxPatents cip = new Stage02CodifyIdxPatents();
		
		File fMainDir = new File("C:/Users/JC/Documents/CodeandData/datasets/2016_smj_data"); //Working directory
		
		File fPatentsNum = new File(fMainDir, "vocabulary_raw.txt"); //Original vocabulary
		File fPatentsIdxs = new File(fMainDir, "vocabulary.txt"); //Codified vocabulary
		String line = "";
		BufferedReader brContent = new BufferedReader(new FileReader(fPatentsNum));
		PrintWriter pwIndexed = new PrintWriter(fPatentsIdxs);
		System.out.println("Codifying vocabulary...");
		int n = 0;
		while((line=brContent.readLine())!=null){
			String code = cip.convertToCode(n);
			pwIndexed.println(code+" "+line); //Stores the codified word  and the original one
			n++;
			if(n%10000==0){ //Outputs the progress of this process
				System.out.println("\tProcessed = "+n+" words");
			}
		}
		brContent.close();
		pwIndexed.close();
		
		fPatentsNum = new File(fMainDir, "patents_numbers.txt"); //Original patent numbers
		fPatentsIdxs = new File(fMainDir, "patents_idxs.txt"); //Codified patent numbers
		brContent = new BufferedReader(new FileReader(fPatentsNum));
		pwIndexed = new PrintWriter(fPatentsIdxs);
		System.out.println("Codifying patent numbers...");
		n = 0;
		while((line=brContent.readLine())!=null){
			String code = cip.convertToCode(n);
			pwIndexed.println(code+" "+line); //Stores the codified patent number and the original one
			n++;
			if(n%100000==0){ //Outputs the progress of this process
				System.out.println("\tProcessed = "+n+" patents");
			}
		}
		brContent.close();
		pwIndexed.close();
	}
}
