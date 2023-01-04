package jimena.weightsCalculator.fileCreator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;

public class CSVNodeNamesCreator {

	public static void createFile(String path, RegulatoryNetwork network) throws IOException{
		
		
		File file = new File(path);
		if (!file.toString().endsWith(".csv")) {
			file = new File(file.toString()+".csv");
		}
		
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
