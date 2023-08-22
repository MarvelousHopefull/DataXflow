package jimena.weightsCalculator.fileCreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import jimena.binarybf.BinaryBooleanFunction;
import jimena.binarybf.actinhibitf.ActivatorInhibitorFunction;
import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.weightsCalculator.D2DMapping;
import jimena.weightsCalculator.gui.TableModel.NodeOfInterest;

/**
 * Used for creating the main.m file for the External Stimuli. Can only be used together with D2D, as some here needed configuration are provided by D2D.
 * 
 * @author Jan Krause
 * @since 07.03.2023
 * */
public class ExternalStimuliFileCreator {

	private static double delta = 1;
	
	/**
	 * Creates the file for the External Stimuli.
	 * 
	 * @param path The String Path where the new File should be saved to.
	 * @param parametersPath The String Path where the File, with the (by D2D) calculated values for the Parameters, is to be found.
	 * @param mapping The saved mapping, done by the DefCreator Class, during the initial usage of the D2D-GUI.
	 * @param network The Network in question. 
	 * @param nodesOfInterestList
	 * @throws Exception
	 */
	public static void createExternalStimuliFile(String path, String parametersPath, D2DMapping mapping, RegulatoryNetwork network, List<NodeOfInterest> nodesOfInterestList) throws Exception {
		if (!path.endsWith("_main.m")) {
			path = path +"_main.m";
		}
		File file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		
		double finalTime = mapping.finalTime();
		String[] constantNodes = mapping.constNodes();
		
		String text = getTextHeader();
		text += getParameters(parametersPath, mapping.parameterMapping(), mapping.regualtorMapping());
		text += getA(nodesOfInterestList, mapping);
		text += getOCP(parametersPath, mapping.nodeMapping(), mapping.regualtorMapping(), finalTime);
		text += getODEs(network, mapping, constantNodes);
		text += getTextTail();
		text += getNodeAlias(mapping);
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
	}
	
	/**
	 * Creates the file for the Switch Analyser.
	 * 
	 * @param path The String Path where the new File should be saved to.
	 * @param model The TableModel where the steady states are to be found.
	 * @param initRow The row in the TableModel where the initial steady state is to be found.
	 * @param targetRow The row in the TableModel where the targeted steady state is to be found.
	 * @param parametersPath The String Path where the File, with the (by D2D) calculated values for the Parameters, is to be found.
	 * @param mapping The saved mapping, done by the DefCreator Class, during the initial usage of the D2D-GUI.
	 * @param network The Network in question. 
	 * @throws Exception
	 */
	public static void createSwitchAnalyserFile(String path, TableModel model, int initRow, int targetRow, String parametersPath, D2DMapping mapping, RegulatoryNetwork network) throws Exception {
		if (!path.endsWith("_main.m")) {
			path = path +"_main.m";
		}
		File file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		
		double finalTime = mapping.finalTime();
		String[] constantNodes = mapping.constNodes();
		
		String text = getTextHeader();
		text += getParameters(parametersPath, mapping.parameterMapping(), mapping.regualtorMapping());
		text += getSwitchAnalyserOCP(model, initRow, mapping.nodeMapping(), mapping.regualtorMapping(), finalTime);
		text += getSwitchAnalyserXD(model, targetRow);
		text += getODEs(network, mapping, constantNodes);
		text += getTextTail();
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
	}
	
