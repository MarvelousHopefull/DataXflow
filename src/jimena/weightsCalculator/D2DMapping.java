/**
 * 
 */
package jimena.weightsCalculator;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Saves the mapping done for D2D, to use to create the ExternalStimuli File afterwards.
 * 
 * @author Jan Krause
 *
 */
public class D2DMapping {
	//mapping to node alias (x1, x2, ...)
	//[i][0] := alias (x1|x2|...)
	//[i][1] := Node-Name
	private String[][] nodeMapping;
	
	//mapping of the regulated nodes, up regulated Nodes before down regulated Nodes
	//[i][0] := alias (u1|u2|...)
	//[i][1] := Node-Name
	//[i][2] := up or down (u|d)
	//[i][3] := delta-Alias	(delta_1|delta_2|...)
	private String[][] regulatorMapping;
	
	//mapping of the parameters (k := source node number, j := node number)
	//alpha(a) and beta(b) represent edge weights between nodes (a : activating edge, b: inhibiting edge)
	//[i][0] := name of the i'te variable (a_k_j|b_k_j|h_j)
	//[i][1] := the node number j
	//[i][2] := -1 for h&gamma and the position of the input node for alpha&beta.
	//[i][3] := the parameter type (a|b|h)
	//[i][4] := the source node alias for a&b (xk) and h for h (h)
	//[i][5] := the source node number k
	private String[][] parameterMapping;
	
	//nodes that have a constant Value
	private String[] constNodes;
	
	//the final time of the experiment
	private double finalTime;
	
	/**
	 * 
	 * @param nodeMapping The alias mapping for the Nodes.
	 * @param regulatorMapping The alias mapping for the up and down regulated Nodes.
	 * @param parameterMapping The alias mapping for the different parameters.
	 * @param finalTime The Time of the experiment.
	 */
	public D2DMapping(String[][] nodeMapping, String[][] regulatorMapping, String[][] parameterMapping, String[] constNodes, double finalTime) throws Exception {
		if(nodeMapping == null || parameterMapping == null) {
			throw new Exception("nodeMapping and parameterMapping cann't be null!");
		}
		this.nodeMapping = nodeMapping;
		this.regulatorMapping = regulatorMapping;
		this.parameterMapping = parameterMapping;
		this.constNodes = constNodes;
		this.finalTime = finalTime;
	}
	
	public String[][] nodeMapping(){
		return this.nodeMapping;
	}
	
	public String[][] regualtorMapping(){
		return this.regulatorMapping;
	}
	
	public String[][] parameterMapping(){
		return this.parameterMapping;
	}
	
	public String[] constNodes() {
		return this.constNodes;
	}
	
	public double finalTime() {
		return this.finalTime;
	}
	
	/**To add more u's (and delta's) to the mapping.
	 * 
	 * @param newUs The new up or down regulated nodes. [i][0] := Node-Name, [i][1] := up or down (u|d)
	 */
	public void addRegulatorMapping(String[][] newUs) {
		//[i][0] := alias (u1|u2|...)
		//[i][1] := Node-Name
		//[i][2] := up or down (u|d)
		//[i][3] := delta-Alias	
		ArrayList<ArrayList<String>> mapping = new ArrayList<ArrayList<String>>();
		ArrayList<String> oneNode;
		int u = 1; //next free alias
		if(regulatorMapping != null) {
			u = regulatorMapping.length + 1;
			for(int i = 0; i < regulatorMapping.length; i++) {
				mapping.add(new ArrayList<>(Arrays.asList(regulatorMapping[i])));
			}
		}
		boolean exists;
		for(int i = 0; i < newUs.length; i++) {
			exists = false;
			for(int j = 0; j < regulatorMapping.length; j++) {
				//if same node and same regulation (both up(u) or both down(d))
				if(regulatorMapping[j][1].equals(newUs[i][0]) && regulatorMapping[j][2].equals(newUs[i][1])) {
					exists = true;
					break;
				}
			}
			if(!exists) {
				oneNode = new ArrayList<String>();
				oneNode.add("u" + u);
				oneNode.add(newUs[i][0]);
				oneNode.add(newUs[i][1]);
				oneNode.add("delta_" + u);
				mapping.add(oneNode);
				u++;
			}
		}
		regulatorMapping = mapping.toArray(regulatorMapping);
		regulatorMapping = new String[mapping.size()][4];
		for(int i = 0; i < mapping.size(); i++) {
			regulatorMapping[i][0] = mapping.get(i).get(0);
			regulatorMapping[i][1] = mapping.get(i).get(1);
			regulatorMapping[i][2] = mapping.get(i).get(2);
			regulatorMapping[i][3] = mapping.get(i).get(3);
		}
	}

}