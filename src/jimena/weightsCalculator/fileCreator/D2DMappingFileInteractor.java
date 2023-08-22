package jimena.weightsCalculator.fileCreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import jimena.weightsCalculator.D2DMapping;

/**
 * Used for creating the _main.m File needed for the ExternalStiumli interactions.
 * @author Jan Krause
 *
 */
public class D2DMappingFileInteractor {
	
	//text that shows up in the file, edit it here or problems with reading the file can occur
	static String nodeMappingStart = "Mapping of Nodes";
	static String nodeMappingColumnNames = "Node-Alias	Node-Name	Node-Number";
	static String regulatorMappingStart = "Mapping of Regulated Nodes";
	static String regulatorMappingColumnNames = "Regulated-Alias	Node-Name	up or down	delta-Alias	is Active	Alias-Number";
	static String parameterMappingStart = "Mapping of Parameters";
	static String parameterMappingColumnNames = "Parameter-Alias	Node-Number	i'te Connection(or -1)	Parameter-Type	Source-Node-Alias(or Parameter-Type)	Source-Node-Number(or 0)";
	static String constNodesStart = "List of Nodes that are constant";
	static String finalTimeStart = "The final Time";
	
	public static D2DMapping getD2DMapping(String path) throws Exception{
		File file = new File(path);
		if(!file.exists()) { 
			throw new Exception("File doesn't exist!");
		}
		if (!path.endsWith("_D2DMapping.tsv")) {
			throw new Exception("Wrong File was loaded!");
		}
		
		String[][] nodeMapping;
		String[][] regulatorMapping;
		String[][] parameterMapping;
		String[] constNodes;
		double finalTime;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			ArrayList<ArrayList<String>> aList = new ArrayList<ArrayList<String>>();
			ArrayList<String> aLine;
			String line = reader.readLine();
			
			//Node Mapping
			//text lines
			while(line != null && (line.startsWith(nodeMappingStart) || line.startsWith(nodeMappingColumnNames))) {
				line = reader.readLine();
			}
			
			int amount = 0;
			while(line != null) {
				if(!line.startsWith("x")) {
					break;
				}
				
				aLine = new ArrayList<String>();
				String[] parts = line.split("\\	");
				if(parts.length < 3) {
					throw new Exception("Some lines in the _D2DMapping.tsv File seam to be missing Information!");
				}
				aLine.add(parts[0]);
				aLine.add(parts[1]);
				aLine.add(parts[2]);
				aList.add(aLine);
				amount++;
				
				line = reader.readLine();
			}
			nodeMapping = new String[amount][3];
			for(int i = 0; i < amount; i++) {
				for(int j = 0; j < 3; j++) {
					nodeMapping[i][j] = aList.get(i).get(j);
				}
			}
			
			//Regulator Mapping, can be null
			aList = new ArrayList<ArrayList<String>>();
			//empty line(s)
			while(line != null && line.isEmpty()) {
				line = reader.readLine();
			}
			//text lines
			while(line != null && (line.startsWith(regulatorMappingStart) || line.startsWith(regulatorMappingColumnNames))) {
				line = reader.readLine();
			}
			amount = 0;
			while(line != null) {
				if(!line.startsWith("u")) {
					break;
				}
				
				aLine = new ArrayList<String>();
				String[] parts = line.split("\\	");
				if(parts.length < 6) {
					throw new Exception("Some lines in the _D2DMapping.tsv File seam to be missing Information!");
				}
				aLine.add(parts[0]);
				aLine.add(parts[1]);
				aLine.add(parts[2]);
				aLine.add(parts[3]);
				aLine.add(parts[4]);
				aLine.add(parts[5]);
				aList.add(aLine);
				amount++;
				
				line = reader.readLine();
			}
			if(amount > 0) {
				regulatorMapping = new String[amount][6];
				for(int i = 0; i < amount; i++) {
					for(int j = 0; j < 6; j++) {
						regulatorMapping[i][j] = aList.get(i).get(j);
					}
				}
			}
			else {
				regulatorMapping = null;
			}

			//Parameter Mapping
			aList = new ArrayList<ArrayList<String>>();
			//empty line(s)
			while(line != null && line.isEmpty()) {
				line = reader.readLine();
			}
			//text lines
			while(line != null && (line.startsWith(parameterMappingStart) || line.startsWith(parameterMappingColumnNames))) {
				line = reader.readLine();
			}
			amount = 0;
			while(line != null) {
				if(!(line.startsWith("a") || line.startsWith("b") || line.startsWith("h"))) {
					break;
				}
				
				aLine = new ArrayList<String>();
				String[] parts = line.split("\\	");
				if(parts.length < 6) {
					throw new Exception("Some lines in the _D2DMapping.tsv File seam to be missing Information!");
				}
				aLine.add(parts[0]);
				aLine.add(parts[1]);
				aLine.add(parts[2]);
				aLine.add(parts[3]);
				aLine.add(parts[4]);
				aLine.add(parts[5]);
				aList.add(aLine);
				amount++;
				
				line = reader.readLine();
			}
			parameterMapping = new String[amount][6];
			for(int i = 0; i < amount; i++) {
				for(int j = 0; j < 6; j++) {
					parameterMapping[i][j] = aList.get(i).get(j);
				}
			}
			
			//Constant Nodes
			//empty line(s)
			while(line != null && line.isEmpty()) {
				line = reader.readLine();
			}
			//text lines
			while(line != null && line.startsWith(constNodesStart)) {
				line = reader.readLine();
			}	
			amount = 0;
			aLine = new ArrayList<String>();
			while(line != null) {
				if(!line.startsWith("node:")) {
					break;
				}
				String[] parts = line.split("\\	");
				if(parts.length < 2) {
					throw new Exception("Some lines in the _D2DMapping.tsv File seam to be missing Information!");
				}
				aLine.add(parts[1]);
				amount++;
				
				line = reader.readLine();
			}
			if(amount > 0) {
				constNodes = new String[amount];
				for(int i = 0; i < amount; i++) {
					constNodes[i] = aLine.get(i);
				}
			}
			else {
				constNodes = null;
			}
			
			//Final Time
			//empty line(s)
			while(line != null && line.isEmpty()) {
				line = reader.readLine();
			}
			//text lines
			while(line != null && line.startsWith(finalTimeStart)) {
				line = reader.readLine();
			}
			finalTime = Double.parseDouble(line);
		}
		return new D2DMapping(nodeMapping,regulatorMapping,parameterMapping,constNodes,finalTime);
	}
	
	public static String createD2DMappingFile(String path, D2DMapping mapping) throws Exception {
		if (!path.endsWith("_D2DMapping.tsv")) {
			path = path +"_D2DMapping.tsv";
		}
		File file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		
		String[][] nodeMapping = mapping.nodeMapping();
		String[][] regulatorMapping = mapping.regulatorMapping();
		String[][] parameterMapping = mapping.parameterMapping();
		String[] constNodes = mapping.constNodes();
		double finalTime = mapping.finalTime();
		
		String text = "";
		
		//Node Mapping
		text += nodeMappingStart + "\r\n";
		text += nodeMappingColumnNames + "\r\n";
		for(int i = 0; i<nodeMapping.length; i++) {
			for(int j = 0; j<nodeMapping[i].length; j++) {
				text += nodeMapping[i][j] + "	";
			}
			text += "\r\n";
		}
		text += "\r\n"; 
		
		//Regulator Mapping
		text += regulatorMappingStart + "\r\n";
		text += regulatorMappingColumnNames + "\r\n";
		if(regulatorMapping != null) {
			for(int i = 0; i<regulatorMapping.length; i++) {
				for(int j = 0; j<regulatorMapping[i].length; j++) {
					text += regulatorMapping[i][j] + "	";
				}
				text += "\r\n";
			}	
		}
		text += "\r\n";
		
		//Parameter Mapping
		text += parameterMappingStart + "\r\n";
		text += parameterMappingColumnNames + "\r\n";
		for(int i = 0; i<parameterMapping.length; i++) {
			for(int j = 0; j<parameterMapping[i].length; j++) {
				text += parameterMapping[i][j] + "	";
			}
			text += "\r\n";
		}
		text += "\r\n";
		
		//Constant Nodes
		text += constNodesStart + "\r\n";
		if(constNodes != null) {
			for(int i = 0; i<constNodes.length; i++) {
				text += "node:" + "	" + constNodes[i];
				text += "\r\n";
			}
		}
		text += "\r\n";		
		
		//Final Time
		text += finalTimeStart + "\r\n";
		text += finalTime;
		
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
		return path;
	}

}
