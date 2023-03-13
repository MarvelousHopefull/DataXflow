/**
 * 
 */
package jimena.weightsCalculator;

/**
 * Saves the mapping done for D2D, to use to create the ExternalStimuli File afterwards.
 * 
 * @author Jan Krause
 *
 */
public class D2DMapping {
	private String[][] nodeMapping;
	private String[][] regulatorMapping;
	private String[][] parameterMapping;
	private String[] constNodes;
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

}
