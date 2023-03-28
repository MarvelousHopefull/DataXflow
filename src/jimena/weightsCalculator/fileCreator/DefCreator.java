package jimena.weightsCalculator.fileCreator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import jimena.binarybf.BinaryBooleanFunction;
import jimena.binarybf.actinhibitf.ActivatorInhibitorFunction;
import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.weightsCalculator.D2DMapping;

import java.io.File;

/**
 * Used for creating model.def & data.def files. Used by D2D, to calculated best fitting weights and other parameters.
 * 
 * @author Jan Krause
 * @since 23.11.2022
 * */
public class DefCreator {
	
	private static boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	/**
	 * Creates a model.def, a data.def, a initValues.txt and a dataNodeNames.csv (if needed) file from a given Network in the format that is requested by D2D.
	 * The four files will be saved as name_model.def, name_data.def, name_initValues.txt and name_DataNodeNames.csv. Where name stands for the selected file name.
	 * If in the folder there are already files with those names they will be overridden.
	 * @param path The String Path where the new Files should be saved to.
	 * @param network The Network in question. 
	 * @param dataNodes Nodes where there exist experiment data.
	 * @param upRNodes Nodes that will be up-regulated.
	 * @param downRNodes Nodes that will be down-regulated.
	 * @param initValues The list of initial values and upper and lower bounds as needed by the InitialValuesFileCreator class. Has to be 13 values in the correct order! If not 13 values, the default values will be used.
	 * @param constantNodes The Nodes that have a constant value.
	 * @param finalTime The finish time of the experiment.
	 * @throws IOException
	 */
	public static D2DMapping createFiles(String path, RegulatoryNetwork network, String[] dataNodes , String[] upRNodes, String[] downRNodes, double[] initValues, String[] constantNodes, double finalTime) throws IOException, Exception{
		//if(fileExists(path)) { return; }
		
		//mapping to node alias (x1, x2, ...)
		//[i][0] := alias (x1|x2|...)
		//[i][1] := Node-Name
		NetworkNode[] nodes = network.getNetworkNodes();
		String[][] mapping = new String[nodes.length][3];
		for(int i = 0; i < nodes.length; i++ ) {
			mapping[i][0] = "x" + (i+1);
			mapping[i][1] = nodes[i].getName();
			mapping[i][2] = "" + (i+1);
		}
		
		List<String> cNList = null; 
		if(constantNodes != null) {
			cNList = Arrays.asList(constantNodes);
		}
		//mapping of the regulated nodes, up regulated Nodes before down regulated Nodes
		//[i][0] := alias (u1|u2|...)
		//[i][1] := Node-Name
		//[i][2] := up or down (u|d)
		//[i][3] := delta-Alias
		//[i][4] := is Active (true|false)
		//[i][5] := alias Number (1|2|...)
		String[][] rMapping = null;
		if((upRNodes != null && upRNodes.length > 0) || (downRNodes != null && downRNodes.length > 0 )) {
			ArrayList<ArrayList<String>> rMList = new ArrayList<ArrayList<String>>();
			ArrayList<String> rMNode;
			int i = 0;
			if(upRNodes != null) {
				for(int j = 0; j < upRNodes.length; j++) {
					if(constantNodes != null && cNList.contains(upRNodes[j])) {
						continue;
					}
					rMNode = new ArrayList<String>();
					rMNode.add("u" + (i+1));
					rMNode.add(upRNodes[j]);
					rMNode.add("u");
					rMNode.add("delta_" + (i+1));
					rMNode.add("true");
					rMNode.add("" + (i+1));
					rMList.add(rMNode);
					i++;
					
				}
			}
			if(downRNodes != null) {
				for(int j = 0; j < downRNodes.length; j++) {
					if(constantNodes != null && cNList.contains(downRNodes[j])) {
						continue;
					}
					rMNode = new ArrayList<String>();
					rMNode.add("u" + (i+1));
					rMNode.add(downRNodes[j]);
					rMNode.add("d");
					rMNode.add("delta_" + (i+1));
					rMNode.add("true");
					rMNode.add("" + (i+1));
					rMList.add(rMNode);
					i++;
				}
			}
			rMapping = new String[i][6];
			for(int j = 0; j < i; j++) {
				for(int k = 0; k < 6; k++) {
					rMapping[j][k] = rMList.get(j).get(k);
				}
			}
		}
		String[][] kMapping = getKMapping(network, mapping, cNList);
		
		createModelFile(path, network, mapping, rMapping, kMapping, constantNodes, finalTime);
		createDataFile(path, network, dataNodes, mapping, rMapping, finalTime);
		
		if(initValues == null || initValues.length != 15) {
			InitialValuesFileCreator.createInitValuesFile(path, kMapping, mapping, rMapping);
		}
		else {
			InitialValuesFileCreator.createInitValuesFile(path, kMapping, mapping, rMapping, initValues[0], initValues[1], initValues[2], initValues[3], initValues[4], initValues[5], initValues[6], initValues[7], initValues[8], initValues[9], initValues[10], initValues[11], initValues[12], initValues[13], initValues[14]);
		}
		DataNodeNamesFileCreator.createNodeNamesFile(path, dataNodes);
		
		return new D2DMapping(mapping, rMapping, kMapping, constantNodes, finalTime);
	}
	
