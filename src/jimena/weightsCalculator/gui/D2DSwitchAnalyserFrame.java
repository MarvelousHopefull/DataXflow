package jimena.weightsCalculator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.solver.NodeTableModel;
import jimena.weightsCalculator.D2DMapping;
import jimena.weightsCalculator.fileCreator.D2DMappingFileInteractor;
import jimena.weightsCalculator.fileCreator.ExternalStimuliFileCreator;
import jimena.weightsCalculator.gui.TableModel.Control;
import jimena.weightsCalculator.gui.TableModel.ControlsTableModel;
import jimena.weightsCalculator.gui.TableModel.NodeOfInterest;
import jimena.weightsCalculator.gui.TableModel.Control.Regulation;

/**
* GUI for SteadyState analysis used with D2D. 
* 
* @author Jan Krause
* @since 13.01.2023
* */
public class D2DSwitchAnalyserFrame extends AnalyserFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	JPanel mainPanel = null;
	private JTable steadyStateTable1;
	private JTable steadyStateTable2;
	
	
	//Frame Size
	private static int width = 884;	//x
	private static int hight = 650;	//y

	//to select up and down regulated notes
	private JTable posReg;	
	private JTable negReg;
	
	private RegulatoryNetwork network = null;
	D2DMapping mapping = null;
	TableModel dataModel;

	private File d2dParameterFile = null;
	private String d2dParameterFileName = "no File selected";
	JTextField d2dParameterF = null;
	
	private File mappingFile = null;
	private String mappingFileName = "no File selected";
	JTextField mappingF = null;
	
	
	/**
	 * Creates the base GUI.
	 */
	public D2DSwitchAnalyserFrame() {
		super("D2DSwitchAnalyserFrame - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(width, hight));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		this.frameName = "D2D Switch Analasyer";
	}
	
	/**
	 * Creates the GUI for the SwitchAnalyser used with D2D fitted parameters.
	 * @param currentFile The File of the current Network.
	 * @throws Exception
	 */
	public D2DSwitchAnalyserFrame(RegulatoryNetwork network) throws Exception {
		this();
		this.network = network;
		NetworkNode[] nodes = this.network.getNetworkNodes();
	
	}
	
	/**
	 * Triggers for the Switch Analyser File creation.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int initR = steadyStateTable1.getSelectedRow();
		int targetR = steadyStateTable2.getSelectedRow();
		
		if (steadyStateTable1.getSelectedColumnCount()==0 || steadyStateTable2.getSelectedColumnCount()==0) {
			JOptionPane.showMessageDialog(this,"Please specify one initial steady state and desired target state in the tables");
			return;
		}
		
		ArrayList<String> upRNList = new ArrayList<String>();
		for (int i1 : posReg.getSelectedRows()) upRNList.add(posReg.getValueAt(i1, 0).toString());		
		ArrayList<String> downRNList = new ArrayList<String>();
		for (int i1 : negReg.getSelectedRows()) downRNList.add(negReg.getValueAt(i1, 0).toString());
		
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			
			try {
		        String[] upRNodes = null; 
		        if(upRNList != null) {
		        	upRNodes = new String[0];
		        	upRNodes = upRNList.toArray(upRNodes);
		        }
		        String[] downRNodes = null;
				if(downRNList != null) {
					downRNodes = new String[0];
					downRNodes = downRNList.toArray(downRNodes);
				}
				
				//change mapping
				if(upRNodes!=null) {
					String[][] upNodes = new String[upRNodes.length][2];
					for(int i = 0; i < upNodes.length; i++) {
						upNodes[i][0] = upRNodes[i];
						upNodes[i][1] = "u";
					}
					mapping.addRegulatorMapping(upNodes);
				}
				if(downRNodes!=null) {
					String[][] downNodes = new String[downRNodes.length][2];
					for(int i = 0; i < downNodes.length; i++) {
						downNodes[i][0] = downRNodes[i];
						downNodes[i][1] = "d";
					}
					mapping.addRegulatorMapping(downNodes);
				}    
				
				ExternalStimuliFileCreator.createSwitchAnalyserFile(selectedFile.toString(),dataModel,initR,targetR, d2dParameterFile.toString(), mapping, network);
				
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}

	}

	/**
	 * Sets up the Frame.
	 * Needed because not all relevant informations are available when the constructor is called.
	 */
	@Override
	public void editFrame(TableModel dataModel, RegulatoryNetwork network) {
		this.network = network;
		this.dataModel = dataModel;
		NetworkNode[] nodes = this.network.getNetworkNodes();

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setOpaque(true);

		// init state
		steadyStateTable1 = new JTable(dataModel);
		steadyStateTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int i = 0; i < steadyStateTable1.getColumnCount(); i++) {
			steadyStateTable1.getColumnModel().getColumn(i).setPreferredWidth(60);
			steadyStateTable1.getColumnModel().getColumn(i).setMinWidth(40);
			steadyStateTable1.getColumnModel().getColumn(i).setMaxWidth(120);
		}
		JScrollPane steadyStateSP1 = new JScrollPane(steadyStateTable1);
		steadyStateSP1.setViewportView(steadyStateTable1);
		steadyStateSP1.setPreferredSize(new Dimension(1024, 100));
		steadyStateSP1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		steadyStateSP1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		steadyStateTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		steadyStateSP1.setBorder(BorderFactory.createTitledBorder("Initial state"));
		mainPanel.add(steadyStateSP1);

		// target state
		steadyStateTable2 = new JTable(dataModel);
		steadyStateTable2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);   // to enable it wider than default.
		steadyStateTable2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i = 0; i < steadyStateTable2.getColumnCount(); i++) {
			steadyStateTable2.getColumnModel().getColumn(i).setPreferredWidth(60);
			steadyStateTable2.getColumnModel().getColumn(i).setMinWidth(40);
			steadyStateTable2.getColumnModel().getColumn(i).setMaxWidth(120);
		}

		JScrollPane steadyStateSP2 = new JScrollPane(steadyStateTable2);
		steadyStateSP2.setPreferredSize(new Dimension(1024, 100));
		steadyStateSP2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		steadyStateSP2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);	
		steadyStateSP2.setBorder(BorderFactory.createTitledBorder("Target state"));
		mainPanel.add(steadyStateSP2);
		
		//Regulation model
		Vector<String> colNames = new Vector<String>();
		colNames.add("Node");
		Vector<Vector<Object>> nodesV = new Vector<Vector<Object>>();
		for (NetworkNode i : nodes) {
			Vector<Object> r = new Vector<Object>();
			r.add(i.getName());
			nodesV.add(r);
		}

		NodeTableModel nodeM = new NodeTableModel(nodesV, colNames);
		
		// Regulation Positive
		posReg = new JTable(nodeM);

		JScrollPane posCtlSP = new JScrollPane(posReg);
		posCtlSP.setPreferredSize(new Dimension(425, 310));
		JToolBar posCtlTB = new JToolBar("Positive regulation", JToolBar.HORIZONTAL);
		JLabel posCtlLbl = new JLabel("Positive regulation");
		posCtlLbl.setForeground(Color.RED.darker());
		posCtlTB.add(posCtlLbl);
		JButton unSelAllBtn1 = new JButton("UnselectAll");
		unSelAllBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				posReg.clearSelection();
			}
		});
		posCtlTB.add(unSelAllBtn1);
		JPanel posCtlP = new JPanel();
		posCtlP.setLayout(new BorderLayout());
		posCtlP.add(posCtlTB, BorderLayout.NORTH);
		posCtlP.add(posCtlSP, BorderLayout.CENTER);		
		
		// Regulation Negative
		negReg = new JTable(nodeM);
		
		JScrollPane negCtlSP = new JScrollPane(negReg);
		negCtlSP.setPreferredSize(new Dimension(425, 310));
		JToolBar negCtlTB = new JToolBar("Negative regulation", JToolBar.HORIZONTAL);
		JLabel negCtlLbl = new JLabel("Negative regulation");
		negCtlLbl.setForeground(Color.BLUE.darker());
		negCtlTB.add(negCtlLbl);
		JButton unSelAllBtn2 = new JButton("UnselectAll");
		unSelAllBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				negReg.clearSelection();
			}
		});
		negCtlTB.add(unSelAllBtn2);
		JPanel negCtlP = new JPanel();
		negCtlP.setLayout(new BorderLayout());
		negCtlP.add(negCtlTB, BorderLayout.NORTH);
		negCtlP.add(negCtlSP, BorderLayout.CENTER);
		
		// the panels, for separating the different selection sections
		// the regulation panels
		JSplitPane regP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		regP.add(posCtlP, JSplitPane.LEFT);
		regP.add(negCtlP, JSplitPane.RIGHT);
		regP.setDividerLocation(0.5);
		JPanel regVP = new JPanel();
		regVP.add(regP, BorderLayout.CENTER);
		
		Border regulationBorder = BorderFactory.createTitledBorder("Regulations (press ctl to select)");
		regVP.setBorder(regulationBorder);
		
		mainPanel.add(regVP);
		
		mainPanel.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, mainPanel);
		this.setLocationByPlatform(true);
		this.pack();
	}
	
	/**
	 * Creates the Tool Bar at the bottom of the GUI.
	 * @return The created ToolBar.
	 */
	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		
		//D2D Parameter File
		JButton loadD2DFileBtn = new JButton("Load Parameter File");
		loadD2DFileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               JFileChooser filechooser = new JFileChooser();

                // Remove standard filters (JFileChooser is not completely adapted to the current locale)
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }

                filechooser.addChoosableFileFilter(tsvFilter);

                filechooser.setDialogTitle("Open D2D Parameter TSV-File");
                filechooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int returnVal = filechooser.showOpenDialog(D2DSwitchAnalyserFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadD2DFile(filechooser.getSelectedFile());
                }

            }
        });
		loadD2DFileBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		loadD2DFileBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(loadD2DFileBtn, new GridBagConstraints(10,0,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelTSVFile = new JLabel("File:");
		toolbar.add(labelTSVFile,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		if(d2dParameterFile != null) {
			d2dParameterFileName = d2dParameterFile.getName();
		}
		d2dParameterF = new JTextField(d2dParameterFileName);
		d2dParameterF.setSize(250, 20);
		d2dParameterF.setPreferredSize(new Dimension(300, 20));
		d2dParameterF.setEditable(false);
		toolbar.add(d2dParameterF,new GridBagConstraints(1,0,3,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		//mapping
		JButton loadMappingFileBtn = new JButton("Load Mapping File");
		loadMappingFileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser filechooser = new JFileChooser();

                // Remove standard filters (JFileChooser is not completely adapted to the current locale)
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }

                filechooser.addChoosableFileFilter(tsvFilter);

                filechooser.setDialogTitle("Open D2D Parameter Mapping TSV-File");
                filechooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int returnVal = filechooser.showOpenDialog(D2DSwitchAnalyserFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadMappingFile(filechooser.getSelectedFile());
                }

            }
        });
		loadMappingFileBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		loadMappingFileBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(loadMappingFileBtn, new GridBagConstraints(10,1,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		JLabel labelMappingFile = new JLabel("File:");
		toolbar.add(labelMappingFile,new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		if(mappingFile != null) {
			mappingFileName = mappingFile.getName();
		}
		mappingF = new JTextField(mappingFileName);
		mappingF.setSize(250, 20);
		mappingF.setPreferredSize(new Dimension(300, 20));
		mappingF.setEditable(false);
		toolbar.add(mappingF,new GridBagConstraints(1,1,3,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		
		JButton generateScriptBtn = new JButton("Generate");
		generateScriptBtn.addActionListener(this);
		generateScriptBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		generateScriptBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(generateScriptBtn, new GridBagConstraints(10,2,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		return toolbar;
	}

	/**
	 * Loads the File with the by D2D calculated values into the GUI.
	 * @param file The File with the values.
	 */
	private void loadD2DFile(File file) {
		this.d2dParameterFile = file;
		d2dParameterFileName = d2dParameterFile.getName();
		d2dParameterF.setText(d2dParameterFileName);
	}
	
	/**
	 * Loads the Mapping done by the D2D GUI into this GUI.
	 * @param file The File with the mapping.
	 */
	private void loadMappingFile(File file) {
		this.mappingFile = file;
		mappingFileName = mappingFile.getName();
		mappingF.setText(mappingFileName);
		try {
			this.mapping = D2DMappingFileInteractor.getD2DMapping(mappingFile.toString());
		}
		catch(Exception e) {
			
		}
	}
	
	private FileFilter tsvFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.getName().matches(".*\\.tsv$") || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "TSV-files (*.tsv)";
        }
    };
}
