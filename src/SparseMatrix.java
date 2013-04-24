/*
 * SparseMatrix class, HashMap of Counters
 * No fixed size or sparsity structure (except rowDim and colDim arguments for indexing). numRows or numCols evaluated strictly
 */
import java.util.*;

public class SparseMatrix {
	private HashMap<Integer, Counter> mat;
	private HashSet<Integer> rows;
	private HashSet<Integer> cols;
	private int rowDim;
	private int colDim;

	SparseMatrix(int rowD, int colD) {
		rowDim = rowD;
		colDim = colD;
		mat = new HashMap<Integer, Counter>();
		rows = new HashSet<Integer>();
		cols = new HashSet<Integer>();
	}

	SparseMatrix(SparseMatrix orig) {
		mat = new HashMap<Integer, Counter>();
		for (int k : orig.mat.keySet()) {
			mat.put(k, new Counter(orig.mat.get(k)));
		}
		rows = orig.rows;
		cols = orig.cols;
		rowDim = orig.rowDim;
		colDim = orig.colDim;
	}

	public int getNumRows() {
		return rows.size();
	}

	public int getNumCols() {
		return cols.size();
	}

	public int cardinality() {
		int sum = 0;
		for (int r : rows) {
			// System.out.print(this.getRow(r).size()+" ");
			// System.out.println(this.getRow(r));
			sum += this.getRow(r).size();
		}
		return sum;
	}

	public HashSet<Integer> getRows() {
		return rows;
	}

	public HashSet<Integer> getCols() {
		return cols;
	}
	
	public SparseMatrix stochasticizeCols(){
		SparseMatrix stochasticMat = new SparseMatrix(this.rowDim,this.colDim);
		double[] columnSums = new double[this.colDim];
		for(int r: this.rows){
			Counter row = this.getRow(r);
			for(int c: row.keySet()){
				columnSums[c] += row.get(c);
			}
		}for(int r: this.rows){
			Counter row = this.getRow(r);
			for(int c: row.keySet()){
				double value = 0;
				if(columnSums[c] != 0){
//				if(true){
					value = this.get(r, c)/columnSums[c];
				}
				stochasticMat.set(r, c, value);
			}
		}
		return stochasticMat;
	}
	
	public SparseMatrix stochasticizeRows(){
		SparseMatrix stochasticMat = new SparseMatrix(this.rowDim,this.colDim);
		double[] rowSums = new double[this.rowDim];
		for(int r: this.rows){
			Counter row = this.getRow(r);
			for(int c: row.keySet()){
				rowSums[r] += row.get(c);
			}
		}for(int r: this.rows){
			Counter row = this.getRow(r);
			for(int c: row.keySet()){
				double value = 0;
				if(rowSums[r] != 0){
//				if(true){
					value = this.get(r, c)/rowSums[r];
				}
				stochasticMat.set(r, c, value);
			}
		}
		return stochasticMat;
	}
	
	public SparseMatrix makeLaplacian(){
		SparseMatrix laplacian = new SparseMatrix(this.rowDim, this.colDim);
		for(int r: this.getRows()){
			Counter row = this.getRow(r);
			laplacian.set(r, r, row.sum());
			for(int c: row.keySet()){
				laplacian.set(r, c, -1*row.get(c));
			}
		}
		return laplacian;
	}
	
