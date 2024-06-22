package jimena.solver;

import java.util.ArrayList;
import java.util.HashMap;

public class SwitchAnalyzerDataContainer {
	private float timeInterval;
	private float timeHorizon;
	private float alphaW;
	private ArrayList<Double> initState;
	private ArrayList<Double> targetState;
	private HashMap<String, Integer> regP;
	private HashMap<String, Integer> regN;
	//initStateString
	//targetStateString
	private float valueAlpha;
	private float valueBeta;
	private float valueGamma;
	private float valueH;
	
	
	public SwitchAnalyzerDataContainer(float timeInterval, float timeHorizon, float alphaW,
			ArrayList<Double> initState, ArrayList<Double> targetState, float valueAlpha, 
			float valueBeta, float valueGamma, float valueH, HashMap<String, Integer> regP, HashMap<String, Integer> regN) {
		this.timeInterval = timeInterval;
		this.timeHorizon = timeHorizon;
		this.alphaW = alphaW;
		this.initState = initState;
		this.targetState = targetState;
		this.valueAlpha = valueAlpha;
		this.valueBeta = valueBeta;
		this.valueGamma = valueGamma;
		this.valueH = valueH;
		this.regP = regP;
		this.regN = regN;
	}
	
	public float getTimeInterval() {
		return this.timeInterval;
	}
	
	public float getTimeHorizon() {
		return this.timeHorizon;
	}
	
	public float getAlphaW() {
		return this.alphaW;
	}
	
	public ArrayList<Double> getInitState() {
		return this.initState;
	}
	
	public ArrayList<Double> getTargetState(){
		return this.targetState;
	}
	
	public float getValueAlpha() {
		return this.valueAlpha;
	}
	
	public float getValueBeta() {
		return this.valueBeta;
	}
	
	public float getValueGamma() {
		return this.valueGamma;
	}
	
	public float getValueH() {
		return this.valueH;
	}
	
	public HashMap<String, Integer> getPosetiveRegulations(){
		return this.regP;
	}
	
	public HashMap<String, Integer> getNegativeRegulations(){
		return this.regN;
	}
	/*
	 * bw.write(matlab_header(nodeMap.size(), posCtlT.getSelectedRowCount() + negCtlT.getSelectedRowCount(),
						Float.parseFloat(valueInterval.getText()), 
						Float.parseFloat(valuelHorizon.getText()), 
						Float.parseFloat(valueAlphaW.getText()), 
						initStateS, targetStateS, onlyFn));
				bw.write(generateFormular(Float.parseFloat(valueAlpha.getText()), Float.parseFloat(valueBeta.getText()), Float.parseFloat(valueGamma.getText()), Float.parseFloat(valueH.getText())));
	int numNodes, int numControls, float timeInterval, 
	float timeHorizon, float alphaW,
			String initStateS, String targetStateS, String onlyFn
				*/
}
