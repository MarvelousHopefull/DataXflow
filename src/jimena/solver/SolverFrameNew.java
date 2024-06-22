package jimena.solver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import jimena.binarybf.actinhibitf.ActivatorInhibitorFunction;
import jimena.binaryrn.Connection;
import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.weightsCalculator.fileCreator.ExternalStimuliFileCreator;
import jimena.weightsCalculator.gui.AnalyzerFrame;
import jimena.weightsCalculator.gui.D2DSwitchAnalyzerFrame;

public class SolverFrameNew extends AnalyzerFrame {

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
	private HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();
	private TableModel steadyStateModel = null; 
	//default values
	float alpha = 1;
	float beta = 10;
	float gamma = 1;
	float h = 10;
	float timeInterval = Float.parseFloat(Main.textDt.getText());   //0.1f;
	float timeHorizon = Float.parseFloat(Main.textmaxt.getText());  //20;
	float alphaW = 0.1f;
	
	// for input text field
	private JTextField valueAlpha;
	private JTextField valueBeta;
	private JTextField valueGamma;	
	private JTextField valueInterval;
	private JTextField valuelHorizon;
	private JTextField valueAlphaW;
	private JTextField valueH;	

	public SolverFrameNew() {
		super("Switch Analyzer - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(700, 450));
		this.setPreferredSize(new Dimension(width, hight));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		this.frameName = "Switch Analyzer";
	}
	
	public SolverFrameNew(RegulatoryNetwork network) {
		this();
		this.network = network;
	}
	
	/**
	 * Triggers for the Switch Analyzer File creation.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int initR = steadyStateTable1.getSelectedRow();
		int targetR = steadyStateTable2.getSelectedRow();
		
		if (steadyStateTable1.getSelectedColumnCount()==0 || steadyStateTable2.getSelectedColumnCount()==0) {
			JOptionPane.showMessageDialog(this,"Please specify one initial steady state and desired target state in the tables");
			return;
		}
		
		ArrayList<Double> initState = new ArrayList<Double>();
		ArrayList<Double> targetState = new ArrayList<Double>();
		for (int i = 0; i < steadyStateTable1.getColumnCount(); i++) {
			initState.add(Double.parseDouble(steadyStateTable1.getValueAt(initR, i).toString()));
			targetState.add(Double.parseDouble(steadyStateTable1.getValueAt(targetR, i).toString()));
		}
		//steadyStateTable1.getValueAt(initR, i)
		
		//maybe?
		HashMap<String, Integer> regP = new HashMap<String, Integer>();
		HashMap<String, Integer> regN = new HashMap<String, Integer>();
	
		int n = 0;
		for (int i1 : posReg.getSelectedRows()) regP.put(posReg.getValueAt(i1, 0).toString(), ++n);		
		
		for (int i1 : negReg.getSelectedRows()) regN.put(negReg.getValueAt(i1, 0).toString(), ++n);
		//maybe end
		
		/*ArrayList<String> upRNList = new ArrayList<String>();
		for (int i1 : posReg.getSelectedRows()) upRNList.add(posReg.getValueAt(i1, 0).toString());		
		ArrayList<String> downRNList = new ArrayList<String>();
		for (int i1 : negReg.getSelectedRows()) downRNList.add(negReg.getValueAt(i1, 0).toString());
		*/
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			SwitchAnalyzerDataContainer data = new SwitchAnalyzerDataContainer(Float.parseFloat(valueInterval.getText()), Float.parseFloat(valuelHorizon.getText()), Float.parseFloat(valueAlphaW.getText()),
					initState, targetState, Float.parseFloat(valueAlpha.getText()), 
					Float.parseFloat(valueBeta.getText()), Float.parseFloat(valueGamma.getText()), Float.parseFloat(valueH.getText()), regP, regN);
			try {
		        /*String[] upRNodes = null; 
		        if(upRNList != null) {
		        	upRNodes = new String[0];
		        	upRNodes = upRNList.toArray(upRNodes);
		        }
		        String[] downRNodes = null;
				if(downRNList != null) {
					downRNodes = new String[0];
					downRNodes = downRNList.toArray(downRNodes);
				}*/
				
				SwitchAnalyzerFileInteractor.createSwitchAnalyzerFile(selectedFile.toString(), nodeMap, data, posReg.getSelectedRowCount() + negReg.getSelectedRowCount());
				//createSwitchAnalyzerFile(selectedFile.toString(),dataModel,initR,targetR, d2dParameterFile.toString(), mapping, network);
				
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}

	}
	
