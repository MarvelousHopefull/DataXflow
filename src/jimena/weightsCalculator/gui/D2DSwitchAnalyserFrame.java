package jimena.weightsCalculator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import jimena.binaryrn.NetworkNode;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;

/**
* GUI for SteadyState analysis used with D2D. 
* 
* @author Jan Krause
* @since 13.01.2023
* */
public class D2DSwitchAnalyserFrame extends AnalyserFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	//Frame Size
	private static int width = 884;	//x
	private static int hight = 520;	//y

	//to select up and down regulated notes
	private JTable posReg;	
	private JTable negReg;
	
	private RegulatoryNetwork network = null;
	
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editFrame(TableModel model, RegulatoryNetwork network) {
		this.network = network;
		NetworkNode[] nodes = this.network.getNetworkNodes();
		
	}

}