	public static Counter PersonalizedPageRank(Counter seedVector, SparseMatrix transitionMat){
		double trimEps = 0.0;
		int iterLimit = 200;
		transitionMat = transitionMat.stochasticizeRows();
		double beta = 0.85;
		long start; long end; 
		Counter vector = new Counter(seedVector);
		double svCard = 0;
		for(int i: seedVector.keySet()){
			svCard += seedVector.get(i);
		}
		Counter oldVector = new Counter(vector);
		vector = transitionMat.multiply(vector);
		double diff = 1;
		int t = 0;
		start = System.currentTimeMillis();
		while(diff > Math.pow(10.0, -10.0) && (diff < 3.99999 || diff > 4.00001) && t < iterLimit){
			t += 1;
			vector.trimKeys(trimEps);
			vector = transitionMat.multiply(vector);
			Set<Integer> vecSeedUnion = vector.concreteKeySet();
			vecSeedUnion.addAll(seedVector.concreteKeySet());
			for(int i: vecSeedUnion){
//				vector.set(i, beta*vector.get(i)/norm+(1-beta)*seedVector.get(i));///Math.max(norm,1.0));
				vector.set(i, beta*vector.get(i)+(1-beta)*seedVector.get(i)/svCard);///Math.max(norm,1.0));
			}
			double norm = 0;
			for(int i: vector.keySet()){
//				norm += vector.get(i)*vector.get(i);
//				norm += Math.abs(vector.get(i));
				norm += vector.get(i);
			}
//			norm = Math.sqrt(norm);
			diff = 0;
			Set<Integer> vecOldUnion = vector.concreteKeySet();
			vecOldUnion.addAll(oldVector.concreteKeySet());
			for(int i: vecOldUnion){
				diff +=(oldVector.get(i)-vector.get(i))*(oldVector.get(i)-vector.get(i));
			}
			System.out.println(diff+" "+norm);
//			System.out.println(vector.toString());
			// System.out.println(oldVector.toString());
			oldVector = new Counter(vector);
		}
//		System.out.println(transitionMat.toStringValues());
		end = System.currentTimeMillis();
		System.out.println("Time: "+(end-start)+" iterations: "+t);
		return vector;
	}
	
	public static Counter TopEig(SparseMatrix mat){
		double trimEps = 0.0;
		int iterLimit = 1000;
		long start; long end; 
		Counter vector = new Counter();
		int vecLen = mat.colDim;
//		for(int i = 0; i < vecLen; i++){
//			vector.add(i, Math.random()-0.5);
//		}
		for(int i = 0; i < vecLen; i++){
			vector.add(i, 1.0/(double)vecLen);
		}
		Counter oldVector = new Counter(vector);
		vector = mat.multiply(vector);
		double diff = 1;
		int t = 0;
		start = System.currentTimeMillis();
		while(diff > Math.pow(10.0, -10.0) && (diff < 3.99999 || diff > 4.00001) && t < iterLimit){
			t += 1;
			vector.trimKeys(trimEps);
			vector = mat.multiply(vector);
			double norm = 0;
			for(int i: vector.keySet()){
				norm += vector.get(i)*vector.get(i);
//				norm += Math.abs(vector.get(i));
			}
			norm = Math.sqrt(norm);
			vector.multiply(1.0/norm);
			diff = 0;
			Set<Integer> vecOldUnion = vector.concreteKeySet();
			vecOldUnion.addAll(oldVector.concreteKeySet());
			for(int i: vecOldUnion){
				diff +=(oldVector.get(i)-vector.get(i))*(oldVector.get(i)-vector.get(i));
			}
			System.out.println(diff+" "+norm);
//			System.out.println(vector.toString());
			// System.out.println(oldVector.toString());
			oldVector = new Counter(vector);
		}
//		System.out.println(mat.toStringValues());
		end = System.currentTimeMillis();
		System.out.println("Time: "+(end-start)+" iterations: "+t);
		return vector;
	}
	
