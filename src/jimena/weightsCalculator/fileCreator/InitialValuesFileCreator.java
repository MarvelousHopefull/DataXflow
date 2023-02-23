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
	public static double alpha = 1;
	public static double beta = 10;
	public static double h = 10;
	public static double delta = 1;
	public static double initNodeValue = 0.5;
	
	/**
	 * Creates a new or overrides an existing .txt file at the specified path, to create a list of initial values for the parameters given through the kMapping, the rMapping and the nodeMapping. The new file name will end with _initValues.
	 * @param path The path to save the file to.
	 * @param kMapping The mapping of the parameters, done as in the DefCreator class.
	 * @param nodeMapping The mapping of the nodes, done as in the DefCreator class.
	 * @param rMapping The mapping of the delta's for up or down regulated nodes, done as in the DefCreator class.
	 * @throws IOException
	 */
	public static void createInitValuesFile(String path, String[][] kMapping, String[][] nodeMapping, String[][] rMapping) throws IOException{
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
		String tLower = "";
		String t4 = ",";
		String tHigh = "";
		String tEnd = ");";
		double v = 0.0;
		for(int i = 0; i < kMapping.length; i++) {
			switch (kMapping[i][3]) {
				case "a":
					v = alpha;
					tLower = "0";
					tHigh = "1000";
					break;
				case "b":
					v = beta;
					tLower = "0";
					tHigh = "1000";
					break;
				case "h":
					v = h;
					tLower = "1";
					tHigh = "1000";
					break;
				case "delta":
		  			v = delta;
		  			tLower = "0";
					tHigh = "1000";
		  			break;
				default:
					v = 0.0;
			}
			if(v != 0.0) {
				text += t1 + kMapping[i][0] + t2 + v + t3 + tLower + t4 + tHigh + tEnd;
				text += "\n";
			}
			
		}
		
		v = initNodeValue;
		tLower = "0";
		tHigh = "1";
		for(int i = 0; i < nodeMapping.length; i++) {
			text += t1 + "init_" + nodeMapping[i][0] + t2 + v + t3 + tLower + t4 + tHigh + tEnd;
			text += "\n";
		}
		
		if (rMapping != null) {
			v = delta;
			for(int i = 0; i < rMapping.length; i++) {
				text += t1 + rMapping[i][3] + t2 + v + t3 + tLower + t4 + tHigh + tEnd;
				text += "\n";
			}
		}
		
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		fw.write(text);
		fw.close();
	}

}
