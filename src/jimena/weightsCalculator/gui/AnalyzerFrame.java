package jimena.weightsCalculator.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.table.TableModel;

import jimena.binaryrn.RegulatoryNetwork;
import jimena.gui.main.Main;

/**
 * An Interface used by TableFrame to add additional analytic frames.
 * @author Jan Krause (April 2023)
 *
 */
public abstract class AnalyzerFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	protected String frameName = "";

	public abstract void editFrame(TableModel model, RegulatoryNetwork network);
	
	public AnalyzerFrame(String text) {
		super(text);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	public String getName() {
		return this.frameName;
	}

}
