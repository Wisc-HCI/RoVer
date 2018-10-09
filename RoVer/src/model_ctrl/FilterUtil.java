package model_ctrl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FilterUtil {
	
	public static ArrayList<String[]> extractRawResults() {
		ArrayList<String[]> rawResults = new ArrayList<String[]>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File("tempout.txt")))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	//System.out.println(line);
		    	if (line.contains(":") && line.contains("=1.0"))
		    		rawResults.add(parseFilterOutput(line));
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rawResults;
	}
	
	public static String[] parseFilterOutput(String line) {
		
		//System.out.println(line);
		String temp = line.substring(line.indexOf("(")+1, line.indexOf(")"));
		String[] parsed = temp.split(",");
		
		return parsed;
	}

}
