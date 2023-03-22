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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;
import jimena.weightsCalculator.D2DMapping;
import jimena.weightsCalculator.fileCreator.D2DMappingFileInteractor;
import jimena.weightsCalculator.fileCreator.ExternalStimuliFileCreator;

public class D2DExternalStimuliFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//Frame Size
	private static int width = 800;	//x
	private static int hight = 500;	//y

	private File currentFile = null;
	
	private JTable nodesTable;
	NodesOfInterestTableModel model;
	
	private File d2dFile = null;
	private String d2dFileName = "no File selected";
	JTextField d2dF = null;
	
	private File mappingFile = null;
	private String mappingFileName = "no File selected";
	JTextField mappingF = null;
	
	
	
	public D2DExternalStimuliFrame() {
		super("D2DExternalStimuli - ver. "+Main.ver);
		setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
		setMinimumSize(new Dimension(500, 400));
		this.setPreferredSize(new Dimension(width, hight));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());		
	}
	
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
		model = new NodesOfInterestTableModel(nodesOfInterest);
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setOpaque(true);
		
		// Node of Interest Selection
		nodesTable = new JTable(model);
		nodesTable.setCellSelectionEnabled(false);

		JScrollPane nodesSP = new JScrollPane(nodesTable);
		nodesSP.setPreferredSize(new Dimension(200, 310));
		
		JPanel nodesP = new JPanel();
		nodesP.setLayout(new BorderLayout());
		nodesP.add(nodesSP, BorderLayout.CENTER);
		
		Border nodesBorder = BorderFactory.createTitledBorder("Nodes of Interest");
		nodesP.setBorder(nodesBorder);
		
		p.add(nodesP);
		
		p.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, p);
		this.setLocationByPlatform(true);
		this.pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		List<NodeOfInterest> nodesList = new ArrayList<NodeOfInterest>();
		for (int i = 0; i < model.getRowCount(); i++) {
			if(model.isNodeOfInterest(i)) {
				nodesList.add(model.getNode(i));
			}		
		}
		
		
		JFileChooser fc = new JFileChooser();
		int result = fc.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fc.getSelectedFile();
			
			try {
				
				RegulatoryNetwork network = new RegulatoryNetwork();
		        // Load a yED GraphML file into the network
		        network.loadYEdFile(currentFile);
				D2DMapping mapping = D2DMappingFileInteractor.getD2DMapping(mappingFile.toString());
				//... change mapping ...
				ExternalStimuliFileCreator.createFile(selectedFile.toString(), d2dFile.toString(), mapping, network, nodesList);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}

	}
	
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
	
	private void loadD2DFile(File file) {
		this.d2dFile = file;
		d2dFileName = d2dFile.getName();
		d2dF.setText(d2dFileName);
	}
	
	private void loadMappingFile(File file) {
		this.mappingFile = file;
		mappingFileName = mappingFile.getName();
		mappingF.setText(mappingFileName);
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
