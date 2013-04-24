/*
 * SparseMatrix class, TreeMap of Counters
 * No fixed size or sparsity structure (except rowDim and colDim arguments for indexing). numRows or numCols evaluated strictly
 */
import java.util.*;

public class TreeSparseMatrix {
	private TreeMap<Integer, Counter> mat;
	private HashSet<Integer> rows;
	private HashSet<Integer> cols;
	private int rowDim;
	private int colDim;

	TreeSparseMatrix(int rowD, int colD) {
		rowDim = rowD;
		colDim = colD;
		mat = new TreeMap<Integer, Counter>();
		rows = new HashSet<Integer>();
		cols = new HashSet<Integer>();
	}

	TreeSparseMatrix(TreeSparseMatrix orig) {
		mat = new TreeMap<Integer, Counter>();
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

	public TreeSparseMatrix transpose() {
		TreeSparseMatrix transp = new TreeSparseMatrix(this.rowDim, this.colDim);
		for (int r : rows) {
			Counter row = this.getRow(r);
			for (int c : row.keySet()) {
				double v = row.get(c);
				transp.set(c, r, v);
			}
		}
		return transp;
	}

	public TreeSparseMatrix symmetric() {
		TreeSparseMatrix symm = new TreeSparseMatrix(this);
		symm = symm.add(symm.transpose());
		for (int r : rows) {
			double v = this.get(r, r);
			this.set(r, r, v / 2.0);
		}
		return symm;
	}

	public TreeSparseMatrix add(TreeSparseMatrix other) {
		TreeSparseMatrix sum = new TreeSparseMatrix(this.rowDim, this.colDim);
		sum = new TreeSparseMatrix(this);
		for (int r : other.rows) {
			sum.addRow(r, other.getRow(r));
		}
		return sum;
	}

	public List<Double> multiply(List<Double> vector) {
		List<Double> resVec = new ArrayList<Double>();
		for (int i = 0; i < rowDim; i++) {
			resVec.add(0.0);
		}
		for (int r : rows) {
			resVec.set(r, this.getRow(r).dot(vector));
		}
		return resVec;
	}

	public TreeSparseMatrix multiply(TreeSparseMatrix other) {
		TreeSparseMatrix mult = new TreeSparseMatrix(this.rowDim, other.colDim);
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

	public TreeSparseMatrix getRedundant() {
		TreeSparseMatrix redMat = new TreeSparseMatrix(this).multiply(this);
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

	public TreeSparseMatrix minimize() {
		TreeSparseMatrix minMat = new TreeSparseMatrix(this);
		TreeSparseMatrix redundant = this.getRedundant();
		minMat.removeEntries(redundant);
		return minMat;
	}

	public void remove(int r, int c) {
		if (hasRow(r)) {
			this.getRow(r).remove(c);
		}
	}

	public void removeEntries(TreeSparseMatrix redundant) {
		for (int r : redundant.getRows()) {
			Counter row = redundant.getRow(r);
			for (int c : row.keySet()) {
				this.remove(r, c);
			}
		}
	}

	public TreeSparseMatrix transitiveClosure() {
		TreeSparseMatrix tcMat = new TreeSparseMatrix(this);
		TreeSparseMatrix transMat = new TreeSparseMatrix(this);
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

	public static void main(String[] args) {
		System.out.println("Working");
		Random rand = new Random();
		int dim = 128;
		int num = dim * dim * 40;
		TreeSparseMatrix testMat = new TreeSparseMatrix(dim, dim);
		for (int i = 0; i < num; i++) {
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
			testMat.add(r, c, 1.0);
		}
		TreeSparseMatrix testMat2 = new TreeSparseMatrix(dim, dim);
		for (int i = 0; i < num; i++) {
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
			testMat2.add(r, c, 1.0);
		}
		long start = System.currentTimeMillis();
		TreeSparseMatrix testMat3 = testMat.multiply(testMat2);
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end - start));
		System.out.println(testMat.cardinality() + " "
				+ testMat.add(testMat2).cardinality() + " "
				+ testMat3.cardinality());
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
