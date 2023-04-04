package jimena.gui.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import jimena.analysis.FunctionsAnalysis;
import jimena.binaryrn.NetworkConnection;
import jimena.binaryrn.RegulatoryNetwork;
import jimena.binaryrn.RegulatoryNetworkObserver;
import jimena.calculationparameters.ConvergenceParameters;
import jimena.gui.charts.ChartsWindow;
import jimena.gui.guilib.TableFrame;
import jimena.gui.guilib.datatable.ConnectionsTable;
import jimena.gui.guilib.datatable.NodesTable;
import jimena.libs.ArrayLib;
import jimena.libs.MathLib;
import jimena.libs.StandardNumberFormat;
import jimena.perturbation.PerturbationsWindow;
import jimena.settings.SettingsWindow;
import jimena.simulation.CalculationController;
import jimena.simulation.ProgressWindow;
import jimena.simulationmethods.ARBNMethod;
import jimena.simulationmethods.BooleCubeMethod;
import jimena.simulationmethods.CRBNMethod;
import jimena.simulationmethods.DARBNMethod;
import jimena.simulationmethods.HillCubeMethod;
import jimena.simulationmethods.NormalizedHillCubeMethod;
import jimena.simulationmethods.SQUADMethod;
import jimena.simulationmethods.SimulationMethod;
import jimena.solver.SolverFrame;
import jimena.sssearcher.RandomSearcher;
import jimena.sssearcher.StepwiseSearcher;
import jimena.weightsCalculator.gui.D2DExternalStimuliFrame;
import jimena.weightsCalculator.gui.D2DFrame;

/**
 * 
 * Implements the main windows of the application.
 *
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 *
 */
public class Main extends JFrame implements RegulatoryNetworkObserver {
    private static final long serialVersionUID = -5355497689587430613L;
    
    public static String ver="2.05";

    // Lower bound of the simulation parameters 
    private final double MINDT = 0.0000001;
    private final double MINMAXT = 0.0001;
    private final double MINSPEED = 0.0001;

    // A preference structure as a persistent data storage
    private Preferences preferences = Preferences.userNodeForPackage(this.getClass());

    // This array determines which simulationMethods are offered to the user
    private SimulationMethod[] simulationMethods = { new SQUADMethod(), new BooleCubeMethod(), new ARBNMethod(), new CRBNMethod(),
            new DARBNMethod(), new HillCubeMethod(), new NormalizedHillCubeMethod() };

    // The network to simulate and its file if it was loaded from a file
    private RegulatoryNetwork network = new RegulatoryNetwork();
    
    public static File currentFile;

    // Other windows
    private ConnectionsTable connectionsFrame = new ConnectionsTable(network);
    private NodesTable nodesFrame = new NodesTable(network);
    private PerturbationsWindow perturbationFrame = new PerturbationsWindow(network);
    private ChartsWindow chartsFrame = new ChartsWindow(network);
    private SettingsWindow settingsFrame = new SettingsWindow();

    // The panel to draw the graph on
    private JPanel panelGraph;

    // Some globally used GUI elements
    private JComboBox<SimulationMethod> comboMethods;
    public static JTextField textDt; 			           //dt  :timeInterval
    public static JTextField textmaxt;       	           //max :timeHorizon
    public static JTextField textMaxSpeed;  
    private JMenuItem menuFile1 = new JMenuItem(preferences.get("file1", ""));
    private JMenuItem menuFile2 = new JMenuItem(preferences.get("file2", ""));
    private JMenuItem menuFile3 = new JMenuItem(preferences.get("file3", ""));
    private JMenuItem menuFile4 = new JMenuItem(preferences.get("file4", ""));

