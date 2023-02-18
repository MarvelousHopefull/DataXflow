package jimena.weightsCalculator.fileCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;


/**
 * To get a list of all Node Names in the network as a .csv file.
 * 
 * @author Jan Krause
 * @since 23.11.2022
 * */
public class CSVNodeNamesCreator {

	public static void createFile(String path, RegulatoryNetwork network) throws IOException{
		
		
		File file = new File(path);
		if (!file.toString().endsWith(".csv")) {
			path = path +"_NodeNames.csv";
		}
		file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		NetworkNode[] nodes = network.getNetworkNodes();
		
		String text = "";
		for(NetworkNode n : nodes) {
			text += n.getName() + "\n";
		}
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
	}
}
