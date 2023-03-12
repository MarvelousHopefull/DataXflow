package jimena.weightsCalculator.fileCreator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import jimena.gui.main.Main;

public class D2DExternalStimuliFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//Frame Size
	private static int width = 800;	//x
	private static int hight = 500;	//y

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
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setOpaque(true);
		
		
		p.add(createToolBar());
		this.getContentPane().add(BorderLayout.CENTER, p);
		this.setLocationByPlatform(true);
		this.pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		
		//D2D Parameter File
		JButton loadD2DFileBtn = new JButton("Load D2D File");
		loadD2DFileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Exceptions are caught by the loadFile() method
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
                // Exceptions are caught by the loadFile() method
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
