/*
 * SparseMatrix class, HashMap of Counters
 * No fixed size or sparsity structure (except rowDim and colDim arguments for indexing). numRows or numCols evaluated strictly
 */
import java.util.*;

public class SparseMatrix {
	private HashMap<Integer,Counter> mat;
	private HashSet<Integer> rows;
	private HashSet<Integer> cols;
	private int rowDim;
	private int colDim;

	SparseMatrix(int rowD, int colD){
		rowDim = rowD;
		colDim = colD;
		mat = new HashMap<Integer,Counter>();
		rows = new HashSet<Integer>();
		cols = new HashSet<Integer>();
	}
	
	SparseMatrix(SparseMatrix orig){
		mat = new HashMap<Integer,Counter>();
		for(int k:  orig.mat.keySet()){
			mat.put(k, new Counter(orig.mat.get(k)));
		}
		rows = orig.rows;
		cols = orig.cols;
		rowDim = orig.rowDim;
		colDim = orig.colDim;
	}
	
	public int getNumRows(){
		return rows.size();
	}
	
	public int getNumCols(){
		return cols.size();
	}
	
	public int cardinality(){
		int sum = 0;
		for(int r: rows){
//			System.out.print(this.getRow(r).size()+" ");
//			System.out.println(this.getRow(r));
			sum += this.getRow(r).size();
		}
		return sum;
	}
	
	public HashSet<Integer> getRows(){
		return rows;
	}
	
	public HashSet<Integer> getCols(){
		return cols;
	}

	public void add(int r, int c, double v){
		Counter newRow = new Counter();
		newRow.add(c, v);
		this.addRow(r, newRow);
		rows.add(r);
		cols.add(c);
	}
	
	public boolean hasRow(int r){
		return mat.containsKey(r);
	}
	
	public void addRow(int r, Counter other){
//		System.out.println("MSG: added row "+r);
		Counter row = this.getRow(r);
		if(row.isEmpty()){
			mat.put(r,row);
			rows.add(r);
		}
		for(int c: other.keySet()){
			cols.add(c);
		}
		row.addAll(other);
	}
	
	public void set(int r, int c, double v){
		Counter row = this.getRow(r);
		if(row.isEmpty()){
			mat.put(r, row);
		}
		row.put(c, v);
		rows.add(r);
		cols.add(c);
	}
	
	public double get(int r, int c){
		Counter row = this.getRow(r);

		double v = row.get(c);
		return v;
	}
	
	public double getPath(int r, int c){
		Counter row = this.getRow(r);

		double v = row.getPath(c);
		return v;
	}
	
	public Counter getRow(int r){
		Counter row = mat.get(r);
		return (((row == null) && !mat.containsKey(r)) ? new Counter() : row);
	}
	
	public Counter getCol(int c){
		Counter col = new Counter();
		for(int r: rows){
			if(this.getRow(r).containsKey(c)){
				col.put(r, this.get(r, c));
			}
		}
		return col;
	}
	
	public boolean isEmpty(){
		return this.cardinality() == 0;
	}
	
	public SparseMatrix transpose(){
		SparseMatrix transp = new SparseMatrix(this.rowDim,this.colDim);
		for(int r: rows){
			Counter row = this.getRow(r);
			for(int c: row.keySet()){
				double v = row.get(c);
				transp.set(c,r,v);
			}
		}
		return transp;
	}
	
	public SparseMatrix symmetric(){
		SparseMatrix symm = new SparseMatrix(this);
		symm = symm.add(symm.transpose());
		for(int r: rows){
			double v = this.get(r,r);
			this.set(r, r, v/2.0);
		}
		return symm;
	}
	
	public SparseMatrix add(SparseMatrix other){
		SparseMatrix sum = new SparseMatrix(this.rowDim,this.colDim);
		sum = new SparseMatrix(this);
		for(int r: other.rows){
			sum.addRow(r, other.getRow(r));
		}
		return sum;
	}
	
