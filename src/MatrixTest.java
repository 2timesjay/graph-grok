import java.util.Random;
import java.util.Date;

public class MatrixTest {
	static Random myRand = new Random();
	static Date myDate = new Date();
	public static void main(String[] args){
		int d0 = Integer.parseInt(args[0]);
		int d1 = Integer.parseInt(args[1]);
		int d2 = Integer.parseInt(args[2]);
		double[][] mat1 = new double[d0][d1];
		for(int i = 0; i < d0; i++){
			for(int j = 0; j < d1; j++){
				mat1[i][j]=myRand.nextGaussian();
			}
		}
		double[][] mat2 = new double[d1][d2];
		for(int j = 0; j < d1; j++){
			for(int k = 0; k < d2; k++){
				mat2[j][k]=myRand.nextGaussian();
			}
		}
		int T = 1;
		d2 = d2;
		double time = 0.0;
		for(int t = 0; t < T; t++){
			long start = System.currentTimeMillis();
			double[][] mat3 = new double[d0][d2];
			for(int i = 0; i < d0; i++){
				for(int j = 0; j < d1; j++){
					for(int k = 0; k < d2; k++){
						mat3[i][k] += mat1[i][j]*mat2[j][k];
					}
				}
			}
			long end = System.currentTimeMillis();
			time += (end-start)/1000.;
		}
		System.out.println(d0+" "+d1+" "+d2+" "+T);
		//		System.out.println("Time: "+(end-start));
		System.out.println("Time: "+time);
		System.out.println("MegaFlops: "+1.0/1000000.0/time*d0*d1*d2*T);
	}
}
