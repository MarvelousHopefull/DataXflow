package jimena.weightsCalculator.gui.TableModel;

public class NodeOfInterest {
	private int nodeNumber;
	private String nodeName;
	private double nodeWeight = 0;
	private double targetedValue = 0;
	
	protected NodeOfInterest(int nodeNumber, String nodeName, double nodeWeight, double targetedValue) throws Exception{
		this.nodeNumber = nodeNumber;
		this.nodeName = nodeName;
		setNodeOfInterest(nodeWeight, targetedValue);
	}
	
	public static NodeOfInterest createNode(int nodeNumber, String nodeName) throws Exception {
		return new NodeOfInterest(nodeNumber, nodeName, 0, 0);
	}
	
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
	
	public void setNodeOfInterest(double weight, double targetedValue) throws Exception {
		try {
			setNodeWeight(weight);
			setTargetedValue(targetedValue);
		}
		catch(Exception e){
			this.nodeWeight = 0;
			//this.targetedValue = 0;
			throw new Exception(e.getMessage());
		}
	}
	
	public void removeNodeOfInterest() {
		this.nodeWeight = 0;
		//this.targetedValue = 0;
	}
	
	public boolean isNodeOfInterest() {
		return (nodeWeight > 0);
	}
}