	public List<Double> multiply(List<Double> vector){
		List<Double> resVec = new ArrayList<Double>();
		for(int i = 0; i < rowDim; i++){
			resVec.add(0.0);
		}
		for(int r: rows){
			if(r%(1024*32) == 0) System.out.println(r +" of "+rowDim);
			resVec.set(r, this.getRow(r).dot(vector));
		}
		return resVec;
	}
	
	public List<Double> multiply(Counter vector){
		List<Double> resVec = new ArrayList<Double>();
		for(int i = 0; i < rowDim; i++){
			resVec.add(0.0);
		}
		for(int r: rows){
			resVec.set(r, this.getRow(r).dot(vector));
		}
		return resVec;
	}
	
	
	
	public Counter bfs(int initNode){
		Counter shortestPaths = new Counter();
		Counter currentNodes = new Counter();
		Counter newNodes = new Counter();
		shortestPaths.add(initNode, 0);
		newNodes.add(initNode, 0);
		while(!newNodes.isEmpty()){
			currentNodes = new Counter(newNodes);
			newNodes = new Counter();
			for(int r: currentNodes.keySet()){
				Counter row = this.getRow(r);
				for(int c: row.keySet()){
					//Weighted case: later paths can be shorter than first path
					double pathLength = currentNodes.get(r)+row.get(c);
					if(pathLength < shortestPaths.getPath(c)){
						newNodes.put(c,pathLength);
						shortestPaths.put(c,pathLength);
					}
				}
			}
		}
		return shortestPaths;
	}
	
	public Counter harmonicAvg(Counter vector){
		Counter currentVec = new Counter();
		Counter newVec = new Counter(vector);
		double dist = currentVec.dist(newVec);
		while(dist > 0.00001){
			currentVec = new Counter(newVec);
			dist = currentVec.dist(newVec);
		}
		return currentVec;
	}
	
	public SparseMatrix multiply(SparseMatrix other){
		SparseMatrix mult = new SparseMatrix(this.rowDim,other.colDim);
		for(int r: rows){
//			System.out.println("multiplying row: "+ r);
			Counter row = this.getRow(r);
//			for(int c: other.cols){
//				Counter col = other.getCol(c);
//				double dotProd = row.dot(col);
//				System.out.println(row.toString()+" "+col.toString()+" "+dotProd);
//				mult.set(r, c, dotProd);
//			}
			for(int c: row.keySet()){
				Counter oRow = other.getRow(c);
				for(int oc: oRow.keySet()){
					mult.add(r,oc,row.get(c)*oRow.get(oc));
				}
			}
		}
		return mult;
	}
	
	/*
	 * Matrix mult but with min-plus, and iterative.
	 * Each min-plus operation that changes the path inserts it into a new queue
	 */
	public SparseMatrix apsp(){
		SparseMatrix shortestPaths = new SparseMatrix(this);
		SparseMatrix currentPairs = new SparseMatrix(this.rowDim, this.colDim);
		SparseMatrix newPairs = new SparseMatrix(this.rowDim, this.colDim);
		newPairs = new SparseMatrix(this);
		for(int d = 0; d < this.rowDim; d++){
			shortestPaths.set(d, d, 0.0);
		}
		for(int d = 0; d < this.rowDim; d++){
			newPairs.set(d, d, 0.0);
		}
		while(!newPairs.isEmpty()){
			currentPairs = new SparseMatrix(newPairs);
			newPairs = new SparseMatrix(this.rowDim, this.colDim);
			for(int r: currentPairs.rows){
				Counter row = currentPairs.getRow(r);
				for(int c: row.keySet()){
					Counter oRow = this.getRow(c);
					for(int oc: oRow.keySet()){
						double pathLength = currentPairs.get(r,c)+oRow.get(oc);
						if(pathLength < shortestPaths.getPath(r,oc)){
							newPairs.set(r,oc,pathLength);
							shortestPaths.set(r,oc,pathLength);
						}
					}
				}
			}
		}
		return shortestPaths;
	}
	
