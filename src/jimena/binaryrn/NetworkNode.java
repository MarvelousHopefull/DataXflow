package jimena.binaryrn;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

import jimena.binarybf.BinaryBooleanFunction;
import jimena.customfunction.CustomFunction;
import jimena.libs.DoubleValue;
import jimena.libs.MathLib;
import jimena.perturbation.Perturbation;

/**
 * Implements a node in the regulatory network. Its boolean function and the connections are immutable, the simulation parameters are not.
 *
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 *
 */
public class NetworkNode implements Serializable {
    public final double STDODEFYDECAY = 1;
    public final double STDSQUADDECAY = 1;
    public final double STDSQUADSTEEPNESS = 10;
    public final double STDSQUADWEIGHT = 1;
    public final double STDHILLN = 2;
    public final double STDHILLK = 0.5;

    private CustomFunction customFunction = null;

    private static final long serialVersionUID = -759500325019694525L;
    private String name;
    private double value;
    private double odefyDecay = STDODEFYDECAY;
    private double squadDecay = STDSQUADDECAY;
    private double squadSteepness = STDSQUADSTEEPNESS;
    private double expHalfSquadSteepness;
    private BinaryBooleanFunction function;
    private Connection[] connections;
    private Rectangle2D.Double rectangle;
    private boolean hillNormalize = true;
    private double[] hillNs;
    private double[] hillKs;
    private double[] squadWeights;
    private double initialValue;
    private LinkedList<Perturbation> perturbations = new LinkedList<Perturbation>();
    private DoubleValue timeIndex;
    // An ArrayList might seem strange for a growing list, but its simply faster
    private ArrayList<Point2D.Double> log = new ArrayList<Point2D.Double>();

    /**
     * Creates a log entry with the current time index and the current value.
     */
    protected void log() {
        log.add(new Point2D.Double(timeIndex.getValue(), getValue()));
    }

    /**
     * Resets the log of this node.
     */
    protected void resetLog() {
        log = new ArrayList<Point2D.Double>();
    }

    /**
     * Returns the log of this node.
     *
     * @return Logged data points
     */
    public ArrayList<Point2D.Double> getLog() {
        return log;
    }

    /**
     * Sets a custom function for this node to override BooleCube, SQUAD etc. functions.
     *
     * @param customFunction
     */
    public void setCustomFunction(CustomFunction customFunction) {
        this.customFunction = customFunction;
    }

    /**
     * Returns the custom function of this node or null if none exists.
     *
     * @return The custom function of the node.
     */
    public CustomFunction getCustomFunction() {
        return customFunction;
    }

    /**
     * Return the current weights of the inputs to the SQUAD function. The values may be changed by editing the array.
     *
     * @return Weights of the inputs
     */
    public double[] getSQUADWeights() {
        return squadWeights;
    }

    /**
     * Sets the weight of the inputs in the SQUAD model.
     *
     * PRIVATE function, use getSQUADWeights to set the values instead.
     *
     * @param squadWeights
     *            The SQUAD weights to set
     */
    private void setSQUADWeights(double[] squadWeights) {
        if (squadWeights == null) {
            throw new NullPointerException();
        }
        if (squadWeights.length != this.squadWeights.length) {
            throw new IllegalArgumentException("The number of SQUAD weigts to set must match the number of inputs to the node.");
        }
        for (double weight : squadWeights) {
            if (weight <= 0) {
                throw new IllegalArgumentException("The SQUAD weight must be greater than 0.");
            }
        }
        this.squadWeights = squadWeights;
    }

    /**
     * Returns the n parameters of the hill function. The values may be changed by editing the array.
     *
     * @return n parameters
     */
    public double[] getHillNs() {
        return hillNs;
    }

    /**
     * Returns the k parameters of the hill function. The values may be changed by editing the array.
     *
     * @return k parameters
     */
    public double[] getHillKs() {
        return hillKs;
    }