	public static Counter ConstrainedEig(SparseMatrix mat, SparseMatrix orthogMat){
		double trimEps = 0;
		int iterLimit = 10000;
		long start; long end; 
		Counter vector = new Counter();
		int vecLen = mat.colDim;
		for(int i = 0; i < vecLen; i++){
//			vector.add(i, 1.0/Math.sqrt(vecLen));
			vector.add(i, Math.random()-0.5);
		}
		Counter oldVector = new Counter();
//		vector = mat.multiply(vector);
//		for(int r: orthogMat.rows){
//			Counter orthogRow = orthogMat.getRow(r);
//			vector.orthogonalize(orthogRow);
////			System.out.println(vector.dot(orthogRow));
//		}
		double norm = 0;
		double sim = 0;
		double diff = 1.0;
		double diffNeg = 1.0;
		int t = 0;
		start = System.currentTimeMillis();
		while(diff > Math.pow(10.0, -16.0) && diffNeg > Math.pow(10.0,-16.0) && t < iterLimit){
			t += 1;
//			vector.trimKeys(trimEps);
			oldVector = new Counter(vector);
			vector = mat.multiply(vector);
			for(int r: orthogMat.rows){
				Counter orthogRow = orthogMat.getRow(r);
//				System.out.println("before: "+vector.dot(orthogRow));
				vector.orthogonalize(orthogRow);
//				System.out.println("after: "+vector.dot(orthogRow));
			}
			norm = 0;
			norm = vector.norm();
			vector.multiply(1.0/norm);
			diff = 0;
			diffNeg = 0;
			Set<Integer> vecOldUnion = vector.concreteKeySet();
			vecOldUnion.addAll(oldVector.concreteKeySet());
			for(int i: vecOldUnion){
//			for(int i = 0; i < mat.colDim; i++){
				diff +=(oldVector.get(i)-vector.get(i))*(oldVector.get(i)-vector.get(i));
				diffNeg +=(oldVector.get(i)+vector.get(i))*(oldVector.get(i)+vector.get(i));
			}
			sim = vector.dot(oldVector);
//			System.out.println(diff+" "+diffNeg+" "+sim+" "+norm+" "+vector.dot(orthogMat.getRow(0)));
//			System.out.println(vector.toString());
			// System.out.println(oldVector.toString());
		}
		System.out.println(norm+" "+orthogMat.rows.size()+" "+sim);
//		System.out.println(mat.toStringValues());
		end = System.currentTimeMillis();
		System.out.println("Time: "+(end-start)+" iterations: "+t);
		return vector;
	}

	public void add(int r, int c, double v) {
		Counter newRow = new Counter();
		newRow.add(c, v);
		this.addRow(r, newRow);
		rows.add(r);
		cols.add(c);
	}

	public boolean hasRow(int r) {
		return mat.containsKey(r);
	}

	public void addRow(int r, Counter other) {
		// System.out.println("MSG: added row "+r);
		Counter row = this.getRow(r);
		if (row.isEmpty()) {
			mat.put(r, row);
			rows.add(r);
		}
		for (int c : other.keySet()) {
			cols.add(c);
		}
		row.addAll(other);
	}

	public void set(int r, int c, double v) {
		Counter row = this.getRow(r);
		if (row.isEmpty()) {
			mat.put(r, row);
		}
		row.put(c, v);
		rows.add(r);
		cols.add(c);
	}

	public double get(int r, int c) {
		Counter row = this.getRow(r);

		double v = row.get(c);
		return v;
	}

	public double getPath(int r, int c) {
		Counter row = this.getRow(r);

		double v = row.getPath(c);
		return v;
	}

	public Counter getRow(int r) {
		Counter row = mat.get(r);
		return (((row == null) && !mat.containsKey(r)) ? new Counter() : row);
	}

	public Counter getCol(int c) {
		Counter col = new Counter();
		for (int r : rows) {
			if (this.getRow(r).containsKey(c)) {
				col.put(r, this.get(r, c));
			}
		}
		return col;
	}

	public boolean isEmpty() {
		return this.cardinality() == 0;
	}

	public SparseMatrix transpose() {
		SparseMatrix transp = new SparseMatrix(this.rowDim, this.colDim);
		for (int r : rows) {
			Counter row = this.getRow(r);
			for (int c : row.keySet()) {
				double v = row.get(c);
				transp.set(c, r, v);
			}
		}
		return transp;
	}

	public SparseMatrix symmetric() {
		SparseMatrix symm = new SparseMatrix(this);
		symm = symm.add(symm.transpose());
		for (int r : rows) {
			double v = this.get(r, r);
			this.set(r, r, v / 2.0);
		}
		return symm;
	}

	public SparseMatrix add(SparseMatrix other) {
		SparseMatrix sum = new SparseMatrix(this.rowDim, this.colDim);
		sum = new SparseMatrix(this);
		for (int r : other.rows) {
			sum.addRow(r, other.getRow(r));
		}
		return sum;
	}
	
	public SparseMatrix multiply(double f){
		SparseMatrix multMat = new SparseMatrix(this.rowDim, this.colDim);
		for(int r: this.rows){
			Counter row = this.getRow(r);
			multMat.addRow(r, row.multiplyImmutable(f));
		}
		return multMat;
	}

