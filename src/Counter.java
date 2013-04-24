/*
 * Python-style counter. Like a HashMap<Integer,Double>, except that it automatically takes care of not-yet-added keys
 * Returns 0 for "get" on non-present keys
 * Creates a (key,0)
 */
import java.util.*;

public class Counter {
	private HashMap<Integer, Double> counts;

	public Counter() {
		counts = new HashMap<Integer, Double>();
	}

	@SuppressWarnings("unchecked")
	public Counter(Counter orig) {
//		this.counts = (HashMap<Integer, Double>) orig.counts.clone();
		counts = new HashMap<Integer, Double>();
		for(int key: orig.counts.keySet()){
			double value = orig.counts.get(key);
			this.counts.put(key, value);
		}
	}

	public boolean isEmpty() {
		return counts.isEmpty();
	}

	public double norm() {
		return Math.sqrt(this.dot(this));
	}
	
	public double sum(){
		double sum = 0;
		for(int k: this.keySet()){
			sum += this.get(k);
		}
		return sum;
	}

	public void add(int k, double v) {
		double sum = this.get(k) + v;
		this.put(k, sum);
	}
	
	public void trimKeys(double epsilon){
		List<Integer> keys = new ArrayList<Integer>(this.keySet());
		for(int i = 0; i < keys.size(); i++){
			int key = keys.get(i);
			if(this.get(key) <= epsilon){
				this.keySet().remove(key);
			}
		}
	}
	
	public void set(int k, double v) {
		this.put(k, v);
	}

	public void multiply(int k, double f) {
		double product = this.get(k) * f;
		this.put(k, product);
	}
	
	public Counter multiplyImmutable(double f) {
		Counter other = new Counter();
		for(int k: this.keySet()){
			double product = this.get(k) * f;
			other.put(k, product);
		}
		return other;
	}

	public void multiply(double f) {
		for (int k : this.keySet()) {
			this.put(k, f*this.get(k));
		}
	}
	
	public void orthogonalize(Counter orthog){
		double factor = 0;
//		factor = -this.dot(orthog)/(orthog.norm());
//		System.out.println(factor);
		Counter orthogNormed = orthog.multiplyImmutable(1.0/orthog.norm());
		factor = this.dot(orthogNormed);
		orthogNormed.multiply(-1.0*factor);
		this.addAll(orthogNormed);
//		System.out.println(this.dot(orthog));
	}
	
	public void projectLinear(Counter orthog, double kappa){
		double factor = 0;
//		factor = -this.dot(orthog)/(orthog.norm());
//		System.out.println(factor);
		Counter orthogNormed = orthog.multiplyImmutable(1.0/orthog.norm());
		factor = this.dot(orthogNormed);
		if(factor < kappa){
			orthogNormed.multiply(factor);
			this.addAll(orthogNormed);		
		}
//		System.out.println(this.dot(orthog));
	}

	public double dot(List<Double> vector) {
		double sum = 0;
		for (int k : this.keySet()) {
			sum += this.get(k) * vector.get(k);
		}
		return sum;
	}

	public double dot(Counter vector) {
		double sum = 0;
		for (int k : this.keySet()) {
			sum += this.get(k) * vector.get(k);
		}
		return sum;
	}

	public double dist(Counter vector) {
		double sum = 0;
		for (int k : this.keySet()) {
			sum += (this.get(k) - vector.get(k))
					* (this.get(k) - vector.get(k));
		}
		return sum;
	}

	public void addAll(Counter other) {
		for (int k : other.keySet()) {
			this.add(k, other.get(k));
		}
//		Set<Integer> keyUnion = new HashSet<Integer>(this.concreteKeySet());
//		keyUnion.addAll(other.concreteKeySet());
//		for(int k: keyUnion){
//			this.add(k, other.get(k));
//		}
	}

	public Double get(int k) {
		Double v = counts.get(k);
		return (((v == null) && !counts.containsKey(k)) ? 0 : v);
	}

	public Double getPath(int k) {
		Double v = counts.get(k);
		return (((v == null) && !counts.containsKey(k)) ? Double.MAX_VALUE : v);
	}

	public void put(int k, double v) {
		counts.put(k, v);
	}

	public Set<Integer> keySet() {
//		Set<Integer> ks = new HashSet<Integer>(counts.keySet());
//		return ks;
		return counts.keySet();
	}
	
	public Set<Integer> concreteKeySet() {
		Set<Integer> ks = new HashSet<Integer>(counts.keySet());
		return ks;
//		return counts.keySet();
	}

	public void remove(int k) {
		if (counts.containsKey(k)) {
			counts.remove(k);
		}
	}

	public int size() {
		return counts.size();
	}

	public boolean containsKey(int k) {
		return counts.containsKey(k);
	}

	public String toString() {
		String myStr = "";
		if (counts.isEmpty()) {
			return "-";
		}
		for (int k : this.keySet()) {
			myStr += "(" + k + "," + this.get(k) + ") ";
		}
		return myStr;
	}
	
	public String toStringValues() {
		String myStr = "";
		if (counts.isEmpty()) {
			return "-";
		}
		for (int k : this.keySet()) {
			myStr += this.get(k)+" ";
		}
		return myStr;
	}

	public static void main(String[] args) {
		System.out.println("we compiled");
		Counter c = new Counter();
		c.add(1, 3);
		c.add(1, 5);
		Counter cc = new Counter(c);
		cc.add(1, 1);
		System.out.println(c.get(1) + " " + cc.get(1) + " " + c.size());
	}

}