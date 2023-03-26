package jimena.weightsCalculator.gui.TableModel;

public class Control {
	
	public enum Regulation { up, down}
	
	//private int nodeNumber;
	private String nodeName;
	private Regulation regulation = Regulation.up; //(up|down)
	private boolean isActive = true;
	
	public Control(String nodeName, Regulation regulation) {
		//this.nodeNumber = nodeNumber;
		this.nodeName = nodeName;
		this.regulation = regulation;
	}
	
	/*public int getNodeNumber() {
		return this.nodeNumber;
	}*/

	public String getNodeName() {
		return this.nodeName;
	}
	
	public Regulation getRegulation() {
		return this.regulation;
	}
	
	public boolean getIsActive() {
		return this.isActive;
	}
	
	public void setActive(boolean value) {
		this.isActive = value;
	}
	
}