	/*
	 * Takes a set of sketch nodes, and returns an ArrayList<Integer>
	 * such that arr.get(i) gives the index of the sketch node that
	 * node i is closest too.
	 * 
	 * Need to work the return values a little bit. Make a proper data structure.
	 */
	public ArrayList<ArrayList<Integer>> distSketch(int len, Counter sketchNodes){
		ArrayList<Integer> closestIndex = new ArrayList<Integer>();
		for(int i = 0; i < len; i++) closestIndex.set(i, -1);
		ArrayList<Double> closestDist = new ArrayList<Double>();
		for(int i = 0; i < len; i++) closestDist.set(i, Double.MAX_VALUE);
		ArrayList<ArrayList<Integer>> sketchReverseIndex = new ArrayList<ArrayList<Integer>>();
		for(int index: sketchNodes.keySet()){
			Counter distances = this.bfs(index);
			for(int j = 0; j < len; j++){
				double curDist = closestDist.get(j);
				double dist = distances.getPath(index);
				if(dist < curDist){
					closestIndex.set(j, index);
				}
			}
			sketchReverseIndex.add(new ArrayList<Integer>());
		}
		for(int j = 0; j< len; j++){
			int closest = closestIndex.get(j);
			sketchReverseIndex.get(closest).add(j);
		}
		//Return sketchReverseIndex, closestIndex forward index, and index correspondence bimap
		return sketchReverseIndex;
	}
	
	public SparseMatrix getRedundant(){
		SparseMatrix redMat = new SparseMatrix(this).multiply(this);
		int oldCard = 0;
		int card = redMat.cardinality();
		int i = 0;
//		for(int r = 0; r < rowDim; r++){
//			if(true){//transMat.hasRow(r)){
//				transMat.add(r, r, 1.0);
//			}
//			if(true){//tcMat.hasRow(r)){
//				tcMat.add(r, r, 1.0);
//			}
//		}
		while(card > oldCard){
//			System.out.println("iter: "+i);
			i++;
			oldCard = card;
			redMat = redMat.add(redMat.multiply(this));
			card = redMat.cardinality();
//			System.out.println(tcMat.toString());
		}
		return redMat;
	}
	
	public SparseMatrix minimize(){
		SparseMatrix minMat = new SparseMatrix(this);
		SparseMatrix redundant = this.getRedundant();
		minMat.removeEntries(redundant);
		return minMat;
	}
	
	public void remove(int r, int c){
		if(hasRow(r)){
			this.getRow(r).remove(c);
		}
	}
	
	public void removeEntries(SparseMatrix redundant){
		for(int r: redundant.getRows()){
			Counter row = redundant.getRow(r);
			for(int c: row.keySet()){
				this.remove(r, c);
			}
		}
	}
	
	public SparseMatrix transitiveClosure(){
		SparseMatrix tcMat = new SparseMatrix(this);
		SparseMatrix transMat = new SparseMatrix(this);
		int oldCard = 0;
		int card = tcMat.cardinality();
		int i = 0;
//		for(int r = 0; r < rowDim; r++){
//			if(true){//transMat.hasRow(r)){
//				transMat.add(r, r, 1.0);
//			}
//			if(true){//tcMat.hasRow(r)){
//				tcMat.add(r, r, 1.0);
//			}
//		}
		while(card > oldCard){
//			System.out.println("iter: "+i);
			i++;
			oldCard = card;
			tcMat = tcMat.add(tcMat.multiply(transMat));
			card = tcMat.cardinality();
//			System.out.println(tcMat.toString());
		}
		return tcMat;
	}
	
	public String toString(){
		String myStr = "";
		for(int r: rows){
			myStr += r+": "+this.getRow(r).toString() + "\r\n";
		}
		return myStr;
	}
	
