import java.util.Random;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.function.tdouble.IntIntDoubleFunction;

public class SparseMatrixTest {
	public static void main(String[] args) {
		System.out.println("Working");

		// SparseDoubleMatrix2D sdm = new SparseDoubleMatrix2D(128,128);

		Random rand = new Random();
		int dim = 128;
		int num = dim * 10;
		SparseCCDoubleMatrix2D matrix = new SparseCCDoubleMatrix2D(128, 128);
		for (int i = 0; i < num; i++) {
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
			matrix.setQuick(r, c, 1.0);
		}
		SparseCCDoubleMatrix2D matrix2 = new SparseCCDoubleMatrix2D(128, 128);
		for (int i = 0; i < num; i++) {
			int r = rand.nextInt(dim);
			int c = rand.nextInt(dim);
			matrix2.setQuick(r, c, 1.0);
		}
		SparseCCDoubleMatrix2D matrix3 = new SparseCCDoubleMatrix2D(128, 128);

		matrix.forEachNonZero(new IntIntDoubleFunction() {
			public double apply(int row, int column, double value) {
				DoubleMatrix1D row = matrix2.viewRow(column);
				matrix3.setQuick(row);
				return value;
			}
		});

		// Random rand = new Random();
		// int dim = 8096*16;
		// int num = dim*10;
		// SparseMatrix testMat = new SparseMatrix(dim,dim);
		// for(int i= 0; i < num; i++){
		// int r = rand.nextInt(dim);
		// int c = rand.nextInt(dim);
		// testMat.add(r,c,1.0);
		// }
		// SparseMatrix testMat2 = new SparseMatrix(dim,dim);
		// for(int i= 0; i < num; i++){
		// int r = rand.nextInt(dim);
		// int c = rand.nextInt(dim);
		// testMat2.add(r,c,1.0);
		// }
		// long start = System.currentTimeMillis();
		// SparseMatrix testMat3 = testMat.multiply(testMat2);
		// long end = System.currentTimeMillis();
		// System.out.println("Time: "+(end-start));
		// System.out.println(testMat.cardinality()+" "+testMat.add(testMat2).cardinality()+" "+testMat3.cardinality());
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
