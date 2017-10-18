/**
 * Created on October, 2017 
 * @author Juan Carlos Gomez
 * @email jc.gomez@ugto.mx
 * @cite Arts, S., Cassiman, B., & Gomez, J. C. (2017). Text matching to measure patent similarity. Strategic Management Journal.
 * 
 * Splits the indexed patent data per year
  */
package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Stage04SplitDataPerYear {
	
	/**
	 * Constructor
	 */
	public Stage04SplitDataPerYear(){}
	
	/**
	 * Splits the indexed patent data per year. It creates new directory (years) inside
	 * the current working directory.
	 * 
	 * @param fContent The file containing the indexed patent data.
	 * @param alYear A list containing the year of each patent.
	 * @param fMainDir The working directory to store the split data.
	 */
	public void splitDataPerYear(File fContent, ArrayList<String> alYears, File fMainDir) throws IOException{
		LinkedHashMap<String, PrintWriter> years = new LinkedHashMap<String, PrintWriter>();
		String line = "";
		String yearPatent = "";
		int yearPat = 0;
		int nPatent = 0;
		BufferedReader brContent = new BufferedReader(new FileReader(fContent));
		File yearsDir = new File(fMainDir+"/years");
		System.out.println(yearsDir);
		if (!yearsDir.exists())  //Create directory if it does not exist
            yearsDir.mkdirs();
		while((line=brContent.readLine())!=null){
			yearPatent = alYears.get(nPatent);
			yearPat = Integer.valueOf(yearPatent);
			if(yearPat>=1900 && yearPat<=2016){ //Check the maximum year of the data
				if(years.get(yearPatent)==null){
					File fYear = new File(yearsDir,"patents_indexed_"+yearPatent+".txt");
					PrintWriter pwYear = new PrintWriter(fYear);
					years.put(yearPatent, pwYear);
				}
				years.get(yearPatent).println(line);
			}
			else{
				System.out.println(yearPat);
			}
			nPatent++;
			if (nPatent%100000==0){ //Outputs the progress of this process
				System.out.println("\tProcessed "+nPatent+" documents...");
			}
		}
		brContent.close();
		for(String pwYear:years.keySet())
			years.get(pwYear).close();
		System.out.println("Total patents = "+nPatent);
	}
	
	/**
	 * Read a file containing the year of each patent. The years are stored one per line, and
	 * there is a correspondence one to one with the indexed patent data.
	 * 
	 * @param fYears The file containing the patent years.
	 * @param years A list to store the year of each patent.
	 */
	public void readYears(File fYears, ArrayList<String> years) throws IOException{
		String line = "";
		BufferedReader brContent = new BufferedReader(new FileReader(fYears));
		while((line=brContent.readLine())!=null){
			years.add(line);
		}
		brContent.close();
	}
	
	public static void main(String[] args) throws IOException{
		Stage04SplitDataPerYear sdpy = new Stage04SplitDataPerYear();
		
		File fMainDir = new File("C:/Users/JC/Documents/CodeandData/datasets/2016_smj_data"); //Working directory
		File fIndexed = new File(fMainDir, "patents_indexed.txt"); //Indexed patent data
		File fYears = new File(fMainDir, "patents_years.txt");  //Patent years file
		
		ArrayList<String> alYears = new ArrayList<String>();
		
		System.out.println("Loading patent years...");
		sdpy.readYears(fYears, alYears);
		System.out.println("Splitting patent data per year...");
		sdpy.splitDataPerYear(fIndexed, alYears, fMainDir);
	}
}
