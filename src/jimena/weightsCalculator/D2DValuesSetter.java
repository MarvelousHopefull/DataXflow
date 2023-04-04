package jimena.weightsCalculator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeSet;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;

/**
 * Used for setting the values and parameters in the network calculated by D2D.
 * @author Jan Krause
 *
 */

public class D2DValuesSetter {
	
	/**
	 * Sets the parameters of the network, provided by D2D.
	 * @param network The network whose parameters are to be set.
	 * @param d2DParametersFile The file where the values are saved.
	 * @param mapping The mapping done for D2D.
	 * @throws Exception
	 */
	public static void setNetworkValues(RegulatoryNetwork network, File d2DParametersFile, D2DMapping mapping) {
		
		try {
			setInitValues(network, d2DParametersFile, mapping);
			setSteepness(network, d2DParametersFile, mapping);
			setWeights(network, d2DParametersFile, mapping);
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	/**
	 * Sets the initial values of the nodes.
	 * @param network The network whose nodes are to be set.
	 * @param d2DParametersFile The file where the values are saved.
	 * @param mapping The mapping done for D2D.
	 * @throws Exception
	 */
	protected static void setInitValues(RegulatoryNetwork network, File d2DParametersFile, D2DMapping mapping) throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader(d2DParametersFile))) {
			String line = reader.readLine();
			ArrayList<Double> valuesList = new ArrayList<Double>();
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
						String[] idString = nodeParts[0].split("_");
						String alias = idString[1];
						String nodeName = mapping.getNodeNameByAlias(alias);
						int index = network.getNodeIndexByName(nodeName);
						int i = 0;
						//sorting by index
						while(i < nodeIDs.size()) {
							if(nodeIDs.get(i) > index) {
								break;
							}
							i++;
						}
						if(i == nodeIDs.size()) {
							valuesList.add(Double.valueOf(nodeParts[5]));
							nodeIDs.add(index);
						}else {
							valuesList.add(i, Double.valueOf(nodeParts[5]));
							nodeIDs.add(i,index);
						}
					}
				}
				line = reader.readLine();
			}
			
			double[] values = new double[valuesList.size()];
			for(int i = 0; i < valuesList.size(); i++) {
				values[i] = valuesList.get(i);
			}
			
			network.setValues(values);
		}
	}
	
	/**
	 * Sets the SQUAD steepness for the nodes.
	 * @param network The network whose nodes are to be set.
	 * @param d2DParametersFile The file where the values are saved.
	 * @param mapping The mapping done for D2D.
	 * @throws Exception
	 */
	protected static void setSteepness(RegulatoryNetwork network, File d2DParametersFile, D2DMapping mapping) throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader(d2DParametersFile))) {
			String line = reader.readLine();
			NetworkNode[] nodes = network.getNetworkNodes();
			while(line != null) {
				//not the title line 
				if(!line.startsWith("parameterId")) {
					String[] hParts = line.split("\\	");
					if(hParts.length < 6) {
						throw new Exception("Some Parameter seems to not have sufficient Informations in the Parameter File!");
					}
					//h values
					if(line.startsWith("h_")) {
						String alias = hParts[0];
						String nodeName = mapping.getNodeNameByHAlias(alias);
						if(nodeName != null) {
							int index = network.getNodeIndexByName(nodeName);
							nodes[index].setSQUADSteepness(Double.valueOf(hParts[5]));
						}
					}
				}
				line = reader.readLine();
			}
		}
	}
	
	/**
	 * Sets the SQUAD weights for the connections of the nodes.
	 * @param network The network whose connections are to be set.
	 * @param d2DParametersFile The file where the values are saved.
	 * @param mapping The mapping done for D2D.
	 * @throws Exception
	 */
	protected static void setWeights(RegulatoryNetwork network, File d2DParametersFile, D2DMapping mapping) throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader(d2DParametersFile))) {
			String line = reader.readLine();
			NetworkNode[] nodes = network.getNetworkNodes();
			int i = 0;
			while(line != null) {
				//not the title line 
				if(!line.startsWith("parameterId")) {
					String[] weightParts = line.split("\\	");
					if(weightParts.length < 6) {
						throw new Exception("Some Parameter seems to not have sufficient Informations in the Parameter File!");
					}
					//weights values
					if(line.startsWith("a_") || line.startsWith("b_")) {
						i++;
						String alias = weightParts[0];
						String targetNodeName = mapping.getTargetNodeNameByWeightAlias(alias);
						String sourceNodeName = mapping.getSourceNodeNameByWeightAlias(alias);		
						if(targetNodeName != null && sourceNodeName != null) {
							int targetIndex = network.getNodeIndexByName(targetNodeName);
							TreeSet<Integer> tSet = network.getConnectionPositionsByName(targetNodeName, sourceNodeName);
							if(tSet.size()>0) {
								int connectionPosition = tSet.first();
								nodes[targetIndex].getSQUADWeights()[connectionPosition] = Double.valueOf(weightParts[5]);
							}
						}
					}
				}
				line = reader.readLine();
			}
		}
	}
}
