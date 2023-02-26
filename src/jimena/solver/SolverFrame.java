/**
 * SolverFrame.java
 */
package jimena.solver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.weightsCalculator.fileCreator.DefCreator;

/**
 * Switch Analyzer frame (SolverFrame)
 * 
 * @author Chunguang Liang, Department of Bioinformatics, University of Würzburg
 * @version 2.0
 *  
 */
// from jimena.gui.guilib.TableFrame


public class SolverFrame extends JFrame implements ActionListener {	


	private static final long serialVersionUID = 1L;
	private File modelFile = null;
	
	private HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>(); // Network
	private TableModel steadyStateModel = null;      // for two steady state tables (init. and target)
	private JTable posCtlT;
	private JTable negCtlT;
	private JTable steadyStateTable1;
	private JTable steadyStateTable2;
	
	
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
	
	// Negative regulation and Positive regulation
	private HashMap<String, Integer> ctlN;
	private HashMap<String, Integer> ctlP;

	public SolverFrame() {
		super("Switch Analyzer - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(700, 450));
		this.setPreferredSize(new Dimension(1024, 768));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
	}

	public SolverFrame(String steadyStateString, TableModel steadyStateModel, File modelFile)
			throws NumberFormatException, IOException {
		this();
		this.modelFile = modelFile;
		this.steadyStateModel = steadyStateModel;
		parseSteadyState();
		parseModelFile();

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setOpaque(true);

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
		p.add(steadyStateSP1);

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
		p.add(steadyStateSP2);

		// Summary of Network
		JTextArea t2 = new JTextArea();
		t2.setWrapStyleWord(true);
		t2.setText(getNetworkSummary());
		t2.setEditable(false);
		t2.setBackground(new Color(225, 225, 255));
		JScrollPane ts2 = new JScrollPane(t2);
		ts2.setPreferredSize(new Dimension(800, 80));
		ts2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		ts2.setBorder(BorderFactory.createTitledBorder("Summary of the network"));
		p.add(ts2);

		// nodes NodeM as tableModel , preparation for two regulation tables.
		Vector<String> colNames = new Vector<String>();
		colNames.add("Node");
		Vector<Vector<Object>> nodesV = new Vector<Vector<Object>>();
		for (Node i : nodeMap.values()) {
			Vector<Object> r = new Vector<Object>();
			r.add(i.getName());
			nodesV.add(r);
		}

		NodeTableModel nodeM = new NodeTableModel(nodesV, colNames);
		posCtlT = new JTable(nodeM);
		TableRowSorter sorterP=new TableRowSorter(nodeM);		
		posCtlT.setRowSorter(sorterP);
		posCtlT.getRowSorter().toggleSortOrder(0);

		// Regulation Positive
		JScrollPane posCtlSP = new JScrollPane(posCtlT);
		posCtlSP.setPreferredSize(new Dimension(500, 310));
		JPanel posCtlP = new JPanel();
		posCtlP.setLayout(new BorderLayout());
		JToolBar posCtlTB = new JToolBar("Positive regulation", JToolBar.HORIZONTAL);
		JLabel posCtlLbl = new JLabel("Positive regulation");
		posCtlLbl.setForeground(Color.RED.darker());
		posCtlTB.add(posCtlLbl);
		JButton unSelAllBtn1 = new JButton("UnselectAll");
		unSelAllBtn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				posCtlT.clearSelection();
			}
		});
		posCtlTB.add(unSelAllBtn1);
		posCtlP.add(posCtlTB, BorderLayout.NORTH);
		posCtlP.add(posCtlSP, BorderLayout.CENTER);

		// Negative Positive
		negCtlT = new JTable(nodeM);
		TableRowSorter sorterN=new TableRowSorter(nodeM);		
		negCtlT.setRowSorter(sorterN);
		negCtlT.getRowSorter().toggleSortOrder(0);
		JScrollPane negCtlSP = new JScrollPane(negCtlT);

		negCtlSP.setPreferredSize(new Dimension(500, 310));
		JToolBar negCtlTB = new JToolBar("Negative regulation", JToolBar.HORIZONTAL);
		JLabel negCtlLbl = new JLabel("Negative regulation");
		negCtlLbl.setForeground(Color.BLUE.darker());
		negCtlTB.add(negCtlLbl);
		JButton unSelAllBtn2 = new JButton("UnselectAll");
		unSelAllBtn2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				negCtlT.clearSelection();
			}
		});
		negCtlTB.add(unSelAllBtn2);
		JPanel negCtlP = new JPanel();
		negCtlP.setLayout(new BorderLayout());
		negCtlP.add(negCtlTB, BorderLayout.NORTH);
		negCtlP.add(negCtlSP, BorderLayout.CENTER);

		JSplitPane regP = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		regP.add(posCtlP, JSplitPane.LEFT);
		regP.add(negCtlP, JSplitPane.RIGHT);
		regP.setDividerLocation(0.5);
		JPanel regVP = new JPanel();
		regVP.add(regP, BorderLayout.CENTER);
		
		Border regulationBorder = BorderFactory.createTitledBorder("Regulations (press ctl to select)");
		regVP.setBorder(regulationBorder);
		p.add(regVP);
		
		// paramenter input toolbar and button (bottom)
		p.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, p);
		this.setLocationByPlatform(true);
		this.pack();
	}

	// currently we do not process it. keep for the future
	private void parseSteadyState() {
	}

	// text summary of the network, for act. inh. actBy inhBy information of each node
	private String getNetworkSummary() {
		String content = "ModelFile: " + modelFile.getName() + "\n";
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

	private void parseModelFile() throws NumberFormatException, IOException {
		BufferedReader br = null;
		if (modelFile == null) return;
			// modelFile = new File("/home/binf027/jimena2/mischnik_network.graphml");  
		br = new BufferedReader(new FileReader(modelFile));
		String ls1;
		Integer target = null;
		Integer source = null;
		Integer nodeId = null;
		String nodeLabel = null;

		while ((ls1 = br.readLine()) != null) {
			Matcher m1 = Pattern.compile("<node\\sid=\\\"n(\\d+)\\\">").matcher(ls1);
			if (m1.find()) {

				nodeId = Integer.parseInt(m1.group(1)) + 1; // System.out.println("Found node_id: " + m1.group(1)); //
															// +1, start from 1 not 0
			} else {
				Matcher m2 = Pattern.compile("\\s*<y:NodeLabel.+?>(.+)?<y:LabelModel>").matcher(ls1);
				if (m2.find()) {
					nodeLabel = m2.group(1);
					if (!nodeMap.containsKey(nodeId))
						nodeMap.put(nodeId, new Node(nodeId, nodeLabel));

				} else {
					Matcher m3 = Pattern.compile("<edge\\sid=\"e(\\d+)\"\\ssource=\"n(\\d+?)\"\\starget=\"n(\\d+?)\">")
							.matcher(ls1);
					if (m3.find()) {
						source = Integer.parseInt(m3.group(2)) + 1;
						target = Integer.parseInt(m3.group(3)) + 1;
					} else {
						Matcher m4 = Pattern.compile("<y:Arrows\\ssource=\"\\w+?\"\\starget=\"(\\w+?)\"\\/>")
								.matcher(ls1);
						if (m4.find()) {
							// System.out.println(m4.group(1)+ "\tsrc: " + source + "\t tgt:"+target);
							if (m4.group(1).equals("standard")) {
								nodeMap.get(target).addActBy(source); // act
								nodeMap.get(source).addAct(target);
							} else if (m4.group(1).equals("t_shape")) {
								nodeMap.get(target).addInhBy(source); // inh
								nodeMap.get(source).addInh(target);
							}
						}
					}
				}
			}
		} // while ls
		br.close();
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		//toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
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

		JLabel labelMFile = new JLabel("Model:");
		toolbar.add(labelMFile,new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

		JTextField modelF = new JTextField(modelFile.getName());
		modelF.setSize(250, 20);
		modelF.setPreferredSize(new Dimension(300, 20));
		modelF.setEditable(false);
		toolbar.add(modelF,new GridBagConstraints(3,0,3,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0));

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
//		   Use GridBagConstrains for the toolbar panel
//		   public GridBagConstraints(int gridx, int gridy,
//                   int gridwidth, int gridheight,
//                   double weightx, double weighty,
//                   int anchor, int fill,
//                   Insets insets, int ipadx, int ipady)
		

		return toolbar;
	}

	public String matlab_header(int numNodes, int numControls, float timeInterval, float timeHorizon, float alphaM,
			String initStateS, String targetStateS, String onlyFn) {
		String a = "function [  ] = "+onlyFn+"()\n";		
		BufferedReader br2=null;
		try {
			br2= new BufferedReader(new FileReader("matlab"+File.separator+"matlabsolver_header.txt"));
			String l="";
			while ((l=br2.readLine())!=null) {
				a+=l+"\n";			
			}
		} catch (Exception e) {			
			e.printStackTrace();
		}	
		
		a   +="\n"
			+ "tol1=10^-1;\n"
			+ "tol2=10^-6;\n"
			+ "tol3=10^-4;\n"
			+ "T_int=10^-1;\n"			
			+ "max_Num=4;\n"
			+ "max_iter=10000;\n"
			+ "combi_method=1;\n"
			+ "local_optimization_method=1;\n\n";
		a   += "OCP=struct(\'numNodes\'," + numNodes + ",\'numControls\',"
			+ numControls + ",\'timeInterval\'," + timeInterval + ",\'timeHorizon\'," 
			+ timeHorizon + ",\'alpha\',"
			+ alphaW + ",\'initialState\',[" + initStateS + "]);\n\n";
		a   += "xd=get_xd([" + targetStateS + "], OCP);\n";
		return a;
	}

	public String matlab_footer() {
		String a = "";
		BufferedReader br2 = null;
		try {
			br2 = new BufferedReader(new FileReader("matlab"+File.separator+"matlabsolver_footer.txt"));
			String l = "";
			while ((l = br2.readLine()) != null) {
				a += l + "\n";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	private String generateFormular(Float alpha, Float beta, Float gamma, Float h) {
		String content = "";

		ctlP = new HashMap<String, Integer>();
		ctlN = new HashMap<String, Integer>();
	
		int n = 0;
		for (int i1 : posCtlT.getSelectedRows()) ctlP.put(posCtlT.getValueAt(i1, 0).toString(), ++n);		
		
		for (int i1 : negCtlT.getSelectedRows()) ctlN.put(negCtlT.getValueAt(i1, 0).toString(), ++n);
	

		for (Integer i : nodeMap.keySet()) {
			String output_j = "";
			System.out.println("Node " + i + " : " + nodeMap.get(i).getName());
			String nodeX = "x(" + i + ")";
			String omega = "";
			if (nodeMap.get(i).getActBy().size() > 0) {
				float alphasum = 0;
				String actsum = "";
				boolean alreadyinput = false;
				for (Integer j : nodeMap.get(i).getActBy()) {
					System.out.println("\tActivated by " + j + ":" + nodeMap.get(j).getName());
					alphasum += alpha;
					if (alreadyinput)
						actsum += '+';
					actsum += (alpha == 1 ? "" : Float.toString(alpha) + "*") + "x(" + Integer.toString(j) + ")";
					alreadyinput = true;
				}
				omega = "(" + Float.toString(1 + alphasum) + "/" + Float.toString(alphasum) + ")*((" + actsum + ")/(1+"
						+ actsum + "))";
			}

			if (nodeMap.get(i).getInhBy().size() > 0) {
				boolean alreadyinput = false;
				float betasum = 0;
				String inhsum = "";
				for (Integer j : nodeMap.get(i).getInhBy()) {
					System.out.println("\tInhed by " + j + ":" + nodeMap.get(j).getName());
					betasum += beta;
					if (alreadyinput)
						inhsum += '+';
					inhsum += (beta == 1 ? "" : Float.toString(beta) + "*") + "x(" + Integer.toString(j) + ")";
					alreadyinput = true;
				}
				if (!omega.isEmpty()) omega += "*";
				
				omega += "(1-"+"(" + Float.toString(1 + betasum) + "/" + Float.toString(betasum) + ")*((" + inhsum + ")/(1+"
						+ inhsum + "))"+")";
			}

			if (nodeMap.get(i).getActBy().size() > 0 || nodeMap.get(i).getInhBy().size() > 0) {
				output_j += "@(x,u)((-exp(" + (0.5 * h) + ")+exp(-" + h + "*((" + omega + ")-0.5)))/((1-exp("
						 + (0.5 * h) + "))*(1+exp(-" + h + "*((" + omega + ")-0.5)))))-"
						 + (gamma == 1 ? nodeX : gamma + "*" + nodeX);				
				// output_j .=
				// "@(x,u)((-exp(".0.5*$h.")+exp(-$h*(($omega)-0.5)))/((1-exp(".0.5*$h."))*(1+exp(-$h*(($omega)-0.5)))))-$gamma*$nodeX";
			} else {
				output_j += "@(x,u)-" + gamma + "*" + nodeX;     // $output_j .="@(x,u)-$gamma*$nodeX";
			}

			System.out.print("Positive regulation: ");
			for (int i1 : posCtlT.getSelectedRows()) {
				System.out.print(posCtlT.getValueAt(i1, 0) + "\t");
			}
			System.out.println();

			System.out.print("Negative regulation: ");
			for (int i1 : negCtlT.getSelectedRows()) {
				System.out.print(negCtlT.getValueAt(i1, 0) + "\t");
			}
			System.out.println();

			if (ctlP.containsKey(nodeMap.get(i).getName())) {
				//System.out.println("positive Control added for node " + nodeMap.get(i).toString());
				output_j += "+u(" + (ctlP.get(nodeMap.get(i).getName())) + ")*(1-x(" + i + "))";				
			}
			if (ctlN.containsKey(nodeMap.get(i).getName())) {
				//System.out.println("negative Control added for node " + nodeMap.get(i).toString());
				output_j += "-u(" + (ctlN.get(nodeMap.get(i).getName())) + ")*x(" + i + ")";				
			}

			if (!content.isEmpty())	content += ",...\n";
			content += output_j;
		} // node loop
		content = "f= {" + content + "};\n";
		return content;
	}
	
	private String getCommentNodes() {
		String content = "% Node_list \n";
		for (Integer i : nodeMap.keySet()) {
			content += "% Node " + i + " : " + nodeMap.get(i).getName() + "\n";			
		}
		
		content += "%\n% Regulation_list\n";	
		for (String n: ctlP.keySet())
			content += "% Pos "+ctlP.get(n).toString()+" : "+n+"\n";
	
		for (String n: ctlN.keySet()) 
			content += "% Neg "+ctlN.get(n).toString()+" : "+n+"\n";		
		return content;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int initR = steadyStateTable1.getSelectedRow();
		int targetR = steadyStateTable2.getSelectedRow();
		
		if (steadyStateTable1.getSelectedColumnCount()==0 || steadyStateTable2.getSelectedColumnCount()==0) {
			JOptionPane.showMessageDialog(this,"Please specify one initial steady state and desired target state in the tables");
			return;
		}
		
		// initial state
		HashMap<String, Object> sst1 = new HashMap<String, Object>();
		for (int i = 0; i < steadyStateTable1.getColumnCount(); i++) {
			sst1.put(steadyStateTable1.getColumnName(i), steadyStateTable1.getValueAt(initR, i));
		}
		String initStateS = "";
		for (Integer i : nodeMap.keySet()) {
			if (!initStateS.isEmpty())
				initStateS += ',';
			initStateS += sst1.get(nodeMap.get(i).getName());
		}

		// target state
		HashMap<String, Object> sst2 = new HashMap<String, Object>();
		for (int i = 0; i < steadyStateTable2.getColumnCount(); i++) {
			sst2.put(steadyStateTable2.getColumnName(i), steadyStateTable2.getValueAt(targetR, i));
		}
		String targetStateS = "";
		for (Integer i : nodeMap.keySet()) {
			if (!targetStateS.isEmpty())
				targetStateS += ',';
			targetStateS += sst2.get(nodeMap.get(i).getName());
		}

//		System.out.println("initial state:" + initStateS);
//		System.out.println("target  state:" + targetStateS);
//		System.out.print("Positive regulation: ");
//		for (int i : posCtlT.getSelectedRows()) System.out.print(posCtlT.getValueAt(i, 0) + "\t");
//		System.out.println();
//		System.out.print("Negative regulation: ");
//		for (int i : negCtlT.getSelectedRows()) System.out.print(negCtlT.getValueAt(i, 0) + "\t");	
//		System.out.println();

		
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			/*
			File selectedDefFile = new File(selectedFile.toString()+".def");
			File selectedCSVNodeNamesFile = new File(selectedFile.toString()+".csv");
			RegulatoryNetwork network = new RegulatoryNetwork();
	        // Load a yED GraphML file into the network
	        network.loadYEdFile(Main.currentFile);
	        */
			if (!selectedFile.toString().endsWith(".m")) {
				selectedFile=new File(selectedFile.toString()+".m");
			}
			String onlyFn=selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.'));
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(selectedFile));
				bw.write(matlab_header(nodeMap.size(), posCtlT.getSelectedRowCount() + negCtlT.getSelectedRowCount(),
						Float.parseFloat(valueInterval.getText()), Float.parseFloat(valuelHorizon.getText()), Float.parseFloat(valueAlphaW.getText()), initStateS, targetStateS, onlyFn));
				bw.write(generateFormular(Float.parseFloat(valueAlpha.getText()), Float.parseFloat(valueBeta.getText()), Float.parseFloat(valueGamma.getText()), Float.parseFloat(valueH.getText())));
				bw.write(matlab_footer());
				bw.write(getCommentNodes());
				bw.close();
				
				/*
				ArrayList<String> upRNList = new ArrayList<>();
		        ArrayList<String> downRNList = new ArrayList<>();
		        
		        for(Integer i : nodeMap.keySet()) {
		        	if (ctlP.containsKey(nodeMap.get(i).getName())) {
						upRNList.add(nodeMap.get(i).getName());
		        	}
					if (ctlN.containsKey(nodeMap.get(i).getName())) {
						downRNList.add(nodeMap.get(i).getName());				
					}
		        }
		        
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
				
				DefCreator.createFile(selectedDefFile.toString(), network, upRNodes, downRNodes);
				CSVNodeNamesCreator.createFile(selectedCSVNodeNamesFile.toString(), network);
				*/
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}	
	}
	

}
