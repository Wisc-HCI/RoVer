package enums;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class StateCompatabilityLookup {
	
	HashMap<StateClass,HashMap<StateClass,Boolean>> lookup;
	
	public StateCompatabilityLookup() {
		initializeLookup();
	}
	
	private void initializeLookup() {
		lookup = new HashMap<StateClass,HashMap<StateClass,Boolean>>();
		
		// populating lookup
		HashMap<Integer,StateClass> scIndex = new HashMap<Integer,StateClass>();
		try {

            BufferedReader br = new BufferedReader(new FileReader("data" + File.separator + "StateClassLookup.csv"));
            // read the first line and get the StateClasses
            String line = br.readLine();
            String[] data = line.split(",");
            for (int i = 1; i < data.length; i++)
            	scIndex.put(i, String2StateClass.findStateClass(data[i]));
            
            // read the rest
            int[][] vals = new int[data.length - 1][data.length - 1];
            int currLine = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                data = line.split(",");
                for (int i = 1; i < data.length; i++) {
                	int val = Integer.parseInt(data[i]);
                	vals[currLine][i-1] = val;
                }
                currLine++;
            }
            
            for (int i = 0; i < vals.length; i++) {
            	StateClass sc1 = scIndex.get(i+1);
            	HashMap<StateClass, Boolean> temp = new HashMap<StateClass, Boolean>();
            	for (int j = 0; j < vals[0].length; j++) {
            		int val = vals[i][j];
            		boolean bool = false;
            		if (val == 1)
            			bool = true;
            		StateClass sc2 = scIndex.get(j+1);
            		temp.put(sc2, bool);
            	}
            	lookup.put(sc1, temp);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	// a dumb temporary compatability checker
	public boolean checkCompatability(StateClass sc1, StateClass sc2) {
		return lookup.get(sc1).get(sc2);
	}
	
	public String toString() {
		String str = "";
		
		Iterator it = lookup.entrySet().iterator();
		while (it.hasNext()) {
		    HashMap.Entry pair = (HashMap.Entry)it.next();		    
		    HashMap<StateClass,Boolean> val = (HashMap<StateClass, Boolean>) pair.getValue();
		    Iterator it2 = val.entrySet().iterator();
		    while (it2.hasNext()) {
		    	HashMap.Entry pair2 = (HashMap.Entry)it2.next();
		    	str += pair.getKey() + " + " +  pair2.getKey() + " : " + pair2.getValue() + "\n";
		    }
		}
		
		return str;
	}
	
}