	/**
	 * Creates a data.def File from a given Network in the format that is requested by D2D.
	 * @param path The String Path where the File should be saved to.
	 * @param network The Network in question. 
	 * @param dataNodes Nodes where there exist experiment data.
	 * @param mapping Mapping of the nodes.
	 * @param rMapping Mapping of the regulated nodes.
	 * @param finalTime The finish time of experiment.
	 * @throws IOException
	 */
	private static void createDataFile(String path, RegulatoryNetwork network, String[] dataNodes, String[][] mapping, String[][] rMapping, double finalTime) throws IOException{
		
		if (!path.endsWith("_data.def")) {
			path = path +"_data.def";
		}
		
		if(fileExists(path)) { 
			File file = new File(path); 
			file.delete();
		}
		
		String text = getDataDescriptionAndPredictor(finalTime);
		text += getDataInputs(rMapping);
		text += getDataObservables(dataNodes, mapping);
		text += getDataErrors(dataNodes);
		text += getDataTail();
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
	}
	
	/**
	 * Creates a model.def File from a given Network in the format that is requested by D2D.
	 * @param path The String Path where the File should be saved to.
	 * @param network The Network in question. 
	 * @param mapping Mapping of the nodes.
	 * @param rMapping Mapping of the regulated nodes.
	 * @param kMapping Mapping of the parameters.
	 * @param constantNodes All Nodes that have a constant value.
	 * @param finalTime The finish time of experiment.
	 * @throws IOException
	 */
	private static void createModelFile(String path, RegulatoryNetwork network, String[][] mapping, String[][] rMapping, String[][] kMapping, String[] constantNodes, double finalTime) throws IOException{

		if (!path.endsWith("_model.def")) {
			path = path +"_model.def";
		}
		
		if(fileExists(path)) { 
			File file = new File(path); 
			file.delete();
		}
		//if(fileExists(path)) { return; }

		String text = getModelDescriptionAndPredictor(finalTime);
		text += getModelCompartments();
		text += getModelStates(mapping, network);
		text += getModelInputs(rMapping);
		text += getModelODEs(network,mapping,kMapping, rMapping, constantNodes);
		text += getModelTail();
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
		
		//addODEs(path, network, mapping);
	}
	
	/**
	 * 
	 * @return The string that is the head of the Data.def document.
	 */
	private static String getDataDescriptionAndPredictor(double finalTime) {
		Date date = new Date();
        SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy");
        String currentYear = getYearFormat.format(date);
		
		//description
		String text = "DESCRIPTION" + 
				"\n" + "\"" + "Potterswheel Data Test" +"\"" +
				"\n" + "\"" + "by Jan" + "\"" +
				"\n" + "\"" + "Date: " + currentYear + "\"" +
				"\n" + "\"" + "Version: 1.0" + "\"" +
				"\n";
		//predictor
		text = text + "\n" + "PREDICTOR" + 
				"\n" + "t	T	" + "\"" + "min" + "\"" + "	" + "\"" + "time" + "\"" + "	0	" + String.valueOf(finalTime) +
				"\n";
		return text;
	}
	
