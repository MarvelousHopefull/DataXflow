package jimena.weightsCalculator.gui.TableModel;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * A Table Model used for JTable creation.
 * Displays all nodes and allows to set a Targeted-Value for each node and a Weight, of how important the reaching of the Targeted-Value is, in comparison to others nodes Targeted-Values.
 * @author Jan Krause
 *
 */
public class NodesOfInterestTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private List<NodeOfInterest> list;
	
	private final String[] columnNames = new String[] {
		"Name", "Weight", "Targeted Value"
    };
	
	private final Class[] columnClass = new Class[] {
		String.class, Double.class, Double.class
	};
	
	public NodesOfInterestTableModel(List<NodeOfInterest> list) {
		this.list = list;
	}
	
	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return list.get(rowIndex).getNodeName();
		}
		else if(columnIndex == 1) {
			return list.get(rowIndex).getNodeWeight();
		}
		else if(columnIndex == 2) {
			return list.get(rowIndex).getTargetedValue();
		}
		return null;
	}
	
	public void setWeightAt(double weight, String nodeName) throws Exception{
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getNodeName().equals(nodeName)) {
				setWeightAt(weight, i);
				break;
			}
		}
	}
	
	public void setWeightAt(double weight, int nodeIndex) throws Exception{
		list.get(nodeIndex).setNodeWeight(weight);
	}
	
	public void setTargetedValueAt(double targetedValue, String nodeName) throws Exception{
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).getNodeName().equals(nodeName)) {
				setTargetedValueAt(targetedValue,i);
				break;
			}
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex){
		if(columnIndex == 0) {
			return;
		}
		else if(columnIndex == 1) {
			try {
				setWeightAt((Double)aValue, rowIndex);
			}
			catch (Exception e) {
				
			}
		}
		else if(columnIndex == 2) {
			try {
				setTargetedValueAt((Double)aValue, rowIndex);
			}
			catch (Exception e) {
				
			}
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex){
		if(columnIndex == 1 || columnIndex == 2) {return true;}
	    return false;
	}

	
	public void setTargetedValueAt(double targetedValue, int nodeIndex) throws Exception{
		list.get(nodeIndex).setTargetedValue(targetedValue);
	}
	
	public int getNodeNumber(int nodeIndex) {
		return list.get(nodeIndex).getNodeNumber();
	}
	
	 @Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClass[columnIndex];
	}
	
	@Override
    public String getColumnName(int column){
        return columnNames[column];
    }
	
	/**
	 * If it should be considered. (a.k.a if the weight is greater than 0)
	 * @param rowIndex
	 * @return
	 */
	public boolean isNodeOfInterest(int rowIndex) {
		return list.get(rowIndex).isNodeOfInterest();
	}
	
	public NodeOfInterest getNode(int rowIndex) {
		return list.get(rowIndex);
	}

}