	@Override
	public void editFrame(TableModel model, RegulatoryNetwork network) {
		// TODO Auto-generated method stub
		parseSteadyState();
		this.network = network;
		this.steadyStateModel = model;
		recalculateNodeMap();
		NetworkNode[] nodes = this.network.getNetworkNodes();

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setOpaque(true);

		// init state
		steadyStateTable1 = new JTable(steadyStateModel);
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
		steadyStateTable2 = new JTable(steadyStateModel);
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
		
		// Summary of Network
		JTextArea t2 = new JTextArea();
		t2.setWrapStyleWord(true);
		t2.setText(getNetworkRepresentation());
		//t2.setText(getNetworkSummary());
		t2.setEditable(false);
		t2.setBackground(new Color(225, 225, 255));
		JScrollPane ts2 = new JScrollPane(t2);
		ts2.setPreferredSize(new Dimension(800, 80));
		ts2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		ts2.setBorder(BorderFactory.createTitledBorder("Summary of the network"));
		mainPanel.add(ts2);
		
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
		
		Border regulationBorder = BorderFactory.createTitledBorder("Regulations (press ctrl to select)");
		regVP.setBorder(regulationBorder);
		
		mainPanel.add(regVP);
		
		mainPanel.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, mainPanel);
		this.setLocationByPlatform(true);
		this.pack();
	}
	
	// currently we do not process it. keep for the future
	private void parseSteadyState() {
	}

	/**
	 * Creates the Tool Bar at the bottom of the GUI.
	 * @return The created ToolBar.
	 */
	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		
		toolbar.setLayout(new GridBagLayout());
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(1024, 25));
		toolbar.setFloatable(false);

		JLabel labelMethod = new JLabel("Analyzer:");
		toolbar.add(labelMethod,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		String[] method = { "Matlab" };
		JComboBox<String> methodBox = new JComboBox<String>(method);
		methodBox.setPreferredSize(new Dimension(100, 20));
		toolbar.add(methodBox,new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		/*JLabel labelMFile = new JLabel("Model:");
		toolbar.add(labelMFile,new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JTextField modelF = new JTextField(); //(modelFile.getName());
		modelF.setSize(250, 20);
		modelF.setPreferredSize(new Dimension(300, 20));
		modelF.setEditable(false);
		toolbar.add(modelF,new GridBagConstraints(3,0,3,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		*/
		JLabel labelInterval = new JLabel("Interval:");
		toolbar.add(labelInterval,new GridBagConstraints(6,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		valueInterval = new JTextField(Float.toString(timeInterval));
		valueInterval.setSize(60, 20);
		valueInterval.setPreferredSize(new Dimension(100, 20));
		valueInterval.setEditable(true);
		toolbar.add(valueInterval,new GridBagConstraints(7,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelHorizon = new JLabel("Horizon:");
		toolbar.add(labelHorizon,new GridBagConstraints(8,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		valuelHorizon = new JTextField(Float.toString(timeHorizon));
		valuelHorizon.setSize(60, 20);
		valuelHorizon.setPreferredSize(new Dimension(100, 20));
		valuelHorizon.setEditable(true);
		toolbar.add(valuelHorizon,new GridBagConstraints(9,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		//
		JLabel labelAlpha = new JLabel("Alpha:");
		toolbar.add(labelAlpha);		
		toolbar.add(labelAlpha,new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		valueAlpha = new JTextField(Float.toString(alpha));
		valueAlpha.setSize(60, 20);
		valueAlpha.setPreferredSize(new Dimension(100, 20));
		valueAlpha.setEditable(true);
		toolbar.add(valueAlpha,new GridBagConstraints(1,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		JLabel labelBeta = new JLabel("Beta:");
		toolbar.add(labelBeta);		
		toolbar.add(labelBeta,new GridBagConstraints(2,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		valueBeta = new JTextField(Float.toString(beta));
		valueBeta.setSize(60, 20);
		valueBeta.setPreferredSize(new Dimension(100, 20));
		valueBeta.setEditable(true);
		toolbar.add(valueBeta,new GridBagConstraints(3,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
			
		
		JLabel labelGamma = new JLabel("Gamma:");
		toolbar.add(labelGamma);		
		toolbar.add(labelGamma,new GridBagConstraints(4,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		valueGamma = new JTextField(Float.toString(gamma));
		valueGamma.setSize(60, 20);
		valueGamma.setPreferredSize(new Dimension(100, 20));
		valueGamma.setEditable(true);
		toolbar.add(valueGamma,new GridBagConstraints(5,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
	
		
		JLabel labelH = new JLabel("H:");
		toolbar.add(labelH);		
		toolbar.add(labelH,new GridBagConstraints(6,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		valueH = new JTextField(Float.toString(h));
		valueH.setSize(60, 20);
		valueH.setPreferredSize(new Dimension(100, 20));
		valueH.setEditable(true);
		toolbar.add(valueH,new GridBagConstraints(7,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
	
		
		JLabel labelAlphaW = new JLabel("AlphaW:");
		toolbar.add(labelAlphaW,new GridBagConstraints(8,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		valueAlphaW= new JTextField(Float.toString(alphaW));
		valueAlphaW.setSize(60, 20);
		valueAlphaW.setPreferredSize(new Dimension(100, 20));
		valueAlphaW.setEditable(true);
		toolbar.add(valueAlphaW,new GridBagConstraints(9,1,1,2,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		
		JButton generateScriptBtn = new JButton("Generate");
		generateScriptBtn.addActionListener(this);
		generateScriptBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		generateScriptBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(generateScriptBtn, new GridBagConstraints(10,1,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		return toolbar;
	}

	private void recalculateNodeMap() {
		NetworkNode[] nodes = this.network.getNetworkNodes();
		Connection[] connections;
		boolean[] activators;
		nodeMap = new HashMap<Integer, Node>(); 
		//add nodes and corresponding in-going edges
		for (int n = 0; n < nodes.length; n++) {
			nodeMap.put(n, new Node(n,nodes[n].getName()));
			connections = nodes[n].getConnections();
			activators = ((ActivatorInhibitorFunction) nodes[n].getFunction()).getActivators();
			//if(activators.length != connections.length) ;
			for(int c = 0; (c < connections.length) && (c < activators.length);c++) {
				if(activators[c]) {
					nodeMap.get(n).addActBy(connections[c].getSource());
				}
				else {
					nodeMap.get(n).addInhBy(connections[c].getSource());
				}
			}
		}
		//add out-going edges
		for (int n = 0; n < nodes.length; n++) {
			if(nodeMap.get(n).getActBy().size() > 0) {
				for(Integer a : nodeMap.get(n).getActBy()) {
					nodeMap.get(a).addAct(n);
				}
			}
			if(nodeMap.get(n).getInhBy().size() > 0) {
				for(Integer i : nodeMap.get(n).getInhBy()) {
					nodeMap.get(i).addInh(n);
				}
			}
		}
	}
	
	private String getNetworkRepresentation(){
		//Nodes are numbered: 0,1,2,... 
		//could be changed to 1,2,3,... by changing all i's and j's not in '()' to i+1 or j+1
		String content = "";//"ModelFile: " + modelFile.getName() + "\n";
		for (Integer i : nodeMap.keySet()) {
			content += "Node " + i + " : " + nodeMap.get(i).getName() + "\n";
			for (Integer j : nodeMap.get(i).getAct())
				content += "\tActivate -> " + j + " : " + nodeMap.get(j).getName() + "\n";
			for (Integer j : nodeMap.get(i).getInh())
				content += "\tInhibit  -| " + j + " : " + nodeMap.get(j).getName() + "\n";
			for (Integer j : nodeMap.get(i).getActBy())
				content += "\tActivated by <- " + j + " : " + nodeMap.get(j).getName() + "\n";
			for (Integer j : nodeMap.get(i).getInhBy())
				content += "\tInhibited by <- " + j + " : " + nodeMap.get(j).getName() + "\n";
		}
		return content;
	}
	
}
/*
private void parseRegulatoryNetwork() {
	int nodeID = 1;
	String nodeLabel;
	int target;
	int sourde;
	NetworkNode[] nodes = this.network.getNetworkNodes();
	for (NetworkNode n : nodes) {
		nodeLabel = n.getName();
		//if (!nodeMap.containsKey(nodeID)) {
		nodeMap.put(nodeID, new Node(nodeID, nodeLabel));
		nodeID++;
		//}
		//Vector<Object> r = new Vector<Object>();
		//r.add(i.getName());
		//nodesV.add(r);
	} 
}*/