	/**
	 * missing: a check if the correspondent nodes are actual nodes 
	 * @param rMapping The mapping of the regulated Nodes to their alias.
	 * @return The String that are the Inputs.
	 */
	private static String getDataInputs(String[][] rMapping) {
		String text = "\n" + "INPUTS" + 
				"\n";
		if(rMapping != null) {
			for(int i = 0; i < rMapping.length; i++) {
				text = text + "\n" + rMapping[i][0] + "	" + "\"" + "step1(t, 0, 0, 1)" + "\"";
				//u1	"step1(t, 0, 0, 1)"
			}
			text = text + "\n";
		}
		
		
		return text;
	}
	
	/**
	 * 
	 * @param dataNodes
	 * @return
	 */
	private static String getDataObservables(String[] dataNodes, String[][] mapping) {
		String text = "\n" + "OBSERVABLES" + 
				"\n";
		
		if(dataNodes != null && dataNodes.length > 0) {
			String symbol = "!Missing!";
			for(int i = 0; i < dataNodes.length; i++) {
				for(int j = 0; j<mapping.length; j++) {
					if(mapping[j][1].equals(dataNodes[i])) {
						symbol = mapping[j][0];
					}
				}
				text = text + "\n" + dataNodes[i] + "_obs" + "	" + "C" + "	" 
					+ "\"" + "au" + "\"" + "	" + "\"" + "conc." + "\"" + "	" + "0" 
					+ "	" + "0" + "	" + "\"" + symbol + "\"";
				//name_obs	"C" "au" "conc." 0 0 "xi"
			}
			text = text + "\n" ;
		}
		
		return text;
	}
	
	/**
	 * 
	 * @param dataNodes
	 * @return
	 */
	private static String getDataErrors(String[] dataNodes) {
		String text = "\n" + "ERRORS" + 
		"\n";
		
		if(dataNodes != null && dataNodes.length > 0) {
			for(int i = 0; i < dataNodes.length; i++) {
				text = text + "\n" + dataNodes[i] + "_obs" + "	" + dataNodes[i] + "_obs" + "_std";
				//name_obs	"sd_name_obs"
			}
			text = text + "\n" ;
		}
		
		return text;
	}
	
	/**
	 * 
	 * @return The string at the end of the data.def file.
	 */
	private static String getDataTail() {
		String text = "\n" + "CONDITIONS" + 
				"\n";
		
		return text;
	}
	
	/**
	 * 
	 * @return The string that is the head of the Model.def document.
	 */
	private static String getModelDescriptionAndPredictor(double finalTime) {
		Date date = new Date();
        SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy");
        String currentYear = getYearFormat.format(date);
		
		//description
		String text = "DESCRIPTION" + 
				"\n" + "\"" + "Potterswheel Model Test" +"\"" +
				"\n" + "\"" + "by Jan" + "\"" +
				"\n" + "\"" + "Date: " + currentYear + "\"" +
				"\n" + "\"" + "Version: 1.0" + "\"" +
				"\n";
		//predictor
		text = text + "\n" + "PREDICTOR" + 
				"\n" + "t	T	" + "\"" + "min" + "\"" + "	" + "\"" + "time" + "\"" + "	0	" + String.valueOf(finalTime) +
				"\n";
		return text;
	}
	
	/**
	 * 
	 * @return The String that represents the Compartments Part for the Document.
	 */
	private static String getModelCompartments(){
		
		String text = "";
		//compartments
		text = text + "\n" + "COMPARTMENTS" + 
				"\n" +
				"\n" + "cyt	V	" + "\"" + "pl" + "\"" + "	" + "\"" +"vol." +  "\"" + "	1" +
				"\n";	
		
		return text;
		
	}
	
	/**
	 * 
	 * @param mapping The Mapping of the Node Names to their alias.
	 * @param network The Network.
	 * @return The String that is the list of Nodes in the Network.
	 */
	private static String getModelStates(String[][] mapping, RegulatoryNetwork network){
		
		//States
		String text = "\n" + "STATES" + 
				"\n";
		NetworkNode[] nodes = network.getNetworkNodes();
		String symbol = "!Missing!";
		for(int i = 0; i < nodes.length; i++ ) {
			for(int j = 0; j<mapping.length; j++) {
				if(mapping[j][1] == nodes[i].getName()) {
					symbol = mapping[j][0];
				}
			}
			text = text + 
					"\n" + symbol + "	C	" + "\"" + "nM" +  "\"" + "	" + "\"" + "conc." + 
					"\"" + "	cyt 1 " + "\"" + nodes[i].getName() +  "\"" + "	0";
		}
		text = text + "\n";
		//x2	C   "nM"      "conc."       cyt 1 "x2"       	1
		
		return text;
	}
	
