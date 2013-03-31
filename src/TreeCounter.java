/*
 * Python-style counter. Like a TreeMap<Integer,Double>, except that it automatically takes care of not-yet-added keys
 * Returns 0 for "get" on non-present keys
 * Creates a (key,0)
 */
import java.util.*;

public class TreeCounter{
	private TreeMap<Integer, Double> counts;
	
	public TreeCounter() {
		counts  = new TreeMap<Integer, Double>();
	}

	@SuppressWarnings("unchecked")
	public TreeCounter(TreeCounter orig){
		this.counts = (TreeMap<Integer, Double>)orig.counts.clone();
	}
	
	public boolean isEmpty(){
		return counts.isEmpty();
	}
	
	public void add(int k, double v){
		double sum = this.get(k) + v;
		this.put(k, sum);
	}
	
	public void multiply(int k, double f){
		double product = this.get(k)*f;
		this.put(k, product);
	}
	
	public void multiply(double f){
		for(int k: this.keySet()){
			this.put(k, f);
		}
	}
	
	public double dot(List<Double> vector){
		double sum = 0;
		for(int k: this.keySet()){
			sum += this.get(k)*vector.get(k);
		}
		return sum;
	}
	
	public double dot(TreeCounter vector){
		double sum = 0;
		for(int k: this.keySet()){
			sum += this.get(k)*vector.get(k);
		}
		return sum;
	}
	
	public void addAll(TreeCounter other){
		for(int k: other.keySet()){
			this.add(k, other.get(k));
		}
	}
	
	public Double get(int k) {
		Double v = counts.get(k);
		return (((v == null) && !counts.containsKey(k)) ? 0 : v);
	}
	
	public void put(int k, double v){
		counts.put(k, v);
	}
	
	public Set<Integer> keySet(){
		return counts.keySet();
	}
	
	public void remove(int k){
		if(counts.containsKey(k)){
			counts.remove(k);
		}
	}
	
	public int size(){
		return counts.size();
	}
	
	public boolean containsKey(int k){
		return counts.containsKey(k);
	}
	
	public String toString(){
		String myStr = "";
		if(counts.isEmpty()){
			return "-";
		}
		for(int k: this.keySet()){
			myStr += "("+k+","+this.get(k)+") ";
		}
		return myStr;
	}
	
	public static void main(String[] args) {
		System.out.println("we compiled");
		TreeCounter c = new TreeCounter();
		c.add(1,3);
		c.add(1, 5);
		TreeCounter cc = new TreeCounter(c);
		cc.add(1, 1);
		System.out.println(c.get(1)+" "+cc.get(1)+" "+c.size());
	}

}