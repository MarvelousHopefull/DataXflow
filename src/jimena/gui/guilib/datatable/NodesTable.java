package jimena.gui.guilib.datatable;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.ImageIcon;

import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.HasActivatableNodes;
import jimena.libs.StringLib;

/**
 * A JFrame that displays information about the nodes and their parameters.
 *
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 *
 */
public class NodesTable extends TableJFrame implements HasActivatableNodes {
    private static final long serialVersionUID = 7948800228008893672L;

    /**
     * Creates a new DataTableNodes window.
     *
     * @param network
     *            The network whose nodes are to be displayed.
     */
    public NodesTable(final RegulatoryNetwork network) {
        // Check of the network done by the network model
        super(new TableModelNodes(network), "Nodes Table - Ctrl+V: load state from clipboard", new ImageIcon("images" + File.separator
                + "table16.png").getImage());

        for (KeyListener listener : getKeyListeners()) {
            getTable().removeKeyListener(listener);
        }
        getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown()) {
                    String clipboard = StringLib.readFromClipboard();

                    String[] possibleSeparators = { "\t", System.getProperty("line.separator"), "\n" }; // No comma due to ambiguity
                    for (String separator : possibleSeparators) {
                        if (clipboard.contains(separator)) {
                            String[] stringValues = clipboard.split(separator);
                            double[] values = new double[network.size()];

                            for (int i = 0; i < Math.min(values.length, stringValues.length); i++) {
                                try {
                                    values[i] = Double.valueOf(stringValues[i]);
                                } catch (NumberFormatException ex) {
                                    return;
                                }
                            }

                            network.setValues(values); // also limits the range to 0-1
                            network.notifyObserversOfChangedValues();

                            return;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void activateNode(int nodeIndex) {
        // This method is called if the used left clicks an a node in the main windows
        // The nodes frame is shown an the node the user clicked on is selected
        setVisible(true);
        getTable().changeSelection(nodeIndex, 1, false, false);
    }
}