	/**
	 * missing: a check if the correspondent nodes are actual nodes 
	 * @param rMapping The mapping of the regulated Nodes to their alias.
	 * @return The String that are the Inputs.
	 */
	private static String getModelInputs(String[][] rMapping) {
		String text = "\n" + "INPUTS" + 
				"\n";
		if(rMapping != null) {
			for(int i = 0; i < rMapping.length; i++) {
				text = text + "\n" + rMapping[i][0] + "	C	" + "\"" + "units/cell" + "\"" 
					+ "	" + "\"" + "conc." + "\"" + "	" + "\"" + "step1(t, 0, 0, 1)" + "\"";
				//u1	C	"units/cell"	"conc."	"step1(t, 0, 0, 1)"
			}
		}
		text = text + "\n";
		
		return text;
	}
	
	/**
	 * Maps all variables/parameters (h, alpha, beta, gamma) to their node and for alpha and beta their corresponding position in the list of input nodes.
	 * @param network The Network.
	 * @param mapping 
	 * @param constNodes
	 * @return The Mapped Array where: [i][0] := name of the i'te variable, [i][1] := the node number, [i][2] := -1 for h&gamma and the position of the input node for alpha&beta.
	 */
	private static String[][] getKMapping(RegulatoryNetwork network, String[][] mapping, List<String> constNodes){
		NetworkNode[] nodes = network.getNetworkNodes();
		
		String[][] kMapping = null;// = new String[length][5];
		ArrayList<ArrayList<String>> kMList = new ArrayList<ArrayList<String>>();
		ArrayList<String> kMNode; //one kMapping Node
		boolean[] activators;
		int i = 0;
		for(int n = 0; n < nodes.length; n++) {
			if(constNodes != null && constNodes.contains(mapping[n][1])) {
				continue;
			}
			
			//gamma
			/*kMapping[i][0] = "y_" + (n+1);
			kMapping[i][1] = "" + (n+1);
			kMapping[i][2] = "" + -1;
			kMapping[i][3] = "y";
			kMapping[i][4] = "y";*/
			
			/*kMNode = new ArrayList<String>();
			kMNode.add("y_" + (n+1));
			kMNode.add("" + (n+1));
			kMNode.add("" + -1);
			kMNode.add("y");
			kMNode.add("y");
			i++;
			kMList.add(kMNode);*/
			
			if(nodes[n].getFunction().getArity()==0) { continue; }
			
			//h
			kMNode = new ArrayList<String>();
			kMNode.add("h_" + (n+1));
			kMNode.add("" + (n+1));
			kMNode.add("" + -1);
			kMNode.add("h");
			kMNode.add("h");
			kMNode.add("" + 0);
			i++;
			kMList.add(kMNode);
			
			activators = ((ActivatorInhibitorFunction)nodes[n].getFunction()).getActivators();
			int j = 0;
			//alpha, beta
			while(j < nodes[n].getFunction().getArity()) {
				String sourceNodeName = network.getConnectionSourceName(n,j);
				String nodeNumber = "x";
				String sourceNodeSymbol = "";
				String sourceNodeNumber = "";
				for(int l = 0; l<mapping.length; l++) {
					if(mapping[l][1] == sourceNodeName) {
						nodeNumber = "" + (l+1); //same as sourceNodeNumber
						sourceNodeSymbol = mapping[l][0];
						sourceNodeNumber = mapping[l][2];
						break;
					}
				}
				kMNode = new ArrayList<String>();
				if(activators[j]) {
					kMNode.add("a_" + nodeNumber + "_" + (n+1));
					kMNode.add("" + (n+1));
					kMNode.add("" + j);
					kMNode.add("a");
					kMNode.add("" + sourceNodeSymbol);
					kMNode.add(sourceNodeNumber);
				}
				else {
					kMNode.add("b_" + nodeNumber + "_" + (n+1));
					kMNode.add("" + (n+1));
					kMNode.add("" + j);
					kMNode.add("b");
					kMNode.add("" + sourceNodeSymbol);
					kMNode.add(sourceNodeNumber);
				}
				j++;
				i++;
				kMList.add(kMNode);
			}
		}
		
		kMapping = new String[i][6];
		for(int j = 0; j < i; j++) {
			for(int k = 0; k < 6; k++) {
				kMapping[j][k] = kMList.get(j).get(k);
			}
		}
		//kMapping = kMList.toArray(kMapping);
		return kMapping;
	}
	
