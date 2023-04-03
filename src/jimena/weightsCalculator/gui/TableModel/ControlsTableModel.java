package jimena.weightsCalculator.gui.TableModel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * A Table Model used for JTable creation.
 * Displays all Controls added in the D2D GUI. Allows deactivating these Controls/Regulations.
 * @author Jan Krause
 *
 */
public class ControlsTableModel extends AbstractTableModel {


	private static final long serialVersionUID = 1L;
	private List<Control> list;
	private final String[] columnNames = new String[] {
		"Name", "Regulation", "Active"
	};
		
	private final Class[] columnClass = new Class[] {
		String.class, String.class, Boolean.class
	};	
	
	public ControlsTableModel(List<Control> list) {
		this.list = list;
	}
	public ControlsTableModel() {
		this.list = new ArrayList<Control>();
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
			return list.get(rowIndex).getRegulation().toString();
		}
		else if(columnIndex == 2) {
			return list.get(rowIndex).getIsActive();
		}
		return null;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex){
		if(columnIndex == 2) {
			list.get(rowIndex).setActive((boolean)aValue);
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex){
		if(columnIndex == 2) {return true;}
		return false;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex){
		return columnClass[columnIndex];
	}
	
	@Override
	public String getColumnName(int column){
		return columnNames[column];
	}
	
	public boolean isActive(int rowIndex) {
		return list.get(rowIndex).getIsActive();
	}
	public Control getControl(int rowIndex) {
		return list.get(rowIndex);
	}

}
