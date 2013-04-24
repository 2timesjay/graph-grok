import java.lang.reflect.Array;
import java.util.*;

import javax.swing.event.ListSelectionEvent;

public class SkipArray {
	double[] values;
	int[] primaryPos;
	int[] primaryKey;
	int[] secondaryKey;
	int pLen;
	int sLen;
	
	/*
	 * Converts a list of counters into a single 1-D array.
	 * 
	 */
	public SkipArray(List<Integer> pk, List<Counter> counters){
//		Collections.sort(pk);//Need to implement argsort
		pLen = pk.size();
		primaryPos = new int[pLen+1];
		primaryKey = new int[pLen];
		int pos = 0;
		for(int p: pk){
			primaryKey[p] = pk.get(p);
			primaryPos[p] = pos;
			Counter sk = counters.get(p);
			pos += sk.size();
		}
		primaryPos[pLen] = pos;
		sLen = pos;
		secondaryKey = new int[sLen];
		values = new double[sLen];
		pos = 0;
		for(int i = 0; i < pLen; i++){
			pos = primaryPos[i];
			Counter sk = counters.get(i);
			int j = 0;
			for(int s: sk.keySet()){
				secondaryKey[pos+j] = s;
				values[pos+j] = sk.get(s);
				j += 1;
			}
			pos += sk.size();
		}
	}
	
	public SkipArray flipPrimarySecondary(){
		//Get union of all secondary keys on first pass
		Set<Integer> secondaryUnique = new HashSet<Integer>();
		for(int i = 0; i < secondaryKey.length; i++){
			secondaryUnique.add(secondaryKey[i]);
		}
		List<Integer> flippedPK = new ArrayList<Integer>(secondaryUnique);
		List<Counter> flippedCounters = new ArrayList<Counter>();
		for(int secondary: flippedPK){
			flippedCounters.add(new Counter());
		}
		int pos = 0;
		int nextPos = 0;
		for(int i = 0; i < pLen; i++){
			int p = primaryKey[i];
			pos = primaryPos[i];
			nextPos = primaryPos[i+1];
			for(int j = 0; j < (nextPos-pos); j++){
				double value = values[pos+j];
				int s = secondaryKey[pos+j];
				int sIndex = 0;//Should use Indexer from Percy/Fig
				flippedCounters.get(sIndex).add(p,value);
			}
		}
		return new SkipArray(flippedPK,flippedCounters);
	}
}