    /**
     * @param name
     *            Name of the node
     * @param value
     *            Current value of the node
     * @param odefyDecay
     *            Decay in Odefy-like simulations
     * @param squadDecay
     *            Decay in SQUAD-like simulations
     * @param squadSteepness
     *            SQUAD steepness parameter
     * @param function
     *            The boolean function of the node
     * @param connections
     *            The connection to the Boolean function
     * @param rectangle
     *            The position of the node
     * @param hillNormalize
     *            Whether the hill function will be normalized
     * @param hillNs
     *            N parameters of the hill function
     * @param hillKs
     *            K parameters of the hill function
     * @param squadWeights
     *            Weights of the inputs in a SQUAD-like simulation
     * @param initialValue
     *            The initial value of the node
     * @param perturbations
     *            Perturbations of the node
     * @param timeIndex
     *            Time index of the node
     * @param log
     */
    public NetworkNode(String name, double value, double odefyDecay, double squadDecay, double squadSteepness,
            BinaryBooleanFunction function, Connection[] connections, Rectangle2D.Double rectangle, boolean hillNormalize, double[] hillNs,
            double[] hillKs, double[] squadWeights, double initialValue, LinkedList<Perturbation> perturbations, DoubleValue timeIndex,
            ArrayList<Point2D.Double> log, CustomFunction customFunction) {
        this(name, function, connections, rectangle, initialValue, timeIndex);
        this.setValue(MathLib.limitToRangeAndAssertNumerical(value, 0, 1));
        this.odefyDecay = MathLib.limitToRangeAndAssertNumerical(odefyDecay, 0, Double.MAX_VALUE);
        this.squadDecay = MathLib.limitToRangeAndAssertNumerical(squadDecay, 0, Double.MAX_VALUE);
        setSQUADSteepness(MathLib.limitToRangeAndAssertNumerical(squadSteepness, 0, Double.MAX_VALUE));
        this.hillNormalize = hillNormalize;
        if (hillNs == null || hillKs == null || perturbations == null || log == null) {
            throw new NullPointerException();
        }
        if (hillNs.length != connections.length || hillKs.length != connections.length) {
            throw new IllegalArgumentException("The number of parameters must match the number of connections.");
        }
        this.hillNs = hillNs;
        this.hillKs = hillKs;
        setSQUADWeights(squadWeights);
        this.perturbations = perturbations;
        this.log = log;
        this.customFunction = customFunction;
    }

    /**
     * Creates a new node in a binary regulatory network. All parameters are set to standard values and no perturbations are created.
     *
     * @param name
     *            Name of the node
     * @param function
     *            The boolean function of the node
     * @param connections
     *            The numbers of the connections to the node
     * @param rectangle
     *            The position of the node
     * @param initialValue
     *            The initial value of the node
     * @param timeIndex
     *            Time index of the node
     */
    public NetworkNode(String name, BinaryBooleanFunction function, Connection[] connections, Rectangle2D.Double rectangle,
            double initialValue, DoubleValue timeIndex) {
        if (name == null || function == null || connections == null || rectangle == null || timeIndex == null) {
            throw new NullPointerException();
        }

        for (Connection i : connections) {
            if (i == null) {
                throw new NullPointerException();
            }
        }

        if (function.getArity() != connections.length) {
            throw new IllegalArgumentException("Arity of the function must correspond to the number of connections.");
        }

        this.name = name;
        this.function = function;
        this.connections = connections;
        this.rectangle = rectangle;

        this.initialValue = MathLib.limitToRangeAndAssertNumerical(initialValue, 0, 1);

        setValue(initialValue);
        this.timeIndex = timeIndex;

        setSQUADSteepness(STDSQUADSTEEPNESS);

        squadWeights = new double[connections.length];
        hillNs = new double[connections.length];
        hillKs = new double[connections.length];

        for (int i = 0; i < connections.length; i++) {
            squadWeights[i] = STDSQUADWEIGHT;
            hillNs[i] = STDHILLN;
            hillKs[i] = STDHILLK;
        }
    }

    /**
     * Returns the name of this node.
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the rectangle of this node.
     *
     * @return The rectangle
     */
    public Rectangle2D.Double getRectangle() {
        return rectangle;
    }

    /**
     * Return the boolean function of this node.
     *
     * @return The boolean function
     */
    public BinaryBooleanFunction getFunction() {
        return function;
    }

    /**
     * Return the connections of this node.
     *
     * @return The connections
     */
    public Connection[] getConnections() {
        return connections;
    }

    /**
     * Return the currently used odefyDecays.
     *
     * @return The odefy decays
     */
    public double getOdefyDecay() {
        return odefyDecay;
    }

    /**
     * Sets the odefyDecay. Default is 1.
     *
     * @param odefyDecay
     *            The odefy decays to set
     */
    public void setOdefyDecay(double odefyDecay) {
        this.odefyDecay = odefyDecay;
    }

    /**
     * Return the currently used Math.exp(0.5*h).
     *
     * @return The squad steepness
     */
    public double getExpHalfSQUADSteepness() {
        return expHalfSquadSteepness;
    }

    /**
     * Return the currently used squad steepness.
     *
     * @return The squad steepness
     */
    public double getSQUADSteepness() {
        return squadSteepness;
    }

    /**
     * Sets the squad steepness. Default is 10.
     *
     * @param squadSteepness
     *            The squad steepness to set
     */
    public void setSQUADSteepness(double squadSteepness) {
        this.squadSteepness = squadSteepness;
        this.expHalfSquadSteepness = Math.exp(0.5 * squadSteepness);
    }

    /**
     * Return the currently used squad decay.
     *
     * @return The squad decay
     */
    public double getSQUADDecay() {
        return squadDecay;
    }

    /**
     * Sets the squad decay. Default is 1.
     *
     * @param squadDecay
     *            The squad decay to set
     */
    public void setSQUADDecay(double squadDecay) {
        this.squadDecay = squadDecay;
    }

    /**
     * Returns whether the nodes hill function will be normalized if the interpolation uses a hill function.
     *
     * @return True if it will be normalized
     */
    public boolean getHillNormalize() {
        return hillNormalize;
    }