	public static void main(String[] args){
		long start;
		long end;
		int card;
		double seconds;
		double megaTEPs;
		System.out.println("Working");
		Random rand = new Random();
		int dim = 10000;
		int num = 200000;
		SparseMatrix testMat = new SparseMatrix(dim,dim);
		for(int i= 0; i < num; i++){
			if((i%(1024*32*20)==0)) System.out.println(i +" of "+num);
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
			testMat.add(r,c,1.0);
		}
		SparseMatrix testMat2 = new SparseMatrix(dim,dim);
		for(int i= 0; i < num; i++){
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
			testMat2.add(r,c,1.0);
		}
		start = System.currentTimeMillis();
		SparseMatrix testMat3 = testMat.multiply(testMat2);
		end = System.currentTimeMillis();
		System.out.println("Time: "+(end-start));
		System.out.println(testMat.cardinality()+" "+testMat.add(testMat2).cardinality()+" "+testMat3.cardinality());

//		start = System.currentTimeMillis();
//		SparseMatrix apspRes = new SparseMatrix(testMat.rowDim, testMat.colDim);
//		int T = 1;
//		for(int iter = 0; iter < T; iter++){
//			apspRes = testMat.apsp();
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Time: "+(end-start));
//		card = testMat.cardinality();
//		seconds = (double)(end-start)/1000.0;
//		megaTEPs = card/seconds*dim/1000000.0;
//		System.out.println("megaTEPs: "+megaTEPs);
//		System.out.println(testMat.toString());
//		System.out.println(apspRes.toString());
		
//		start = System.currentTimeMillis();
//		Counter bfsRes = new Counter();
//		int iterMax = dim;
//		for(int iter = 0; iter < iterMax; iter++){
//			int init = iter;
//			bfsRes = testMat.bfs(init);
////			System.out.println(bfsRes.toString());
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Time: "+(end-start));
//		card = testMat.cardinality();
//		seconds = (double)(end-start)/1000.0;
//		megaTEPs = card/seconds*iterMax/1000000.0;
//		System.out.println("megaTEPs: "+megaTEPs);
//		System.out.println(init);
//		System.out.println(testMat.toString());
//		System.out.println(bfsRes.toString());
		
//		ArrayList<Double> vector = new ArrayList<Double>();
//		for(int i = 0; i < dim; i++){
//			vector.add(1.0);
//		}
//		ArrayList<Double> oldVector = new ArrayList<Double>(vector);
//		vector = (ArrayList<Double>) testMat.multiply(vector);
//		double diff = 1;
//		int t = 0;
//		start = System.currentTimeMillis();
//		while(diff > 0.00000001 && (diff < 3.99999 || diff > 4.00001)){
//			t += 1;
//			vector = (ArrayList<Double>) testMat.multiply(vector);
//			double norm = 0;
//			for(int i = 0; i < dim; i++){
//				norm += vector.get(i)*vector.get(i);
//			}
//			norm = Math.sqrt(norm);
//			for(int i = 0; i < dim; i++){
//				vector.set(i, vector.get(i)/Math.max(norm,1.0));
//			}
//			diff = 0;
//			for(int i = 0; i < dim; i++){
//				diff += (oldVector.get(i)-vector.get(i))*(oldVector.get(i)-vector.get(i));
//			}
//			System.out.println(diff+" "+norm);
////			System.out.println(vector.toString());
////			System.out.println(oldVector.toString());
//			oldVector = new ArrayList<Double>(vector);
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Time: "+(end-start));
		
//		SparseMatrix tcMin = testMat.minimize().transitiveClosure();
//		SparseMatrix tcMat = testMat.transitiveClosure();
//		System.out.println(tcMin.cardinality()+" "+tcMat.cardinality());
//		testMat.add(0,0,1.0);
//		testMat.add(0,1,1.0);
//		testMat.add(1,1,1.0);
//		testMat.add(1,2,1.0);
//		testMat.add(2,2,1.0);
//		System.out.println(testMat.cardinality());
//		for(int t = 0; t < 1000; t++){
//			testMat = testMat.multiply(testMat);
//			System.out.println(testMat.cardinality());
//		}
//		System.out.println(testMat.toString());
	}
	
}
