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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.solver.NodeTableModel;
import jimena.weightsCalculator.D2DMapping;
import jimena.weightsCalculator.fileCreator.D2DMappingFileInteractor;
import jimena.weightsCalculator.fileCreator.ExternalStimuliFileCreator;
import jimena.weightsCalculator.gui.TableModel.Control;
import jimena.weightsCalculator.gui.TableModel.Control.Regulation;
import jimena.weightsCalculator.gui.TableModel.ControlsTableModel;
import jimena.weightsCalculator.gui.TableModel.NodeOfInterest;
import jimena.weightsCalculator.gui.TableModel.NodesOfInterestTableModel;

/**
 * The GUI for loading the Data calculated by D2D and for creating the file needed for the ExternalStimuli.
 * @author Jan Krause
 *
 */
public class D2DExternalStimuliFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//Frame Size
	private static int width = 900;	//x
	private static int hight = 450;	//y

	private File currentFile = null;
	
	private JTable nodesTable;
	private NodesOfInterestTableModel nModel;
	
	private JTable controlsTable;
	private ControlsTableModel cModel;
	
	private JTable posReg;
	private JTable negReg;
	
	private File d2dFile = null;
	private String d2dFileName = "no File selected";
	JTextField d2dF = null;
	
	private File mappingFile = null;
	private String mappingFileName = "no File selected";
	JTextField mappingF = null;
	
	
	
	protected D2DExternalStimuliFrame() {
		super("D2DExternalStimuli - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(width, hight));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());		
	}
	
	/**
	 * Creates the GUI for the loading of the by D2D calculated values and the creation of the ExternalStimuli _main.m File.
	 * @param currentFile The File of the current Network.
	 * @throws Exception
	 */
	public D2DExternalStimuliFrame(File currentFile) throws Exception {
		this();
		if(currentFile == null) {
			throw new Exception("No RN File selected!");
		}
		this.currentFile = currentFile;
		
		RegulatoryNetwork network = new RegulatoryNetwork();
        // Load a yED GraphML file into the network
        network.loadYEdFile(currentFile);
		NetworkNode[] nodes = network.getNetworkNodes();
		//System.out.println(nodes.length);
		List<NodeOfInterest> nodesOfInterest = new ArrayList<NodeOfInterest>();
		for (int i = 0; i < nodes.length; i++) {
			nodesOfInterest.add(NodeOfInterest.createNode((i+1), nodes[i].getName()));
		}
		nModel = new NodesOfInterestTableModel(nodesOfInterest);
		cModel = new ControlsTableModel();
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setOpaque(true);
		
		// Node of Interest Selection
		nodesTable = new JTable(nModel);
		nodesTable.setCellSelectionEnabled(false);

		JScrollPane nodesSP = new JScrollPane(nodesTable);
		nodesSP.setPreferredSize(new Dimension(250, 310));
		
		JPanel nodesP = new JPanel();
		nodesP.setLayout(new BorderLayout());
		nodesP.add(nodesSP, BorderLayout.CENTER);
		
		Border nodesBorder = BorderFactory.createTitledBorder("Nodes of Interest");
		nodesP.setBorder(nodesBorder);
		
		// Controls deactivation/activation
		controlsTable = new JTable(cModel);
		controlsTable.setCellSelectionEnabled(false);
		
		JScrollPane controlsSP = new JScrollPane(controlsTable);
		controlsSP.setPreferredSize(new Dimension(150, 310));
		
		JPanel controlsP = new JPanel();
		controlsP.setLayout(new BorderLayout());
		controlsP.add(controlsSP, BorderLayout.CENTER);
		
		Border controlsBorder = BorderFactory.createTitledBorder("Set Controls");
		controlsP.setBorder(controlsBorder);
		
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
		posCtlSP.setPreferredSize(new Dimension(200, 310));
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
		negCtlSP.setPreferredSize(new Dimension(200, 310));
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
		
		//separating nodes of interest and control nodes
		JSplitPane nodesControlP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		nodesControlP.add(nodesP, JSplitPane.LEFT);
		nodesControlP.add(controlsP, JSplitPane.RIGHT);
		nodesControlP.setDividerLocation(0.5);
		JPanel nodesControlVP = new JPanel();
		nodesControlVP.add(nodesControlP, BorderLayout.CENTER);
		
		//separating nodes panels and dataNodes panel
		JSplitPane nodesRegP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		nodesRegP.add(nodesControlVP, JSplitPane.LEFT);
		nodesRegP.add(regVP, JSplitPane.RIGHT);
		nodesRegP.setDividerLocation(0.5);
		JPanel nodesRegVP = new JPanel();
		nodesRegVP.add(nodesRegP, BorderLayout.CENTER);
		
		p.add(nodesRegVP);
		
		p.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, p);
		this.setLocationByPlatform(true);
		this.pack();
	}
	
	/**
	 * Triggers for the ExternalStimuli File creation.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		List<NodeOfInterest> nodesList = new ArrayList<NodeOfInterest>();
		for (int i = 0; i < nModel.getRowCount(); i++) {
			if(nModel.isNodeOfInterest(i)) {
				nodesList.add(nModel.getNode(i));
			}		
		}
		ArrayList<ArrayList<String>> controlNodes = new ArrayList<ArrayList<String>>();
		ArrayList<String> node;
		Control control;
		String regulation;
		for(int i = 0; i < cModel.getRowCount(); i++) {
			if(!cModel.isActive(i)) {
				control = cModel.getControl(i);
				node = new ArrayList<String>();
				node.add(control.getNodeName());
				if(control.getRegulation().equals(Regulation.up)) {
					regulation = "u";
				}
				else {
					regulation = "d";
				}
				node.add(regulation);
				controlNodes.add(node);
			}
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
				
				RegulatoryNetwork network = new RegulatoryNetwork();
		        // Load a yED GraphML file into the network
		        network.loadYEdFile(currentFile);
				D2DMapping mapping = D2DMappingFileInteractor.getD2DMapping(mappingFile.toString());
				
				for(int i = 0; i < controlNodes.size(); i++) {
					mapping.setActiveRegulatorMapping(controlNodes.get(i).get(0), controlNodes.get(i).get(1), false);
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
				
				ExternalStimuliFileCreator.createFile(selectedFile.toString(), d2dFile.toString(), mapping, network, nodesList);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}

	}
	
	/**
	 * Creates the Tool Bar at the bottom of the GUI.
	 * @return The created ToolBar.
	 */
	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		
		//D2D Parameter File
		JButton loadD2DFileBtn = new JButton("Load D2D File");
		loadD2DFileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               JFileChooser filechooser = new JFileChooser();

                // Remove standard filters (JFileChooser is not completely adapted to the current locale)
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }

                filechooser.addChoosableFileFilter(tsvFilter);

                filechooser.setDialogTitle("Open D2D TSV-File");
                filechooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int returnVal = filechooser.showOpenDialog(D2DExternalStimuliFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadD2DFile(filechooser.getSelectedFile());
                }

            }
        });
		loadD2DFileBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		loadD2DFileBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(loadD2DFileBtn, new GridBagConstraints(10,0,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelTSVFile = new JLabel("D2D TSV-File:");
		toolbar.add(labelTSVFile,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		if(d2dFile != null) {
			d2dFileName = d2dFile.getName();
		}
		d2dF = new JTextField(d2dFileName);
		d2dF.setSize(250, 20);
		d2dF.setPreferredSize(new Dimension(300, 20));
		d2dF.setEditable(false);
		toolbar.add(d2dF,new GridBagConstraints(1,0,3,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		//mapping
		JButton loadMappingFileBtn = new JButton("Load D2D Parameter Mapping File");
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

                int returnVal = filechooser.showOpenDialog(D2DExternalStimuliFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadMappingFile(filechooser.getSelectedFile());
                }

            }
        });
		loadMappingFileBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		loadMappingFileBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(loadMappingFileBtn, new GridBagConstraints(10,1,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelMappingFile = new JLabel("D2D TSV-File:");
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
		this.d2dFile = file;
		d2dFileName = d2dFile.getName();
		d2dF.setText(d2dFileName);
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
			D2DMapping mapping = D2DMappingFileInteractor.getD2DMapping(mappingFile.toString());
			String[][] regulatorMapping = mapping.regualtorMapping();
			ArrayList<Control> controlsList = new ArrayList<Control>();
			Regulation reg;
			for(int i = 0; i < regulatorMapping.length; i++) {
				if(regulatorMapping[i][2].equals("u")) {
					reg = Regulation.up;
				}
				else {
					reg = Regulation.down;
				}
				controlsList.add(new Control(regulatorMapping[i][1],reg));
			}
			cModel = new ControlsTableModel(controlsList);
			controlsTable.setModel(cModel);
			
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