	public List<Double> multiply(List<Double> vector) {
		List<Double> resVec = new ArrayList<Double>();
		for (int i = 0; i < rowDim; i++) {
			resVec.add(0.0);
		}
		for (int r : rows) {
//			if (r % (1024 * 32) == 0)
//				System.out.println(r + " of " + rowDim);
			resVec.set(r, this.getRow(r).dot(vector));
		}
		return resVec;
	}

	public Counter multiply(Counter vector) {
		Counter resVec = new Counter();

//		for(int r: vector.keySet()){
//			Counter row = this.getRow(r);
//			double sum = 0;
//			for(int c: row.keySet()){
////				resVec.add(r, row.get(c)*vector.get(c));
//				sum += row.get(c)*vector.get(c);
//			}
//			resVec.add(r, sum);
//		}
		
		for (int r: rows) {
			resVec.set(r, this.getRow(r).dot(vector));
		}
		return resVec;
	}

	public Counter bfs(int initNode) {
		Counter shortestPaths = new Counter();
		Counter currentNodes = new Counter();
		Counter newNodes = new Counter();
		shortestPaths.add(initNode, 0);
		newNodes.add(initNode, 0);
		while (!newNodes.isEmpty()) {
			currentNodes = new Counter(newNodes);
			newNodes = new Counter();
			for (int r : currentNodes.keySet()) {
				Counter row = this.getRow(r);
				for (int c : row.keySet()) {
					// Weighted case: later paths can be shorter than first path
					double pathLength = currentNodes.get(r) + row.get(c);
					if (pathLength < shortestPaths.getPath(c)) {
						newNodes.put(c, pathLength);
						shortestPaths.put(c, pathLength);
					}
				}
			}
		}
		return shortestPaths;
	}
	
	/*
	 * Should return path from initial node to terminal;
	 * Means getting new path-backtracker
	 */
	public Counter bfs(int initNode, int terminalNode) {
		Counter shortestPaths = new Counter();
		Counter currentNodes = new Counter();
		Counter newNodes = new Counter();
		shortestPaths.add(initNode, 0);
		newNodes.add(initNode, 0);
		while (!newNodes.isEmpty()) {
			currentNodes = new Counter(newNodes);
			newNodes = new Counter();
			for (int r : currentNodes.keySet()) {
				Counter row = this.getRow(r);
				for (int c : row.keySet()) {
					// Weighted case: later paths can be shorter than first path
					double pathLength = currentNodes.get(r) + row.get(c);
					if (pathLength < shortestPaths.getPath(c)) {
						newNodes.put(c, pathLength);
						shortestPaths.put(c, pathLength);
					}
				}
			}
			if(shortestPaths.getPath(terminalNode) < Double.MAX_VALUE/2.0){
				break;
			}
		}
		return shortestPaths;
	}

	public Counter harmonicAvg(Counter vector) {
		Counter currentVec = new Counter();
		Counter newVec = new Counter(vector);
		double dist = currentVec.dist(newVec);
		while (dist > 0.00001) {
			currentVec = new Counter(newVec);
			dist = currentVec.dist(newVec);
		}
		return currentVec;
	}

	public SparseMatrix multiply(SparseMatrix other) {
		SparseMatrix mult = new SparseMatrix(this.rowDim, other.colDim);
		for (int r : rows) {
			// System.out.println("multiplying row: "+ r);
			Counter row = this.getRow(r);
			// for(int c: other.cols){
			// Counter col = other.getCol(c);
			// double dotProd = row.dot(col);
			// System.out.println(row.toString()+" "+col.toString()+" "+dotProd);
			// mult.set(r, c, dotProd);
			// }
			for (int c : row.keySet()) {
				Counter oRow = other.getRow(c);
				for (int oc : oRow.keySet()) {
					mult.add(r, oc, row.get(c) * oRow.get(oc));
				}
			}
		}
		return mult;
	}