    // File filters for the dialogs
    private FileFilter graphmlFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.getName().matches(".*\\.graphml$") || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "yEd GraphML-files (*.graphml)";
        }
    };

    private FileFilter jimenaFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.getName().matches(".*\\.jimena$") || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Jimena-files (*.jimena)";
        }
    };

    private FileFilter txtFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.getName().matches(".*\\.txt$") || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Text file (*.txt, format: [node-name][tab][value])";
        }
    };

    // Longer Calculations encapsulated in threads

    /**
     * An abstract superclass for all longer calculations.
     *
     * This class locks and unlocks the GUI and checks whether the network is empty.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private abstract class Calculation extends Thread {
        @Override
        public final void run() {
            lockGUI();

            if (network.size() != 0) {
                try {
                    doCalculation();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Calculation failed", JOptionPane.OK_OPTION
                            ^ JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "No nodes in the network.", "Search failed", JOptionPane.OK_OPTION
                        ^ JOptionPane.ERROR_MESSAGE);
            }

            unlockGUI();
        }

        abstract void doCalculation() throws Exception;
    }

    /**
     * Queries the user for an integer.
     *
     * @param text
     *            Text of the query.
     * @param title
     *            Titles of the query
     * @param standard
     *            Standard input.
     * @return Input, or -1 if the input was not numerical.
     */
    private int inputNumber(String text, String title, int standard) {
        String s = (String) JOptionPane.showInputDialog(Main.this, text, title, JOptionPane.QUESTION_MESSAGE, null, null,
                String.valueOf(standard));

        if (s == null) {
            return -1;
        }

        int result = 0;
        try {
            result = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            JOptionPane
                    .showMessageDialog(null, "Please enter a number", "Input Invalid", JOptionPane.OK_OPTION ^ JOptionPane.ERROR_MESSAGE);
            return -1;
        }

        return result;

    }

    /**
     * Encapsulates a calculation that finds SSS of the network.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class FindSSSThread extends Calculation {
        @Override
        void doCalculation() {
            int maxTime = inputNumber("Number of seconds to search", "Maximum Calculation Time", 10);

            if (maxTime == -1) {
                return;
            }

            StepwiseSearcher sssearcher = new RandomSearcher();

            int initializeWithDiscreteSSS = JOptionPane.showConfirmDialog(null,
                    "Do you want to include the discrete stable states as initial values for the search?\n\n(Since an external"
                            + " library is used to calculate the discrete stable\nstates this might crash the programm if the network"
                            + " is unusually complex or\nfeatures a very large number of discrete stable states.)",
                    "Discrete Stable States", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch (initializeWithDiscreteSSS) {
            case JOptionPane.YES_OPTION:
                sssearcher.setInitializeWithDiscreteSSS(true);
                break;
            case JOptionPane.NO_OPTION:
                sssearcher.setInitializeWithDiscreteSSS(false);
                break;
            default:
                // Cancel
                return;
            }

            ArrayList<double[]> result = network.stableSteadyStates(getConvergenceParametersFromGUI(), maxTime * 1000, sssearcher);

            if (result != null) {
                (new TableFrame(Main.this, "-Stable States-", ArrayLib.doublesListToObjectsListUnchecked(result), network.getNodeNames(),
                        Double.class, null,currentFile)).setVisible(true);
            }
        }
    }

    private ConvergenceParameters getConvergenceParametersFromGUI() {
        return new ConvergenceParameters(getMethod(), getDt(), getMaxt(), new ProgressWindow(),
                SettingsWindow.MAXSTABILITYDIFFERENCE.getValue(), SettingsWindow.MINSTABILITYTIME.getValue(),
                SettingsWindow.THREADS.getValue(), SettingsWindow.MAXDUPLICATEDIFFERENCE.getValue());
    }

    /**
     * Encapsulates a calculation that simulates the network shown in the window.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class SimulateThread extends Calculation {
        boolean singleStep;

        public SimulateThread(boolean singleStep) {
            this.singleStep = singleStep;
        }

        @Override
        void doCalculation() {
            CalculationController controller = new ProgressWindow();
            try {
                network.simulate(getMethod(), getDt(), singleStep ? getDt() : getMaxt(), getMaxSpeed(),
                        SettingsWindow.MINSIMULATIONTIMEBETWEENLOGS.getValue(), controller);
            } catch (Exception e) {
                controller.notifyCalculationFinished();
                throw e;
            }

        }
    }

    /**
     * Encapsulates a calculation that determines the SSS the current values converge to.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class SSSFromCurrentValuesThread extends Calculation {
        @Override
        void doCalculation() throws Exception {
            ConvergenceParameters parameters = getConvergenceParametersFromGUI();
            CalculationController controller = parameters.getCalculationController();
            try {
                double[] stableSteadyState = network.stableSteadyState(network.getValues(), parameters);

                if (stableSteadyState != null) {
                    ArrayList<double[]> resultAsList = new ArrayList<double[]>();
                    resultAsList.add(stableSteadyState);

                    (new TableFrame(Main.this, "Discrete Stable States", ArrayLib.doublesListToObjectsListUnchecked(resultAsList),
                            network.getNodeNames(), Double.class, null)).setVisible(true);

                } else {
                    JOptionPane.showMessageDialog(null, "The simulation did not converge in the specified time.",
                            "Simulation Did Not Converge", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                controller.notifyCalculationFinished();
                throw e;
            }
        }
    }

    /**
     * Encapsulates a calculation of the discrete SSS of the current network.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class DiscreteSSSThread extends Calculation {
        @Override
        void doCalculation() {
            (new TableFrame(Main.this, "Discrete Stable States", ArrayLib.bytesListToObjectsListUnchecked(network
                    .discreteStableSteadyStates()), network.getNodeNames(), Byte.class, null)).setVisible(true);
        }
    }

    private interface CentralityGetter {
        public double[] getCentralities() throws Exception;

        @Override
        public String toString();
    }

    private int numberOfSimulations() {
        return inputNumber("Enter the number of simulations to approximate the integral in the formula.\n\n"
                + "Large numbers of simulations increase the accuracy of the approximation " + "and the duration of the calculation.\n"
                + "In very large networks, the GUI may become unresponsive, if the number of simulations is too great.",
                "Number of simulations", 20);
    }

    private int numberOfInnerSimulations() {
        return inputNumber("Enter the number of simulations to approximate the minimum in the formula.\n\n"
                + "Large numbers of simulations increase the accuracy of the approximation " + "and the duration of the calculation.\n"
                + "In very large networks, the GUI may become unresponsive, if the number of simulations is too great.",
                "Number of simulations", 20);
    }

    /**
     * Encapsulates the calculation of the centralities of nodes in the network.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class AllNodeCentralities extends Calculation {
        private class AllNodeTCs implements CentralityGetter {
            @Override
            public double[] getCentralities() throws Exception {
                int simulations = numberOfSimulations();
                if (simulations == -1) {
                    return null;
                }
                ConvergenceParameters parameters = getConvergenceParametersFromGUI();
                CalculationController controller = parameters.getCalculationController();
                try {
                    return FunctionsAnalysis.totalCentralities(network, simulations, parameters);
                } catch (Exception e) {
                    controller.notifyCalculationFinished();
                    throw e;
                }
            }

            @Override
            public String toString() {
                return "Total centrality";
            }
        }

        private class AllNodeVCs implements CentralityGetter {
            @Override
            public double[] getCentralities() throws Exception {
                int simulations = numberOfSimulations();
                if (simulations == -1) {
                    return null;
                }
                ConvergenceParameters parameters = getConvergenceParametersFromGUI();
                CalculationController controller = parameters.getCalculationController();
                try {
                    return FunctionsAnalysis.valueCentralities(network, simulations, parameters);
                } catch (Exception e) {
                    controller.notifyCalculationFinished();
                    throw e;
                }
            }

            @Override
            public String toString() {
                return "Value centrality";
            }
        }

        private class AllNodeDCs implements CentralityGetter {
            @Override
            public double[] getCentralities() throws Exception {
                int simulations = numberOfSimulations();
                if (simulations == -1) {
                    return null;
                }
                int innerSimulations = numberOfInnerSimulations();
                if (innerSimulations == -1) {
                    return null;
                }

                ConvergenceParameters parameters = getConvergenceParametersFromGUI();
                CalculationController controller = parameters.getCalculationController();
                try {
                    return FunctionsAnalysis.dynamicCentralities(network, simulations, innerSimulations, parameters);
                } catch (Exception e) {
                    controller.notifyCalculationFinished();
                    throw e;
                }
            }

            @Override
            public String toString() {
                return "Dynamic centrality";
            }
        }

        @Override
        void doCalculation() throws Exception {
            CentralityGetter[] modes = new CentralityGetter[] { new AllNodeTCs(), new AllNodeVCs(), new AllNodeDCs() };
            CentralityGetter mode = (CentralityGetter) JOptionPane.showInputDialog(Main.this, "Select the calculation", "Calculation",
                    JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
            if (mode == null) {
                return;
            }

            double[] centralities = mode.getCentralities();

            if (centralities == null) {
                return;
            }

            ArrayList<double[]> result = new ArrayList<double[]>();
            result.add(centralities);

            (new TableFrame(Main.this, mode.toString(), ArrayLib.doublesListToObjectsListUnchecked(result), network.getNodeNames(),
                    Double.class, new String[] { "Node", "Centrality" })).setVisible(true);
        }
    }

    /**
     * Encapsulates the calculation of the centralities of nodes in the network.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class AllConnectionCentralities extends Calculation {
        private class AllConnectionTCs implements CentralityGetter {
            @Override
            public double[] getCentralities() throws Exception {
                int simulations = numberOfSimulations();
                if (simulations == -1) {
                    return null;
                }
                ConvergenceParameters parameters = getConvergenceParametersFromGUI();
                CalculationController controller = parameters.getCalculationController();
                try {
                    double[] results = new double[network.getConnections().size()];
                    int index = 0;
                    for (NetworkConnection connection : network.getConnections()) {
                        results[index] = FunctionsAnalysis.totalCentrality(network, connection, simulations,
                                parameters.cloneWithoutController());
                        index++;

                        if (parameters.getCalculationController() != null && !parameters.getCalculationController().isOn()) {
                            return null;
                        }
                        if (parameters.getCalculationController() != null) {
                            parameters.getCalculationController().setProgress(index + 1, network.getConnections().size());
                        }
                    }

                    if (parameters.getCalculationController() != null) {
                        parameters.getCalculationController().notifyCalculationFinished();
                    }

                    return results;
                } catch (Exception e) {
                    controller.notifyCalculationFinished();
                    throw e;
                }
            }

            @Override
            public String toString() {
                return "Total centrality";
            }
        }

        private class AllConnectionVCs implements CentralityGetter {
            @Override
            public double[] getCentralities() throws Exception {
                int simulations = numberOfSimulations();
                if (simulations == -1) {
                    return null;
                }
                ConvergenceParameters parameters = getConvergenceParametersFromGUI();
                CalculationController controller = parameters.getCalculationController();
                try {
                    double[] results = new double[network.getConnections().size()];
                    int index = 0;
                    for (NetworkConnection connection : network.getConnections()) {
                        results[index] = FunctionsAnalysis.valueCentrality(network, connection, simulations,
                                parameters.cloneWithoutController());
                        index++;

                        if (parameters.getCalculationController() != null && !parameters.getCalculationController().isOn()) {
                            return null;
                        }
                        if (parameters.getCalculationController() != null) {
                            parameters.getCalculationController().setProgress(index + 1, network.getConnections().size());
                        }
                    }

                    if (parameters.getCalculationController() != null) {
                        parameters.getCalculationController().notifyCalculationFinished();
                    }

                    return results;
                } catch (Exception e) {
                    controller.notifyCalculationFinished();
                    throw e;
                }
            }

            @Override
            public String toString() {
                return "Value centrality";
            }
        }

        private class AllConnectionDCs implements CentralityGetter {
            @Override
            public double[] getCentralities() throws Exception {
                int simulations = numberOfSimulations();
                if (simulations == -1) {
                    return null;
                }
                int innerSimulations = numberOfInnerSimulations();
                if (innerSimulations == -1) {
                    return null;
                }

                ConvergenceParameters parameters = getConvergenceParametersFromGUI();
                CalculationController controller = parameters.getCalculationController();
                try {
                    double[] results = new double[network.getConnections().size()];
                    int index = 0;
                    for (NetworkConnection connection : network.getConnections()) {
                        results[index] = FunctionsAnalysis.totalCentrality(network, connection, simulations,
                                parameters.cloneWithoutController());
                        index++;

                        if (parameters.getCalculationController() != null && !parameters.getCalculationController().isOn()) {
                            return null;
                        }
                        if (parameters.getCalculationController() != null) {
                            parameters.getCalculationController().setProgress(index + 1, network.getConnections().size());
                        }
                    }

                    if (parameters.getCalculationController() != null) {
                        parameters.getCalculationController().notifyCalculationFinished();
                    }

                    return results;
                } catch (Exception e) {
                    controller.notifyCalculationFinished();
                    throw e;
                }
            }

            @Override
            public String toString() {
                return "Dynamic centrality";
            }
        }

        @Override
        void doCalculation() throws Exception {
            CentralityGetter[] modes = new CentralityGetter[] { new AllConnectionTCs(), new AllConnectionVCs(), new AllConnectionDCs() };
            CentralityGetter mode = (CentralityGetter) JOptionPane.showInputDialog(Main.this, "Select the calculation", "Calculation",
                    JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
            if (mode == null) {
                return;
            }

            double[] singleResult = mode.getCentralities();
            if (singleResult == null) {
                return;
            }
            ArrayList<double[]> result = new ArrayList<double[]>();
            result.add(singleResult);
            (new TableFrame(Main.this, mode.toString(), ArrayLib.doublesListToObjectsListUnchecked(result),
                    network.getConnectionsStrings(), Double.class, new String[] { "Node", "Centrality" })).setVisible(true);
        }
    }

    /**
     * Returns the extension of a file.
     *
     * @param file
     *            The File
     * @return The Extension
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf(".");
        return name.substring(dot + 1);
    }

    /**
     * Loads a file into the network and refreshes the window size. The adequate loading method will be inferred from the extension.
     *
     * @param file
     *            The file to load
     */
    public void loadFile(File file) {
        if (getFileExtension(file).equalsIgnoreCase("graphml")) {
            try {
                network.loadYEdFile(file);
                network.sortNetworkNodes();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "GraphML Loading Failed", JOptionPane.OK_OPTION
                        ^ JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (getFileExtension(file).equalsIgnoreCase("jimena")) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                RegulatoryNetwork storedNetwork = (RegulatoryNetwork) objectInputStream.readObject();
                network.loadNetwork(storedNetwork);
                objectInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e.getMessage(), "Jimena Loading Failed", JOptionPane.OK_OPTION
                        ^ JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            JOptionPane.showMessageDialog(null, "No valid file extension.", "Jimena Loading Failed", JOptionPane.OK_OPTION
                    ^ JOptionPane.ERROR_MESSAGE);
            return;

        }

        currentFile = file;
        refreshTitle();
        relayoutWindows();
        addToLastUsed(file);
    }

    /**
     * Creates a new main window.
     */
    public Main() {
        // Set up the window
        super("Jimena - ver. "+ver);
        setIconImage(new ImageIcon("images" + File.separator + "chart16.png").getImage());
        setMinimumSize(new Dimension(900, 600));
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        network.addObserver(this);

        // Create menus
        setJMenuBar(createMenubar());
        getContentPane().add(createToolBar(), BorderLayout.PAGE_START);

        // Add graph panel
        panelGraph = new PanelGraphDrawer(network, nodesFrame, chartsFrame);

        JScrollPane scrollPaneGraph = new JScrollPane(panelGraph);
        scrollPaneGraph.getViewport().addChangeListener(new ChangeListener() {
            // Make sure that the graph is repainted after scrolling
            @Override
            public void stateChanged(ChangeEvent arg0) {
                panelGraph.repaint();
            }
        });

        getContentPane().add(scrollPaneGraph, BorderLayout.CENTER);

        // Show window
        setVisible(true);
        relayoutWindows();
    }

    /**
     * A common class for menu items that show or hide a window.
     *
     * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
     *
     */
    private class ToggleWindowMenuEntry extends JCheckBoxMenuItem {
        private static final long serialVersionUID = -52102852821756779L;

        private JFrame frame;

        public ToggleWindowMenuEntry(JFrame frame, String menuCaption) {
            super(menuCaption);
            this.frame = frame;
            // An icon would override the tick
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    ToggleWindowMenuEntry.this.frame.setVisible(!ToggleWindowMenuEntry.this.frame.isVisible());
                }
            });
        }

        // setSelected will not be called when the user clicks on the item.

        @Override
        public boolean isSelected() {
            if (frame == null) {
                return false;
            }
            return frame.isVisible();
        }
    }

    /**
     * Creates a menu bar for the main Window
     *
     * @return The menu bar
     */
    private JMenuBar createMenubar() {
        JMenuBar menubar = new JMenuBar();

        // NETWORK
        JMenu menuNetwork = new JMenu("Network");
        menuNetwork.setMnemonic(KeyEvent.VK_N);
        menubar.add(menuNetwork);

        // NETWORK|LOAD
        JMenuItem menuLoad = new JMenuItem("Load Simulation", new ImageIcon("images" + File.separator + "openfile16.png"));
        menuLoad.setMnemonic(KeyEvent.VK_L);
        menuLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Exceptions are caught by the loadFile() method

                JFileChooser filechooser = new JFileChooser();

                // Remove standard filters (JFileChooser is not completely adapted to the current locale)
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }

                filechooser.addChoosableFileFilter(jimenaFilter);

                filechooser.setDialogTitle("Open Jimena-File");
                filechooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int returnVal = filechooser.showOpenDialog(Main.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadFile(filechooser.getSelectedFile());
                }

            }
        });
        menuNetwork.add(menuLoad);

        // NETWORK|OPEN
        JMenuItem menuOpen = new JMenuItem("Import yEd File", new ImageIcon("images" + File.separator + "openfile16.png"));
        menuOpen.setMnemonic(KeyEvent.VK_O);
        menuOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Exceptions are caught by the loadFile() method
                JFileChooser filechooser = new JFileChooser();

                // Remove standard filters (JFileChooser is not completely adapted to the current locale)
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }
                filechooser.addChoosableFileFilter(graphmlFilter);

                filechooser.setDialogTitle("Open a yEd GraphML-file");
                filechooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int returnVal = filechooser.showOpenDialog(Main.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadFile(filechooser.getSelectedFile());
                }
            }
        });
        menuNetwork.add(menuOpen);

        // NETWORK|RELOAD
        JMenuItem menuReload = new JMenuItem("Reload", new ImageIcon("images" + File.separator + "refresh16.png")) {
            private static final long serialVersionUID = -730380585179996276L;

            // Cannot be influenced by overriding the isEnabled method
            @Override
            protected void paintComponent(Graphics g) {
                setEnabled(currentFile != null);
                super.paintComponent(g);
            }
        };
        menuReload.setMnemonic(KeyEvent.VK_R);
        menuReload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Exceptions are caught by the loadFile() method
                loadFile(currentFile);
                Main.this.relayoutWindows();
            }
        });
        menuNetwork.add(menuReload);

        // NETWORK|SAVE
        JMenuItem menuSave = new JMenuItem("Save Simulation", new ImageIcon("images" + File.separator + "save16.png")) {
            private static final long serialVersionUID = -730380585179996276L;

            // Cannot be influenced by overriding the isEnabled method
            @Override
            protected void paintComponent(Graphics g) {
                setEnabled(currentFile != null);
                super.paintComponent(g);
            }
        };
        menuSave.setMnemonic(KeyEvent.VK_S);
        menuSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    JFileChooser filechooser = new JFileChooser();

                    // Remove standard filters
                    for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                        filechooser.removeChoosableFileFilter(filter);
                    }
                    // Add correct filters
                    filechooser.addChoosableFileFilter(jimenaFilter);

                    filechooser.setDialogTitle("Save to Jimena-File");
                    filechooser.setDialogType(JFileChooser.SAVE_DIALOG);

                    int returnVal = filechooser.showSaveDialog(Main.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = filechooser.getSelectedFile();
                        if (!jimenaFilter.accept(filechooser.getSelectedFile())) {
                            file = new File(file.toString() + ".jimena");
                        }

                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                        objectOutputStream.writeObject(network);
                        objectOutputStream.flush();
                        objectOutputStream.close();
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Saving failed", JOptionPane.OK_OPTION ^ JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        menuNetwork.add(menuSave);

        // NETWORK|SEPARATOR

        menuNetwork.addSeparator();

        // NETWORK|LAST USED FILES

        refreshLastUsedFiles();
        menuFile1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile(new File(preferences.get("file1", "")));
            }
        });
        menuNetwork.add(menuFile1);

        menuFile2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile(new File(preferences.get("file2", "")));
            }
        });
        menuNetwork.add(menuFile2);

        menuFile3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile(new File(preferences.get("file3", "")));
            }
        });
        menuNetwork.add(menuFile3);

        menuFile4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile(new File(preferences.get("file4", "")));
            }
        });
        menuNetwork.add(menuFile4);

        // NETWORK|SEPARATOR

        menuNetwork.addSeparator();

        // NETWORK|CLOSE

        JMenuItem menuClose = new JMenuItem("Exit", new ImageIcon("images" + File.separator + "exit16.png"));
        menuClose.setMnemonic(KeyEvent.VK_E);
        menuClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);

            }
        });
        menuNetwork.add(menuClose);

        // VIEW
        JMenu menuView = new JMenu("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        menubar.add(menuView);

        // VIEW|SHOW/HIDE NODESFRAME

        menuView.add(new ToggleWindowMenuEntry(nodesFrame, "Nodes Table"));

        // VIEW|SHOW/HIDE INPUTFRAME

        menuView.add(new ToggleWindowMenuEntry(connectionsFrame, "Connections Table"));

        // VIEW|SHOW/HIDE PERTURBATIONSTABLE

        menuView.add(new ToggleWindowMenuEntry(perturbationFrame, "Perturbations Table"));

        // VIEW|SHOW/HIDE CHARTSWINDOW

        menuView.add(new ToggleWindowMenuEntry(chartsFrame, "Charts Window/Data Export"));

        // VIEW|SHOW/HIDE SETTINGSWINDOW

        menuView.add(new ToggleWindowMenuEntry(settingsFrame, "Settings Window"));

        // VIEW|SEPARATOR

        menuView.addSeparator();

        // VIEW|SHOW ALL WINDOWS

        JMenuItem menuShowAllWindows = new JMenuItem("Show All Windows", new ImageIcon("images" + File.separator + "defaultwindows16.png"));
        menuShowAllWindows.setMnemonic(KeyEvent.VK_S);
        menuShowAllWindows.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                for (Frame f : Frame.getFrames()) {
                    f.setVisible(true);
                }
            }
        });
        menuView.add(menuShowAllWindows);

        // ANALYSIS

        JMenu menuAnalysis = new JMenu("Analysis");
        menuAnalysis.setMnemonic(KeyEvent.VK_A);
        menubar.add(menuAnalysis);

        // ANALYSIS | RANDOM VALUES

        JMenuItem menuRandomValues = new JMenuItem("Random Values");
        menuRandomValues.setIcon(new ImageIcon("images" + File.separator + "random16.png"));
        menuRandomValues.setMnemonic(KeyEvent.VK_R);
        menuRandomValues.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                network.setValues(MathLib.randomVector(network.getNetworkNodes().length));
                if (!getMethod().isContinuous()) {
                    network.makeDiscrete();
                }
                network.notifyObserversOfChangedValues();
            }
        });
        menuAnalysis.add(menuRandomValues);

        // ANALYSIS | LOAD STATE

        JMenuItem menuLoadState = new JMenuItem("Load State From File", new ImageIcon("images" + File.separator + "openfile16.png"));
        menuLoadState.setMnemonic(KeyEvent.VK_L);
        menuLoadState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Exceptions are caught by the loadFile() method

                JFileChooser filechooser = new JFileChooser();

                // Remove standard filters (JFileChooser is not completely adapted to the current locale)
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }

                filechooser.addChoosableFileFilter(txtFilter);

                filechooser.setDialogTitle("Open State-File");
                filechooser.setDialogType(JFileChooser.OPEN_DIALOG);

                int returnVal = filechooser.showOpenDialog(Main.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    BufferedReader br;
                    try {
                        br = new BufferedReader(new FileReader(filechooser.getSelectedFile()));
                    } catch (FileNotFoundException e1) {
                        JOptionPane.showMessageDialog(null, "The file could not be read.", "Import Error", JOptionPane.OK_OPTION
                                ^ JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String line;
                    try {
                        while ((line = br.readLine()) != null) {
                            String[] parts = line.split("\t");
                            if (parts.length < 2) {
                                continue;
                            }
                            try {
                                network.getNodeByName(parts[0]).setValue(Double.valueOf(parts[1]));
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "The value " + parts[1] + " could not be parsed.", "Import Error",
                                        JOptionPane.OK_OPTION ^ JOptionPane.ERROR_MESSAGE);
                                br.close();
                                return;
                            } catch (IllegalArgumentException e) {
                                JOptionPane.showMessageDialog(null, "The node " + parts[0] + " could not be found.", "Import Error",
                                        JOptionPane.OK_OPTION ^ JOptionPane.ERROR_MESSAGE);
                                br.close();
                                return;
                            }
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "The file could not be read.", "Import Error", JOptionPane.OK_OPTION
                                ^ JOptionPane.ERROR_MESSAGE);
                        try {
                            br.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        return;
                    }
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        menuAnalysis.add(menuLoadState);

        // ANALYSIS | SAVE STATE

        JMenuItem menuSaveState = new JMenuItem("Save State to File", new ImageIcon("images" + File.separator + "save16.png"));
        menuSaveState.setMnemonic(KeyEvent.VK_T);
        menuSaveState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser filechooser = new JFileChooser();

                // Remove standard filters
                for (FileFilter filter : filechooser.getChoosableFileFilters()) {
                    filechooser.removeChoosableFileFilter(filter);
                }
                // Add correct filters
                filechooser.addChoosableFileFilter(txtFilter);

                filechooser.setDialogTitle("Save State to Text-File");
                filechooser.setDialogType(JFileChooser.SAVE_DIALOG);

                int returnVal = filechooser.showSaveDialog(Main.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = filechooser.getSelectedFile();
                    if (!txtFilter.accept(filechooser.getSelectedFile())) {
                        file = new File(file.toString() + ".txt");
                    }

                    try {
                        StringBuilder save = new StringBuilder();
                        String[] names = network.getNodeNames();
                        double[] values = network.getValues();

                        for (int i = 0; i < names.length; i++) {
                            save.append(names[i] + "\t" + values[i]);
                            if (i != names.length - 1) {
                                save.append("\n");
                            }
                        }

                        PrintWriter printStream = new PrintWriter(file);
                        printStream.print(save);
                        printStream.flush();
                        printStream.close();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "The file could not be written.", "Export Error", JOptionPane.OK_OPTION
                                ^ JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });
        menuAnalysis.add(menuSaveState);

        // ANALYSIS | STEADY STATE

        JMenuItem menuSteadyState = new JMenuItem("Find Stable State From Current State");
        menuSteadyState.setIcon(new ImageIcon("images" + File.separator + "simulatetime16.png"));
        menuSteadyState.setMnemonic(KeyEvent.VK_F);
        menuSteadyState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new SSSFromCurrentValuesThread().start();
            }
        });
        menuAnalysis.add(menuSteadyState);

        // ANALYSIS | SEPARATOR

        menuAnalysis.addSeparator();

        // ANALYSIS | STEADY STATES
        JMenuItem menuSteadyStates = new JMenuItem("Find Stable States");
        menuSteadyStates.setIcon(new ImageIcon("images" + File.separator + "table16.png"));
        menuSteadyStates.setMnemonic(KeyEvent.VK_A);
        menuSteadyStates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                (new FindSSSThread()).start();
            }
        });
        menuAnalysis.add(menuSteadyStates);

        // ANALYSIS | DISCRETE STABLE STEADY STATES

        JMenuItem menuDiscreteStableSteadyStates = new JMenuItem("Discrete Stable States");
        menuDiscreteStableSteadyStates.setIcon(new ImageIcon("images" + File.separator + "table16.png"));
        menuDiscreteStableSteadyStates.setMnemonic(KeyEvent.VK_D);
        menuDiscreteStableSteadyStates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                new DiscreteSSSThread().start();
            }
        });
        menuAnalysis.add(menuDiscreteStableSteadyStates);

        // ANALYSIS | SEPARATOR

        menuAnalysis.addSeparator();

        JMenuItem menuNodeCentrality = new JMenuItem("Node centralities");
        menuNodeCentrality.setIcon(new ImageIcon("images" + File.separator + "allnodes.png"));
        menuNodeCentrality.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                (new AllNodeCentralities()).start();
            }
        });
         menuAnalysis.add(menuNodeCentrality);

        JMenuItem menuConnectionCentrality = new JMenuItem("Connection centralities");
        menuConnectionCentrality.setIcon(new ImageIcon("images" + File.separator + "allconnections.png"));
        menuConnectionCentrality.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                (new AllConnectionCentralities()).start();
            }
        });
         menuAnalysis.add(menuConnectionCentrality);
         
         //changes for D2D start
         // ANALYSIS | SEPARATOR
         menuAnalysis.addSeparator();
         
         // ANALYSIS | D2D
         JMenuItem menuD2D = new JMenuItem("D2D");
         menuD2D.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 runD2D();
             }
         });
         menuAnalysis.add(menuD2D);
         
         // ANALYSIS | D2D External Stimuli
         JMenuItem menuD2DExternalStimuli = new JMenuItem("D2D ExternalStimuli");
         menuD2DExternalStimuli.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 runD2DExternalStimuli();
             }
         });
         menuAnalysis.add(menuD2DExternalStimuli);
         //changes for D2D end

        // HELP
        JMenu menuHelp = new JMenu("Help");
        menuHelp.setMnemonic(KeyEvent.VK_H);
        menubar.add(menuHelp);

        // HELP | GUI TUTORIAL

        JMenuItem menuGuiTutorial = new JMenuItem("GUI tutorial", new ImageIcon("images" + File.separator + "help16.png"));
        menuGuiTutorial.setMnemonic(KeyEvent.VK_G);
        menuGuiTutorial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (!java.awt.Desktop.isDesktopSupported()
                            || !java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                        JOptionPane.showMessageDialog(Main.this, "Your internet browser could not be started automatically.\n\n"
                                + " Please open the web page http://stefan-karl.de/jimena/guitutorial.php",
                                "Internet browser could not be started", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    java.awt.Desktop.getDesktop().browse(new URI("http://stefan-karl.de/jimena/guitutorial.php"));
                } catch (IOException e) {
                    // Does not happen
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    // Does not happen
                    e.printStackTrace();
                }

            }
        });
        menuHelp.add(menuGuiTutorial);

        // VIEW|SEPARATOR

        menuHelp.addSeparator();

        // HELP | ABOUT JIMENA

        JMenuItem menuAbout = new JMenuItem("About Jimena", new ImageIcon("images" + File.separator + "about16.png"));
        menuAbout.setMnemonic(KeyEvent.VK_A);
        menuAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(Main.this, "Jimena is developed by\n" + "\n"
                        + "Chunguang Liang and Stefan Karl\nDepartment of Bioinformatics, University of Würzburg\n"
                        + "liang[at]biozentrum.uni-wuerzburg.de\n\n"
                        + "The project is supervised by Thomas Dandekar, head of the Department of Bioinformatics\n\n"
                        + "Acknowledgements:\n"
                        + "JavaBDD (GNU LGPL) by John Whaley (http://javabdd.sourceforge.net) is used as a BDD framework\n"
                        + "Icons (GNU LGPL) by Oxygen Team (http://www.iconarchive.com/artist/oxygen-icons.org.html)", "About Jimena",
                        JOptionPane.OK_OPTION, new ImageIcon("images" + File.separator + "chart72.png"));
            }
        });
        menuHelp.add(menuAbout);

        return menubar;
    }
    
    /**
     * Opens the D2DFrame
     */
    private void runD2D() {    	
    	D2DFrame f = null;
		try {
			f = new D2DFrame(currentFile);
		} catch (Exception e1) {			
			e1.printStackTrace();
		} 
    	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	f.pack();
    	f.setLocationRelativeTo(null); 
    	f.setVisible(true);    	    	
    }
    
    /**
     * Opens the D2DExternalStimuliFrame
     */
    private void runD2DExternalStimuli() {    	
    	D2DExternalStimuliFrame f = null;
		try {
			f = new D2DExternalStimuliFrame(network);
		} catch (Exception e1) {			
			e1.printStackTrace();
		} 
    	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	f.pack();
    	f.setLocationRelativeTo(null); 
    	f.setVisible(true);    	    	
    }

    /**
     * Refreshes the title of the window with information from the current network.
     */
    private void refreshTitle() {
        String fileName = currentFile != null ? currentFile.getName() : "No File Loaded";
        Main.this.setTitle("Jimena - " + fileName + " - Time: "
                + StandardNumberFormat.NUMBERFORMAT.format(network.getTimeIndex().getValue()));
    }

    /**
     * Creates the toolbar for the window.
     *
     * @return The toolbar of the window
     */
    private JToolBar createToolBar() {
        // Set up the toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        toolbar.setRollover(true);
        toolbar.setPreferredSize(new Dimension(80, 33));
        toolbar.setFloatable(false);

        // SIMULATION METHOD

        JLabel labelMethod = new JLabel("Simulation Method:");
        toolbar.add(labelMethod);

        comboMethods = new JComboBox<SimulationMethod>();
        for (SimulationMethod method : simulationMethods) {
            comboMethods.addItem(method);
        }
        comboMethods.setToolTipText("If you chose a discrete simulation method Simulated Time/dt steps will be simulated");
        comboMethods.setFocusable(false);
        toolbar.add(comboMethods);

        // SIMULATION TIME

        JLabel labelMaxt = new JLabel("Simulated Time: ");
        toolbar.add(labelMaxt);

        textmaxt = new JFormattedTextField(StandardNumberFormat.NUMBERFORMAT);
        textmaxt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
                try {
                    if (Double.valueOf(textmaxt.getText()) < MINMAXT) {
                        textmaxt.setText(String.valueOf(MINMAXT));
                    }
                } catch (NumberFormatException e) {
                    // Do nothing, the field will be reset by the JFormattedTextField
                }
            }
        });
        textmaxt.setToolTipText("This limit applies to the time in the simulation, not the time of the calculation.");
        textmaxt.setText("50");
        textmaxt.setPreferredSize(new Dimension(40, 20));
        toolbar.add(textmaxt);

        // DT

        JLabel labelDt = new JLabel("dt: ");
        toolbar.add(labelDt);

        textDt = new JFormattedTextField(StandardNumberFormat.NUMBERFORMAT);
        textDt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
                try {
                    if (Double.valueOf(textDt.getText()) < MINDT) {
                        textDt.setText(String.valueOf(MINDT));
                    }
                } catch (NumberFormatException e) {
                    // Do nothing, the field will be reset by the JFormattedTextField
                }
            }
        });
        textDt.setText("0.1");
        textDt.setPreferredSize(new Dimension(80, 20));
        toolbar.add(textDt);

        // MAXIMUM SPEED

        JLabel labelMaxSpeed = new JLabel("Max Speed: ");
        toolbar.add(labelMaxSpeed);

        textMaxSpeed = new JFormattedTextField(StandardNumberFormat.NUMBERFORMAT);
        textMaxSpeed.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
                try {
                    if (Double.valueOf(textMaxSpeed.getText()) < MINSPEED && Double.valueOf(textMaxSpeed.getText()) != 0) {
                        textMaxSpeed.setText(String.valueOf(MINSPEED));
                    }
                } catch (NumberFormatException e) {
                    // Do nothing, the field will be reset by the JFormattedTextField
                }
            }
        });
        textMaxSpeed.setToolTipText("Enter '0' for unlimited speed.");
        textMaxSpeed.setText("1");
        textMaxSpeed.setPreferredSize(new Dimension(60, 20));
        toolbar.add(textMaxSpeed);

        // SINGLE STEP

        JButton buttonSingleStep = new JButton(new ImageIcon("images" + File.separator + "singlestep16.png"));
        buttonSingleStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SimulateThread(true).start();
            }
        });
        // Has to be focusable to ensure that the TextField lose the focus and are checked.
        buttonSingleStep.setFocusable(true);
        buttonSingleStep.setBorderPainted(false);
        buttonSingleStep.setToolTipText("Single Step");
        toolbar.add(buttonSingleStep);

        // SIMULATE

        JButton buttonSimulate = new JButton(new ImageIcon("images" + File.separator + "simulatetime16.png"));
        buttonSimulate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SimulateThread(false).start();
            }
        });
        // Has to be focusable to ensure that the TextField lose the focus and are checked.
        buttonSimulate.setFocusable(true);
        buttonSimulate.setBorderPainted(false);
        buttonSimulate.setToolTipText("Simulate");
        toolbar.add(buttonSimulate);

        // RESET

        JButton buttonReset = new JButton(new ImageIcon("images" + File.separator + "revert16.png"));
        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                network.reset();
            }
        });
        // Has to be focusable to ensure that the TextField lose the focus and are checked.
        buttonReset.setFocusable(true);
        buttonReset.setBorderPainted(false);
        buttonReset.setToolTipText("Reset Simulation");
        toolbar.add(buttonReset);

        return toolbar;
    }

    /**
     * Returns a checked dt taken from the GUI.
     *
     * @return Checked dt
     */
    private double getDt() {
        return Double.valueOf(textDt.getText());
    }

    /**
     * Returns a checked maxT taken from the GUI.
     *
     * @return Checked maxT
     */
    private double getMaxt() {
        return Double.valueOf(textmaxt.getText());
    }

    /**
     * Returns a checked max speed taken from the GUI.
     *
     * @return Checked max speed
     */
    private double getMaxSpeed() {
        double maxSpeed = Double.valueOf(textMaxSpeed.getText());

        if (maxSpeed == 0) {
            maxSpeed = Double.MAX_VALUE;
        }

        return maxSpeed;
    }

    /**
     * Returns a checked simulation method taken from the GUI.
     *
     * @return Checked simulation method
     */
    private SimulationMethod getMethod() {
        return (SimulationMethod) Main.this.comboMethods.getSelectedItem();
    }

    /**
     * Starts the application.
     *
     * @param args
     *            Command line parameters (not used)
     */
    public static void main(String[] args) {
        // http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Startup failed", JOptionPane.OK_OPTION ^ JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        Locale.setDefault(Locale.US);

        new Main();
    }

    /**
     * Sets initial positions for the windows of the applications.
     */
    public void relayoutWindows() {
        pack();

        int curX = (int) this.getLocation().getX();
        int curY = (int) this.getLocation().getY();
        for (Frame f : Frame.getFrames()) {
            if (f == this) {
                continue;
            }

            curX += 20;
            curY += 20;
            f.setLocation(curX, curY);
        }
    }

    /**
     * Performs a simulation with the values entered in the GUI.
     */
    public void lockGUI() {
        // Changes must be reversed in the notifySimulationEnded method
        for (Frame f : Frame.getFrames()) {
            recursiveSetEnabled(f, false);
        }
    }

    /**
     * Recursively sets the enabled property in a tree of Components/Containers
     *
     * @param c
     *            The root container of the tree
     * @param value
     *            The value to set
     */
    private static void recursiveSetEnabled(Container c, boolean value) {
        for (Component cc : c.getComponents()) {
            cc.setEnabled(value);
            if (cc instanceof Container) {
                if (((Container) cc).getComponents().length != 0) {
                    recursiveSetEnabled((Container) cc, value);
                }
            }
        }
    }

    /**
     * Locks the whole GUI.
     */
    public void unlockGUI() {
        for (Frame f : Frame.getFrames()) {
            recursiveSetEnabled(f, true);
        }
    }

    /**
     * Adds a file to the last used files.
     *
     * @param file
     *            The file to add
     */
    private void addToLastUsed(File file) {
        if (preferences.get("file1", "").equals(file.getPath())) {
            // Do nothing
        } else if (preferences.get("file2", "").equals(file.getPath())) {
            preferences.put("file2", preferences.get("file1", ""));
            preferences.put("file1", file.getPath());
        } else if (preferences.get("file3", "").equals(file.getPath())) {
            preferences.put("file3", preferences.get("file2", ""));
            preferences.put("file2", preferences.get("file1", ""));
            preferences.put("file1", file.getPath());
        } else {
            preferences.put("file4", preferences.get("file3", ""));
            preferences.put("file3", preferences.get("file2", ""));
            preferences.put("file2", preferences.get("file1", ""));
            preferences.put("file1", file.getPath());
        }
        refreshLastUsedFiles();
    }

    /**
     * Refreshes the last used files in the menu.
     */
    private void refreshLastUsedFiles() {
        menuFile1.setText("1 " + (new File(preferences.get("file1", ""))).getName());
        menuFile1.setVisible(!preferences.get("file1", "").equals(""));
        menuFile2.setText("2 " + (new File(preferences.get("file2", ""))).getName());
        menuFile2.setVisible(!preferences.get("file2", "").equals(""));
        menuFile3.setText("3 " + (new File(preferences.get("file3", ""))).getName());
        menuFile3.setVisible(!preferences.get("file3", "").equals(""));
        menuFile4.setText("4 " + (new File(preferences.get("file4", ""))).getName());
        menuFile4.setVisible(!preferences.get("file4", "").equals(""));
    }

    @Override
    public void notifyNetworkChanged() {
        notifyValuesChanged();
    }

    @Override
    public void notifyValuesChanged() {
        refreshTitle();
    }
}
