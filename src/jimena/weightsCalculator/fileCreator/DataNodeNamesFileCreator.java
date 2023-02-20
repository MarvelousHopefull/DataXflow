package jimena.weightsCalculator.fileCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * To get a list of all Node Names where Data exists for in the network as a .csv file.
 * 
 * @author Jan Krause
 * @since 18.02.2023
 * */

public class DataNodeNamesFileCreator {
	
	/**
	 * Creates a .csv file containing all the Node names.
	 * Creates no file if there are no nodes listed.
	 * @param path Where the file should be saved to.
	 * @param nodes The names of the nodes that should be listed in the file.
	 * @throws IOException
	 */
	public static void createNodeNamesFile(String path, String[] nodes) throws IOException{
		if(nodes == null || nodes.length == 0) {
			return;
		}
		
		File file = new File(path);
		if (!file.toString().endsWith("_DataNodeNames.csv")) {
			path = path +"_DataNodeNames.csv";
		}
		file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		
		String text = "gene_name" + "\n";
		for(String s : nodes) {
			text += s + "\n";
		}
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
	}
	
}