	/**
	 * 
	 * @param network
	 * @param mapping
	 * @param kMapping
	 * @param rMapping
	 * @param constantNodes
	 * @return
	 */
	private static String getModelODEs(RegulatoryNetwork network, String[][] mapping, String[][] kMapping, String[][] rMapping, String[] constantNodes){
		String text = "\n" + "ODES" + 
				"\n";
		NetworkNode[] nodes = network.getNetworkNodes();
		BinaryBooleanFunction bbf;
		String nodeName;
		String h;
		String y;
		String w;
		boolean isConstant = false;
		for(int i = 0; i < nodes.length; i++ ) {
			nodeName = "";
			for(int j = 0; j < mapping.length; j++) {
				if(mapping[j][1].equals(nodes[i].getName())) {
					nodeName = mapping[j][0];
					break;
				}
			}
			if(constantNodes != null) {
				isConstant = false;
				for(int j = 0; j < constantNodes.length; j++) {
					if(constantNodes[j].equals(nodes[i].getName())) {
						isConstant = true;
						break;
					}
				}
				if(isConstant) {
					text += "\n" + "\"" + "0" + "\"";
					continue;
				}
			}
			
			h = "";
			y = "1";
			String t = "" + (i+1);
			for(int j = 0; j < kMapping.length; j++) {
				if(kMapping[j][1].equals(t)) {
					h = kMapping[j][0];
					//y = kMapping[j+1][0];
					//y = "1";
					break;
				}
			}
			
			bbf = nodes[i].getFunction();
			boolean[] activators = ((ActivatorInhibitorFunction)bbf).getActivators();
			boolean hasActivators = false;
			boolean hasInhibitors = false;
			for(int j = 0; j < activators.length ; j++) {
				if(activators[j]) {
					hasActivators = true;
				}
				else {
					hasInhibitors = true;
				}
				if(hasActivators && hasInhibitors) {
					break;
				}
			}
			
			String acti = "";
			String aSum = ""; //Sum of alpha
			String aWSum = ""; //Sum of weighted nodes
			String inhi = "";
			String bSum = ""; //Sum of beta
			String bWSum = ""; //Sum of weighted nodes
			for(int k = 0; k < kMapping.length; k++) {
				//if alpha or beta of the right node
				if(kMapping[k][1].equals(t) && Integer.valueOf(kMapping[k][2]) >= 0) {
					if(kMapping[k][3].equals("a")) {
						if(aSum.equals("")) {
							aSum += kMapping[k][0];
							aWSum += kMapping[k][0] + "*" + kMapping[k][4];
						}
						else {
							aSum += "+" + kMapping[k][0];
							aWSum += "+" + kMapping[k][0] + "*" + kMapping[k][4];
						}
					}
					else if(kMapping[k][3].equals("b")) {
						if(bSum.equals("")) {
							bSum += kMapping[k][0];
							bWSum += kMapping[k][0] + "*" + kMapping[k][4];
						}
						else {
							bSum += "+" + kMapping[k][0];
							bWSum += "+" + kMapping[k][0] + "*" + kMapping[k][4];
						}
					}
				}
			}
			if(hasActivators) {
				acti += "(((1+(" + aSum + ")) / (" + aSum + ")) * ((" + aWSum + ") / (1+" + aWSum +")))";
			}
			if(hasInhibitors) {
				inhi += "(1 - (((1+(" + bSum + ")) / (" + bSum + ")) * ((" + bWSum + ") / (1+" + bWSum +"))))";
			}
			
			if(hasActivators && hasInhibitors) {
				w = acti + " * " + inhi;
			}
			else {
				w = acti + inhi;
			}
			
			text += "\n" + "\"";
			//text += "(";
			
			if(bbf.getArity()>0) {
				text += "((-exp(0.5*" + h + ")+exp(-" + h + "*(" + w + "-0.5)))"
					+ " / ((1-exp(0.5*" + h + "))*(1+exp(-" + h + "*(" + w + "-0.5))))" + ")";
			}
			
			text += " - " + y + "*" + nodeName;
			
			//For Up or Down Regulated nodes
			boolean regulated = false;
			boolean up = true;
			String rAlias = "";
			String deltaAlias = "";
			if(rMapping != null) {
				for(int j = 0; j <rMapping.length; j++) {
					
					if(rMapping[j][1].equals(nodes[i].getName())) {
						regulated = true;
						rAlias = rMapping[j][0];
						deltaAlias = rMapping[j][3];
						if(rMapping[j][2].equals("d")) {
							up = false;
						}
						break;
					}
				}
			}
			
			if(regulated) {
				if(up) { text += " + " + deltaAlias + "*" + rAlias + "*(1-" + nodeName + ")"; }
				else { text += " - " + deltaAlias + "*" + rAlias + "*" + nodeName; }
			}
			
			/*text += ")";
			if(isConstant) {
				text +=  " * 0";
			}*/
			text += "\"";
		}
		text = text + "\n";
		/*how the text should look like:
		((-exp(5.0)+exp(-10.0*(((2.0/1.0)*((x(34))/(1+x(34))))-0.5)))
		/((1-exp(5.0))*(1+exp(-10.0*(((2.0/1.0)*((x(34))/(1+x(34))))-0.5)))))-x(9)-u(5)*x(9)
		... +u(2)*(1-x(16))
		"k12 - k5*x3*x2/(k6+x2) - k7*x4*x2/(k8+x2) - k9*x5*x2/(k10+x2) + x12" */
		
		return text;
	}
	
