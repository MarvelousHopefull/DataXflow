/**
 * 
 */
package jimena.weightsCalculator.fileCreator;

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
	
	public D2DMapping(String[][] nodeMapping, String[][] regulatorMapping, String[][] parameterMapping) {
		this.nodeMapping = nodeMapping;
		this.regulatorMapping = regulatorMapping;
		this.parameterMapping = parameterMapping;
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

}
