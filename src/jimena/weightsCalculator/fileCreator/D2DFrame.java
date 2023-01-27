package jimena.weightsCalculator.fileCreator;

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
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.solver.NodeTableModel;

/**
 * GUI for D2D. 
 * For now only creates the model.def and data.def files.
 * 
 * The GUI is mostly copy&paste from SolverFrame. (Thanks Chunguang Liang!)
 * 
 * @author Jan Krause
 * @since 13.01.2023
 * */
public class D2DFrame extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;

	//to select up and down regulated notes
	private JTable posReg;	
	private JTable negReg;
	
	//to select Nodes where experiment data is available 
	private JTable eDataNodes;
	
	//private HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>(); // Network
	private NetworkNode[] nodes = null;
	private File currentFile = null;
	
	
	public D2DFrame() {
		super("D2D - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(964, 450));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
	}
	
	public D2DFrame(File currentFile) {
		this();
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
		posCtlSP.setPreferredSize(new Dimension(300, 310));
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

		negCtlSP.setPreferredSize(new Dimension(300, 310));
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

		dataNodesSP.setPreferredSize(new Dimension(300, 310));
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
		
		
		// the panels
		// the regulation panels
		JSplitPane regP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		regP.add(posCtlP, JSplitPane.LEFT);
		regP.add(negCtlP, JSplitPane.RIGHT);
		regP.setDividerLocation(0.5);
		JPanel regVP = new JPanel();
		regVP.add(regP, BorderLayout.CENTER);
		
		Border regulationBorder = BorderFactory.createTitledBorder("Regulations (press ctl to select)");
		regVP.setBorder(regulationBorder);
		
		
		JSplitPane listP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		listP.add(regVP, JSplitPane.LEFT);
		listP.add(dataNodesP, JSplitPane.RIGHT);
		listP.setDividerLocation(0.5);
		JPanel listVP = new JPanel();
		listVP.add(listP, BorderLayout.CENTER);
		
		p.add(listVP);
		
		p.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, p);
		this.setLocationByPlatform(true);
		this.pack();
	
	}
	
	public void actionPerformed(ActionEvent e) {
		
		ArrayList<String> upRNList = new ArrayList<String>();
		ArrayList<String> downRNList = new ArrayList<String>();
		ArrayList<String> dataNodeList = new ArrayList<String>();
		for (int i1 : posReg.getSelectedRows()) upRNList.add(posReg.getValueAt(i1, 0).toString());		
		for (int i1 : negReg.getSelectedRows()) downRNList.add(negReg.getValueAt(i1, 0).toString());
		for (int i1 : eDataNodes.getSelectedRows()) dataNodeList.add(eDataNodes.getValueAt(i1, 0).toString());
		
		
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
		        	upRNodes = new String[1];
		        	upRNodes = upRNList.toArray(upRNodes);
		        }
		        String[] downRNodes = null;
				if(downRNList != null) {
					downRNodes = new String[1];
					downRNodes = downRNList.toArray(downRNodes);
				}
				
				String[] dataNodes = null;
				if(dataNodeList != null) {
					dataNodes = new String[1];
					dataNodes = dataNodeList.toArray(downRNodes);
				}
				
				RegulatoryNetwork network = new RegulatoryNetwork();
		        // Load a yED GraphML file into the network
		        network.loadYEdFile(currentFile);
				
				DefCreator.createFiles(selectedFile.toString(), network, dataNodes, upRNodes, downRNodes, 10);
				
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
		toolbar.setMaximumSize(new Dimension(624, 25));
		toolbar.setFloatable(false);
		
		JButton generateScriptBtn = new JButton("Generate");
		generateScriptBtn.addActionListener(this);
		generateScriptBtn.setIcon(new ImageIcon("images" + File.separator + "chart16.png"));
		generateScriptBtn.setForeground(Color.MAGENTA.darker());
		toolbar.add(generateScriptBtn, new GridBagConstraints(10,1,1,1,1.0,1.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		return toolbar;
	}
	
	
	
}
