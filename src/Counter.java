/*
 * Python-style counter. Like a HashMap<Integer,Double>, except that it automatically takes care of not-yet-added keys
 * Returns 0 for "get" on non-present keys
 * Creates a (key,0)
 */
import java.util.*;

public class Counter{
	private HashMap<Integer, Double> counts;
	
	public Counter() {
		counts  = new HashMap<Integer, Double>();
	}

	@SuppressWarnings("unchecked")
	public Counter(Counter orig){
		this.counts = (HashMap<Integer, Double>)orig.counts.clone();
	}
	
	public boolean isEmpty(){
		return counts.isEmpty();
	}
	
	public double norm(){
		return Math.sqrt(this.dot(this));
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
	
	public double dot(Counter vector){
		double sum = 0;
		for(int k: this.keySet()){
			sum += this.get(k)*vector.get(k);
		}
		return sum;
	}

	public double dist(Counter vector){
		double sum = 0;
		for(int k: this.keySet()){
			sum += (this.get(k)-vector.get(k))*(this.get(k)-vector.get(k));
		}
		return sum;
	}
	
	public void addAll(Counter other){
		for(int k: other.keySet()){
			this.add(k, other.get(k));
		}
	}
	
	public Double get(int k) {
		Double v = counts.get(k);
		return (((v == null) && !counts.containsKey(k)) ? 0 : v);
	}
	
	public Double getPath(int k) {
		Double v = counts.get(k);
		return (((v == null) && !counts.containsKey(k)) ? Double.MAX_VALUE : v);
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
		Counter c = new Counter();
		c.add(1,3);
		c.add(1, 5);
		Counter cc = new Counter(c);
		cc.add(1, 1);
		System.out.println(c.get(1)+" "+cc.get(1)+" "+c.size());
	}

}