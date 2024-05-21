package jimena.weightsCalculator.gui.TableModel;

/**
 * Used to set Targeted-Values and weights (of how important the reaching of the value is) for nodes, in the D2DExternalStimuli GUI.
 * @author Jan Krause
 *
 */
public class NodeOfInterest {
	private int nodeNumber;	//Jimena dependent index (seems to be alphabetical sorted)
	private String nodeName;
	private double nodeWeight = 0;
	private double targetedValue = 0;
	
	protected NodeOfInterest(int nodeNumber, String nodeName, double nodeWeight, double targetedValue) throws Exception{
		this.nodeNumber = nodeNumber;
		this.nodeName = nodeName;
		setNodeOfInterest(nodeWeight, targetedValue);
	}
	
	/**
	 * In stead of a constructor this method allows the creation of a blank NodeOfInterst. (Values of 0)
	 * @param nodeNumber The Jimena dependent index number representing the node.
	 * @param nodeName The Name of the node.
	 * @return The newly created blank NodeOfInterest, with weight and targeted Value of 0.
	 * @throws Exception but not in this case.
	 */
	public static NodeOfInterest createNode(int nodeNumber, String nodeName) throws Exception {
		return new NodeOfInterest(nodeNumber, nodeName, 0, 0);
	}
	
	/**
	 * In stead of a constructor this method allows the creation of a NodeOfInterst.
	 * @param nodeNumberThe Jimena dependent index number representing the node.
	 * @param nodeName The Name of the node.
	 * @param nodeWeight How important it is for this node to achieve the targetedValue.
	 * @param targetedValue The value this node should be stirred to.
	 * @return The newly created NodeOfInterest, with set weights and targetedValues.
	 * @throws Exception in the case of invalid values (below 0) for nodeWeight or targetedValue.
	 */
	public static NodeOfInterest createNodeOfInterest(int nodeNumber, String nodeName, double nodeWeight, double targetedValue) throws Exception {
		return new NodeOfInterest(nodeNumber, nodeName, nodeWeight, targetedValue);
	}
	public int getNodeNumber() {
		return this.nodeNumber;
	}

	public String getNodeName() {
		return this.nodeName;
	}
	
	public double getNodeWeight() {
		return this.nodeWeight;
	}
	public void setNodeWeight(double weight) throws Exception {
		if(weight < 0) {
			throw new Exception("Weight has to be greater or equal to 0!");
		}
		this.nodeWeight = weight;
	}
	
	public double getTargetedValue() {
		return this.targetedValue;
	}
	public void setTargetedValue(double value) throws Exception {
		if(value < 0) {
			throw new Exception("The value has to be greater or equal to 0!");
		}
		this.targetedValue = value;
	}
	
	/**
	 * Allows for changing the nodes weight and targetedValue.
	 * @param weight How important it is for this node to achieve the targetedValue.
	 * @param targetedValue The value this node should be stirred to.
	 * @throws Exception in the case of invalid values (below 0) for weight or targetedValue.
	 */
	
	public void setNodeOfInterest(double weight, double targetedValue) throws Exception {
		try {
			setNodeWeight(weight);
			setTargetedValue(targetedValue);
		}
		catch(Exception e){
			this.nodeWeight = 0;
			throw new Exception(e.getMessage());
		}
	}
	
	public void removeNodeOfInterest() {
		this.nodeWeight = 0;
	}
	
	public boolean isNodeOfInterest() {
		return (nodeWeight > 0);
	}
}