	/*
	 * Matrix mult but with min-plus, and iterative. Each min-plus operation
	 * that changes the path inserts it into a new queue
	 */
	public SparseMatrix apsp() {
		SparseMatrix shortestPaths = new SparseMatrix(this);
		SparseMatrix currentPairs = new SparseMatrix(this.rowDim, this.colDim);
		SparseMatrix newPairs = new SparseMatrix(this.rowDim, this.colDim);
		newPairs = new SparseMatrix(this);
		for (int d = 0; d < this.rowDim; d++) {
			shortestPaths.set(d, d, 0.0);
		}
		for (int d = 0; d < this.rowDim; d++) {
			newPairs.set(d, d, 0.0);
		}
		while (!newPairs.isEmpty()) {
			currentPairs = new SparseMatrix(newPairs);
			newPairs = new SparseMatrix(this.rowDim, this.colDim);
			for (int r : currentPairs.rows) {
				Counter row = currentPairs.getRow(r);
				for (int c : row.keySet()) {
					Counter oRow = this.getRow(c);
					for (int oc : oRow.keySet()) {
						double pathLength = currentPairs.get(r, c)
								+ oRow.get(oc);
						if (pathLength < shortestPaths.getPath(r, oc)) {
							newPairs.set(r, oc, pathLength);
							shortestPaths.set(r, oc, pathLength);
						}
					}
				}
			}
		}
		return shortestPaths;
	}

	/*
	 * Takes a set of sketch nodes, and returns an ArrayList<Integer> such that
	 * arr.get(i) gives the index of the sketch node that node i is closest too.
	 * 
	 * Need to work the return values a little bit. Make a proper data
	 * structure.
	 */
	public ArrayList<ArrayList<Integer>> distSketch(int len, Counter sketchNodes) {
		ArrayList<Integer> closestIndex = new ArrayList<Integer>();
		for (int i = 0; i < len; i++)
			closestIndex.set(i, -1);
		ArrayList<Double> closestDist = new ArrayList<Double>();
		for (int i = 0; i < len; i++)
			closestDist.set(i, Double.MAX_VALUE);
		ArrayList<ArrayList<Integer>> sketchReverseIndex = new ArrayList<ArrayList<Integer>>();
		for (int index : sketchNodes.keySet()) {
			Counter distances = this.bfs(index);
			for (int j = 0; j < len; j++) {
				double curDist = closestDist.get(j);
				double dist = distances.getPath(index);
				if (dist < curDist) {
					closestIndex.set(j, index);
				}
			}
			sketchReverseIndex.add(new ArrayList<Integer>());
		}
		for (int j = 0; j < len; j++) {
			int closest = closestIndex.get(j);
			sketchReverseIndex.get(closest).add(j);
		}
		// Return sketchReverseIndex, closestIndex forward index, and index
		// correspondence bimap
		return sketchReverseIndex;
	}

	public SparseMatrix getRedundant() {
		SparseMatrix redMat = new SparseMatrix(this).multiply(this);
		int oldCard = 0;
		int card = redMat.cardinality();
		int i = 0;
		// for(int r = 0; r < rowDim; r++){
		// if(true){//transMat.hasRow(r)){
		// transMat.add(r, r, 1.0);
		// }
		// if(true){//tcMat.hasRow(r)){
		// tcMat.add(r, r, 1.0);
		// }
		// }
		while (card > oldCard) {
			// System.out.println("iter: "+i);
			i++;
			oldCard = card;
			redMat = redMat.add(redMat.multiply(this));
			card = redMat.cardinality();
			// System.out.println(tcMat.toString());
		}
		return redMat;
	}

	public SparseMatrix minimize() {
		SparseMatrix minMat = new SparseMatrix(this);
		SparseMatrix redundant = this.getRedundant();
		minMat.removeEntries(redundant);
		return minMat;
	}

	public void remove(int r, int c) {
		if (hasRow(r)) {
			this.getRow(r).remove(c);
		}
	}

	public void removeEntries(SparseMatrix redundant) {
		for (int r : redundant.getRows()) {
			Counter row = redundant.getRow(r);
			for (int c : row.keySet()) {
				this.remove(r, c);
			}
		}
	}

	public SparseMatrix transitiveClosure() {
		SparseMatrix tcMat = new SparseMatrix(this);
		SparseMatrix transMat = new SparseMatrix(this);
		int oldCard = 0;
		int card = tcMat.cardinality();
		int i = 0;
		// for(int r = 0; r < rowDim; r++){
		// if(true){//transMat.hasRow(r)){
		// transMat.add(r, r, 1.0);
		// }
		// if(true){//tcMat.hasRow(r)){
		// tcMat.add(r, r, 1.0);
		// }
		// }
		while (card > oldCard) {
			// System.out.println("iter: "+i);
			i++;
			oldCard = card;
			tcMat = tcMat.add(tcMat.multiply(transMat));
			card = tcMat.cardinality();
			// System.out.println(tcMat.toString());
		}
		return tcMat;
	}

