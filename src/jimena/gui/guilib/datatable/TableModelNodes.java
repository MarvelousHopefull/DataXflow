package jimena.gui.guilib.datatable;

import jimena.binaryrn.RegulatoryNetwork;
import jimena.binaryrn.RegulatoryNetworkObserver;

/**
 * 
 * A table model to display information about the nodes and their parameters.
 * 
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 * 
 */
public class TableModelNodes extends NetworkTableModel implements RegulatoryNetworkObserver {
    private static final long serialVersionUID = 8901352050900459647L;

    /**
     * Creates a new TableModelNodes model.
     * 
     * @param network
     *            The network whose nodes are to be displayed.
     */
    public TableModelNodes(RegulatoryNetwork network) {
        // Check of the network done by the superclass
        super(network);
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public int getRowCount() {
        return network.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
        case 0:
            return network.getNetworkNodes()[row].getName();
        case 1:
            return network.getNetworkNodes()[row].getValue();
        case 2:
            return network.getNetworkNodes()[row].getHillNormalize();
        case 3:
            return network.getNetworkNodes()[row].getOdefyDecay();
        case 4:
            return network.getNetworkNodes()[row].getSQUADDecay();
        case 5:
            return network.getNetworkNodes()[row].getSQUADSteepness();
        }

        return "";
    }

    @Override
    public String getColumnName(int col) {
        String[] columns = { "Node", "Value", "Hill normalized", "Odefy decay", "SQUAD decay", "SQUAD steepness" };

        return columns[col];
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (value == null) {
            return;
        }

        switch (col) {
        case 1:
            network.getNetworkNodes()[row].setValue((Double) value);
            network.notifyObserversOfChangedValues();
            return;

        case 2:
            network.getNetworkNodes()[row].setHillNormalize((Boolean) value);
            network.notifyObserversOfChangedValues();
            return;
        case 3:
            network.getNetworkNodes()[row].setOdefyDecay((Double) value);
            network.notifyObserversOfChangedValues();
            return;
        case 4:
            network.getNetworkNodes()[row].setSQUADDecay((Double) value);
            network.notifyObserversOfChangedValues();
            return;
        case 5:
            network.getNetworkNodes()[row].setSQUADSteepness((Double) value);
            network.notifyObserversOfChangedValues();
            return;
        }

        throw new IllegalArgumentException("Cell not editable.");

    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class<?>[] columnClasses = { String.class, Double.class, Boolean.class, Double.class, Double.class, Double.class };

        return columnClasses[col];
    }

    @Override
    public void notifyNetworkChanged() {
        fireTableStructureChanged();
        notifyValuesChanged();
    }

    @Override
    public void notifyValuesChanged() {
        fireTableRowsUpdated(0, getRowCount());
    }

}
