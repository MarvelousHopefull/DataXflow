package jimena.weightsCalculator.fileCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Used for creating initialValues.txt files. Used by D2D, to get the initial values for all parameters and nodes.
 * 
 * @author Jan Krause
 * @since 16.02.2023
 * */

public class InitialValuesFileCreator {
	
	//change the values for different initial values here
	//but don't change to 0 or 0.0! (or change the code in createInitValuesFile too!! around line 70)
	private static double sAlpha = 1; //initial
	private static double sLBAlpha = 0; //lowerBound
	private static double sUBAlpha = 1000.0; //uperBound
	
	private static double sBeta = 10;
	private static double sLBBeta = 0.0;
	private static double sUBBeta = 1000.0;
	
	private static double sH = 10;
	private static double sLBH = 1;
	private static double sUBH = 1000.0;
	
	private static double sDelta = 1;
	private static double sLBDelta = 0.0;
	private static double sUBDelta = 1000.0;
	
	private static double sInitNodeValue = 0.5;
	private static double sLBNodeValue = 0.0;
	private static double sUBNodeValue = 1.0;
	
	/**
	 * Creates a new or overrides an existing .txt file at the specified path, to create a list of initial values for the parameters given through the kMapping, the rMapping and the nodeMapping. The new file name will end with _initValues. Using the standard init values.
	 * @param path The path to save the file to.
	 * @param kMapping The mapping of the parameters, done as in the DefCreator class.
	 * @param nodeMapping The mapping of the nodes, done as in the DefCreator class.
	 * @param rMapping The mapping of the delta's for up or down regulated nodes, done as in the DefCreator class.
	 * @throws IOException
	 */
	public static void createInitValuesFile(String path, String[][] kMapping, String[][] nodeMapping, String[][] rMapping) throws IOException{
		createInitValuesFile(path,kMapping,nodeMapping,rMapping,sAlpha,sLBAlpha,sUBAlpha,sBeta,sLBBeta,sUBBeta,sH,sLBH,sUBH,sDelta,sLBDelta,sUBDelta,sInitNodeValue,sLBNodeValue,sUBNodeValue);
	}
	
	
	/**
	 * Creates a new or overrides an existing .txt file at the specified path, to create a list of initial values for the parameters given through the kMapping, the rMapping and the nodeMapping. The new file name will end with _initValues. The init values have to be given.
	 * @param path The path to save the file to.
	 * @param kMapping The mapping of the parameters, done as in the DefCreator class.
	 * @param nodeMapping The mapping of the nodes, done as in the DefCreator class.
	 * @param rMapping The mapping of the delta's for up or down regulated nodes, done as in the DefCreator class.
	 * @param initAlpha	The initial value for all alphas.
	 * @param lbAlpha The lower bound for alpha.
	 * @param ubAlpha The upper bound for alpha.
	 * @param initBeta	The initial value for all betas.
	 * @param lbBeta The lower bound for beta.
	 * @param ubBeta The upper bound for beta.
	 * @param initH	The initial value for all Hs.
	 * @param lbH The lower bound for H.
	 * @param ubH The upper bound for H.
	 * @param initDelta	The initial value for all deltas.
	 * @param lbDelta The lower bound for delta.
	 * @param ubDelta The upper bound for delta.
	 * @param initNodeValue	The initial value for all nodes.
	 * @throws IOException
	 */
	public static void createInitValuesFile(String path, String[][] kMapping, String[][] nodeMapping, String[][] rMapping, 
			double initAlpha, double lbAlpha, double ubAlpha, double initBeta, double lbBeta, double ubBeta, double initH, double lbH, double ubH, double initDelta, double lbDelta, double ubDelta, double initNodeValue, double lbNodeValue, double ubNodeValue) throws IOException{
		if(nodeMapping == null || kMapping == null) {
			return;
		}
		
		if (!path.endsWith("_initValues.txt")) {
			path = path +"_initValues.txt";
		}
		
		File file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		String text = "";
		String t1 = "arSetPars(" + "\'";
		String t2 = "\'" + ",";
		String t3 = ",1,0,";
		Double lb = 0.0;	//lower bound
		String t4 = ",";
		Double ub = 0.0;	//upper bound
		String tEnd = ");";
		double v = 0.0;
		for(int i = 0; i < kMapping.length; i++) {
			switch (kMapping[i][3]) {
				case "a":
					v = initAlpha;
					lb = lbAlpha;
					ub = ubAlpha;
					break;
				case "b":
					v = initBeta;
					lb = lbBeta;
					ub = ubBeta;
					break;
				case "h":
					v = initH;
					lb = lbH;
					ub = ubH;
					break;
				default:
					v = 0.0;
			}
			if(v != 0.0) {
				text += t1 + kMapping[i][0] + t2 + v + t3 + lb + t4 + ub + tEnd;
				text += "\n";
			}
			
		}
		
		v = initNodeValue;
		lb = lbNodeValue;
		ub = ubNodeValue;
		for(int i = 0; i < nodeMapping.length; i++) {
			text += t1 + "init_" + nodeMapping[i][0] + t2 + v + t3 + lb + t4 + ub + tEnd;
			text += "\n";
		}
		
		if (rMapping != null) {
			v = initDelta;
			lb = lbDelta;
			ub = ubDelta;
			for(int i = 0; i < rMapping.length; i++) {
				text += t1 + rMapping[i][3] + t2 + v + t3 + lb + t4 + ub + tEnd;
				text += "\n";
			}
		}
		
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		fw.write(text);
		fw.close();
	}

}