	public String toString() {
		String myStr = "";
		for (int r : rows) {
			myStr += r + ": " + this.getRow(r).toString() + "\r\n";
		}
		return myStr;
	}
	
	public String toStringValues(){
		String myStr = "";
		for (int r : rows) {
			myStr += this.getRow(r).toStringValues() + "\r\n";
		}
		return myStr;
	}

	public static void main(String[] args) {
		long start;
		long end;
		int card;
		double seconds;
		double megaTEPs;
		System.out.println("Working");
		Random rand = new Random();
		int dim = 16384;
		int num = dim*10;
		SparseMatrix testMat = new SparseMatrix(dim, dim);
		for (int i = 0; i < num; i++) {
			if ((i % (1024 * 32 * 20) == 0))
				System.out.println(i + " of " + num);
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
//		    int r = (i+2659)*3571%dim;
//		    int c = (i+2659)*2081%dim;
			testMat.add(r, c, 1.0);
			testMat.add(c, r, 1.0);
//			testMat.add(r, r, 1.0);
//			testMat.add(c, c, 1.0);
//			testMat.set(r, c, 1.0);
//			testMat.set(c, r, 1.0);
		}
		boolean isSymm = true;
		int sum = 0;
		for(int r = 0; r < dim; r++){
			for(int c = 0; c < dim; c++){
				double val = testMat.get(r,c);
				sum += val;
				if(val > 0.0){
					if(testMat.get(c,r) == 0){
						isSymm = false;
					}
				}
			}
		}
		System.out.println(sum+" "+testMat.cardinality()+" "+isSymm);
//		SparseMatrix testMat2 = new SparseMatrix(dim, dim);
//		for (int i = 0; i < num; i++) {
//			int r = rand.nextInt(dim);
//			int c = rand.nextInt(dim);testMat
//			testMat2.add(r, c, 1.0);
//		}
		// start = System.currentTimeMillis();
		// SparseMatrix testMat3 = testMat.multiply(testMat2);
		// end = System.currentTimeMillis();
		// System.out.println("Time: "+(end-start));
		// System.out.println(testMat.cardinality()+" "+testMat.add(testMat2).cardinality()+" "+testMat3.cardinality());

//		start = System.currentTimeMillis();
//		SparseMatrix apspRes = new SparseMatrix(testMat.rowDim, testMat.colDim);
//		int T = 1;testMat
//		for (int iter = 0; iter < T; iter++) {
//			apspRes = testMat.apsp();
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Time: " + (end - start));
//		card = testMat.cardinality();
//		seconds = (double) (end - start) / 1000.0;
//		megaTEPs = card / seconds * dim / 1000000.0;
//		System.out.println("megaTEPs: " + megaTEPs);
//		System.out.println(testMat.toString());
//		System.out.println(apspRes.toString());

		// start = System.currentTimeMillis();
		// Counter bfsRes = new Counter();
		// int iterMax = dim;
		// for(int iter = 0; iter < iterMax; iter++){
		// int init = iter;
		// bfsRes = testMat.bfs(init);
		// // System.out.println(bfsRes.toString());
		// }
		// end = System.currentTimeMillis();
		// System.out.println("Time: "+(end-start));
		// card = testMat.cardinality();
		// seconds = (double)(end-start)/1000.0;
		// megaTEPs = card/seconds*iterMax/1000000.0;
		// System.out.println("megaTEPs: "+megaTEPs);
		// System.out.println(init);
		// System.out.println(testMat.toString());
		// System.out.println(bfsRes.toString());

//		SparseMatrix testMatStoch = testMat.stochasticizeRows();
//		double beta = 0.85;
//		ArrayList<Double> vector = new ArrayList<Double>();
//		for(int i = 0; i < dim; i++){
//			vector.add(0.0);
//			if(i == 0) vector.set(i,1.0);
//		}
//		ArrayList<Double> seedVector = new ArrayList<Double>(vector);
//		double svCard = 0;
//		for(int i = 0; i < dim; i++){
//			svCard += seedVector.get(i);
//		}
//		ArrayList<Double> oldVector = new ArrayList<Double>(vector);
//		vector = (ArrayList<Double>) testMatStoch.multiply(vector);
//		double diff = 1;
//		int t = 0;
//		start = System.currentTimeMillis();
//		while(diff > Math.pow(10.0, -10.0) && (diff < 3.99999 || diff > 4.00001) && t < 200){
//			t += 1;
//			vector = (ArrayList<Double>) testMatStoch.multiply(vector);
//			double norm = 0;
//			for(int i = 0; i < dim; i++){
////				norm += vector.get(i)*vector.get(i);
//				norm += Math.abs(vector.get(i));
//			}
////			norm = Math.sqrt(norm);
//			for(int i = 0; i < dim; i++){r
////				vector.set(i, beta*vector.get(i)/norm+(1-beta)*seedVector.get(i));///Math.max(norm,1.0));
//				vector.set(i, beta*vector.get(i)+(1-beta)*seedVector.get(i));///Math.max(norm,1.0));
//			}
//			diff = 0;
//			for(int i = 0; i < dim; i++){
//				diff +=(oldVector.get(i)-vector.get(i))*(oldVector.get(i)-vector.get(i));
//			}
//			System.out.println(diff+" "+norm);
////			System.out.println(vector.toString());
//			// System.out.println(oldVector.toString());
//			oldVector = new ArrayList<Double>(vector);
//		}
////		System.out.println(testMatStoch.toStringValues());
//		end = System.currentTimeMillis();
//		System.out.println("Time: "+(end-start)+" iterations: "+t);
		
//		Counter sv = new Counter();
//		sv.set(rand.nextInt(dim),1.0);
//		sv.set(0, 1.0);
//		SparseMatrix.PersonalizedPageRank(sv, testMat);

//		SparseMatrix laplacian = testMat.makeLaplacian();
//		SparseMatrix normedLap = laplacian.stochasticizeRows();
//		for(int i = 0; i < testMat.rowDim; i++){
//			testMat.add(i,i,100.0);
//		}
//		SparseMatrix normedTest = testMat.stochasticizeRows();
//		testMat = testMat.symmetric();
		SparseMatrix orthogMat = new SparseMatrix(0,testMat.colDim);
		Counter topEig = SparseMatrix.ConstrainedEig(testMat,orthogMat);
//		Counter topEig = SparseMatrix.TopEig(testMat);
//		for(int i = 0; i < orthogMat.colDim; i++){
//			orthogMat.set(0, i, 1.0);
//		}
		int topK = 10;
		for(int j = 0; j < topK; j++){
			orthogMat.addRow(j, new Counter(topEig));
//			double norm = testMat.multiply(topEig).norm();
//			System.out.println("norm: "+norm);
			topEig = SparseMatrix.ConstrainedEig(testMat, orthogMat);
		}
//		orthogMat.addRow(topK, new Counter(topEig));
//		for(int i: orthogMat.rows){
//			Counter eig = orthogMat.getRow(i);
//			for(int j: orthogMat.rows){
//				Counter eigOther = orthogMat.getRow(j);
//				double sim = eig.dot(eigOther);
////				int sim = (int)(eig.dot(eigOther)+0.9999);
//				System.out.print(sim+" ");
//			}
//			System.out.println();
//		}
			
		// SparseMatrix tcMin = testMat.minimize().transitiveClosure();
		// SparseMatrix tcMat = testMat.transitiveClosure();
		// System.out.println(tcMin.cardinality()+" "+tcMat.cardinality());
		// testMat.add(0,0,1.0);
		// testMat.add(0,1,1.0);
		// testMat.add(1,1,1.0);
		// testMat.add(1,2,1.0);
		// testMat.add(2,2,1.0);
		// System.out.println(testMat.cardinality());
		// for(int t = 0; t < 1000; t++){
		// testMat = testMat.multiply(testMat);
		// System.out.println(testMat.cardinality());
		// }
		// System.out.println(testMat.toString());
	}

}