    /**
     * Set whether the nodes hill function will be normalized if the interpolation uses a hill function.
     *
     * @param hillNormalize
     *            True if it will be normalized
     */
    public void setHillNormalize(boolean hillNormalize) {
        this.hillNormalize = hillNormalize;
    }

    /**
     * Returns the initial value of the node
     *
     * @return Initial value of the node
     */
    public double getInitialValue() {
        return initialValue;
    }

    /**
     * Returns the value of the node.
     *
     * @return The value
     */
    public double getValue() {
        return applyPerturbations(value, timeIndex.getValue());
    }

    /**
     * Returns the value of the node if the current value was the given value at the given time index. This method applies the Perturbations
     * of the node to the given value.
     *
     * @param value
     *            The value to assume
     * @param t
     *            The time index to assume
     * @return The value
     */
    public double applyPerturbations(double value, double t) {
        if (perturbations.size() == 0) {
            return value;
        }

        double pertubationsValue = getPerturbationsValue(t);

        if (MathLib.isNaN(pertubationsValue)) {
            return value;
        } else {
            return pertubationsValue;
        }
    }

    /**
     * Sets the value of the node.
     *
     * @param value
     *            The value to set
     */
    public void setValue(Double value) {
        this.value = MathLib.limitToRangeAndAssertNumerical(value, 0, 1);
    }

    /**
     * Returns the value of the perturbations at a given Time.
     *
     * @param t
     *            The time to assume
     *
     * @return The combined value of the perturbations or a value < 0 if none are active.
     */
    private double getPerturbationsValue(double t) {
        int active = 0;
        double sum = 0;
        for (Perturbation perturbation : perturbations) {
            double value = perturbation.getValue(t);
            if (!(value < 0)) {
                sum += value;
                active++;
            }
        }

        if (active == 0) {
            return Double.NaN;
        }

        return sum / active;
    }

    /**
     * Returns the perturbations of this node.
     *
     * @return A LinkedList of the perturbations of this node
     */
    public LinkedList<Perturbation> getPerturbations() {
        return perturbations;
    }

    /**
     * Resets the Node to its initial value and reset the log.
     */
    public void reset() {
        setValue(initialValue);
        resetLog();
    }

    @Override
    public String toString() {
        // This is used in several parts of the GUI and may not be changed
        return name;
    }

    /**
     * Creates a deep copy of this node.
     *
     * @param timeIndex
     *            The time index of the copied node.
     * @param includeLogs
     *            Whether to include the logs in the copy
     * @return The cloned Node.
     */
    public NetworkNode clone(DoubleValue timeIndex, boolean includeLogs) {
        Connection[] copiedConnections = new Connection[connections.length];
        for (int i = 0; i < connections.length; i++) {
            copiedConnections[i] = connections[i].clone();
        }

        LinkedList<Perturbation> copiedPerturbations = new LinkedList<Perturbation>();
        for (Perturbation perturbation : perturbations) {
            copiedPerturbations.add(perturbation.clone());
        }

        ArrayList<Point2D.Double> copiedLog = new ArrayList<Point2D.Double>();
        if (includeLogs) {
            for (Point2D.Double logEntry : log) {
                copiedLog.add((java.awt.geom.Point2D.Double) logEntry.clone());
            }
        }

        return new NetworkNode(name, value, odefyDecay, squadDecay, squadSteepness, function.clone(), copiedConnections,
                (Rectangle2D.Double) rectangle.clone(), hillNormalize, hillNs.clone(), hillKs.clone(), squadWeights.clone(), initialValue,
                copiedPerturbations, timeIndex.clone(), copiedLog, customFunction);
    }

    /**
     * Creates a deep copy of this node without logs and NO new time index.
     *
     * @return The cloned Node.
     */
    @Override
    public NetworkNode clone() {
        return clone(this.timeIndex, false);
    }

    /**
     * Returns the set of the indexes of all input nodes to the node.
     *
     * @return A TreeSet of the input's indexes
     */
    public TreeSet<Integer> getConnectionSources() {
        TreeSet<Integer> result = new TreeSet<Integer>();
        for (Connection connection : connections) {
            result.add(connection.getSource());
        }
        return result;
    }

    /**
     * The value of the node is set to 0 if it is < 0.5 and to 1 otherwise.
     */
    public void makeDiscrete() {
        if (value < 0.5) {
            value = 0;
        } else {
            value = 1;
        }
    }

    /**
     * Sets the name of the node. Be careful not the set two different nodes to the same name.
     *
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name may not be null");
        }
        this.name = name;
    }

    public void setFunction(BinaryBooleanFunction function, Connection[] connections) {
        if (function == null || connections == null || function.getArity() != connections.length) {
            throw new IllegalArgumentException("Not a valid function.");
        }
        for (Connection connection : connections) {
            if (connection == null || connection.getPath() == null || connection.getPathOrigin() == null
                    || connection.getPathTarget() == null || connection.getSource() < 0) {
                throw new IllegalArgumentException("Not a valid function.");
            }
        }
        this.function = function;
        this.connections = connections;
    }
}