	/**
	 * 
	 * @return The string at the end of the model.def file.
	 */
	private static String getModelTail() {
		String text = "\n" + "DERIVED" + 
				"\n" +
				"\n" + "OBSERVABLES" + 
				"\n" +
				"\n" + "ERRORS" + 
				"\n" +
				"\n" + "CONDITIONS" + 
				"\n";
		return text;
	}
	
	
	public static void main(String[] args) throws IOException{
		String text = "a";
		try {
			
			RegulatoryNetwork network = new RegulatoryNetwork();

	        // Load a yED GraphML file into the network
	        network.loadYEdFile(new File("C:\\Uni\\Job\\Jimena\\ExampleGraphs\\WorkingGraphs\\20221221_lungcancer_D2D_short.graphml"));
	        text = "b";
	        //String[] upRNodes = null;
	        String[] upRNodes = new String[1];
	        upRNodes[0] = "TGFR";
			String[] downRNodes = null;
			
			String[] dataNodes = null;
			//String[] dataNodes = new String[1];
			//dataNodes[0] = "TRPM7";
			
			String[] constantNodes = null;
			//String[] constantNodes = new String[1];
			//constantNodes[0] = "TGFR";
			
			String path = "C:\\Uni\\Job\\Jimena\\ExampleGraphs\\WorkingGraphs\\test.txt";
			String parameterPath = "C:\\Uni\\Job\\Jimena\\JimenaDocs\\Rueckweg\\20230302_ErsterVersuch_parameters.tsv";
	        // specify where to put the new File and how to name it, it will override any existing file with the same name at the same place
			D2DMapping mapping = createFiles(path, network, dataNodes, upRNodes, downRNodes, null, constantNodes, 10);
			String mappingFile = D2DMappingFileInteractor.createD2DMappingFile(path, mapping);
			text = "c";
			D2DMapping recreatedMapping = D2DMappingFileInteractor.getD2DMapping(mappingFile);
			text = "c1";
			ExternalStimuliFileCreator.createFile(path, parameterPath, recreatedMapping, network,null);
			text = "d";
		}
		catch(Exception e) {
			System.out.print(text + "\n" + e.getMessage());
		}
		
		
	}

}