	private static String getTextHeader() {
		String text = "function [  ] = main_comp_therapies()" + "\r\n";
		
		// the first block of comments
		text += "%Main-file for calculating external stimuli for a regulatory network" + "\r\n"
				+ "%causing the lowest target functional value" + "\r\n"
				+ "%For details see corresponding paper" + "\r\n"
				+ "\r\n"
				+ "%Parameters determined by the network" + "\r\n"
				+ "%numNodes:     Number of nodes of the network, has to correspond to the number of entries of the right hand-side f" + "\r\n"
				+ "%numControls:  Number of external stimuli of the network, has to correspond to the number of different external stimuli used in the right hand-side f, corresponds to the maximum index of the external stimuli" + "\r\n"
				+ "\r\n"
				+ "%Parameters determined by the user\r\n"
				+ "%timeInterval:  Number, time discretization for the ordinary differential equations" + "\r\n"
				+ "%timeHorizon:   Number, time duration, [0, timeHorizon], in which network is simulated" + "\r\n"
				+ "%alpha:         Number, Weights the contribution of the controls to the target functional" + "\r\n"
				+ "%initialState:  1 x numNodes row vector, The steady state the network is in at the beginning, that is time t=0" + "\r\n"
				+ "\r\n";
		
		text += "%Tolerance parameters" + "\r\n"
				+ "tol2=10^-14;            %Tolerance for the stopping criterion for the sequential quadratic Hamiltonian method, input in function SQH_method" + "\r\n"
				+ "tol3=10^-4;             %Tolerance for the stopping criterion for the projected gradient method, input in function projected_gradient_method" + "\r\n"
				+ "max_Num=1;              %Input in function combinatorial_method, Maximum number of external stimuli applied at once to the network in order to find a set of external stimuli that cause the desired switch, max_Num = 1,...,numControls" + "\r\n"
				+ "max_iter=10000;         %Maximum number of updates on the control for the sequential quadratic Hamiltonian method or maximum number of iterations of the projected gradient method" + "\r\n"
				+ "\r\n"
				+ "numColu=2;              %Time curves of the active external stimuli are plotted in a window with two columns" + "\r\n"
				+ "numColx=2;              %Time curves of the nodes of interest are plotted in a window with two columns" + "\r\n"
				+ "intv=3;                 %Number of intervals of equal length into which the range of the external stimuli [0,1] is divided" + "\r\n"
				+ "\r\n"
				+ "%flags" + "\r\n"
				+ "combi_method=1;                 %If flag equals 1, a combinatorial search with the function combinatorial_method by trial and error search is performed in order to determine external stimuli causing the desired switch, if flag equals 0 it is not performed" + "\r\n"
				+ "local_optimization_method=1;    %If flag equals 0, no local optimization method is performed, if local_optimization_method equals 1, then the sequential quadratic Hamiltonian (SQH) method is performed, if local_optimization_method equals 2, then a projected gradient method is performed" + "\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "%Variables" + "\r\n"
				+ "%x:         numNodes x ((timeHorizon/timeInterval)+1)-matrix, state of the network, row i corresponds" + "\r\n"
				+ "%           to the  node i, i=1,...,numNodes, column j corresponds to" + "\r\n"
				+ "%           the time t=(j-1)*timeInterval, j=1,...,((timeHorizon/timeInterval)+1), entry (i,j) corresponds to value x(i) of the i-th" + "\r\n"
				+ "%           node at time t=(j-1)*timeInterval" + "\r\n"
				+ "%xd:        number nodes of interest x ((timeHorizon/timeInterval)+1)-matrix, Desired activity level of the nodes of interest," + "\r\n"
				+ "%           row i corresponds to the desired state of the i-th node of interest, column j corresponds to the time" + "\r\n"
				+ "%           t=(j-1)*timeInterval, j=1,...,((timeHorizon/timeInterval)+1), entry (i,j) value of the i-th" + "\r\n"
				+ "%           desired state xd(i) of the i-th node of interest at time t=(j-1)*timeInterval" + "\r\n"
				+ "%u:         numControls x (timeHorizon/timeInterval)-matrix, external stimuli, row" + "\r\n"
				+ "%           i corresponds to the external stimulus u(i), i=1,...,numControls, column j" + "\r\n"
				+ "%           corresponds to the time t=(j-1)*timeInterval," + "\r\n"
				+ "%           j=1,...,(timeHorizon/timeInterval), entry (i,j) value of the i-th" + "\r\n"
				+ "%           external stimulus u(i) at time t=(j-1)*timeInterval" + "\r\n"
				+ "\r\n";
		
		
		
		return text;
	}
	
