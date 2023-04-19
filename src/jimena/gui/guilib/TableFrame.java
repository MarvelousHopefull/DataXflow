package jimena.gui.guilib;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import jimena.libs.StringLib;
import jimena.libs.TxtFileChooser;
import jimena.solver.SolverFrame;
import jimena.gui.main.Main;
import jimena.libs.OptimizeMatlab;

/**
 * Shows a table of numbers in a modal dialog.
 *
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 *
 */
public class TableFrame extends JDialog {
    private static final long serialVersionUID = 6543306651498035545L;

    private JTable table = null;
    private String[] titles = null;
    private TableModel model = null;
    private File currentFile=null;

    /**
     * Creates a export string of the selected rows in the table.
     *
     * @return
     */
    private String getExportString() {
        ArrayList<Object[]> exportData = new ArrayList<Object[]>();

        Object[] header = new Object[TableFrame.this.model.getColumnCount()];
        for (int j = 0; j < TableFrame.this.model.getColumnCount(); j++) {
            header[j] = TableFrame.this.model.getColumnName(j);
        }
        exportData.add(header);

        // Take the data directly from the source to avoid rounding
        if (TableFrame.this.table.getSelectedRowCount() != 0) {
            for (int i : TableFrame.this.table.getSelectedRows()) {
                Object[] row = new Object[TableFrame.this.model.getColumnCount()];
                for (int j = 0; j < TableFrame.this.model.getColumnCount(); j++) {
                    row[j] = TableFrame.this.model.getValueAt(i, j);
                }
                exportData.add(row);
            }
        } else {
            for (int i = 0; i < TableFrame.this.table.getRowCount(); i++) {
                Object[] row = new Object[TableFrame.this.model.getColumnCount()];
                for (int j = 0; j < TableFrame.this.model.getColumnCount(); j++) {
                    row[j] = TableFrame.this.model.getValueAt(i, j);
                }
                exportData.add(row);
            }
        }

        return StringLib.arrayToTabSeparatedUnchecked(exportData);
    }
    

    private void runSolverEvent() {    	
    	SolverFrame f = null;
		try {
			f = new SolverFrame(getExportString(), this.model, currentFile);
		} catch (NumberFormatException e1) {			
			e1.printStackTrace();
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
    	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	f.pack();
    	f.setLocationRelativeTo(null); 
    	f.setVisible(true);    	    	
    }

    /**
     * Exports the selected rows of the table to a file.
     */
    private void exportToFile() {
        TxtFileChooser.writeToTextFile(getExportString(), TableFrame.this);
    }

    /**
     * Exports the selected rows of the table to the clipboard.
     */
    private void exportToClipboard() {
        StringLib.writeToClipboard(getExportString());
    }
    public TableFrame(JFrame parent, String title, ArrayList<Object[]> data, String[] titles, Class<?> type, String[] horizontal) {
    	this(parent,title,data,titles,type, horizontal, Main.currentFile); 
    }
    /**
     * Creates a new table of numbers.
     *
     * @param parent
     *            Parent of the model dialog
     * @param title
     *            Title of the dialog
     * @param data
     *            The data to display
     * @param titles
     *            Names of the data arrays
     * @param type
     *            Type of the data in the ArrayList
     * @param horizontal
     *            Titles of the columns if the data is displayed horizontally, null for a vertical presentation
     */
    public TableFrame(JFrame parent, String title, ArrayList<Object[]> data, String[] titles, Class<?> type, String[] horizontal, File currentFile) {
        super(parent, title + " - Ctrl+C to copy a line - Export the data for better accuracy", false);
        this.currentFile=currentFile;
        this.titles = titles;

        // Checks are done by the table model

        getContentPane().setLayout(new BorderLayout());

        model = new DataTableModel(data, titles, type, horizontal);
        table = new JTable(model) {
            private static final long serialVersionUID = -7707184520358542400L;
            {
                for (KeyListener listener : getKeyListeners()) {
                    removeKeyListener(listener);
                }
                addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                            exportToFile();
                        } else if (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) {
                            exportToClipboard();
                        }
                    }
                });
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }
        };

        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setPreferredWidth(50);
        }

        table.setTableHeader(new JTableHeader(TableFrame.this.table.getColumnModel()) {
            private static final long serialVersionUID = -8257137320637006229L;

            @Override
            public String getToolTipText(MouseEvent e) {
                int i = TableFrame.this.table.getColumnModel().getColumnIndexAtX(e.getX());
                return TableFrame.this.titles[i];
            }
        });

        table.getTableHeader().setReorderingAllowed(false);
        table.setToolTipText("Select several row using the shift and control keys and save the values to a csv file by pressing Ctrl + [S].");
        table.setDefaultRenderer(Double.class, new FractionCellRenderer(5));

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        // Add a control panel with buttons
        JPanel controlPanel = new JPanel(new GridLayout(1, 3));

        JButton closeButton = new JButton("Close Window");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                TableFrame.this.setVisible(false);
            }
        });
        controlPanel.add(closeButton);

        JButton exportFile = new JButton("Export to File");
        exportFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                exportToFile();
            }
        });
        controlPanel.add(exportFile);

        
        JButton runAnalyzer = new JButton("Switch Analyzer (data-driven method)");
        runAnalyzer.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
        runAnalyzer.setForeground(this.getForeground().darker());
        runAnalyzer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	runSolverEvent();
            }
        });
        controlPanel.add(runAnalyzer);
        
        JButton exportClipboard = new JButton("Export to Clipboard");
        exportClipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                exportToClipboard();
            }
        });
        controlPanel.add(exportClipboard);

        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        pack();

        this.getRootPane().setDefaultButton(closeButton);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    /**
     * Renders a double with more than 3 fraction digits
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    class FractionCellRenderer extends DefaultTableCellRenderer {
        // Originally: http://www.java-forum.org/java-basics-anfaenger-themen/36497-ausgabeformatierung-fuer-double-jtable.html

        private static final long serialVersionUID = 4206747185037314310L;
        final private int fraction;
        final private NumberFormat formatter = NumberFormat.getInstance();

        /**
         * Creates a new renderer for doubles.
         *
         * @param fraction
         *            Maximum number of fraction digits.
         */
        public FractionCellRenderer(int fraction) {
            this.fraction = fraction;
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        protected void setValue(Object value) {
            if (value == null) {
                throw new NullPointerException();
            }

            if (!(value instanceof Double)) {
                throw new IllegalArgumentException("This rendere only works with doubles.");
            }

            formatter.setMaximumFractionDigits(fraction);
            setText(formatter.format(((Number) value).doubleValue()));
        }
    }
    

}
