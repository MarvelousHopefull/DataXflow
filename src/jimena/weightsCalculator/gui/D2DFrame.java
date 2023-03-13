package jimena.weightsCalculator.gui;

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
import javax.swing.table.TableRowSorter;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.solver.NodeTableModel;
import jimena.weightsCalculator.D2DMapping;
import jimena.weightsCalculator.fileCreator.D2DMappingFileInteractor;
import jimena.weightsCalculator.fileCreator.DefCreator;

//The GUI is in some parts mostly copy&paste from SolverFrame. (Thanks Chunguang Liang!) 
/**
 * GUI for D2D. 
 * Used for creating the model.def, data.def, initValues.txt and DataNodeNames.csv files, needed when working with D2D.
 * 
 * @author Jan Krause
 * @since 13.01.2023
 * */
public class D2DFrame extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	//Frame Size
	private static int width = 884;	//x
	private static int hight = 520;	//y

	//to select up and down regulated notes
	private JTable posReg;	
	private JTable negReg;
	
	//to select Nodes where experiment data is available 
	private JTable eDataNodes;
	
	//to select Nodes that should be seen as having a constant value 
	private JTable constantNodes;
	
	//private HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>(); // Network
	private NetworkNode[] nodes = null;
	private File currentFile = null;
	
	//default values
	private double alpha = 1;
	private double lbAlpha = 0;
	private double ubAlpha = 1000;
	
	private double beta = 10;
	private double lbBeta = 0;
	private double ubBeta = 1000;
	
	private double h = 10;
	private double lbH = 1;
	private double ubH = 1000;
	
	private double delta = 1;
	private double lbDelta = 0;
	private double ubDelta = 1000;
	
	private double initNodeValue = 0.5;
	private double lbNodeValue = 0;
	private double ubNodeValue = 1;
	
	private double initTime = 10;
	
	// for input text field (initial values, lower bound, upper bound)
	private JTextField initValueAlpha;
	private JTextField lbValueAlpha;
	private JTextField ubValueAlpha;
	
	private JTextField initValueBeta;
	private JTextField lbValueBeta;
	private JTextField ubValueBeta;
	
	private JTextField initValueH;
	private JTextField lbValueH;
	private JTextField ubValueH;
	
	private JTextField initValueDelta;	
	private JTextField lbValueDelta;
	private JTextField ubValueDelta;
	
	private JTextField initValueX;
	private JTextField lbValueX;
	private JTextField ubValueX;
	//alpha, beta, h, delta, init_x
	
	private JTextField time;

	
	public D2DFrame() {
		super("D2D - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(width, hight));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
	}
	
	public D2DFrame(File currentFile) throws Exception {
		this();
		if(currentFile == null) {
			throw new Exception("No RN File selected!");
		}
		this.currentFile = currentFile;
		RegulatoryNetwork network = new RegulatoryNetwork();
        // Load a yED GraphML file into the network
        network.loadYEdFile(currentFile);
        
        nodes = network.getNetworkNodes();
        
        JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setOpaque(true);
		
		// nodes NodeM as tableModel , preparation for two regulation tables.
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
		TableRowSorter sorterP = new TableRowSorter(nodeM);		
		posReg.setRowSorter(sorterP);
		posReg.getRowSorter().toggleSortOrder(0);

		JScrollPane posCtlSP = new JScrollPane(posReg);
		posCtlSP.setPreferredSize(new Dimension(200, 310));
		JPanel posCtlP = new JPanel();
		posCtlP.setLayout(new BorderLayout());
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
		posCtlP.add(posCtlTB, BorderLayout.NORTH);
		posCtlP.add(posCtlSP, BorderLayout.CENTER);

		// Regulation Negative
		negReg = new JTable(nodeM);
		TableRowSorter sorterN = new TableRowSorter(nodeM);		
		negReg.setRowSorter(sorterN);
		negReg.getRowSorter().toggleSortOrder(0);
		
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
		
		// Experiment Nodes
		eDataNodes = new JTable(nodeM);
		TableRowSorter sorterE = new TableRowSorter(nodeM);		
		eDataNodes.setRowSorter(sorterE);
		eDataNodes.getRowSorter().toggleSortOrder(0);
		
		JScrollPane dataNodesSP = new JScrollPane(eDataNodes);
		dataNodesSP.setPreferredSize(new Dimension(200, 310));
		JToolBar dataNodesTB = new JToolBar("Experiment Nodes", JToolBar.HORIZONTAL);
		JLabel dataNodesLbl = new JLabel("Experiment Nodes");
		dataNodesLbl.setForeground(Color.BLUE.darker());
		dataNodesTB.add(dataNodesLbl);
		JButton unSelAllBtn3 = new JButton("UnselectAll");
		unSelAllBtn3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eDataNodes.clearSelection();
			}
		});
		dataNodesTB.add(unSelAllBtn3);
		JPanel dataNodesP = new JPanel();
		dataNodesP.setLayout(new BorderLayout());
		dataNodesP.add(dataNodesTB, BorderLayout.NORTH);
		dataNodesP.add(dataNodesSP, BorderLayout.CENTER);	
		
		Border dataNodesBorder = BorderFactory.createTitledBorder("Data Nodes (press ctl to select)");
		dataNodesP.setBorder(dataNodesBorder);
		
		// Constant Nodes
		constantNodes = new JTable(nodeM);
		TableRowSorter sorterC = new TableRowSorter(nodeM);		
		constantNodes.setRowSorter(sorterC);
		constantNodes.getRowSorter().toggleSortOrder(0);
		
		JScrollPane constantNodesSP = new JScrollPane(constantNodes);
		constantNodesSP.setPreferredSize(new Dimension(200, 310));
		JToolBar constantNodesTB = new JToolBar("Constant Nodes", JToolBar.HORIZONTAL);
		JLabel constantNodesLbl = new JLabel("Constant Nodes");
		constantNodesLbl.setForeground(Color.BLUE.darker());
		constantNodesTB.add(constantNodesLbl);
		JButton unSelAllBtnC = new JButton("UnselectAll");
		unSelAllBtnC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				constantNodes.clearSelection();
			}
		});
		constantNodesTB.add(unSelAllBtnC);
		JPanel constantNodesP = new JPanel();
		constantNodesP.setLayout(new BorderLayout());
		constantNodesP.add(constantNodesTB, BorderLayout.NORTH);
		constantNodesP.add(constantNodesSP, BorderLayout.CENTER);	
				
		Border constantNodesBorder = BorderFactory.createTitledBorder("Constant Nodes (press ctl to select)");
		constantNodesP.setBorder(constantNodesBorder);
				
		
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
		
		//separating regulation panels and dataNodes panel
		JSplitPane dataRegP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		dataRegP.add(regVP, JSplitPane.LEFT);
		dataRegP.add(dataNodesP, JSplitPane.RIGHT);
		dataRegP.setDividerLocation(0.5);
		JPanel dataRegVP = new JPanel();
		dataRegVP.add(dataRegP, BorderLayout.CENTER);
		
		//separating constant Nodes panel from the rest
		JSplitPane listP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		listP.add(dataRegVP, JSplitPane.LEFT);
		listP.add(constantNodesP, JSplitPane.RIGHT);
		listP.setDividerLocation(0.5);
		JPanel listVP = new JPanel();
		listVP.add(listP, BorderLayout.CENTER);
		
		//adding all panels
		p.add(listVP);
		
		p.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, p);
		this.setLocationByPlatform(true);
		this.pack();
	
	}
	
	public void actionPerformed(ActionEvent e) {
		
		ArrayList<String> upRNList = new ArrayList<String>();
		for (int i1 : posReg.getSelectedRows()) upRNList.add(posReg.getValueAt(i1, 0).toString());		
		ArrayList<String> downRNList = new ArrayList<String>();
		for (int i1 : negReg.getSelectedRows()) downRNList.add(negReg.getValueAt(i1, 0).toString());
		ArrayList<String> dataNodeList = new ArrayList<String>();
		for (int i1 : eDataNodes.getSelectedRows()) dataNodeList.add(eDataNodes.getValueAt(i1, 0).toString());
		ArrayList<String> constantNodeList = new ArrayList<String>();
		for (int i1 : constantNodes.getSelectedRows()) constantNodeList.add(constantNodes.getValueAt(i1, 0).toString());
		
		ArrayList<Double> initValuesList = new ArrayList<Double>();
		
		initValuesList.add(Double.parseDouble(initValueAlpha.getText()));
		initValuesList.add(Double.parseDouble(lbValueAlpha.getText()));
		initValuesList.add(Double.parseDouble(ubValueAlpha.getText()));
		initValuesList.add(Double.parseDouble(initValueBeta.getText()));
		initValuesList.add(Double.parseDouble(lbValueBeta.getText()));
		initValuesList.add(Double.parseDouble(ubValueBeta.getText()));
		initValuesList.add(Double.parseDouble(initValueH.getText()));
		initValuesList.add(Double.parseDouble(lbValueH.getText()));
		initValuesList.add(Double.parseDouble(ubValueH.getText()));
		initValuesList.add(Double.parseDouble(initValueDelta.getText()));
		initValuesList.add(Double.parseDouble(lbValueDelta.getText()));
		initValuesList.add(Double.parseDouble(ubValueDelta.getText()));
		initValuesList.add(Double.parseDouble(initValueX.getText()));
		initValuesList.add(Double.parseDouble(lbValueX.getText()));
		initValuesList.add(Double.parseDouble(ubValueX.getText()));
		
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			/*if (!selectedFile.toString().endsWith(".def")) {
				selectedFile=new File(selectedFile.toString()+".def");
			}*/
			
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
				
				String[] dataNodes = null;
				if(dataNodeList != null) {
					dataNodes = new String[0];
					dataNodes = dataNodeList.toArray(dataNodes);
				}
				
				String[] constantNodes = null;
				if(constantNodeList != null) {
					constantNodes = new String[0];
					constantNodes = constantNodeList.toArray(constantNodes);
				}
				
				double[] initValues = null;
				if(initValuesList != null && initValuesList.size() == 15) {
					initValues = new double[15];
					for(int i = 0; i < 15; i++) {
						initValues[i] = initValuesList.get(i);
					}
				}
				
				double fTime = Double.parseDouble(time.getText());
				
				RegulatoryNetwork network = new RegulatoryNetwork();
		        // Load a yED GraphML file into the network
		        network.loadYEdFile(currentFile);
				
				D2DMapping mapping = DefCreator.createFiles(selectedFile.toString(), network, dataNodes, upRNodes, downRNodes, initValues, constantNodes, fTime);
				D2DMappingFileInteractor.createD2DMappingFile(selectedFile.toString(), mapping);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
	}
	
	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		//toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolbar.setLayout(new GridBagLayout());
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(width, 39));
		toolbar.setFloatable(false);
		
		JButton generateScriptBtn = new JButton("Generate");
		generateScriptBtn.addActionListener(this);
		generateScriptBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		generateScriptBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(generateScriptBtn, new GridBagConstraints(10,2,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelAlpha = new JLabel("Init Alpha:");
		toolbar.add(labelAlpha);		
		toolbar.add(labelAlpha,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		initValueAlpha = new JTextField(Double.toString(alpha));
		initValueAlpha.setSize(60, 20);
		initValueAlpha.setPreferredSize(new Dimension(100, 20));
		initValueAlpha.setEditable(true);
		toolbar.add(initValueAlpha,new GridBagConstraints(1,0,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		JLabel labelLBAlpha = new JLabel("LB Alpha:");
		toolbar.add(labelLBAlpha);		
		toolbar.add(labelLBAlpha,new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		lbValueAlpha = new JTextField(Double.toString(lbAlpha));
		lbValueAlpha.setSize(60, 20);
		lbValueAlpha.setPreferredSize(new Dimension(100, 20));
		lbValueAlpha.setEditable(true);
		toolbar.add(lbValueAlpha,new GridBagConstraints(1,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		JLabel labelUBAlpha = new JLabel("UB Alpha:");
		toolbar.add(labelUBAlpha);		
		toolbar.add(labelUBAlpha,new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		ubValueAlpha = new JTextField(Double.toString(ubAlpha));
		ubValueAlpha.setSize(60, 20);
		ubValueAlpha.setPreferredSize(new Dimension(100, 20));
		ubValueAlpha.setEditable(true);
		toolbar.add(ubValueAlpha,new GridBagConstraints(1,2,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		JLabel labelBeta = new JLabel("Init Beta:");
		toolbar.add(labelBeta);		
		toolbar.add(labelBeta,new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		initValueBeta = new JTextField(Double.toString(beta));
		initValueBeta.setSize(60, 20);
		initValueBeta.setPreferredSize(new Dimension(100, 20));
		initValueBeta.setEditable(true);
		toolbar.add(initValueBeta,new GridBagConstraints(3,0,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelLBBeta = new JLabel("LB Beta:");
		toolbar.add(labelLBBeta);		
		toolbar.add(labelLBBeta,new GridBagConstraints(2,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		lbValueBeta = new JTextField(Double.toString(lbBeta));
		lbValueBeta.setSize(60, 20);
		lbValueBeta.setPreferredSize(new Dimension(100, 20));
		lbValueBeta.setEditable(true);
		toolbar.add(lbValueBeta,new GridBagConstraints(3,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelUBBeta = new JLabel("UB Beta:");
		toolbar.add(labelUBBeta);		
		toolbar.add(labelUBBeta,new GridBagConstraints(2,2,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		ubValueBeta = new JTextField(Double.toString(ubBeta));
		ubValueBeta.setSize(60, 20);
		ubValueBeta.setPreferredSize(new Dimension(100, 20));
		ubValueBeta.setEditable(true);
		toolbar.add(ubValueBeta,new GridBagConstraints(3,2,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		
		JLabel labelH = new JLabel("Init H:");
		toolbar.add(labelH);		
		toolbar.add(labelH,new GridBagConstraints(4,0,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		initValueH = new JTextField(Double.toString(h));
		initValueH.setSize(60, 20);
		initValueH.setPreferredSize(new Dimension(100, 20));
		initValueH.setEditable(true);
		toolbar.add(initValueH,new GridBagConstraints(5,0,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelLBH = new JLabel("LB H:");
		toolbar.add(labelLBH);		
		toolbar.add(labelLBH,new GridBagConstraints(4,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		lbValueH = new JTextField(Double.toString(lbH));
		lbValueH.setSize(60, 20);
		lbValueH.setPreferredSize(new Dimension(100, 20));
		lbValueH.setEditable(true);
		toolbar.add(lbValueH,new GridBagConstraints(5,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelUBH = new JLabel("UB H:");
		toolbar.add(labelUBH);		
		toolbar.add(labelUBH,new GridBagConstraints(4,2,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		ubValueH = new JTextField(Double.toString(ubH));
		ubValueH.setSize(60, 20);
		ubValueH.setPreferredSize(new Dimension(100, 20));
		ubValueH.setEditable(true);
		toolbar.add(ubValueH,new GridBagConstraints(5,2,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelDelta = new JLabel("Init Delta:");
		toolbar.add(labelDelta);		
		toolbar.add(labelDelta,new GridBagConstraints(6,0,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		initValueDelta = new JTextField(Double.toString(delta));
		initValueDelta.setSize(60, 20);
		initValueDelta.setPreferredSize(new Dimension(100, 20));
		initValueDelta.setEditable(true);
		toolbar.add(initValueDelta,new GridBagConstraints(7,0,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelLBDelta = new JLabel("LB Delta:");
		toolbar.add(labelLBDelta);		
		toolbar.add(labelLBDelta,new GridBagConstraints(6,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		lbValueDelta = new JTextField(Double.toString(lbDelta));
		lbValueDelta.setSize(60, 20);
		lbValueDelta.setPreferredSize(new Dimension(100, 20));
		lbValueDelta.setEditable(true);
		toolbar.add(lbValueDelta,new GridBagConstraints(7,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelUBDelta = new JLabel("UB Delta:");
		toolbar.add(labelUBDelta);		
		toolbar.add(labelUBDelta,new GridBagConstraints(6,2,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		ubValueDelta = new JTextField(Double.toString(ubDelta));
		ubValueDelta.setSize(60, 20);
		ubValueDelta.setPreferredSize(new Dimension(100, 20));
		ubValueDelta.setEditable(true);
		toolbar.add(ubValueDelta,new GridBagConstraints(7,2,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelNodeValues = new JLabel("Init Node Values:");
		toolbar.add(labelNodeValues);		
		toolbar.add(labelNodeValues,new GridBagConstraints(8,0,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		initValueX = new JTextField(Double.toString(initNodeValue));
		initValueX.setSize(60, 20);
		initValueX.setPreferredSize(new Dimension(100, 20));
		initValueX.setEditable(true);
		toolbar.add(initValueX,new GridBagConstraints(9,0,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelLBNodeValue = new JLabel("LB NodeValue:");
		toolbar.add(labelLBNodeValue);		
		toolbar.add(labelLBNodeValue,new GridBagConstraints(8,1,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		lbValueX = new JTextField(Double.toString(lbNodeValue));
		lbValueX.setSize(60, 20);
		lbValueX.setPreferredSize(new Dimension(100, 20));
		lbValueX.setEditable(true);
		toolbar.add(lbValueX,new GridBagConstraints(9,1,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelUBNodeValue = new JLabel("UB NodeValue:");
		toolbar.add(labelUBNodeValue);		
		toolbar.add(labelUBNodeValue,new GridBagConstraints(8,2,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		ubValueX = new JTextField(Double.toString(ubNodeValue));
		ubValueX.setSize(60, 20);
		ubValueX.setPreferredSize(new Dimension(100, 20));
		ubValueX.setEditable(true);
		toolbar.add(ubValueX,new GridBagConstraints(9,2,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JLabel labelTime = new JLabel("Final Time:");
		toolbar.add(labelTime);		
		toolbar.add(labelTime,new GridBagConstraints(0,3,1,1,1.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));
		time = new JTextField(Double.toString(initTime));
		time.setSize(60, 20);
		time.setPreferredSize(new Dimension(100, 20));
		time.setEditable(true);
		toolbar.add(time,new GridBagConstraints(1,3,1,1,2.0,1.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		return toolbar;
	}
	
	
	
}
