package jimena.weightsCalculator.fileCreator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

import jimena.binarybf.BinaryBooleanFunction;
import jimena.binarybf.actinhibitf.ActivatorInhibitorFunction;
import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;

import java.io.File;

/**
 * Used for creating .def files. Used by D2D, to calculated best fitting weights.
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
	 * Creates a File from a given Network in the format that is requested by D2D.
	 * @param path The String Path where the File should be saved to.
	 * @param network The Network in question. 
	 * @param upRNodes Nodes that will be up-regulated.
	 * @param downRNodes Nodes that will be down-regulated.
	 * @throws IOException
	 */
	public static void createFile(String path, RegulatoryNetwork network, String[] upRNodes, String[] downRNodes) throws IOException{
		if(fileExists(path)) { 
			File file = new File(path); 
			file.delete();
		}
		//if(fileExists(path)) { return; }
		
		//mapping to node alias (x1, x2, ...)
		//[i][0] := alias (x1|x2|...)
		//[i][1] := Node-Name
		NetworkNode[] nodes = network.getNetworkNodes();
		String[][] mapping = new String[nodes.length][2];
		for(int i = 0; i < nodes.length; i++ ) {
			mapping[i][0] = "x" + (i+1);
			mapping[i][1] = nodes[i].getName();
		}
		
		//mapping of the regulated nodes, up regulated Nodes before down regulated Nodes
		//[i][0] := alias (u1|u2|...)
		//[i][1] := Node-Name
		//[i][2] := up or down (u|d)
		String[][] rMapping = null;
		if((upRNodes != null && upRNodes.length > 0) || (downRNodes != null && downRNodes.length > 0 )) {
			int upL = 0; 
			if(upRNodes != null) {
				upL = upRNodes.length;
			}
			int downL = 0;
			if(downRNodes != null) {
				downL = downRNodes.length;
			}
			rMapping = new String[upL + downL][3];
			int i = 0;
			if(upRNodes != null) {
				for(int j = 0; j < upRNodes.length; j++) {
					rMapping[i][0] = "u" + (i+1);
					rMapping[i][1] = upRNodes[j];
					rMapping[i][2] = "u";
					i++;
				}
			}
			if(downRNodes != null) {
				for(int j = 0; j < downRNodes.length; j++) {
					rMapping[i][0] = "u" + (i+1);
					rMapping[i][1] = downRNodes[j];
					rMapping[i][2] = "d";
					i++;
				}
			}
		}
		
		String[][] kMapping = getKMapping(network, mapping);
		
		String text = getDescriptionAndPredictor();
		text += getCompartments();
		text += getStates(mapping, network);
		text += getInputs(rMapping);
		text += getODEs(network,mapping,kMapping, rMapping);
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
		
		//addODEs(path, network, mapping);
	}
	
	/**
	 * 
	 * @return The String that is the Head of the Document.
	 */
	private static String getDescriptionAndPredictor() {
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
				"\n" + "t	T	" + "\"" + "min" + "\"" + "	" + "\"" + "time" + "\"" + "	0	10" +
				"\n";
		return text;
	}
	
	/**
	 * 
	 * @return The String that represents the Compartments Part for the Document.
	 */
	private static String getCompartments(){
		
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
	private static String getStates(String[][] mapping, RegulatoryNetwork network){
		
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
					"\"" + "	cyt 1 " + "\"" + nodes[i].getName() +  "\"" + "	1";
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
	private static String getInputs(String[][] rMapping) {
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
	 * Maps all variables (h, alpha, beta, gamma) to their node and for alpha and beta their corresponding position in the list of input nodes.
	 * @param network The Network.
	 * @return The Mapped Array where: [i][0] := name of the i'te variable, [i][1] := the node number, [i][2] := -1 for h&gamma and the position of the input node for alpha&beta.
	 */
	private static String[][] getKMapping(RegulatoryNetwork network, String[][] mapping){
		int length = 0;
		NetworkNode[] nodes = network.getNetworkNodes();
		BinaryBooleanFunction bbf;
		
		for(int i = 0; i < nodes.length; i++) {
			bbf = nodes[i].getFunction();
			length += (2 + bbf.getArity());
		}
		
		String[][] kMapping = new String[length][5];
		
		boolean[] activators;
		int i = 0;
		for(int n = 0; n < nodes.length; n++) {
			//h
			kMapping[i][0] = "h_" + (n+1);
			kMapping[i][1] = "" + (n+1);
			kMapping[i][2] = "" + -1;
			kMapping[i][3] = "h";
			kMapping[i][4] = "h";
			i++;
			//gamma
			kMapping[i][0] = "y_" + (n+1);
			kMapping[i][1] = "" + (n+1);
			kMapping[i][2] = "" + -1;
			kMapping[i][3] = "y";
			kMapping[i][4] = "y";
			i++;
			activators = ((ActivatorInhibitorFunction)nodes[n].getFunction()).getActivators();
			//int a = 1;
			//int b = 1;
			int j = 0;
			//alpha, beta
			while(j < nodes[n].getFunction().getArity()) {
				String nodeName = network.getConnectionSourceName(n,j);
				String nodeNumber = "x";
				String nodeSymbol = "";
				for(int l = 0; l<mapping.length; l++) {
					if(mapping[l][1] == nodeName) {
						nodeNumber = "" + (l+1);
						nodeSymbol = mapping[l][0];
						break;
					}
				}
				if(activators[j]) {
					kMapping[i][0] = "a_" + nodeNumber + "_" + (n+1);
					kMapping[i][1] = "" + (n+1);
					kMapping[i][2] = "" + j;
					kMapping[i][3] = "a";
					kMapping[i][4] = nodeSymbol;
				}
				else {
					kMapping[i][0] = "b_" + nodeNumber + "_" + (n+1);
					kMapping[i][1] = "" + (n+1);
					kMapping[i][2] = "" + j;
					kMapping[i][3] = "b";
					kMapping[i][4] = nodeSymbol;
				}
				j++;
				i++;
			}
		}
		return kMapping;
	}
	
	/**
	 * 
	 * @param network
	 * @param mapping
	 * @param kMapping
	 * @param rMapping
	 * @return
	 */
	private static String getODEs(RegulatoryNetwork network, String[][] mapping, String[][] kMapping, String[][] rMapping){
		String text = "\n" + "ODES" + 
				"\n";
		NetworkNode[] nodes = network.getNetworkNodes();
		BinaryBooleanFunction bbf;
		String nodeName;
		String h;
		String y;
		String w;
		for(int i = 0; i < nodes.length; i++ ) {
			nodeName = "";
			for(int j = 0; j < mapping.length; j++) {
				if(mapping[j][1].equals(nodes[i].getName())) {
					nodeName = mapping[j][0];
					break;
				}
			}
			
			h = "";
			y = "";
			String t = "" + (i+1);
			for(int j = 0; j < kMapping.length; j++) {
				if(kMapping[j][1].equals(t)) {
					h = kMapping[j][0];
					y = kMapping[j+1][0];
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
			
			w = "w_" + i;
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
			if(bbf.getArity()>0) {
				text += "((-exp(0.5*" + h + ")+exp(-" + h + "*(" + w + "-0.5)))"
					+ " / ((1-exp(0.5*" + h + "))*(1+exp(-" + h + "*(" + w + "-0.5))))" + ")";
			}
			
			text += " - " + y + "*" + nodeName;
			
			//For Up or Down Regulated nodes
			boolean regulated = false;
			boolean up = true;
			String rAlias = "";
			if(rMapping != null) {
				for(int j = 0; j <rMapping.length; j++) {
					System.out.print(i + " " + j);
					
					if(rMapping[j][1].equals(nodes[i].getName())) {
						regulated = true;
						rAlias = rMapping[j][0];
						if(rMapping[j][2].equals("d")) {
							up = false;
						}
						break;
					}
				}
			}
			
			if(regulated) {
				if(up) { text += " + " + rAlias + "*(1-" + nodeName + ")"; }
				else { text += " - " + rAlias + "*" + nodeName; }
			}
			
			text = text + "\"";
		}
		text = text + "\n";
		/*((-exp(5.0)+exp(-10.0*(((2.0/1.0)*((x(34))/(1+x(34))))-0.5)))
		/((1-exp(5.0))*(1+exp(-10.0*(((2.0/1.0)*((x(34))/(1+x(34))))-0.5)))))-x(9)-u(5)*x(9)
		... +u(2)*(1-x(16))
		"k12 - k5*x3*x2/(k6+x2) - k7*x4*x2/(k8+x2) - k9*x5*x2/(k10+x2) + x12" */
		
		return text;
	}
	
	
	public static void main(String[] args) throws IOException{
		String text = "a";
		try {
			
			RegulatoryNetwork network = new RegulatoryNetwork();

	        // Load a yED GraphML file into the network
	        network.loadYEdFile(new File("C:\\Uni\\Job\\Jimena\\WorkingGraphs\\20221221_lungcancer_D2D_short.graphml"));
	        text = "b";
	        String[] upRNodes = null;//new String[1];
	        //upRNodes[0] = "TRPM7";
			String[] downRNodes = null;
	        // specify where to put the new File and how to name it, it will override any existing file with the same name at the same place
			createFile("C:\\Uni\\Job\\Jimena\\WorkingGraphs\\20221221_lungcancer_D2D_short.def", network, upRNodes, downRNodes);
			text = "c";
		}
		catch(Exception e) {
			System.out.print(text + e.getMessage());
		}
		
		
	}

}