	private static String getParameters(String parametersPath, String[][] parameterMapping, String[][] regulatorMapping) throws Exception {
		File file = new File(parametersPath);
		if(!file.exists()) { 
			throw new Exception("Parameter File doesn't exist!");
		}
		
		ArrayList<String> deltasList = new ArrayList<String>();
		String text = "";
		String parametersText = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(parametersPath))) {
			String line = reader.readLine();
			String parameterName = "";
			String parameterValue = "";
			int parameterLinesAmount = 0;
			while(line != null) {
				//not the title line 
				if(!line.startsWith("parameterId")) {
					String[] parts = line.split("\\	");
					if(parts.length < 6) {
						throw new Exception("Some Parameter seems to not have sufficient Informations in the Parameter File!");
					}
					//all non x parameters
					if(!line.startsWith("init_x")) {
						if(line.startsWith("delta_")) {
							int i;
							for(i = 0; i < regulatorMapping.length; i++) {
								if(regulatorMapping[i][3].equals(parts[0])) {
									break;
								}
							}
							if(i == regulatorMapping.length) {
								throw new Exception("Some Parameter doesn't appear in the mappint!");
							}
							if(regulatorMapping[i][4].equals("false")) {
								line = reader.readLine();
								continue;
							}
							deltasList.add(parts[0]);
						}
						parameterName = parts[0];
						parameterValue = parts[5];
						parametersText += parameterName + "=" + parameterValue + ";\r\n";
						parameterLinesAmount++;
					}
					
				}
				line = reader.readLine();
			}
			if(parameterLinesAmount != parameterMapping.length) {
				//throw new Exception("There seem to be an unequal amount of parameters in the referenced topology and the File provided by D2D!");
			}
			for(int i = 0; i < regulatorMapping.length; i++) {
				if(!deltasList.contains(regulatorMapping[i][3]) && !regulatorMapping[i][4].equals("false")) {
					parameterName = regulatorMapping[i][3];
					parameterValue = "" + delta;
					parametersText += parameterName + "=" + parameterValue + ";\r\n";
				}
			}
		}
		parametersText += "\r\n";
		text += parametersText;
		return text;
	}
	
	private static String getA(List<NodeOfInterest> nodesOfInterestList, D2DMapping mapping) {
		String text = "";
		String nodeValueText = "";
		boolean firstElement = true;
		int nodeNumber = -1;
		String[][] nodes = mapping.nodeMapping();
		for(NodeOfInterest node : nodesOfInterestList) {
			if(!firstElement) {
				nodeValueText += ";";
			}
			else {
				firstElement = false;
			}
			for(int i = 0; i < nodes.length; i++) {
				if(nodes[i][1].equals(node.getNodeName())) {
					nodeNumber = Integer.valueOf(nodes[i][2]);
					break;
				}
			}
			nodeValueText += nodeNumber + "," + node.getTargetedValue() + "," + node.getNodeWeight();
		}
		text += "A=[" + nodeValueText + "];";
		text += "           %Matrix where each row is for a node of interest, first column is for its index which it has in the network, second column is its desired constant activity level, third column is its corresponding weight in the target functional\r\n";
		text += "\r\n";
		return text;
	}
	
	private static String getOCP(String parametersPath, String[][] nodeMapping, String[][] regulatorMapping, double finalTime) throws Exception {
		String text = "";
		try (BufferedReader reader = new BufferedReader(new FileReader(parametersPath))) {
			String line = reader.readLine();
			String parameterValue = "";
			String nodeValueText = "";
			int numberOfNodes = 0;
			int numberOfControls = 0;
			ArrayList<String> nodeList = new ArrayList<String>();
			ArrayList<Integer> nodeIDs = new ArrayList<Integer>();
			while(line != null) {
				//not the title line 
				if(!line.startsWith("parameterId")) {
					String[] nodeParts = line.split("\\	");
					if(nodeParts.length < 6) {
						throw new Exception("Some Parameter seems to not have sufficient Informations in the Parameter File!");
					}
					//x values
					if(line.startsWith("init_x")) {
						String[] idString = nodeParts[0].split("_x");
						int id = Integer.valueOf(idString[1]);
						int i = 0;
						//sorting by ID
						while(i < nodeIDs.size()) {
							if(nodeIDs.get(i) > id) {
								break;
							}
							i++;
						}
						if(i == nodeIDs.size()) {
							nodeList.add(nodeParts[5]);
							nodeIDs.add(id);
						}else {
							nodeList.add(i, nodeParts[5]);
							nodeIDs.add(i,id);
						}
					}
				}
				line = reader.readLine();
			}
			for(int i = 0; i < nodeList.size(); i++) {
				if(nodeValueText != "") {
					nodeValueText += ",";
				}
				parameterValue = nodeList.get(i);
				nodeValueText += parameterValue;
				numberOfNodes++;
			}
			if(numberOfNodes != nodeMapping.length) {
				//throw new Exception("There seem to be an unequal amount of nodes in the referenced topology and the File provided by D2D!");
			}
			if(regulatorMapping != null) {
				numberOfControls = regulatorMapping.length;
			}
			text += "OCP = struct('numNodes',"
					+ numberOfNodes
					+ ",'numControls',"
					+ numberOfControls
					+ ",'timeInterval',0.1,'timeHorizon'," + finalTime + ",'alpha',0,'initialState',[";
			text += nodeValueText;
		}
		text += "],'DataNoi',A);" + "\r\n"
				+ "\r\n";
		text += "xd = get_xd(OCP); %Creates the desired state in which the nodes of interest are expected to be" + "\r\n"
				+ "\r\n";
		return text;
	}
	
	/**
	 * Creates the String representing the initial steady state.
	 * @param model The TableModel where the steady states are to be found.
	 * @param initRow The row in the TableModel where the initial steady state is to be found.
	 * @param nodeMapping
	 * @param regulatorMapping
	 * @param finalTime
	 * @return
	 * @throws Exception
	 */
	private static String getSwitchAnalyserOCP(TableModel model, int initRow, String[][] nodeMapping, String[][] regulatorMapping, double finalTime) throws Exception {
		String text = "";
				
		//BufferedReader reader = new BufferedReader(new FileReader(parametersPath))) {
		//String line = reader.readLine();
		String parameterValue = "";
		String nodeValueText = "";
		int numberOfNodes = 0;
		int numberOfControls = 0;
		ArrayList<String> nodeList = new ArrayList<String>();
		for(int i = 0; i < model.getColumnCount(); i++) {
			nodeList.add(model.getValueAt(initRow, i).toString());
		}
			
		for(int i = 0; i < nodeList.size(); i++) {
			if(nodeValueText != "") {
				nodeValueText += ",";
			}
			parameterValue = nodeList.get(i);
			nodeValueText += parameterValue;
			numberOfNodes++;
		}
		if(numberOfNodes != nodeMapping.length) {
			//throw new Exception("There seem to be an unequal amount of nodes in the referenced topology and the File provided by D2D!");
		}
		if(regulatorMapping != null) {
			numberOfControls = regulatorMapping.length;
		}
		text += "OCP = struct('numNodes',"
				+ numberOfNodes
				+ ",'numControls',"
				+ numberOfControls
				+ ",'timeInterval',0.1,'timeHorizon'," + finalTime + ",'alpha',0,'initialState',[";
		text += nodeValueText;
		
		text += "],'DataNoi',A);" + "\r\n"
				+ "\r\n";
		return text;
	}
	
	/**
	 * Creates the String representing the target steady state.
	 * @param model The TableModel where the steady states are to be found.
	 * @param targetRow The row in the TableModel where the targeted steady state is to be found.	 
	 * @return
	 */
	private static String getSwitchAnalyserXD(TableModel model, int targetRow) {
		String text = "";
		
		String parameterValue = "";
		String nodeValueText = "";
		
		ArrayList<String> nodeList = new ArrayList<String>();
		for(int i = 0; i < model.getColumnCount(); i++) {
			nodeList.add(model.getValueAt(targetRow, i).toString());
		}
		
		for(int i = 0; i < nodeList.size(); i++) {
			if(nodeValueText != "") {
				nodeValueText += ",";
			}
			parameterValue = nodeList.get(i);
			nodeValueText += parameterValue;
		}
		
		text += "xd = get_xd(";
		text += nodeValueText;
		text += ",OCP); %Creates the desired state in which the nodes of interest are expected to be" + "\r\n"
				+ "\r\n";
		return text;
	}
	
	private static String getODEs(RegulatoryNetwork network, D2DMapping mapping, String[] constantNodes) {
		String text = "";
		text += "%f is the right hand-side of the ordinary differential equation dx(t)/dt=f(x(t),u(t)) corresponding to the network, here as a list of function handles" + "\r\n";
		text += "f = {";
		String text1 = "";
		NetworkNode[] nodes = network.getNetworkNodes();
		String[][] nodeMapping = mapping.nodeMapping();
		String[][] rMapping = mapping.regualtorMapping();
		String[][] kMapping = mapping.parameterMapping();
		
		BinaryBooleanFunction bbf;
		String nodeName;
		String h;
		String y;
		String w;
		boolean isConstant = false;
		for(int i = 0; i < nodeMapping.length; i++) {
			int index = -1;
			text1 = "";
			nodeName = "";
			for(int j = 0; j < nodes.length; j++) {
				if(nodeMapping[i][1].equals(nodes[j].getName())) {
					nodeName = "x(" + nodeMapping[i][2] + ")";
					index = j;
					break;
				}
			}
			text1 += "@(x,u)";
			if(constantNodes != null) {
				isConstant = false;
				for(int j = 0; j < constantNodes.length; j++) {
					if(constantNodes[j].equals(nodes[index].getName())) {
						isConstant = true;
						break;
					}
				}
				if(isConstant) {
					text1 += "0";
					//if last
					if((i+1) == nodeMapping.length) {
						text1 += "};" + "\r\n";
					}
					else {
						text1 +=   ",..." + "\r\n";
					}
					if(!text.endsWith("{")) {
						text += "	" + text1;
					}
					else {
						text += text1;
					}
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
			
			bbf = nodes[index].getFunction();
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
			
			//activating/inhibiting inputs
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
							aWSum += kMapping[k][0] + "*x(" + kMapping[k][5] + ")";
						}
						else {
							aSum += "+" + kMapping[k][0];
							aWSum += "+" + kMapping[k][0] + "*x(" + kMapping[k][5] + ")";
						}
					}
					else if(kMapping[k][3].equals("b")) {
						if(bSum.equals("")) {
							bSum += kMapping[k][0];
							bWSum += kMapping[k][0] + "*x(" + kMapping[k][5] + ")";
						}
						else {
							bSum += "+" + kMapping[k][0];
							bWSum += "+" + kMapping[k][0] + "*x(" + kMapping[k][5] + ")";
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
			
			if(bbf.getArity()>0) {
				text1 += "((-exp(0.5*" + h + ")+exp(-" + h + "*(" + w + "-0.5)))"
					+ " / ((1-exp(0.5*" + h + "))*(1+exp(-" + h + "*(" + w + "-0.5))))" + ")";
			}
			
			text1 += " - " + y + "*" + nodeName;
			
			//For Up or Down Regulated nodes
			boolean up = false;
			boolean down = false;
			String rAliasU = "";
			String deltaAliasU = "";
			String rAliasD = "";
			String deltaAliasD = "";
			if(rMapping != null) {
				for(int j = 0; j <rMapping.length; j++) {
					
					if(rMapping[j][1].equals(nodes[index].getName())) {
						if(rMapping[j][4].equals("false")) {
							continue;
						}
						
						if(!up && rMapping[j][2].equals("u")) {
							up = true;
							rAliasU = "u(" + rMapping[j][5] + ")";
							deltaAliasU = rMapping[j][3];
						}
						if(!down && rMapping[j][2].equals("d")) {
							down = true;
							rAliasD = "u(" + rMapping[j][5] + ")";
							deltaAliasD = rMapping[j][3];
						}
					}
				}
			}
			if(up) { text1 += " + " + deltaAliasU + "*" + rAliasU + "*(1-" + nodeName + ")"; }
			if(down) { text1 += " - " + deltaAliasD + "*" + rAliasD + "*" + nodeName; }
			
			if((i+1) == nodeMapping.length) {
				text1 += "};" + "\r\n";
			}
			else {
				text1 +=   ",..." + "\r\n";
			}
			if(!text.endsWith("{")) {
				text += "	" + text1;
			}
			else {
				text += text1;
			}
		}
		text += "\r\n";
		return text;
	}
	
	private static String getTextTail() {
		String text = "u=zeros(OCP.numControls,round(OCP.timeHorizon/OCP.timeInterval));   %Initial guess for the controls if no heuristical search is performed before the local optimization framework, any control can be set to any value between 0 and 1 for an initial guess for example ones() instead of zeros()" + "\r\n"
				+ "\r\n"
				+ "if(combi_method==1)                                                 %Block for the combinatorial method" + "\r\n"
				+ "    u=combinatorial_method(f,xd,max_Num,intv,OCP);" + "\r\n"
				+ "end" + "\r\n"
				+ "\r\n"
				+ "if(local_optimization_method==1 || local_optimization_method==2)                             %Block for the local optimization method" + "\r\n"
				+ "    [df_x,cmx,df_u,cmu]=createJacobian(f,OCP);                                               %Creates derivatives of f with respect to x and u, Jacobian of the right hand side f" + "\r\n"
				+ "    if(local_optimization_method==1)" + "\r\n"
				+ "        u=SQH_method( @get_J_SQH,f,df_x,cmx,df_u,cmu,tol2,u,xd,max_iter,OCP); %Sequential quadratic Hamiltonian method as a local optimization scheme, returns u, u the external stimuli optimizing the target functional" + "\r\n"
				+ "                                                                              %Input: @get_J function handle for the target functional, f right hand-side of the ordinary differential equation corresponding to the network with dx/dt=f(x(t),u(t))" + "\r\n"
				+ "                                                                              %df_x function handle for the derivative of f with respect to x, cmx notes the nonzero elements of df_x, df_u function handle for the derivative of f with respect to u, cmu notes the nonzero elements of df_u, see output function createJacobian" + "\r\n"
				+ "                                                                              %u initial guess for the external stimuli, can be taken from the combinatorial method" + "\r\n"
				+ "                                                                              %xd desired state for the values x of the corresponding nodes, tol2 stopping criterion, max_iter maximum number of updates on the control u of the sequential quadratic Hamiltonian" + "\r\n"
				+ "    end" + "\r\n"
				+ "\r\n"
				+ "    if (local_optimization_method==2)" + "\r\n"
				+ "        [u,~,~]=projected_gradient_method( @get_gradient, @projection, @get_J,f,df_x,cmx,df_u,cmu, u, xd, tol3, max_iter, OCP );    %Projected gradient method as a local optimization scheme, returns [u,J,count], u the external stimuli optimizing the target functional" + "\r\n"
				+ "                                                                                                                                    %Input:@get_gradient function handle for the gradient of the reduced target functional, @projection function handle of the projection, projects u into [0,1]" + "\r\n"
				+ "                                                                                                                                    %@get_J function handle for the target functional, f right hand-side of the ordinary differential equation corresponding to the network with dx/dt=f(x(t),u(t))" + "\r\n"
				+ "                                                                                                                                    %df_x function handle for the derivative of f with respect to x, cmx notes the nonzero elements of df_x, df_u function handle for the derivative of f with respect to u, cmu notes the nonzero elements of df_u, see output function createJacobian" + "\r\n"
				+ "                                                                                                                                    %u initial guess for the external stimuli, can be taken from the combinatorial method" + "\r\n"
				+ "                                                                                                                                    %xd desired state for the values x of the corresponding nodes, tol2 stopping criterion, max_iter maximum iteration number of the projected gradient method" + "\r\n"
				+ "    end" + "\r\n"
				+ "end" + "\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "drawStimuli( u,numColu,OCP );           %Draws time curves of the of the active external stimuli" + "\r\n"
				+ "\r\n"
				+ "x=forward(f,u,OCP);                     %Calculates the state of the network corresponding to the external stimuli u calculated with the schemes above" + "\r\n"
				+ "\r\n"
				+ "drawStates(x,numColx,OCP);              %Draws time curves of the activity level of the nodes of interest" + "\r\n"
				+ "\r\n"
				+ "fprintf('\\n')" + "\r\n"
				+ "fprintf('Save data to file...\\n');" + "\r\n"
				+ "dlmwrite('x.txt',[0:OCP.timeInterval:round(OCP.timeHorizon/OCP.timeInterval)*OCP.timeInterval;x]);               %Writes the state x in a text-file \"x.txt\" where the first row corresponds to the discrete time steps, separated by commas, row i=2,...,numNode+1 corresponds to state x(i-1), values in the columns, separated by commas, value of x(i) at the corresponding time step" + "\r\n"
				+ "dlmwrite('u.txt',[0:OCP.timeInterval:(round(OCP.timeHorizon/OCP.timeInterval)-1)*OCP.timeInterval;u]);           %Writes the external stimuli u in a text-file \"u.txt\" where the first row corresponds to the discrete time steps, separated by commas, row i=2,...,numControls+1 corresponds to external stimulus u(i), values in the columns, separated by commas, value of u(i) at the corresponding time step" + "\r\n"
				+ "fprintf('Done!\\n');" + "\r\n"
				+ "end" + "\r\n";
		return text;
	}
	
	private static String getNodeAlias(D2DMapping mapping) {
		String[][] nodeMapping = mapping.nodeMapping();
		String text = "\r\n"
				+ "%Nodes:" + "\r\n";
		for(int i = 0; i<nodeMapping.length; i++) {
			text += "%" + nodeMapping[i][1] + "	" + nodeMapping[i][0] + "\r\n";
		}
				
		
		return text;
	}
}
