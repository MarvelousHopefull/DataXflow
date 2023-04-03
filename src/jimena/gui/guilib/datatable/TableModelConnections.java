package jimena.gui.guilib.datatable;


import java.util.TreeMap;

import jimena.binaryrn.Connection;
import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.binaryrn.RegulatoryNetworkObserver;

/**
 * 
 * A table model to display information about the connections of the nodes and their parameters.
 * 
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 * 
 */
public class TableModelConnections extends NetworkTableModel implements RegulatoryNetworkObserver {
    private static final long serialVersionUID = 8255195334564743582L;

    /**
     * Creates a new TableModelConnections model.
     * 
     * @param network
     *            The network whose connections are to be displayed.
     */
    public TableModelConnections(RegulatoryNetwork network) {
        // Check of the network done by the superclass
        super(network);
    }

    private TreeMap<Integer, Integer> mapRowToNode;
    private TreeMap<Integer, Integer> mapRowToConnection;

    @Override
    public String getColumnName(int col) {
        String[] columns = { "Node", "Input", "Hill k", "Hill n", "SQUAD weight" };

        return columns[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        Class<?>[] columnClasses = { String.class, String.class, Double.class, Double.class, Double.class };

        return columnClasses[col];
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getRowCount() {
        return mapRowToNode.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (!mapRowToConnection.containsKey(row)) {
            // The line represents an node
            int nodeIndex = mapRowToNode.get(row);

            switch (col) {
            case 0:
                return network.getNetworkNodes()[nodeIndex].getName();
            }
        } else {
            // The line represents a connection
            int nodeIndex = mapRowToNode.get(row);
            int connectionIndex = mapRowToConnection.get(row);

            switch (col) {
            case 0:
                return "";
            case 1:
                int sourceIndex = network.getNetworkNodes()[nodeIndex].getConnections()[connectionIndex].getSource();
                return network.getNetworkNodes()[sourceIndex].getName();
            case 2:
                return network.getNetworkNodes()[nodeIndex].getHillKs()[connectionIndex];
            case 3:
                return network.getNetworkNodes()[nodeIndex].getHillNs()[connectionIndex];
            case 4:
                return network.getNetworkNodes()[nodeIndex].getSQUADWeights()[connectionIndex];
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (!mapRowToConnection.containsKey(row)) {
            // The line represents an node
        } else {
            // The line represents a connection
            int nodeIndex = mapRowToNode.get(row);
            int connectionIndex = mapRowToConnection.get(row);

            switch (col) {
            case 2:
                network.getNetworkNodes()[nodeIndex].getHillKs()[connectionIndex] = (Double) value;
                network.notifyObserversOfChangedValues();
                return;
            case 3:
                network.getNetworkNodes()[nodeIndex].getHillNs()[connectionIndex] = (Double) value;
                network.notifyObserversOfChangedValues();
                return;
            case 4:
                network.getNetworkNodes()[nodeIndex].getSQUADWeights()[connectionIndex] = (Double) value;
                network.notifyObserversOfChangedValues();
                return;
            }
        }
        throw new IllegalArgumentException("Cell not editable.");

    }

    @Override
    public void notifyNetworkChanged() {        
        mapRowToNode = new TreeMap<Integer, Integer>();
        mapRowToConnection = new TreeMap<Integer, Integer>();

        int rowIndex = 0;
        int nodeIndex = 0;

        for (NetworkNode node : network.getNetworkNodes()) {
            // One line for the node
            mapRowToNode.put(rowIndex, nodeIndex);

            rowIndex++;
            int connectionIndex = 0;
            for (@SuppressWarnings("unused")
            Connection connection : node.getConnections()) {
                mapRowToNode.put(rowIndex, nodeIndex);
                mapRowToConnection.put(rowIndex, connectionIndex);
                rowIndex++;
                connectionIndex++;
            }

            nodeIndex++;
        }

        fireTableStructureChanged();

        notifyValuesChanged();
    }

    @Override
    public void notifyValuesChanged() {
        fireTableRowsUpdated(0, getRowCount());
    }

}
